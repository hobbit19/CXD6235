package com.tcl.manucase;

import java.io.IOException;

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

import android.content.Context;
import android.os.Handler;
import android.os.Vibrator;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.LinearLayout.LayoutParams;

public class VibratorTest extends Test implements DownTimeCallBack {

	private static final String TAG = "VibratorTest";
	private String text = "is vibrator on?";
	private Vibrator vibrator = null;
	long[] pattern = new long[] { 100, 500, 500, 500, 500, 500 };
	// PR661424-yinbin-zhang-20140515 begin
	private FrameLayout mFrameLayout;
	private LinearLayout mLinearLayout;
	private TestCountDownTimer mTestCountDownTimer;
	// PR661424-yinbin-zhang-20140515 end
	private boolean vibrate_flag = true;

	public VibratorTest(ID id, String name) {
		super(id, name);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void setUp() {
		// TODO Auto-generated method stub
//		Tool.toolLog(TAG + " setUp ...");
		Tool.toolLog(TAG + "_start_test");
		vibrator = (Vibrator) mContext
				.getSystemService(Context.VIBRATOR_SERVICE);
		
		mTestCountDownTimer = new TestCountDownTimer(6*SECOND, SECOND, this);
		mTestCountDownTimer.start();
		Tool.toolLog(TAG + " no vibrator " + vibrator.hasVibrator());
		if(!vibrator.hasVibrator()){
			vibrate_flag = false;
			Tool.toolLog(TAG + " has no vibrator");
		}

	}

	@Override
	public void initView() {
		// init layout view for case display
//		Tool.toolLog(TAG + " initView ...");
/*		mLayout = (LinearLayout) View.inflate(mContext, R.layout.base_screen,
				null);*/

		mLayout = (LinearLayout) View.inflate(mContext,
				R.layout.manu_base_screen, null);

		 bt_left = (Button) mLayout.findViewById(R.id.bt_left);
		 bt_right = (Button) mLayout.findViewById(R.id.bt_right);
		text_top_zone = (TextView) mLayout.findViewById(R.id.text_top_zone);
		text_cen_zone = (TextView) mLayout.findViewById(R.id.text_cen_zone);

		text_top_zone.setText(mName);
		text_cen_zone.setText(text);

		// PR661424-yinbin-zhang-20140515 begin
		 bt_left.setText(R.string.pass);
		 bt_right.setText(R.string.fail);

		 bt_left.setOnClickListener(pass_listener);
		 bt_right.setOnClickListener(failed_listener);
		// bt_left.setText(R.string.pass);
		// bt_right.setText(R.string.fail);
		//
		// bt_left.setOnClickListener(pass_listener);
		// bt_right.setOnClickListener(failed_listener);

		mFrameLayout = new FrameLayout(mContext);
		// mLinearLayout = new LinearLayout(mContext);
		// mLinearLayout.setLayoutParams(new
		// LinearLayout.LayoutParams(LayoutParams.FILL_PARENT,
		// LayoutParams.FILL_PARENT));
		// mLinearLayout.setGravity(Gravity.BOTTOM);
		// Button retestButton = new Button(mContext);
		// retestButton.setText("Retest");
		// LinearLayout.LayoutParams mLayoutParams = new
		// LinearLayout.LayoutParams(LayoutParams.FILL_PARENT,
		// LayoutParams.WRAP_CONTENT);
		// mLayoutParams.bottomMargin = 80;
		// mLinearLayout.addView(retestButton,mLayoutParams);
		// retestButton.setOnClickListener(new OnClickListener() {
		//
		// @Override
		// public void onClick(View v) {
		// // TODO Auto-generated method stub
		// mHandler.removeCallbacks(mRunnable);
		// mHandler.postDelayed(mRunnable, 0);
		// text_cen_zone.setText("Vibrator Retest");
		// mTestCountDownTimer.start();
		// }
		// });

		// mFrameLayout.addView(mLinearLayout);
		mFrameLayout.addView(mLayout);
		mContext.setContentView(mFrameLayout);
		// PR661424-yinbin-zhang-20140515 end
		
		mHandler.removeCallbacks(mRunnable);
		mHandler.postDelayed(mRunnable, SECOND);
	}

	@Override
	public void finish() {
		// TODO Auto-generated method stub
	}

	private Handler mHandler = new Handler();
	private Runnable mRunnable = new Runnable() {
		public void run() {
			if(!vibrate_flag){
//				onFinish();
				failed();
			}
			
			update();
			if (vibrate_flag) {
//				FinishThread tFinishThread = new FinishThread(0x01,ExecuteTest.temppositon);
//				tFinishThread.start();
//				try {
//					tFinishThread.join();
//				} catch (InterruptedException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
//
//				Msg.exitWithSuccessTest(mContext, TAG, 20, true,"Pass");
//
//				if(AllMainActivity.mainAllTest && !AllMainActivity.autofileFlag){
//					Msg.WriteModelResult(mContext, AllMainActivity.all_items_file_text,
//							"PASS");
//				}else {
//					Msg.WriteModelResult(mContext, autoRunActivity.auto_file_text,
//							"PASS");
//				}
			}else {

			}
			bt_left.setEnabled(true);
			bt_right.setEnabled(true);
		}

		void update() {
			Tool.toolLog(TAG + " mRunnable ...");
			try{
				vibrator.vibrate(pattern, -1);
				
			}catch (Exception e) {
				// TODO: handle exception
				vibrate_flag = false;
				Tool.toolLog(TAG + " has no vibratoring ...");
			}
		}
		
	};

	@Override
	public void onFinish() {
		// TODO Auto-generated method stub
		Tool.toolLog(TAG + " onFinish");
/*		text_cen_zone.setText("Test finished");
		// Add by Jianke.Zhang 01/23
		*//*if (vibrate_flag) {
			FinishThread tFinishThread = new FinishThread(0x01,ExecuteTest.temppositon);
			tFinishThread.start();
			try {
				tFinishThread.join();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			Msg.exitWithSuccessTest(mContext, TAG, 20, true,"Pass");

			if(AllMainActivity.mainAllTest && !AllMainActivity.autofileFlag){
				Msg.WriteModelResult(mContext, AllMainActivity.all_items_file_text,
						"PASS");
			}else {
				Msg.WriteModelResult(mContext, autoRunActivity.auto_file_text,
						"PASS");
			}
		}else*//*{
			mContext.setResult(Test.RESULT.FAILED.ordinal());
			Tool.toolLog(TAG + " index 8886 -> " + ExecuteTest.temppositon);
			int double_test;
			if(AllMainActivity.mainAllTest){
				double_test = AllMainActivity.double_check_for_auto[ExecuteTest.temppositon]++;
			}else {
				double_test = autoRunActivity.double_check[ExecuteTest.temppositon]++;
			}
			Tool.toolLog(TAG + " double_test 9996 -> " + double_test);
			FinishThread tFinishThread = new FinishThread(0x02,ExecuteTest.temppositon);
			tFinishThread.start();
			try {
				tFinishThread.join();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if(double_test==1){
				Msg.exitWithException(mContext, TAG,20, true,"Pass");
				if(AllMainActivity.mainAllTest && !AllMainActivity.autofileFlag){
					Msg.WriteModelResult(mContext, AllMainActivity.all_items_file_text,
							"FAIL");
				}else {
					Msg.WriteModelResult(mContext, autoRunActivity.auto_file_text,
							"FAIL");
				}
			}else{
				Msg.exitWithException(mContext, TAG,20, true,"Fail");
			}

		}*/
		// End
	}

	@Override
	public void onTick() {
		// TODO Auto-generated method stub
		//Tool.toolLog(TAG + " 1223131 onTick");
	}
	// PR661424-yinbin-zhang-20140515 end

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
		if (vibrator != null) {
			vibrator.cancel();
		}
		if(mRunnable != null){
			mHandler.removeCallbacks(mRunnable);
		}
		if (mTestCountDownTimer != null) {
			mTestCountDownTimer.cancel();
			mTestCountDownTimer = null;
		}
	}
}
