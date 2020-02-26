#!/usr/bin/python
#coding=utf-8

import os
import re
import sys
import getpass
from commands import *
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

class ModemPatchMerge(CheckFile,dotProjectDb,checkPatchInfor):
    def __init__(self,basedir='/local'):
	self.repopath = '/local/tools/repo/repo'
        os.chdir(basedir)
        self.__baseDir = os.getcwd()		
        self.__debug = False
        self.__pushCodeToGit = True

        wb = xlrd.open_workbook('%s/pixi3-45-australia-jb3-mp-import.xls' % getoutput("dirname %s" % (sys.argv[0])))
        mtkSheet = wb.sheet_by_name(u'ModemInfo')
        self.__branch = mtkSheet.cell(1, 0).value.strip()
        self.__patchFormat = mtkSheet.cell(1, 1).value.strip()
        self.__mailServer = mtkSheet.cell(1, 2).value.strip()
        self.__mailTitle = mtkSheet.cell(1, 3).value.strip()
        self.__mailAccount = mtkSheet.cell(1, 4).value.strip()
        self.__mailSender = mtkSheet.cell(1, 5).value.strip()
        self.__mailToList = mtkSheet.cell(1, 6).value.strip().split(',')
        self.__mailccList = mtkSheet.cell(1, 7).value.strip().split(',')
        self.__SPMList = mtkSheet.cell(1, 8).value.strip().split(',')

        #self.__importCode = self.__baseDir + "/" + self.__branch + "_MD"
        #self.__ModemCode = self.__importCode + "/" + "MT6572_Modem"
        #self.__mergeDone = self.__baseDir + "/mergeDone"
        #self.__ignorePatch = self.__baseDir + "/ignorePatch"
        #self.__ongoingPatch = self.__baseDir + "/TODO"
        #self.__mailDir = self.__baseDir + "/Mail"
        self.__importCode = "/" + "local" + "/mtk_patch_import" + "/" + self.__branch + "_MD"
        self.__ModemCode = self.__importCode + "/MT6572_Modem"
        self.__mergeDone = "/" + "local" + "/mtk_patch_import" + "/mergeDone"
        self.__ignorePatch = "/" + "local" + "/mtk_patch_import" + "/ignorePatch"
        self.__ongoingPatch = "/" + "local" + "/mtk_patch_import" + "/TODO"
        self.__mailDir = "/" + "local" + "/mtk_patch_import" + "/" + "/Mail"
	self.__start = "/" + "local" + "/mtk_patch_import" + "/start"
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
	self.checkFileDict = self.getAffectFileDict()
	self.projectList = self.checkProjectName()
        if not os.path.isdir(self.__start):
            os.system('mkdir -p %s' % self.__start)

        if not os.path.isdir(self.__importCode):
            os.system('mkdir -p %s' % self.__importCode)
        os.chdir(self.__importCode)
        if os.path.exists(self.__importCode + "/MT6572_Modem" + "/.git") == False:
            os.system('git clone git@10.92.32.10:sdd1/mtk6572/modem.git MT6572_Modem')
            os.chdir(self.__ModemCode)
            os.system('git checkout %s' % self.__branch)

        print "baseDir = %s\nbranch = %s\npatchFormat = %s\nmailServer = %s\nmailTitle = %s\nmailAccount = %s\nmailSender = %s\nongoingPatch = %s" % (self.__baseDir,self.__branch, self.__patchFormat, self.__mailServer, self.__mailTitle, self.__mailAccount,self.__mailSender, self.__ongoingPatch)
        print "mailToList = %s\nmailccList = %s\nSPMList = %s\n" % (self.__mailToList, self.__mailccList, self.__SPMList)
        
    def MergeModemPatch(self):
        print "Please create file(start) on /local/mtk_patch_import directory. If not exist, the script will stop and exit."
        while (os.path.exists("/" + "local" + "/mtk_patch_import" + "/start") == True):
            self.__reset()
            patchNo = self.__getNextPatchNo()
            print "getNextPatchNo %s" % patchNo
            patchFilename = self.__takePatch(patchNo)
            if 'Nofile' == patchFilename:
                print "Wait next patch, patchNo = %s" % patchNo
                continue
            os.chdir(self.__ModemCode)
            prevMD5 = getoutput('rm -rf /tmp/tempfile; git add .; git status >> /tmp/tempfile; md5sum /tmp/tempfile').split()[0]
            if 'Success' == self.__compile():
                print "Compile success"
                self.__copyModemImage()
                self.__reset()
                patchFilename = self.__takePatch(patchNo)
                comment = 'porting P%s_%s' %(patchNo, patchFilename.replace('(', '_').replace(')', '_'))
                print comment
                if True == self.__pushCodeToGit:
                    print 'start merge.....'
                    os.system('git add .')
                    nextMD5 = getoutput('rm -rf /tmp/tempfile; git status >> /tmp/tempfile; md5sum /tmp/tempfile').split()[0]
                    if prevMD5 == nextMD5:
                        #os.system('git commit -m "%s"' %comment)
                        os.system('git commit -m "%s"; git push origin HEAD:refs/heads/%s' % (comment,self.__branch))
                        self.__movePatchToMergeDone(patchNo)
                    print 'finish merge....'
                    self.__pushModemImage(patchNo)
            #else:
            #    print "Compile failed"
            #    mailBody = []
            #    mailBody.append('Compile Error: patchNo = %s' % patchNo)
            #    self.__sendMail(patchNo, mailBody)

    def MergeModemOnePatch(self,patchtype,patchNo=-1):
        print "Please create file(start) on /local/mtk_patch_import directory. If not exist, the script will stop and exit."
        if (os.path.exists("/" + "local" + "/mtk_patch_import" + "/start") == True):
            self.__reset()
            #patchNo = self.__getNextPatchNo()
            print "patchNo %s" % patchNo
            patchFilename = self.__takePatch(patchNo)
            if 'Nofile' == patchFilename:
                print "No p%s patch. Please copy the correct patch to TODO directory" % patchNo
                sys.exit(1)
            os.chdir(self.__ModemCode)
            prevMD5 = getoutput('rm -rf /tmp/tempfile; git add .; git status >> /tmp/tempfile; md5sum /tmp/tempfile').split()[0]
            if sys.argv[5].lower() == "yes" and 'Success' == self.__compile():
                print "Compile success"
                self.__copyModemImage()
                self.__reset()
                patchFilename = self.__takePatch(patchNo)
                self.patch_type,self.vnum,self.pnum,self.eservice_ID = self.getMtkPatchInfor(patchFilename)
                self.description = self.getDescriptionFromPatchListFile(self.__importCode, self.untardir,self.eservice_ID)
                os.chdir(self.__ModemCode)
                print "description",self.description 	        
                self.insertAllImportInfoTO_importSheet(self.devCodeProjectIDList,self.__branch,self.patch_type,self.vnum,self.pnum,self.eservice_ID,self.description,patchtype)
                comment = 'porting P%s_%s' %(patchNo, patchFilename.replace('(', '_').replace(')', '_'))
		tagname = 'porting_modem_patch_%s' % patchFilename.replace('(', '_').replace(')', '_')
		tagcomment = 'porting modem patch P%s' % patchNo
                print "commit comment is %s" % comment
		print "tagname is %s" % tagname
		print "tagcomment is %s" % tagcomment
                print comment
                if True == self.__pushCodeToGit:
                    print 'start merge.....'
                    os.system('git add .')
                    nextMD5 = getoutput('rm -rf /tmp/tempfile; git status >> /tmp/tempfile; md5sum /tmp/tempfile').split()[0]
                    if prevMD5 == nextMD5:
                        #os.system('git commit -m "%s"' %comment)
                        os.system('git commit -m "%s"; git push origin HEAD:refs/heads/%s; git tag -a %s -m "%s"; git push origin %s' % (comment,self.__branch,tagname,tagcomment,tagname))
                        #self.__movePatchToMergeDone(patchNo)
                    print 'finish merge....'
                    self.__pushModemImage(patchNo)
            else:
                print "The code build failed"
                sys.exit(1)
                
    def __reset(self):
        if False == self.__debug:
            os.chdir(self.__ModemCode)
            sleep(120)
            print "reset start... \nClean all, reset to HEAD and git pull"
            os.system('rm -rf ./build')
            os.system('git reset --hard HEAD')
            os.system('git clean -df')
            os.system('git pull')
            print "reset done"

    def __getNextPatchNo(self):
        PatchNo = 0
        os.chdir(self.__ModemCode)
        patchList = getoutput('git log origin/%s --format=%s | grep %s' %(self.__branch, '%s', self.__patchFormat)).split('\n')
        for item in patchList: 
            #tmpNo = re.match(r'porting P(\d+).+', item).group(1)
            match = re.match(r'porting P(\d+).+', item)
            if match:
                tmpNo = match.group(1)
            else:
                continue
            
            if int(tmpNo) > int(PatchNo):
                PatchNo = tmpNo
        return int(PatchNo) + 1

    def __takePatch(self,patchNo=''):
        if '' == patchNo:
            print "patchNo is none"
            sys.exit(1)
        elif '0' == patchNo:            
            match_patch = re.match(r'For_(.*)', self.__patchFormat) 
            if match_patch:
                patch_format = match_patch.group(1)
                patchFilename = getoutput("ls %s | grep %s.tar.gz" % (self.__ongoingPatch, patch_format))
        else:
            patchFilename = getoutput("ls %s | grep %s | grep P%s\).tar.gz" % (self.__ongoingPatch, self.__patchFormat, patchNo))
        if 0 == len(patchFilename):
            return "Nofile"
        self.untardir = "%s/MD-P%s" % (self.__ongoingPatch,patchNo)
        os.system('mkdir -p %s' % self.untardir)
        os.system('tar zxvf "%s/%s" -C %s' % (self.__ongoingPatch,patchFilename,self.untardir))
        if not os.path.isdir(self.__ModemCode):
            os.system('mkdir -p %s' % self.__ModemCode)
        
        os.system('git rm %s/mtk_rel/JRDHZ72_WE_72_S1A_KK_GPRS/DEFAULT/tst/database/BPLGUInfoCustomApp*' %self.__ModemCode)
        os.system('git rm %s/mtk_rel/JRDHZ72_WE_72_S1A_KK_GPRS/DEFAULT/tst/database/_BPLGUInfoCustomApp*' %self.__ModemCode)
        os.system('git rm %s/mtk_rel/JRDHZ72_WE_72_S1A_KK_HSPA/DEFAULT/tst/database/BPLGUInfoCustomApp*' %self.__ModemCode)
        os.system('git rm %s/mtk_rel/JRDHZ72_WE_72_S1A_KK_HSPA/DEFAULT/tst/database/_BPLGUInfoCustomApp*' %self.__ModemCode)

        os.system('cp -dpRv %s/* %s' %(self.untardir,self.__ModemCode))
        #os.system('rm -rf %s' % untardir)		
        return patchFilename
    
    def __movePatchToMergeDone(self, patchNo):
        os.system('mkdir -p %s' % self.__mergeDone)
        if '0' == patchNo:
            match_patch = re.match(r'For_(.*)', self.__patchFormat) 
            if match_patch:
                patch_format = match_patch.group(1)
                os.system('mv %s/%s.tar.gz %s' % (self.__ongoingPatch,patch_format,self.__mergeDone))
        else:
            os.system('mv %s/*%s*.P%s\).tar.gz %s' % (self.__ongoingPatch,self.__patchFormat,patchNo,self.__mergeDone))
    def __compile(self):		
        space = getoutput('df -h /local/ | awk \'{print $4}\'').split('\n')[1][:-1]
        if (space < 5):
            print "**Error: there is no enough space left on device!"
            return "Fail"
        os.chdir(self.__ModemCode)
        print "building......"
        if False == self.__debug:
            buildStatus = getstatusoutput("./make.sh \"JRDHZ72_WE_72_S1A_KK_HSPA.mak\" new")
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


    def __copyModemImage(self):
        mdImg = 'mdImg'
        OUT_PATH1 = 'MT6572_Modem/build/JRDHZ72_WE_72_S1A_KK_HSPA/DEFAULT/bin'
        OUT_PATH2 = 'MT6572_Modem/build/JRDHZ72_WE_72_S1A_KK_HSPA/DEFAULT/tst/database'
        OUT_PATH3 = 'MT6572_Modem/mtk_rel/JRDHZ72_WE_72_S1A_KK_HSPA/DEFAULT/tst/database'
        OUT_PATH4 = mdImg
        PATH1 = "JRDHZ72_WE_72_S1A_KK_HSPA"

        os.chdir(self.__importCode)
        os.system('rm -rfv %s'%mdImg)
        os.system('mkdir %s' %mdImg)
        
        os.system('cp %s/JRDHZ72_WE_72_S1A_KK_HSPA_PCB01_MT6572_S00.elf %s' %(OUT_PATH1,OUT_PATH4))
        os.system('cp %s/JRDHZ72_WE_72_S1A_KK_HSPA_PCB01_MT6572_S00.MOLY_WR8_*.bin %s/modem_1_wg_n.img'%(OUT_PATH1,OUT_PATH4))
        os.system('cp %s/JRDHZ72_WE_72_S1A_KK_HSPA.mak %s/modem_1_wg_n.mak'%(OUT_PATH1,OUT_PATH4))         
        os.system('cp %s/DbgInfo* %s'%(OUT_PATH1,OUT_PATH4))        
        os.system('cp %s/BPLGUInfoCustomApp* %s'%(OUT_PATH2,OUT_PATH4))
        os.system('cp %s/catcher_filter.bin %s/catcher_filter_1_wg_n.bin'%(OUT_PATH2,OUT_PATH4))
        os.system('cp %s/mcddll.dll %s/' %(OUT_PATH3,OUT_PATH4))
        os.chdir(self.__importCode + "/%s" %OUT_PATH4)
        name1 = getoutput('ls -a DbgInfo*')
        os.system('mv %s %s_1_wg_n'%(name1,name1))
        name2 = getoutput('ls -a BPLGUInfoCustomAppSrcP_MT6572_S00_MOLY_WR8_*')
        os.system('mv %s %s_1_wg_n'%(name2,name2))
        os.system('mv %s_PCB01_MT6572_S00.elf %s_PCB01_MT6572_S00_1_wg_n.elf'%(PATH1,PATH1))
        os.system('zip ELF.zip %s_PCB01_MT6572_S00_1_wg_n.elf'%PATH1)
        #os.system('rm -rf JRDHZ72_WE_TWIN_JB3_HSPA_PCB01_MT6572_S00.elf')


        
    def __pushModemImage(self,patchNo):
	action = "yes"
        #action = raw_input('Push modem image to alps(yes or no): ')
        if action == "yes":
            path = 'custom/common/modem/jrdhz72_we_72_s1a_kk'      
            print 'Start to push modem image'
            os.chdir(self.__importCode)
            os.system('rm -rf mediatek')
            os.system('git clone git@10.92.32.10:sdd1/mtk6572/mediatek.git')
            os.chdir(self.__importCode + "/" + "mediatek" )
            os.system('git checkout %s' % self.__branch)

            os.system('git rm -rf custom/common/modem/jrdhz72_we_72_s1a_kk/*')
            os.system('mkdir -vp %s' % path)
      
            os.system('cp -dpRv ../mdImg/* %s' % path)
            os.system('git add .')
            os.system('git commit -m "update modem image base on modem patch P%s"' %patchNo)
            os.system('git push origin HEAD:refs/heads/%s' %self.__branch)
            
        else:
            print 'don\'t push image to apls'

    def makePatchMail(self,patchNo=-1):
        '''parameter patchNo is 0,1,2,3...'''
        if int(patchNo) == -1:
            print "Parameter patchNo(%s) is error." % patchNo
            return
	print self.__ongoingPatch, self.__patchFormat, patchNo
        patchFilename = getoutput("ls %s | grep %s | grep P%s\).tar.gz" % (self.__ongoingPatch, self.__patchFormat, patchNo))
	print 'patchFilename',patchFilename
        self.patch_type,self.vnum,self.pnum,self.eservice_ID = self.getMtkPatchInfor(patchFilename) 
        if not os.path.isdir(self.__mailDir):
            os.system('mkdir -p %s' % self.__mailDir)
        os.chdir(self.__ModemCode)
        #Please don't add 'repo sync' action here, it will affect merge work.
        patchList = getoutput('git log origin/%s --format=%s\ | grep "%s" | grep "porting P%s_" | sort' %(self.__branch, '%s^^^^^^%H^^^^^^$PWD', self.__patchFormat, patchNo)).split('\n')
        for item in range(len(patchList)):
            itemInfo = patchList[item].split('^^^^^^')
            if 3 != len(itemInfo):
                print "patchList have problems. patchList = %s" % patchList
		print "The pacth has no modification"
                sys.exit(1)
        mailBody = []
	dearName = sys.argv[4].split('@')[0]
        mailBody.append('<p align=\'Left\'><b>Dear %s,</b></p>' % dearName)

        mailBody.append('<p align=\'Left\'>MTK modem Patch <b><font color="#FF0000">P%s</font></b> has been merged to import branch(%s)<br/></p>' % (patchNo,self.__branch))
        mailBody.append('<p align=\'Left\'><b>Please help to merge patch to below branch:</b><br/></p>')
        for eachCodeBranchName in self.devCodeBranch:		
            mailBody.append('<p align=\'Left\'><b><font color="#FF0000">%s</font></b></p>' % eachCodeBranchName)
        mailBody.append('<p align=\'Left\'><font color="#FF0000">Please kindly give a feedback in 24h.</b></font><br/><br/></p>')		
        mailBody.append('<p align=\'Left\'>Patch Link in import branch:</p>')
        for item in range(len(patchList)):
            itemInfo = patchList[item].split('^^^^^^')    			
            gitNamefilder = 'modem'  		
            gitLink = 'http://10.92.32.10/sdd1/gitweb-odm/?p=modem.git;a=commit;h=%s' % itemInfo[1]
            self.insertImportCommitInfoTO_dotp_mtk_commit(self.importIdDict,self.devCodeProjectIDList,self.__branch,self.patch_type,self.vnum,self.pnum,self.eservice_ID,itemInfo[1],gitLink,gitNamefilder)
            mailBody.append('<p align=\'Left\'>%s)modem</p><p align=\'Left\'><a href="%s">%s</a></p>' % (item+1,gitLink,gitLink))
            for keyname in self.checkFileDict.keys():
		filename = self.checkFileDict[keyname]['filename']
		if self.checkFileDict[keyname]['gitname'] == 'modem':
			if True == self.getFileLibNvram(self.__ModemCode,filename):
				mailBody.append('<p align=\'Left\'><font color="#FF0000"><b>Note: The file of %s has been changed in patch P%s.</font></b></p>' % (filename,patchNo))
				mailBody.append('<p align=\'Left\'><font color="#FF0000"><b>Please contact Drive team to check whether to merge this modification to project code</font></b></p>')
				mailBody.append('<br/>')		
            for projectname in self.projectList:
		if True == self.getFileLibNvram(self.__ModemCode,projectname):
			project_str = u'请将以上链接中' + projectname + u'文件夹的修改merge到项目对应的文件夹中'
			mailBody.append('<p align=\'Left\'><font color="#FF0000">%s:%s</font></p>' % (gitNamefilder,project_str))
            a = u"\\\\10.92.32.12\\RDhzKM\\4-SWD1\\B-技术文档\\A-MTK源代码\\6572\\ALPS.KK1.MP6.V1_JRDHZ72_WE_72_S1A_KK_INHOUSE"
            mailBody.append('<p align=\'Left\'><b>You can also find this patch (P%s) in:<font color="#FF0000">%s</font></b></p>' % (patchNo, a))
            mailBody.append('<p align=\'Left\'><b>You can also find this patch (P%s) in:<font color="#FF0000">%s</font></b></p>' % (patchNo, a))

            mailBody.append('<p align=\'Left\'><b>After you complete to merge the MTK6572 Pixi3-45 australia patch P%s,</b></p>' % patchNo)
            mailBody.append('<p align=\'Left\'><b>please make sure the related issues of this MTK Patch you merged are fixed,and</b></p>')
            mailBody.append('<p align=\'Left\'><font color="#FF0000"><b>please send a remind email to us!!!</b></font></p>')
        print 'self.importIdDict',self.importIdDict
        print 'self.projectid_codeBranch_Dict',self.projectid_codeBranch_Dict	
        for projectID1 in self.importIdDict.keys():
		for projectID2 in self.projectid_codeBranch_Dict.keys():
			if projectID1 == projectID2:
				self.insertImportId_And_DevBranch_To_dotp_mtk_merge(self.importIdDict[projectID1],self.projectid_codeBranch_Dict[projectID2],sys.argv[4])                          
        #os.system('python /local/int_jenkins/mtk_patch/lib/insertdbToWr.py %s %s %s %s' % (self.__branch,self.vnum,self.pnum,self.eservice_ID) )
        os.system('python /local/int_jenkins/mtk_patch/lib/insertInforToManpower.py %s %s %s %s %s' % (self.__branch,self.vnum,self.pnum,self.eservice_ID,sys.argv[6]) )                        
            #mailBody.append('<br/><p align=\'Left\'><b>Best Regards</b></p><p align=\'Left\'><b>Integration Team</b></p>')
                          
        self.__sendMail(patchNo, mailBody)
    def __sendMail(self,patchNo,mailBody=[], to=[]):
        '''parameter toList: the PR owner(assigned) or interface or leader'''
        smtpServer = smtplib.SMTP(self.__mailServer)
	reload(sys)
	print sys.setdefaultencoding('gbk')	
        msg = MIMEText(''.join(mailBody), 'html','utf-16')
        msg.set_charset('gb2312')   
        msg['From'] = self.__mailSender
        msg['Subject'] = '%s P%s modem Patch Merge' %(self.__mailTitle, patchNo)
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
if __name__ == "__main__":
    if len(sys.argv) == 5 and sys.argv[2] == 'MergePatch':
        pm = ModemPatchMerge(sys.argv[1])
        pm.MergeModemPatch()
    elif len(sys.argv) ==5 and sys.argv[2] == 'MergeOnePatch':
        pm = ModemPatchMerge(sys.argv[1])
        pm.MergeModemOnePatch(sys.argv[4],sys.argv[3])

    #elif len(sys.argv) == 4 and sys.argv[2] == 'PatchMail':
    elif len(sys.argv) == 7 and sys.argv[2] == 'PatchMail':
        pm = ModemPatchMerge(sys.argv[1])
        pm.makePatchMail(sys.argv[3])
    else:
        print "%s Usage:" % sys.argv[0]
        print "\t%s BaseDir MergePatch" % sys.argv[0]
        print "\t%s BaseDir MergeOnePatch PatchNo" % sys.argv[0]
        #print "\t%s BaseDir PatchMail PatchNo" % sys.argv[0]
        #print "For example: \n\t%s . MergePatch\n\t%s . MergeOnePatch 1\n\t%s . PatchMail 1" % (sys.argv[0], sys.argv[0],sys.argv[0])
