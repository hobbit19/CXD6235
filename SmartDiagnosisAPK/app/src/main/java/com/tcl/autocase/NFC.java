package com.tcl.autocase;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
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

public class NFC extends Test implements DownTimeCallBack {
	private final static String TAG = "NFC";
  public static	String text = "";
	private TestCountDownTimer testCountDownTimer = null;
	
	private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			
			//Log.i("qinhao", "jieshouguangbo");
			 Bundle bundle = intent.getExtras();
			String result = bundle.getString("result");
			//Log.i("qinhao", "jieshouguangbo::::"+result);
			if(result.equals("success"))
			{
				text = "NFC  connected\n";
				text_cen_zone.setText(text);
				finish();
				onSucess();
			}
			else if(result.equals("fail"))
			{
				text = "NFC not connected\n";
				text_cen_zone.setText(text);
				finish();
				onFail();
			}
			
		}
	};
	public NFC(ID id, String name) {
		super(id, name);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void onFinish() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onTick() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setUp() {
		// TODO Auto-generated method stub
		Tool.toolLog(TAG + " _start_test");
		Log.d(TAG, "33333 _start_test");
		   IntentFilter filters = new IntentFilter();  
	        filters.addAction("com.sdt");  
	        filters.setPriority(Integer.MAX_VALUE);  
	       mContext.registerReceiver(mReceiver, filters); 
		   text = "begin searching...\n";
		   testCountDownTimer = new TestCountDownTimer(10*SECOND, SECOND, this);
		   testCountDownTimer.start();
		   text = "Waiting ...";
		
	}

	@Override
	public void initView() {
		// TODO Auto-generated method stub
		mLayout = (LinearLayout) View.inflate(mContext, R.layout.base_screen,null);

		text_top_zone = (TextView) mLayout.findViewById(R.id.text_top_zone);
		text_cen_zone = (TextView) mLayout.findViewById(R.id.text_cen_zone);

		text_top_zone.setText(mName);
		text_cen_zone.setText(text);
		 

		Log.d(TAG, "33333 initView 111");
		mContext.setContentView(mLayout);
		   Intent intent=new Intent();  
           intent.setClass(mContext, NFCAutoTest.class);  
           //MainActivity.this.startActivity(intent);
		mContext.startActivity(intent);
		Log.d(TAG, "33333 initView 222");
		
	}
/*	// �ص��������ӵڶ���ҳ�������ʱ���ִ���������
		@Override
		protected void onActivityResult(int requestCode, int resultCode, Intent data) {
			String change01 = data.getStringExtra("change01");
			String change02 = data.getStringExtra("change02");
			// �������淢�͹�ȥ��������������
			switch (requestCode) {
			case 0:
				text_cen_zone.setText(change01);
				break;

			default:
				break;
			}
		}*/
	@Override
	public void finish() {
		// TODO Auto-generated method stub
		
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
		mContext.unregisterReceiver(mReceiver);
	}
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
}
