#!/bin/bash
path=`pwd`

cd $path
echo $path

if [ -z "$1" ]; then
	echo "Usage: Need 1 Params!!!! Please input system img name,like Y***.mbn"
	exit 1
else
systemimg=$1
fi

deviceinfo=$(adb devices)
echo -e "$deviceinfo"
if [ "$deviceinfo" == "" ];then
 adb wait-"for"-device
 sleep 2

fi

echo "----------begin fastboot-------"
echo "adb reboot bootloader"
adb reboot bootloader
echo "fastboot oem unlock"
fastboot oem unlock
echo "fastboot flash system $systemimg" 
fastboot flash system $systemimg
echo "fastboot -w"
fastboot -w
echo "fastboot reboot"
fastboot reboot


echo "----------end fastboot-------"

