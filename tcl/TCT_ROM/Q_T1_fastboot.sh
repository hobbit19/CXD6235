#!/bin/bash
path=`pwd`
echo "Please copy amss_2019_spf2.0\SM6150.LA.2.0\common\build\ufs and out\target\product\t1 on this path or an path"


if [ -z "$1" ]; then
	echo "Usage: Need 1 Params "
	img_dir=$path
else
   img_dir=$1
fi

cd $img_dir
echo $img_dir

deviceinfo=$(adb devices)
echo -e "$deviceinfo"
if [ "$deviceinfo" == "" ];then
 adb wait-"for"-device
 sleep 2
fi

echo "----------begin fastboot-------"
echo "adb root"
adb root
echo "adb reboot-bootloader"
adb reboot-bootloader
echo "fastboot amss_2019_spf2.0\SM6150.LA.2.0\common\build\ufs"
fastboot flash xbl 'xbl.elf'
fastboot flash xbl_config 'xbl_config.elf'
fastboot flash tz 'tz.mbn'
fastboot flash aop 'aop.mbn'
fastboot flash hyp 'hyp.mbn'
fastboot flash keymaster 'km4.mbn'
fastboot flash cmnlib 'cmnlib.mbn'
fastboot flash cmnlib64 'cmnlib64.mbn'
fastboot flash modem 'NON-HLOS.bin'
fastboot flash dsp 'dspso.bin'
fastboot flash bluetooth 'BTFM.bin'
fastboot flash imagefv 'imagefv.elf'
fastboot flash uefisecapp 'uefi_sec.mbn'
fastboot flash devcfg 'devcfg.mbn'
fastboot flash qupfw 'qupv3fw.elf'
fastboot flash logfs 'logfs_ufs_8mb.bin'
fastboot flash storsec 'storsec.mbn'
echo "fast android"
fastboot flash boot 'boot.img'
fastboot flash userdata 'userdata.img'
fastboot flash vendor 'vendor.img'
fastboot flash system 'system.img'
fastboot flash vbmeta 'vbmeta.img' --disable-verification
fastboot flash dtbo 'dtbo.img'
fastboot flash abl 'abl.elf'
fastboot flash recovery 'recovery.img'
fastboot flash cache 'cache.img'
echo "erase modemst1 and modemst2"
fastboot erase modemst1
fastboot erase modemst2
echo "fastboot reboot"
fastboot reboot
echo "----------end fastboot-------"
