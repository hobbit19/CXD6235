package com.tcl.manucase;

import com.tcl.autotest.AllMainActivity;
import com.tcl.autotest.R;
import com.tcl.autotest.manuRunActivity;
import com.tcl.autotest.tool.Tool;
import com.tcl.autotest.utils.ManuFinishThread;
import com.tcl.autotest.utils.Msg;
import com.tcl.autotest.utils.Test;
import com.tcl.autotest.utils.TestCountDownTimer;

import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class LightSensorTest extends Test implements SensorEventListener {
	private static String TAG = "LightSensorTest";
	
	private int HIGH = 30;
	// PR505415-yancan-zhu-20130820 begin
	private int LOW = 21;
	// PR505415-yancan-zhu-20130820 end
	private int fh = -1;
	private int fl = -1;
	private SensorManager mSensorManager;
	private Sensor lsensor;
	String text = "";

	public LightSensorTest(ID id, String name, Boolean updateFlag) {
		super(id, name, updateFlag);
		if(AllMainActivity.deviceName.equals("DiabloHD_LTE")){
			HIGH = 100;
			LOW = 51;
		}
	}

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
	}

	@Override
	public void initView() {
		// TODO Auto-generated method stub
		mLayout = (LinearLayout) View.inflate(mContext,
				R.layout.manu_base_screen, null);

		bt_left = (Button) mLayout.findViewById(R.id.bt_left);
		bt_right = (Button) mLayout.findViewById(R.id.bt_right);
		text_top_zone = (TextView) mLayout.findViewById(R.id.text_top_zone);
		text_cen_zone = (TextView) mLayout.findViewById(R.id.text_cen_zone);

		text_top_zone.setText(mName);
		text_cen_zone.setText(text);
		// PR661395-yinbin-zhang-20140516 begin
		/*
		 * bt_left.setText(R.string.fail); bt_right.setText(R.string.pass);
		 * bt_right.setEnabled(false);
		 * 
		 * bt_left.setOnClickListener(failed_listener);
		 * bt_right.setOnClickListener(pass_listener);
		 */
		bt_left.setText(R.string.pass);
		bt_right.setText(R.string.fail);
		bt_left.setEnabled(false);

		bt_left.setOnClickListener(pass_listener);
		bt_right.setOnClickListener(failed_listener);
		// PR661395-yinbin-zhang-20140516 end

		mContext.setContentView(mLayout);
	}

	@Override
	public void destroy() {
		// TODO Auto-generated method stub
		if (mSensorManager != null) {
			mSensorManager.unregisterListener(this, lsensor);
		}
		fl = fh = -1;
//		Tool.toolLog("Finish unregisterListener");
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		// TODO Auto-generated method stub
//		Tool.toolLog(TAG + " onSensorChanged: (" + event.values[0] + ", "
//				+ event.values[1] + ", " + event.values[2] + ")");

		int value = (int) event.values[0];
		Tool.toolLog(TAG + " value " + value);
		if (value < LOW) {
			fl = 1;
		} else if (value > HIGH) {
			fh = 1;
		}

		String s = "ambient light is " + value + "\n\n";
		if (fl > 0) {
			s = s + "dark: OK\n";
		} else {
			s = s + "dark: not tested\n";
		}

		if (fh > 0) {
			s = s + "bright: OK\n";
		} else {
			s = s + "bright: not tested\n";
		}

		text_cen_zone.setText(s);

		if ((fl > 0) && (fh > 0)) {
			bt_left.setEnabled(true);
			// PR480730-yancan-zhu-20130702 begin
			// mContext.setResult(Test.RESULT.PASS.ordinal());
			// mContext.finish();
			// PR480730-yancan-zhu-20130702 end
			// Msg.sendManuMessage(TAG,mContext);

			// new ManuFinishThread(0x01).start();
			// End
		}
	}

	public void updateView() {
		text = "ambient light should change \n\n" + "dark: not tested\n"
				+ "bright: not tested\n";
		text_cen_zone.setText(text);
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
