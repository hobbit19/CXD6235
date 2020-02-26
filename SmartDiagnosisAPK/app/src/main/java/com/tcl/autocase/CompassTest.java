package com.tcl.autocase;

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
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

public class CompassTest extends Test implements SensorEventListener,DownTimeCallBack {
	String text = "compass test start : \n move the phone in an 8 shape";
	String mDisplayString;
	FrameLayout fl;
	private static SensorManager mSensorManager;
	private Sensor mCompass;
	private SampleView mView;
	private float[] mValues;
	private static final String TAG = "CompassTest";
	
	private TestCountDownTimer testCountDownTimer = null;
	private boolean isGetCompass = false;
	
	public CompassTest(ID id, String name) {
		super(id, name);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void setUp() {
		// TODO Auto-generated method stub
		Tool.toolLog(TAG + "_start_test");
		if (mSensorManager == null)
            mSensorManager = (SensorManager)mContext.getSystemService(Context.SENSOR_SERVICE);
		mCompass = mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION); 
//		Tool.toolLog(TAG + " mCompass " + mCompass);
		if(mCompass != null){
			if (!mSensorManager.registerListener(this, mCompass,SensorManager.SENSOR_DELAY_NORMAL)) {
				 Tool.toolLog(TAG + " register listener for sensor " + mCompass.getName() + " failed");
			 }
		}
		
		testCountDownTimer = new TestCountDownTimer(10*SECOND, SECOND, this);
		testCountDownTimer.start();
	}

	@Override
	public void initView() {
//		Tool.toolLog(TAG + " initView ...");
		// TODO Auto-generated method stub
		//Delete by Jianke.Zhang 02/04
//		mLayout = (LinearLayout) View.inflate(mContext,
//				R.layout.manu_base_screen, null);
//		fl = new FrameLayout(mContext);
//
//		bt_left = (Button) mLayout.findViewById(R.id.bt_left);
//		bt_right = (Button) mLayout.findViewById(R.id.bt_right);
//		text_top_zone = (TextView) mLayout.findViewById(R.id.text_top_zone);
//		text_cen_zone = (TextView) mLayout.findViewById(R.id.text_cen_zone);
//
//		text_top_zone.setText(mName);
//		text_cen_zone.setText(text);
		//End
		// PR661395-yinbin-zhang-20140516 begin
		/*
		 * bt_left.setText(R.string.fail); bt_right.setText(R.string.pass);
		 * 
		 * bt_left.setOnClickListener(failed_listener);
		 * bt_right.setOnClickListener(pass_listener);
		 */
		//Delete by Jianke.Zhang 02/04
//		bt_left.setText(R.string.pass);
//		bt_right.setText(R.string.fail);
//
//		bt_left.setOnClickListener(pass_listener);
//		bt_right.setOnClickListener(failed_listener);
		// PR661395-yinbin-zhang-20140516 end

//		fl.addView(mLayout);
//
//		mContext.setContentView(fl);
		//End
		
		//Add by Jianke.Zhang 02/04
		mView = new SampleView(this.mContext);
		getView();
		//End
	}

	@Override
	public void destroy() {
		// TODO Auto-generated method stub
		if (mSensorManager != null) {
			mSensorManager.unregisterListener(this, mCompass);
		}
		if(testCountDownTimer != null){
			testCountDownTimer.cancel();
			testCountDownTimer = null;
		}
		mView = null;
	}

	public void getView() {
//		Tool.toolLog(TAG + " getView ...");
		mLayout = (LinearLayout) View.inflate(mContext,
				R.layout.base_screen_view, null);

		bt_left = (Button) mLayout.findViewById(R.id.bt_left);
		bt_right = (Button) mLayout.findViewById(R.id.bt_right);
		text_top_zone = (TextView) mLayout.findViewById(R.id.text_top_zone);
		bt_left.setVisibility(View.GONE);
		bt_right.setVisibility(View.GONE);
		
		LinearLayout addview = (LinearLayout) mLayout
				.findViewById(R.id.addview);
		addview.setPadding(5, 80, 5, 50);
		addview.addView(mView);

		text_top_zone.setText("No " + mName);
		// PR661395-yinbin-zhang-20140516 begin
		/*
		 * bt_left.setText(R.string.fail); bt_right.setText(R.string.pass);
		 * 
		 * bt_left.setOnClickListener(failed_listener);
		 * bt_right.setOnClickListener(pass_listener);
		 */
//		bt_left.setText(R.string.pass);
//		bt_right.setText(R.string.fail);
//		bt_left.setEnabled(false);
		
//		bt_left.setOnClickListener(pass_listener);
//		bt_right.setOnClickListener(failed_listener);
		// PR661395-yinbin-zhang-20140516 end
		mContext.setContentView(mLayout);
	}

//	public void pass() {
//		// TODO Auto-generated method stub
//		if (mView == null) {
//			Tool.toolLog(TAG + " pass... ");
//			mView = new SampleView(this.mContext);
//			getView();
//		} else {
//			super.pass();
//		}
//	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		// TODO Auto-generated method stub
	    mValues = event.values;
//	    Tool.toolLog(TAG + " mValues " + mValues);
	    
	    if(!isGetCompass){
	    	if(event.sensor.getType() == Sensor.TYPE_ORIENTATION){
//		    	mCompass = mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
//		    	bt_left.setEnabled(true);
//		    	bt_right.setEnabled(false);
		    	text_top_zone.setText(mName);
		    	
		    	mHandler.sendEmptyMessageDelayed(0, SECOND);
		    	isGetCompass = true;
		    }
		    
//	        Log.d(TAG, "sensorChanged (" + mValues[0] + ", " + mValues[1] + ", " + mValues[2] + ")");		
			if (mView != null) {
				 mView.invalidate();
			 }
	    }
	    
	}


	
Handler mHandler = new Handler(){
		
	public void handleMessage(android.os.Message msg) {
		success();
		}
	};
		
	private void success() {
		// TODO Auto-generated method stub

		mContext.setResult(Test.RESULT.PASS.ordinal());
		FinishThread tFinishThread = new FinishThread(0x01,ExecuteTest.temppositon);
		tFinishThread.start();
		try {
			tFinishThread.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		Msg.exitWithSuccessTest(mContext,TAG, 10,true,"Pass");
		
		if(AllMainActivity.mainAllTest && !AllMainActivity.autofileFlag){
			Msg.WriteModelResult(mContext,AllMainActivity.all_items_file_text, "PASS");
		}else {
			Msg.WriteModelResult(mContext,autoRunActivity.auto_file_text, "PASS");
		}	
	}
	
	class SampleView extends View {
		private Paint mPaint = new Paint();
		private Path mPath = new Path();

		public SampleView(Context context) {
			super(context);

//			Tool.toolLog(TAG + " SampleView");
			// Construct a wedge-shaped path
			mPath.moveTo(0, -50);
			mPath.lineTo(-20, 60);
			mPath.lineTo(0, 50);
			mPath.lineTo(20, 60);
			mPath.close();
		}

		@Override
		protected void onDraw(Canvas canvas) {
//			Tool.toolLog(TAG + " onDraw");
			Paint paint = mPaint;

			canvas.drawColor(Color.WHITE);

			paint.setAntiAlias(true);
			paint.setColor(Color.BLACK);
			paint.setStyle(Paint.Style.FILL);

			int w = canvas.getWidth();
			int h = canvas.getHeight();
			int cx = w / 2;
			int cy = h / 2;

			canvas.translate(cx, cy);
//			Tool.toolLog(TAG + " mValues " + mValues);
			if (mValues != null) {
				canvas.rotate(-mValues[0]);
			}
			canvas.drawPath(mPath, mPaint);
		
	}

	public String getmContextTag() {
		// TODO Auto-generated method stub
		return TAG;
	}

	}

	@Override
	public void onFinish() {
		// TODO Auto-generated method stub
		//��ʱ�仹û��⵽compass��Ϊʧ��
		fail();
	}

	private void fail() {
		// TODO Auto-generated method stub
		mContext.setResult(Test.RESULT.FAILED.ordinal());
		Tool.toolLog(TAG + " index 8887 -> " + ExecuteTest.temppositon);
		int double_test;
		if(AllMainActivity.mainAllTest){
			double_test = AllMainActivity.double_check_for_auto[ExecuteTest.temppositon]++;
		}else {
			double_test = autoRunActivity.double_check[ExecuteTest.temppositon]++;
		}
		Tool.toolLog(TAG + " double_test 9997 -> " + double_test);
		FinishThread tFinishThread = new FinishThread(0x02,ExecuteTest.temppositon);
		tFinishThread.start();
		try {
			tFinishThread.join();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if(double_test==1){
			Msg.exitWithException(mContext,TAG,20,true,"Pass");
			//Write data into file
			if(AllMainActivity.mainAllTest && !AllMainActivity.autofileFlag){
				Msg.WriteModelResult(mContext,AllMainActivity.all_items_file_text, "FAIL");
			}else {
				Msg.WriteModelResult(mContext,autoRunActivity.auto_file_text, "FAIL");
			}
			//End
		}else{
			Msg.exitWithException(mContext,TAG,20,true,"Fail");
		}
	}

	@Override
	public void onTick() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void finish() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String getmContextTag() {
		// TODO Auto-generated method stub
		return TAG;
	}

	@Override
	public void setmContextTag() {
		// TODO Auto-generated method stub
		
	}
}
