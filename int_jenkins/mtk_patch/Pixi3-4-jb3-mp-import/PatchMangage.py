#!/usr/bin/python
#coding=utf-8

import os
import re
import sys
import getpass
from commands import *
import commands
from time import *
import pexpect
import smtplib
import MySQLdb
from email.header import Header
from email.MIMEText import MIMEText
sys.path.append('/local/int_jenkins/lib')
sys.path.append('/local/int_jenkins/mtk_patch/lib')
import xlrd
import string
from CheckFile import *
from dotProjectDb import *
from checkPatchInfor import *


class PatchMerge(CheckFile,dotProjectDb,checkPatchInfor):
    def __init__(self,basedir='/local'):
	self.repopath = '/local/tools/repo/repo'
        os.chdir(basedir)
        self.__baseDir = os.getcwd()		
        self.__debug = False
        self.__pushCodeToGit = True
	self.sharefoldname = 'tmp-patch'
        wb = xlrd.open_workbook('%s/Pixi3-4-jb3-mp-import.xls' % getoutput("dirname %s" % (sys.argv[0])))
        mtkSheet = wb.sheet_by_name(u'MTKInfo')
        self.__branch = mtkSheet.cell(1, 0).value.strip()
        self.__project = mtkSheet.cell(1, 1).value.strip()
        self.__patchFormat = mtkSheet.cell(1, 2).value.strip()
        self.__mailServer = mtkSheet.cell(1, 3).value.strip()
        self.__mailTitle = mtkSheet.cell(1, 4).value.strip()
        self.__mailAccount = mtkSheet.cell(1, 5).value.strip()
        self.__mailSender = mtkSheet.cell(1, 6).value.strip()
        self.__mailToList = mtkSheet.cell(1, 7).value.strip().split(',')
        self.__mailccList = mtkSheet.cell(1, 8).value.strip().split(',')
        self.__SPMList = mtkSheet.cell(1, 9).value.strip().split(',')
        self.__importCode = "/" + "local" + "/mtk_patch_import" +  "/" + self.__branch
        self.__mergeDone = "/" + "local" + "/mtk_patch_import" + "/mergeDone"
        self.__ignorePatch = "/" + "local" + "/mtk_patch_import" + "/ignorePatch"
        self.__ongoingPatch = "/" + "local" + "/mtk_patch_import" + "/TODO"
        self.__mailDir = "/" + "local" + "/mtk_patch_import" + "/Mail"
	self.__start = "/" + "local" + "/mtk_patch_import" + "/start"
	self.checkFileDict = self.getAffectFileDict()
	self.projectList = self.checkProjectName()
	self.devCodeBranch = []
	self.devCodeProjectIDList = []
	self.SPMIDList = []
	self.SPMEmailList = []
	self.importIdDict = {}
	self.projectid_codeBranch_Dict = {}
	self.patch_type = ''
	self.vnum = ''
	self.pnum = ''
	self.eservice_ID = ''
	self.description = ''
	self.untardir = ''
	self.get_db_connection()
	self.devCodeProjectIDList = self.getProjectIDFromImportBranch(self.__branch)
	print self.devCodeProjectIDList
	self.SPMIDList = self.getContactIDList(self.devCodeProjectIDList)
	print "self.SPMIDList",self.SPMIDList
	self.SPMEmailList = self.getSpmEmailList(self.SPMIDList)
	print "self.SPMEmailList",self.SPMEmailList
	self.devCodeBranch = self.getDevBranchNameFromIProjectID(self.devCodeProjectIDList,self.projectid_codeBranch_Dict)
        if not os.path.isdir(self.__start):
            os.system('mkdir -p %s' % self.__start)


        print "baseDir = %s\nbranch = %s\nproject = %s\npatchFormat = %s\nmailServer = %s\nmailTitle = %s\nmailAccount = %s\nmailSender = %s\nongoingPatch = %s" % (self.__baseDir,self.__branch, self.__project, self.__patchFormat, self.__mailServer, self.__mailTitle, self.__mailAccount,self.__mailSender, self.__ongoingPatch)
        print "mailToList = %s\nmailccList = %s\nSPMList = %s\n" % (self.__mailToList, self.__mailccList, self.__SPMList)
		
    def MergePatch(self):
        print "Please create file(start) on /local/mtk_patch_import directory. If not exist, the script will stop and exit."

        while (os.path.exists("/" + "local" + "/mtk_patch_import" + "/start") == True):
            self.__reset()
            patchNo = self.__getNextPatchNo()
            print "getNextPatchNo %s" % patchNo
            patchFilename = self.__takePatch(patchNo)
            if 'Nofile' == getNextPatchNo:
                print "Wait next patch, patchNo = %s" % patchNo
                continue
            prevMD5 = getoutput('rm -rf /tmp/tempfile; %s forall -c "git add .; git status" >> /tmp/tempfile; md5sum /tmp/tempfile' % self.repopath).split()[0]
            if sys.argv[5].lower() == "yes" and 'Success' == self.__compile():
                print "Compile success"
                self.__reset()
                patchFilename = self.__takePatch(patchNo)
                comment = 'porting P%s_%s' %(patchNo, patchFilename.replace('(', '_').replace(')', '_'))
                print comment
                if True == self.__pushCodeToGit:
                    print 'start merge.....'
                    os.system('%s forall -c git add .' % self.repopath)
                    nextMD5 = getoutput('rm -rf /tmp/tempfile; %s forall -c "git status" >> /tmp/tempfile; md5sum /tmp/tempfile' % self.repopath).split()[0]
                    if prevMD5 == nextMD5:
                        os.system('%s forall -c git commit -m "%s"; %s forall -c git push jgs HEAD:refs/heads/%s' % (self.repopath,comment,self.repopath,self.__branch))
                        self.__movePatchToMergeDone(patchNo)
                    print 'finish merge....'
                #self.makePatchMail(patchNo)
            elif sys.argv[5].lower() == "no":
		print "You choice not to build the project"
                comment = 'porting P%s_%s' %(patchNo, patchFilename.replace('(', '_').replace(')', '_'))
                print comment
                if True == self.__pushCodeToGit:
                    print 'start merge.....'
                    os.system('%s forall -c git add .' % self.repopath)
                    os.system('%s forall -c git commit -m "%s"; %s forall -c git push jgs HEAD:refs/heads/%s' % (self.repopath,comment,self.repopath,self.__branch))
                    self.__movePatchToMergeDone(patchNo)
            else:
                print "Compile failed"
                mailBody = []
                mailBody.append('Compile Error: patchNo = %s' % patchNo)
                self.__sendMail(patchNo, mailBody)
    
    def MergeOnePatch(self,patchtype,patchNo=-1):
        print "Now start to repo code"
        self.RepoCode(self.__importCode, self.__branch)
        print "Please create file(start) on /local/mtk_patch_import directory. If not exist, the script will stop and exit."
        '''parameter patchNo is 0,1,2,3...'''
        if int(patchNo) == -1:
            print "Parameter patchNo(%s) is error." % patchNo
            return
        if (os.path.exists("/" + "local" + "/mtk_patch_import"+"/start") == True):
            print "Now start to get import code from server"
            self.__reset()
            print "patchNo %s" % patchNo
            patchFilename = self.__takePatch(patchNo)
            if 'Nofile' == patchFilename:
                print "No p%s patch. Please copy the correct patch to TODO directory" % patchNo
                sys.exit(1)

            self.patch_type,self.vnum,self.pnum,self.eservice_ID = self.getMtkPatchInfor(patchFilename) 
            self.description = self.getDescriptionFromPatchListFile(self.__importCode, self.untardir,self.eservice_ID)
            print "description",self.description	        
            self.insertAllImportInfoTO_importSheet(self.devCodeProjectIDList,self.__branch,self.patch_type,self.vnum,self.pnum,self.eservice_ID,self.description,patchtype)

                    
            comment = 'porting P%s_%s' %(patchNo, patchFilename.replace('(', '_').replace(')', '_'))
            tagname = 'porting_mtk_patch_%s' % patchFilename.replace('(', '_').replace(')', '_')
            tagcomment = 'porting mtk patch P%s' % patchNo
            print "commit comment is %s" % comment
            print "tagname is %s" % tagname
            print "tagcomment is %s" % tagcomment
            if True == self.__pushCodeToGit :
		print 'start merge.....'
		os.system('%s forall -c git add -A' % self.repopath)
		#os.system('%s forall -c git commit -m "%s"; %s forall -c git push jgs HEAD:refs/heads/%s; %s forall -c git tag -a %s -m "%s";%s forall -c git push jgs %s' % (self.repopath,comment,self.repopath,self.__branch,self.repopath,tagname,tagcomment,self.repopath,tagname))
		os.system('%s forall -c git commit -m "%s"; %s forall -c git push jgs HEAD:refs/heads/%s' % (self.repopath,comment,self.repopath,self.__branch))
		if int(patchNo) % 10 == 0:
			os.system('%s forall -c git tag -a %s -m "%s";%s forall -c git push jgs %s' % (self.repopath,tagname,tagcomment,self.repopath,tagname))
		print 'finish merge....'


    def RepoCode(self, CodeDir, BranchName):
	print "Now checking the code if exist"
        if not os.path.isdir(CodeDir):
            os.system('mkdir -p %s' % CodeDir)
        os.chdir(CodeDir)
        if os.path.exists(CodeDir + "/.repo") == False:  
            repoinit = pexpect.spawn('%s init -u git@10.92.32.10:sdd1/manifest -m %s.xml' % (self.repopath, BranchName))
            repoinit.expect('Your\s+Name.*')
            repoinit.sendline()
            repoinit.expect('Your\s+Email.*')
            repoinit.sendline()
            repoinit.expect('is this.*')
            repoinit.sendline('yes')	
	
    def makePatchMail(self,patchNo=-1):
        '''parameter patchNo is 0,1,2,3...'''
        if int(patchNo) == -1:
            print "Parameter patchNo(%s) is error." % patchNo
            return
        if not os.path.isdir(self.__mailDir):
            os.system('mkdir -p %s' % self.__mailDir)
	print self.__ongoingPatch, self.__patchFormat, patchNo
        patchFilename = getoutput("ls %s | grep %s | grep _P%s\).tar.gz" % (self.__ongoingPatch, self.__patchFormat, patchNo))
	print 'patchFilename',patchFilename
        self.patch_type,self.vnum,self.pnum,self.eservice_ID = self.getMtkPatchInfor(patchFilename) 
        os.chdir(self.__importCode)
        #Please don't add 'repo sync' action here, it will affect merge work.
        patchList = getoutput('%s forall -c \'git log jgs/%s --format=%s\' | grep "%s" | grep "porting P%s_" | sort' %(self.repopath,self.__branch, '%s^^^^^^%H^^^^^^$PWD', self.__patchFormat, patchNo)).split('\n')

        for item in range(len(patchList)):
            itemInfo = patchList[item].split('^^^^^^')
            if 3 != len(itemInfo):
                print "patchList have problems. patchList = %s" % patchList
		print "The pacth has no modification"
                exit()

        bugInfo = self.__getBugzillaInfo(patchList[0].split('^^^^^^')[0])
        mailBody = []
        dearName = sys.argv[4].split('@')[0]
        if bugInfo['patchID'] == 'JRD':
            mailBody.append('<p align=\'Left\'><b>Dear %s,</b></p>' % dearName)
            mailBody.append('<p align=\'Left\'>MTK6572 Platform Bug %s(<a href="%s">%s </a>)</p>' %(bugInfo['bugID'], bugInfo['bugLink'], bugInfo['bugLink']))
                              
        if bugInfo['patchID'] == 'ALPS':
            mailBody.append('<p align=\'Left\'><b>Dear %s,</b></p>' % dearName)
            mailBody.append('<p align=\'Left\'>MTK6572 Platform Patch %s<br/></p>' %bugInfo['bugID'])
        self.getEmailContent(mailBody,patchNo,bugInfo,patchList)
        print 'self.importIdDict',self.importIdDict
        print 'self.projectid_codeBranch_Dict',self.projectid_codeBranch_Dict	
        for projectID1 in self.importIdDict.keys():
		for projectID2 in self.projectid_codeBranch_Dict.keys():
			if projectID1 == projectID2:
				self.insertImportId_And_DevBranch_To_dotp_mtk_merge(self.importIdDict[projectID1],self.projectid_codeBranch_Dict[projectID2],sys.argv[4])			             
        #os.system('python /local/int_jenkins/mtk_patch/lib/insertdbToWr.py %s %s %s %s' % (self.__branch,self.vnum,self.pnum,self.eservice_ID) )
        os.system('python /local/int_jenkins/mtk_patch/lib/insertInforToManpower.py %s %s %s %s %s' % (self.__branch,self.vnum,self.pnum,self.eservice_ID,sys.argv[6]) )			             
        self.__sendMail(patchNo, mailBody, [bugInfo['assigned']])

    def getEmailContent(self,mailBody,patchNo,bugInfo,patchList):
        #mailBody.append('<p align=\'Left\'>Summary: </p>')
	mailBody.append('<p align=\'Left\'>MTK Patch <b><font color="#FF0000">P%s</font></b> has been merged to import branch(mtk6572-kk-v1.0-import)<br/></p>' % patchNo)
	mailBody.append('<p align=\'Left\'><b>Please help to merge patch to below branch:</b></p>')
        for eachCodeBranchName in self.devCodeBranch:
            mailBody.append('<p align=\'Left\'><b><font color="#FF0000">%s</font></b></p>' % eachCodeBranchName)
	mailBody.append('<p align=\'Left\'><font color="#FF0000">Please kindly give a feedback in 24h.</b></font><br/></p>')
	mailBody.append('<p align=\'Left\'>Note: When commit MTK related patch, please follow below comment.</p>')
	mailBody.append('<p align=\'Left\'><font color="#FF0000">The comment same as: %s</b></font><br/></p>' % bugInfo['comment'])			
	mailBody.append('<p align=\'Left\'>Patch Link in import branch:</p>')
	for item in range(len(patchList)):
		itemInfo = patchList[item].split('^^^^^^')
                gitName = itemInfo[2].split(self.__branch)[1]
                match = re.match("^/(.*)",gitName)
                gitName = match.group(1)
		gitNamefilder = gitName
                print gitName
                gitLink = 'http://10.92.32.10/sdd1/gitweb-sdd1-all/?p=mtk6572/%s.git;a=commit;h=%s' % (gitName, itemInfo[1])
		self.insertImportCommitInfoTO_dotp_mtk_commit(self.importIdDict,self.devCodeProjectIDList,self.__branch,self.patch_type,self.vnum,self.pnum,self.eservice_ID,itemInfo[1],gitLink,gitNamefilder)
                mailBody.append('<p align=\'Left\'>%s) %s<br/></p><p align=\'Left\'><a href="%s">%s</a></p>' % (item+1,gitName,gitLink,gitLink))
		gitnameDir = self.__importCode + "/"+ gitNamefilder
		for keyname in self.checkFileDict.keys():
			filename = self.checkFileDict[keyname]['filename']
			if self.checkFileDict[keyname]['gitname'] == gitName:
				if True == self.getFileLibNvram(gitnameDir,filename):
					mailBody.append('<p align=\'Left\'><font color="#FF0000"><b>Note: The file of %s has been changed in patch P%s.</font></b></p>' % (filename,patchNo))
					mailBody.append('<p align=\'Left\'><font color="#FF0000"><b>Please contact System team to check whether to merge this modification to project code</font></b></p>')
					mailBody.append('<br/>')  
		projectnamestr = '' 
		for projectname in self.projectList:
			if True == self.getFileLibNvram(gitnameDir,projectname):
				projectnamestr = projectname + ',' + projectnamestr
				projectnamestr = projectnamestr.strip(',')
		if projectnamestr:
			project_str = u'请将以上链接中' + projectnamestr + u'文件夹的修改merge到项目对应的文件夹中'
			mailBody.append('<p align=\'Left\'><b><font color=green>%s:%s</font></b></p>' % (gitName,project_str))		
                if gitName == "vendor":		 
		    sofilename = "libnvram.so"			 
		    if 'sochanged' == self.getFileLibNvram(gitnameDir,sofilename):	 
			mailBody.append('<p align=\'Left\'><font color="#FF0000">Note: The file of libnvram.so has been changed.Please get the following files from MTK for merging:</font></p>')
			mailBody.append('<p align=\'Left\'><font color="#FF0000">Libnvram.c(mediatek\\external\\nvram\\libnvram)</font></p>')
			mailBody.append('<p align=\'Left\'><font color="#FF0000">Libnvram.h(mediatek\\external\\nvram\\libnvram)</font></p>')
			mailBody.append('<p align=\'Left\'><font color="#FF0000">libnvram_sec.h(mediatek\\external\\nvram\\libnvram)</font></p>')
			mailBody.append('<p align=\'Left\'><font color="#FF0000">CFG_file_public.h(mediatek\\external\\nvram\\libnvram)</font></p>')
		if gitNamefilder == 'build':
			platform_security_value = self.checkBuildCore(gitnameDir)
			if platform_security_value:
				mailBody.append('<p align=\'Left\'><b><font color=green>Note: The file of build/core/version_defaults.mk has been changed.Please check the modification from the above link.</font></b></p>')
				mailBody.append('<p align=\'Left\'><b><font color=green>The current value of %s </font></b></p>' % platform_security_value)
	a = u"\\\\10.92.32.12\\RDhzKM\\SWD-Share\\INT\\MTKPatch\\%s"%self.sharefoldname
	mailBody.append('<p align=\'Left\'><b>You can also find this patch (P%s) in:<font color="#FF0000">%s</font></b></p>' % (patchNo, a))
	mailBody.append('<p align=\'Left\'><b>After you complete to merge the MTK6572 patch P%s,</b></p>' % patchNo)
	mailBody.append('<p align=\'Left\'><b>please make sure the related issues of this MTK Patch you merged are fixed, and</b></p>')
	mailBody.append('<p align=\'Left\'><font color="#FF0000"><b>please send a remind email to us!!!</b></font></p>') 

		
    def __reset(self):
        if False == self.__debug:
            os.chdir(self.__importCode)
            sleep(120)
            print "reset start... \nClean all, reset to HEAD and repo sync"
            os.system('rm -rf ./out')
            os.system('%s forall -c "git reset --hard HEAD; git clean -df"' % self.repopath)
            os.system('%s sync' % self.repopath)
            print "reset done"

    def __getNextPatchNo(self):
        PatchNo = 0
        os.chdir(self.__importCode)
        patchList = getoutput('%s forall -c "git log jgs/%s --format=%s | grep %s"' %(self.repopath, self.__branch, '%s', self.__patchFormat)).split('\n')
        for item in patchList: 
            match = re.match(r'porting P(\d+).+', item)
            if match:
                tmpNo = match.group(1)
            else:
                continue
            
            if int(tmpNo) > int(PatchNo):
                PatchNo = tmpNo
        return int(PatchNo) + 1

    def __takePatch(self, patchNo=''):
        if '' == patchNo:
            print "patchNo is none"
            sys.exit(1)
        patchFilename = getoutput("ls %s | grep %s | grep _P%s\).tar.gz" % (self.__ongoingPatch, self.__patchFormat, patchNo))
        if 0 == len(patchFilename):
            return "Nofile"
        self.untardir = "%s/P%s" % (self.__ongoingPatch,patchNo)
        os.system('mkdir -p %s' % self.untardir)
        os.system('tar zxvf "%s/%s" -C %s' % (self.__ongoingPatch,patchFilename,self.untardir))
        if not os.path.isdir(self.__importCode):
            os.system('mkdir -p %s' % self.__importCode)
        codedir = self.__importCode
        self.deletefile(codedir, self.untardir)
        os.system('cp -dpRv %s/alps/* %s' %(self.untardir,codedir))
        #os.system('rm -rf %s' % untardir)		
        return patchFilename


    def deletefile(self, codedir, untardir):
        os.chdir(untardir)
        patchlistFilename = commands.getstatusoutput('ls | grep *.txt')	
        if patchlistFilename[0] == 0:
            gitconf = untardir + "/" + patchlistFilename[1]
            f = open(gitconf,'r')	
            os.chdir(codedir)
            for line in f:
            	match = re.search("^delete ",line)		
            	if match:
			line = line.replace("delete ",'')
			line = line.strip()
			print "the delete file is %s" % line	
			os.system('chmod -R 777 %s' % line)	
			deleteResult = commands.getstatusoutput('rm -rf %s' % line)
			if deleteResult[0] != 0:
				print "delete file %s failed" % line
				print "Please check why the file %s cannot be deleted" % line
				sys.exit(1)
			else:
				print "delete file %s successfully" % line

    def __compile(self):		
        space = getoutput('df -h /local/ | awk \'{print $4}\'').split('\n')[1][:-1]
        if (space < 20):
            print "**Error: there is no enough space left on device!"
            return "Fail"
        os.chdir(self.__importCode)
        print "building......"
        if False == self.__debug:
            buildStatus = getstatusoutput("./makeMtk -o=TARGET_BUILD_VARIANT=eng %s new" % self.__project)
            if (buildStatus[0] >> 8) == 0:
                print buildStatus[1]
                return 'Success' 
            else:
                print buildStatus[1]
                return 'Fail'
        else:
            buildInfo = raw_input('[Debug mode] Please input compile result Info "==> [FAIL]" or "other string":') 
            if buildInfo.find("==> [FAIL]") >= 0:
                return "Fail"
            return 'Success'
	
    def __movePatchToMergeDone(self, patchNo):
        os.system('mkdir -p %s' % self.__mergeDone)
        os.system('mv %s/*%s*_P%s\).tar.gz %s' % (self.__ongoingPatch,self.__patchFormat,patchNo,self.__mergeDone))

    def __sendMail(self,patchNo,mailBody=[], to=[]):
        '''parameter toList: the PR owner(assigned) or interface or leader'''
        smtpServer = smtplib.SMTP(self.__mailServer)
	reload(sys)
	sys.setdefaultencoding('utf-8')
        msg = MIMEText(''.join(mailBody), 'html')
        msg['From'] = self.__mailSender
        msg['Subject'] = '%s P%s Patch Merge' %(self.__mailTitle, patchNo)
        domainAccount = self.__mailAccount
        domainPassword = "Hzsw#123"
        smtpServer.login(self.__mailAccount, domainPassword)
        sendTo = "all"
        if sendTo == "self":
            msg['To'] = self.__mailSender
            smtpServer.sendmail(self.__mailSender, self.__mailSender , msg.as_string())
        elif sendTo == "all":
            if len(sys.argv) == 7 and sys.argv[5] == "true":		
            	msg['To'] = ','.join(list(set(self.SPMEmailList + [sys.argv[4]])))
            	msg['Cc'] = ','.join(list(set(self.__mailToList)))
            	smtpServer.sendmail(self.__mailSender, list(set(self.__mailToList + self.__mailccList + self.__SPMList + self.SPMEmailList + [sys.argv[4]])), msg.as_string())
            else:
            	msg['To'] = ','.join(list(set(self.__mailToList)))
            	msg['Cc'] = ','.join(list(set(self.__mailccList + self.__SPMList)))
            	smtpServer.sendmail(self.__mailSender, list(set(self.__mailToList + self.__mailccList + self.__SPMList)), msg.as_string())
        else:
            print "Send Mail to (%s) Error: %s" % (sendTo, msg.as_string())
        fp = open(self.__mailDir + "/P%s.eml" % str(patchNo), 'wb')
        fp.write(msg.as_string())
        fp.close()
        smtpServer.quit()

    def __getBugzillaInfo(self, comment=''):
        result = {}
        if 0 == len(comment):
            return "comment is none"
        match =re.match(r'porting P\d+_+ALPS(\d+)_.+', comment)
        if match:
            #reciever = raw_input('Please manual add a new e-mail address: ')
            reciever = 'renzhi.yang.hz@tcl.com'
            if reciever.endswith('.com'):
                result['assigned'] = reciever
            while True:
		InputString = "Summary:"             
                if len(InputString) > 0:
                    result['shortDesc'] = InputString
                    break
                else:
                    continue
            result['bugID'] = 'ALPS'+match.group(1)
            result['comment'] = comment
            result['patchID'] = 'ALPS'
            return result
                            
        bugID =re.match(r'porting P\d+_.+?(\d+)_.+', comment).group(1)
        while True:
            try:
                conn = MySQLdb.connect(host='172.24.61.199',user='scm_tools',passwd='SCM_TOOLS123!',db='bugs', port=3306, charset='gbk')
                cur = conn.cursor()
                cur.execute('select assigned_to from bugs where bug_id = %s' % bugID)		
                cur.execute('select login_name from profiles where userid = %s' % cur.fetchone()[0])		
                result['assigned'] = cur.fetchone()[0].encode('gb2312')
                cur.execute('select short_desc from bugs where bug_id = %s' % bugID)
                result['shortDesc'] = cur.fetchone()[0].encode('gb2312') 
            except Exception, e:
                cur.close()
                conn.close()
                if '99999999999' != bugID:
                    print 'getBugzillaInfo Exception: %s.' % e
                    print 'Maybe bugID(%s) is error, you could input bugID by manually.' % bugID
		result['assigned'] = 'renzhi.yang.hz@tcl.com'
		result['shortDesc'] = 'no'		
		break
                while True:
                    terminalInputValue = raw_input('Please input bugID:')
                    print terminalInputValue
                    '''bugID length is 6. If need, you could change 7.'''
                    if len(terminalInputValue) == 6:
                        bugID = terminalInputValue
                        break
                    else:
                        continue
            else:
                cur.close()
                conn.close()
                print "Find: %s " % result
                #InputValue = raw_input('Please input "yes" or "no" to confirm the above PR(%s) information:' % bugID)
                InputValue = "yes"
                if InputValue == "yes":
                    break
                else:
                    bugID = "99999999999"

        for spm in self.__SPMList:
            print spm.split('@')[0]
            if spm.split('@')[0] == result['assigned'].split('@')[0]:
                print 'Reciever is SPM, please confirm another reciever !'
                while True:		
                    reciever = raw_input('Please manual add a new e-mail address: ')
                    if reciever.endswith('.com'):
                        result['assigned'] = reciever
                        InputValue = "yes"
                        #InputValue = raw_input('Please input "yes" or "no" to confirm the E-mail address(%s):' % result['assigned'])
                        if InputValue == "yes":
                            result['assigned'] = reciever
                            break
                        else:
                            continue

        result['bugID'] = bugID
        result['comment'] = comment
        result['bugLink'] = "http://bugzilla.tcl-ta.com/show_bug.cgi?id=%s" % bugID
        result['patchID'] = 'JRD'
        return result


	
    def justBuild(self):
        print "Just to build now"
        print "Repo sync latest import code"
        self.RepoCode(self.__importCode, self.__branch)
        self.__reset()
        if 'Fail' == self.__compile():
		print "Build Failed"
		sys.exit(1)

if __name__ == "__main__":
    print sys.argv
    if len(sys.argv) == 5 and sys.argv[2] == 'MergePatch':
        pm = PatchMerge(sys.argv[1])
        pm.MergePatch()
    elif len(sys.argv) == 5 and sys.argv[2] == 'MergeOnePatch':
        pm = PatchMerge(sys.argv[1])
        pm.MergeOnePatch(sys.argv[4],sys.argv[3])

    elif len(sys.argv) == 7 and sys.argv[2] == 'PatchMail':
        pm = PatchMerge(sys.argv[1])
        pm.makePatchMail(sys.argv[3])
    elif len(sys.argv) == 3 and sys.argv[2] == 'build':
        pm = PatchMerge(sys.argv[1])
        pm.justBuild()
    else:
        print "%s Usage:" % sys.argv[0]
        print "\t%s BaseDir MergePatch" % sys.argv[0]
        print "\t%s BaseDir MergeOnePatch PatchNo" % sys.argv[0]
        print "\t%s BaseDir PatchMail PatchNo" % sys.argv[0]
        print "For example: \n\t%s . MergePatch\n\t%s . MergeOnePatch 1\n\t%s . PatchMail 1" % (sys.argv[0], sys.argv[0],sys.argv[0])

