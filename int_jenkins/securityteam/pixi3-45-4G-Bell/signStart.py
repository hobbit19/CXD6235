#!/usr/bin/python
####################################
#build for sign apks
####################################

from Utils import *
import os
import sys

class sign_apk():
	tmpdir = ''
	def __init__(self,secchar,compilePrj,version):
		print 'start ...'
		self.tmpdir = os.getcwd()
                self.secchar = secchar
		self.compilePrj = compilePrj
                self.version = version
		print 'tmpdir=',self.tmpdir
	def replace(self):
		docmd('cp /local/int_tools/securityteam/pixi3-45-4G-Bell/sign_target_files_apks.py .')
		#docmd('cp /local/int_tools/securityteam/jrd_magic.pl mediatek/build/tools/')
#googlesecurity
		docmd('mv build/target/product/security build/target/product/googlesecurity')
                docmd('mkdir -p build/target/product/security')
                #docmd('zip -r security_bc.zip security')
                os.chdir(self.tmpdir)
		#docmd('rm -rf build/target/product/security/*')
		docmd('cp /local/int_tools/securityteam/pixi3-45-4G-Bell/TCL_ReleaseKeys.zip build/target/product/security/')
		os.chdir('build/target/product/security')
		docmd('unzip TCL_ReleaseKeys.zip')
		print 'end replace -----'
	
	def build(self):
		#docmd('./makeMtk -t -o=TARGET_BUILD_VARIANT=user %s dist'%self.compilePrj)
		os.chdir(self.tmpdir)
		docmd('rm out/target/product/%s/system/plugin/Signatures/mplugin_guard.xml' %self.compilePrj)
		docmd('cp /local/int_tools/securityteam/pixi3-45-4G-Bell/mplugin_guard.xml out/target/product/%s/system/plugin/Signatures/'%self.compilePrj)
		#docmd('make dist')
                #Added by yufei.qin for pixi3-45-4g eng version signature  20150423
                #if self.version[2:3] == 'C':
		if self.version != None:
			if self.version[2:3] == 'D':
				docmd('./tclMake -o=TARGET_BUILD_VARIANT=user,EMMC_WRITE_PROTECT=false %s dist' %self.compilePrj)
			if self.version[2:3] == '9':
                                docmd('./tclMake -o=TARGET_BUILD_VARIANT=user,EMMC_WRITE_PROTECT=true %s dist' %self.compilePrj)
                else:
                        docmd('./tclMake -o=TARGET_BUILD_VARIANT=user,EMMC_WRITE_PROTECT=true %s dist' %self.compilePrj)
		#pass
		
	def signapk(self):
		pl = []
                pl.append('Enter\spassword\sfor\sbuild\/target\/product\/security\/media\skey>')
                pl.append('Enter\spassword\sfor\sbuild\/target\/product\/security\/platform\skey>')
                pl.append('Enter\spassword\sfor\sbuild\/target\/product\/security\/releasekey\skey>')
                pl.append('Enter\spassword\sfor\sbuild\/target\/product\/security\/shared\skey>')
                pl.append('done\.')

		os.chdir(self.tmpdir)
		print 'start sign the apks ..................'
		child = pexpect.spawn('./sign_target_files_apks.py out/dist/full_%s-target_*.zip out/dist/sign_target_apks.zip'%self.compilePrj)
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
		docmd('mv out/target/product/%s/custpack.img out/target/product/%s/custpack.img_unsign'%(self.compilePrj,self.compilePrj))
		docmd('mv out/target/product/%s/system.img out/target/product/%s/system.img_unsign'%(self.compilePrj,self.compilePrj))
		docmd('mv out/target/product/%s/recovery.img out/target/product/%s/recovery.img_unsign'%(self.compilePrj,self.compilePrj))
		docmd('mv out/target/product/%s/boot.img out/target/product/%s/boot.img_unsign'%(self.compilePrj,self.compilePrj))
		docmd('cp out/dist/sign_img/custpack.img out/target/product/%s/'%self.compilePrj)
		docmd('cp out/dist/sign_img/system.img out/target/product/%s/'%self.compilePrj)
		docmd('cp out/dist/sign_img/recovery.img out/target/product/%s/'%self.compilePrj)
		docmd('cp out/dist/sign_img/boot.img out/target/product/%s/'%self.compilePrj)
		docmd('perl /local/int_tools/securityteam/jrd_magic.pl %s'%self.compilePrj)
        
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
	if len(sys.argv)==4:
		if sys.argv[3][2:3] == 'D':
			dist = sign_apk(sys.argv[1], 'jhz6735m_35u_na_l',sys.argv[3])
		if sys.argv[3][2:3] == '9':
                        dist = sign_apk(sys.argv[1], 'jhz6735m_35u_na_l',sys.argv[3])
	else:
		dist = sign_apk(sys.argv[1], 'jhz6735m_35u_na_l',None)
	#dist = sign_apk('')
	#dist = sign_apk('')
	dist.build()
	dist.replace()
	dist.signapk()
	#dist.CheckSign()
	os.chdir('build/target/product')
	docmd('rm -rf security')
	docmd('mv googlesecurity security')
	#docmd('rm security_bc.zip')
