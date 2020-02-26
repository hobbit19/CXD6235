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
import com.tcl.autotest.R;
import com.tcl.autotest.tool.HomeKeyLocker;
import com.tcl.autotest.tool.Tool;
import com.tcl.autotest.utils.Test;

public class BBKeyBoardTest extends Test {

	private static final String TAG = "BBKeyBoardTest";
	private int testKey = 0;
	private int num = 0x01;

	FrameLayout fl = null;
	LinearLayout keys = null;
	IntentFilter intentfilter;

	KeyguardLock lock;

	WindowManager wm;
	public BBKeyBoardTest(ID id, String name) {
		super(id, name);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void setUp() {
		// TODO Auto-generated method stub
		Tool.toolLog(TAG + "_start_test");

		fl = new FrameLayout(mContext);
		mLayout = (LinearLayout) View.inflate(mContext,
				R.layout.manu_base_screen, null);
		keys = (LinearLayout) View.inflate(mContext, R.layout.bbkeypad, null);

		wm = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);

	}

	@Override
	public void initView() {
		// TODO Auto-generated method stub

		bt_left = (Button) mLayout.findViewById(R.id.bt_left);
		bt_right = (Button) mLayout.findViewById(R.id.bt_right);
		text_top_zone = (TextView) mLayout.findViewById(R.id.text_top_zone);

		text_top_zone.setText(mName);
		bt_left.setText(R.string.pass);
		bt_right.setText(R.string.fail);

		bt_left.setOnClickListener(pass_listener);
		bt_right.setOnClickListener(failed_listener);
		bt_left.setEnabled(false);

		fl.addView(keys);
		fl.addView(mLayout);

		mContext.setContentView(fl);
	}

	@Override
	public void finish() {
		// TODO Auto-generated method stub
		// Add by Jianke.Zhang 01/21
		// Tool.toolLog("finish KeyPadTest");
//		mContext.unregisterReceiver(homereceiver);

//		mHomeKeyLocker.unlock();
//		mHomeKeyLocker = null;
	}


	@Override
	public boolean onKeyTouch(KeyEvent event) {
		// TODO Auto-generated method stub
		Message msg = new Message();
		int keyCode = event.getKeyCode();
		Tool.toolLog(TAG + " " + mName + " " + String.valueOf(keyCode));
		switch (keyCode) {
		case KeyEvent.KEYCODE_VOLUME_UP:
			msg.what = KeyEvent.KEYCODE_VOLUME_UP;
			mHandler.sendMessage(msg);
			break;
		case KeyEvent.KEYCODE_VOLUME_DOWN:
			msg.what = KeyEvent.KEYCODE_VOLUME_DOWN;
			mHandler.sendMessage(msg);
			break;
		case KeyEvent.KEYCODE_BACK:
			msg.what = KeyEvent.KEYCODE_BACK;
			mHandler.sendMessage(msg);
			break;
		case KeyEvent.KEYCODE_HOME:
			msg.what = KeyEvent.KEYCODE_HOME;
			mHandler.sendMessage(msg);
			break;
		/*
		 * case KeyEvent.KEYCODE_RECENTAPP: msg.what =
		 * KeyEvent.KEYCODE_RECENTAPP; mHandler.sendMessage(msg); break;
		 */
		// Task key should be menu key in some project
		case KeyEvent.KEYCODE_MENU:
			msg.what = KeyEvent.KEYCODE_MENU;
			mHandler.sendMessage(msg);
			break;
		default:
			msg.what = keyCode;
			mHandler.sendMessage(msg);
			break;

		}
		return true;
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		Tool.toolLog(TAG + " onKeyDown 88888888:"+keyCode);
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
			Button bt = null;
			Tool.toolLog(TAG + " msg.what " + msg.what);
			switch (msg.what) {
				case KeyEvent.KEYCODE_VOLUME_UP:
					bt = (Button) keys.findViewById(R.id.bt_vol_up);
					break;
				case KeyEvent.KEYCODE_VOLUME_DOWN:
					bt = (Button) keys.findViewById(R.id.bt_vol_down);
					break;
				case KeyEvent.KEYCODE_BACK:
					bt = (Button) keys.findViewById(R.id.bt_back);
					break;
				case KeyEvent.KEYCODE_HOME:
					bt = (Button) keys.findViewById(R.id.bt_home);
					//lock.disableKeyguard();
					break;
				case KeyEvent.KEYCODE_MENU:
					bt = (Button) keys.findViewById(R.id.bt_task);
					//lock.disableKeyguard();
					break;
				case KeyEvent.KEYCODE_Q:
					bt = (Button) keys.findViewById(R.id.q);
					//lock.disableKeyguard();
					break;
				case KeyEvent.KEYCODE_W:
					bt = (Button) keys.findViewById(R.id.w);
					break;
				case KeyEvent.KEYCODE_E:
					bt = (Button) keys.findViewById(R.id.e);
					break;
				case KeyEvent.KEYCODE_R:
					bt = (Button) keys.findViewById(R.id.r);
					break;
				case KeyEvent.KEYCODE_T:
					bt = (Button) keys.findViewById(R.id.t);
					break;
				case KeyEvent.KEYCODE_Y:
					bt = (Button) keys.findViewById(R.id.y);
					break;
				case KeyEvent.KEYCODE_U:
					bt = (Button) keys.findViewById(R.id.u);
					break;
				case KeyEvent.KEYCODE_I:
					bt = (Button) keys.findViewById(R.id.i);
					break;
				case KeyEvent.KEYCODE_O:
					bt = (Button) keys.findViewById(R.id.o);
					break;
				case KeyEvent.KEYCODE_P:
					bt = (Button) keys.findViewById(R.id.p);
					break;
				case KeyEvent.KEYCODE_A:
					bt = (Button) keys.findViewById(R.id.a);
					break;
				case KeyEvent.KEYCODE_S:
					bt = (Button) keys.findViewById(R.id.s);
					break;
				case KeyEvent.KEYCODE_D:
					bt = (Button) keys.findViewById(R.id.d);
					break;
				case KeyEvent.KEYCODE_F:
					bt = (Button) keys.findViewById(R.id.f);
					break;
				case KeyEvent.KEYCODE_G:
					bt = (Button) keys.findViewById(R.id.g);
					break;
				case KeyEvent.KEYCODE_H:
					bt = (Button) keys.findViewById(R.id.h);
					break;
				case KeyEvent.KEYCODE_J:
					bt = (Button) keys.findViewById(R.id.j);
					break;
				case KeyEvent.KEYCODE_K:
					bt = (Button) keys.findViewById(R.id.k);
					break;
				case KeyEvent.KEYCODE_L:
					bt = (Button) keys.findViewById(R.id.l);
					break;
				case KeyEvent.KEYCODE_Z:
					bt = (Button) keys.findViewById(R.id.z);
					break;
				case KeyEvent.KEYCODE_X:
					bt = (Button) keys.findViewById(R.id.x);
					break;
				case KeyEvent.KEYCODE_C:
					bt = (Button) keys.findViewById(R.id.c);
					break;
				case KeyEvent.KEYCODE_V:
					bt = (Button) keys.findViewById(R.id.v);
					break;
				case KeyEvent.KEYCODE_B:
					bt = (Button) keys.findViewById(R.id.b);
					break;
				case KeyEvent.KEYCODE_N:
					bt = (Button) keys.findViewById(R.id.n);
					break;
				case KeyEvent.KEYCODE_M:
					bt = (Button) keys.findViewById(R.id.m);
					break;

			}

			if(bt != null && bt.isEnabled()){
				testKey++;
			}

			 Tool.toolLog(TAG+"testKey " + testKey);
			// Log.d("MMITEST", String.valueOf(testKey));
			// When send a key the testKeyNu
			if (testKey > 15) {
				// Tool.toolLog(TAG + " key finish");
				bt_left.setEnabled(true);
				mContext.setResult(RESULT.PASS.ordinal());
			}
			if(bt != null){
				bt.setEnabled(false);
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
		
	}

}
