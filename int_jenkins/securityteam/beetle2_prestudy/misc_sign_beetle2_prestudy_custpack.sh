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
CustPackApkforPlatformKey="\
framework-res.apk \
JrdShared.apk \
MediaTekLocationProvider.apk \
ActivityNetwork.apk \
CalendarImporter.apk \
MtkWeatherWidget.apk \
MobileLog.apk"

#parameters-end

echo " exclude .apk file found in CUSTPACK :"
echo

#list all the applications in custpack
#CustpackApkList=$(unzip -l $1 | awk ' BEGIN {list="" } /CUSTPACK.*apk/ { res=gensub(/.*CUSTPACK.*\/(.*\.apk)/,"\\1 ","g"); list=res list } END{ print list }')
CustpackApkList=$(unzip -l $1 | awk '/CUSTPACK.*\.apk$/ {print $4}'|awk -F \/ '{print $NF}')

#build the script parameters for custpack apks
for apkname in $CustpackApkList
do
if [[ -n $(echo $CustPackApkforPlatformKey | grep -i $apkname) ]];then
  CustpackApkListCmd="$CustpackApkListCmd -e $apkname=$path_to_key/platform"
else
  CustpackApkListCmd="$CustpackApkListCmd -e $apkname= "
fi
done


echo "custpack list : $CustpackApkListCmd "

build/tools/releasetools/sign_target_files_apks \
  -o \
  -d $path_to_key \
$CustpackApkListCmd \
-e MediaTekLocationProvider.apk=build/target/product/security/platform \
-e LocationEM.apk=build/target/product/security/platform \
-e QQGame.apk=build/target/product/security/platform \
-e SRSToolkit.apk=build/target/product/security/releasekey \
-e SRSTruMedia.apk=build/target/product/security/releasekey \
-e ActivityNetwork.apk=build/target/product/security/platform \
-e CalendarImporter.apk=build/target/product/security/platform \
-e MtkWeatherWidget.apk=build/target/product/security/platform \
-e MobileLog.apk=build/target/product/security/platform \
-e GoogleCheckin.apk=build/target/product/security/platform \
-e GooglePartnerSetup.apk=build/target/product/security/releasekey \
-e GoogleSubscribedFeedsProvider.apk=build/target/product/security/platform \
-e NetworkLocation.apk=build/target/product/security/shared \
-e Settings.apk=build/target/product/security/platform \
-e AccountAndSyncSettings.apk=build/target/product/security/platform \
-e SystemUI.apk=build/target/product/security/platform \
-e SettingsProvider.apk=build/target/product/security/platform \
-e EngineerModeSim.apk=build/target/product/security/platform \
-e VpnServices.apk=build/target/product/security/platform \
-e PinyinIME.apk=build/target/product/security/shared \
-e YGPS.apk=build/target/product/security/platform \
-e MobileLog.apk=build/target/product/security/platform \
-e ActivityNetwork.apk=build/target/product/security/platform \
-e MtkWeatherProvider.apk=build/target/product/security/shared \
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
-e GoogleCalendarSyncAdapter.apk=build/target/product/security/shared \
-e GoogleContactsSyncAdapter.apk=build/target/product/security/shared \
-e GoogleServicesFramework.apk=build/target/product/security/shared \
-e StartupWizard_320x480_Alcatel_20110523_A1.02.00_TCT_Plat_signed.apk=build/target/product/security/platform \
-e Swype.apk=build/target/product/security/platform \
-e JrdUser2Root.apk=build/target/product/security/platform \
-e JrdPowerSaver.apk=build/target/product/security/platform \
-e MMITestdev.apk=build/target/product/security/platform \
-e JrdFotaService.apk=build/target/product/security/platform \
-e TrafficManager.apk=build/target/product/security/releasekey \
-e SetupWizard.apk=build/target/product/security/releasekey \
-e JrdAlcatelHelp.apk=build/target/product/security/releasekey \
-e JrdDigitalClock.apk=build/target/product/security/releasekey \
-e JrdWorldClock.apk=build/target/product/security/releasekey \
-e NotePad2.apk=build/target/product/security/releasekey \
-e MusicWidget.apk=build/target/product/security/releasekey \
-e JrdLauncher.apk=build/target/product/security/shared \
-e Facebook_APKInstaller.apk=build/target/product/security/shared \
-e GmailProvider.apk,\
FaceLock.apk,\
GoogleFeedback.apk,\
Maps_alldpi.apk,\
Phonesky.apk,\
VideoEditorGoogle.apk,\
Books.apk,\
GalleryGoogle.apk,\
GoogleLoginService.apk,\
Maps_hdpi.apk,\
PlusOne.apk,\
Videos.apk,\
BrowserProviderProxy.apk,\
GenieWidget.apk,\
GooglePartnerSetup.apk,\
Maps_mdpi.apk,\
SetupWizard.apk,\
VoiceSearchStub.apk,\
CalendarGoogle.apk,\
Gmail.apk,\
GoogleServicesFramework.apk,\
Maps_xhdpi.apk,\
Street.apk,\
YouTube.apk,\
Chrome.apk,\
GmsCore.apk,\
GoogleTTS.apk,\
MediaUploader.apk,\
TagGoogle.apk,\
ChromeBookmarksSyncAdapter.apk,\
GoogleBackupTransport.apk,\
LatinImeDictionaryPack.apk,\
Music2.apk,\
Talk.apk,\
Chrome_x86.apk,\
GoogleCalendarSyncAdapter.apk,\
LatinImeGoogle.apk,\
NetworkLocation.apk,\
talkback.apk,\
DeskClockGoogle.apk,\
GoogleContactsSyncAdapter.apk,\
Magazines.apk,\
OneTimeInitializer.apk,\
Velvet.apk,\
Bebbled.apk,\
Pinball.apk,\
Android_UCBrowser.apk,\
BaiduMap.apk,\
BaiduSearch.apk,\
Dict.apk,\
IReader.apk,\
ShouJiMail.apk,\
ShouJiYingYeTing.apk,\
Weibo.apk,\
WOShangDian.apk,\
Street.apk,\
EnhancedGoogleSearchProvider.apk,\
MediaUploader.apk,\
TalkProvider.apk,\
YouTube.apk,Talk.apk,\
GoogleSettingsProvider.apk,\
Vending.apk,\
gtalkservice.apk,\
GoogleApps.apk,\
Gmail.apk,\
MarketUpdater.apk,\
HmAgent.apk,\
Maps.apk,\
Wo3G.apk,\
DeviceRegister_debug.apk,\
Apps_on_SD_TCL.apk,\
wo116114.apk,\
GMS_Maps.apk,\
moffice_unicom.apk,\
Weibo_unicom.apk,\
QQ_2012_3_unicom.apk,\
DeviceRegister.apk,\
BaiduSearch_unicom.apk,\
PhotoWonder_unicom.apk,\
IReader_unicom.apk,\
pris_unicom.apk,\
Tcloudstore_unicom.apk,\
SogouInput_unicom.apk,\
VoiceSearch.apk= \
$input_package $output_package

echo "signed package written in $output_package"
