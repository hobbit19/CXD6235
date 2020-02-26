#!/usr/bin/python
####################################
#build for sign apks
####################################

from Utils import *
import os
import sys
import linecache
from commands import *

class sign_apk():
	tmpdir = ''
	def __init__(self,secchar):
		print 'start ...'
		self.tmpdir = os.getcwd()
		#chargeversion = getoutput("sed -n '1p' version_rav4/version.inc |awk '{print $3}'|cut -c3-4")
		#if chargeversion == "1G" or chargeversion == "2G":
		self.compiledir = 'jrdsz89_cu_jb2'
		#else:
		#	self.compiledir = 'jrdsz89_cu_jb'
                self.secchar = secchar
		print 'tmpdir=',self.tmpdir
	def replace(self):
		print 'replace ...'
		docmd('cp /local/int_tools/securityteam/beetle2_prestudy/misc_sign_beetle2_prestudy_custpack.sh .')
                os.chdir('build/target/product')
                docmd('zip -r security_bc.zip security')
                os.chdir(self.tmpdir)
		docmd('rm -rf build/target/product/security/*')
		docmd('cp /local/int_tools/securityteam/beetle2_prestudy/TCT_ReleaseKeys.zip build/target/product/security/')
		os.chdir('build/target/product/security')
		docmd('unzip TCT_ReleaseKeys.zip')
	
	def build(self):
		docmd('./makeMtk -t -o=TARGET_BUILD_VARIANT=user,JRD_GEMINI_SUPPORT=yes,JRD_CU_SUPPORT=no,JRD_GMS_SUPPORT=no jrdsz89_cu_jb2 dist')

		
	def signapk(self):
		pl = []
                pl.append('Enter\spassword\sfor\sbuild\/target\/product\/security\/media\skey>')
                pl.append('Enter\spassword\sfor\sbuild\/target\/product\/security\/platform\skey>')
                pl.append('Enter\spassword\sfor\sbuild\/target\/product\/security\/releasekey\skey>')
                pl.append('Enter\spassword\sfor\sbuild\/target\/product\/security\/shared\skey>')
                pl.append('done\.')

		os.chdir(self.tmpdir)
		print 'start sign the apks ..................'
		child = pexpect.spawn('bash ./misc_sign_beetle2_prestudy_custpack.sh out/dist/%s-target_*.zip out/dist/sign_target_apks.zip'%self.compiledir)
		child.logfile = sys.stdout
                cpl = child.compile_pattern_list(pl)
                flag = True
                while flag:
                        try:
                                index = child.expect_list(cpl,child.timeout)
                                if index == 0:
                                        child.sendline(self.secchar)
                                if index == 1:
                                        child.sendline(self.secchar)
                                if index == 2:
                                        child.sendline(self.secchar)
                                if index == 3:
                                        child.sendline(self.secchar)
                                if index == 4:
                                        continue
                        except pexpect.EOF:
                        	flag = False
                        except pexpect.TIMEOUT:
                                continue

		print 'start gen the imgs ...................'
		docmd('build/tools/releasetools/img_from_target_files out/dist/sign_target_apks.zip out/dist/signed-img.zip')
		os.chdir('out/dist')
		if os.path.exists('sign_img'):
			docmd('rm -rf sign_img')
		docmd('mkdir sign_img')
		os.chdir('sign_img')
		print('start unzip the imgs')
		docmd('unzip ../signed-img.zip')
		os.chdir(self.tmpdir)
		docmd('mv out/target/product/%s/custpack.img  out/target/product/%s/custpack.img_unsign'%(self.compiledir,self.compiledir))
		docmd('mv out/target/product/%s/system.img    out/target/product/%s/system.img_unsign'%(self.compiledir,self.compiledir))
		docmd('mv out/target/product/%s/recovery.img  out/target/product/%s/recovery.img_unsign'%(self.compiledir,self.compiledir))
		docmd('mv out/target/product/%s/boot.img      out/target/product/%s/boot.img_unsign'%(self.compiledir,self.compiledir))
		docmd('cp out/dist/sign_img/custpack.img out/target/product/%s/'%self.compiledir)
		docmd('cp out/dist/sign_img/system.img   out/target/product/%s/'%self.compiledir)
		docmd('cp out/dist/sign_img/recovery.img out/target/product/%s/'%self.compiledir)
		docmd('cp out/dist/sign_img/boot.img     out/target/product/%s/'%self.compiledir)                     

if __name__ == "__main__":
	#if len(sys.argv) != 2:
	#	print "You must give a argument!"
	#	sys.exit(1)
	dist = sign_apk(sys.argv[1])
        #dist = sign_apk('')
        dist.build()
	dist.replace()
        dist.signapk()
        os.chdir('build/target/product')
        docmd('rm -rf security')
        docmd('unzip -o security_bc.zip')
        docmd('rm security_bc.zip')

