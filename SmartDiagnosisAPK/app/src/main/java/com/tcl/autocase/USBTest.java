package com.tcl.autocase;

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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.Handler;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

public class USBTest extends Test implements DownTimeCallBack {

	private static String TAG = "USBTest";
	String text = "USB Test";
	private TestCountDownTimer testCountDownTimer = null;
	private boolean mRegistered = false;

	IntentFilter inf = null;
	private PlugStateReceiever USBRcv = null;
	private boolean mUSBStatus = false;

	private boolean isGetUSBPlug = false;
	
	public USBTest(ID id, String name, Boolean updateFlag, long timeout) {
		super(id, name, updateFlag, timeout);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void setUp() {
		Tool.toolLog(TAG + "_start_test");
		inf = new IntentFilter();
		inf.addAction(Intent.ACTION_UMS_DISCONNECTED);
		inf.addAction(Intent.ACTION_UMS_CONNECTED);
		inf.addAction(Intent.ACTION_BATTERY_CHANGED);

		testCountDownTimer = new TestCountDownTimer(6*SECOND, SECOND, this);
		testCountDownTimer.start();
		
		if (!mRegistered) {
			USBRcv = new PlugStateReceiever();
			mContext.registerReceiver(USBRcv, inf);
			mRegistered = true;
		}
//		ShowStatus(mUSBStatus);
	}

	@Override
	public void initView() {
		// TODO Auto-generated method stub
		mLayout = (LinearLayout) View.inflate(mContext,
				R.layout.base_screen, null);

//		bt_left = (Button) mLayout.findViewById(R.id.bt_left);
//		bt_right = (Button) mLayout.findViewById(R.id.bt_right);
		text_top_zone = (TextView) mLayout.findViewById(R.id.text_top_zone);
		text_cen_zone = (TextView) mLayout.findViewById(R.id.text_cen_zone);

		text_top_zone.setText(mName);
		text_cen_zone.setText(text);

		// PR661432-yinbin-zhang-20140515 begin
		/*
		 * bt_left.setText(R.string.fail); bt_right.setText(R.string.pass);
		 * bt_right.setEnabled(false);
		 * 
		 * bt_left.setOnClickListener(failed_listener);
		 * bt_right.setOnClickListener(pass_listener);
		 */

//		bt_left.setText(R.string.pass);
//		bt_right.setText(R.string.fail);
//		bt_left.setEnabled(false);
//		bt_right.setEnabled(false);

//		bt_left.setOnClickListener(pass_listener);
//		bt_right.setOnClickListener(failed_listener);

		mContext.setContentView(mLayout);
	}

	@Override
	public void destroy() {
		// TODO Auto-generated method stub

		mUSBStatus = false;
		if (mRegistered) {
			mContext.unregisterReceiver(USBRcv);
			mRegistered = false;
		}
		if (testCountDownTimer != null) {
			testCountDownTimer.cancel();
		}
	}

	private void ShowStatus(boolean usbStatus) {
		// PR661432-yinbin-zhang-20140515 begin
		/*
		 * text = "Please insert USB cable.\n\n"; if(usbStatus){ text = text +
		 * "USB : OK\n"; }else{ text = text + "USB : unknown\n"; }
		 */

		if (usbStatus) {
			text = "USB OK\n\n";
		}
//			text = "Please insert USB cable\n";
		
		// PR661432-yinbin-zhang-20140515 end
	}

	public class PlugStateReceiever extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {

			Tool.toolLog(TAG + " intent received 111 : " + intent.getAction());
			if(!isGetUSBPlug){
				String action = intent.getAction();
//				Tool.toolLog(TAG + " intent received 111 : " + action);
				if (action.equals(Intent.ACTION_BATTERY_CHANGED)) {
					int mBatteryPlug = intent.getIntExtra(
							BatteryManager.EXTRA_PLUGGED, -1);

					Tool.toolLog(TAG + " mBatteryPlug: " + mBatteryPlug);

	//				Tool.toolLog(TAG + " intent received 222 : " + mBatteryPlug);
					if ((mBatteryPlug == BatteryManager.BATTERY_PLUGGED_USB || mBatteryPlug == BatteryManager.BATTERY_PLUGGED_AC)
							&& true/*!mUSBStatus*/) {
	//					Tool.toolLog(TAG + " 3333333");
	//					mUSBStatus = false;
	//					ShowStatus(mUSBStatus);
						text_cen_zone.setText("USB OK\n");
						isGetUSBPlug = true;
						
						mHandler.sendEmptyMessageDelayed(0, SECOND);
					} else if (mBatteryPlug != BatteryManager.BATTERY_PLUGGED_USB
							&& true/*mUSBStatus*/) {
	//					Tool.toolLog(TAG + " 44444444");
	//					mUSBStatus = true;
	//					ShowStatus(mUSBStatus);
						text_cen_zone.setText("Please insert USB cable");
					}
				}
			}
		}
	}

	Handler mHandler = new Handler(){
		
		public void handleMessage(android.os.Message msg) {
			success();
		};
	};
	
	private void success(){
		//�����⵽�Ѿ� ����USBֱ�ӳɹ�
		mContext.setResult(Test.RESULT.PASS.ordinal());
		FinishThread tFinishThread = new FinishThread(0x01,ExecuteTest.temppositon);
		tFinishThread.start();
		try {
			tFinishThread.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		//����ٶ�̫�죬����û���ֽ���

		Msg.exitWithSuccessTest(mContext,TAG, 10,true,"Pass");
		
		if(AllMainActivity.mainAllTest && !AllMainActivity.autofileFlag){
			Msg.WriteModelResult(mContext,AllMainActivity.all_items_file_text, "PASS");
		}else {
			Msg.WriteModelResult(mContext,autoRunActivity.auto_file_text, "PASS");
		}
	}
	
	@Override
	public void timeout() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onFinish() {
		// TODO Auto-generated method stub
//		text = "USB : OK \n\n " + "Please remove USB cable";
		Tool.toolLog(TAG + " onFinish ... ");
		if(!isGetUSBPlug){
			text_cen_zone.setText("USB Time out.");
			//�����ʱ�仹û�м�⵽USB����ô��ʧ��
			mContext.setResult(Test.RESULT.FAILED.ordinal());
			Tool.toolLog(TAG + " index 8887 -> " + ExecuteTest.temppositon);
			int double_test;
			if(AllMainActivity.mainAllTest){
				double_test = AllMainActivity.double_check_for_auto[ExecuteTest.temppositon]++;
			}else {
				double_test = autoRunActivity.double_check[ExecuteTest.temppositon]++;
			}
			Tool.toolLog(TAG + " double_test 9997 -> " + double_test);
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
				//Write data into file
				if(AllMainActivity.mainAllTest && !AllMainActivity.autofileFlag){
					Msg.WriteModelResult(mContext,AllMainActivity.all_items_file_text, "FAIL");
				}else {
					Msg.WriteModelResult(mContext,autoRunActivity.auto_file_text, "FAIL");
				}
				//End
			}else{
				Msg.exitWithException(mContext,TAG,20,true,"Fail");
			}
		}
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
