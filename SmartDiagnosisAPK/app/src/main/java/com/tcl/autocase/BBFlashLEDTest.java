package com.tcl.autocase;

import android.hardware.Camera;
import android.os.Handler;
import android.os.Message;
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

public class BBFlashLEDTest extends Test implements DownTimeCallBack{
	private static String TAG = "BBFlashLEDTest";
	String text = "";
	private Camera mCamera;
	private Camera.Parameters mParameters;
	private boolean successFlag = true;
	public static boolean sysFlash = false;

	TestCountDownTimer testCountDownTimer = null;

	private final String CAMERA_FLASH_TEST_MODE = "test-dual-led-value";
	private Handler mHandler = new Handler(){
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			setParameters(SWITCH_OFF, MIN, MIN);
			switch (msg.what){
				case 0:
					setParameters(SWITCH_ON, MIN, MAX);
					startTestbbb100(1);
					break;
				case 1:
					setParameters(SWITCH_ON, MAX, MIN);
					startTestbbb100(2);
					break;
				case 2:
					onSucess();
				default:
					break;
			}
		}
	};
	private enum CAMERALED {
		BACK_WARM_LED_TORCH, BACK_COLD_LED_TORCH, BACK_WARM_LED_FLASH, BACK_COLD_LED_FLASH,
	}

	private CAMERALED mLED = CAMERALED.BACK_WARM_LED_TORCH;
	public static final int SWITCH_OFF = 0;
	public static final int SWITCH_ON = 1;
	public static final int SWITCH_ON_FLASH = 2;
	public static final int MIN = 0;
	public static final int MAX = 255;

	public BBFlashLEDTest(ID id, String name) {
		super(id, name);
		// TODO Auto-generated constructor stub
	}

	private void startTest(){
		mHandler.removeCallbacksAndMessages(null);
		if (mLED == CAMERALED.BACK_WARM_LED_TORCH) {
			setParameters(SWITCH_OFF, MIN, MIN);
			mHandler.postDelayed(openRun_cold_torch, 1000);
		} else if (mLED == CAMERALED.BACK_COLD_LED_TORCH) {
			setParameters(SWITCH_OFF, MIN, MIN);
			mHandler.postDelayed(openRun_warm_flash, 500);

		} else if (mLED == CAMERALED.BACK_WARM_LED_FLASH) {
			setParameters(SWITCH_OFF, MIN, MIN);
			mHandler.postDelayed(openRun_cold_flash, 500);

		}
	}



	private void startTestbbb100(int index){
		mHandler.removeCallbacksAndMessages(null);
		switch (index){
			case 0:
				mHandler.sendEmptyMessageDelayed(0,1000);
				break;
			case 1:
				mHandler.sendEmptyMessageDelayed(1,1000);
				break;
			case 2:
				mHandler.sendEmptyMessageDelayed(2,1000);
				break;
		}
	}


	@Override
	public void setUp() {
		// TODO Auto-generated method stub
		Tool.toolLog(TAG + "_start_test");
		testCountDownTimer = new TestCountDownTimer(SECOND*20, SECOND, this);
		testCountDownTimer.start();

		try {
			mCamera = Camera.open();
			// Tool.toolLog(TAG + " mCamera " + mCamera);
		} catch (Exception e) {
			Tool.toolLog(TAG + " can't open camera ");
			text = "Flash LED open failed";
			successFlag = false;
		}

		if (mCamera != null) {
			if(AllMainActivity.deviceName.contains("bbb100")){
				startTestbbb100(0);
			}else{
				startTest();
			}
//

			Log.i("qinhao", "shanguangdeng:::::"+mCamera.getParameters().getFlashMode());
			text = "turning on";
//			new FinishThread(0x01, ExecuteTest.temppositon).start();
		} else {
			mContext.setResult(RESULT.FAILED.ordinal());
			Tool.toolLog(TAG + " index 8884 -> " + ExecuteTest.temppositon);
			int double_test;
			if (AllMainActivity.mainAllTest) {
				double_test = AllMainActivity.double_check_for_auto[ExecuteTest.temppositon]++;
				// AllMainActivity.mainAllTest = false;
			} else {
				double_test = autoRunActivity.double_check[ExecuteTest.temppositon]++;
			}
			Tool.toolLog(TAG + " double_test 9994-> " + double_test);
			FinishThread tFinishThread = new FinishThread(0x02, ExecuteTest.temppositon);
			tFinishThread.start();
			try {
				tFinishThread.join();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if (double_test == 1) {
				
				Msg.exitWithException(mContext, TAG, 20, true, "Pass");
				if (AllMainActivity.mainAllTest
						&& !AllMainActivity.autofileFlag) {
					Msg.WriteModelResult(mContext,
							AllMainActivity.all_items_file_text, "FAIL");
				} else {
					Msg.WriteModelResult(mContext,
							autoRunActivity.auto_file_text, "FAIL");
				}

			} else {
				Msg.exitWithException(mContext, TAG, 20, true, "Fail");
			}

		}

	}

	@Override
	public void initView() {
		// TODO Auto-generated method stub
		mLayout = (LinearLayout) View.inflate(mContext, R.layout.base_screen,
				null);

		// bt_left = (Button) mLayout.findViewById(R.id.bt_left);
		// bt_right = (Button) mLayout.findViewById(R.id.bt_right);
		text_top_zone = (TextView) mLayout.findViewById(R.id.text_top_zone);
		text_cen_zone = (TextView) mLayout.findViewById(R.id.text_cen_zone);

		text_top_zone.setText(mName);
		text_cen_zone.setText(text);
		// PR661395-yinbin-zhang-20140516 begin
		/*
		 * bt_left.setText(R.string.fail); bt_right.setText(R.string.pass);
		 * 
		 * bt_left.setOnClickListener(failed_listener);
		 * bt_right.setOnClickListener(pass_listener);
		 */
		// bt_left.setText(R.string.pass);
		// bt_right.setText(R.string.fail);

		// bt_left.setOnClickListener(pass_listener);
		// bt_right.setOnClickListener(failed_listener);
		// PR661395-yinbin-zhang-20140516 end

		mContext.setContentView(mLayout);
		// Add by Jianke.Zhang 01/23
		new Thread() {
			public void run() {
				while (true) {
					if (sysFlash) {
						sysFlash = false;
						break;
					}
					Tool.sleepTimes(10);
				}
				if (mCamera != null) {
					Msg.exitWithSuccessTest(mContext, TAG, 20, true, "Pass");
				}
			}
		}.start();
		// End
	}

	@Override
	public void finish() {
		// TODO Auto-generated method stub
		if (mCamera != null) {
			// Tool.toolLog("back finish");
			mCamera.release();

			// Add by Jianke.Zhang 01/27

			// End
		}
	}

	private Handler fhandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			if (msg.what == 0x10) {
				Tool.toolLog(TAG + " 5555555555");
			}
		}
	};

	public String getmContextTag() {
		// TODO Auto-generated method stub
		return TAG;
	}

	public void setmContextTag() {
		// TODO Auto-generated method stub
	}

	@Override
	public void destroy() {
		// TODO Auto-generated method stub
		if (mCamera != null) {
			// Tool.toolLog("back finish");
			mParameters = mCamera.getParameters();
			mParameters.set("flash-mode", "off");
			mCamera.setParameters(mParameters);
			text = "turning off";
			mCamera.release();
			mCamera = null;
			if (successFlag) {

				if (AllMainActivity.mainAllTest
						&& !AllMainActivity.autofileFlag) {
					Msg.WriteModelResult(mContext,
							AllMainActivity.all_items_file_text, "PASS");
				} else {
					Msg.WriteModelResult(mContext,
							autoRunActivity.auto_file_text, "PASS");
				}
			}else{
				if (AllMainActivity.mainAllTest
						&& !AllMainActivity.autofileFlag) {
					Msg.WriteModelResult(mContext,
							AllMainActivity.all_items_file_text, "FAIL");
				} else {
					Msg.WriteModelResult(mContext,
							autoRunActivity.auto_file_text, "FAIL");
				}
			}
		}
		
		if(testCountDownTimer != null){
			testCountDownTimer.cancel();
			testCountDownTimer = null;
		}
	}

	@Override
	public void onFinish() {
		// TODO Auto-generated method stub
		successFlag = false;
		FinishThread tFinishThread = new FinishThread(0x02, ExecuteTest.temppositon);
		tFinishThread.start();
		try {
			tFinishThread.join();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
			
		Msg.exitWithException(mContext, TAG, 20, true, "Pass");
			
	}

	@Override
	public void onTick() {
		// TODO Auto-generated method stub
		
	}

	private Runnable openRun_cold_torch = new Runnable() {

		@Override
		public void run() {
			// TODO Auto-generated method stub
			setParameters(SWITCH_ON, MIN, MAX);
			mLED = CAMERALED.BACK_COLD_LED_TORCH;
			startTest();
		}
	};

	private int mCount_wram = 3;
	private Runnable openRun_warm_flash = new Runnable() {

		@Override
		public void run() {
			// TODO Auto-generated method stub
			setParameters(SWITCH_ON_FLASH, MAX, MIN);
			mLED = CAMERALED.BACK_WARM_LED_FLASH;
			mCount_wram--;
			mHandler.postDelayed(openRun_warm_flash_restart, 2000);
			if(mCount_wram == 0) {
				startTest();
			}
		}
	};

	private Runnable openRun_warm_flash_restart = new Runnable() {

		@Override
		public void run() {
			// TODO Auto-generated method stub
			if(mCount_wram > 0) {
				setParameters(SWITCH_OFF, MIN, MIN);
				mHandler.postDelayed(openRun_warm_flash, 500);
			}
		}
	};

	private int mCount_cold = 3;
	private Runnable openRun_cold_flash = new Runnable() {

		@Override
		public void run() {
			// TODO Auto-generated method stub
			setParameters(SWITCH_ON_FLASH, MIN, MAX);
			mLED = CAMERALED.BACK_COLD_LED_FLASH;
			mCount_cold--;
			mHandler.postDelayed(openRun_cold_flash_restart, 2000);
		}
	};

	private Runnable openRun_cold_flash_restart = new Runnable() {

		@Override
		public void run() {
			// TODO Auto-generated method stub
			if(mCount_cold > 0) {
				setParameters(SWITCH_OFF, MIN, MIN);
				mHandler.postDelayed(openRun_cold_flash, 500);
			}else {
				onSucess();
			}
		}
	};

	private void setParameters(int _switch, int warm, int cold) {
		if (mCamera == null)
			return;
		Tool.toolLog(TAG+ "do: setParameters. " + _switch + "," + warm + "," + cold);
		mParameters = mCamera.getParameters();
		mParameters.set(CAMERA_FLASH_TEST_MODE, _switch + "," + warm + "," + cold);
		mCamera.setParameters(mParameters);
		if (_switch == SWITCH_ON || _switch == SWITCH_ON_FLASH) {
			mParameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
		} else {
			mParameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
		}
		mCamera.setParameters(mParameters);

	}

	public void onSucess() {
		// TODO Auto-generated method stub
//		Tool.toolLog(TAG + " onFinish");
		Test.state = "Pass";
		Tool.toolLog(TAG + " Test.state " + Test.state);
		FinishThread finishThread = new FinishThread(0x01, ExecuteTest.temppositon);
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
			Msg.WriteModelResult(mContext, autoRunActivity.auto_file_text, "PASS");
		}
		//End
	}
}
