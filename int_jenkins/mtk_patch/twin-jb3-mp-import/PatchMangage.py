#!/usr/bin/python
#coding=gb2312

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
import xlrd
import string


class PatchMerge():
    def __init__(self,basedir='/local'):
        os.chdir(basedir)
        self.__baseDir = os.getcwd()		
        self.__debug = False
        self.__pushCodeToGit = True

        #wb = xlrd.open_workbook(self.__baseDir + '/mtk6577-jb-import.xls')
        wb = xlrd.open_workbook('%s/mt6572-jb3-mp-import.xls' % getoutput("dirname %s" % (sys.argv[0])))
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
        #self.__mailccList.append(self.__mailSender)
        self.__SPMList = mtkSheet.cell(1, 9).value.strip().split(',')

        self.__importCode = "/" + "local" + "/mtk_patch_import" + "/" + self.__branch
        self.__mergeDone = "/" + "local" + "/mtk_patch_import" + "/mergeDone"
        self.__ignorePatch = "/" + "local" + "/mtk_patch_import" + "/ignorePatch"
        self.__ongoingPatch = "/" + "local" + "/mtk_patch_import" + "/TODO"
        self.__mailDir = "/" + "local" + "/mtk_patch_import" + "/Mail"

        if not os.path.isdir(self.__importCode):
            os.system('mkdir -p %s' % self.__importCode)
        os.chdir(self.__importCode)
        if os.path.exists(self.__importCode + "/.repo") == False:
            #os.system('repo init -u git@10.92.32.10:sdd1/manifest -m %s.xml' % self.__branch)
            repoinit = pexpect.spawn('repo init -u git@10.92.32.10:sdd1/manifest -m %s.xml' % self.__branch)
            repoinit.expect('Your\s+Name.*')
            repoinit.sendline()
            repoinit.expect('Your\s+Email.*')
            repoinit.sendline()
            repoinit.expect('is this.*')
            repoinit.sendline('yes')

        print "baseDir = %s\nbranch = %s\nproject = %s\npatchFormat = %s\nmailServer = %s\nmailTitle = %s\nmailAccount = %s\nmailSender = %s\nongoingPatch = %s" % (self.__baseDir,self.__branch, self.__project, self.__patchFormat, self.__mailServer, self.__mailTitle, self.__mailAccount,self.__mailSender, self.__ongoingPatch)
        print "mailToList = %s\nmailccList = %s\nSPMList = %s\n" % (self.__mailToList, self.__mailccList, self.__SPMList)
		
    def MergePatch(self):
        print "Please create file(start) on /local/mtk_patch_import directory. If not exist, the script will stop and exit."
        while (os.path.exists("/" + "local" + "/mtk_patch_import" + "/start") == True):
            self.__reset()
            patchNo = self.__getNextPatchNo()
            print "getNextPatchNo %s" % patchNo
            if 'Nofile' == self.__takePatch(patchNo):
                print "Wait next patch, patchNo = %s" % patchNo
                continue
            prevMD5 = getoutput('rm -rf /tmp/tempfile; repo forall -c "git add .; git status" >> /tmp/tempfile; md5sum /tmp/tempfile').split()[0]
            if 'Success' == self.__compile():
                print "Compile success"
                self.__reset()
                patchFilename = self.__takePatch(patchNo)
                comment = 'porting P%s_%s' %(patchNo, patchFilename.replace('(', '_').replace(')', '_'))
                print comment
                if True == self.__pushCodeToGit:
                    print 'start merge.....'
                    os.system('repo forall -c git add .')
                    nextMD5 = getoutput('rm -rf /tmp/tempfile; repo forall -c "git status" >> /tmp/tempfile; md5sum /tmp/tempfile').split()[0]
                    if prevMD5 == nextMD5:
                        os.system('repo forall -c git commit -m "%s"; repo forall -c git push jgs HEAD:refs/heads/%s' % (comment,self.__branch))
                        self.__movePatchToMergeDone(patchNo)
                    print 'finish merge....'
                #self.makePatchMail(patchNo)
            else:
                print "Compile failed"
                mailBody = []
                mailBody.append('Compile Error: patchNo = %s' % patchNo)
                self.__sendMail(patchNo, mailBody)
    
    def MergeOnePatch(self,patchNo=-1):
        print "Please create file(start) on /local/mtk_patch_import directory. If not exist, the script will stop and exit."
        '''parameter patchNo is 0,1,2,3...'''
        if int(patchNo) == -1:
            print "Parameter patchNo(%s) is error." % patchNo
            return
        if (os.path.exists("/" + "local" + "/mtk_patch_import"+"/start") == True):
            self.__reset()
            print "patchNo %s" % patchNo
            if 'Nofile' == self.__takePatch(patchNo):
                print "No p%s patch." % patchNo
                    
            prevMD5 = getoutput('rm -rf /tmp/tempfile; repo forall -c "git add .; git status" >> /tmp/tempfile; md5sum /tmp/tempfile').split()[0]
            if 'Success' == self.__compile():
                print "Compile success"
                self.__reset()
                patchFilename = self.__takePatch(patchNo)
                comment = 'porting P%s_%s' %(patchNo, patchFilename.replace('(', '_').replace(')', '_'))
                print comment
                if True == self.__pushCodeToGit:
                    print 'start merge.....'
                    os.system('repo forall -c git add .')
                    nextMD5 = getoutput('rm -rf /tmp/tempfile; repo forall -c "git status" >> /tmp/tempfile; md5sum /tmp/tempfile').split()[0]
                    if prevMD5 == nextMD5:
                        os.system('repo forall -c git commit -m "%s"; repo forall -c git push jgs HEAD:refs/heads/%s' % (comment,self.__branch))
                        self.__movePatchToMergeDone(patchNo)
                    print 'finish merge....'
                #self.makePatchMail(patchNo)
            else:
                print "Compile failed"
                mailBody = []
                mailBody.append('Compile Error: patchNo = %s' % patchNo)
                self.__sendMail(patchNo, mailBody)
	
    def makePatchMail(self,patchNo=-1):
        '''parameter patchNo is 0,1,2,3...'''
        if int(patchNo) == -1:
            print "Parameter patchNo(%s) is error." % patchNo
            return
        if not os.path.isdir(self.__mailDir):
            os.system('mkdir -p %s' % self.__mailDir)
        os.chdir(self.__importCode)
        #Please don't add 'repo sync' action here, it will affect merge work.
        patchList = getoutput('repo forall -c \'git log jgs/%s --format=%s\' | grep "%s" | grep "porting P%s_" | sort' %(self.__branch, '%s^^^^^^%H^^^^^^$PWD', self.__patchFormat, patchNo)).split('\n')
        ######patchList = getoutput('repo forall -c \'git log jgs/%s --format=%s\' | grep "%s" | grep "_P%s" | sort' %(self.__branch, '%s^^^^^^%H^^^^^^$PWD', self.__patchFormat, patchNo)).split('\n')
        for item in range(len(patchList)):
            itemInfo = patchList[item].split('^^^^^^')
            if 3 != len(itemInfo):
                print "patchList have problems. patchList = %s" % patchList
                exit()

        bugInfo = self.__getBugzillaInfo(patchList[0].split('^^^^^^')[0])
        mailBody = []
        dearName = string.capwords(bugInfo['assigned'][bugInfo['assigned'].index('.')+1:bugInfo['assigned'].index('@')])
        if dearName == '':
            dearName = string.capwords(bugInfo['assigned'][:bugInfo['assigned'].index('@')])
        else:
            dearName = dearName + ' ' + string.capwords(bugInfo['assigned'][:bugInfo['assigned'].index('.')])
        if bugInfo['patchID'] == 'JRD':
            mailBody.append('<p align=\'Left\'><b>Dear %s,</b></p>' % dearName)
            mailBody.append('<p align=\'Left\'>MTK6572 Platform Bug %s(<a href="%s">%s </a>)</p>' %(bugInfo['bugID'], bugInfo['bugLink'], bugInfo['bugLink']))
            mailBody.append('<p align=\'Left\'>Summary: %s</p>' % bugInfo['shortDesc'])
            mailBody.append('<p align=\'Left\'>MTK Patch <b><font color="#FF0000">P%s</font></b> has been merged to import branch(twin2.8-v1.0-import)<br/></p>' % patchNo)
            mailBody.append('<p align=\'Left\'><b>Please help to merge patch to below branch:</b><br/></p>')
            mailBody.append('<p align=\'Left\'><b><font color="#FF0000">twin2.8-v1.0-dint</font></b><br/></p>')
            #mailBody.append('<p align=\'Left\'><b><font color="#FF0000">california-orange-v1.0-dint</font></b><br/><br/></p>')
            #mailBody.append('<p align=\'Left\'><b><font color="#FF0000">yarisl-common-v1.0-dint</font></b><br/><br/></p>')
            mailBody.append('<p><br/><br/></p>')			
            mailBody.append('<p align=\'Left\'><font color="#FF0000">Please kindly give a feedback in 24h.</b></font><br/><br/></p>')
            mailBody.append('<p align=\'Left\'>Note: When commit MTK related patch, please follow below comment.<br/><br/></p>')
            mailBody.append('<p align=\'Left\'><font color="#FF0000">The comment same as:%s</b></font></p>' % bugInfo['comment'])		
            mailBody.append('<p align=\'Left\'>Patch Link in import branch:</p>')
            for item in range(len(patchList)):
                itemInfo = patchList[item].split('^^^^^^')
                gitName = itemInfo[2][len(self.__importCode) + 1:]
                gitLink = 'http://10.92.32.10/sdd1/gitweb-sdd1-all/?p=mtk6572/%s.git;a=commit;h=%s' % (gitName, itemInfo[1])
                mailBody.append('<p align=\'Left\'>%s) %s</p><p align=\'Left\'><a href="%s">%s</a></p>' % (item+1,gitName,gitLink,gitLink))
            mailBody.append('<p><br/><br/><br/></p>')
            #mailBody.append('<p align=\'Left\'>Note: To avoid mistakes, we suggest you use command <font color="#FF0000">"git cherry-pick &lt;commit id&gt;"</font> to get modified files priorly, the commit id you can get from patch link.</p>')
            #mailBody.append('<p align=\'Left\'>Then <font color="#FF0000">"git reset HEAD^"</font> roll back commit and <font color="#FF0000">"git add"</font> and use patch delivery tool to submit modifications.</p>')
            mailBody.append('<p align=\'Left\'><b>You can also find this patch (P%s) in:<font color="#FF0000">\\\\rd-filebackup\\RDhzKM\\4-SDD1\\B-技术文档\\A-MTK源代码\\6572\\6572-patch</font></b></p>' %patchNo)
            mailBody.append('<p align=\'Left\'><b>After you complete to merge the MTK6572 patch P%s,</b></p>' % patchNo)
            mailBody.append('<p align=\'Left\'><b>please make sure the related issues of this MTK Patch you merged are fixed,and</b></p>')
            mailBody.append('<p align=\'Left\'><font color="#FF0000"><b>please send a remind email to us!!!</b></font></p>')
            mailBody.append('<br/><p align=\'Left\'><b>Best Regards</b></p><p align=\'Left\'><b>Integration Team</b></p>')
                               
        if bugInfo['patchID'] == 'ALPS':
            mailBody.append('<p align=\'Left\'><b>Dear %s,</b></p>' % dearName)
            mailBody.append('<p align=\'Left\'>MTK6572 Platform Patch %s<br/></p>' %bugInfo['bugID'])
            mailBody.append('<p align=\'Left\'>Summary: %s<br/></p>' % bugInfo['shortDesc'])
            mailBody.append('<p align=\'Left\'>MTK Patch <b><font color="#FF0000">P%s</font></b> has been merged to import branch(twin2.8-v1.0-import)<br/></p>' % patchNo)
            mailBody.append('<p align=\'Left\'><b>Please help to merge patch to below branch:</b></p>')
            mailBody.append('<p align=\'Left\'><b><font color="#FF0000">twin2.8-v1.0-dint</font></b></p>')
            #mailBody.append('<p align=\'Left\'><b><font color="#FF0000">california-orange-v1.0-dint</font></b></p>')
            #mailBody.append('<p align=\'Left\'><b><font color="#FF0000">yarisl-common-v1.0-dint</font></b><br/><br/><br/></p>')
            mailBody.append('<p align=\'Left\'><font color="#FF0000">Please kindly give a feedback in 24h.</b></font><br/></p>')
            mailBody.append('<p align=\'Left\'>Note: When commit MTK related patch, please follow below comment.</p>')
            mailBody.append('<p align=\'Left\'><font color="#FF0000">The comment same as: %s</b></font><br/></p>' % bugInfo['comment'])
            mailBody.append('<p align=\'Left\'><br/><br/><br/></p>')			
            mailBody.append('<p align=\'Left\'>Patch Link in import branch:</p>')
            for item in range(len(patchList)):
                itemInfo = patchList[item].split('^^^^^^')
                gitName = itemInfo[2][len(self.__importCode) + 1:]
                gitLink = 'http://10.92.32.10/sdd1/gitweb-sdd1-all/?p=mtk6572/%s.git;a=commit;h=%s' % (gitName, itemInfo[1])
                mailBody.append('<p align=\'Left\'>%s) %s<br/></p><p align=\'Left\'><a href="%s">%s</a></p>' % (item+1,gitName,gitLink,gitLink))
            mailBody.append('<p><br/><br/><br/></p>')
            #mailBody.append('<p align=\'Left\'>Note: To avoid mistakes, we suggest you use command <font color="#FF0000">"git cherry-pick &lt;commit id&gt;"</font> to get modified files priorly, the commit id you can get from patch link.</p>')
            #mailBody.append('<p align=\'Left\'>Then <font color="#FF0000">"git reset HEAD^"</font> roll back commit and <font color="#FF0000">"git add"</font> and use patch delivery tool to submit modifications.</p>')
            mailBody.append('<p align=\'Left\'><b>You can also find this patch (P%s) in:<font color="#FF0000">\\\\rd-filebackup\\RDhzKM\\4-SDD1\\B-技术文档\\A-MTK源代码\\6572\\6572-patch</font></b></p>' %patchNo)
            mailBody.append('<p align=\'Left\'><b>After you complete to merge the MTK6572 patch P%s,</b></p>' % patchNo)
            mailBody.append('<p align=\'Left\'><b>please make sure the related issues of this MTK Patch you merged are fixed, and</b></p>')
            mailBody.append('<p align=\'Left\'><font color="#FF0000"><b>please send a remind email to us!!!</b></font></p>')
            mailBody.append('<br/><p align=\'Left\'><b>Best Regards</b></p><p align=\'Left\'><b>Integration Team</b></p>')
                            
        self.__sendMail(patchNo, mailBody, [bugInfo['assigned']])
		
    def __reset(self):
        if False == self.__debug:
            os.chdir(self.__importCode)
            sleep(120)
            print "reset start... \nClean all, reset to HEAD and repo sync"
            os.system('rm -rf ./out')
            os.system('repo forall -c "git reset --hard HEAD; git clean -df"')
            os.system('repo sync')
            print "reset done"

    def __getNextPatchNo(self):
        PatchNo = 0
        os.chdir(self.__importCode)
        patchList = getoutput('repo forall -c "git log jgs/%s --format=%s | grep %s"' %(self.__branch, '%s', self.__patchFormat)).split('\n')
        for item in patchList: 
            #tmpNo = re.match(r'.+P(\d+).+', item).group(1)
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
            exit(0)
        patchFilename = getoutput("ls %s | grep %s | grep _P%s\).tar.gz" % (self.__ongoingPatch, self.__patchFormat, patchNo))
        if 0 == len(patchFilename):
            return "Nofile"
        untardir = "%s/P%s" % (self.__ongoingPatch,patchNo)
        os.system('mkdir -p %s' % untardir)
        os.system('tar zxvf "%s/%s" -C %s' % (self.__ongoingPatch,patchFilename,untardir))
        if not os.path.isdir(self.__importCode):
            os.system('mkdir -p %s' % self.__importCode)
        os.system('cp -dpRv %s/alps/* %s' %(untardir,self.__importCode))
        os.system('rm -rf %s' % untardir)		
        return patchFilename

    def __compile(self):		
        space = getoutput('df -h /local/ | awk \'{print $4}\'').split('\n')[1][:-1]
        if (space < 20):
            print "**Error: there is no enough space left on device!"
            return "Fail"
        os.chdir(self.__importCode)
        print "building......"
        if False == self.__debug:
            #buildInfo = getoutput('./makeMtk -o=TARGET_BUILD_VARIANT=eng %s new' % self.__project).replace('\n', ' ').replace('\t', ' ')
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
        msg = MIMEText(''.join(mailBody), 'html')
        msg.set_charset('gb2312')
        msg['From'] = self.__mailSender
        msg['Subject'] = '%s P%s Patch Merge' %(self.__mailTitle, patchNo)
        domainAccount = self.__mailAccount
        domainPassword = getpass.getpass('Passowrd for Email account <%s>:' %self.__mailAccount)
        smtpServer.login(self.__mailAccount, domainPassword)
        sendTo = raw_input('Send mail to <self|all>:')
        if sendTo == "self":
            msg['To'] = self.__mailSender
            smtpServer.sendmail(self.__mailSender, self.__mailSender , msg.as_string())
        elif sendTo == "all":
            #msg['To'] = ','.join(list(set(to)))
            msg['To'] = ','.join(list(set(self.__mailToList + to)))
            msg['Cc'] = ','.join(list(set(self.__mailccList + self.__SPMList)))
            smtpServer.sendmail(self.__mailSender, list(set(self.__mailToList + to + self.__mailccList + self.__SPMList)), msg.as_string())
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
            reciever = raw_input('Please manual add a new e-mail address: ')
            if reciever.endswith('.com'):
                result['assigned'] = reciever
            while True:
                InputString = raw_input('Please input shortDesc for this patch:')
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
                                    
                #while True:
                #    InputValue = raw_input('Please input "yes" or "no" to confirm the E-mail address(%s):' % result['assigned'])
                #    if InputValue == "yes":
                #        break
                #    else:
                #        reciever = raw_input('Please manual add a new e-mail address: ')
                #        if reciever.endswith('.com'):
                #            result['assigned'] = reciever
                #            break
            except Exception, e:
                cur.close()
                conn.close()
                if '99999999999' != bugID:
                    print 'getBugzillaInfo Exception: %s.' % e
                    print 'Maybe bugID(%s) is error, you could input bugID by manually.' % bugID
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
                InputValue = raw_input('Please input "yes" or "no" to confirm the above PR(%s) information:' % bugID)
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
                        InputValue = raw_input('Please input "yes" or "no" to confirm the E-mail address(%s):' % result['assigned'])
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

if __name__ == "__main__":
    if len(sys.argv) == 3 and sys.argv[2] == 'MergePatch':
        pm = PatchMerge(sys.argv[1])
        pm.MergePatch()
    elif len(sys.argv) == 4 and sys.argv[2] == 'MergeOnePatch':
        pm = PatchMerge(sys.argv[1])
        pm.MergeOnePatch(sys.argv[3])
    elif len(sys.argv) == 4 and sys.argv[2] == 'PatchMail':
        pm = PatchMerge(sys.argv[1])
        pm.makePatchMail(sys.argv[3])
    else:
        print "%s Usage:" % sys.argv[0]
        print "\t%s BaseDir MergePatch" % sys.argv[0]
        print "\t%s BaseDir MergeOnePatch PatchNo" % sys.argv[0]
        print "\t%s BaseDir PatchMail PatchNo" % sys.argv[0]
        print "For example: \n\t%s . MergePatch\n\t%s . MergeOnePatch 1\n\t%s . PatchMail 1" % (sys.argv[0], sys.argv[0],sys.argv[0])

