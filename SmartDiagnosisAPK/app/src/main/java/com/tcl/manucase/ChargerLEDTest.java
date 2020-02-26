package com.tcl.manucase;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

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

import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.LinearLayout.LayoutParams;

public class ChargerLEDTest extends Test implements DownTimeCallBack{

	private static final String TAG = "ChargerLEDTest";
	private static final String CHARGERLEDPATH = "/sys/class/leds/red/brightness";
	private static final String CHARGERLEDPATH2 = "/sys/class/leds/green/brightness";
	private final long TurnOn = 255;
	private final long TurnOff = 0;
	BufferedReader ChargerLedBuffer;
	String ChargerLedValue = null;
	File ChargerLedPath = new File(CHARGERLEDPATH);
	String text = "Please check charger LED";
	
	// PR661412-yinbin-zhang-20140515 begin
	private FrameLayout mFrameLayout;
	private LinearLayout mLinearLayout;
	private TestCountDownTimer mTestCountDownTimer;
	private Button retestButton;
	private boolean flag = false;

	// PR661412-yinbin-zhang-20140515 end

	public ChargerLEDTest(ID id, String name) {
		super(id, name);
	}

	// PR661412-yinbin-zhang-20140515 begin

	private Handler mHandler = new Handler();
	private Runnable mRunnable = new Runnable() {
		public void run() {
			testprogress();
		}
	};

	public void testprogress() {
		if (!flag) {
			try {
				//Just read the current status 07/07
//				Tool.toolLog(TAG + " Open charger LED");
//				FileOutputStream outStream = new FileOutputStream(
//						ChargerLedPath);
//				outStream.write(((Long) TurnOn).toString().getBytes());
////				Tool.toolLog(TAG + " write the value 255 successfully");
//				outStream.close();
				
				FileInputStream inStreamEND = new FileInputStream(ChargerLedPath);
				ChargerLedBuffer = new BufferedReader(new InputStreamReader(
						inStreamEND));
				ChargerLedValue = ChargerLedBuffer.readLine();
				inStreamEND.close();
				Tool.toolLog(TAG + " close ChargerLedValue = " + ChargerLedValue);
				
			} catch (Exception e) {
				e.printStackTrace();
				Tool.toolLog(TAG + " MMI Test " + "can't open ChargerLED "
						+ e.toString());
				//"Have no right to test!" + "\n" + 
				text_cen_zone.setText("Check whether the charger led is bright!");
				Tool.toolLog(TAG + " index 888 -> " + ExecuteTest.temppositon);
				
				flag = true;
				bt_left.setEnabled(true);
				return;
			}

			try {
				Thread.sleep(1000);
			} catch (Exception e) {
				// TODO: handle exception
				e.printStackTrace();
			}
			
			Message msg = new Message();
			if(Integer.parseInt(ChargerLedValue) > 0){
				msg.what = 0x01;
			}else{
				msg.what = 0x02;
			}
			mHandler2.sendMessage(msg);
		}

	}


	@Override
	public void setUp() {
		// TODO Auto-generated method stub
		Tool.toolLog(TAG + "_start_test");
		//First judge the file if exist 07/07
		if(!ChargerLedPath.exists()){
			ChargerLedPath = new File(CHARGERLEDPATH2);
		}
		mTestCountDownTimer = new TestCountDownTimer(30*SECOND, SECOND, this);
		mTestCountDownTimer.start();
		// PR661412-yinbin-zhang-20140515 begin
		/*
		 * try{ FileInputStream inStream = new FileInputStream(ChargerLedPath);
		 * ChargerLedBuffer = new BufferedReader(new
		 * InputStreamReader(inStream)); ChargerLedValue =
		 * ChargerLedBuffer.readLine(); inStream.close();
		 * 
		 * { Log.d(TAG,"Open charger LED"); FileOutputStream outStream = new
		 * FileOutputStream(ChargerLedPath);
		 * outStream.write(((Long)TurnOn).toString().getBytes());
		 * Log.d(TAG,"write the value 255 successfully"); outStream.close();
		 * 
		 * FileInputStream inStreamCharger = new
		 * FileInputStream(ChargerLedPath); ChargerLedBuffer = new
		 * BufferedReader(new InputStreamReader(inStreamCharger));
		 * ChargerLedValue = ChargerLedBuffer.readLine();
		 * inStreamCharger.close(); } } catch (FileNotFoundException e) {
		 * e.printStackTrace(); } catch (IOException e) { e.printStackTrace(); }
		 * catch (Exception e){ Log.e("MMI Test", "can't open ChargerLED ");
		 * return; } Log.d(TAG,"open ChargerLedValue="+ChargerLedValue);
		 * 
		 * 
		 * if(((Long)TurnOn).toString().equals(ChargerLedValue)) { text =
		 * "Charger LED turning on"; Log.d(TAG,"Open charger LED successfully");
		 * } else { text = "Charger LED turning off";
		 * Log.d(TAG,"Charger LED turning off"); }
		 */
		// PR661412-yinbin-zhang-20140515 end
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

		mHandler.removeCallbacks(mRunnable);
		mHandler.postDelayed(mRunnable, 500);
	}

	
	
	@Override
	public void destroy() {
		// TODO Auto-generated method stub
		if(mTestCountDownTimer != null){
			mTestCountDownTimer.cancel();
			mTestCountDownTimer = null;
		}
	}

	Handler mHandler2 = new Handler(){

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			if(msg.what == 0x01){
				bt_left.setEnabled(true);
			}else if(msg.what == 0x02){
				flag = true;
			}
			text_cen_zone.setText("Check whether the charger led is bright!");
			bt_left.setEnabled(true);
		}
		
	};

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
