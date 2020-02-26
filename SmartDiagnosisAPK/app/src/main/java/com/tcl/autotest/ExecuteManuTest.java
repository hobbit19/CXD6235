package com.tcl.autotest;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;

import com.tcl.autotest.tool.Tool;
import com.tcl.autotest.utils.Test;
import com.tcl.autotest.utils.TestList;


public class ExecuteManuTest extends Activity {

	private final String TAG = "ExecuteManuTest";
	private Test currentTest = null;
	private int position;
	
	public static int temppositon;
	
	private static final int FLAG_HOMEKEY_DISPATCHED = 0x80000000;
	private static final int FLAG_JRD_RECENTAPPKEY_DISPATCHED = 0x60000000;


	final Runnable mUpdateView = new Runnable() {
		public void run() {
			currentTest.updateView();
		}
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE); 
		super.onCreate(savedInstanceState);
//		int mask = WindowManager.LayoutParams.FLAG_FULLSCREEN
//				| WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON | FLAG_HOMEKEY_DISPATCHED | FLAG_JRD_RECENTAPPKEY_DISPATCHED;
//		getWindow().setFlags(mask, mask);

//		Tool.toolLog(TAG + " onCreate ExecuteManuTest 2");
/*		Window	window = getWindow();  
		        WindowManager.LayoutParams params = window.getAttributes();  
		      params.systemUiVisibility = View.SYSTEM_UI_FLAG_LOW_PROFILE;  
	        window.setAttributes(params); */ 
		Intent intent = getIntent();
		position = intent.getExtras().getInt("position", 0);
//		Tool.toolLog("position " + position);
		currentTest = TestList.getTestList()[position];
		
		temppositon = position;
		
//		Tool.toolLog(TAG + " currentTest " + currentTest);
//		Tool.toolLog(TAG + " name ... "  + currentTest.getClass().getName());
//		Tool.toolLog(TAG + " this " + this);
		currentTest.setContext(this);
		init();
	}
	
    @Override
	protected void onNewIntent(Intent intent) {
		// TODO Auto-generated method stub
		super.onNewIntent(intent);
//		Tool.toolLog(TAG + " onNewIntent ExecuteManuTest 3");
		currentTest.newIntent(intent);
	}
    
	@Override
	protected void onStart() {
		super.onStart();
//		Tool.toolLog(TAG + " onStart ExecuteManuTest 4");
		
	}

	@Override
	public void onResume() {
		super.onResume();
//		Tool.toolLog(TAG + " onResume ExecuteManuTest 6");
        currentTest.onResume();
	}

	@Override
	public void onPause() {
//		Tool.toolLog(TAG + " onPause ExecuteManuTest 5");
		//Jianke.Zhang 02/10
		//currentTest.finish();
		super.onPause();
	}

	@Override
	public void onStop() {
		super.onStop();

	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		currentTest.destroy();
//		Tool.toolLog(TAG + " onDestroy ExecuteManuTest 8");
//		manuRunActivity.Over_test = false;
	}

	private void init() {
		currentTest.setUp();
		currentTest.initView();
		if (currentTest.updateFlag) {
			Tool.toolLog(TAG + " updateFlag");
			runOnUiThread(mUpdateView);
		}
	}

	
	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		// TODO Auto-generated method stub
//		Tool.toolLog(TAG + " dispatchTouchEvent 22222222222");
		return super.dispatchTouchEvent(ev);
	}

	@Override
	public void onAttachedToWindow() {
		// TODO Auto-generated method stub
//		Tool.toolLog(TAG + " onAttachedToWindow 333333333");
//		this.getWindow().setType(WindowManager.LayoutParams.TYPE_KEYGUARD);  //,TYPE_KEYGUARD_DIALOG
//		KeyguardManager keyguardManager = (KeyguardManager)getSystemService(KEYGUARD_SERVICE);
//		KeyguardLock lock = keyguardManager.newKeyguardLock(KEYGUARD_SERVICE);
//		lock.disableKeyguard();
		super.onAttachedToWindow();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
//		Tool.toolLog(TAG + " onKeyDown 1111111111");
		if(keyCode == KeyEvent.KEYCODE_HOME){
			Tool.toolLog(TAG + " onKeyDown KEYCODE_HOME");
			return true;
		}
		return currentTest.onKeyDown(keyCode, event);
	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		//Tool.toolLog(TAG + " onKeyUp 555555555");
		return currentTest.onKeyUp(keyCode, event);
	}

//	@Override
//	void launchHomeFromHotKey(){
//		
//	}
	
	
	@Override
	public void onBackPressed() {
	
		Tool.toolLog(TAG + " onBackPressed");
	}

	public boolean dispatchKeyEvent(KeyEvent event) {
		// in case we want to filter keys, return true to indicate the event was
		// consumed
		int keycode = event.getKeyCode();
		Tool.toolLog(TAG + " keycode ... " + keycode);
		if (Test.ID.KEYPAD.ordinal() == currentTest.getId() || Test.ID.TPLOCK.ordinal() == currentTest.getId()
				|| Test.ID.KEYBOARD.ordinal() == currentTest.getId() || Test.ID.BBKEYBOARD.ordinal() == currentTest.getId()
				|| Test.ID.HALL.ordinal()== currentTest.getId()) {
//			Tool.toolLog(TAG + " keycode 22222 ... " + keycode);
			return currentTest.onKeyTouch(event);
		}

//		if (Test.flag == Test.MODE.AUTO
//				&& KeyEvent.ACTION_UP == event.getAction()) {
//			if (event.getEventTime() - event.getDownTime() > 500) {
//				switch (keycode) {
//				case KeyEvent.KEYCODE_BACK:
//					setResult(Test.RESULT.INTERRUPT.ordinal());
//					finish();
//					break;
//				case KeyEvent.KEYCODE_ENDCALL:
//					break;
//				case KeyEvent.KEYCODE_HOME:
//					break;
//				default:
//					break;
//				}
//				return true;
//			} else {
//				;
//			}
//		} else if (Test.flag == Test.MODE.MAUI
//				&& KeyEvent.ACTION_UP == event.getAction()) {
//			switch (keycode) {
//			case KeyEvent.KEYCODE_BACK:
//				finish();
//				break;
//			case KeyEvent.KEYCODE_ENDCALL:
//				break;
//			case KeyEvent.KEYCODE_HOME:
//				break;
//			default:
//				break;
//			}
//		}
		return true;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		currentTest.onActivityResult(requestCode, resultCode, data);
	}
}
