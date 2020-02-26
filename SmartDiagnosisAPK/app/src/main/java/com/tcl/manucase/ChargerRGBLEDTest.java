package com.tcl.manucase;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.ContentResolver;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.tcl.autotest.ExecuteTest;
import com.tcl.autotest.R;
import com.tcl.autotest.tool.Tool;
import com.tcl.autotest.utils.DownTimeCallBack;
import com.tcl.autotest.utils.Msg;
import com.tcl.autotest.utils.Test;
import com.tcl.autotest.utils.TestCountDownTimer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;

public class ChargerRGBLEDTest extends Test implements DownTimeCallBack{

	private static final String TAG = "ChargerRGBLEDTest";
	String text = "Please check charger LED";

	// PR661412-yinbin-zhang-20140515 begin
	private FrameLayout mFrameLayout;
	private TestCountDownTimer mTestCountDownTimer;
	// PR661412-yinbin-zhang-20140515 end

	private final static int CHARGE_LED_ID = 1000;
	private NotificationManager mNotificationManager = null;
	private ContentResolver mContentResolver = null;
	private int notificationLight = 0;
	private enum CHARGE_LED {
		RED, GREEN, BLUE,
	}

	private Notification mNotification;
	private CHARGE_LED mLED = CHARGE_LED.RED;

	public ChargerRGBLEDTest(ID id, String name) {
		super(id, name);
	}

	// PR661412-yinbin-zhang-20140515 begin


	@Override
	public void setUp() {
		// TODO Auto-generated method stub
		Tool.toolLog(TAG + "_start_test");
		//First judge the file if exist 07/07
		mTestCountDownTimer = new TestCountDownTimer(30*SECOND, SECOND, this);
		mTestCountDownTimer.start();
	}

	@Override
	public void initView() {
		// init layout view for case display
//		Tool.toolLog(TAG + " initView ...");
		mLayout = (LinearLayout) View.inflate(mContext, R.layout.manu_base_screen,
				null);

		bt_left = (Button) mLayout.findViewById(R.id.bt_left);
		bt_right = (Button) mLayout.findViewById(R.id.bt_right);
		text_top_zone = (TextView) mLayout.findViewById(R.id.text_top_zone);
		text_cen_zone = (TextView)mLayout.findViewById(R.id.text_cen_zone);

		text_top_zone.setText(mName);
		text_cen_zone.setText(text);

		 bt_left.setText(R.string.pass);
		 bt_right.setText(R.string.fail);
		
		 bt_left.setOnClickListener(pass_listener);
		 bt_left.setEnabled(false);
		 bt_right.setOnClickListener(failed_listener);

		mFrameLayout = new FrameLayout(mContext);
		mFrameLayout.addView(mLayout);
		mContext.setContentView(mFrameLayout);
		testprogress();
	}

	public void testprogress() {

		mNotificationManager = (NotificationManager) mContext.getSystemService(mContext.NOTIFICATION_SERVICE);
		mContentResolver = mContext.getContentResolver();

		notificationLight = Settings.System.getInt(mContentResolver, Settings.System.NOTIFICATION_LIGHT_PULSE, 0);

		Notification.Builder mBuild = new Notification.Builder(mContext);
		mNotification = mBuild.build();
		if (notificationLight == 0) {
			Settings.System.putInt(mContentResolver, Settings.System.NOTIFICATION_LIGHT_PULSE, 1);
		}

		Settings.Global.putInt(mContentResolver, "tct_mmitest", 1);

		if (mLED == CHARGE_LED.RED) {
			mNotification.ledARGB = 0xffff0000;
		} else if (mLED == CHARGE_LED.GREEN) {
			mNotification.ledARGB = 0xff00ff00;
			text_cen_zone.setText("Test green!");
		} else {
			mNotification.ledARGB = 0xff0000ff;
			text_cen_zone.setText("Test blue!");
		}
		mNotification.flags |= Notification.FLAG_SHOW_LIGHTS;
		mNotification.ledOnMS = 1;
		mNotification.ledOffMS = 0;
		mNotificationManager.notify(CHARGE_LED_ID, mNotification);
	}
	
	@Override
	public void destroy() {
		// TODO Auto-generated method stub
		if(mTestCountDownTimer != null){
			mTestCountDownTimer.cancel();
			mTestCountDownTimer = null;
		}
	}



	@Override
	public void onFinish() {
		// TODO Auto-generated method stub
		Msg.exitWithException(mContext,TAG,10,false,"Fail");
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
