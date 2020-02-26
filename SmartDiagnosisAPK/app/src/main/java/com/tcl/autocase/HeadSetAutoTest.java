package com.tcl.autocase;


import java.util.Iterator;

import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NfcAdapter;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

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

public class HeadSetAutoTest extends Test implements DownTimeCallBack{
	private final static String TAG = "HeadSetTest";
	String text = "";
	private TestCountDownTimer testCountDownTimer = null;
	private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			 if(intent.hasExtra("state")){  
	              /*  if(intent.getIntExtra("state", 0)==0){  
	                    //Toast.makeText(context, "headset not connected", Toast.LENGTH_LONG).show(); 
	                	text = "headset not connected\n";
						text_cen_zone.setText(text);
						finish();
						onFail();
						
	                } */ 
	                 if(intent.getIntExtra("state", 0)==1){  
	                    //Toast.makeText(context, "headset  connected", Toast.LENGTH_LONG).show();  
	                	text = "headset  connected\n";
						text_cen_zone.setText(text);
						finish();
						onSucess();
						
						
	                }  
	            }
		}
	};
	
	public HeadSetAutoTest(ID id, String name) {
		super(id, name);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void onFinish() {
		// TODO Auto-generated method stub
		finish();
		onFail();
	}

	@Override
	public void onTick() {
		// TODO Auto-generated method stub
	}

	@Override
	public void setUp() {
		// TODO Auto-generated method stub
		Tool.toolLog(TAG + "_start_test");
		Log.d(TAG, " 33333 3_start_test");
		 IntentFilter  filter = new IntentFilter(); 
		  filter.addAction("android.intent.action.HEADSET_PLUG");
		  mContext.registerReceiver(mReceiver, filter);
		   text = "begin searching...\n";
		   testCountDownTimer = new TestCountDownTimer(24*SECOND, SECOND, this);
		   testCountDownTimer.start();
		   text = "Waiting ...";
	}

	@Override
	public void initView() {
				mLayout = (LinearLayout) View.inflate(mContext, R.layout.base_screen,null);

				text_top_zone = (TextView) mLayout.findViewById(R.id.text_top_zone);
				text_cen_zone = (TextView) mLayout.findViewById(R.id.text_cen_zone);

				text_top_zone.setText(mName);
				text_cen_zone.setText(text);

				mContext.setContentView(mLayout);
	}

	@Override
	public void finish() {
		// TODO Auto-generated method stub
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

	@Override
	public String getmContextTag() {
		// TODO Auto-generated method stub
		return TAG;
	}

	@Override
	public void setmContextTag() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void destroy() {
		// TODO Auto-generated method stub
		finish();
	}
	public void onSucess() {
		// TODO Auto-generated method stub
//		Tool.toolLog(TAG + " onFinish");
		Test.state = "Pass";
		Tool.toolLog(TAG + " Test.state " + Test.state);
		Log.d(TAG, " 33333 Test.state " + Test.state);
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
		Tool.toolLog(TAG + " onFail");
		Log.d(TAG, "33333 onFail");
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
		Log.d(TAG, "33333 onFail double_test: " + double_test);
		if(double_test==1){
			Log.d(TAG, "33333 onFail Msg.exitWithException(mContext,TAG,50,true,Pass)");
			Msg.exitWithException(mContext,TAG,50,true,"Pass");
			if(AllMainActivity.mainAllTest && !AllMainActivity.autofileFlag){
				Msg.WriteModelResult(mContext,AllMainActivity.all_items_file_text, "FAIL");
			}else {
				Msg.WriteModelResult(mContext,autoRunActivity.auto_file_text, "FAIL");
			}
		}else{
			Log.d(TAG, "33333 onFail Msg.exitWithException(mContext,TAG,50,true,Fail)");
			Msg.exitWithException(mContext,TAG,50,true,"Fail");
		}
		//End
	}
}
