#!/usr/bin/python

import os
import sys
import re
from glob import *
from Utils import *
from UserInfo import *
from Config import *
from AllProjectNewAlm import *
import xlrd
from pyExcelerator import *
from ftplib import *
from commands import *
import commands



class project(AllProjectNewAlm):
	def __init__(self):
		self.conf=Config()
	

	def initConfFromXls(self, conf):
		print "init conf from xls"
		## get version from here if bigversion
		versionStr = conf.getConf('version', 'Version number {([0-9A-Z]{3,4}|[0-9A-Z]{3,4}-[0-9A-Z]{1}-[A-Z]{2})$}')
		## if version
		if(versionStr[-2] >= 'U' and not versionStr.__contains__('-')):
			self.BAND = conf.getConf('BAND', 'which BAND version to deliver? <CN|EU1|EU2|US1|US2|2M|AWS|2G|LATAM3G|LATAM2G|US0-1S|US0-2S|US1-1S|US1-2S|US2-2S|US3-1S|US3-2S|US4-1S|US4-2S|US5-2S|US5-1S>')

		self.ProjectConfig = commands.getoutput('/local/int_jenkins/bin/MT6580_X_GetVerInfo '+ versionStr[0:3]+'X -All').split('\n')
		argvList = []
		argvList.extend(['-projbuildroot', '/local/build/%s-release/' % self.conf.getConf('project', 'project name')])
		argvList.extend(['-officelist', self.ProjectConfig[4].strip()])
		argvList.extend(['-Dailylist', self.ProjectConfig[5].strip().lower()])
		argvList.extend(['-minilist', self.ProjectConfig[17].strip().lower()])
		argvList.extend(['-manifestprefix', 'int/%s/'%self.ProjectConfig[1].strip().lower()])
		argvList.extend(['-prlistprojname', self.ProjectConfig[1].strip()])
		argvList.extend(['-releasenoteprojname', self.ProjectConfig[1].strip()])
		argvList.extend(['-custstorepath', self.ProjectConfig[7].strip()])
		argvList.extend(['-checklist.projname', self.ProjectConfig[1].strip()])
		argvList.extend(['-bugzillaproductid', self.ProjectConfig[6].strip()[3:]])		
		argvList.extend(['-projbugbranch',self.ProjectConfig[10].strip()])
		argvList.extend(['-delivtitle',self.ProjectConfig[3].strip()])
		argvList.extend(['-versionX',self.ProjectConfig[0].strip()])
		argvList.extend(['-codebranch',self.ProjectConfig[2].strip()])
		argvList.extend(['-Spmlist',self.ProjectConfig[21].strip()])
		argvList.extend(['-owner','shuangyan.he_yunna.hua'])
		argvList.extend(['-mtkproject',self.ProjectConfig[9].strip()])
		argvList.extend(['-mtkBaseLine','alps-mp-m0.mp1-V2.34_jhz6580.we.m'])
		conf.addFromArg(argvList)
		print conf.dumpConfPretty()

	#get mail list
	def getMailList(self,conf):
		version = self.getVersion(conf)
		## big version will send to office list
		if conf.getConf('isBigVersion','is big version <yes|no>') == 'yes':
			toList =  conf.getConf('officelist','if not mail list,pls input')
		## mini version will send to mini list
		elif conf.getConf('isMiniVersion', 'is mini version <yes|no>') == 'yes':
			toList = conf.getConf('minilist','if not mail list,pls input')
		## Daily version will send to Dialy list
		elif conf.getConf('isDailyVersion', 'is Daily version<yes|no>') == 'yes':
			toList = conf.getConf('Dailylist','if not mail list,pls input')
		else:
			toList = ['<shie.zhao@tcl.com>']
		return toList
	
	## name rule 
	def getNameRule(self):
		return 'Pixi4-5_3g-Software_Naming_Rule_v0.6.xls'
	
	## if you add other test case please input this method, flow example reboot
	def doMiniTestAddFromProject(self, worksheet, conf, checkListConf):
		self.miniTestCheckItem(worksheet, conf, checkListConf, 'openmtklog', 'Use *#*#825364#*#* and *#*#82533284#*#* to check mtklog open.')
		self.miniTestCheckItem(worksheet, conf, checkListConf, 'versioninfo', 'Use *#3228# to check version number and version info.')
		self.miniTestCheckItem(worksheet, conf, checkListConf, 'outcall1', 'Make an outgoing call. Hangup by self phone. Check call log.')
		self.miniTestCheckItem(worksheet, conf, checkListConf, 'outcall2', 'Make an outgoing call. Hangup by opposite side. Check call log.')
		self.miniTestCheckItem(worksheet, conf, checkListConf, 'incall1', 'Make an incoming call. Hangup by self phone. Check call log and incoming ringtone.')
		self.miniTestCheckItem(worksheet, conf, checkListConf, 'incall2', 'Make an incoming call. Hangup by opposite side. Check call log and incoming ringtone.')
		self.miniTestCheckItem(worksheet, conf, checkListConf, 'outcall1_dual_sim', 'For dual sim project, with SIM2, make an outgoing call. Hangup by self phone. Check call log.')
		self.miniTestCheckItem(worksheet, conf, checkListConf, 'outcall2_dual_sim', 'For dual sim project, with SIM2, make an outgoing call. Hangup by opposite side. Check call log.')
		self.miniTestCheckItem(worksheet, conf, checkListConf, 'incall1_dual_sim', 'For dual sim project, with SIM2, make an incoming call. Hangup by self phone. Check call log and incoming ringtone.')
		self.miniTestCheckItem(worksheet, conf, checkListConf, 'incall2_dual_sim', 'For dual sim project, with SIM2, make an incoming call. Hangup by opposite side. Check call log and incoming ringtone.')
		self.miniTestCheckItem(worksheet, conf, checkListConf, 'ringtone', 'Change ringtone.')
		self.miniTestCheckItem(worksheet, conf, checkListConf, 'sms', 'Send a sms to 10086 or 10010. Check the reply on notification bar.')
		self.miniTestCheckItem(worksheet, conf, checkListConf, 'sms_dual_sim', 'For dual sim project, with SIM2, send a sms to 10086 or 10010. Check the reply on notification bar.')
		self.miniTestCheckItem(worksheet, conf, checkListConf, 'auto_sync', 'Login GMS (just Sync account). Check the network settings. To execute auto-sync Contact, Calendar...')
		self.miniTestCheckItem(worksheet, conf, checkListConf, 'gallely', 'Go to Gallery. Check picture/video display.')
		self.miniTestCheckItem(worksheet, conf, checkListConf, 'alarm', 'Set an alarm. Check it can work normally.')
		self.miniTestCheckItem(worksheet, conf, checkListConf, 'contact', 'Create a contact, then delete it.')
		self.miniTestCheckItem(worksheet, conf, checkListConf, 'takephoto', 'Take a photo by camera.')
		self.miniTestCheckItem(worksheet, conf, checkListConf, 'browser', 'Launch browser to access "sina.cn".')
		self.miniTestCheckItem(worksheet, conf, checkListConf, 'usb', 'Connect USB. 1) Check USB charge. 2) Select "Mount" to check USB massive storage.', 'Connect USB.\n 1) Check USB charge.\n 2) Select "Mount" to check USB massive storage.')
		self.miniTestCheckItem(worksheet, conf, checkListConf, 'inputmethod', 'Switch input method in any input box.')
		self.miniTestCheckItem(worksheet, conf, checkListConf, 'fm', 'Check if FM works normally(the phone must support FM feature).')
		self.miniTestCheckItem(worksheet, conf, checkListConf, 'flightmode', 'Switch normal mode/filght mode.')
		self.miniTestCheckItem(worksheet, conf, checkListConf, 'musicplayer', 'Go to music player to check lauder speaker and whether phone can play music normally.')
		self.miniTestCheckItem(worksheet, conf, checkListConf, 'aboutphone', 'Go to setting, check "about phone".')
		self.miniTestCheckItem(worksheet, conf, checkListConf, 'switchbt', 'Turn on/off BT.')
		self.miniTestCheckItem(worksheet, conf, checkListConf, 'switchwifi', 'Turn on/off Wifi.')
		self.miniTestCheckItem(worksheet, conf, checkListConf, 'hotspot', 'Turn on/off Hotspot, accept other mobile client connection and the client can access "sina.cn".')
		self.miniTestCheckItem(worksheet, conf, checkListConf, 'gsensor', 'Go to message list, rotate the phone to check the display mode(portraint and landscape) with g-sensor.')
		self.miniTestCheckItem(worksheet, conf, checkListConf, 'reboot', 'Reboot the phone.')
		self.miniTestCheckItem(worksheet, conf, checkListConf, 'FOTA', 'Go to Settings, about phone, system updates and press continue to check FOTA update UI.')
		self.miniTestCheckItem(worksheet, conf, checkListConf, 'DB files', 'Go to Teleweb,make sure that two DB files exists in this version')
		return
	
        ##update for modem version
	def getModemVersion(self, version):
		##beetlelite CU
		if(version[1:2] == '7' or version[1:2] == 'X'):
			return 'MOLY.WR8.W1449.MD.WG.MP.V41'
		else:###other beetlelite project
			return 'MOLY.WR8.W1449.MD.WG.MP.V41'

        ##only for pixi4-5 upload img to weitai FTP --shie
	def uploadImgFTP(self,conf):
		projectname = conf.getConf('prlistprojname', 'Project name in PR List file name')
		version = conf.getConf('version','current version')
		mtkproject = conf.getConf('mtkproject','mtk product')	
                if conf.getConf('isDailyVersion', 'version is Daily version') == 'yes':
                        FTPpath = '/TCL/Pixi4-5/SW_release/DailyBuild'
                else:
                        FTPpath = '/TCL/Pixi4-5/SW_release/'
                imgPath = '/local/release/%s-release/' %projectname
		if os.path.exists('/local/release/%s-release/v%s' %(projectname,version)):
			print "begin to upload IMG to FTP"
                        os.system('/local/int_jenkins/misc/uploadImgTFTP.sh %s %s %s '%(FTPpath,projectname,version,)) 
                        return True               
                else:
                   	print "NO /local/release/%s-release/v%s, stop to upload to weitai FTP ,error!!!"
			print "please check and upload again" 
                        return False    


	def getMtkBaseLineCheck(self, version, conf):
		baseLine = conf.getConf('mtkBaseLine', 'mtk baseline info')
		gitDir = conf.getConf('projbuildroot', 'the project build dir') + 'v'+ version + '/device'
		os.chdir(gitDir)
		checkResult = commands.getstatusoutput('git grep %s' % baseLine)
		if checkResult[0] == 0 and len(checkResult[1]) >= 1:
			return 'True'
		else:
			return 'False'
