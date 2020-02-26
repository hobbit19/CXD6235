package com.tcl.manucase;

import android.app.KeyguardManager;
import android.app.KeyguardManager.KeyguardLock;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.tcl.autotest.AllMainActivity;
import com.tcl.autotest.ExecuteTest;
import com.tcl.autotest.R;
import com.tcl.autotest.autoRunActivity;
import com.tcl.autotest.tool.HomeKeyLocker;
import com.tcl.autotest.tool.Tool;
import com.tcl.autotest.utils.DownTimeCallBack;
import com.tcl.autotest.utils.FinishThread;
import com.tcl.autotest.utils.Msg;
import com.tcl.autotest.utils.Test;
import com.tcl.autotest.utils.TestCountDownTimer;

/**
 * creat by nanbing.zou for A5A INFINI at 2017-11-23
 */

public class TPlockTest extends Test implements DownTimeCallBack{

	private static final String TAG = "TPlockTest";
	private boolean isPowerKey = false;

	FrameLayout fl = null;
	IntentFilter intentfilter;
	ScreenOnOffReceiver mPowerReceiver;
	private TestCountDownTimer testCountDownTimer = null;

	public TPlockTest(ID id, String name) {
		super(id, name);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void onFinish() {
		failed();
	}

	@Override
	public void onTick() {

	}

	@Override
	public void setUp() {
		// TODO Auto-generated method stub
		Tool.toolLog(TAG + "_start_test");

		fl = new FrameLayout(mContext);
		mLayout = (LinearLayout) View.inflate(mContext,
				R.layout.manu_base_screen, null);

		mPowerReceiver = new ScreenOnOffReceiver();
		intentfilter = new IntentFilter(Intent.ACTION_SCREEN_OFF);
		mContext.registerReceiver(mPowerReceiver, intentfilter);
		testCountDownTimer = new TestCountDownTimer(20*SECOND, SECOND, this);
		testCountDownTimer.start();
	}

	@Override
	public void initView() {
		// TODO Auto-generated method stub

		bt_left = (Button) mLayout.findViewById(R.id.bt_left);
		bt_right = (Button) mLayout.findViewById(R.id.bt_right);
		text_top_zone = (TextView) mLayout.findViewById(R.id.text_top_zone);

		text_cen_zone = (TextView) mLayout.findViewById(R.id.text_cen_zone);

		text_top_zone.setText(mName);
		text_cen_zone.setText("Please press power key!");
		bt_left.setText(R.string.pass);
		bt_right.setText(R.string.fail);

		bt_left.setOnClickListener(pass_listener);
		bt_right.setOnClickListener(failed_listener);
		bt_left.setEnabled(false);
		fl.addView(mLayout);
		mContext.setContentView(fl);
	}

	@Override
	public void finish() {
		// TODO Auto-generated method stub
		if(testCountDownTimer!=null){
			testCountDownTimer.cancel();
		}
	}


	@Override
	public boolean onKeyTouch(KeyEvent event) {
		// TODO Auto-generated method stub
//		Message msg = new Message();
//		int keyCode = event.getKeyCode();
////		Tool.toolLog(TAG + " " + mName + " " + String.valueOf(keyCode));
//		switch (keyCode) {
//		case KeyEvent.KEYCODE_POWER:
//		case KeyEvent.KEYCODE_VOLUME_DOWN:
//			msg.what = KeyEvent.KEYCODE_POWER;
//			mHandler.sendMessage(msg);
//			break;
//		}
		return true;
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
//		Tool.toolLog(TAG + " onKeyDown 88888888");
		return true;
	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
//		Tool.toolLog(TAG + " onKeyUp 66666666");

		return true;
	}

	private Handler mHandler = new Handler() {

		@Override
		public void dispatchMessage(Message msg) {
			// TODO Auto-generated method stub

			switch (msg.what) {
				case KeyEvent.KEYCODE_POWER:
				case KeyEvent.KEYCODE_VOLUME_DOWN:
					isPowerKey = true;
					break;
			}

			if (isPowerKey) {
				bt_left.setEnabled(true);
//				pass();
				if(testCountDownTimer!=null){
					testCountDownTimer.cancel();
				}
			}

			super.handleMessage(msg);
		}

	};



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
		mContext.unregisterReceiver(mPowerReceiver);
		if (testCountDownTimer != null) {
			testCountDownTimer.cancel();
			testCountDownTimer = null;
		}
	}



	private class ScreenOnOffReceiver extends BroadcastReceiver{
		@Override
		public void onReceive(Context context, Intent intent) {

			if(intent.getAction().equals(Intent.ACTION_SCREEN_OFF)){
				isPowerKey = true;
				mHandler.sendEmptyMessage(0);

			}

		}
	}

}
