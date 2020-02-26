package com.tcl.autotest;


import com.tcl.autotest.tool.Tool;
import com.tcl.autotest.utils.Test;
import com.tcl.autotest.utils.TestList;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.WindowManager;


public class ExecuteTest extends Activity {

	private final String TAG = "ExecuteTest";
	private Test currentTest = null;
	private int position;
	private static final int FLAG_HOMEKEY_DISPATCHED = 0x80000000;
	private static final int FLAG_JRD_RECENTAPPKEY_DISPATCHED = 0x60000000;
	public static int temppositon;
	
	final Runnable mUpdateView = new Runnable() {
		public void run() {
			currentTest.updateView();
		}
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		int mask = WindowManager.LayoutParams.FLAG_FULLSCREEN
				//| WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;
				| WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON | FLAG_HOMEKEY_DISPATCHED | FLAG_JRD_RECENTAPPKEY_DISPATCHED;
		getWindow().setFlags(mask, mask);

//		Tool.toolLog("onCreate ExecuteTest 1");
		
		Intent intent = getIntent();
		position = intent.getExtras().getInt("position", 0);
//		Tool.toolLog("position " + position);
//		currentTest = TestList.getTestList()[position];
		Log.i("nbnbnb", "TestList"+TestList.getAutoTestList().length+"position:"+position);
		currentTest = TestList.getAutoTestList()[position];
		temppositon = position;
//		Tool.toolLog(TAG + " currentTest -> " + currentTest);
//		Tool.toolLog(TAG + " this -> " + this.getTaskId());
		currentTest.setContext(this);
		init();// ADD XIBIN
	}
	
    @Override
	protected void onNewIntent(Intent intent) {
		// TODO Auto-generated method stub
		super.onNewIntent(intent);
//		Tool.toolLog(TAG + " onNewIntent ExecuteTest 3");
		currentTest.newIntent(intent);
	}
    
	@Override
	protected void onStart() {
		super.onStart();
//		Tool.toolLog(TAG + " onStart ExecuteTest 4");
	}

	@Override
	public void onResume() {
		super.onResume();
//		Tool.toolLog(TAG + " onResume ExecuteTest 6");
        currentTest.onResume();
	}

	@Override
	public void onPause() {
//		Tool.toolLog(TAG + " onPause ExecuteTest 5");
		//currentTest.finish();  ADD XIBIN
		super.onPause();
	}

	@Override
	public void onStop() {
		super.onStop();
//		Tool.toolLog(TAG + " onStop ExecuteTest 7");
		//Add by Jianke.Zhang 2015/01/15 setContext
		Activity mTempContext = currentTest.getContext();
//		if(mTempContext != null){
//			Tool.toolLog(TAG + " mTempContext " + mTempContext);
//			mTempContext.finish();
//		}
		//End
	currentTest.onStop();
	}

	@Override
	public void onDestroy() {
	    currentTest.destroy();
		super.onDestroy();
//		Tool.toolLog(TAG + " onDestroy ExecuteTest 8");
	}

	private void init() {
		currentTest.setUp();
		currentTest.initView();
		if (currentTest.updateFlag) {
			Tool.toolLog(TAG + " init mUpdateView");
			runOnUiThread(mUpdateView);
		}
	}

//	@Override
//	public boolean dispatchKeyEvent(KeyEvent event) {
//		// in case we want to filter keys, return true to indicate the event was
//		// consumed
//		int keycode = event.getKeyCode();
//		if (Test.ID.KEYPAD.ordinal() == currentTest.getId()) {
//			return currentTest.onKeyTouch(event);
//		}
//
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
//		return true;
//	}
	
	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		Tool.toolLog(TAG + " onBackPressed ...");
	}
	
	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
		// TODO Auto-generated method stub
		
		int keycode = event.getKeyCode();
		switch (keycode){
			case KeyEvent.KEYCODE_HOME:
				break;
			default:
				return super.dispatchKeyEvent(event);
		}
		
		return true;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		currentTest.onActivityResult(requestCode, resultCode, data);
	}
}
