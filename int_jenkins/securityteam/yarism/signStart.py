#!/usr/bin/python
####################################
#build for sign apks
####################################

from Utils import *
import os
import sys

class sign_apk():
	tmpdir = ''
	def __init__(self,secchar):
		print 'start ...'
		self.tmpdir = os.getcwd()
                self.secchar = secchar
		print 'tmpdir=',self.tmpdir
	def replace(self):
		docmd('cp /local/int_tools/securityteam/yarism/misc_sign_yarism_custpack.sh .')
                os.chdir('build/target/product')
                docmd('zip -r security_bc.zip security')
                os.chdir(self.tmpdir)
		docmd('rm -rf build/target/product/security/*')
		docmd('cp /local/int_tools/securityteam/yarism/TCT_ReleaseKeys.zip build/target/product/security/')
		os.chdir('build/target/product/security')
		docmd('unzip TCT_ReleaseKeys.zip')
	
	def build(self):

		docmd('./makeMtk -t -o=TARGET_BUILD_VARIANT=user jrdhz72_we_jb3 dist')
		
	def signapk(self):
		pl = []
                pl.append('Enter\spassword\sfor\sbuild\/target\/product\/security\/media\skey>')
                pl.append('Enter\spassword\sfor\sbuild\/target\/product\/security\/platform\skey>')
                pl.append('Enter\spassword\sfor\sbuild\/target\/product\/security\/releasekey\skey>')
                pl.append('Enter\spassword\sfor\sbuild\/target\/product\/security\/shared\skey>')
                pl.append('done\.')

		os.chdir(self.tmpdir)
		print 'start sign the apks ..................'
		child = pexpect.spawn('bash ./misc_sign_yarism_custpack.sh out/dist/jrdhz72_we_jb3-target_*.zip out/dist/sign_target_apks.zip')
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
		docmd('mv out/target/product/jrdhz72_we_jb3/custpack.img out/target/product/jrdhz72_we_jb3/custpack.img_unsign')
		docmd('mv out/target/product/jrdhz72_we_jb3/system.img out/target/product/jrdhz72_we_jb3/system.img_unsign')
		docmd('mv out/target/product/jrdhz72_we_jb3/recovery.img out/target/product/jrdhz72_we_jb3/recovery.img_unsign')
		docmd('mv out/target/product/jrdhz72_we_jb3/boot.img out/target/product/jrdhz72_we_jb3/boot.img_unsign')
		docmd('cp out/dist/sign_img/custpack.img out/target/product/jrdhz72_we_jb3/')
		docmd('cp out/dist/sign_img/system.img out/target/product/jrdhz72_we_jb3/')
		docmd('cp out/dist/sign_img/recovery.img out/target/product/jrdhz72_we_jb3/')
		docmd('cp out/dist/sign_img/boot.img out/target/product/jrdhz72_we_jb3/')
		docmd('perl mediatek/build/tools/jrd_magic.pl jrdhz72_we_jb3')

	def CheckSign(self):
		docmd("rm -rf /tmp/checksign && mkdir /tmp/checksign")
		docmd('unzip otacerts.zip -d /tmp/checksign')
	        f_key = open('/tmp/checksign/keys')
	        for line in f_key.readlines():
	                if line.find('1795090719') != -1:
	                        print "recovery.img is not signed with release key"
	                        sys.exit(1)
	                else:
	                        print "recovery.img has been signed with release key"
	        f_key.close()
	        docmd("unzip /tmp/checksign/otacerts.zip -d /tmp/checksign")
	        if os.path.isfile("/tmp/checksign/build/target/product/security/releasekey.x509.pem"):
	                print "system.img has been signed with release key"
	        else:
	                print "system.img is not signed with release key"
	                sys.exit(1)
	        docmd("rm -rf /tmp/checksign")

if __name__ == "__main__":
	#if len(sys.argv) != 2:
	#	print "You must give a argument!"
	#	sys.exit(1)
	dist = sign_apk(sys.argv[1])
	#dist = sign_apk('')
	dist.build()
	dist.replace()
	dist.signapk()
	#dist.CheckSign()
	os.chdir('build/target/product')
	docmd('rm -rf security')
	docmd('unzip -o security_bc.zip')
	docmd('rm security_bc.zip')
