package com.tcl.autocase;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
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

public class LightSensorTestAuto extends Test implements SensorEventListener,DownTimeCallBack{
	LinearLayout addview;
	private LinearLayout psview;
	private static String TAG = "LightSensorTest";
    boolean mPass = false;
	private int HIGH = 40;
	// PR505415-yancan-zhu-20130820 begin
	private int LOW = 145;
	// PR505415-yancan-zhu-20130820 end
	private int fh = -1;
	private int fl = -1;
	private SensorManager mSensorManager;
	private Sensor lsensor;
	String text = "";
	private Handler mhandler = new Handler();
	private Runnable runnable = null;
	private TestCountDownTimer testCountDownTimer = null;
	public LightSensorTestAuto(ID id, String name) {
		super(id, name);
		// TODO Auto-generated constructor stub
		/*if(AllMainActivity.deviceName.equals("DiabloHD_LTE")){
			HIGH = 100;
			LOW = 51;
		}*/
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		
		Log.v("qinhao", "LightSensorTestAutoonSensorChanged");
		if(!mPass)
		{
		// TODO Auto-generated method stub
		float value;
		value= event.values[0];
		//int value = (int) event.values[0];
		Tool.toolLog(TAG + " value " + value);
		Log.i("qinhao", "LightSensorTestAuto value is:"+value);
		if (value <=LOW) {
			fl = 1;
		}  
		if (value > HIGH) {
			fh = 1;
		}

		//String s = "ambient light is " + value + "\n\n";
		StringBuilder ps_info = new StringBuilder();
		ps_info.append("ambient light is "  + value +"\n\n");
		if (fl > 0) {
			//s = s + "dark: OK\n";
			ps_info.append("\n" + "dark: OK");
		} else {
			//s = s + "dark: not tested\n";
			ps_info.append("\n" + "dark: not tested");
		}

		if (fh > 0) {
			//s = s + "bright: OK\n";
			ps_info.append("\n" + "bright: OK");
			
		} else {
			//s = s + "bright: not tested\n";
			ps_info.append("\n" + "bright: not tested");
		}
        //text = s; 
		text = ps_info.toString();
		text_cen_zone.setText(text);

		if ((fl > 0) && (fh > 0)) {
			mHandler.sendEmptyMessageDelayed(0, SECOND);
			mPass = true;
		}
		}
	
		
		
	}
	
public Handler mHandler = new Handler(){
		
		public void handleMessage(android.os.Message msg) {	
			
			
				FinishThread tFinishThread = new FinishThread(0x01,ExecuteTest.temppositon);
				tFinishThread.start();
				try {
					tFinishThread.join();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				Msg.exitWithSuccessTest(mContext,TAG,20,true,"Pass");
				
				//Write data into file
				if(AllMainActivity.mainAllTest && !AllMainActivity.autofileFlag){
					Msg.WriteModelResult(mContext,AllMainActivity.all_items_file_text, "PASS");
				}else{
					Msg.WriteModelResult(mContext,autoRunActivity.auto_file_text, "PASS");
				
				//End
			}
		};
	};

	@Override
	public void setUp() {
		// TODO Auto-generated method stub
	
		Tool.toolLog(TAG + "_start_test");
		if (mSensorManager == null) {
			mSensorManager = (SensorManager) mContext
					.getSystemService(Context.SENSOR_SERVICE);
		}

		lsensor = mSensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
		if (lsensor != null) {
//			Tool.toolLog(TAG + " LightSensor opened : " + lsensor.getName());
			if (!mSensorManager.registerListener(this, lsensor,
					SensorManager.SENSOR_DELAY_NORMAL)) {
				Tool.toolLog(TAG + " register listener for sensor "
						+ lsensor.getName() + " failed");
			}
			text = "opening ...";
		} else {
			text = "No Sensor found";
		}
		
		testCountDownTimer = new TestCountDownTimer(SECOND*10, SECOND, this);
	}

	@Override
	public void initView() {

		mLayout = (LinearLayout) View.inflate(mContext,
				R.layout.psensor_base_screen_view, null);
		text_top_zone = (TextView) mLayout.findViewById(R.id.text_top_zone);
		text_top_zone.setText(mName);
		text_cen_zone = new TextView(mContext);
		text_cen_zone.setGravity(Gravity.CENTER);
		text_cen_zone.setText(text);
		psview = (LinearLayout) mLayout.findViewById(R.id.addview);
		psview.setGravity(Gravity.CENTER);
		psview.addView(text_cen_zone);
		mContext.setContentView(mLayout);
		testCountDownTimer.start();
	
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

	@Override
	public void destroy() {
		// TODO Auto-generated method stub
		if (mSensorManager != null) {
			mSensorManager.unregisterListener(this, lsensor);
		}
		if (mhandler != null) {
			mhandler.removeCallbacks(runnable);
		}
		fl = -1;
		fh = -1;
		if (testCountDownTimer != null) {
			testCountDownTimer.cancel();
			
		}
		
		
	}

	@Override
	public void onFinish() {
		// TODO Auto-generated method stub
		if ((fl < 0) || (fh < 0)){mContext.setResult(Test.RESULT.FAILED.ordinal());
		Tool.toolLog(TAG + " index 888 -> " + ExecuteTest.temppositon);
		int double_test;
		if(AllMainActivity.mainAllTest){
			double_test = AllMainActivity.double_check_for_auto[ExecuteTest.temppositon]++;
			//AllMainActivity.mainAllTest = false;
		}else {
			double_test = autoRunActivity.double_check[ExecuteTest.temppositon]++;
		}
		Tool.toolLog(TAG + " double_test 999-> " + double_test);
		FinishThread finishThread=	new FinishThread(0x02,ExecuteTest.temppositon);
		finishThread.start();
		try {
            finishThread.join();
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
		if(double_test==1){
			//Add by Jianke.Zhang 02/02
			
			Msg.exitWithException(mContext,TAG,20,true,"Pass");
			
			//Write data into file 06/17 bug
			if(AllMainActivity.mainAllTest && !AllMainActivity.autofileFlag){
				Msg.WriteModelResult(mContext,AllMainActivity.all_items_file_text, "FAIL");
			}else{
				Msg.WriteModelResult(mContext,autoRunActivity.auto_file_text, "FAIL");
			}
			//End
		}else{
			Msg.exitWithException(mContext,TAG,20,true,"Fail");
			
		}
	}
		
	}

	@Override
	public void onTick() {
		// TODO Auto-generated method stub
		
	}

}
