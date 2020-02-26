package com.tcl.autocase;

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
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class CarrierSignalTest extends Test implements DownTimeCallBack {
	
	private static String TAG = "CarrierSignalTest";
	
	String text = "searching ...";
	private int mStatusCount = 0;
	private TestCountDownTimer testCountDownTimer = null;
	
	TelephonyManager Tel;  
	MyPhoneStateListener MyListener; 
	   
	private boolean isSuccess = false;
	private boolean isSuccessFinish = false;
	private boolean isFirst = true;
	
	private int index;
	TelephonyManager tm;
	public CarrierSignalTest(ID id, String name) {
		super(id, name);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void setUp() {
		// TODO Auto-generated method stub
		Tool.toolLog(TAG + "_start_test");
		Log.d(TAG, " 33333 _start_test");
		isSuccess = false;
		/* Update the listener, and start it */  
        MyListener = new MyPhoneStateListener();  
        Tel = (TelephonyManager)mContext.getSystemService(Context.TELEPHONY_SERVICE);  
        
        testCountDownTimer = new TestCountDownTimer(10*SECOND, 2*SECOND, this);
		testCountDownTimer.start();
		
	String s = 	getNetworkTypeName(this.mContext);
	Log.i("qinhao", "wangluoleixing:"+s);
	Log.i("qinhao", "33333 网络类型: "+s);
	
	 tm = (TelephonyManager)this.mContext
			.getSystemService(Context.TELEPHONY_SERVICE);
	}

	@Override
	public void initView() {
		// TODO Auto-generated method stub
		mLayout = (LinearLayout) View.inflate(mContext, R.layout.base_screen,
				null);

		text_top_zone = (TextView) mLayout.findViewById(R.id.text_top_zone);
		text_cen_zone = (TextView) mLayout.findViewById(R.id.text_cen_zone);

		text_top_zone.setText(mName);
		text_cen_zone.setText(text);

		mContext.setContentView(mLayout);
		
		Tel.listen(MyListener,PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);
	}

	@Override
	public void destroy() {
		// TODO Auto-generated method stub
		if(MyListener != null || Tel != null){
			Tel.listen(MyListener, PhoneStateListener.LISTEN_NONE);
			MyListener = null;
			Tel = null;
		}
		
		if (testCountDownTimer != null) {
			testCountDownTimer.cancel();
			testCountDownTimer = null;
		}
	}

	@Override
	public void onFinish() {
		// TODO Auto-generated method stub
		Tool.toolLog(TAG + " onFinish ...");
		if(isSuccessFinish){
			return;
		}
		Log.d(TAG, " 33333 SIGNAL STRENGTH POOR, fail");
		text_cen_zone.setText("SIGNAL STRENGTH POOR");
		Tool.fail(mContext, TAG);
	}

	@Override
	public void onTick() {
		// TODO Auto-generated method stub
		Tel.listen(MyListener, PhoneStateListener.LISTEN_NONE);
		
		Tel.listen(MyListener,PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);
	}
	
	class MyPhoneStateListener extends PhoneStateListener {  
      /* Get the Signal strength from the provider, each tiome there is an update  �ӵõ����ź�ǿ��,ÿ��tiome��Ӧ���и���*/  
      
	  @Override  
      public void onSignalStrengthsChanged(SignalStrength signalStrength)  
      {  
		  
		  
		 /* String ssignal = signalStrength.toString();
		  String[] parts = ssignal.split(" ");
		  Log.v("qinhao", "qinhaoppppppp "+parts[1]);*/
		  
         super.onSignalStrengthsChanged(signalStrength);
         Tool.toolLog(TAG + " onSignalStrengthsChanged " + index++);
         Log.d(TAG, " 33333 onSignalStrengthsChanged " + index++);
         String level = null;
         int asu = 0;
         
         if(!isSuccess){
     
        		 String ssignal = signalStrength.toString();
        		
        		 String[] parts = ssignal.split(" ");
        		 int dbm = 0;
        		 if ( tm.getNetworkType() == TelephonyManager.NETWORK_TYPE_LTE){
	        		  dbm = Integer.parseInt(parts[8])*2-113;
	        		  Log.i("qinhao", "dbm:"+dbm);
	        		  asu= Math.abs(dbm);
	        		  Log.i("qinhao", "33333 dbm: "+dbm);
	             	 text_cen_zone.setText(text);
	                  if (asu <= 2 || asu == 99){
	                 	 level = "SIGNAL STRENGTH NONE OR UNKNOWN";
	                  } else if(asu >= 12){
	                 	 level = "SIGNAL STRENGTH GREAT";
	                 	 isSuccess = true;
	                  } else if(asu >= 8){
	                 	 level = "SIGNAL STRENGTH GOOD";
	                 	 isSuccess = true;
	                  } else if(asu >= 5){
	                 	 level = "SIGNAL STRENGTH MODERATE";
	                 	 isSuccess = true;
	                  } else{
	                 	 level = "SIGNAL STRENGTH POOR";
	                  } 
        		 }
        		 else{
        		  /*if (signalStrength.getGsmSignalStrength() != 99) {
        		      int intdbm = -113 + 2
        		        * signalStrength.getGsmSignalStrength();
        		      dbm = intdbm;
        		     }*/
//        			 asu = signalStrength.getGsmSignalStrength();
					 asu = signalStrength.getAsuLevel();
        			 Log.i("qinhao", "33333 asu: "+asu);
                	 text_cen_zone.setText(text);
                     if (asu <= 2 || asu == 99){
                    	 level = "SIGNAL STRENGTH NONE OR UNKNOWN";
                     } else if(asu >= 12){
                    	 level = "SIGNAL STRENGTH GREAT";
                    	 isSuccess = true;
                     } else if(asu >= 8){
                    	 level = "SIGNAL STRENGTH GOOD";
                    	 isSuccess = true;
                     } else if(asu >= 5){
                    	 level = "SIGNAL STRENGTH MODERATE";
                    	 isSuccess = true;
                     } else{
                    	 level = "SIGNAL STRENGTH POOR";
                     } 
        		 }
        		   Message msg = new Message();
                   msg.what = 0x01;
                   msg.obj = level + "\n " + asu;
      	         mHandler.sendMessageDelayed(msg, SECOND);
        		}
        /*	 else
        	 {
        	 asu = signalStrength.getGsmSignalStrength();
        	 text_cen_zone.setText(text);
             if (asu <= 2 || asu == 99){
            	 level = "SIGNAL STRENGTH NONE OR UNKNOWN";
             } else if(asu >= 12){
            	 level = "SIGNAL STRENGTH GREAT";
            	 isSuccess = true;
             } else if(asu >= 8){
            	 level = "SIGNAL STRENGTH GOOD";
            	 isSuccess = true;
             } else if(asu >= 5){
            	 level = "SIGNAL STRENGTH MODERATE";
            	 isSuccess = true;
             } else{
            	 level = "SIGNAL STRENGTH POOR";
             } 
             Message msg = new Message();
             msg.what = 0x01;
             msg.obj = level + "\n " + asu;
	         mHandler.sendMessageDelayed(msg, SECOND);
         }*/
         //}
         if(isSuccess && isFirst){
        	 isSuccessFinish = true;
        	 isFirst = false;
	         Message msg = new Message();
	         msg.what = 0x02;
	         msg.obj = level + "\n " + asu;
	         mHandler.sendMessageDelayed(msg, 2*SECOND);
         }
      }
    }

	Handler mHandler = new Handler(){
		
		public void handleMessage(android.os.Message msg) {
			text_cen_zone.setText(msg.obj.toString());
			switch(msg.what){
			case 0x01:
				break;
			case 0x02:
				Tool.success(mContext, TAG);
				break;
			}
			
		};
	};
	
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
	
	public static String getNetworkTypeName(Context context) {
        if (context != null) {
            ConnectivityManager connectMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            if (connectMgr != null) {
                NetworkInfo info = connectMgr.getActiveNetworkInfo();
                if (info != null) {
                    switch (info.getType()) {
                    case ConnectivityManager.TYPE_WIFI:
                        return "WIFI";
                    case ConnectivityManager.TYPE_MOBILE:
                        return getNetworkTypeName(info.getSubtype());
                    }
                }
            }
        }
        return getNetworkTypeName(TelephonyManager.NETWORK_TYPE_UNKNOWN);
    }

    public static String getNetworkTypeName(int type) {
        switch (type) {
        case TelephonyManager.NETWORK_TYPE_GPRS:
            return "GPRS";
        case TelephonyManager.NETWORK_TYPE_EDGE:
            return "EDGE";
        case TelephonyManager.NETWORK_TYPE_UMTS:
            return "UMTS";
        case TelephonyManager.NETWORK_TYPE_HSDPA:
            return "HSDPA";
        case TelephonyManager.NETWORK_TYPE_HSUPA:
            return "HSUPA";
        case TelephonyManager.NETWORK_TYPE_HSPA:
            return "HSPA";
        case TelephonyManager.NETWORK_TYPE_CDMA:
            return "CDMA";
        case TelephonyManager.NETWORK_TYPE_EVDO_0:
            return "CDMA - EvDo rev. 0";
        case TelephonyManager.NETWORK_TYPE_EVDO_A:
            return "CDMA - EvDo rev. A";
        case TelephonyManager.NETWORK_TYPE_EVDO_B:
            return "CDMA - EvDo rev. B";
        case TelephonyManager.NETWORK_TYPE_1xRTT:
            return "CDMA - 1xRTT";
        case TelephonyManager.NETWORK_TYPE_LTE:
            return "LTE";
        case TelephonyManager.NETWORK_TYPE_EHRPD:
            return "CDMA - eHRPD";
        case TelephonyManager.NETWORK_TYPE_IDEN:
            return "iDEN";
        case TelephonyManager.NETWORK_TYPE_HSPAP:
            return "HSPA+";
        default:
            return "UNKNOWN";
        }
    }
}
