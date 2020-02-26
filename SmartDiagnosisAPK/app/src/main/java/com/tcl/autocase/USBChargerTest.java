package com.tcl.autocase;

import java.io.IOException;

import com.tcl.autotest.AllMainActivity;
import com.tcl.autotest.ExecuteTest;
import com.tcl.autotest.R;
import com.tcl.autotest.autoRunActivity;
import com.tcl.autotest.tool.Tool;
import com.tcl.autotest.utils.DownTimeCallBack;
import com.tcl.autotest.utils.FinishThread;
import com.tcl.autotest.utils.Msg;
import com.tcl.autotest.utils.Test;
import com.tcl.autotest.utils.TestCountDownTimer;
import com.tcl.autotest.utils.TracabilityStruct;


//import com.jrdcom.utils.JRDRapi;
//import android.content.Context;
import android.os.BatteryManager;
import android.os.Handler;
import android.telephony.TelephonyManager;
//import com.mediatek.telephony.TelephonyManagerEx;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

//import com.mediatek.common.featureoption.FeatureOption;

public class USBChargerTest extends Test implements DownTimeCallBack {

	private final static String TAG = "USBChargerTest";
	
	UsbManager usm = null;
	
	private TestCountDownTimer testCountDownTimer = null;

	String mDisplayUSBChargerTestString = "USB Charging";

	boolean State = false;
	
	public USBChargerTest(ID id, String name) {
		super(id, name);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void setUp() {
		// TODO Auto-generated method stub
		Tool.toolLog(TAG + "_start_test");
		testCountDownTimer = new TestCountDownTimer(SECOND*6, SECOND, this);
		
//		usm =  (UsbManager) mContext.getSystemService(Context.USB_SERVICE);
		
		IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
		Intent batteryStatus = mContext.registerReceiver(null, ifilter);
		
		// �Ƿ��ڳ��
		int status = batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
		boolean isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
		                     status == BatteryManager.BATTERY_STATUS_FULL;
		// ��ô��
		int chargePlug = batteryStatus.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
		boolean usbCharge = chargePlug == BatteryManager.BATTERY_PLUGGED_USB;
		boolean acCharge = chargePlug == BatteryManager.BATTERY_PLUGGED_AC;
		
		Tool.toolLog(TAG + " isCharging " + isCharging + " usbCharge " + usbCharge);
		if(isCharging || (usbCharge || acCharge)){
			State = true;
		}
	}

	@Override
	public void initView() {
		// TODO Auto-generated method stub
		mLayout = (LinearLayout) View.inflate(mContext, R.layout.base_screen,
				null);

		text_top_zone = (TextView) mLayout.findViewById(R.id.text_top_zone);
		text_cen_zone = (TextView) mLayout.findViewById(R.id.text_cen_zone);

		text_top_zone.setText(mName);
		text_cen_zone.setText(mDisplayUSBChargerTestString);

		mContext.setContentView(mLayout);
		testCountDownTimer.start();
		
		mHandler.sendEmptyMessageDelayed(0, SECOND);
	}

	public Handler mHandler = new Handler(){
		
		public void handleMessage(android.os.Message msg) {
			
			if (State == true){
				text_cen_zone.setText(mDisplayUSBChargerTestString + " is OK!");
				Tool.sleepTimes(20);
				//Add by Jianke.Zhang 02/02
				FinishThread tFinishThread = new FinishThread(0x01,ExecuteTest.temppositon);
				tFinishThread.start();
				try {
					tFinishThread.join();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				Msg.exitWithSuccessTest(mContext,TAG,20,true,"Pass");
				
				//Write data into file
				if(AllMainActivity.mainAllTest && !AllMainActivity.autofileFlag){
					Msg.WriteModelResult(mContext,AllMainActivity.all_items_file_text, "PASS");
				}else{
					Msg.WriteModelResult(mContext,autoRunActivity.auto_file_text, "PASS");
				}
				//End
			}
		};
	};
	
	
	@Override
	public void onTick() {
//		Tool.toolLog(TAG + " onTick ...");
	}

	@Override
	public void onFinish() {
		// TODO Auto-generated method stub
//		mContext.setResult(Test.RESULT.PASS.ordinal());
		Tool.toolLog(TAG + " onFinish ... ");
		if(!State){
			text_cen_zone.setText(mDisplayUSBChargerTestString + " is Wrong!");
			Tool.sleepTimes(20);
			mContext.setResult(Test.RESULT.FAILED.ordinal());
			Tool.toolLog(TAG + " index 888 -> " + ExecuteTest.temppositon);
			int double_test;
			if(AllMainActivity.mainAllTest){
				double_test = AllMainActivity.double_check_for_auto[ExecuteTest.temppositon]++;
				//AllMainActivity.mainAllTest = false;
			}else {
				double_test = autoRunActivity.double_check[ExecuteTest.temppositon]++;
			}
			Tool.toolLog(TAG + " double_test 999-> " + double_test);
			FinishThread tFinishThread = new FinishThread(0x02,ExecuteTest.temppositon);
			tFinishThread.start();
			try {
				tFinishThread.join();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if(double_test==1){
				Msg.exitWithException(mContext,TAG,20,true,"Pass");
				if(AllMainActivity.mainAllTest && !AllMainActivity.autofileFlag){
					Msg.WriteModelResult(mContext,AllMainActivity.all_items_file_text, "FAIL");
				}else{
					Msg.WriteModelResult(mContext,autoRunActivity.auto_file_text, "FAIL");
				}
			}else{
				Msg.exitWithException(mContext,TAG,20,true,"Fail");
			}
		}
	}

	@Override
	public void finish() {
		// TODO Auto-generated method stub
		Tool.toolLog(TAG + " finish");
	}

	public String getmContextTag() {
		// TODO Auto-generated method stub
		return TAG;
	}

	public void setmContextTag() {
		// TODO Auto-generated method stub
	}

	@Override
	public void destroy() {
		// TODO Auto-generated method stub
		if (testCountDownTimer != null) {
			testCountDownTimer.cancel();
			testCountDownTimer = null;
		}
	}

}
