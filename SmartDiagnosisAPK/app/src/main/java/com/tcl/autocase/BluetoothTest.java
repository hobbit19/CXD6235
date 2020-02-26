package com.tcl.autocase;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.Iterator;

import com.tcl.autotest.AllMainActivity;
import com.tcl.autotest.AutotestMainActivity;
import com.tcl.autotest.ExecuteTest;
import com.tcl.autotest.R;
import com.tcl.autotest.autoRunActivity;
import com.tcl.autotest.tool.Tool;
import com.tcl.autotest.utils.DownTimeCallBack;
import com.tcl.autotest.utils.FinishThread;
import com.tcl.autotest.utils.Msg;
import com.tcl.autotest.utils.Test;
import com.tcl.autotest.utils.TestCountDownTimer;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;


public class BluetoothTest extends Test implements DownTimeCallBack {
	private final static String TAG = "BluetoothTest";
	String text = "";
	private BluetoothAdapter mBtAdapter;
	ArrayList<String> mDevicesList = new ArrayList<String>();
	private int mBluetoothState = 0, mBluetoothOldState = 0;
	private IntentFilter filter;
    private int index = 0;
    private TestCountDownTimer testCountDownTimer = null;
    
	private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			Tool.toolLog(TAG + " BluetoothTest action " + action);
			
			// When discovery finds a device
			if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
				/* STATE_OFF, STATE_TURNING_ON, STATE_ON, STATE_TURNING_OFF */
				mBluetoothState = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, 0);
				mBluetoothOldState = intent.getIntExtra(BluetoothAdapter.EXTRA_PREVIOUS_STATE, 0);
				if (mBluetoothState == BluetoothAdapter.STATE_TURNING_ON) {
//					bt_right.setEnabled(false);
//					bt_left.setEnabled(false);
					text = "Enabling Bluetooth...";
					text_cen_zone.setText(text);
//					Tool.toolLog(TAG + "  BluetoothTest STATE_TURNING_ON");
				}
				if (mBluetoothState == BluetoothAdapter.STATE_ON) {
					doDiscovery();
//					bt_right.setEnabled(false);
//					bt_left.setEnabled(false);
					text = "searching...\n";
					text_cen_zone.setText(text);
//					Tool.toolLog(TAG + " STATE_ON");
				}
				if (mBluetoothState == BluetoothAdapter.STATE_TURNING_OFF) {
//					bt_right.setEnabled(false);
//					bt_left.setEnabled(false);
					text = "Disabling Bluetooth...";
					text_cen_zone.setText(text);
//					Tool.toolLog(TAG + " STATE_TURNING_OFF");
				}
				if (mBluetoothState == BluetoothAdapter.STATE_OFF) {
//					Tool.toolLog(TAG + " STATE_OFF");
					mDevicesList.clear();
					onFail();
				}
//				Hashtable<Integer, String> h = new Hashtable<Integer, String>();
//				h.put(BluetoothAdapter.STATE_OFF, "STATE_OFF");
//				h.put(BluetoothAdapter.STATE_ON, "STATE_ON");
//				h.put(BluetoothAdapter.STATE_TURNING_OFF, "STATE_TURNING_OFF");
//				h.put(BluetoothAdapter.STATE_TURNING_ON, "STATE_TURNING_ON");
//				Log.d("info", "state changed " + h.get(mBluetoothOldState) + "=>" + h.get(mBluetoothState));

			} else if (BluetoothDevice.ACTION_FOUND.equals(action)) {
				// Get the BluetoothDevice object from the Intent
				BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
				
				String btinfo = device.getName()+ " : " + device.getAddress();
				if (!mDevicesList.contains(btinfo)) {
					mDevicesList.add(btinfo);
					index++;
				} 
//				Tool.toolLog(TAG + " ACTION_FOUND " + index);
				
				if (!mDevicesList.isEmpty()) {
					text = "Devices :\n";
					for(Iterator<String> it=mDevicesList.iterator(); it.hasNext(); ){
						text = text + it.next() + "\n";
					}
					text_cen_zone.setText(text);
//					bt_right.setEnabled(true);
//					bt_left.setEnabled(true);
//					Tool.toolLog(TAG + " " + text);
					if(index == 1){
						onSucess();
					}
				}
				// When discovery is finished, change the Activity title
			} else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
				if (mDevicesList.size() == 0) {
					text = "No device found";
					text_cen_zone.setText(text);
//					bt_right.setEnabled(true);
//					bt_left.setEnabled(true);
					onFail();
				}
//				Tool.toolLog(TAG + " ACTION_DISCOVERY_FINISHED");
			} else if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
//				Tool.toolLog(TAG + " ACTION_DISCOVERY_STARTED started");
			} else {
				return; /* don't change test state */
			}
		}
	};

	public BluetoothTest(ID id, String name) {
		super(id, name);
		// TODO Auto-generated constructor stub
		
	}

	public BluetoothTest(ID id, String name, Boolean updateFlag) {
		super(id, name, updateFlag);
		// TODO Auto-generated constructor stub
	}

	public BluetoothTest(ID id, String name, Boolean updateFlag, long timeout) {
		super(id, name, updateFlag, timeout);
		// TODO Auto-generated constructor stub
	}

	public void initBt() {
		mBtAdapter = BluetoothAdapter.getDefaultAdapter();
		
		testCountDownTimer = new TestCountDownTimer(30*SECOND, SECOND, this);
		testCountDownTimer.start();
		
		if (!mBtAdapter.isEnabled()) {
//			Tool.toolLog(TAG + " 11111 isEnabled");
			mBtAdapter.enable();
		} else {
			doDiscovery();
			text = "begin searching...\n";
		}
	}

	@Override
	public void setUp() {
		// TODO Auto-generated method stub
		Test.gettag = TAG;
		Test.state = null;
		Tool.toolLog(TAG + "_start_test");
		filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
		filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
		filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
		filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
		mContext.registerReceiver(mReceiver, filter);
		text = "Enabling Bluetooth...";
//		Tool.toolLog(TAG + " BluetoothTest 1111111111");
		initBt();
	}

	private void doDiscovery() {
//		Tool.toolLog(TAG + " doDiscovery()");
		// If we're already discovering, stop it
		if (mBtAdapter.isDiscovering()) {
//			Tool.toolLog(TAG + " 2222 isDiscovering");
			mBtAdapter.cancelDiscovery();
		}
		// Request discover from BluetoothAdapter
		mBtAdapter.startDiscovery();
	}

	@Override
	public void initView() {
		// TODO Auto-generated method stub
		mLayout = (LinearLayout) View.inflate(mContext, R.layout.base_screen,null);

//		bt_left = (Button) mLayout.findViewById(R.id.bt_left);
//		bt_right = (Button) mLayout.findViewById(R.id.bt_right);
		text_top_zone = (TextView) mLayout.findViewById(R.id.text_top_zone);
		text_cen_zone = (TextView) mLayout.findViewById(R.id.text_cen_zone);

		text_top_zone.setText(mName);
		text_cen_zone.setText(text);

//		bt_left.setText(R.string.pass);
//		bt_right.setText(R.string.fail);
//		bt_right.setEnabled(false);
//		bt_left.setEnabled(false);
//		bt_left.setOnClickListener(pass_listener);
//		bt_right.setOnClickListener(failed_listener);
		//PR661395-yinbin-zhang-20140516 end

		mContext.setContentView(mLayout);
//		Tool.toolLog(TAG + " BluetoothTest 0000000000");
	}

	public void finish() {
		// TODO Auto-generated method stub
//		Tool.toolLog(TAG + " BluetoothTest finish");
		if (mBtAdapter.isDiscovering()) {
			mBtAdapter.cancelDiscovery();
		}
		mBtAdapter.disable();
		try {
			if (mReceiver != null) {
				mContext.unregisterReceiver(mReceiver);
			}
		} catch (Exception e) {
			Tool.toolLog(TAG + " already unregistered");
		}
		
		if(testCountDownTimer!=null){
			testCountDownTimer.cancel();
		}
	}

	//public static String state = null ;
//	@Override
	public void onSucess() {
		// TODO Auto-generated method stub
//		Tool.toolLog(TAG + " onFinish");
		Test.state = "Pass";
		Tool.toolLog(TAG + " Test.state " + Test.state);
		FinishThread  finishThread = new FinishThread(0x01,ExecuteTest.temppositon);
		finishThread.start();
		try {
			finishThread.join();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		/*Jianke
		 * ���Ҳ����Ҫͬ��*/
		Msg.exitWithSuccessTest(mContext, TAG, 10,true,"Pass");
		//Write data into file
		if(AllMainActivity.mainAllTest && !AllMainActivity.autofileFlag){
			Msg.WriteModelResult(mContext,AllMainActivity.all_items_file_text, "PASS");
		}else{
			Msg.WriteModelResult(mContext,autoRunActivity.auto_file_text, "PASS");
		}
		//End
	}

	public void onFail(){
//		Tool.toolLog(TAG + " onFail");
		//Begin Add By Jianke.Zhang 2015/01/14
		//Write data into file
		Test.state = "Fail";
		mContext.setResult(Test.RESULT.FAILED.ordinal());
		Tool.toolLog(TAG + " index 8882 -> " + ExecuteTest.temppositon);
		int double_test;
		if(AllMainActivity.mainAllTest){
			double_test = AllMainActivity.double_check_for_auto[ExecuteTest.temppositon]++;
			//AllMainActivity.mainAllTest = false;
		}else {
			double_test = autoRunActivity.double_check[ExecuteTest.temppositon]++;
		}
		Tool.toolLog(TAG + " double_test 9992-> " + double_test);
	
		FinishThread tFinishThread = new FinishThread(0x02,ExecuteTest.temppositon);
		tFinishThread.start();
		try {
			tFinishThread.join();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		/*Jianke
		 * ���Ҳ����Ҫͬ��*/
		if(double_test==1){
			Msg.exitWithException(mContext,TAG,50,true,"Pass");
			if(AllMainActivity.mainAllTest && !AllMainActivity.autofileFlag){
				Msg.WriteModelResult(mContext,AllMainActivity.all_items_file_text, "FAIL");
			}else {
				Msg.WriteModelResult(mContext,autoRunActivity.auto_file_text, "FAIL");
			}
		}else{
			Msg.exitWithException(mContext,TAG,50,true,"Fail");
		}
		//End
	}
	
	@Override
	public void onTick() {
		// TODO Auto-generated method stub
//		Tool.toolLog(TAG + " onTick ...");
	}

	@Override
	public void onFinish() {
		// TODO Auto-generated method stub
		Tool.toolLog(TAG + " onFinish ...");
		onFail();
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
	    finish();
	}
	
}
