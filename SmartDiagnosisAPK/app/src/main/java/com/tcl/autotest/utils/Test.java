package com.tcl.autotest.utils;


import java.io.IOException;

import com.tcl.autotest.AllMainActivity;
import com.tcl.autotest.AutotestMainActivity;
import com.tcl.autotest.ExecuteManuTest;
import com.tcl.autotest.manuRunActivity;
import com.tcl.autotest.tool.Tool;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.os.Handler;
import android.os.Message;
import android.os.UserHandle;
import android.provider.Settings;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public abstract class Test {
	private static final String TAG = "Test";

	// Current running activity
	protected Activity mContext = null;
	
	// Current running test name
	protected String mName;
	
	// Test flag to control auto test or maui test
	public Boolean updateFlag = false;
	
	// Test time out 
	protected long tout = 0;
	
	// Current running test id
	protected int mId;
	
	// Indicate current case is running on Auto mode or Maui mode.
	public static MODE flag = Test.MODE.NONE;
	
	// Indicate the test result
	public static RESULT result = RESULT.NOT_TEST;
	
	//lock 
	public static Object lock = new Object();
	
	// Default UI with two button
	protected   LinearLayout mLayout = null;
	protected   Button bt_left = null;
	protected   Button bt_right = null;
	protected TextView text_top_zone = null;
	protected TextView text_cen_zone = null;
	
	public static String state;
	public static String gettag;
	
	// Test flag to control auto test or maui test
	public enum MODE {
		AUTO,
		MAUI,
		NONE,
	};
	
	public enum RESULT {
		NOT_TEST,
		PASS,
		FAILED,
		// For Failed cases
		RE_TEST,
		NO_RE_TEST,
		NEXT,
		INTERRUPT,
	};
	
	public enum ID {
		EMPTY, 
		LCD_MIRERGB, 
		LCD_BLACK, 
		LCD_CHECKER, 
		LCD_GREYCHART,
		LCD_LEVEL,
		LCD_WHITE, 
		LCD_MENU, 
		LCD_MACBETH, 
		KEYPAD,
		KEYPADAI,
		KEYPAD_INIT, 
		CAMERA_INIT,
                CAMERA_OIS,
                CAMERA_SHAKE_OIS,
		CAMERA_IMG,
		CAMERA_IMG_FRONT,
		CAMERA2,
		CAMERA_ZOOM, 
		CAMERA_LED,
        MAIN2_Camera,
        WIDE_CAMERA,
		CHARGER_LED,
		BACKLIGHT, 
		KBD_BACKLIGHT, 
		SKBD_BACKLIGHT, 
		VIB, 
		SLIDE,
		CMMB,
		NFC,
                //PR483965-yancan-zhu-20130712 begin
                SMARTCOVER,
                //PR483965-yancan-zhu-20130712 end
                STYLUS,
                TPCALIBRATION,
                //PR547129-yancan-zhu-20131030 begin
                TP0,
                //PR547129-yancan-zhu-20131030 end
                TOUCHPANEL,
		TP1,
		TP2,
		POINT,
		SIM, 
		CHARGER_PRES, 
		CHARGER_MISS, 
		HEADSET_IN, 
		HEADSET_LEFT, 
		HEADSET_RIGHT, 
		HEADSET_OUT, 
		MELODY, 
		MIC,
		FM,
		BT, 
		WIFI,
		MEMORYCARD, 
		MEMORYCARD_RW, 
		USB,
		MHL,
		MISC,
		TEMPBAT,
		TRACABILITY,
		COMPASS,
		GSENSOR,
		LIGHTSENSOR,
		GPS,
		TS_CALIBRATION,
		EMERGENCY_CALL,
		HEADSET,
		NWSETTING,
		FMRADIO,
		ALSPS,
		NFC_ACTIVE,
    	NFC_PASSIVE,
	        CALL,
		INFRARED_RAY,
		GYROSCOPE,//PR661437-yinbin-zhang-20140520
		HIFI,//PR661437-yinbin-zhang-20140520
		SMARTPA,
		CAMERA_DECO,
		FACTORYRESET,
		LCD_ALLCOLOR,
		CARR_SIGNAL,
		USB_Charger,
		MAX_ITEMS,
		TPLOCK,//added by nanbing.zou for A5A INFINI at 2017-11-23 begin
		LCDBACKLIGHT,
		OTG,
		GYRO,
		GSENSORCB,
		FINGER,//added by nanbing.zou for A5A INFINI at 2017-11-23 end
		DTV,
		IR,
		WIFI5G,
		WIFI2_4G,
		CAPACITIVE,
		BUTTONBACKLIGHT,
		KEYBACKLIGHT,
		HALL,
		FINGERENROLL,
		KEYBOARD,
		SUBCAMERA,
		DUALCAMERA,
		PDAF,
		HDCP,
		FRONT_LED,
		CHARGER_RGBLED,
		BBCAMERA_LED,
		BBKEYBOARD
	};
	

	
	protected Test(ID id, String name){
		this.mId = id.ordinal();
		this.mName = name;
	}
	
	protected Test(ID id, String name, Boolean updateFlag){
		this.mId = id.ordinal();
		this.mName = name;
		this.updateFlag = updateFlag;
		
	}
	
	protected Test(ID id, String name, Boolean updateFlag, long tout){
		this.mId = id.ordinal();
		this.mName = name;
		this.updateFlag = updateFlag;
		this.tout = tout;
		
	}

	public int getId(){
		return mId;
	}
	
	public String toString(){
		return mName;
	}
	
	public void setContext(Activity context){
		this.mContext = context;
	}
	
	//Add by Jianke.Zhang 2015/01/15
	public Activity getContext(){
		return this.mContext;
	}
	//End
	
	public View getLayout(){
		return mLayout;
	}
	
	public long getTimeOut(){
		return tout;
	}
	
	public abstract void setUp() ;
	
	public abstract void initView() ;
	
	public abstract void finish() ;

	public abstract String getmContextTag();
	
	public abstract void setmContextTag() ;
	
    public void newIntent(Intent i) {};
	
	public void onResume() {};
	public void onStop() {};
	public void updateView(){
		// this method should be over write if needed;
	};
	public abstract void destroy();
	public boolean onKeyTouch(KeyEvent event) {
		// TODO Auto-generated method stub
		return true;
	}
	
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		return true;
	}
	
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		
		return true;
	}
	
	public void timeout(){
		// this method should be over write if needed;
	}
	

	
	
	public void pass() {
		mContext.setResult(Test.RESULT.PASS.ordinal());
		
		String tag = getmContextTag();
		Tool.toolLog(TAG + " ---- 8989 " + tag);
		
		ManuFinishThread tFinish = new ManuFinishThread(0x01);
		tFinish.start();
		try {
			tFinish.join();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		Msg.exitWithSuccessTest(mContext, tag, 10, false,"Pass");
		
		if(AllMainActivity.mainAllTest && !AllMainActivity.manufileFlag){
			Msg.WriteModelResult(mContext, AllMainActivity.all_items_file_text, "PASS");
		}else {
			Msg.WriteModelResult(mContext, manuRunActivity.filetxt, "PASS");
		}
		//End
	}

	public void failed() {
		mContext.setResult(Test.RESULT.FAILED.ordinal());
		
		String tag = getmContextTag();
		Tool.toolLog(TAG + " ---- 898910 " + tag);
		
		
		ManuFinishThread tFinish = new ManuFinishThread(0x02);
//		tFinish.run();
		tFinish.start();
		try {
			tFinish.join();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Msg.exitWithException(mContext,tag,10,false,"Fail");
		
		if(AllMainActivity.mainAllTest && !AllMainActivity.manufileFlag){
			Msg.WriteModelResult(mContext, AllMainActivity.all_items_file_text, "FAIL");
		}else {
			Msg.WriteModelResult(mContext, manuRunActivity.filetxt, "FAIL");
		}
		
	}
	
	protected OnClickListener pass_listener = new OnClickListener() {
		@Override	
		public void onClick(View v) {
			if (isFastDoubleClick()) {
				Tool.toolLog("pass isFastDoubleClick");
				return;
			}
			
			if(bt_left!=null && bt_right!=null ){
				bt_left.setEnabled(false);
				bt_right.setEnabled(false);
			}

			pass();
			
		}	
	};
	
	protected OnClickListener failed_listener = new OnClickListener() {
		@Override	
		public void onClick(View v) {
			if (isFastDoubleClick()) {
				Tool.toolLog("fail isFastDoubleClick");
				return;
			}
			
			if(bt_left!=null && bt_right!=null ){
				bt_left.setEnabled(false);
				bt_right.setEnabled(false);
			}
			
			failed();
		}	
	};
	

	public void handleMessage(Message msg){
		
	}
	
	public Handler mHandler = new Handler(){
		@Override
		public void dispatchMessage(Message msg) {
			// TODO Auto-generated method stub
			
			handleMessage(msg);
			super.dispatchMessage(msg);
		}
	};

	private long lastClickTime;  
	public boolean isFastDoubleClick() {
		long time = System.currentTimeMillis();
		long timeD = time - lastClickTime;  
		if ( 0 < timeD && timeD < 500) {
			return true;
		}     
		lastClickTime = time;
		return false;
	}

	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		
	}
	protected void hideNavigationBar() {

		final View mDecorView = mContext.getWindow().getDecorView();
		final int flags = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
				| View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
		mDecorView.setSystemUiVisibility(flags);
		mDecorView.setOnSystemUiVisibilityChangeListener(new View.OnSystemUiVisibilityChangeListener() {
			@Override
			public void onSystemUiVisibilityChange(int visibility) {
				if ((visibility & View.SYSTEM_UI_FLAG_HIDE_NAVIGATION) == 0) {
					mDecorView.setSystemUiVisibility(flags);
				}
			}
		});
	}
}
