package com.tcl.autotest;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.nfc.NfcAdapter;
import android.nfc.NfcManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.StatFs;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewConfiguration;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.ListView;
import com.tcl.autotest.tool.Tool;
import com.tcl.autotest.utils.Auto;
import com.tcl.autotest.utils.CopyFileFromAssets;
import com.tcl.autotest.utils.Info;
import com.tcl.autotest.utils.Manual;
import com.tcl.autotest.utils.Msg;
import com.tcl.autotest.utils.Test;
import com.tcl.autotest.utils.XMLDocument;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;



@SuppressLint("NewApi")
public class AllMainActivity extends Activity {

	public static String XmlName = "";

	private static final String TAG = "AllMainActivity";
	ImageButton btn_refresh;
	ImageButton btn_start;
	ImageButton btn_result;
	public static ArrayList<Integer> totalList = new ArrayList<Integer>();
	public static ArrayList<Boolean> boolList = new ArrayList<Boolean>();
	public static String all_items_file_text = "allItemsTest";
	ListView allitemslv;
	private ArrayList<String> allautolist;
	private ArrayList<String> allmanulist;
	private ArrayList<String> allitemslist;
	public static int[] double_check_for_auto = null;
	private AllItemsAdapter allAdapter;

	private static Class<?> mClassType = null;
	private static Method mGetMethod = null;
	public static String deviceName;

	public static boolean refresh_fail_idle = false;
	public static boolean refresh_all_bool = true;

	DataAllSevice broadcastallfinish;
	private boolean autoFinish_Flag = false;
	private boolean Refresh_retest = false;
	private boolean manuFinish_Flag = false;

	private int autoTestResume = 0;
	private int manuTestResume = 0;

	// for all test flag file
	public static boolean mainAllTest = true;
	// for auto or manu flag file
	public static boolean autofileFlag = false;
	public static boolean manufileFlag = false;

	// 09/29 socket start flag
	public static boolean socketAllStart = false;

	public static Map testItemsMap = null;

	public static int allItemsListCounts = 0;
	public static int autoItemsListCounts = 0;
	public static int manuItemsListCounts = 0;
	public static int manuTP1Width = 30;
	public static int manuTP2Width = 50;
	public static int statusBarHeight = 0;
	public static Activity allActivity;
	public static boolean auto_refresh = false;
	IdleRefresh irbrdcast;
	public static String idlRefreshSignal = "Main.Idle.Refresh";
	ArrayList<Auto> AutoArray = new ArrayList<Auto>();
	ArrayList<Manual> ManualArray = new ArrayList<Manual>();

	private boolean isExistProduct = true;
	
	private boolean isRunCase = false;
	
	private String devPath = "sys/class/android_usb/android0/iSerial";
	public static String mSerial = null;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.all_items_test_activity);
		/*
		 * ActionBar actionBar = getActionBar();
		 * actionBar.setDisplayShowTitleEnabled(false);
		 * actionBar.setDisplayShowHomeEnabled(false); actionBar.hide();
		 */
		/*
		 * Rect frame = new Rect();
		 * getWindow().getDecorView().getWindowVisibleDisplayFrame(frame);
		 * statusBarHeight = frame.top;
		 */
		int contentTop = getWindow().findViewById(Window.ID_ANDROID_CONTENT)
				.getTop();
		// statusBarHeight������״̬���ĸ߶�
		statusBarHeight = contentTop - statusBarHeight;
		// statusBarHeight = getActionBar().getHeight();
		
		XMLDocument xd = new XMLDocument();

		// copyfile.copy(this, "idol347.xml", path, "e591e741.xml");
		//
		// getProjectFromXml(path + "/e591e741.xml");

		/*
		 * for(int i = 0;i<getVersion().length;i++) { Log.i("qinhao",
		 * "TIMES:"+getVersion()[i]); }
		 */

		/*
		 * WifiManager wifi = (WifiManager) getSystemService
		 * (Context.WIFI_SERVICE);
		 * 
		 * WifiInfo info = wifi.getConnectionInfo();
		 */

		// Log.i("qinhao", "TIMES:"+getMacAddress());

		/*
		 * try { mClassType = Class.forName("android.os.SystemProperties");
		 * mGetMethod = mClassType.getDeclaredMethod("get", String.class);
		 * deviceName = (String) mGetMethod.invoke(mClassType,
		 * "ro.product.device"); Tool.toolLog(TAG + " DeviceName " +
		 * deviceName); } catch (Exception e) { // TODO Auto-generated catch
		 * block e.printStackTrace(); }
		 */

		btn_refresh = (ImageButton) findViewById(R.id.refresh);

		btn_start = (ImageButton) findViewById(R.id.start);
		btn_start.setClickable(true);

		btn_result = (ImageButton) findViewById(R.id.result);

		btn_refresh.setOnClickListener(alllistener);
		btn_start.setOnClickListener(alllistener);
		btn_result.setOnClickListener(alllistener);

		// 06/30
		// Msg.deleteOldFile(all_items_file_text);
		// Msg.createFile(all_items_file_text);

		allitemslv = (ListView) findViewById(R.id.all_items_lv);
		deviceName = getProductName();
		if(deviceName.equals("Alto45"))
		{
			deviceName = getProductNameForTab();
		}
		Tool.toolLog(TAG + " --> deviceName --> " + deviceName);
		Log.d(TAG, " 33333 --> deviceName --> " + deviceName);
		
		refresh();

		String json_text = "";
		try {
			String jsonpath = "/sdcard/biglog";
			FileInputStream file = new FileInputStream(jsonpath);
			BufferedReader reader = new BufferedReader(new InputStreamReader(file));
			String line = "";
			while ((line=reader.readLine()) != null) {
				json_text += line;
			}
			//Log.e(TAG, "33333 json content: " + json_text);
		} catch (FileNotFoundException e) {
			Log.e(TAG, "33333 FileNotFoundException occurred when trying to read JSON file!");
			e.printStackTrace();
		} catch (Exception e) {
			Log.e(TAG, "33333 exception occurred when trying to read JSON file!");
			e.printStackTrace();
		}
		if (null != json_text) {
			String    version = "";
			String    product_model = "";
			String    life_time = "";
			String    used_time = "";
			String    sim_time = "";
			String    wifi_time = "";
			try {
				JSONObject jsonObj = new JSONObject(json_text);
				version = jsonObj.getString("version");
				product_model = jsonObj.getString("PRODUCT_MODEL");
				life_time = String.valueOf(jsonObj.getInt("LIFE_TIME"));
				JSONObject jsonUserTime = jsonObj.getJSONObject("USER_TIME");
				used_time = String.valueOf(jsonUserTime.getInt("used_time"));
				sim_time = String.valueOf(jsonUserTime.getInt("sim_time"));
				wifi_time = String.valueOf(jsonUserTime.getInt("wifi_time"));
				//Log.e(TAG, "33333 wifi_time: " + wifi_time);
			} catch (JSONException e) {
				Log.e(TAG, "33333 JSONException occurred!");
				e.printStackTrace();
			}
			xd.addNode("INFO", "BigLog", "Big Log", XmlName);
			xd.addNode("BigLog", "BigLogChild", "Version", version, XmlName);
			xd.addNode("BigLog", "BigLogChild", "ProductModel", product_model, XmlName);
			xd.addNode("BigLog", "BigLogChild", "LifeTime", life_time, XmlName);
			xd.addNode("BigLog", "BigLogChild", "UsedTime", used_time, XmlName);
			xd.addNode("BigLog", "BigLogChild", "SimTime", sim_time, XmlName);
			xd.addNode("BigLog", "BigLogChild", "WifiTime", wifi_time, XmlName);
		}
		else
			Log.e(TAG, "33333 could not get biglog!");
		
		try {
			String smartlog = "/sdcard/smart";
			FileInputStream file = new FileInputStream(smartlog);
			BufferedReader reader = new BufferedReader(new InputStreamReader(file));
			char[] buf;

			// part_damaged_count_record
			reader.mark(file.available());
			reader.reset();
			reader.skip(0x8A);
			int nStructSize = 12*2;
			buf = new char[nStructSize];
			int readlen = reader.read(buf, 0, nStructSize);
			int lk1_bad_count = buf[0] + buf[1]*0x100;
			int lk1_correct_count = buf[2] + buf[3]*0x100;
			int lk2_bad_count = buf[4] + buf[5]*0x100;
			int lk2_correct_count = buf[6] + buf[7]*0x100;
			int boot1_bad_count = buf[8] + buf[9]*0x100;
			int boot1_correct_count = buf[10] + buf[11]*0x100;
			int boot2_bad_count = buf[12] + buf[13]*0x100;
			int boot2_correct_count = buf[14] + buf[15]*0x100;
			int recovery1_bad_count = buf[16] + buf[17]*0x100;
			int recovery1_correct_count = buf[18] + buf[19]*0x100;
			int recovery2_bad_count = buf[20] + buf[21]*0x100;
			int recovery2_correct_count = buf[22] + buf[23]*0x100;

			// part_fec_record
			reader.reset();
			reader.skip(0xA2);
			nStructSize = 20*3;
			buf = new char[nStructSize];
			readlen = reader.read(buf, 0, nStructSize);
			String fec_decode_time = "";
			String fec_failed_time = "";
			String fec_repair_time = "";
			for (int i=0; i<20; i++) {
				if (buf[i] == 0x0)
					break;
				fec_decode_time += buf[i];
			}
			for (int i=20; i<40; i++) {
				if (buf[i] == 0x0)
					break;
				fec_failed_time += buf[i];
			}
			for (int i=40; i<60; i++) {
				if (buf[i] == 0x0)
					break;
				fec_repair_time += buf[i];
			}
			
			// lk_laucher_start_time_record   and   e2fsck_info_struct
			reader.reset();
			reader.skip(0xDE);
			nStructSize = 20*3;
			buf = new char[nStructSize];
			readlen = reader.read(buf, 0, nStructSize);
			String lk_start_time = "";
			String launcher_start_time = "";
			String e2fsck_time = "";
			for (int i=0; i<20; i++) {
				if (buf[i] == 0x0)
					break;
				lk_start_time += buf[i];
			}
			for (int i=20; i<40; i++) {
				if (buf[i] == 0x0)
					break;
				launcher_start_time += buf[i];
			}
			for (int i=40; i<60; i++) {
				if (buf[i] == 0x0)
					break;
				e2fsck_time += buf[i];
			}
			Log.e(TAG, "33333 e2fsck_time: " + e2fsck_time);

			xd.addNode("INFO", "SmartLog", "Smart Log", XmlName);
			xd.addNode("SmartLog", "SmartLogChild", "lk1_bad_count", Integer.toString(lk1_bad_count), XmlName);
			xd.addNode("SmartLog", "SmartLogChild", "lk1_correct_count", Integer.toString(lk1_correct_count), XmlName);
			xd.addNode("SmartLog", "SmartLogChild", "lk2_bad_count", Integer.toString(lk2_bad_count), XmlName);
			xd.addNode("SmartLog", "SmartLogChild", "lk2_correct_count", Integer.toString(lk2_correct_count), XmlName);
			xd.addNode("SmartLog", "SmartLogChild", "boot1_bad_count", Integer.toString(boot1_bad_count), XmlName);
			xd.addNode("SmartLog", "SmartLogChild", "boot1_correct_count", Integer.toString(boot1_correct_count), XmlName);
			xd.addNode("SmartLog", "SmartLogChild", "boot2_bad_count", Integer.toString(boot2_bad_count), XmlName);
			xd.addNode("SmartLog", "SmartLogChild", "boot2_correct_count", Integer.toString(boot2_correct_count), XmlName);
			xd.addNode("SmartLog", "SmartLogChild", "recovery1_bad_count", Integer.toString(recovery1_bad_count), XmlName);
			xd.addNode("SmartLog", "SmartLogChild", "recovery1_correct_count", Integer.toString(recovery1_correct_count), XmlName);
			xd.addNode("SmartLog", "SmartLogChild", "recovery2_bad_count", Integer.toString(recovery2_bad_count), XmlName);
			xd.addNode("SmartLog", "SmartLogChild", "recovery2_correct_count", Integer.toString(recovery2_correct_count), XmlName);
			xd.addNode("SmartLog", "SmartLogChild", "fec_decode_time", fec_decode_time, XmlName);
			xd.addNode("SmartLog", "SmartLogChild", "fec_failed_time", fec_failed_time, XmlName);
			xd.addNode("SmartLog", "SmartLogChild", "fec_repair_time", fec_repair_time, XmlName);
			xd.addNode("SmartLog", "SmartLogChild", "lk_start_time", lk_start_time, XmlName);
			xd.addNode("SmartLog", "SmartLogChild", "launcher_start_time", launcher_start_time, XmlName);
			xd.addNode("SmartLog", "SmartLogChild", "e2fsck_time", e2fsck_time, XmlName);
		} catch (FileNotFoundException e) {
			Log.e(TAG, "33333 FileNotFoundException: could not get smartlog!");
		} catch (Exception e) {
			Log.e(TAG, "33333 Exception: could not get smartlog!");
		}

		/**
		 * �����Ƿ��ֻ�root
		 */
		boolean isroot = isRoot();
		String isrootornot = "";
		if (isroot) {
			isrootornot = "Rooted";
		} else {
			isrootornot = "Not Rooted";
		}
		xd.addNode("CurrentInfo", "currentChild", "ROOT", isrootornot, XmlName);

		/**
		 * �ֻ���RAM
		 */
		long zs = getTotalMemory();
		//int totalRAM = (int)zs;
		int totalRAMMB = 1024 * 1024;
		xd.addNode("CurrentInfo", "currentChild", "RAM TOTAL", "" + zs
				/ totalRAMMB + "MB", XmlName);
		
		/**
		 * �����ֻ�RAM
		 */
		long s = getAvailMemory();
		int leftRAM = (int) (s);
		int leftRAMMB = 1024 * 1024;

		xd.addNode("CurrentInfo", "currentChild", "RAM LEFT", "" + leftRAM
				/ leftRAMMB + "MB", XmlName);

		/**
		 * RAMʣ�°ٷ�ֵ
		 * */
		String tpercent = getRAMpecent();
		String percentRateRamshow;
		if (isArabia()) {
			String temppp = tpercent.substring(0, 6);
			String percentRam = String.valueOf(Double.parseDouble(temppp)*100);
			try{
				percentRateRamshow = percentRam.substring(0, 5);
			}catch(Exception e){
				percentRateRamshow = percentRam.substring(0, 4);
			}
		} else {
			double percent = Double.parseDouble(tpercent);
			DecimalFormat percentRate = new DecimalFormat("#.####");
			String percentRateRam = percentRate.format(percent);
			percentRateRam = replaceSpChar(percentRateRam);
			double percentRam = Double.parseDouble(percentRateRam)*100;
			DecimalFormat percentRate2 = new DecimalFormat("#.##");
			percentRateRamshow = percentRate2.format(percentRam);
		}
		
	    xd.addNode("CurrentInfo", "currentChild", "RAM Percent", "" + percentRateRamshow
				 + "%", XmlName);
	    
	    /**
	     * �õ��ֻ���ROM*/
	    String totalROM = getTotalInternalMemorySize();
	    xd.addNode("CurrentInfo", "currentChild", "ROM TOTAL", ""
				+ totalROM + "MB", XmlName);
	    
		/**
		 * 
		 * �����ֻ�ROM
		 */
	    String leftROM = getAvailableInternalMemorySize();
//		if(!isArabia()){
//			leftROM = Double.parseDouble(leftROM);
//		}
		xd.addNode("CurrentInfo", "currentChild", "ROM LEFT", ""
				+ leftROM + "MB", XmlName);
		
		/**
		 * �õ�ROMʣ��ٷֱ�*/
		String percentRateRomshow;
		if(isArabia()){
			percentRateRomshow = leftROM + "/" + totalROM;
		    xd.addNode("CurrentInfo", "currentChild", "ROM Percent", "" + percentRateRomshow
					 , XmlName);
		}else{
			double reLeftROM = Double.parseDouble(leftROM);
			double reTotalROM = Double.parseDouble(totalROM);
			double percent2 = (double)reLeftROM/reTotalROM;
			DecimalFormat percentRate = new DecimalFormat("#.####");
			String percentRateRom = percentRate.format(percent2);
			percentRateRom = replaceSpChar(percentRateRom);
			double percentRom = Double.parseDouble(percentRateRom)*100;
			DecimalFormat percentRate2 = new DecimalFormat("#.##");
			percentRateRomshow = percentRate2.format(percentRom);
		    xd.addNode("CurrentInfo", "currentChild", "ROM Percent", "" + percentRateRomshow
					 + "%", XmlName);
		}

	    
		/**
		 * ���뿪��ʱ��
		 */
		String time = getTimes();
		xd.addNode("CurrentInfo", "currentChild", "Up Time", "" + time, XmlName);

		/***
		 * ����APK�汾
		 */
		try {
			String apkVersion = getAPKVersion();
			xd.addNode("CurrentInfo", "currentChild", "APK Version", ""
					+ apkVersion, XmlName);
		} catch (NameNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		/***
		 * ���뵱ǰ��ѹ
		 */

		String DeVoltage = getDeVoltage();
		xd.addNode("CurrentInfo", "currentChild", "Current Voltage", DeVoltage,
				XmlName);

		// /
		// /BasicInfo
		// /

		/**
		 * �����ֻ�DEVICEID
		 */

		String deviceID = getSerialNumber();
		xd.addNode("BasicInfo", "BasicChild", "Device ID", deviceID, XmlName);

		/**
		 * ����IMEI
		 */
		TelephonyManager telephonyManager = (TelephonyManager) this
				.getSystemService(Context.TELEPHONY_SERVICE);
		String imei = telephonyManager.getDeviceId();
		
		if(imei==""||imei==null)
		{
			imei=getSerialNumber();
		}
		xd.addNode("BasicInfo", "BasicChild", "IMEI", imei, XmlName);
		
		/***
		 * ����BT address
		 */
		// String bt = getBTMAC();
		xd.addNode("BasicInfo", "BasicChild", "BT address", "", XmlName);

		/**
		 * ����wifi address
		 */
		String wifiAddress = getWifiAddress();
		xd.addNode("BasicInfo", "BasicChild", "WIFI address", wifiAddress,
				XmlName);

		/**
		 * ����phone name
		 */
		String phoneName = getPhoneName();

		Tool.toolLog(TAG + " phoneName:"+phoneName);
		xd.addNode("BasicInfo", "BasicChild", "Phone Name", phoneName, XmlName);

		/***
		 * ����Internal Name
		 */
		/*String productName = getProductName();
		if(productName.equals("Alto45"))
		{
			productName = getProductNameForTab();
		}*/
		
		String productName =  getProductNameForTab();
		if(productName.equals("")||productName==null)
		{
			productName = getProductName();
		}
		xd.addNode("BasicInfo", "BasicChild", "Internal Name", productName,
				XmlName);

		
		
		/***
		 * ��������汾��
		 */
		String swVersion = getSWVersion();
		xd.addNode("BasicInfo", "BasicChild", "SW Version", swVersion, XmlName);

		//����svn Ver
		String svnver = getAndroidSVN();
		xd.addNode("BasicInfo", "BasicChild", "SVN", svnver, XmlName);
				
		/***
		 * ����android�汾��
		 */
		String ver = getAndroidVersion();
		xd.addNode("BasicInfo", "BasicChild", "Android Version", ver, XmlName);

		/***
		 * ����оƬ����
		 */
		String cpuInfo = getCpuInfoInPro();
		xd.addNode("BasicInfo", "BasicChild", "Chip manufacturer", cpuInfo,
				XmlName);

		/***
		 * ����ƽ̨��Ϣ
		 */
		String platform = getPlatform();
		xd.addNode("BasicInfo", "BasicChild", "platform", platform, XmlName);

		// /
		// /VoltageInfo
		// /

		/***
		 * ������Ƶ�ѹ
		 */
		/*
		 * 
		 * xd.addNode("VoltageInfo","VoltageChild","Designer Voltage",
		 * "",XmlName);
		 *//***
		 * ���������
		 */
		/*
		 * 
		 * xd.addNode("VoltageInfo","VoltageChild","Cycle Count", "",XmlName);
		 */

		/*
		 * �����Ƿ���Ҫʹ��Common.xml
		 * 
		 */
		 
		xd.addNode("BasicInfo", "BasicChild", "isExistProduct", String.valueOf(isExistProduct), XmlName);
		
		// Begin Jianke.Zhang
		broadcastallfinish = new DataAllSevice();
		IntentFilter receiverAllStart = new IntentFilter(
				autoRunActivity.AutoFinishSignal);
		receiverAllStart.addAction(manuRunActivity.ManuFinishSignal);
		this.registerReceiver(broadcastallfinish, receiverAllStart);
		// End

		// getOverflowMenu();

		// irbrdcast = new IdleRefresh();
		// IntentFilter recrefreshFilter = new IntentFilter(idlRefreshSignal);
		// this.registerReceiver(irbrdcast, recrefreshFilter);

		// Create Folder for Config File 07/09
		Msg.createFolder();

		// Get Density
		getResolution();

		// Jianke 09/28 Open the annotation if use socket
		// AutoMonitorThread amt = new AutoMonitorThread();
		// amt.start();
		// End
		if(isArabia()){
			Tool.toolLog(TAG + " isAR");
		}
		Tool.toolLog(TAG + " save XML Finish");
		
	}

	/***
	 * ��ȡ��ǰAPK�汾��
	 * 
	 * @throws NameNotFoundException
	 * 
	 */
	public String getAPKVersion() throws NameNotFoundException {

		PackageManager pm = getPackageManager();
		PackageInfo pi = pm.getPackageInfo(getPackageName(), 0);// getPackageName()���㵱ǰ��İ�����0�����ǻ�ȡ�汾��Ϣ
		String name = pi.versionName;
		int code = pi.versionCode;
		return name;
	}

	/***
	 * ��ȡ�ֻ���Ƶ�ѹ
	 */

	public String getDeVoltage() {
		/*
		 * String Voltage = null;
		 * 
		 * try {
		 * 
		 * Class<?> c = Class.forName("android.os.SystemProperties");
		 * 
		 * Method get = c.getMethod("get", String.class);
		 * 
		 * Voltage = (String) get.invoke(c, "sys.shutdown.voltage");
		 * 
		 * } catch (Exception e) {
		 * 
		 * e.printStackTrace();
		 * 
		 * }
		 * 
		 * return Voltage;
		 */
		String result = "";
		Intent batteryInfoIntent = this.registerReceiver(null,
				new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
		int voltage = batteryInfoIntent.getIntExtra("voltage", 0);
		return "" + voltage;
	}

	/***
	 * ��ȡandroidversion
	 */

	public String getAndroidVersion() {
		String AndroidVersion = null;

		try {

			Class<?> c = Class.forName("android.os.SystemProperties");

			Method get = c.getMethod("get", String.class);

			AndroidVersion = (String) get.invoke(c, "ro.build.version.release");

		} catch (Exception e) {

			e.printStackTrace();

		}

		return AndroidVersion;
	}

	/***
	 * ��ȡSWversion
	 */
	public String getSWVersion() {
		String swversion = null;

		try {

			Class<?> c = Class.forName("android.os.SystemProperties");

			Method get = c.getMethod("get", String.class);

			swversion = (String) get.invoke(c, "ro.build.version.incremental");

		} catch (Exception e) {

			e.printStackTrace();

		}
		/*
		 * if(!swversion.equals("")) { swversion = swversion.substring(2, 5); }
		 */
		return swversion;
	}

	/***
	 * ��ȡƽ̨��Ϣ
	 */

	public String getPlatform() {
		String platform = null;

		try {

			Class<?> c = Class.forName("android.os.SystemProperties");

			Method get = c.getMethod("get", String.class);

			platform = (String) get.invoke(c, "ro.board.platform");

		} catch (Exception e) {

			e.printStackTrace();

		}

		return platform;
	}

	/***
	 * ��ȡоƬ��Ϣ
	 */
	public String getCpuInfoInPro() {
		String cpuInfo = null;

		try {

			Class<?> c = Class.forName("android.os.SystemProperties");

			Method get = c.getMethod("get", String.class);

			cpuInfo = (String) get.invoke(c, "ro.hardware");

		} catch (Exception e) {

			e.printStackTrace();

		}

		return cpuInfo;
	}

	/***
	 * ��ȡ�ֻ�name
	 */
	public String getPhoneName() {
		String PhoneName = null;

		try {

			Class<?> c = Class.forName("android.os.SystemProperties");

			Method get = c.getMethod("get", String.class);

			PhoneName = (String) get.invoke(c, "ro.product.name");

		} catch (Exception e) {

			e.printStackTrace();

		}

		return PhoneName;
	}



	public String getPhoneModel() {
		String model = null;

		try {

			Class<?> c = Class.forName("android.os.SystemProperties");

			Method get = c.getMethod("get", String.class);

			model = (String) get.invoke(c, "ro.product.model");

		} catch (Exception e) {

			e.printStackTrace();

		}

		return model;
	}

	/***
	 * ��ȡ�ֻ�wifi��ַ
	 */
	public String getWifiAddress() {
		String Wifi = null;

		try {

			Class<?> c = Class.forName("android.os.SystemProperties");

			Method get = c.getMethod("get", String.class);

			Wifi = (String) get.invoke(c, "ro.tct.wifimac");

		} catch (Exception e) {

			e.printStackTrace();

		}

		return Wifi;
	}

	/***
	 * ��ȡ�ֻ���Ŀ��
	 */

	public String getProductName() {
		String product = null;

		try {

			Class<?> c = Class.forName("android.os.SystemProperties");

			Method get = c.getMethod("get", String.class);

			product = (String) get.invoke(c, "ro.product.device");

		} catch (Exception e) {

			e.printStackTrace();

		}

		return product;
	}
	
	
	/***
	 * ��ȡ�ֻ���Ŀ����T-TAB��Ŀ��
	 */
	public String getProductNameForTab() {
		String product = null;

		try {

			Class<?> c = Class.forName("android.os.SystemProperties");

			Method get = c.getMethod("get", String.class);

			product = (String) get.invoke(c, "ro.tct.product");

		} catch (Exception e) {

			e.printStackTrace();

		}

		return product;
	}

	/***
	 * 
	 * 
	 * ��ȡ�ֻ�deviceID
	 * 
	 * @return
	 */

	public static String getSerialNumber() {

		if(mSerial==null ){
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1)	{
			Tool.toolLog(TAG + "cannot get ro.serialno try another way");
			mSerial = Build.getSerial();

			Tool.toolLog(TAG + " ro.serialno " + mSerial);
		}

		else if(Build.VERSION.SDK_INT > Build.VERSION_CODES.N){
			Tool.toolLog(TAG + "cannot get ro.serialno try another way");
			mSerial = Build.SERIAL;
			Tool.toolLog(TAG + " ro.serialno " + mSerial);
		}
		else {
			try {

				Class<?> c = Class.forName("android.os.SystemProperties");

				Method get = c.getMethod("get", String.class);

				mSerial = (String) get.invoke(c, "ro.serialno");

			} catch (Exception e) {
				mSerial = null;
				e.printStackTrace();
			}
		}
		}
		if(mSerial.equalsIgnoreCase("unknown")){
			mSerial = "CUIRF6RW7XQSLZCQ";
		}

		if(mSerial == null || mSerial.equalsIgnoreCase("")){
			mSerial = "test";
		}


		return mSerial;

	}

	/**
	 * ��ȡadb devices��ֵ
	 * adb shell cat sys/class/android_usb/android0/iSerial 
	 * */
	public String getDevice(String path){
		StringBuffer s = new StringBuffer();
		FileInputStream in = null;
		try {
			String temp = "";
			in = new FileInputStream(path);
			BufferedReader read = new BufferedReader(new InputStreamReader(in));
			try {
				while ((temp = read.readLine()) != null) {
					s.append(temp);
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
			try{
				if(in != null){
					in.close();
				} 
			}catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}			
		}
		
		return s.toString();
	}
	/**
	 * �жϵ�ǰ�ֻ��Ƿ���ROOTȨ��
	 * 
	 * @return
	 */
	public boolean isRoot() {
		boolean bool = false;

		try {
			if ((!new File("/system/bin/su").exists())
					&& (!new File("/system/xbin/su").exists())) {
				bool = false;
			} else {
				bool = true;
			}
			Log.d(TAG, "bool = " + bool);
		} catch (Exception e) {

		}
		return bool;
	}

	/* �õ����õ��б����ֵ */
	public static String getMaxList(int type) {
		String LsMax;
		XMLDocument xd = new XMLDocument();
		InputStream is = xd.getInputStreamFromSDcard(XmlName);
		XmlPullParser parser;

		try {
			parser = XmlPullParserFactory.newInstance().newPullParser();

			// ����XML��ȡ�Զ�������
			LsMax = xd.ParseAutoManualListMaxXml(is, parser, type);
			try {
				is.close();

				return LsMax;
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} catch (XmlPullParserException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		return null;
	}

	/***
	 * ��ȡ�ֶ����Զ����б�
	 * 
	 * @return
	 */

	public ArrayList<Manual> getManualArray() {
		ArrayList<Manual> ManualArrayList = new ArrayList<Manual>();
		XMLDocument xd = new XMLDocument();
		String path = Environment.getExternalStorageDirectory().toString();
		InputStream is = xd.getInputStreamFromSDcard(XmlName);
		XmlPullParser parser;
		try {
			parser = XmlPullParserFactory.newInstance().newPullParser();
			// ����XML��ȡ�ֶ�������
			ManualArrayList = xd.ParseManualXml(is, parser);
			try {
				is.close();

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			// ����XML��ȡ�ֶ�������
			/*
			 * ManualArray = xd.ParseManualXml(iss, parser); for(int
			 * j=0;j<ManualArray.size();j++) {
			 * allmanulist.add(ManualArray.get(j).getManualName().toString()); }
			 */
			// initAllItemsData();

		} catch (XmlPullParserException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		return ManualArrayList;

	}

	/***
	 * ��ȡ�Զ����Զ����б�
	 * 
	 * @return
	 */
	public ArrayList<Auto> getAutoArray() {
		ArrayList<Auto> AutoArrayList = new ArrayList<Auto>();
		XMLDocument xd = new XMLDocument();
		String path = Environment.getExternalStorageDirectory().toString();
		InputStream is = xd.getInputStreamFromSDcard(XmlName);
		XmlPullParser parser;

		try {
			parser = XmlPullParserFactory.newInstance().newPullParser();
			// ����XML��ȡ�Զ�������
			AutoArrayList = xd.ParseAutoXml(is, parser);
			try {
				is.close();

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			// ����XML��ȡ�ֶ�������
			/*
			 * ManualArray = xd.ParseManualXml(iss, parser); for(int
			 * j=0;j<ManualArray.size();j++) {
			 * allmanulist.add(ManualArray.get(j).getManualName().toString()); }
			 */
			// initAllItemsData();

		} catch (XmlPullParserException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		return AutoArrayList;
	}

	/***
	 * ��ȡ�ֶ������������б�
	 * 
	 * @return
	 */
	public ArrayList refreshManual() {
		ArrayList<Manual> ManualArrayList = new ArrayList<Manual>();
		XMLDocument xd = new XMLDocument();
		String path = Environment.getExternalStorageDirectory().toString();
		InputStream is = xd.getInputStreamFromSDcard(XmlName);
		XmlPullParser parser;
		ArrayList<String> manuallist = new ArrayList<String>();
		try {
			parser = XmlPullParserFactory.newInstance().newPullParser();
			// parser.setInput(is, "utf-8");

			// allmanulist = new ArrayList<String>();
			// ����XML��ȡ�Զ�������
			ManualArrayList = xd.ParseManualXml(is, parser);
			for (int i = 0; i < ManualArrayList.size(); i++) {
				manuallist.add(ManualArrayList.get(i).getManualName()
						.toString());

			}
			try {
				is.close();

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			// ����XML��ȡ�ֶ�������
			/*
			 * ManualArray = xd.ParseManualXml(iss, parser); for(int
			 * j=0;j<ManualArray.size();j++) {
			 * allmanulist.add(ManualArray.get(j).getManualName().toString()); }
			 */
			// initAllItemsData();

		} catch (XmlPullParserException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		return manuallist;
	}

	/***
	 * ��ȡ�Զ������������б�
	 * 
	 * @return
	 */
	public ArrayList refreshAuto() {
		ArrayList<Auto> AutoArrayList = new ArrayList<Auto>();
		XMLDocument xd = new XMLDocument();
		String path = Environment.getExternalStorageDirectory().toString();
		InputStream is = xd.getInputStreamFromSDcard(XmlName);
		XmlPullParser parser;
		ArrayList<String> autolist = new ArrayList<String>();
		try {
			parser = XmlPullParserFactory.newInstance().newPullParser();
			// parser.setInput(is, "utf-8");

			// allmanulist = new ArrayList<String>();
			// ����XML��ȡ�Զ�������
			AutoArrayList = xd.ParseAutoXml(is, parser);
			for (int i = 0; i < AutoArrayList.size(); i++) {
				autolist.add(AutoArrayList.get(i).getAutoName().toString());

			}
			try {
				is.close();

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			// ����XML��ȡ�ֶ�������
			/*
			 * ManualArray = xd.ParseManualXml(iss, parser); for(int
			 * j=0;j<ManualArray.size();j++) {
			 * allmanulist.add(ManualArray.get(j).getManualName().toString()); }
			 */
			// initAllItemsData();

		} catch (XmlPullParserException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		return autolist;
	}

	/***
	 * ˢ���б�
	 */
	public void refresh() {

		String deviceID = getSerialNumber();
		XmlName = deviceID + ".xml";
		XMLDocument xd = new XMLDocument();
		/*String product = getProductName();
		if(product.equals("Alto45"))
		{
			product = getProductNameForTab();
		}*/
		String product =  getProductNameForTab();
		if(product.equals("")||product==null)
		{
			product = getProductName();
		}
		// CopyFileFromAssets copyfile = new CopyFileFromAssets();

		String path = Environment.getExternalStorageDirectory().toString();
		Tool.toolLog(TAG + " path:" + path + "  product " + product);
		Log.d(TAG, " 33333 path:" + path + "  product " + product);
		
		if (product.equals("pixi4_5_4g_vf")) {
			Log.d(TAG, "33333 vodfone");
			Settings.Secure.putInt(getContentResolver(), "location_mode", 1);
			PackageManager pm = getPackageManager();
			Log.d(TAG, "33333 ACCESS_FINE_LOCATION: " + pm.checkPermission(Manifest.permission.ACCESS_FINE_LOCATION, "com.tcl.autotest"));
			Log.d(TAG, "33333 ACCESS_COARSE_LOCATION: " + pm.checkPermission(Manifest.permission.ACCESS_COARSE_LOCATION, "com.tcl.autotest"));
		}

		String phoneModel = getPhoneModel();//getPhoneName();
		Log.d(TAG, "phoneModel:"+phoneModel);
		if((product.equals("A3A") && phoneModel.equals(Info.CODE_NAME_0))
				|| (product.equals("U5A_PLUS_4G") && (phoneModel.equals(Info.CODE_NAME_1) || phoneModel.equals(Info.CODE_NAME_2) || phoneModel.equals(Info.CODE_NAME_3) || phoneModel.equals(Info.CODE_NAME_18) || phoneModel.equals(Info.CODE_NAME_19)))
				|| (product.equals("U3A_PLUS_4G") && (phoneModel.equals(Info.CODE_NAME_4) || phoneModel.equals(Info.CODE_NAME_5) || phoneModel.equals(Info.CODE_NAME_16)))
				|| (product.equals("U3A_7_WIFI") && (phoneModel.equals(Info.CODE_NAME_6) || phoneModel.equals(Info.CODE_NAME_7) || phoneModel.equals(Info.CODE_NAME_20)))
				|| (product.equals("BUZZ6T3G") && (phoneModel.equals(Info.CODE_NAME_8)))
				|| (product.equals("A5X") && (phoneModel.equals(Info.CODE_NAME_9)/* || phoneModel.equals(Info.CODE_NAME_10)*/))
				|| (product.equals("Pepito") && phoneModel.equals(Info.CODE_NAME_11))
				|| (product.equals("U50A_PLUS_ATT") && phoneModel.equals(Info.CODE_NAME_12))
				|| (product.equals("U50APLUSTMO") && phoneModel.equals(Info.CODE_NAME_13))
				|| (product.equals("Tokyo") && phoneModel.equals(Info.CODE_NAME_22))
				|| (product.equals("Tokyo") && phoneModel.equals(Info.CODE_NAME_23))
				|| (product.equals("TokyoPro") && phoneModel.equals(Info.CODE_NAME_24))
				|| (product.equals("TokyoPro") && phoneModel.equals(Info.CODE_NAME_25))){
			product = phoneModel;
		}
        if(deviceName.equals("Seoul") && phoneModel.equals(Info.CODE_NAME_26)){
            product = "Seoul_GL_CAN";
        }

		if((deviceName.equals("A3A") && phoneModel.equals(Info.CODE_NAME_14)) || (deviceName.equals("U5A_PLUS_4G") && phoneModel.equals(Info.CODE_NAME_19))){
			deviceName = phoneModel;
		}

		Log.i("qinhao","product:"+product);
		//�ж��Ƿ���product����
		isExistProduct = CopyFileFromAssets.isCommon(this, product + ".xml");
		
		CopyFileFromAssets.copy(this, product + ".xml", path, deviceID + ".xml");

		Log.e(TAG, "33333 copy asset file " + product + ".xml" + " to phone path: " + path + "/" + deviceID + ".xml");
		
		InputStream is = xd.getInputStreamFromSDcard(XmlName);
		InputStream iss = xd.getInputStreamFromSDcard(XmlName);
		XmlPullParser parser;
		try {
			parser = XmlPullParserFactory.newInstance().newPullParser();
			// parser.setInput(is, "utf-8");
			allautolist = new ArrayList<String>();
			allmanulist = new ArrayList<String>();
			// ����XML��ȡ�Զ�������
			AutoArray = xd.ParseAutoXml(is, parser);
			for (int i = 0; i < AutoArray.size(); i++) {
				// 隐蔽一些无效的测试项
				boolean bIgnoreItem = false;
				if (product.equals("shine_lite")) {
					if (AutoArray.get(i).getAutoName().toString().equals("NFC")) {
						NfcManager nfcMgr = (NfcManager)this.getSystemService(Context.NFC_SERVICE);
						NfcAdapter adapter = nfcMgr.getDefaultAdapter();
						if ((null!=adapter) && (adapter.isEnabled()))
							Log.e(TAG, "33333 has nfc, no need to ignore nfc" );
						else {
							Log.e(TAG, "33333 doesn't have nfc, ignore and hide nfc" );
							bIgnoreItem = true;
						}
					}
				}
				
				if (!bIgnoreItem)
					allautolist.add(AutoArray.get(i).getAutoName().toString());
			}
			autoItemsListCounts = allautolist.size();
			try {
				is.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			// ����XML��ȡ�ֶ�������
			ManualArray = xd.ParseManualXml(iss, parser);
			for (int j = 0; j < ManualArray.size(); j++) {
				allmanulist.add(ManualArray.get(j).getManualName().toString());
			}
			manuItemsListCounts = allmanulist.size();

			allItemsListCounts = autoItemsListCounts + manuItemsListCounts;
			initAllItemsData();

		} catch (XmlPullParserException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		Tool.toolLog(TAG + " settingSucessed");
	}

	private ListView initAllItemsData() {
		// allautolist = new ArrayList<String>();
		// allmanulist = new ArrayList<String>();
		allitemslist = new ArrayList<String>();
		// 1,init auto arrayList
		// initAutoArray();
		// 2,init manu arrayList
		// initManuArray();

		initAllData();

		return allitemslv;
	}

	/*
	 * private void initManuArray() {
	 * 
	 * System.out.println(
	 * "ͨ��Map.entrySetʹ��iterator����key��value��");
	 * Iterator<Map.Entry<String, String>> it =
	 * testItemsMap.entrySet() .iterator(); while
	 * (it.hasNext()) { Map.Entry<String, String>
	 * entry = it.next(); //
	 * System.out.println("key= " + entry.getKey() +
	 * " and value= " + // entry.getValue());
	 * Log.i("qinhao", "key= " + entry.getKey() +
	 * " and value= " + entry.getValue()); } // TODO
	 * Auto-generated method stub if
	 * (testItemsMap.get
	 * ("TracabilityTest").equals("true")) {
	 * allmanulist.add("TRACABILITY"); } if
	 * (testItemsMap.get("Tp1Test").equals("true"))
	 * { allmanulist.add("TP1"); } if
	 * (testItemsMap.get("Tp2Test").equals("true"))
	 * { allmanulist.add("TP2"); } if
	 * (testItemsMap.get
	 * ("PointTest").equals("true")) {
	 * allmanulist.add("POINT"); } if
	 * (testItemsMap.get
	 * ("LcdAllColorTest").equals("true")) {
	 * allmanulist.add("LCD ALLCOLOR"); } if
	 * (testItemsMap
	 * .get("KeyPadTest").equals("true")) {
	 * allmanulist.add("KEYPAD"); }
	 * 
	 * // ------- no add ----- //
	 * allmanulist.add("HEADSET");
	 * 
	 * if
	 * (testItemsMap.get("USBTest").equals("true"))
	 * { allmanulist.add("USB"); } if
	 * (testItemsMap.get
	 * ("CompassTest").equals("true")) {
	 * allmanulist.add("COMPASS"); } if
	 * (testItemsMap
	 * .get("GSensorTest").equals("true")) {
	 * allmanulist.add("GSENSOR"); } if
	 * (testItemsMap
	 * .get("LightSensorTest").equals("true")) {
	 * allmanulist.add("LIGHT SENSOR"); } if
	 * (testItemsMap
	 * .get("AlsPsTest").equals("true")) {
	 * allmanulist.add("PROXIMITY"); } if
	 * (testItemsMap
	 * .get("BatteryTempTest").equals("true")) {
	 * allmanulist.add("BATTERY TEMP"); } if
	 * (testItemsMap
	 * .get("GPSSensorTest").equals("true")) {
	 * allmanulist.add("GPS"); } if
	 * (testItemsMap.get
	 * ("CarrierSignalTest").equals("true")) {
	 * allmanulist.add("CARR SIGNAL"); } if
	 * (testItemsMap
	 * .get("ChargerLEDTest").equals("true")) {
	 * allmanulist.add("CHARGER LED"); } if
	 * (testItemsMap.get("MICTest").equals("true"))
	 * { allmanulist.add("MIC"); } // FM Test
	 * 
	 * if(true){
	 * if(testItemsMap.get("FMTest").equals
	 * ("true")){ allmanulist.add("FM"); } }
	}
	*/
	
	/*
	private void initAutoArray() {
	 * // TODO Auto-generated method stub if
	 * (testItemsMap != null) { Set<String> key =
	 * testItemsMap.keySet(); for (Iterator it =
	 * key.iterator(); it.hasNext();) { String s =
	 * (String) it.next(); String value = (String)
	 * testItemsMap.get(s); Tool.toolLog(TAG +
	 * " key " + s + " value " + value); }
	 * 
	 * if
	 * (testItemsMap.get("BluetoothTest").equals("true"
	 * )) { allautolist.add("BlueTooth"); }
	 * 
	 * // no write permission if
	 * (testItemsMap.get("LightTest"
	 * ).equals("true")) {
	 * allautolist.add("KEYPAD & LCD BACKLIGHT"); }
	 * 
	 * if
	 * (testItemsMap.get("CameraTest0").equals("true"
	 * )) { allautolist.add("CAMERA"); } if
	 * (testItemsMap
	 * .get("CameraTest1").equals("true")) {
	 * allautolist.add("CAMERA IMG FRONT"); } if
	 * (testItemsMap
	 * .get("FlashLEDTest").equals("true")) {
	 * allautolist.add("CAMERA LED"); } if
	 * (testItemsMap
	 * .get("AudioTest").equals("true")) {
	 * allautolist.add("AUDIO"); } if
	 * (testItemsMap.get
	 * ("VibratorTest").equals("true")) {
	 * allautolist.add("VIBRATOR"); } if
	 * (testItemsMap.get("WifiTest").equals("true"))
	 * { allautolist.add("WIFI"); } if
	 * (testItemsMap.get("SIMTest").equals("true"))
	 * { allautolist.add("SIM CARD"); }
	 * 
	 * // no write permission if
	 * (testItemsMap.get("MemorycardTest"
	 * ).equals("true")) {
	 * allautolist.add("SD CARD"); }
	 * 
	 * if(testItemsMap.get("CallTest").equals("true")
	 * ){ allautolist.add("CALL"); }
	 * 
	 * 
	 * if
	 * (testItemsMap.get("USBChargerTest").equals(
	 * "true")) { allautolist.add("USBCharger"); }
	 * 
	 * }
	}
	*/
	
	public void initAllData() {
		initAllList();
		Tool.toolLog(TAG + " al --> " + allitemslist.size());
		allAdapter = new AllItemsAdapter(allitemslist, this);
		allitemslv.setAdapter(allAdapter);
	}

	private void initAllList() {
		// Merge auto and manu arrayList
		allitemslist = (ArrayList<String>) allautolist.clone();
		Tool.toolLog(TAG + " allitemslist " + allitemslist);

		for (int k = 0; k < allmanulist.size(); k++) {
			allitemslist.add(allmanulist.get(k));
		}

	}

	OnClickListener alllistener = new View.OnClickListener() {

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			switch (v.getId()) {
			/* case R.id.refresh: */
			// Msg.deleteOldFile(all_items_file_text);
			// Get Config File from /storage/sdcard0/AutoTestRecord
			/*
			 * String configFilePath = "/storage/sdcard0/AutoTestRecord/" +
			 * deviceName + ".ini"; try { testItemsMap =
			 * getConfigFlag(configFilePath); } catch (IOException e) { // TODO
			 * Auto-generated catch block e.printStackTrace(); } // init List
			 * 07/01 initAllItemsData();
			 */
			/*
			 * refresh(); allItemsListCounts = allitemslist.size();
			 * autoItemsListCounts = allautolist.size(); manuItemsListCounts =
			 * allmanulist.size();
			 */
			/*
			 * allItemsListCounts = Integer.parseInt((String) testItemsMap
			 * .get("allItemsCounts"));
			 */
			/*
			 * autoItemsListCounts = Integer.parseInt((String) testItemsMap
			 * .get("autoItemsCounts"));
			 */
			/*
			 * manuItemsListCounts = Integer.parseInt((String) testItemsMap
			 * .get("menuItemsCounts"));
			 */
			/*
			 * manuTP1Width = Integer.parseInt((String) testItemsMap
			 * .get("tp1Width")); manuTP2Width = Integer.parseInt((String)
			 * testItemsMap .get("tp2Width")); break;
			 */
			case R.id.start:
				StartAllItemsActivity();
				break;
			case R.id.result:
				showAutoLogDialog();
				break;
			default:
				break;
			}
		}
	};

	public static Activity getActivity() {

		return allActivity;
	}

	/*
	 * public void refreshIdle() { String configFilePath =
	 * "/storage/sdcard0/AutoTestRecord/" + deviceName + ".ini"; try {
	 * testItemsMap = getConfigFlag(configFilePath); } catch (IOException e) {
	 * // TODO Auto-generated catch block e.printStackTrace(); } // init List
	 * 07/01 initAllItemsData(); allItemsListCounts = Integer.parseInt((String)
	 * testItemsMap .get("allItemsCounts")); autoItemsListCounts =
	 * Integer.parseInt((String) testItemsMap .get("autoItemsCounts"));
	 * manuItemsListCounts = Integer.parseInt((String) testItemsMap
	 * .get("menuItemsCounts")); manuTP1Width = Integer.parseInt((String)
	 * testItemsMap.get("tp1Width")); manuTP2Width = Integer.parseInt((String)
	 * testItemsMap.get("tp2Width")); }
	 */

	/*
	 * public Map getConfigFlag(String configFilePath) throws IOException { //
	 * Read ini File String strLine; String key; String value; Map<String,
	 * String> containerAllItems = new HashMap<String, String>();
	 * 
	 * BufferedReader bufferedReader = new BufferedReader(new FileReader(
	 * configFilePath)); try { while ((strLine = bufferedReader.readLine()) !=
	 * null) { strLine = strLine.trim(); if (strLine.contains(";")) { continue;
	 * } if (strLine.contains("=")) { String[] strArray = strLine.split("="); if
	 * (strArray.length == 2) { key = strArray[0].trim(); value =
	 * strArray[1].trim(); containerAllItems.put(key, value); } } } } finally {
	 * bufferedReader.close(); }
	 * 
	 * return containerAllItems; }
	 */

	public void StartAllItemsActivity() {
		btn_start.setEnabled(false);
		btn_start.setClickable(false);
		SimpleDateFormat sDateFormat = new SimpleDateFormat("hh:mm:ss");
		String date = sDateFormat.format(new java.util.Date());
		Tool.toolLog("StartAllItemsActivity_autotest" + date);

		socketAllStart = true;

		if (broadcastallfinish == null) {
			broadcastallfinish = new DataAllSevice();
			IntentFilter receiverAllStart = new IntentFilter(
					autoRunActivity.AutoFinishSignal);
			receiverAllStart.addAction(manuRunActivity.ManuFinishSignal);
			this.registerReceiver(broadcastallfinish, receiverAllStart);
		}
		totalList.clear();
		boolList.clear();
		Msg.deleteOldFile(all_items_file_text);
		Msg.createFile(all_items_file_text);
		autofileFlag = false;
		manufileFlag = false;
		// refresh_all_bool = true;
		new Thread() {
			public void run() {
				isRunCase = true;
				RunAllCases();
				isRunCase = false;
			}
		}.start();
	}

	public void RunAllCases() {
		// 1 test auto items first
		autoTestResume = allautolist.size();
		double_check_for_auto = new int[autoTestResume];
		Tool.toolLog(TAG + " autoTestResume " + autoTestResume);
		// int test_time = 0;
		for(int i=0; i<0+0; i++)
			AllMainActivity.boolList.add(i, true);
		for (int i = 0; i < autoTestResume; i++) {
			 //while (true) { // Tool.toolLog(TAG + " test_time " +
			 //test_time++); if (!refresh_all_bool) { refresh_all_bool = true;
			 //break; } }
			Tool.sleepTimes(15);
			refresh_fail_idle = false;
			totalList.add(i);
			runAutoCase(i);
			waitForAutoFinished();
		}
  
		// XIBIN begin
		// runAutoCase(0);
		// XIBIN END

		// 2 test manu items
		
		manuTestResume = allmanulist.size();
		for (int k = 0; k < manuTestResume; k++) {
			/*
			 * while (true) { if (!refresh_all_bool) { refresh_all_bool = true;
			 * break; } }
			 */
			manuFinish_Flag=false;
			Tool.sleepTimes(15);
			refresh_fail_idle = false;
			totalList.add(autoTestResume + k);
			runManuCase(k);
			waitForManuFinished();
		}

		SetButtonUse();

		Tool.toolLog(TAG + " finish all the test!");
		this.unregisterReceiver(broadcastallfinish);
		broadcastallfinish = null;
	}

	private void runManuCase(int caseIndex) {
		// TODO Auto-generated method stub
		Tool.toolLog(TAG + " start manu test position: " + caseIndex);
		mainAllTest = true;
		Intent intent = new Intent(Intent.ACTION_MAIN, null);
		intent.putExtra("position", caseIndex);
		intent.setComponent(new ComponentName("com.tcl.autotest",
				"com.tcl.autotest.ExecuteManuTest"));
		this.startActivity(intent);
	}

	public void runAutoCase(int caseIndex) {
		Tool.toolLog("start auto test position: " + caseIndex);
		mainAllTest = true;
		Intent intent = new Intent(Intent.ACTION_MAIN, null);
		intent.putExtra("position", caseIndex);
		intent.setComponent(new ComponentName("com.tcl.autotest",
				"com.tcl.autotest.ExecuteTest"));
		this.startActivityForResult(intent, caseIndex);
	}

	@Override
	protected void onActivityResult(final int requestCode, int resultCode,
			Intent data) {
		// TODO Auto-generated method stub
		Tool.toolLog(TAG + " all 23232323232");
		//XIBIN ADD BEGIN 
		/*if (data == null) {

			int caseIndex = data.getIntExtra("position", -1);
			if (resultCode == Test.RESULT.FAILED.ordinal()) {
				runAutoCase(caseIndex);
			} else {
				if (caseIndex < autoTestResume - 1) {
					totalList.add(caseIndex);
					runAutoCase(caseIndex + 1);
				}
			}
		//XIBIN ADD END 
		}*/
		if (resultCode == Test.RESULT.FAILED.ordinal()
				&& double_check_for_auto[ExecuteTest.temppositon] < 2) {
			Tool.toolLog(TAG + " requestCode 6666666666 -> " + requestCode);
			// Wait for refresh fail idle 06/16
			new Thread() {
				public void run() {
//					Tool.sleepTimes(3);
//					while (true) {
//						if (refresh_fail_idle) {
//							refresh_fail_idle = false;
//							break;
//						}
//					}
					Tool.sleepTimes(30);
					Tool.toolLog(TAG + " 111111111111111");
					boolList.remove(requestCode);
					runAutoCase(requestCode);
					waitForFinishedRetest();
				}
			}.start();
			// End
		}
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		Tool.toolLog(TAG + " all onResume 00000000000000");
		if (true) {
			// Tool.toolLog("auto onResume ... jkkkkkkkkkkk " +
			// index_resume_global);
			// allitemslv.setAdapter(allAdapter);
			allAdapter.notifyDataSetChanged();
		}
	}

	public void SetButtonUse() {
		Message msg = new Message();
		msg.what = 0x30;
		handler.sendMessage(msg);
	}

	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			if (msg.what == 0x30) {
				btn_start.setEnabled(true);
				btn_start.setClickable(true);
			}
		}
	};

	public void showAutoLogDialog() {
		String wholeName = Msg.getAllItemsFilePath();
		Tool.toolLog(TAG + " all items 222222 wholeName " + wholeName);
		Intent intent = new Intent();
		intent.setClass(this, ShowAllItemsRcdsActivity.class);
		Bundle bundle = new Bundle();
		bundle.putString("RCD_FILE_ALL_ITEMS_NAME", wholeName);
		intent.putExtras(bundle);
		startActivity(intent);
	}

	public class DataAllSevice extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			Bundle bundle = intent.getExtras();
			Tool.toolLog(TAG + " bundle " + bundle);
			String action = intent.getAction();
			Tool.toolLog(TAG + " action 232 " + action);
			Log.d(TAG, " 33333 BroadcastReceiver action: " + action);

			if (bundle != null) {
				if (action.equals(autoRunActivity.AutoFinishSignal)) {
					Log.d(TAG, " 33333 bundle.getString: " + bundle.getString("autoRunActivity.Test"));
					if (bundle.getString("autoRunActivity.Test").contains(
							"autoRunActivity.Finish")) {
						Tool.toolLog(TAG + " autoRunActivity Receive message!!!!!!!");
						Log.d(TAG, "33333 autoRunActivity Receive message!!!!!!!");
						if (bundle.getString("autoRunActivity.Result")
								.contains("Pass")) {
							Tool.toolLog(TAG + " 8888888888888");
							Log.d(TAG, " 33333 autoFinish_Flag = true");
							autoFinish_Flag = true;
						} else {
							Refresh_retest = true;
							Tool.toolLog(TAG + " Refresh_retest = true");
							Log.d(TAG, "autoFinish_Flag = true");
						}
					}
				}

				if (action.equals(manuRunActivity.ManuFinishSignal)) {
					if (bundle.getString("manuRunActivity.Test").contains(
							"manuRunActivity.Finish")) {
						Tool.toolLog(TAG + " manuRunActivity Receive message!!!!!!!");
						manuFinish_Flag = true;
					}
				}
			}

		}

	}

	public class IdleRefresh extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			String action = intent.getAction();
			if (action.equals(idlRefreshSignal)) {
				// refreshIdle();
			}
			Tool.sleepTimes(10);
			context.unregisterReceiver(irbrdcast);
			irbrdcast = null;
		}

	}

	private void waitForFinishedRetest() {
		int count = 0;
		while (true) {
			count++;
			if (Refresh_retest) {
				Refresh_retest = false;
				break;
			} else {
				if (count % 5 == 0) {
					Tool.toolLog(TAG + " sleep " + count);
				}
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

			if (count > 1000) {
				// new FinishThread(0x02).start();
				break;
			}
		}
	}

	private void waitForAutoFinished() {
		int count = 0;
		while (true) {
			count++;
			if (autoFinish_Flag) {
				autoFinish_Flag = false;
				break;
			} else {
				if (count % 5 == 0) {
					Tool.toolLog(TAG + " sleep " + count);
				}
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

			if (count > 1000) {
				// new FinishThread(0x02).start();
				break;
			}
		}
	}

	private void waitForManuFinished() {
		int count = 0;
		while (true) {
			count++;
			if (manuFinish_Flag) {
				manuFinish_Flag = false;
				break;
			} else {
				if (count % 5 == 0) {
					Tool.toolLog(TAG + " sleep " + count);
				}
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

			if (count > 1000) {
				// new FinishThread(0x02).start();
				break;
			}
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		menu.add(0, 1, 0, "Auto/Manu");
		menu.add(0, 2, 0, "Exit");
		getMenuInflater().inflate(R.menu.menu_main, menu);
		// Tool.toolLog(TAG + " onCreateOptionsMenu");
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		// Tool.toolLog(TAG + " onOptionsItemSelected");
		Tool.toolLog(TAG + " 2222 " + item.getItemId());
		Intent intentTab;
		switch (item.getItemId()) {
		case 1:
			intentTab = new Intent(this, AutotestMainActivity.class);
			this.startActivity(intentTab);
			break;
		case 2:
			this.unregisterReceiver(broadcastallfinish);
			broadcastallfinish = null;
			this.finish();
			break;
		default:
			intentTab = new Intent(this, AutotestMainActivity.class);
			this.startActivity(intentTab);
			break;
		}

		return true;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		// TODO Auto-generated method stub
		// Tool.toolLog(TAG + " onPrepareOptionsMenu");

		return true;
	}

	// force to show overflow menu in actionbar
	private void getOverflowMenu() {
		try {
			ViewConfiguration config = ViewConfiguration.get(this);
			Field menuKeyField = ViewConfiguration.class
					.getDeclaredField("sHasPermanentMenuKey");
			if (menuKeyField != null) {
				menuKeyField.setAccessible(true);
				menuKeyField.setBoolean(config, false);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void finish() {
		// TODO Auto-generated method stub
		super.finish();
		if (broadcastallfinish != null) {
			this.unregisterReceiver(broadcastallfinish);
			broadcastallfinish = null;
		}
	}

	// Get Density
	public void getResolution() {
		// TODO Auto-generated method stub
		Display display = this.getWindowManager().getDefaultDisplay();
		DisplayMetrics displayMetrics = new DisplayMetrics();
		display.getMetrics(displayMetrics);
		// float density = displayMetrics.density; //�õ��ܶ�
		float width = displayMetrics.widthPixels;// �õ����
		float height = displayMetrics.heightPixels;// �õ��߶�
		Tool.toolLog(TAG + " density " + width + "x" + height);
	}

	public void getTotalMemory2() {
		String str1 = "/proc/meminfo";
		String str2 = "";
		try {
			FileReader fr = new FileReader(str1);
			BufferedReader localBufferedReader = new BufferedReader(fr, 8192);
			while ((str2 = localBufferedReader.readLine()) != null) {
				Log.i(TAG, "---" + str2);
			}
		} catch (IOException e) {
		}
	}

	public long getAvailMemory() {
		ActivityManager am = (ActivityManager) this
				.getSystemService(Context.ACTIVITY_SERVICE);
		ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo();
		am.getMemoryInfo(mi);
		return mi.availMem;
	}

	public long getTotalMemory(){
		ActivityManager am = (ActivityManager) this
				.getSystemService(Context.ACTIVITY_SERVICE);
		ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo();
		am.getMemoryInfo(mi);
        
        return mi.totalMem;
	}
	
	public String getRAMpecent(){
		long total = getTotalMemory();
		long left = getAvailMemory();
		String percent;
//		if(isArabia()){
//			percent = left + "/" + total;
//		}else
		{
			double tpercent = (double)left/total;
			percent = String.valueOf(tpercent);
		}
		
		return percent;
	}
	
	public String getMacAddress() {
		String result = "";
		String Mac = "";
		result = callCmd("busybox ifconfig", "HWaddr");
		if (result == null) {
			return "���������������";
		}
		if (result.length() > 0 && result.contains("HWaddr")) {
			Mac = result.substring(result.indexOf("HWaddr") + 6,
					result.length() - 1);
			if (Mac.length() > 1) {
				result = Mac.toLowerCase();
			}
		}
		return result.trim();
	}

	public String callCmd(String cmd, String filter) {
		String result = "";
		String line = "";
		try {
			Process proc = Runtime.getRuntime().exec(cmd);
			InputStreamReader is = new InputStreamReader(proc.getInputStream());
			BufferedReader br = new BufferedReader(is);

			// ִ������cmd��ֻȡ����к���filter����һ��
			while ((line = br.readLine()) != null
					&& line.contains(filter) == false) {
				// result += line;
				Log.i("test", "line: " + line);
			}

			result = line;
			Log.i("test", "result: " + result);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}

	// ��ȡrom

	public long[] getRomMemroy() {
		long[] romInfo = new long[2];
		// Total rom memory
//		romInfo[0] = getTotalInternalMemorySize();

		// Available rom memory
		File path = Environment.getDataDirectory();
		StatFs stat = new StatFs(path.getPath());
		long blockSize = stat.getBlockSize();
		long availableBlocks = stat.getAvailableBlocks();
		romInfo[1] = blockSize * availableBlocks;
		// getVersion();
		return romInfo;
	}

	private String calculateSizeInMB(StatFs stat,boolean isAvail) {
		DecimalFormat dFormat = new DecimalFormat("#.##");
		String temp = null;
		String result;
		if (stat != null){
			if(isAvail){
				temp = dFormat.format(stat.getAvailableBlocks()* (stat.getBlockSize()/(1024f * 1024f)));
			}
			else{
				temp = dFormat.format(stat.getBlockCount()*(stat.getBlockSize()/(1024f * 1024f)));
			}
			if(isArabia()){
				result = temp;
			}else{
				result = replaceSpChar(temp);
			}

			return result;
		}
		
		return null;
	}

	/**
	 * 
	 * @return ROMʣ��洢�ռ��MB��
	 */
	private String getAvailableInternalMemorySize() {

		String path = getInternalMemoryPath();// ��ȡ����Ŀ¼
		StatFs stat = getStatFs(path);

		return calculateSizeInMB(stat,true);
	}

	/*
	 * @param path �ļ�·��
	 * 
	 * @return �ļ�·����StatFs����
	 * 
	 * @throws Exception ·��Ϊ�ջ�Ƿ��쳣�׳�
	 */
	private StatFs getStatFs(String path) {
		try {
			return new StatFs(path);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 
	 * @return ROM�洢·��
	 */
	private String getInternalMemoryPath() {
		return Environment.getDataDirectory().getPath();
	}

	public String getTotalInternalMemorySize() {
		File path = Environment.getDataDirectory();
		StatFs stat = new StatFs(path.getPath());
		
		return calculateSizeInMB(stat,false);
	}

	// ��ȡCPU��Ϣ
	public String[] getCpuInfo() {
		String str1 = "/proc/cpuinfo";
		String str2 = "";
		String[] cpuInfo = { "", "" };
		String[] arrayOfString;
		try {
			FileReader fr = new FileReader(str1);
			BufferedReader localBufferedReader = new BufferedReader(fr, 8192);
			str2 = localBufferedReader.readLine();
			arrayOfString = str2.split("\\s+");
			for (int i = 2; i < arrayOfString.length; i++) {
				cpuInfo[0] = cpuInfo[0] + arrayOfString[i] + " ";
			}
			str2 = localBufferedReader.readLine();
			arrayOfString = str2.split("\\s+");
			cpuInfo[1] += arrayOfString[2];
			localBufferedReader.close();
		} catch (IOException e) {
		}
		return cpuInfo;
	}

	// ��ȡ�汾��Ϣ
	public String[] getVersion() {
		String[] version = { "null", "null", "null", "null", "null" };
		String str1 = "/proc/version";
		String str2;
		String[] arrayOfString;
		try {
			FileReader localFileReader = new FileReader(str1);
			BufferedReader localBufferedReader = new BufferedReader(
					localFileReader, 8192);
			str2 = localBufferedReader.readLine();
			arrayOfString = str2.split("\\s+");
			version[0] = arrayOfString[2];// KernelVersion
			localBufferedReader.close();
		} catch (IOException e) {
		}
		version[1] = Build.VERSION.RELEASE;// firmware version
		version[2] = Build.MODEL;// model
		version[3] = Build.DISPLAY;// system version
		version[4] = android.os.Build.VERSION.SDK;
		return version;
	}

	public String[] getOtherInfo() {
		String[] other = { "null", "null" };
		WifiManager wifiManager = (WifiManager) this
				.getSystemService(Context.WIFI_SERVICE);
		WifiInfo wifiInfo = wifiManager.getConnectionInfo();
		if (wifiInfo.getMacAddress() != null) {
			other[0] = wifiInfo.getMacAddress();
		} else {
			other[0] = "Fail";
		}
		other[1] = getTimes();
		return other;
	}

	private String getTimes() {
		long ut = SystemClock.elapsedRealtime() / 1000;
		if (ut == 0) {
			ut = 1;
		}
		int m = (int) ((ut / 60) % 60);
		int h = (int) ((ut / 3600));
		return h + ":" + m + " ";
	}

	private String getBTMAC() {
		BluetoothAdapter bAdapt = BluetoothAdapter.getDefaultAdapter();
		String btMac = "";
		if (bAdapt != null) {
			if (!bAdapt.isEnabled()) {
				Intent TurnOnBtIntent = new Intent(
						BluetoothAdapter.ACTION_REQUEST_ENABLE);
				startActivityForResult(TurnOnBtIntent, 0);
			}

			btMac = bAdapt.getAddress();
		} else {
			btMac = "No Bluetooth Device!";
		}
		return btMac;
	}
	
	@Override
	public void onBackPressed() {
		if(isRunCase){
			Tool.toolLog(TAG + " onBackPressed");
		}else{
			super.onBackPressed();
		}
	}

	private String replaceSpChar(String temp){
		String resultStr=temp;
		//�������ַ�һ�������滻
		String pattern = ",";
		Pattern p = Pattern.compile(pattern);
		Matcher m = p.matcher(temp);
		if(m.find()){
			resultStr = temp.replace(',', '.');
		}
		
		Tool.toolLog(TAG + " resultStr --> " + resultStr);
		return resultStr;
	}
	
	private boolean isArabia() {
		Locale locale = getResources().getConfiguration().locale;
		String language = locale.getLanguage();
		if (language.endsWith("ar")){

			return true;
		}
		else
			return false;
		}
	
	//�õ�svn
	public String getAndroidSVN() {
		String AndroidSVN = null;

		try {

			Class<?> c = Class.forName("android.os.SystemProperties");

			Method get = c.getMethod("get", String.class);

			AndroidSVN = (String) get.invoke(c, "ro.def.software.svn");

		} catch (Exception e) {

			e.printStackTrace();

		}

		return AndroidSVN;
	}



//	private String getRealProduct(String product){
//
//		String phoneName = "";
//		String baseChar = "_";
//		switch (product){
//			case "mickey6":
//				phoneName = baseChar+getPhoneName();
//				break;
//			default:
//				break;
//		}
//
//		return product+phoneName;
//	}

}
