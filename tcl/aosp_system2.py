#!/usr/bin/python
#coding=utf-8
#
#aosp system.img 2
#

###
#GSI刷机步骤如下：
#1	adb reboot-bootloader
#2	long press Volume up key
'''	
=> FASTBOOT mode...
'''
#3	fastboot flashing unlock
'''
Unlock bootloader?

If you unlock the bootloader,you will be able to install cus
tom operating
system software on this phone.

A custom OS is not subject to the same testing as the origin
al OS, and can 
cause your phone and installed applications to stop working 
properly.

To prevent unauthorized accesss to your personal data,unlocki
ng the bootloader
will also delete all personal data from your phone(a "factor
y data reset").

Press the Volum UP/Down buttons to select Yes or No.

Yes (Volume UP):Unlock(may void warranty).

No (Volume Down):Do not unlock bootloader.
'''
#4	Press Volume up key
'''
Select Boot Mode:
[VOLUME_UP to select.VOLUME_DOWN is OK.]

[Recovery 	Mode]
[Fastboot 	Mode]		<<==
[Normal 	Boot]
=> FASTBOOT mode...
'''
#5	fastboot flash system /home/swd3/android-sdk-linux/GSIs/system_aosp_arm_a/system_aosp_arm_a_20180205.img
'''
USB Transferring...
'''
'''
USB Transferring...
USB Transmission OK Time:-296030ms Vel:0KB/s

Writing Flash
Write Data > 100% Time: 3s Vel:202796KB/s/s

OK
'''
#6	fastboot reboot

#img_dir			aosp img director
#sdk_platform_tools_dir 	sdk platform-tools director
#adb
import os,sys
import commands
import time

class operation_state(object):
	def __init__(self,img_dir):
		self.img_dir = img_dir
	def handle(self):
		pass
class adb_root(operation_state):
	def handle(self):
		print "adb root"
		os.system("adb root")
class adb_disable_verity(operation_state):
	def handle(self):
		print "adb disable-verity"
		os.system("adb disable-verity")
class reboot_bootloader(operation_state):
	def handle(self):
		while True:
			enable = raw_input("Please enable OEM unlocking and USB debugging in settings.Y|N:\n")
			if enable == "Y" or enable =="y" or enable== "":
				print "adb reboot-bootloader"
				#os.system("adb reboot-bootloader")
				os.system("adb reboot-bootloader")
				print "Long press Volume up key into FASTBOOT mode.And then you will see this at the bottom of the phone:"
				print "=> FASTBOOT mode..."
				#time.sleep(10)
				break
			elif enable == "N" or enable =="n":
				print "wait for 1s"
				time.sleep(1)
			else:
				print "invalid input!!!system exit."
				sys.exit(2)
	
class flashing_unlock(operation_state):
	def handle(self):
		while True:
			enable = raw_input("Do you see 'FASTBOOT mode...'?Y|N:\n")
			if enable == "Y" or enable =="y" or enable== "":
				print "fastboot flashing unlock"
				tmp_strs = \
'''
Unlock bootloader?

If you unlock the bootloader,you will be able to install cus
tom operating
system software on this phone.

A custom OS is not subject to the same testing as the origin
al OS, and can 
cause your phone and installed applications to stop working 
properly.

To prevent unauthorized accesss to your personal data,unlocki
ng the bootloader
will also delete all personal data from your phone(a "factor
y data reset").

Press the Volum UP/Down buttons to select Yes or No.

Yes (Volume UP):Unlock(may void warranty).

No (Volume Down):Do not unlock bootloader.
'''
				print "you will see something like this:"
				print tmp_strs
				print "Press Volum UP to unlock the bootloader."
				#os.system("fastboot flashing unlock")
				os.system("fastboot flashing unlock")
				enable = raw_input("Do you do this?Y|N:\n")
				while True:
					if enable == "Y" or enable =="y" or enable== "":
						print "you will see something like this:"
						tmp_strs = \
'''
Select Boot Mode:
[VOLUME_UP to select.VOLUME_DOWN is OK.]

[Recovery 	Mode]
[Fastboot 	Mode]		<<==
[Normal 	Boot]
=> FASTBOOT mode...
'''
						print tmp_strs
						time.sleep(1)
						break
					elif enable == "N" or enable =="n":
						print "wait for 1s"
						time.sleep(1)
					else:
						print "invalid input!!!system exit."
						sys.exit(2)
				break
			elif enable == "N" or enable =="n":
				print "wait for 1s"
				time.sleep(1)
			else:
				print "invalid input!!!system exit."
				sys.exit(2)
class flashing_oem_unlock(operation_state):
	def handle(self):
		while True:
			enable = raw_input("Do you see 'FASTBOOT mode...'?Y|N:\n")
			if enable == "Y" or enable =="y" or enable== "":
				print "fastboot flashing unlock"
				tmp_strs = \
'''
Unlock bootloader?

If you unlock the bootloader,you will be able to install cus
tom operating
system software on this phone.

A custom OS is not subject to the same testing as the origin
al OS, and can 
cause your phone and installed applications to stop working 
properly.

To prevent unauthorized accesss to your personal data,unlocki
ng the bootloader
will also delete all personal data from your phone(a "factor
y data reset").

Press the Volum UP/Down buttons to select Yes or No.

Yes (Volume UP):Unlock(may void warranty).

No (Volume Down):Do not unlock bootloader.
'''
				print "you will see something like this:"
				print tmp_strs
				print "Press Volum UP to unlock the bootloader."
				#os.system("fastboot flashing unlock")
				os.system("fastboot oem unlock")
				enable = raw_input("Do you do this?Y|N:\n")
				while True:
					if enable == "Y" or enable =="y" or enable== "":
						print "you will see something like this:"
						tmp_strs = \
'''
Select Boot Mode:
[VOLUME_UP to select.VOLUME_DOWN is OK.]

[Recovery 	Mode]
[Fastboot 	Mode]		<<==
[Normal 	Boot]
=> FASTBOOT mode...
'''
						print tmp_strs
						time.sleep(1)
						break
					elif enable == "N" or enable =="n":
						print "wait for 1s"
						time.sleep(1)
					else:
						print "invalid input!!!system exit."
						sys.exit(2)
				break
			elif enable == "N" or enable =="n":
				print "wait for 1s"
				time.sleep(1)
			else:
				print "invalid input!!!system exit."
				sys.exit(2)
class flash(operation_state):
	def handle(self):
		print "fastboot flash system '%s'" % self.img_dir
		print "you will see something like this:"
		print "USB Transferring..."
		print "after fastboot flash finished,you will see something like this:"
		tmp_strs = \
'''
USB Transferring...
USB Transmission OK Time:-296030ms Vel:0KB/s

Writing Flash
Write Data > 100% Time: 3s Vel:202796KB/s/s

OK
'''
		print tmp_strs
		os.system("fastboot flash system '%s'" % self.img_dir)
class flash_vbmeta(operation_state):
	def handle(self):
		_img_dir = os.path.dirname(self.img_dir)+"/vbmeta.img"
		print "fastboot flash vbmeta '%s' --disable-verification" % _img_dir
		#os.system("fastboot flash system '%s'" % vbmetaimg_dir)
		os.system("fastboot flash vbmeta '%s' --disable-verification" % _img_dir)

class flash_vendor(operation_state):
	def handle(self):
		_img_dir = os.path.dirname(self.img_dir) + "/vendor.img"
                print "fastboot flash vendor '%s'" % _img_dir
		os.system("fastboot flash vendor '%s'" % _img_dir)
	
class fastboot_w(operation_state):
	def handle(self):
		print "fastboot -w"
		os.system("fastboot -w")
		print "fastboot -w finished."		

class reboot(operation_state):
	def handle(self):
		print "fastboot reboot"
		os.system("fastboot reboot")
		print "All operation has been completed.Wait for the phone to reboot."
class flash_xbl(operation_state):
	def handle(self):
		_img_dir = os.path.dirname(self.img_dir) + "/xbl.elf"
                print "fastboot flash xbl '%s'" % _img_dir
		os.system("fastboot flash xbl '%s'" % _img_dir)
class flash_xbl_config(operation_state):
	def handle(self):
		_img_dir = os.path.dirname(self.img_dir) + "/xbl_config.elf"
                print "fastboot flash xbl_config '%s'" % _img_dir
		os.system("fastboot flash xbl_config '%s'" % _img_dir)
class flash_tz(operation_state):
	def handle(self):
		_img_dir = os.path.dirname(self.img_dir) + "/tz.mbn"
                print "fastboot flash tz '%s'" % _img_dir
		os.system("fastboot flash tz '%s'" % _img_dir)
class flash_aop(operation_state):
	def handle(self):
		_img_dir = os.path.dirname(self.img_dir) + "/aop.mbn"
                print "fastboot flash aop '%s'" % _img_dir
		os.system("fastboot flash aop '%s'" % _img_dir)
class flash_hyp(operation_state):
	def handle(self):
		_img_dir = os.path.dirname(self.img_dir) + "/hyp.mbn"
                print "fastboot flash hyp '%s'" % _img_dir
		os.system("fastboot flash hyp '%s'" % _img_dir)

class flash_keymaster(operation_state):
	def handle(self):
		_img_dir = os.path.dirname(self.img_dir) + "/km4.mbn"
                print "fastboot flash keymaster '%s'" % _img_dir
		os.system("fastboot flash keymaster '%s'" % _img_dir)

class flash_cmnlib(operation_state):
	def handle(self):
		_img_dir = os.path.dirname(self.img_dir) + "/cmnlib.mbn"
                print "fastboot flash cmnlib '%s'" % _img_dir
		os.system("fastboot flash cmnlib '%s'" % _img_dir)

class flash_cmnlib64(operation_state):
	def handle(self):
		_img_dir = os.path.dirname(self.img_dir) + "/cmnlib64.mbn"
                print "fastboot flash cmnlib64 '%s'" % _img_dir
		os.system("fastboot flash cmnlib64 '%s'" % _img_dir)

class flash_modem(operation_state):
	def handle(self):
		_img_dir = os.path.dirname(self.img_dir) + "/NON-HLOS.bin"
                print "fastboot flash modem '%s'" % _img_dir
		os.system("fastboot flash modem '%s'" % _img_dir)

class flash_dsp(operation_state):
	def handle(self):
		_img_dir = os.path.dirname(self.img_dir) + "/dspso.bin"
                print "fastboot flash dsp '%s'" % _img_dir
		os.system("fastboot flash dsp '%s'" % _img_dir)

class flash_bluetooth(operation_state):
	def handle(self):
		_img_dir = os.path.dirname(self.img_dir) + "/BTFM.bin"
                print "fastboot flash bluetooth '%s'" % _img_dir
		os.system("fastboot flash bluetooth '%s'" % _img_dir)

class flash_imagefv(operation_state):
	def handle(self):
		_img_dir = os.path.dirname(self.img_dir) + "/imagefv.elf"
                print "fastboot flash imagefv '%s'" % _img_dir
		os.system("fastboot flash imagefv '%s'" % _img_dir)

class flash_uefisecapp(operation_state):
	def handle(self):
		_img_dir = os.path.dirname(self.img_dir) + "/uefi_sec.mbn"
                print "fastboot flash uefisecapp '%s'" % _img_dir
		os.system("fastboot flash uefisecapp '%s'" % _img_dir)

class flash_devcfg(operation_state):
	def handle(self):
		_img_dir = os.path.dirname(self.img_dir) + "/devcfg.mbn"
                print "fastboot flash devcfg '%s'" % _img_dir
		os.system("fastboot flash devcfg '%s'" % _img_dir)
class flash_qupfw(operation_state):
	def handle(self):
		_img_dir = os.path.dirname(self.img_dir) + "/qupv3fw.elf"
                print "fastboot flash qupfw '%s'" % _img_dir
		os.system("fastboot flash qupfw '%s'" % _img_dir)
class flash_logfs(operation_state):
	def handle(self):
		_img_dir = os.path.dirname(self.img_dir) + "/logfs_ufs_8mb.bin"
                print "fastboot flash logfs '%s'" % _img_dir
		os.system("fastboot flash logfs '%s'" % _img_dir)
class flash_storsec(operation_state):
	def handle(self):
		_img_dir = os.path.dirname(self.img_dir) + "/storsec.mbn"
                print "fastboot flash storsec '%s'" % _img_dir
		os.system("fastboot flash storsec '%s'" % _img_dir)
class flash_boot(operation_state):
	def handle(self):
		_img_dir = os.path.dirname(self.img_dir) + "/boot.img"
                print "fastboot flash boot '%s'" % _img_dir
		os.system("fastboot flash boot '%s'" % _img_dir)
class flash_userdata(operation_state):
	def handle(self):
		_img_dir = os.path.dirname(self.img_dir) + "/userdata.img"
                print "fastboot flash userdata '%s'" % _img_dir
		os.system("fastboot flash userdata '%s'" % _img_dir)
class flash_dtbo(operation_state):
	def handle(self):
		_img_dir = os.path.dirname(self.img_dir) + "/dtbo.img"
                print "fastboot flash dtbo '%s'" % _img_dir
		os.system("fastboot flash dtbo '%s'" % _img_dir)
class flash_abl(operation_state):
	def handle(self):
		_img_dir = os.path.dirname(self.img_dir) + "/abl.elf"
                print "fastboot flash abl '%s'" % _img_dir
		os.system("fastboot flash abl '%s'" % _img_dir)
class flash_recovery(operation_state):
	def handle(self):
		_img_dir = os.path.dirname(self.img_dir) + "/recovery.img"
                print "fastboot flash recovery '%s'" % _img_dir
		os.system("fastboot flash recovery '%s'" % _img_dir)
class flash_cache(operation_state):
	def handle(self):
		_img_dir = os.path.dirname(self.img_dir) + "/cache.img"
                print "fastboot flash cache '%s'" % _img_dir
		os.system("fastboot flash cache '%s'" % _img_dir)
class erase_modemst1(operation_state):
	def handle(self):
                print "fastboot erase modemst1"
		os.system("fastboot erase modemst1")
class erase_modemst2(operation_state):
	def handle(self):
                print "fastboot erase modemst2"
		os.system("fastboot erase modemst2")

class environment(object):
	
	def __init__(self,dir_list):
		if type(dir_list) is str:
			dir_list = [dir_list]
		if len(dir_list) <= 0:
			print "env list memeber must be larger than 0"
			sys.exit(1)
		self._dir = ":".join(dir_list)
	def check_env(self,env_list = ['adb','fastboot']):
		if type(env_list) is str:
			env_list = [env_list]
		if len(env_list) <= 0:
			print "env list memeber must be larger than 0"
			sys.exit(1)
		assert type(env_list) is list,"env list must be string or list"
		env_ok = True	
		for env in env_list:
			#print "which %s" % env
			result = commands.getoutput("which %s" % env)
			#print result
			if len(result) > 0:
				env_ok = env_ok & True
			else:
				env_ok = env_ok & False
		#print env_ok
		#print self._dir
		#print commands.getoutput("echo $PATH")
		if not env_ok:
			os_path = os.getenv("PATH")
			os_path = self._dir + ':'  +  os_path
			os.putenv("PATH",os_path)	
		#print commands.getoutput("echo $PATH")
		#for env in env_list:
			#print "which %s" % env
			#result = commands.getoutput("which %s" % env)
			#print result


class contextFactory(object):
	def __init__(self,operations_list,img_dir):
		if type(operations_list) is str:
			operations_list = [operations_list]
		if type(operations_list) is list:
			self.operations_list = operations_list
		self.state = ''
		self.img_dir = img_dir
	def __next__(self):
		if len(self.operations_list) >0:
			self.operations_list.remove(self.operations_list[0])
			return True
		return False
	def __gene_state__(self):
		if len(self.operations_list)<=0:
			return ''
		_oper = self.check_expression(self.operations_list[0])
		if _oper:
			self.state = getattr(aosp,_oper)(img_dir)
			_next = self.__next__()
			if _next:
				return self.state
			else:
				return ''
		return ''
	def check_expression(self,operation):
		if operation:
			return 	operation
		return ''
	def operate(self):
		_state = self. __gene_state__()
		if _state:
			self.state.handle()
	def check_fish(self):
		if len(self.operations_list) > 0:
			return True
		return False

if __name__ == '__main__':
	#androidO1
	#operations = ['reboot_bootloader','flashing_oem_unlock','flash','fastboot_w','reboot']
	#androidP
	#operations = ['adb_root','adb_disable_verity','reboot_bootloader','flash_vbmeta','flash','fastboot_w','reboot']
	#operations = ['reboot_bootloader','flash_vbmeta','flash','fastboot_w','reboot']
	#android Q T1
	#amss_2019_spf2.0\SM6150.LA.2.0\common\build\ufs
	operations = [	'reboot_bootloader','flash_vbmeta','flash_xbl','flash_xbl_config','flash_tz','flash_aop',
			'flash_hyp','flash_keymaster','flash_cmnlib','flash_cmnlib64','flash_modem','flash_dsp',
			'flash_bluetooth','flash_imagefv','flash_uefisecapp','flash_devcfg','flash_qupfw',
			'flash_logfs','flash_storsec','flash_boot','flash_userdata','flash_vendor','flash',
			'flash_dtbo','flash_abl','flash_recovery','flash_cache','erase_modemst1','erase_modemst2','reboot']
	#operations = [	'reboot_bootloader','flash_vbmeta','flash_boot','flash_userdata','flash_vendor','flash',
	#		'flash_dtbo','flash_abl','flash_recovery','flash_cache','erase_modemst1','erase_modemst2','reboot']
	#img_dir	the img director of GSI IMG
	#img_dir = '/home/swd3/android-sdk-linux/GSIs/8.1_r4_arm_a/system-aosp_arm_a-2018-07-05.img'
	#img_dir = '/home/swd3/android-sdk-linux/GSIs/8.1_r4_arm_a/system-aosp_arm_a-2018-08-05.img'
	#img_dir = '/home/swd3/android-sdk-linux/GSIs/TCT_ROM/YQ1039032000.mbn'
	#img_dir = '/home/swd3/android-sdk-linux/GSIs/TCT_ROM/Gauss/YA10K9064000.mbn'
	img_dir = '/home/swd3/MTK_PATH/t1/system.img'
	#img_dir = '/home/swd3/android-sdk-linux/GSIs/TCT_ROM/benz/YQ10M9032000.mbn'
	#img_dir = '/home/swd3/android-sdk-linux/GSIs/TCT_ROM/a5x/YM10G9032000.mbn'
	pwd = os.getcwd()
	sys.path.append(pwd)
	aosp = __import__('aosp_system2')
	#env_dir	the platform-tools of the sdk director in your system
	#env_dir = '/local/tools/adt-bundle-linux-x86_64-20151030/sdk/platform-tools'
	env_dir = '/local/tools/adt-bundle-linux-x86_64-20161120/sdk/platform-tools'
	env = getattr(aosp,'environment')(env_dir)
	env.check_env()
	factory = contextFactory(operations,img_dir)
	while factory.check_fish():
		factory.operate()




