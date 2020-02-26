#!/bin/bash
if [[ $# < 1 ]];then
echo "usage : $0 /path/to/package-files.zip  <out.zip>"
exit 0
fi

#parameters-start
input_package=$1

if [[ -n "$2" ]];then
output_package=$2
else
output_package=/tmp/out.zip
fi

path_to_key="build/target/product/security"

#list of custpack applications which need signing with 'platform' key
#CustPackApkforPlatformKey="\
#framework-res.apk \
#Plugger.apk \
#Provision.apk \
#SyncMLClient.apk \
#JrdShared.apk"

#parameters-end

echo " exclude .apk file found in CUSTPACK :"
echo

#list all the applications in custpack
#CustpackApkList=$(unzip -l $1 | awk ' BEGIN {list="" } /CUSTPACK.*apk/ { res=gensub(/.*CUSTPACK.*\/(.*\.apk)/,"\\1 ","g"); list=res list } END{ print list }')  #ubuntu11.10 &12.04 not support this format,modify it the following format
#CustpackApkList=$(unzip -l $1 | awk '/CUSTPACK.*\.apk$/ {print $4}'|awk -F \/ '{print $NF}')

#build the script parameters for custpack apks
#for apkname in $CustpackApkList
#do
#if [[ -n $(echo $CustPackApkforPlatformKey | grep -i $apkname) ]];then
#  CustpackApkListCmd="$CustpackApkListCmd -e $apkname=$path_to_key/platform"
#else
#  CustpackApkListCmd="$CustpackApkListCmd -e $apkname= "
#fi
#done


#echo "custpack list : $CustpackApkListCmd "

build/tools/releasetools/sign_target_files_apks \
  -o \
  -d $path_to_key \
$CustpackApkListCmd \
-e GoogleCheckin.apk=build/target/product/security/platform \
-e GooglePartnerSetup.apk=build/target/product/security/platform \
-e GoogleSubscribedFeedsProvider.apk=build/target/product/security/platform \
-e NetworkLocation.apk=build/target/product/security/shared \
-e Settings.apk=build/target/product/security/platform \
-e AccountAndSyncSettings.apk=build/target/product/security/platform \
-e JrdUser2Root.apk=build/target/product/security/platform \
-e SystemUI.apk=build/target/product/security/platform \
-e SettingsProvider.apk=build/target/product/security/platform \
-e EngineerModeSim.apk=build/target/product/security/platform \
-e VpnServices.apk=build/target/product/security/platform \
-e AccountAndSyncSettings.apk=build/target/product/security/platform \
-e MobileLog.apk=build/target/product/security/platform \
-e ActivityNetwork.apk=build/target/product/security/platform \
-e AcwfDialog.apk=build/target/product/security/platform \
-e ModemLog.apk=build/target/product/security/platform \
-e TelephonyProvider.apk=build/target/product/security/platform \
-e StkSelection.apk=build/target/product/security/platform \
-e Stk.apk=build/target/product/security/platform \
-e CellConnService.apk=build/target/product/security/platform \
-e EngineerMode.apk=build/target/product/security/platform \
-e Phone.apk=build/target/product/security/platform \
-e JrdSetupWizard.apk=build/target/product/security/platform \
-e GoogleBackupTransport.apk=build/target/product/security/platform \
-e MediaTekLocationProvider.apk=build/target/product/security/platform \
-e TCLWatchDog.apk=build/target/product/security/platform \
-e MMITestdev.apk=build/target/product/security/platform \
-e WifiP2PWizardy.apk=build/target/product/security/platform \
-e YGPS.apk=build/target/product/security/platform \
-e DeskClock.apk=build/target/product/security/platform \
-e VpnServices.apk=build/target/product/security/platform \
-e EngineerModeSim.apk=build/target/product/security/platform \
-e StartupWizard_320x480_Alcatel_20110523_A1.02.00_TCT_Plat_signed.apk=build/target/product/security/platform \
-e Swype.apk=build/target/product/security/platform \
-e JrdFotaService.apk=build/target/product/security/platform \
-e SystemUI.apk=build/target/product/security/platform \
-e Settings.apk=build/target/product/security/platform \
-e JrdPowerSaver.apk=build/target/product/security/platform \
-e GoogleCalendarSyncAdapter.apk=build/target/product/security/shared \
-e GoogleContactsSyncAdapter.apk=build/target/product/security/shared \
-e QuickSearchBox.apk=build/target/product/security/shared \
-e GoogleServicesFramework.apk=build/target/product/security/shared \
-e Talk.apk=build/target/product/security/shared \
-e PinyinIME.apk=build/target/product/security/shared \
-e MtkWeatherProvider.apk=build/target/product/security/shared \
-e DownloadProvider.apk=build/target/product/security/media \
-e FMRadio.apk=build/target/product/security/media \
-e Gallery.apk=build/target/product/security/media \
-e DrmProvider.apk=build/target/product/security/media \
-e DownloadProviderUi.apk=build/target/product/security/media \
-e NotePad2.apk=build/target/product/security/releasekey \
-e MusicWidget.apk=build/target/product/security/releasekey \
-e JrdAlcatelHelp.apk=build/target/product/security/releasekey \
-e JrdEmailFeedback.apk=build/target/product/security/releasekey \
-e VoiceDialer.apk=build/target/product/security/releasekey \
-e GmailProvider.apk,\
Street.apk,\
EnhancedGoogleSearchProvider.apk,\
MediaUploader.apk,\
TalkProvider.apk,\
TclYouWen.apk,\
Tcloud.apk,\
YouTube.apk,Talk.apk,\
GoogleSettingsProvider.apk,\
SetupWizard.apk,\
Vending.apk,\
GoogleApps.apk,\
SogouInput.apk,\
BaiduSearch.apk,\
mobileqq.apk,\
Pinball.apk,\
dict.apk,\
SogouHaomatong.apk,\
Gmail.apk,\
MarketUpdater.apk,\
AIMail_Android_V370a_orange96.apk,\
unicomclient.apk,\
Android_v2.5.2.apk,\
iReader.apk,\
wo116114.apk,\
UCBrowser.apk,\
HmAgent.apk,\
Maps.apk,\
DeviceRegister_debug.apk,\
VoiceSearch.apk= \
$input_package $output_package

echo "signed package written in $output_package"
