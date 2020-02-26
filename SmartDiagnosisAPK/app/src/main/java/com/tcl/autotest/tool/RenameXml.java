package com.tcl.autotest.tool;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.security.AllPermission;
import java.util.List;

import org.apache.http.conn.ssl.AllowAllHostnameVerifier;

import com.tcl.autotest.AllMainActivity;

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
import android.os.Environment;
import android.os.PowerManager;
import android.telephony.TelephonyManager;

public class RenameXml extends BroadcastReceiver {

	private static final String TAG = "RenameXml";
	String renamecmd = "com.tcl.autotest.tool.renameXml";
	String action;

	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
		action = intent.getAction();
		String devS = intent.getStringExtra("keyDev");
		Tool.toolLog(TAG + " action " + action + " devS --> " + devS);
		if (action.equals(renamecmd)) {

			new StartApp(context, devS).run();
		}
	}

	class StartApp extends Thread {

		Context resetContext;
		String devs;

		public StartApp(Context mContext, String devs) {
			this.devs = devs;
			resetContext = mContext.getApplicationContext();
		}

		@Override
		public void run() {
			renameXml(devs);
		}

		private void renameXml(String devS) {
			// TODO Auto-generated method stub
			String path = Environment.getExternalStorageDirectory().toString();
			String deviceID = AllMainActivity.getSerialNumber();
			String file = path + File.separator + deviceID + ".xml";
			File oldFile = new File(file);
			if (oldFile.exists() && !devS.equals(deviceID)) {
				String newname = path + File.separator + devS + ".xml";
				File newfile = new File(newname);
				copy(oldFile, newfile);
				// newFile.renameTo(new File(newname));
				// AllMainActivity.mSerial=devS;
				// AllMainActivity.XmlName=AllMainActivity.mSerial+".xml";
			}
			Tool.toolLog(TAG + " Rename done!");
		}

		private void copy(File sourcepath, File destPath) {
			InputStream is = null;
			FileOutputStream fos = null;
			try {
				is = new FileInputStream(sourcepath);
				fos = new FileOutputStream(destPath);
				byte[] buffer = new byte[1024];
				int count = 0;
				while ((count = is.read(buffer)) > 0) {
					fos.write(buffer, 0, count);
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				if (is != null) {
					try {
						is.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				if (fos != null) {
					try {
						fos.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}

			}

		}

	}
}
