package com.tcl.manucase;

import java.io.IOException;

import com.android.fmradio.FmNative;
import com.tcl.autotest.R;
import com.tcl.autotest.tool.Tool;
import com.tcl.autotest.utils.Test;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.AssetFileDescriptor;
import android.content.res.Resources;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.os.SystemProperties;
//import android.media.AudioSystem;
//import com.android.fmradio.FmNative;
import android.view.Window;
import com.tct.fmradio.device.FMDevice;
import com.tct.fmradio.device.FMDeviceListener;

public class FMTest extends Test {
	
	private final static String TAG = "FMTest";
	String text = ""; 
	private int FIXED_STATION_FREQ = 1036; // 1036 * 100k Hz
	private String FM_RADIO_FREQ = "FMRadio freq 103.6 MHz";
	private final HeadInsertBroadcastReceiver mHeadsetStatus = new HeadInsertBroadcastReceiver();
	private final int UNKNOWN = -1, OUT = 0, IN = 1;
	private int loop = 0;
	private AudioManager am;
	private float mVol = 1.0f;
	private MediaPlayer mMediaPlayer;
	private String isMMI = "false";
	private boolean isFMon = false;
	private boolean init = false;
	private int rssi;
	private final int HEADSET_LEFT = 0;
	private final int HEADSET_RIGHT = 1;
	private final int HEADSET_LOOP = 2;
	private final int HEADSET_FM = 3;
	private final int HEADSET_REMOVE = 4;
	private final int END = 5;
	public final static int QCOM_FM = 100;
	public final static int MTK_FM = 101;
	public static int mCurrentFM = 0;
	public static final int DEFAULT_FREQUENCY = 87500;
	private int mCurrentFreq = DEFAULT_FREQUENCY;
	private static FMDevice mFMDevice = null;

	private boolean isMTKPlatform() {
		try {
			String platform = SystemProperties.get("ro.mediatek.platform");
			if (platform.startsWith("MT")) {
				return true;
			}
		} catch (Exception e) {
			Tool.toolLog(TAG + "It's not MTK platform");
		}
		return false;
	}
	private boolean isQcomPlatform() {
		try {
			Class<?> managerClass = Class.forName("qcom.fmradio.FmConfig");
			if (managerClass.getClass() != null) {
				return true;
			}
		} catch (ClassNotFoundException e) {
			Tool.toolLog(TAG + "Can't find the class 'qcom.fmradio.FmConfig',maybe it's not Qcom platform.");
		} catch (LinkageError e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}
	public void setmCurrentFM() {
		if (isMTKPlatform()) {
			Tool.toolLog(TAG + "MTK platform!");
			mCurrentFM = MTK_FM;
		} else if (isQcomPlatform()) {
			Tool.toolLog(TAG + "Qcom platform!");
			mCurrentFM = QCOM_FM;
		} else {
			Tool.toolLog(TAG + "Don't get the definite platform!");
			mCurrentFM = 0;
		}
	}
	
	public FMTest(ID id, String name) {
		super(id, name);
		// TODO Auto-generated constructor stub
	}
	
	private void playMelody(Resources resources, int res) {
		// player.setAudioStreamType(AudioManager.STREAM_MUSIC);
		AssetFileDescriptor afd = resources.openRawResourceFd(res);

		try {

			if (afd != null) {
				mMediaPlayer.setDataSource(afd.getFileDescriptor(),
						afd.getStartOffset(), afd.getLength());
				afd.close();
			}

			mMediaPlayer.setLooping(true);
			mMediaPlayer.prepare();
			mMediaPlayer.start();
		} catch (IOException e) {
			Log.e(TAG, "can't play melody cause:" + e);
		}
	}

	private void PlayFM() {
		boolean bRet = false;
		mContext.setVolumeControlStream(AudioManager.STREAM_RING/*STREAM_FM*/);
		if (mMediaPlayer == null) {
			mMediaPlayer = new MediaPlayer();
		} else {
			mMediaPlayer.reset();
		}
		try {
			mMediaPlayer.setDataSource("MEDIATEK://MEDIAPLAYER_PLAYERTYPE_FM");
		} catch (IOException ex) {
			// TODO: notify the user why the file couldn't be opened
			Log.e(TAG, "setDataSource: " + ex);
			// return;
		} catch (IllegalArgumentException ex) {
			// TODO: notify the user why the file couldn't be opened
			Log.e(TAG, "setDataSource: " + ex);
			// return;
		} catch (IllegalStateException ex) {
			Log.e(TAG, "setDataSource: " + ex);
			// return;
		}
		mMediaPlayer.setAudioStreamType(AudioManager.STREAM_RING/*STREAM_FM*/);

		bRet = FmNative.openDev();
		Tool.toolLog(TAG + " --- FmNative.openDev: " + bRet);

		FmNative.setMute(true);
		FmNative.powerUp((float) FIXED_STATION_FREQ / 10);

		try {
			mMediaPlayer.prepare();
		} catch (Exception e) {
			Tool.toolLog(TAG + " Exception: Cannot call MediaPlayer prepare.");
		}
	
		if(mMediaPlayer!=null) {
			AudioManager am = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
			if(false)//((SystemProperties.get("ro.mmitest")).equals("true"))//PR713430-yinbin.zhang
			{
				am.setStreamVolume(AudioManager.STREAM_RING/*STREAM_FM*/,11,0);
			}
			else
			{
				am.setStreamVolume(AudioManager.STREAM_RING/*STREAM_FM*/,15,0);
			}
			am.setSpeakerphoneOn(false);
			am.setWiredHeadsetOn(true);
		}

		mMediaPlayer.start();
		FmNative.setMute(false);
	}

	void StopFM() {

		FmNative.setMute(true);
		mMediaPlayer.stop();
		mMediaPlayer.release();
		mMediaPlayer = null;
		FmNative.powerDown(0);
		FmNative.closeDev();
	}
	
	@Override
	public void pass() {
		// TODO Auto-generated method stub
		switch (loop) {
		/*case HEADSET_LEFT:
			text = "Headset left discrete test";
			text_cen_zone.setText(text);
			if (mMediaPlayer != null) {
				am = (AudioManager) mContext
						.getSystemService(Context.AUDIO_SERVICE);
				{
					am.setStreamVolume(AudioManager.STREAM_MUSIC, 15, 0);
					am.setStreamVolume(AudioManager.STREAM_RING, 15, 0);
				}
				am.setSpeakerphoneOn(false);
				am.setWiredHeadsetOn(true);

				mMediaPlayer.setVolume(mVol, 0.000f);
				playMelody(mContext.getResources(), R.raw.mojito_112_signal_30s_9db);
			}
			loop ++;
			break;
		case HEADSET_RIGHT:
			text = "Headset right discrete test";
			text_cen_zone.setText(text);
			if (mMediaPlayer != null) {
				mMediaPlayer.setVolume(0.000f, mVol);
			}
//			isMMI = SystemProperties.get("ro.mmitest");
			if (isMMI.equals("true")) {
				loop++;
			} else {
				loop = HEADSET_FM;
			}
			break;
		case HEADSET_LOOP:
			if (mMediaPlayer != null) {
				mMediaPlayer.stop();
				mMediaPlayer.release();
				mMediaPlayer = null;
			}
			
			if (isMMI.equals("true")) {
				text = "Headset Mic loop";
				text_cen_zone.setText(text);
				am.setStreamMute(AudioManager.STREAM_MUSIC, false);
				am.setStreamMute(AudioManager.STREAM_NOTIFICATION, false);
				am.setStreamMute(AudioManager.STREAM_RING, false);
//				am.setMode(AudioSystem.MODE_NORMAL);
				SystemClock.sleep(1000);
				am.setParameters("SET_LOOPBACK_TYPE=2,2");
			}
			loop ++;
			break;*/
		case HEADSET_FM:
			PlayFM();
			isFMon = true;
			rssi = FmNative.readRssi();
			text = FM_RADIO_FREQ+"\nrssi: "+rssi;
			text_cen_zone.setText(text);
			loop ++;
			
			break;
		case HEADSET_REMOVE:
			if(isFMon) {
				StopFM();
				isFMon = false;
			}
			bt_left.setEnabled(false);
			text = "Please remove headset";
			text_cen_zone.setText(text);
			loop = END;
			break;
		case END:	
			super.pass();
			break;
		}
	}

	@Override
	public void setUp() {
		// TODO Auto-generated method stub
		Tool.toolLog(TAG + "_start_test");
		text = "Please insert headset";
		init = false;
		IntentFilter intentFilter = new IntentFilter(
				Intent.ACTION_HEADSET_PLUG);
		mContext.registerReceiver(mHeadsetStatus, intentFilter);
		mMediaPlayer = new MediaPlayer();
//		setmCurrentFM();
	}

	@Override
	public void initView() {
		// TODO Auto-generated method stub
		mLayout = (LinearLayout)View.inflate(mContext,R.layout.manu_base_screen, null);
  
		bt_left = (Button) mLayout.findViewById(R.id.bt_left);
		bt_right = (Button) mLayout.findViewById(R.id.bt_right);
		text_top_zone = (TextView)mLayout.findViewById(R.id.text_top_zone);
		text_cen_zone = (TextView)mLayout.findViewById(R.id.text_cen_zone);

		text_top_zone.setText(mName);
		text_cen_zone.setText(text);
		//PR661395-yinbin-zhang-20140516 begin
		/*bt_left.setText(R.string.fail); 
		bt_right.setText(R.string.pass);*/
		bt_left.setText(R.string.pass); 
		bt_right.setText(R.string.fail);
                //PR502134-yancan-zhu-20130812 begin
		//bt_right.setEnabled(false);
                //PR502134-yancan-zhu-20130812 end
		bt_left.setEnabled(false);
		bt_left.setOnClickListener(pass_listener);
		bt_right.setOnClickListener(failed_listener);
		//PR661395-yinbin-zhang-20140516 end
		mContext.setContentView(mLayout);
	}

	@Override
	public void destroy() {
		// TODO Auto-generated method stub
		if(isMMI.equals("true"))  {
			am.setParameters("SET_LOOPBACK_TYPE=0,2");
		}
		if(isFMon) {
			StopFM();
		}
		init = false;
		isFMon = false;
		isMMI = "false";
		if (mMediaPlayer != null) {
			mMediaPlayer.stop();
			mMediaPlayer.release();
			mMediaPlayer = null;
		}
		try {
			mContext.unregisterReceiver(mHeadsetStatus);
		} catch (Exception e) {
			Tool.toolLog(TAG + " unregister failed " + e);
		}
	}
	
	class HeadInsertBroadcastReceiver extends BroadcastReceiver {

		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (action.equals(Intent.ACTION_HEADSET_PLUG)) {
				int mPlugStatus = intent.getIntExtra("state", UNKNOWN);
				if (mPlugStatus == IN) {
					init = true;
					loop = 3;
					text = "Headset detected";
					text_cen_zone.setText(text);
					bt_left.setEnabled(true);
					pass();
				} else {
//					if (init && loop != END) {
//						failed();
//					} 
					if (init && loop == END) {
						bt_left.setEnabled(true);
					}
				}
			}
		}

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
