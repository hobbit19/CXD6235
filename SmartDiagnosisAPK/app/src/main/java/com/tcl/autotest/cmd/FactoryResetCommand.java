package com.tcl.autotest.cmd;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.List;

import com.tcl.autotest.ImeiRunActivity;
import com.tcl.autotest.manuRunActivity;
import com.tcl.autotest.cmd.ManuCommand.manuStartApp;
import com.tcl.autotest.tool.Tool;

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
import android.os.PowerManager;
import android.telephony.TelephonyManager;

public class FactoryResetCommand extends BroadcastReceiver {

	private static final String TAG = "FactoryResetCommand";
	String manucmd = "com.tcl.autotest.cmd.FactoryReset";
	String action;
	
	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
		action = intent.getAction();
		if(action.equals(manucmd)){
//			Tool.toolLog(TAG + " IMEIandDevId .......");
			
			new StartApp(context).start();
		}
	}

	class StartApp extends Thread{
		
		Context resetContext;
		Intent ii;
		TelephonyManager tm;
		private BluetoothAdapter mBtAdapter;
		private WifiManager mWifiManager;
		
		public StartApp(Context mContext){
			resetContext = mContext.getApplicationContext();
		}
		
		@Override
		public void run(){
			//启动恢复出厂设置界面
			Intent intent = new Intent();  
			ComponentName cm = new ComponentName("com.android.settings","com.android.settings.Settings");  
			intent.setComponent(cm);  
			intent.setAction("android.intent.action.VIEW");  
			//intent.setAction("android.intent.action.MASTER_CLEAR"); 
			Tool.toolLog(TAG + " 1111111122222222");
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			resetContext.startActivity(intent);
		}
	}
}
