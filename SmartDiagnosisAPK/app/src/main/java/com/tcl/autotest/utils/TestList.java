package com.tcl.autotest.utils;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import com.tcl.autocase.BBFlashLEDTest;
import com.tcl.autocase.BatteryTempTest;
import com.tcl.autocase.BluetoothTest;
import com.tcl.autocase.ButtonBacklightTest;
import com.tcl.autocase.CapacitiveKeyBoardTest;
import com.tcl.autocase.CarrierSignalTest;
import com.tcl.autocase.CompassTest;
import com.tcl.autocase.FrontFlashLEDTest;
import com.tcl.autocase.HDCPTest;
import com.tcl.autocase.HallTest;
import com.tcl.autocase.IRTest;
import com.tcl.autocase.KeybacklightTest;
import com.tcl.autocase.Wifi_2_4GTest;
import com.tcl.autocase.Wifi_5GTest;
import com.tcl.manucase.AlsPsTest;
import com.tcl.manucase.BBKeyBoardTest;
import com.tcl.manucase.ChargerRGBLEDTest;
import com.tcl.manucase.DtvTest;
import com.tcl.manucase.DualCameraTest;
import com.tcl.manucase.FMTest;
import com.tcl.manucase.FingerEnrollTest;
import com.tcl.manucase.FingerprintTest;
import com.tcl.autocase.FlashLEDTest;
import com.tcl.autocase.GSensorTestAuto;
import com.tcl.autocase.GyroScopeTest;
import com.tcl.autocase.HeadSetAutoTest;
import com.tcl.autocase.LCDBacklightTest;
import com.tcl.autocase.LightSensorTestAuto;
import com.tcl.autocase.LightTest;
import com.tcl.autocase.MemorycardTest;
import com.tcl.autocase.NFC;
import com.tcl.autocase.OTGTest;
import com.tcl.autocase.PSensorTest;
import com.tcl.autocase.SIMTest;
import com.tcl.autocase.USBChargerTest;
import com.tcl.autocase.USBTest;
import com.tcl.manucase.HeadSetTest;
import com.tcl.manucase.VibratorTest;
import com.tcl.autocase.WifiTest;
import com.tcl.autotest.AllMainActivity;
import com.tcl.autotest.tool.Tool;
import com.tcl.manucase.ChargerLEDTest;
//import com.tcl.manucase.HeadSetTest;
import com.tcl.manucase.AudioTest;
import com.tcl.manucase.CameraTest;
import com.tcl.manucase.GPSSensorTest;
import com.tcl.manucase.GsensorCallibrationTest;
import com.tcl.manucase.KeyBoardTest;
import com.tcl.manucase.KeyPadTest;
import com.tcl.manucase.KeyPadAITest;
import com.tcl.manucase.LcdAllColorTest;
//import com.tcl.manucase.LcdBlackTest;
//import com.tcl.manucase.LcdGreyChartTest;
//import com.tcl.manucase.LcdGreyTest;
//import com.tcl.manucase.LcdMireRgbTest;
//import com.tcl.manucase.LcdWhiteTest;
import com.tcl.manucase.MICTest;
//import com.tcl.manucase.PointTest;
import com.tcl.manucase.PDAFTest;
import com.tcl.manucase.SubCameraTest;
import com.tcl.manucase.TPlockTest;
import com.tcl.manucase.TouchPanel;
//import com.tcl.manucase.Tp1Test;
//import com.tcl.manucase.Tp2Test;
import com.tcl.manucase.TracabilityTest;

import android.util.Log;

public class TestList {

	private static final ArrayList<Test> list = new ArrayList<Test>();
	private static final ArrayList<Test> autolist = new ArrayList<Test>();
	private static final String TAG = "TestList";
//	private static TracabilityStruct mTStruct;
	public static int autotestelementNum = 0;
	public static int elementNum = 0;
	private static  String PATH_JRDCAM_RESULT = "/sys/jrd_cam/sensor_module";
	private static boolean isOISMFlag = false;
	
	private static String readFile(String path) {

    	String s = "";
    	FileInputStream in = null;
    	
    	try {
    		
    		String temp = null;
		    in = new FileInputStream(path);
		    BufferedReader read = new BufferedReader(new InputStreamReader(in));
		    while((temp = read.readLine()) != null) {
		    	s = s + temp + "\n";
		    }
		    
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
                            s = "Read " + path + " fail...";//PR570967
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
                                 if (in != null)//PR570967
				   in.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
    	return s;
    }
	
	public static boolean isOISModule()
	{
		String str = readFile(PATH_JRDCAM_RESULT);
		Log.d("zzzz", str);
		if(str.startsWith("OIS module"))
		{
			isOISMFlag = true;
			Log.d("zzzz", "11111111111");
		}
		else {
			isOISMFlag = false;
			Log.d("zzzz", "22222222");
		}
		return isOISMFlag;
	}
	
//	public static boolean isDiablocamera()
//	{
//		if (mTStruct == null) {
//			try {
//				mTStruct = new TracabilityStruct();
//			} catch (Exception e) {
//				Log.d("TestList", e.toString());
//			}
//		}
		
//		boolean Diablocamera = false;
//		byte[] shortcode = mTStruct.getItem(TracabilityStruct.ID.SHORT_CODE_I);
//		String string = new String(shortcode);
//		if (string.startsWith("8FJ")) {
//			Diablocamera = true;
//		}else {
//			Diablocamera = false;
//		}
//		
//		return Diablocamera;
//	}
	
//	public static boolean isSingleSim() {
//		if (mTStruct == null) {
//			try {
//				mTStruct = new TracabilityStruct();
//			} catch (Exception e) {
//				Log.d("TestList", e.toString());
//			}
//		}
//		boolean single = false;
//		String shortcode = mTStruct.getItem(TracabilityStruct.ID.SHORT_CODE_I)
//				.toString();
//
//		// PR475446-yancan-zhu-20130621 begin
//		if (shortcode.startsWith("7FJ") || shortcode.startsWith("8FJ")
//				|| shortcode.startsWith("9FJ")) {
//			// PR475446-yancan-zhu-20130621 end
//			single = true;/* Single sim 7FJ,8FJ,9FJ */
//		}
//		// PR490336-yancan-zhu-20130717 begin
//		if (shortcode.startsWith("AFJ")) {
//			// PR490336-yancan-zhu-20130717 end
//			single = false;/* Dual sim AFJ */
//		}
//		return single;
//	}

//	// PR475446-yancan-zhu-20130621 begin
//	public static boolean isHasMHL() {
//		if (mTStruct == null) {
//			try {
//				mTStruct = new TracabilityStruct();
//			} catch (Exception e) {
//				Log.d("TestList", e.toString());
//			}
//		}
//		boolean single = false;
//		byte shortcode = mTStruct.getItem(TracabilityStruct.ID.SHORT_CODE_I)[0];
//		if (shortcode == '1') {
//			single = true;
//		}
//		return single;
//	}
//
//	// PR475446-yancan-zhu-20130621 end
//	// PR522432-yancan-zhu-20130913 begin
//	public static boolean isHasStylus() {
//		if (mTStruct == null) {
//			try {
//				mTStruct = new TracabilityStruct();
//			} catch (Exception e) {
//				Log.d("TestList", e.toString());
//			}
//		}
//		boolean single = true;
//		byte shortcode = mTStruct.getItem(TracabilityStruct.ID.SHORT_CODE_I)[0];
//		if (shortcode == 'C' || shortcode == 'Z') {
//			single = false;
//		}
//		return single;
//	}

	// PR522432-yancan-zhu-20130913 end
//	private static boolean isHasNFC() {
//		TracabilityStruct mTs = null;
//		String SHORT_CODE_I = null;
//		byte data[] = { 0 };
//		try {
//			mTs = new TracabilityStruct();
//		} catch (Exception e) {
//		}
//
//		if (mTs != null) {
//			SHORT_CODE_I = new String(
//					mTs.getItem(TracabilityStruct.ID.SHORT_CODE_I));
//		}
//		if (SHORT_CODE_I.regionMatches(0, "YKJ", 0, 3)) {
//			return false;
//		} else {
//			return true;
//		}
//	}

	//Add by Jianke.Zhang 2015/01/19
	public static Test[] getAutoTestList(){
		autolist.clear();
		AllMainActivity ama = new AllMainActivity();
		//autolist = ama.refreshAuto();
		ArrayList<Auto> AutoArrayList = new ArrayList<Auto>();
		AutoArrayList = ama.getAutoArray();
		String name = "";
		int len = AutoArrayList.size();
		
		for(int i = 0;i<AutoArrayList.size();i++)
		{
			name = AutoArrayList.get(i).getAutoName().toString();
			
			//Move Light sensor to AutoCase
			if(AutoArrayList.get(i).getAutoName().toString().equals("NFC"))
			 {
				 autolist.add(new NFC(Test.ID.NFC,"NFC"));
			 }
//			if(AutoArrayList.get(i).getAutoName().toString().equals("HEADSET"))
//			 {
//				 autolist.add(new HeadSetAutoTest(Test.ID.HEADSET,"HEADSET"));
//			 }
			if(AutoArrayList.get(i).getAutoName().toString().equals("GSENSOR"))
			 {
				 autolist.add(new GSensorTestAuto(Test.ID.GSENSOR,"GSENSOR"));
			 }
			 if(AutoArrayList.get(i).getAutoName().toString().equals("LIGHT SENSOR"))
			 {
				 autolist.add(new LightSensorTestAuto(Test.ID.LIGHTSENSOR,"LIGHT SENSOR"));
			 }
//			 if(AutoArrayList.get(i).getAutoName().toString().equals("PROXIMITY"))
//			 {
//				 autolist.add(new PSensorTest(Test.ID.ALSPS,"PROXIMITY"));
//			 }
			if(AutoArrayList.get(i).getAutoName().toString().equals("BlueTooth")){
				autolist.add(new BluetoothTest(Test.ID.BT, "BT"));
			}
			//��������Զ��������XML��һһ��Ӧ��
			if(AutoArrayList.get(i).getAutoName().toString().equals("KEYPAD")){
				autolist.add(new LightTest(Test.ID.BACKLIGHT, "KEYPAD & LCD BACKLIGHT"));
			}

			if(AutoArrayList.get(i).getAutoName().toString().equals("KEYPADAI")){
				autolist.add(new LightTest(Test.ID.BACKLIGHT, "KEYPAD & LCD BACKLIGHT"));
			}
			
			if(AutoArrayList.get(i).getAutoName().toString().equals("CAMERA LED")){
				autolist.add(new FlashLEDTest(Test.ID.CAMERA_LED, "CAMERA LED"));
			}

			if(AutoArrayList.get(i).getAutoName().toString().equals("CAMERA Front LED")){
				autolist.add(new FrontFlashLEDTest(Test.ID.FRONT_LED, "CAMERA Front LED"));
			}

			if(AutoArrayList.get(i).getAutoName().toString().equals("BB CAMERA LED")){
				autolist.add(new BBFlashLEDTest(Test.ID.BBCAMERA_LED, "BB CAMERA LED"));
			}
			/*if(AutoArrayList.get(i).getAutoName().toString().equals("AUDIO")){
				autolist.add(new AudioTest(Test.ID.MELODY, "AUDIO"));
			}*/
//			if(AutoArrayList.get(i).getAutoName().toString().equals("VIBRATOR")){
//				autolist.add(new VibratorTest(Test.ID.VIB, "VIBRATOR"));
//			}
			if(AutoArrayList.get(i).getAutoName().toString().equals("WIFI")){
				autolist.add(new WifiTest(Test.ID.WIFI, "WIFI", true,DownTimeCallBack.SECOND * 15));
			}
			if(AutoArrayList.get(i).getAutoName().toString().equals("SIM CARD")){
				autolist.add(new SIMTest(Test.ID.SIM, "SIM CARD"));
			}
			if(AutoArrayList.get(i).getAutoName().toString().equals("SD CARD")){
				autolist.add(new MemorycardTest(Test.ID.MEMORYCARD, "SD CARD"));
			}
			if(AutoArrayList.get(i).getAutoName().toString().equals("USBCharging")){
				autolist.add(new USBChargerTest(Test.ID.USB_Charger, "USBCharging"));
			}
			
			//Move USBTest to AutoCase
			if(AutoArrayList.get(i).getAutoName().toString().equals("USB")){
				autolist.add(new USBTest(Test.ID.USB, "USB", true,
						DownTimeCallBack.SECOND * 3));
			}
			
			//Move Compass to AutoCase
			if(AutoArrayList.get(i).getAutoName().toString().equals("COMPASS")){
				autolist.add(new CompassTest(Test.ID.COMPASS, "COMPASS"));
			}
			
			//Move BATTERY TEMP to AutoCase
			if(AutoArrayList.get(i).getAutoName().toString().equals("BATTERY TEMP")){
				autolist.add(new BatteryTempTest(Test.ID.TEMPBAT, "BATTERY TEMP"));
			}
			
			//Move Carr signal to AutoCase
			if(AutoArrayList.get(i).getAutoName().toString().equals("CARR SIGNAL")){
				autolist.add(new CarrierSignalTest(Test.ID.CARR_SIGNAL, "CARR SIGNAL"));
			}

			//added by nanbing.zou for A5A INFINI at 2017-11-23 begin

			if(AutoArrayList.get(i).getAutoName().toString().equals("LCD BACKLIGHT")){
				autolist.add(new LCDBacklightTest(Test.ID.LCDBACKLIGHT, "LCD BACKLIGHT"));
			}

			if(AutoArrayList.get(i).getAutoName().toString().equals("OTG")){
				autolist.add(new OTGTest(Test.ID.OTG, "OTG", true, DownTimeCallBack.SECOND * 3));
			}

			if(AutoArrayList.get(i).getAutoName().toString().equals("GYRO")){
				autolist.add(new GyroScopeTest(Test.ID.GYRO, "GYRO", true, DownTimeCallBack.SECOND * 3));
			}
			//added by nanbing.zou for A5A INFINI at 2017-11-23 end
			if(AutoArrayList.get(i).getAutoName().toString().equals("IR Test")){
				autolist.add(new IRTest(Test.ID.IR, "IR Test"));
			}

			if(AutoArrayList.get(i).getAutoName().toString().equals("WIFI 5G")){
				autolist.add(new Wifi_5GTest(Test.ID.WIFI5G, "Wifi 5G Test"));
			}
			if(AutoArrayList.get(i).getAutoName().toString().equals("WIFI 2.4G")){
				autolist.add(new Wifi_2_4GTest(Test.ID.WIFI2_4G, "WIFI 2.4G Test"));
			}

			if(AutoArrayList.get(i).getAutoName().toString().equals("Capacitive Keyboard")){
				autolist.add(new CapacitiveKeyBoardTest(Test.ID.CAPACITIVE, "Capacitive Keyboard"));
			}

			if(AutoArrayList.get(i).getAutoName().toString().equals("Button Backlight")){
				autolist.add(new ButtonBacklightTest(Test.ID.BUTTONBACKLIGHT, "Button Backlight"));
			}

			if(AutoArrayList.get(i).getAutoName().toString().equals("Key Backlight")){
				autolist.add(new KeybacklightTest(Test.ID.KEYBACKLIGHT, "Key Backlight"));
			}

			if(AutoArrayList.get(i).getAutoName().toString().equals("Hall")){
				autolist.add(new HallTest(Test.ID.HALL, "Hall"));
			}
//
//			if(AutoArrayList.get(i).getAutoName().toString().equals("PDAF")){
//				autolist.add(new CameraAutoTest(Test.ID.PDAF, "PDAF"));
//			}

			if(AutoArrayList.get(i).getAutoName().toString().equals("HDCP")){
				autolist.add(new HDCPTest(Test.ID.HDCP, "HDCP"));
			}
		}
		//---1---
		/*if(AllMainActivity.testItemsMap.get("BluetoothTest").equals("true")){
			autolist.add(new BluetoothTest(Test.ID.BT, "BT"));
		}*/
		
		/*if(AllMainActivity.testItemsMap.get("LightTest").equals("true")){
			autolist.add(new LightTest(Test.ID.BACKLIGHT, "KEYPAD & LCD BACKLIGHT"));
		}
		*/
		//---2---
		/*if(AllMainActivity.testItemsMap.get("CameraTest0").equals("true")){
			autolist.add(new CameraTest(Test.ID.CAMERA_IMG, "CAMERA"));
		}*/
		/*if(AllMainActivity.testItemsMap.get("CameraTest1").equals("true")){
			autolist.add(new CameraTest(Test.ID.CAMERA_IMG_FRONT, "CAMERA IMG FRONT"));
		}*/
		
		//autolist.add(new ChargerLEDTest(Test.ID.CHARGER_LED, "CHARGER LED"));
		
		//---3---
		/*if(AllMainActivity.testItemsMap.get("FlashLEDTest").equals("true")){
			autolist.add(new FlashLEDTest(Test.ID.CAMERA_LED, "CAMERA LED"));
		}*/
		/*if(AllMainActivity.testItemsMap.get("AudioTest").equals("true")){
			autolist.add(new AudioTest(Test.ID.MELODY, "AUDIO"));
		}*/
		/*if(AllMainActivity.testItemsMap.get("VibratorTest").equals("true")){
			autolist.add(new VibratorTest(Test.ID.VIB, "VIBRATOR"));
		}*/
		/*if(AllMainActivity.testItemsMap.get("WifiTest").equals("true")){
			autolist.add(new WifiTest(Test.ID.WIFI, "WIFI", true,DownTimeCallBack.SECOND * 15));
		}*/
		
		//-----4---------
		/*if(AllMainActivity.testItemsMap.get("SIMTest").equals("true")){
			autolist.add(new SIMTest(Test.ID.SIM, "SIM CARD"));
		}*/
		/*if(AllMainActivity.testItemsMap.get("MemorycardTest").equals("true")){
			autolist.add(new MemorycardTest(Test.ID.MEMORYCARD, "SD CARD"));
		}*/
			
		
		//autolist.add(new CallTest(Test.ID.CALL, "CALL"));
		
		//------5----------
		/*if(AllMainActivity.testItemsMap.get("USBChargerTest").equals("true")){
			autolist.add(new USBChargerTest(Test.ID.USB_Charger, "USBCharging"));
		}*/
		
		Test[] testList = new Test[autolist.size()];
//		autotestelementNum = testList.length;
		
		autolist.toArray(testList);

//		for(Test tt : testList){
//			Tool.toolLog("tt -> " + tt);
//		}
//		Tool.toolLog("auto list -> " + testList.length);
		
		return testList;
	}
	//End
	
	public static Test[] getTestList() {
		list.clear();
		
		AllMainActivity ama = new AllMainActivity();
		//autolist = ama.refreshAuto();
		ArrayList<Manual> ManualArrayList = new ArrayList<Manual>();
		ManualArrayList = ama.getManualArray();
/*		if (Test.flag == Test.MODE.AUTO) {
			list.add(new FinalTest(Test.ID.EMPTY, "EMPTY"));
		}*/
		
		//-------1------------
		//Tool.toolLog(TAG + " AllMainActivity.testItemsMap " + AllMainActivity.testItemsMap);
		for(int i = 0;i<ManualArrayList.size();i++)
		{


			//added by nanbing.zou for A5AINIFINI at 2017-11-23 begin
			if(ManualArrayList.get(i).getManualName().toString().equals("Finger")){
				list.add(new FingerprintTest(Test.ID.FINGER, "Finger"));
			}

			if(ManualArrayList.get(i).getManualName().toString().equals("Fingerprint Enroll")){
				list.add(new FingerEnrollTest(Test.ID.FINGERENROLL, "Fingerprint Enroll"));
			}

			if(ManualArrayList.get(i).getManualName().toString().equals("Keyboard")){
				list.add(new KeyBoardTest(Test.ID.KEYBOARD, "Keyboard"));
			}


			if(ManualArrayList.get(i).getManualName().toString().equals("BBKeyboard")){
				list.add(new BBKeyBoardTest(Test.ID.BBKEYBOARD, "BBKeyboard"));
			}


			if(ManualArrayList.get(i).getManualName().toString().equals("DTV")){
				list.add(new DtvTest(Test.ID.DTV, "DTV"));
			}

			if(ManualArrayList.get(i).getManualName().toString().equals("TPlock")){
				list.add(new TPlockTest(Test.ID.TPLOCK, "TPlock"));
			}
			if(ManualArrayList.get(i).getManualName().toString().equals("GSENSORCB")){
				list.add(new GsensorCallibrationTest(Test.ID.GSENSORCB, "GSENSORCB"));
			}
			//added by nanbing.zou for A5AINIFINI at 2017-11-23 end

			if(ManualArrayList.get(i).getManualName().equals("TRACABILITY"))
			{
				list.add(new TracabilityTest(Test.ID.TRACABILITY, "TRACABILITY"));
			}
			
			if(ManualArrayList.get(i).getManualName().equals("TOUCHPANEL")){
				list.add(new TouchPanel(Test.ID.TOUCHPANEL, "TouchPanel"));
			}
			
//			if(ManualArrayList.get(i).getManualName().equals("TP1")){
//				list.add(new Tp1Test(Test.ID.TP1, "TP1"));
//			}
			
			//-------3------------
//			if(ManualArrayList.get(i).getManualName().equals("TP2")){
//				list.add(new Tp2Test(Test.ID.TP2, "TP2"));
//			}
			
			//Add Point //-------4------------
			
			/*if(ManualArrayList.get(i).getManualName().equals("POINT")){
				list.add(new PointTest(Test.ID.POINT, "Point"));
			}*/
			
			//----------5-------------
			
			if(ManualArrayList.get(i).getManualName().equals("LCD ALLCOLOR")){
				list.add(new LcdAllColorTest(Test.ID.LCD_ALLCOLOR, "LCD_ALLCOLOR"));
			}
			
			//-------6------------

			if(ManualArrayList.get(i).getManualName().equals("KEYPAD")){
				list.add(new KeyPadTest(Test.ID.KEYPAD, "KEYPAD"));
			}

			if(ManualArrayList.get(i).getManualName().equals("KEYPADAI")){
				list.add(new KeyPadAITest(Test.ID.KEYPADAI, "KEYPADAI"));
			}
			if(ManualArrayList.get(i).getManualName().equals("Hall")){
				list.add(new com.tcl.manucase.HallTest(Test.ID.HALL,"Hall"));
			}
			//-------7------------
			
			
			/*if(ManualArrayList.get(i).getManualName().equals("USB")){
				list.add(new USBTest(Test.ID.USB, "USB", true,
						DownTimeCallBack.SECOND * 30));
			}*/
			
			//-------8------------
			
			/*if(ManualArrayList.get(i).getManualName().equals("COMPASS")){
				list.add(new CompassTest(Test.ID.COMPASS, "COMPASS"));
			}*/
				
			
			
			//-------9------------
			
			/*if(ManualArrayList.get(i).getManualName().equals("GSENSOR")){
				list.add(new GSensorTest(Test.ID.GSENSOR, "GSENSOR"));
			}*/
			
			//--------10-----------
			
			/*if(ManualArrayList.get(i).getManualName().equals("LIGHT SENSOR")){
				list.add(new LightSensorTest(Test.ID.LIGHTSENSOR, "LIGHT SENSOR", true));
			}*/
			
			//------------11------------
			
			if(ManualArrayList.get(i).getManualName().equals("PROXIMITY")){
				list.add(new AlsPsTest(Test.ID.ALSPS, "PROXIMITY", true));
			}

			//------------12--------------
			/*if(ManualArrayList.get(i).getManualName().equals("BATTERY TEMP")){
				list.add(new BatteryTempTest(Test.ID.TEMPBAT, "BATTERY TEMP"));
			}*/
			
			//-----------13-------------
			if(ManualArrayList.get(i).getManualName().equals("GPS")){
				list.add(new GPSSensorTest(Test.ID.GPS, "GPS"));
			}
			
			//-------------14-------------
			
			/*if(ManualArrayList.get(i).getManualName().equals("CARR SIGNAL")){
				list.add(new CarrierSignalTest(Test.ID.CARR_SIGNAL, "CARR SIGNAL"));
			}*/
			
			//---------15----------

			if(ManualArrayList.get(i).getManualName().equals("CHARGER LED")){
				list.add(new ChargerLEDTest(Test.ID.CHARGER_LED, "CHARGER LED"));
			}
			if(ManualArrayList.get(i).getManualName().equals("CHARGER RGBLED")){
				list.add(new ChargerRGBLEDTest(Test.ID.CHARGER_RGBLED, "CHARGER RGBLED"));
			}
						
			if(ManualArrayList.get(i).getManualName().equals("MIC")){
				list.add(new MICTest(Test.ID.MIC, "MIC",false,
						DownTimeCallBack.SECOND * 30));
			}
			
			if(ManualArrayList.get(i).getManualName().toString().equals("AUDIO")){
				list.add(new AudioTest(Test.ID.MELODY, "AUDIO"));
			}
			
			if(ManualArrayList.get(i).getManualName().toString().equals("CAMERA")){
				list.add(new CameraTest(Test.ID.CAMERA_IMG, "CAMERA"));
			}
			
			if(ManualArrayList.get(i).getManualName().toString().equals("CAMERA IMG FRONT")){
				list.add(new CameraTest(Test.ID.CAMERA_IMG_FRONT, "CAMERA IMG FRONT"));
			}

			if(ManualArrayList.get(i).getManualName().toString().equals("CAMERA2")){
				list.add(new CameraTest(Test.ID.CAMERA2, "CAMERA2"));
			}
			if(ManualArrayList.get(i).getManualName().toString().equals("MAIN2 Camera")){
				list.add(new CameraTest(Test.ID.MAIN2_Camera, "MAIN2 Camera"));
			}
			if(ManualArrayList.get(i).getManualName().toString().equals("WIDE CAMERA")){
				list.add(new CameraTest(Test.ID.WIDE_CAMERA, "WIDE CAMERA"));
			}

			if(ManualArrayList.get(i).getManualName().toString().equals("SubCamera")){
				list.add(new SubCameraTest(Test.ID.SUBCAMERA, "Sub Camera"));
			}

			if(ManualArrayList.get(i).getManualName().toString().equals("DualCamera")){
				list.add(new DualCameraTest(Test.ID.DUALCAMERA, "Dual Camera"));
			}
			if(ManualArrayList.get(i).getManualName().toString().equals("Macro CAMERA")){
				list.add(new CameraTest(Test.ID.WIDE_CAMERA, "Macro CAMERA"));
			}

			if(ManualArrayList.get(i).getManualName().toString().equals("PDAF")){
				list.add(new PDAFTest(Test.ID.PDAF, "PDAF"));
			}

			if(ManualArrayList.get(i).getManualName().toString().equals("FM")){
				list.add(new FMTest(Test.ID.FM, "FM"));
			}

			if(ManualArrayList.get(i).getManualName().toString().equals("VIBRATOR")){
				list.add(new VibratorTest(Test.ID.VIB, "VIBRATOR"));
			}
			if(ManualArrayList.get(i).getManualName().toString().equals("HEADSET")){
				list.add(new HeadSetTest(Test.ID.HEADSET, "HEADSET"));
			}
		}
		/*if(AllMainActivity.testItemsMap.get("TracabilityTest").equals("true")){
			list.add(new TracabilityTest(Test.ID.TRACABILITY, "TRACABILITY"));
		}*/
		
//		// PR522432-yancan-zhu-20130913 begin
//		// if(isHasStylus()) {
//		 list.add(new StylusTest(Test.ID.STYLUS,"STYLUS DETECT"));
//		// }
//		// PR522432-yancan-zhu-20130913 end
//		if (MINITestActivity.isST && MINITestActivity.isMMI)
//			list.add(new TpCalibration(Test.ID.TPCALIBRATION, "TP_CALIBRATION"));
//		// PR547129-yancan-zhu-20131030 begin
		// if (MINITestActivity.bvx) {
		//
//		list.add(new Tp0Test(Test.ID.TP0, "TP0"));
		//
		// }
//		// PR547129-yancan-zhu-20131030 end
		
		//-------2------------
		/*if(AllMainActivity.testItemsMap.get("Tp1Test").equals("true")){
			list.add(new Tp1Test(Test.ID.TP1, "TP1"));
		}
		
		//-------3------------
		if(AllMainActivity.testItemsMap.get("Tp2Test").equals("true")){
			list.add(new Tp2Test(Test.ID.TP2, "TP2"));
		}
		
		//Add Point //-------4------------
		if(AllMainActivity.testItemsMap.get("PointTest").equals("true")){
			list.add(new PointTest(Test.ID.POINT, "Point"));
		}
		
		//----------5-------------
		if(AllMainActivity.testItemsMap.get("LcdAllColorTest").equals("true")){
			list.add(new LcdAllColorTest(Test.ID.LCD_ALLCOLOR, "LCD_ALLCOLOR"));
		}
		
//		list.add(new LcdMireRgbTest(Test.ID.LCD_MIRERGB, "LCD_MIRERGB"));
//		list.add(new LcdBlackTest(Test.ID.LCD_BLACK, "LCD_BLACK"));
//		list.add(new LcdGreyTest(Test.ID.LCD_GREYCHART, "LCD_GREYCHART"));
//		list.add(new LcdGreyChartTest(Test.ID.LCD_LEVEL, "LCD GRAYLEVEL"));
//		list.add(new LcdWhiteTest(Test.ID.LCD_WHITE, "LCD WHITE"));
		
		//-------6------------
		if(AllMainActivity.testItemsMap.get("KeyPadTest").equals("true")){
			list.add(new KeyPadTest(Test.ID.KEYPAD, "KEYPAD"));
		}
		
//		list.add(new LightTest(Test.ID.BACKLIGHT, "KEYPAD & LCD BACKLIGHT"));
		
//        if (Test.flag == Test.MODE.MAUI)
//                        list.add(new CameraShakeOIS(Test.ID.CAMERA_SHAKE_OIS, "CAMERA SHAKE OIS"));
//        //if(!isDiablocamera())
//        if(isOISModule())
//        {
//        	list.add(new CameraOIS(Test.ID.CAMERA_OIS, "CAMERA OIS"));
//            list.add(new CameraDeco(Test.ID.CAMERA_DECO, "CAMERA DECO"));
//        }
//		list.add(new CameraTest(Test.ID.CAMERA_IMG, "CAMERA"));
//		list.add(new FlashLEDTest(Test.ID.CAMERA_LED, "CAMERA LED"));
//		list.add(new CameraTest(Test.ID.CAMERA_IMG_FRONT, "CAMERA IMG FRONT"));
//		list.add(new AudioTest(Test.ID.MELODY, "AUDIO"));
//		list.add(new VibratorTest(Test.ID.VIB, "VIBRATOR"));
//		list.add(new HiFiTest(Test.ID.HIFI, "HIFI"));//PR693990-yinbin-zhang-20140605
//		list.add(new HeadSetTest(Test.ID.HEADSET, "HEADSET"));
		
		//-------7------------
		if(AllMainActivity.testItemsMap.get("USBTest").equals("true")){
			list.add(new USBTest(Test.ID.USB, "USB", true,
					DownTimeCallBack.SECOND * 30));
		}
		
		//PR661432-yinbin-zhang-20140515
		//list.add(new Charger(Test.ID.CHARGER_PRES, "CHARGER", true,
		//		DownTimeCallBack.SECOND * 30));//PR661428-yinbin-zhang-20140519
		
		//Modify by Jianke.Zhang 03/09
//		Tool.toolLog(TAG + " 1111 " + AutotestMainActivity.deviceName);
		
		//-------8------------
		if(AllMainActivity.testItemsMap.get("CompassTest").equals("true")){
			list.add(new CompassTest(Test.ID.COMPASS, "COMPASS"));
		}
			
		
		//End
//      if (Test.flag == Test.MODE.MAUI)
		
		//-------9------------
		if(AllMainActivity.testItemsMap.get("GSensorTest").equals("true")){
			list.add(new GSensorTest(Test.ID.GSENSOR, "GSENSOR"));
		}
		
		//--------10-----------
		if(AllMainActivity.testItemsMap.get("LightSensorTest").equals("true")){
			list.add(new LightSensorTest(Test.ID.LIGHTSENSOR, "LIGHT SENSOR", true));
		}
		
		//------------11------------
		if(AllMainActivity.testItemsMap.get("AlsPsTest").equals("true")){
			list.add(new AlsPsTest(Test.ID.ALSPS, "PROXIMITY", true));
		}
		
//		list.add(new SIMTest(Test.ID.SIM, "SIM CARD"));
//		//if (isSingleSim()) {
//		list.add(new MemorycardTest(Test.ID.MEMORYCARD, "SD CARD"));
//		//}//PR661437-yinbin-zhang-20140519
		//------------12--------------
		if(AllMainActivity.testItemsMap.get("BatteryTempTest").equals("true")){
			list.add(new BatteryTempTest(Test.ID.TEMPBAT, "BATTERY TEMP"));
		}
		
//		list.add(new BluetoothTest(Test.ID.BT, "BT"));
//		list.add(new WifiTest(Test.ID.WIFI, "WIFI", true,
//				DownTimeCallBack.SECOND * 15));
		//-----------13-------------
		if(AllMainActivity.testItemsMap.get("GPSSensorTest").equals("true")){
			list.add(new GPSSensorTest(Test.ID.GPS, "GPS"));
		}
		
//		list.add(new HongwaiTest(Test.ID.INFRARED_RAY,"INFRARED_RAY"));
//		//list.add(new GyroscopeTest(Test.ID.GYROSCOPE, "GYROSCOPE"));//PR661437-yinbin-zhang-20140520
//		//list.add(new HiFiTest(Test.ID.HIFI, "HIFI"));//PR661437-yinbin-zhang-20140520
//		// PR488442-yancan-zhu-20130716 begin
//		list.add(new SmartCoverTest(Test.ID.SMARTCOVER,"LED ARRAY TEST"));//PR684417-yinbin-zhang-20140611
//		list.add(new SmartCoverTest(Test.ID.SMARTCOVER,"HALL TEST"));//PR684417-yinbin-zhang-20140611
//		// PR488446-yancan-zhu-20130716 end
//		if (isHasNFC()) {
//			list.add(new NFCTest(Test.ID.NFC_ACTIVE, "NFC_ACTIVE"));
//		}
//		// PR475446-yancan-zhu-20130621 begin
//		//if (isHasMHL()) {
//		//	list.add(new MHLTest(Test.ID.MHL, "MHL"));
//		//}
//		// PR475446-yancan-zhu-20130621 end
//		list.add(new GyroscopeTest(Test.ID.GYROSCOPE, "GYROSCOPE"));//PR693990-yinbin-zhang-20140605
//		if (Test.flag == Test.MODE.MAUI) {
//			list.add(new CallTest(Test.ID.CALL, "CALL"));
//		}
//		//if (Test.flag == Test.MODE.MAUI && !MINITestActivity.isMMI
//				//&& MINITestActivity.isST) {
//		if (Test.flag == Test.MODE.MAUI && !MINITestActivity.isMMI) {
//			list.add(new TpCalibration(Test.ID.TPCALIBRATION, "TP_CALIBRATION"));
//		}//PR732119-yinbin-zhang-20140710
		//-------------14-------------
		if(AllMainActivity.testItemsMap.get("CarrierSignalTest").equals("true")){
			list.add(new CarrierSignalTest(Test.ID.CARR_SIGNAL, "CARR SIGNAL"));
		}
		
//		if (Test.flag == Test.MODE.MAUI)
//		{
//			list.add(new SmartPATest(Test.ID.SMARTPA,"SMARTPA CAL"));
			//list.add(new FactoryresetTest(Test.ID.FACTORYRESET,"FACTORY RESET"));
//		}
		
		//---------15----------
		if(AllMainActivity.testItemsMap.get("ChargerLEDTest").equals("true")){
			list.add(new ChargerLEDTest(Test.ID.CHARGER_LED, "CHARGER LED"));
		}
		
//		if(AllMainActivity.testItemsMap.get("HeadSetTest").equals("true")){
//			list.add(new HeadSetTest(Test.ID.HEADSET, "HEADSET"));
//		}
		
		if(AllMainActivity.testItemsMap.get("MICTest").equals("true")){
			list.add(new MICTest(Test.ID.MIC, "MIC",false,
					DownTimeCallBack.SECOND * 30));
		}

		if(true){
			if(AllMainActivity.testItemsMap.get("FMTest").equals("true")){
			list.add(new FMTest(Test.ID.FM, "FM"));
		}
		}*/
		
		
		Test[] testList = new Test[list.size()];
//		elementNum = testList.length;
		list.toArray(testList);
		
		Tool.toolLog(TAG + " testList --> " + testList);
//		Log.i(TAG,"manu list -> " + testList.length);
		
		return testList;
	}
}
