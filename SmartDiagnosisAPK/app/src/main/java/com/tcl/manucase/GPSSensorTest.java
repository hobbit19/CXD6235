package com.tcl.manucase;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.NoSuchElementException;

import com.tcl.autotest.R;
import com.tcl.autotest.tool.Tool;
import com.tcl.autotest.utils.DownTimeCallBack;
import com.tcl.autotest.utils.Test;
import com.tcl.autotest.utils.TestCountDownTimer;


import android.content.Context;
import android.location.GpsSatellite;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class GPSSensorTest extends Test implements LocationListener,
		DownTimeCallBack {
	private static String TAG = "GPSSensorTest";
	
	String text = "";
	private boolean init = false;
	private boolean initOpen = true;
	private LocationManager mLocationManager;
	private int mStatusCount = 0;
	private TestCountDownTimer testCountDownTimer = null;
	private GpsStatus mGpsStatus = null;
	GPSStatusListener mGpsStatusListener = new GPSStatusListener();

	public GPSSensorTest(ID id, String name) {
		super(id, name);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void setUp() {
		// TODO Auto-generated method stub
		Tool.toolLog(TAG + "_start_test");
		mLocationManager = (LocationManager) mContext
				.getSystemService(Context.LOCATION_SERVICE);
		text = "GPS Initializing...";
		//Add 03/10
		Class<?> c;
		Method[] ms;
		Class<?>[] cs;
		Method mGet;
		boolean locationEnabled;
		Method mSet;
		try {
			c = Class.forName("android.provider.Settings$Secure"); //.Secure
//			cs = c.getDeclaredClasses();
//			ms = c.getDeclaredMethods();
//			for(Class<?> css : cs){
//				Tool.toolLog(TAG + " css -> " + css);
//			}
//			for(Method m : ms){
//				Tool.toolLog(TAG + " m -> " + m);
//			}
			mGet = c.getDeclaredMethod("isLocationProviderEnabled", 
					android.content.ContentResolver.class,java.lang.String.class);
			locationEnabled = (Boolean) mGet.invoke(c, mContext.getContentResolver(),LocationManager.GPS_PROVIDER);
//			Tool.toolLog(TAG + " locationEnabled " + locationEnabled);
			
			if(!locationEnabled){
				mSet = c.getDeclaredMethod("setLocationProviderEnabled",
						android.content.ContentResolver.class,
						java.lang.String.class,boolean.class);
//				Tool.toolLog(TAG + " xxxxxxxxxx " + locationEnabled);
				//Add system authority in AndroidManifest.xml that can set
				mSet.invoke(c, mContext.getContentResolver(),LocationManager.GPS_PROVIDER, true);
				locationEnabled = (Boolean) mGet.invoke(c, mContext.getContentResolver(),LocationManager.GPS_PROVIDER);
//				Tool.toolLog(TAG + " locationEnabled ... " + locationEnabled);
			}
		} catch (InvocationTargetException e){
			//java.lang.SecurityException: Permission denial: writing to secure settings requires android.permission.WRITE_SECURE_SETTINGS
//			Tool.toolLog(TAG + " 22222222222222 " + e.getTargetException());
			testCountDownTimer = new TestCountDownTimer(10*SECOND, SECOND, this);
			testCountDownTimer.start();
			initOpen = false;
		} catch (Exception e1) {
			// TODO Auto-generated catch block
//			e1.printStackTrace();
			Tool.toolLog(TAG + " 1111111111111111 " + e1);
			testCountDownTimer = new TestCountDownTimer(10*SECOND, SECOND, this);
			testCountDownTimer.start();
			initOpen = false;
		} 
		//End
		//Delete for test by Jianke.Zhang 02/05
//		if (!Settings.Secure.isLocationProviderEnabled(
//				mContext.getContentResolver(), LocationManager.GPS_PROVIDER)) {
//			Settings.Secure.setLocationProviderEnabled(
//					mContext.getContentResolver(),
//					LocationManager.GPS_PROVIDER, true);
//		}
		//End
//		Tool.toolLog(TAG + " providers : " + mLocationManager.getAllProviders());
		mGpsStatus = mLocationManager.getGpsStatus(null);
//		Tool.toolLog(TAG + " mGpsStatus 565665 " + mGpsStatus);
		try {
			mLocationManager.requestLocationUpdates(
					LocationManager.GPS_PROVIDER, 1000, 0, this);
		} catch (Exception e) {
			Tool.toolLog(TAG + " can't init Location Listener " + e.getMessage());
		}
		mLocationManager.addGpsStatusListener(mGpsStatusListener);
		if(initOpen){
			testCountDownTimer = new TestCountDownTimer(10*SECOND, SECOND, this);
			testCountDownTimer.start();
		}
	}

	@Override
	public void initView() {
		// TODO Auto-generated method stub
		mLayout = (LinearLayout) View.inflate(mContext, R.layout.manu_base_screen,
				null);

		bt_left = (Button) mLayout.findViewById(R.id.bt_left);
		bt_right = (Button) mLayout.findViewById(R.id.bt_right);
		text_top_zone = (TextView) mLayout.findViewById(R.id.text_top_zone);
		text_cen_zone = (TextView) mLayout.findViewById(R.id.text_cen_zone);

		text_top_zone.setText(mName);
		text_cen_zone.setText(text);
		// PR661395-yinbin-zhang-20140516 begin
		/*
		 * bt_left.setText(R.string.fail); bt_right.setText(R.string.pass);
		 * 
		 * bt_left.setVisibility(View.INVISIBLE);
		 * bt_right.setVisibility(View.INVISIBLE); bt_right.setEnabled(false);
		 * bt_left.setOnClickListener(failed_listener);
		 * bt_right.setOnClickListener(pass_listener);
		 */
		bt_left.setText(R.string.pass);
		bt_right.setText(R.string.fail);

		bt_left.setVisibility(View.INVISIBLE);
		bt_right.setVisibility(View.INVISIBLE);
//		Tool.toolLog(TAG + " initView ...");
		bt_left.setEnabled(false);
		bt_left.setOnClickListener(pass_listener);
		bt_right.setOnClickListener(failed_listener);
		// PR661395-yinbin-zhang-20140516 end

		mContext.setContentView(mLayout);
	}

	@Override
	public void destroy() {
		// TODO Auto-generated method stub
		if (testCountDownTimer != null) {
			testCountDownTimer.cancel();
			testCountDownTimer = null;
		}
		init = false;
		mStatusCount = 0;
		text = "";
		if (mLocationManager != null) {
			mLocationManager.removeUpdates(this);
			mLocationManager.removeGpsStatusListener(mGpsStatusListener);
			mLocationManager = null;
		}
	}

	@Override
	public void onLocationChanged(Location location) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onProviderDisabled(String provider) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onProviderEnabled(String provider) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		// TODO Auto-generated method stub

	}

	class GPSStatusListener implements GpsStatus.Listener {

		public void onGpsStatusChanged(int event) {
			Tool.toolLog(TAG + " GPSStatusListener init " + init);
			if (mLocationManager != null && init) {
//				Tool.toolLog(TAG + " GPSStatusListener mGpsStatus 1111111111111");
				mLocationManager.getGpsStatus(mGpsStatus);
				mStatusCount++;
			}
			int numSatellites = 0;
			String SatellitesPRNs = "";
			String SatellitesSNRs = "";
			float AverSNR = 0;
			float SumSNR = 0;
			if (mGpsStatus != null && init) {
//				Tool.toolLog(TAG + " mGpsStatus 22222222");
				Iterator<GpsSatellite> mSatArray = mGpsStatus.getSatellites()
						.iterator();
				while (mSatArray.hasNext()) {
					numSatellites++;
					int PRN = 0;
					float SNR = 0.0f;
					try {
						GpsSatellite mSatellite = mSatArray.next();
						PRN = mSatellite.getPrn();
						SNR = mSatellite.getSnr();
						SumSNR += SNR;
					} catch (NoSuchElementException e) {
						Log.e(TAG, "BUG: Here should be a Satellite!");
					}
					SatellitesPRNs += Integer.toString(PRN)
							+ (mSatArray.hasNext() ? "/" : " ");
					SatellitesSNRs += Float.toString(SNR)
							+ (mSatArray.hasNext() ? "/" : " ");
				}
				AverSNR = SumSNR / numSatellites;
			}
			if (init) {
				init = false;
				text = numSatellites
						+ " satellite "
						+ "\n"
						+ (numSatellites > 0 ? "PRN=" + SatellitesPRNs
								+ "tracked\n" + "SNR=" + SatellitesSNRs
								+ "tracked\n\n" + "AverSNR="
								+ String.format("%.1f", AverSNR) + "\n\n" + "updates = " + mStatusCount
								: "\n" + "updates = " + mStatusCount + "\n\nGo to Outdoor");
				text_cen_zone.setText(text);
				if (numSatellites >= 1) {
					bt_left.setEnabled(true);
					pass();
				}else {
					failed();
				}
			}
		}
	}

	@Override
	public void onFinish() {
		// TODO Auto-generated method stub
		Tool.toolLog(TAG + " onFinish ...");
		init = true;
		text = "0 satellite " + "\n\n\n" + "updates = 0" + "\n\nOpen gps switch";
		text_cen_zone.setText(text);
		bt_left.setVisibility(View.VISIBLE);
		bt_right.setVisibility(View.VISIBLE);
	}

	@Override
	public void onTick() {
		// TODO Auto-generated method stub
		Tool.toolLog(TAG + " onTick ...");
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
	public void finish() {
		// TODO Auto-generated method stub
		
	}
}
