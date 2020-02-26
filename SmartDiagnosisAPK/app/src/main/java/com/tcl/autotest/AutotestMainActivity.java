package com.tcl.autotest;

import java.lang.reflect.Method;
import java.util.ArrayList;

import com.tcl.autotest.tool.Tool;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.app.Activity;
import android.app.TabActivity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TabHost;
import android.widget.TabHost.OnTabChangeListener;

@SuppressWarnings("deprecation")
public class AutotestMainActivity extends TabActivity 
	/*implements TabHost.TabContentFactory*/ {

	private TabHost myTabhost;
	private ArrayList<String> autolist;
	private ArrayList<String> list;
	private AutoAdapter aAdapter;
	private MyAdapter mAdapter;
	private ListView lv;
	private ListView autolv;
	private boolean tab_flag = true; //false;
	ViewHolder holder;
	//Jianke.Zhang Add Begin 2015/01/19
	private static final String TAG = "AutotestMainActivity";
	boolean Finish_Flag = false;
//	DataSevice broadcastfinish;
	boolean isfinished = false;
	//End
	private static Class<?> mClassType = null;
    private static Method mGetMethod = null;
//    public static String deviceName;
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// setContentView(R.layout.activity_autotest_main);

		//Add by Jianke.Zhang 03/09 Begin
//		SystemProperties.get("ro.product.name");
		
//	    try {
//			mClassType = Class.forName("android.os.SystemProperties");
//			mGetMethod = mClassType.getDeclaredMethod("get", String.class);
//			deviceName = (String) mGetMethod.invoke(mClassType, "ro.product.device");
//			Tool.toolLog(TAG + " DeviceName " + deviceName);
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		
		// Show Tab view
		myTabhost = this.getTabHost();
		// LayoutInflater.from(this).inflate(R.layout.tab_test,myTabhost.getTabContentView(),true);
		Intent intentauto = new Intent();
		intentauto.setClass(this, autoRunActivity.class);
		myTabhost.addTab(myTabhost.newTabSpec("tab1").setIndicator("Auto"/*"Manu"*/)
				.setContent(intentauto)); // R.id.lv
		Intent intentmanu = new Intent();
		intentmanu.setClass(this, manuRunActivity.class);
		myTabhost.addTab(myTabhost.newTabSpec("tab2").setIndicator("Manu"/*"Auto"*/)
				.setContent(intentmanu)); // R.id.TextView02

        //End	
	}
}
