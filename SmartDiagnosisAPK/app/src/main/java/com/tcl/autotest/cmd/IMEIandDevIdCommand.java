package com.tcl.autotest.cmd;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.text.DecimalFormat;
import java.util.List;

import com.tcl.autotest.ImeiRunActivity;
import com.tcl.autotest.manuRunActivity;
import com.tcl.autotest.cmd.ManuCommand.manuStartApp;
import com.tcl.autotest.tool.Tool;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.MemoryInfo;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbManager;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.BatteryManager;
import android.os.Environment;
import android.os.PowerManager;
import android.os.StatFs;
import android.os.SystemClock;
import android.telephony.TelephonyManager;
import android.text.format.DateUtils;
import android.util.DisplayMetrics;
import android.view.Display;

public class IMEIandDevIdCommand extends BroadcastReceiver {

	private static final String TAG = "IMEIandDevIdCommand";
	String manucmd = "com.tcl.autotest.cmd.IMEIandDevId";
	String action;
	
	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
		action = intent.getAction();
		if(action.equals(manucmd)){
//			Tool.toolLog(TAG + " IMEIandDevId .......");
			new imeiStartApp(context).start();
		}
	}

	class imeiStartApp extends Thread{
		
		Context imeiContext;
		Intent ii;
		TelephonyManager tm;
		private BluetoothAdapter mBtAdapter;
		private WifiManager mWifiManager;
		
		public imeiStartApp(Context mContext){
			imeiContext = mContext.getApplicationContext();
		}
		
		@Override
		public void run(){
//			ii = new Intent(imeiContext,ImeiRunActivity.class);
//			ii.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//			this.imeiContext.startActivity(ii);
			tm = (TelephonyManager) imeiContext.getSystemService(Context.TELEPHONY_SERVICE);
			//IMEI IMSI code
			Tool.toolLog(TAG + " IMEI " + tm.getDeviceId());
			Tool.toolLog("TCTDiagnosisTest V1.0");
			Tool.toolLog(TAG + " IMSI " + tm.getSubscriberId());
			//SN
			//BT WIFI
			mBtAdapter = BluetoothAdapter.getDefaultAdapter();
			Tool.toolLog(TAG + " BT " + mBtAdapter.getAddress());
			mWifiManager = (WifiManager) imeiContext
					.getSystemService(Context.WIFI_SERVICE);
			Tool.toolLog(TAG + " WIFI " + mWifiManager.getConnectionInfo().getMacAddress());

//			List<WifiConfiguration> lw = mWifiManager.getConfiguredNetworks();
//			for(WifiConfiguration wcf : lw){
//				Tool.toolLog(TAG + " wcf " + wcf);
//			}
//			TelephonyManagerEx tme = TelephonyManagerEx
//			Method[] ms;
//			Class<?> c;
//			try {
//				c = Class.forName(tm.getClass().getName());
//				ms = c.getDeclaredMethods();
//				for(Method m : ms){
//					Tool.toolLog(TAG + " " + m);
//				}
//			} catch (ClassNotFoundException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
			
//			try {
//				String cmd = "adb devices";//"E:\\Tools\\android-sdk\\tools\\adb.exe devices";
//				Process p = Runtime.getRuntime().exec("cmd adb devices",null,null);
//				InputStream input = p.getInputStream();
//				BufferedReader in = new BufferedReader(new InputStreamReader(input));
//				StringBuffer buffer = new StringBuffer();
//				String line = "";
//				while ((line = in.readLine()) != null) {
//					buffer.append(line);
//				}
//
//				int status = p.waitFor();
//				Tool.toolLog(TAG + " " + status + " " + buffer.toString());
//			} catch (Exception e) {
//				// TODO Auto-generated catch block
//				Tool.toolLog(TAG + " " + e.getMessage());
//				e.printStackTrace();
//			}
			
			//For Battery
			Intent batteryInfoIntent = imeiContext.registerReceiver( null ,
									new IntentFilter( Intent.ACTION_BATTERY_CHANGED));
			int status = batteryInfoIntent.getIntExtra( "status" , 0 );
			int health = batteryInfoIntent.getIntExtra( "health" , 1 );
			//boolean present = batteryInfoIntent.getBooleanExtra( "present" , false );
			int level = batteryInfoIntent.getIntExtra( "level" , 0 );
			int scale = batteryInfoIntent.getIntExtra( "scale" , 0 );
			int plugged = batteryInfoIntent.getIntExtra( "plugged" , 0 );
			int voltage = batteryInfoIntent.getIntExtra( "voltage" , 0 );
//			int desinerVoltage = batteryInfoIntent.getIntExtra(BatteryManager., defaultValue)
			//int temperature = batteryInfoIntent.getIntExtra( "temperature" , 0 ); // 温度的单位是10℃
			String technology = batteryInfoIntent.getStringExtra( "technology" );
			Tool.toolLog(TAG + " status " + status + " health " + health + 
					" level " + level + " scale "+ scale + " plugged " + plugged +
					" voltage " + voltage + " technology " + technology);
			
			//Get ROM Rate 07/09
			double romLeft = getRate();
			Tool.toolLog(TAG + " RomLeft " + romLeft);
			
			//Get Up time 07/09
			String upTiString = updateBatteryStats();
			Tool.toolLog(TAG + " UpTime " + upTiString);
			
			//Get RAM 07/09
			double ramLeft = getAvailMemory(imeiContext);
			Tool.toolLog(TAG + " RamLeft " + ramLeft);
			

		}
		
		//Get ROM
		public long getAvailableInternalMemorySize() {
			File path = Environment.getDataDirectory();
			StatFs stat = new StatFs(path.getPath());
			long blockSize = stat.getBlockSize();
			long availableBlocks = stat.getAvailableBlocks();
			return availableBlocks * blockSize;
		}

		public long getTotalInternalMemorySize() {
			File path = Environment.getDataDirectory();
			StatFs stat = new StatFs(path.getPath());
			long blockSize = stat.getBlockSize();
			long totalBlocks = stat.getBlockCount();
			return totalBlocks * blockSize;
		}
		
		public double getRate() {
			double available = getAvailableInternalMemorySize();
			double total = getTotalInternalMemorySize();
			double rate = (double) available / total;
			DecimalFormat pecentRate = new DecimalFormat("#.##");
			String pecentRate2 = pecentRate.format(rate);
			double pecentRateDb = Double.parseDouble(pecentRate2);

			return pecentRateDb;
		}
		
		//Get UpTime
		public String updateBatteryStats() {       
		    long uptime = SystemClock.elapsedRealtime();  
		    String upTimeStr = DateUtils.formatElapsedTime(uptime/1000);
		    
		    return upTimeStr;
		} 
		
		//Get free/available RAM
		@SuppressLint("NewApi")
		public double getAvailMemory(Context context){
	        // 获取android当前可用内存大小 
	        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
	        MemoryInfo mi = new MemoryInfo();
	        am.getMemoryInfo(mi);
	        long totalMem = mi.totalMem;
	        long freeMem = mi.availMem; //当前系统的可用内存
	        totalMem = totalMem/1024;
	        freeMem =  freeMem/1024;
	        Tool.toolLog(TAG + " totalMem " + totalMem + " freeMem " + freeMem);
	        double precent = (double)freeMem/totalMem;
	        DecimalFormat pecentRate = new DecimalFormat("#.##");
	        String pecentRate2 = pecentRate.format(precent);
			double pecentRateDb = Double.parseDouble(pecentRate2);
			
			return pecentRateDb;
	    }
		
		
		
	}
}
