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

public class FrontFlashLEDTest extends Test implements DownTimeCallBack{
	private static String TAG = "FrontFlashLEDTest";
	String text = "";
	private Camera mCamera;
	private Camera.Parameters mParameters;
	private boolean successFlag = true;
	public static boolean sysFlash = false;

	TestCountDownTimer testCountDownTimer = null;

	public FrontFlashLEDTest(ID id, String name) {
		super(id, name);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void setUp() {
		// TODO Auto-generated method stub
		Tool.toolLog(TAG + "_start_test");
		testCountDownTimer = new TestCountDownTimer(SECOND*10, SECOND, this);
		testCountDownTimer.start();

		try {
			mCamera = Camera.open(1);
			// Tool.toolLog(TAG + " mCamera " + mCamera);
		} catch (Exception e) {
			Tool.toolLog(TAG + " can't open camera ");
			text = "Flash LED open failed";
			successFlag = false;
		}
		if (mCamera != null) {
			mParameters = mCamera.getParameters();
			mParameters.set("flash-mode", "torch");
			mCamera.setParameters(mParameters);
			Camera.Parameters p = mCamera.getParameters();
			p.getFlashMode();//��ȡ����Ƶ�״̬

			Log.i("qinhao", "shanguangdeng:::::"+p.getFlashMode());
			text = "turning on";
			new FinishThread(0x01, ExecuteTest.temppositon).start();
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
}
