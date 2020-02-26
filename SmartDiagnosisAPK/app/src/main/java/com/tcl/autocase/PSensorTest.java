package com.tcl.autocase;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;

import android.annotation.SuppressLint;
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

public class PSensorTest extends Test implements SensorEventListener,
DownTimeCallBack {

	
	private static final String TAG = "AlsPsTest";
	// PR518448-yancan-zhu-20130903 begin
	private final String PATH = "/sys/devices/platform/als_ps/driver/ps";
	private final int PADLEFT = 310;
	private LinearLayout psview;
	private TextView data;
	private TextView text_cen_zone;
	private Handler mhandler = new Handler();
	private Runnable runnable = null;
	// PR518448-yancan-zhu-20130903 end
	private String text = "";
	private SensorManager mSensorManager;
	private Sensor mProximitySensor;
	private int far2neer = 0;
	private int near2far = -1;
	private TestCountDownTimer testCountDownTimer = null;
	boolean state = false;
	
	public PSensorTest(ID id, String name) {
		super(id, name);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void onFinish() {
		// TODO Auto-generated method stub
		if (!state){mContext.setResult(Test.RESULT.FAILED.ordinal());
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


    public Handler mHandler = new Handler(){
        public void handleMessage(android.os.Message msg) {
        	Log.d(TAG, "33333 handleMessage: 111");
			FinishThread tFinishThread = new FinishThread(0x01,ExecuteTest.temppositon);
			tFinishThread.start();
			try {
				tFinishThread.join();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			Log.d(TAG, "33333 handleMessage: 222");
			Msg.exitWithSuccessTest(mContext,TAG,20,true,"Pass");
			//Write data into file
			if(AllMainActivity.mainAllTest && !AllMainActivity.autofileFlag){
				Msg.WriteModelResult(mContext,AllMainActivity.all_items_file_text, "PASS");
			}else{
				Msg.WriteModelResult(mContext,autoRunActivity.auto_file_text, "PASS");
			}
		};
	};
	
	public String readPsRawData(String path) {
		StringBuffer s = new StringBuffer();
		FileInputStream in = null;
		try {
			String temp = "";
			in = new FileInputStream(path);
			Tool.toolLog(TAG + " in " + in);
			BufferedReader read = new BufferedReader(new InputStreamReader(in));
			while ((temp = read.readLine()) != null) {
				s.append(temp);
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				//Maybe no driver path so in is null
				if(in != null){
					in.close();
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return s.toString();
	}
	@Override
	public void setUp() {
		// TODO Auto-generated method stub
		Tool.toolLog(TAG + "_start_test");
		if (mSensorManager == null) {
			mSensorManager = (SensorManager) mContext
					.getSystemService(Context.SENSOR_SERVICE);
		}

		mProximitySensor = mSensorManager
				.getDefaultSensor(Sensor.TYPE_PROXIMITY);

		if (mProximitySensor != null) {
			
			
			if (!mSensorManager.registerListener(this, mProximitySensor,
					SensorManager.SENSOR_DELAY_UI)) {
				Log.v("qinhao", "mProximitySensor:");
				Tool.toolLog(TAG + " register listener for ProximitySensor "
						+ mProximitySensor.getName() + " failed");
			
			}
			text = "Opening.....";
			Log.v("qinhao", "Opening:");
		} else {
			text = "ProximitySensor not detected";
			Log.v("qinhao", "text:"+text);
		}
		testCountDownTimer = new TestCountDownTimer(SECOND*100, SECOND*50, this);
		
	}

	@Override
	public void initView() {
		// TODO Auto-generated method stub
		mLayout = (LinearLayout) View.inflate(mContext,
				R.layout.psensor_base_screen_view, null);

		text_top_zone = (TextView) mLayout.findViewById(R.id.text_top_zone);
		

		text_cen_zone = new TextView(mContext);
		text_cen_zone.setGravity(Gravity.CENTER);
	
		psview = (LinearLayout) mLayout.findViewById(R.id.addview);
		psview.setGravity(Gravity.CENTER);
		psview.addView(text_cen_zone);
		//psview.addView(ldata);
		text_top_zone.setText(mName);
		StringBuilder ps_info = new StringBuilder();
		ps_info.append("ProximitySensor detected");
		ps_info.append("\n\n\n" + "near->far:" + near2far);
		ps_info.append("\n" + "far->near:" + far2neer);
		text = ps_info.toString();
		Log.v("qinhao", "ps_info:"+ps_info);
		text_cen_zone.setText(text);
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
		// TODO Auto-generated method stub
				if (mSensorManager != null) {
					mSensorManager.unregisterListener(this, mProximitySensor);
				}
				if (mhandler != null) {
					mhandler.removeCallbacks(runnable);
				}
				far2neer = 0;
				near2far = -1;
				state = false;
				if (testCountDownTimer != null) {
					testCountDownTimer.cancel();
				}
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		// TODO Auto-generated method stub
		// TODO Auto-generated method stub
				Log.v("qinhao", "mProximitySensor:onSensorChanged");
				float distance;
				Tool.toolLog(TAG + " onSensorChanged: (" + event.values[0] + ")");
				Log.d(TAG, "33333 onSensorChanged: (" + event.values[0] + ")");
				distance = event.values[0];
				if(distance > 0 && distance != 1){
					distance = 1;
				}
				if (distance == 0) {
					far2neer++;
				}
				if (distance == 1) {
					near2far++;
				}
				StringBuilder ps_info = new StringBuilder();
				ps_info.append("ProximitySensor detected");
				ps_info.append("\n\n\n" + "near->far:" + near2far);
				ps_info.append("\n" + "far->near:" + far2neer);
				text = ps_info.toString();
				Log.v("qinhao", "ps_info:"+ps_info);
				text_cen_zone.setText(text);
				if ((near2far != 0) && (far2neer != 0) && (!state)) {
					state = true;
					mHandler.sendEmptyMessageDelayed(0, SECOND);
				}
	}
}
