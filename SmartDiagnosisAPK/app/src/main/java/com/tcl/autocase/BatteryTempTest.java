package com.tcl.autocase;

import java.io.BufferedReader;

import com.tcl.autotest.R;
import com.tcl.autotest.tool.Tool;
import com.tcl.autotest.utils.DownTimeCallBack;
import com.tcl.autotest.utils.Test;
import com.tcl.autotest.utils.TestCountDownTimer;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class BatteryTempTest extends Test implements DownTimeCallBack {

	private static String TAG = "BatteryTempTest";
	
	BufferedReader mBattTempReader;
	String text = "";
	private IntentFilter mIntentFilter;
	private String mBattTempValue;
	private String mBattContValue;
	private String mBattVoltageValue;
	private int mBattTemp;
	private boolean broadFlag = false;
	private TestCountDownTimer testCountDownTimer = null;

	private boolean isGetSuccess = false;
	
	public BatteryTempTest(ID id, String name) {
		super(id, name);
		// TODO Auto-generated constructor stub
	}

	private final String tenthsToFixedString(int x) {
		int tens = x / 10;
		return Integer.toString(tens) + "." + (x - 10 * tens);
	}

	private final String milliToFixedString(int x) {
		int tens = x / 1000;
		return Integer.toString(tens) + "." + (x - 1000 * tens);
	}

	private final BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
//			Tool.toolLog(TAG + " action " + action);
			if(!isGetSuccess){
				if (action.equals(Intent.ACTION_BATTERY_CHANGED)) {
					mBattTemp = intent.getIntExtra("temperature", 0);
	//				Tool.toolLog(TAG + " mBattTemp " + mBattTemp);
					mBattTempValue = tenthsToFixedString(intent.getIntExtra(
							"temperature", 0));
					mBattContValue = "" + intent.getIntExtra("level", 0);
					mBattVoltageValue = milliToFixedString(intent.getIntExtra(
							"voltage", 0));
	
					if (mBattTempValue == null) {
						text = "can't read battery temperature";
					} else {
						text = "battery temperature\n"
								+ "should be between\n"
								+ "20~50 C\n"
								+ "current is :\n"
								+ mBattTempValue
								+ " C\n\nBattery content should\nbe between 30~80%\n"
								+ "current is " + mBattContValue + "%\n\n"
								+ "Battery Voltage is " + mBattVoltageValue + "V";
						//the temperature must in the standard
						if(Double.parseDouble(mBattTempValue) > 20.0 && Double.parseDouble(mBattTempValue) < 50.0){
							mHandler.sendEmptyMessageDelayed(0, SECOND);
							isGetSuccess = true;
						}
					}
				}
			}
		}
	};

	Handler mHandler = new Handler(){
		
		public void handleMessage(android.os.Message msg) {
			
			//success
			text_cen_zone.setText(text);
			Tool.success(mContext,TAG);
			
		};
	};
	
	
	@Override
	public void setUp() {
		// TODO Auto-generated method stub
		Tool.toolLog(TAG + "_start_test");
		testCountDownTimer = new TestCountDownTimer(5*SECOND, SECOND, this);
		testCountDownTimer.start();
		
		mIntentFilter = new IntentFilter();
		mIntentFilter.addAction(Intent.ACTION_BATTERY_CHANGED);
		mContext.registerReceiver(mIntentReceiver, mIntentFilter);
		broadFlag = true;
		text = "Init battery broadcast... ...";
	}

	@Override
	public void initView() {
		// TODO Auto-generated method stub
		mLayout = (LinearLayout) View.inflate(mContext, R.layout.base_screen,
				null);

		text_top_zone = (TextView) mLayout.findViewById(R.id.text_top_zone);
		text_cen_zone = (TextView) mLayout.findViewById(R.id.text_cen_zone);

		text_top_zone.setText(mName);
		text_cen_zone.setText(text);
		// PR661395-yinbin-zhang-20140516 begin
		/*
		 * bt_left.setText(R.string.fail); bt_right.setText(R.string.pass);
		 * 
		 * bt_left.setOnClickListener(failed_listener);
		 * bt_right.setOnClickListener(pass_listener);
		 */
		// PR661395-yinbin-zhang-20140516 end

		mContext.setContentView(mLayout);
	}

	@Override
	public void destroy() {
		// TODO Auto-generated method stub
		if (broadFlag) {
			broadFlag = false;
			mContext.unregisterReceiver(mIntentReceiver);
		}
		if (testCountDownTimer != null) {
			testCountDownTimer.cancel();
		}
	}

	@Override
	public void onFinish() {
		// TODO Auto-generated method stub
		text_cen_zone.setText(text);
		Tool.fail(mContext,TAG);
		
	}

	@Override
	public void onTick() {
		// TODO Auto-generated method stub

	}

	public String getmContextTag() {
		// TODO Auto-generated method stub
		return TAG;
	}

	@Override
	public void setmContextTag() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void finish() {
		// TODO Auto-generated method stub
		
	}

}
