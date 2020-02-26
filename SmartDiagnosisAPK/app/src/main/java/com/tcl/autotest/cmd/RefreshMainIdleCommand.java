package com.tcl.autotest.cmd;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.security.AllPermission;
import java.util.List;

import org.apache.http.conn.ssl.AllowAllHostnameVerifier;

import com.tcl.autotest.AllMainActivity;
import com.tcl.autotest.ImeiRunActivity;
import com.tcl.autotest.autoRunActivity;
import com.tcl.autotest.manuRunActivity;
import com.tcl.autotest.cmd.ManuCommand.manuStartApp;
import com.tcl.autotest.tool.Tool;

import android.app.Activity;
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
import android.os.Bundle;
import android.os.PowerManager;
import android.telephony.TelephonyManager;

public class RefreshMainIdleCommand extends BroadcastReceiver {

	private static final String TAG = "RefreshMainIdleCommand";
	String manucmd = "com.tcl.autotest.cmd.RefreshMainIdle";
	String action;
	
	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
		action = intent.getAction();
		Tool.toolLog(TAG + " action " + action);
		if(action.equals(manucmd)){
//			Tool.toolLog(TAG + " RefreshMainIdle .......");
			
			new StartApp(context).start();
		}
	}

	class StartApp extends Thread{
		
		Context resetContext;
		Intent intent;
		Activity asActivity;
		
		public StartApp(Context mContext){
			resetContext = mContext.getApplicationContext();
		}
		
		@Override
		public void run(){
//			Tool.toolLog(TAG + " 123123123");
			AllMainActivity.auto_refresh = true;
			/*intent = new Intent();
			intent.setComponent(new ComponentName("com.tcl.autotest",
					"com.tcl.autotest.AllMainActivity"));
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			resetContext.startActivity(intent);*/
			sendIdleRefreshMessage(resetContext);
		}
		
		public void sendIdleRefreshMessage(Context mContext){
			Intent intent;
			intent = new Intent(AllMainActivity.idlRefreshSignal);
			sendRefreshMsg(mContext,intent);
		}
		
		public void sendRefreshMsg(Context mContext,Intent i){
			mContext.sendBroadcast(i);
		}
	}
}
