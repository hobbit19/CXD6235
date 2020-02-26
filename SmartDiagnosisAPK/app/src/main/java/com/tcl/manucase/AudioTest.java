package com.tcl.manucase;

import java.io.IOException;

import com.tcl.autotest.AllMainActivity;
import com.tcl.autotest.ExecuteTest;
import com.tcl.autotest.R;
import com.tcl.autotest.autoRunActivity;
import com.tcl.autotest.tool.Tool;
import com.tcl.autotest.utils.DownTimeCallBack;
import com.tcl.autotest.utils.FinishThread;
import com.tcl.autotest.utils.ManuFinishThread;
import com.tcl.autotest.utils.Msg;
import com.tcl.autotest.utils.Test;
import com.tcl.autotest.utils.TestCountDownTimer;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.Resources;
import android.media.AudioManager;
import android.media.AudioSystem;
import android.media.MediaPlayer;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
//import android.media.AudioSystem;
import android.media.AudioFormat;
//import android.os.SystemProperties;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;

public class AudioTest extends Test implements DownTimeCallBack{
	private static final String TAG = "AudioTest";
	private AudioManager am;
	private MediaPlayer mMediaPlayer = null;
	private int loop = 0;
	private final int END = 3;
	String text = "";
	private String isMMI = "false";
	private boolean isEnd = false;
	private boolean audio_flag = true;
	private String tag;
	
	//��ʱ��
	private TestCountDownTimer testCountDownTimer = null;
	
	public AudioTest(ID id, String name) {
		super(id, name);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void setUp() {
		// TODO Auto-generated method stub
//		Tool.toolLog(TAG + " setUp ... ");
		Tool.toolLog(TAG + "_start_test");
		am = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
		mMediaPlayer = new MediaPlayer();
		// am.setMode(AudioSystem.MODE_NORMAL);
//		am.setMode(AudioManager.MODE_NORMAL);
		am.setSpeakerphoneOn(false);
//		am.setMode(AudioManager.MODE_IN_CALL);
		am.setStreamVolume(AudioManager.STREAM_MUSIC,
				am.getStreamMaxVolume(AudioManager.STREAM_MUSIC), 0);
		am.setStreamVolume(AudioManager.STREAM_VOICE_CALL,
				am.getStreamMaxVolume(AudioManager.STREAM_VOICE_CALL), 0);
		try {
			setDataSourceFromResource(mContext.getResources(), mMediaPlayer,
					R.raw.mojito_112_signal_30s_9db);
			startMelody();
			mMediaPlayer.setVolume(1.0f, 1.0f);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		text = "Speaker test";
		
		//������ʱ
		testCountDownTimer = new TestCountDownTimer(SECOND*15, SECOND, this);
//		testCountDownTimer.start();
	}

	private void setDataSourceFromResource(Resources resources,
			MediaPlayer player, int res) throws java.io.IOException {
		AssetFileDescriptor afd = resources.openRawResourceFd(res);
		if (afd != null) {
			player.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(),
					afd.getLength());
			afd.close();
		}
	}



	private void testReceiver(){
		Tool.toolLog(TAG + " testReceiver");
		text = "Receiver test";
		text_cen_zone.setText(text);
		mMediaPlayer.stop();
		mMediaPlayer.release();
		mMediaPlayer = null;
		mMediaPlayer = new MediaPlayer();
		// set receiver max volume
		am.setStreamVolume(AudioManager.STREAM_VOICE_CALL,
				am.getStreamMaxVolume(AudioManager.STREAM_VOICE_CALL), 0);

		// speaker is default, change stream type to receiver if recevier
		// testing
			int mConfig = AudioSystem.getForceUse(AudioSystem.FOR_COMMUNICATION);
			if (mConfig == 1){
				AudioSystem.setForceUse(AudioSystem.FOR_COMMUNICATION, AudioSystem.FORCE_NONE);
			}
			am.setMode(AudioManager.MODE_IN_CALL);
			mMediaPlayer.setAudioStreamType(AudioManager.STREAM_VOICE_CALL);
		try {
			setDataSourceFromResource(mContext.getResources(), mMediaPlayer,
					R.raw.mojito_112_signal_30s_9db);
			mMediaPlayer.prepare();
			mMediaPlayer.start();
			mhandler.sendEmptyMessageDelayed(0x03, 2000);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}


	private void startMelody() throws java.io.IOException,
			IllegalArgumentException, IllegalStateException {
		Tool.toolLog(TAG + " startMelody");
		mMediaPlayer.setLooping(true);
		mMediaPlayer.prepare();
		mMediaPlayer.start();
		Tool.toolLog(TAG + " startMelody end");
		//Add by Jianke.Zhang 01/23
//		new Thread(){
//			public void run(){
//				Tool.sleepTimes(80);
//				loop = 1;
//				pass();
//			}
//		}.start();
		
		mhandler.sendEmptyMessageDelayed(0x02, 5000);
	}

	@Override
	public void initView() {
		// TODO Auto-generated method stub
//		Tool.toolLog(TAG + " initView");
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
		 * bt_left.setOnClickListener(failed_listener);
		 * bt_right.setOnClickListener(pass_listener);
		 */
		bt_left.setText(R.string.pass);
		bt_right.setText(R.string.fail);
		bt_left.setEnabled(false);
		
		bt_left.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if(isEnd){
					pass();
				}else {
					bt_left.setEnabled(false);
					testReceiver();
				}
			}
		});
		bt_right.setOnClickListener(failed_listener);

		mContext.setContentView(mLayout);
	}

	@Override
	public void destroy() {
		// TODO Auto-generated method stub
//		Tool.toolLog(TAG + " finish");
		loop = 0;
		if (true) {
			am.setParameters("SET_LOOPBACK_TYPE=0,1");
		}
		isMMI = "false";
		if (mMediaPlayer != null) {
			mMediaPlayer.stop();
			mMediaPlayer.release();
			mMediaPlayer = null;
		}
		if(testCountDownTimer != null){
			testCountDownTimer.cancel();
			testCountDownTimer = null;
		}
	}

	public void pass1() {
		// TODO Auto-generated method stub
		Tool.toolLog(TAG + " pass " + "loop " + loop);
		switch (loop) {
		case 0:
			mMediaPlayer.reset();
			am.setStreamVolume(AudioManager.STREAM_MUSIC,
					am.getStreamMaxVolume(AudioManager.STREAM_MUSIC), 0);// PR685102-yinbin-zhang-20140606
			// am.setMode(AudioSystem.MODE_NORMAL);
			am.setMode(AudioManager.MODE_NORMAL);
			am.setSpeakerphoneOn(true);
			mMediaPlayer.setVolume(0.5f, 0.5f);
			try {
				setDataSourceFromResource(mContext.getResources(),
						mMediaPlayer, R.raw.mojito_112_signal_30s_9db);
				startMelody();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			text = "Speaker Melody test";
			text_cen_zone.setText(text);
			// isMMI = SystemProperties.get("ro.mmitest");
			if (true) {
				loop++;
			} else {
				loop = END;
			}
			break;
		case 1:
			if (mMediaPlayer != null) {
				mMediaPlayer.stop();
				mMediaPlayer.release();
				mMediaPlayer = null;
			}

			if (true) {
				// am.setMode(AudioSystem.MODE_NORMAL);
				am.setMode(AudioManager.MODE_NORMAL);
				SystemClock.sleep(1000);
				am.setParameters("SET_LOOPBACK_TYPE=1,1");
				text = "loop from MIC test";
			}
			loop++;
			pass1();
			break;
		case 2:
			if (mMediaPlayer != null) {
				mMediaPlayer.stop();
				mMediaPlayer.release();
				mMediaPlayer = null;
			}
			if (true) {
				am.setParameters("SET_LOOPBACK_TYPE=0,1");
				// isMMI = "false";
			}
			if (true) {
//				 am.setMode(AudioSystem.MODE_NORMAL);
				am.setMode(AudioManager.MODE_NORMAL);
				SystemClock.sleep(1000);
				am.setParameters("SET_LOOPBACK_TYPE=3,1");
				text = "loop from Sub-MIC test";
			}
			loop++;
			pass1();
			break;
		case END:
			if (true) {
				am.setParameters("SET_LOOPBACK_TYPE=0,1");
				isMMI = "false";
			}
			mhandler.sendEmptyMessageDelayed(0x02, 1000);

			break;
		}
	}

	//Add by Jianke.Zhang 01/23
	class showThread extends Thread {

		int msginfo;
		String text;
		
		public showThread(int msginfo,String text) {
			this.msginfo = msginfo;
			this.text = text;
		}

		public void run() {
			Message msg = new Message();
			msg.what = this.msginfo;
			msg.obj = text;
			mhandler.sendMessage(msg);
		}
	}
	
	private Handler mhandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			if (msg.what == 0x01) {
				text_cen_zone.setText(msg.obj.toString());

			} else if(msg.what == 0x02){
				bt_left.setEnabled(true);
			} else if(msg.what == 0x03){
				bt_left.setEnabled(true);
				isEnd = true;
			}
		}
	};
	//End

	public String getmContextTag() {
		// TODO Auto-generated method stub
		return TAG;
	}

	@Override
	public void setmContextTag() {
		// TODO Auto-generated method stub
		this.tag = TAG;
	}

	@Override
	public void finish() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onFinish() {
		// TODO Auto-generated method stub
		//�����ʱʱ�䵽���͵�������ʧ��
		Tool.toolLog(TAG + " onFinish");
		audio_flag = false;
		ManuFinishThread tFinishThread = new ManuFinishThread(0x02);
		tFinishThread.start();
		try {
			tFinishThread.join();
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		Msg.exitWithException(mContext, TAG,20, false,"Fail");
	}

	@Override
	public void onTick() {
		// TODO Auto-generated method stub
		
	}

	
}
