package com.tcl.manucase;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;

import com.tcl.autotest.R;
import com.tcl.autotest.tool.Tool;
import com.tcl.autotest.utils.DownTimeCallBack;
import com.tcl.autotest.utils.Test;
import com.tcl.autotest.utils.TestCountDownTimer;


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

public class AlsPsTest extends Test implements SensorEventListener,
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

	public AlsPsTest(ID id, String name) {
		super(id, name);
		// TODO Auto-generated constructor stub
	}

	public AlsPsTest(ID id, String name, Boolean updateFlag) {
		super(id, name, updateFlag);
		// TODO Auto-generated constructor stub
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
				Tool.toolLog(TAG + " register listener for ProximitySensor "
						+ mProximitySensor.getName() + " failed");
			}
			text = "Opening.....";
		} else {
			text = "ProximitySensor not detected";
		}
		testCountDownTimer = new TestCountDownTimer(SECOND*10, SECOND, this);
		testCountDownTimer.start();
	}

	public void mView() {

		mLayout = (LinearLayout) View.inflate(mContext,
				R.layout.base_screen_view, null);
		bt_left = (Button) mLayout.findViewById(R.id.bt_left);
		bt_right = (Button) mLayout.findViewById(R.id.bt_right);
		text_top_zone = (TextView) mLayout.findViewById(R.id.text_top_zone);
		text_top_zone.setText(mName);
		text_cen_zone = new TextView(mContext);
		text_cen_zone.setGravity(Gravity.CENTER);
		// PR661395-yinbin-zhang-20140516 begin
		/*
		 * bt_left.setText(R.string.fail); bt_right.setText(R.string.pass);
		 * bt_left.setOnClickListener(failed_listener);
		 * bt_right.setOnClickListener(pass_listener);
		 */
		bt_left.setText(R.string.pass);
		bt_right.setText(R.string.fail);
		bt_left.setVisibility(View.VISIBLE);
		bt_right.setVisibility(View.VISIBLE);
		bt_left.setEnabled(false);
		bt_left.setOnClickListener(pass_listener);
		bt_right.setOnClickListener(failed_listener);
		/*LinearLayout ldata = new LinearLayout(mContext);
		ldata.setOrientation(LinearLayout.HORIZONTAL);
		ldata.setPadding(PADLEFT, 0, 0, 0);
		TextView dv = new TextView(mContext);
		dv.setText("\n\npsrawdata:");
		data = new TextView(mContext);
		data.setText("\n\n" + "ps_dec=0");
		ldata.addView(dv);
		ldata.addView(data);
		dataThread();*/
		psview = (LinearLayout) mLayout.findViewById(R.id.addview);
		psview.setGravity(Gravity.CENTER);
		psview.addView(text_cen_zone);
		//psview.addView(ldata);
		mContext.setContentView(mLayout);
	}

	private void dataThread() {
		runnable = new Runnable() {

			@Override
			public void run() {
				String s = readPsRawData(PATH);
				Tool.toolLog(TAG + " ssss " + s);
				// TODO Auto-generated method stub
				int n = trans16to10(s);
				String str = String.valueOf(n);
				data.setText("\n\n" + str);
				mhandler.postDelayed(this, 200);
			}

		};
		mhandler.postDelayed(runnable, 200);
	}

	// PR518448-yancan-zhu-20130903 end

	private String getExactNumber(String s){
		String se = "";
		int i;
		for(i=0;i<s.length();i++){
			if(s.charAt(i)==' '){
				break;
			}
		}
		se = s.substring(i);
		
		return se;
	}
	
	
	public int trans16to10(String s) {
		//s = getExactNumber(s);
		int total = 0;
		int num = 1;
		int count = 0;
		char[] mchar = s.toCharArray();
//		for(int i=0;i<mchar.length;i++){
//			Tool.toolLog(TAG + " mchar " + mchar[i]);
//		}
//		Tool.toolLog(TAG + " s.length() " + s.length());
		for (int i = s.length() - 1; i > 1; i--) {
			switch (mchar[i]) {
			case 'A':
				count = 10;
				break;
			case 'B':
				count = 11;
				break;
			case 'C':
				count = 12;
				break;
			case 'D':
				count = 13;
				break;
			case 'E':
				count = 14;
				break;
			case 'F':
				count = 15;
				break;
			default:
				count = Integer.valueOf(String.valueOf(mchar[i]));
				break;
			}
			total = total + count * num;
			num = 16 * num;
		}
//		Tool.toolLog(TAG + " total " + total);
		
		return total;
	}

	@Override
	public void initView() {
		// TODO Auto-generated method stub
		mView();
	}

	@Override
	public void updateView() {
		// TODO Auto-generated method stub
		text = "ProximitySensor detected \n\n" + "near->far:0\n"
				+ "far->neer:0\n";
		text_cen_zone.setText(text);
	}

	@Override
	public void destroy() {
		// TODO Auto-generated method stub
		if (mSensorManager != null) {
			mSensorManager.unregisterListener(this, mProximitySensor);
		}
		if (mhandler != null) {
			mhandler.removeCallbacks(runnable);
		}
		far2neer = 0;
		near2far = -1;
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
		float distance;
		Tool.toolLog(TAG + " onSensorChanged: (" + event.values[0] + ")");
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
		text_cen_zone.setText(text);
		if ((near2far != 0) && (far2neer != 0)) {
//			Tool.toolLog(TAG + " 100000000000");
			bt_left.setEnabled(true);
		}
	}

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

	// PR518448-yancan-zhu-20130903 end

	@Override
	public void onFinish() {
		// TODO Auto-generated method stub
//		Tool.toolLog(TAG + " 88889988888"); 
//		bt_left.setEnabled(true);
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
