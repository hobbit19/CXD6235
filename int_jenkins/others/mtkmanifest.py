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
from Config import *
sys.path.append('/local/int_jenkins/mtk_patch')
from Mangage import *
import smtplib
from email.header import Header
from email.MIMEText import MIMEText
import xml.dom.minidom
import datetime

def main():

                manifest = sys.argv[1]
                print "manifest is %s" %manifest
		dom = xml.dom.minidom.parse('%s' % manifest) 
		destDom = xml.dom.minidom.Document()
		manifestNode = destDom.createElement('manifest')
		destDom.appendChild(manifestNode)
		remoteList = dom.getElementsByTagName('remote')                              
		remoteNode = remoteList[0]               
		remoteNode.setAttribute('fetch', 'https://git01.mediatek.com/alps_release/')
		newNode = destDom.importNode(remoteNode, True)	
		manifestNode.appendChild(newNode)   
		defaultList = dom.getElementsByTagName('default')
		DefaultNode = defaultList[0]
		DefaultNode.setAttribute('sync-c', 'true')
		newNode = destDom.importNode(DefaultNode, True)	
		manifestNode.appendChild(newNode)  
		projList = dom.getElementsByTagName('project')  
		for proj in projList:
                            newNode = destDom.importNode(proj, True)                          
			    projClone = proj.getAttribute('clone-depth') 
			    if projClone != '':
                                  newNode.removeAttribute('clone-depth')                            
                            manifestNode.appendChild(newNode)
		#print "manifestNode",manifestNode
		xmlExisting = False
		workDir=os.getcwd()
		if os.path.isfile('%s' % (manifest)):
		           os.system('rm -f %s' % (manifest))
		fp = file('%s' % (manifest), 'w+')
		for line in destDom.toprettyxml(indent='  ', newl='\n', encoding='utf-8').split('\n'):
		           if line and not re.match('^\s*$', line):
			      fp.write('%s\n' % line)
		fp.close()
		print "%s has been updated" %manifest




if __name__ == "__main__":
        main()

	
