package com.tcl.manucase;

import java.util.LinkedList;

import com.tcl.autotest.R;
import com.tcl.autotest.tool.Tool;
import com.tcl.autotest.utils.DownTimeCallBack;
import com.tcl.autotest.utils.Test;
import com.tcl.autotest.utils.TestCountDownTimer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.os.BatteryManager;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MICTest extends Test implements DownTimeCallBack {

	private static String TAG = "MICTest";
	String text = "Mic Test";
	private TestCountDownTimer testCountDownTimer = null;

	IntentFilter inf = null;

	/**
	 * AudioRecord д�뻺������С
	 */
	protected int m_in_buf_size;
	/**
	 * ¼����Ƶ����
	 */
	private AudioRecord m_in_rec;
	/**
	 * ¼����ֽ�����
	 */
	private byte[] m_in_bytes;
	/**
	 * ���¼���ֽ�����Ĵ�С
	 */
	private LinkedList<byte[]> m_in_q;
	/**
	 * AudioTrack ���Ż����С
	 */
	private int m_out_buf_size;
	/**
	 * ������Ƶ����
	 */
	private AudioTrack m_out_trk;
	/**
	 * ���ŵ��ֽ�����
	 */
	private byte[] m_out_bytes;
	/**
	 * ¼����Ƶ�߳�
	 */
	private Thread record;
	/**
	 * ������Ƶ�߳�
	 */
	private Thread play;
	/**
	 * ���߳�ֹͣ�ı�־
	 */
	private boolean flag = true;
	
	public MICTest(ID id, String name, Boolean updateFlag, long timeout) {
		super(id, name, updateFlag, timeout);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void setUp() {
		Tool.toolLog(TAG + "_start_test");
		init();
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

		bt_left.setText(R.string.pass);
		bt_right.setText(R.string.fail);
		bt_left.setEnabled(false);

		bt_left.setOnClickListener(pass_listener);
		bt_right.setOnClickListener(failed_listener);

		mContext.setContentView(mLayout);
		
		//testCountDownTimer = new TestCountDownTimer(20*SECOND, SECOND, this);
		//testCountDownTimer.start();
		
		record = new Thread(new recordSound());
		play = new Thread(new playRecord());
		// ����¼���߳�
		record.start();
		// ���������߳�
		play.start();
	}

	@Override
	public void destroy() {
		// TODO Auto-generated method stub
		
		if (testCountDownTimer != null) {
			//testCountDownTimer.cancel();
			//testCountDownTimer = null;
		}
		
		flag = false;
		m_in_rec.stop();
		m_in_rec = null;
		m_out_trk.stop();
		m_out_trk = null;

	}


	@Override
	public void timeout() {
		// TODO Auto-generated method stub
		text_cen_zone.setText("Time out.");
		//finish();
	}

	@Override
	public void onFinish() {
		// TODO Auto-generated method stub
//		text_cen_zone.setText(text);
	}

	@Override
	public void onTick() {
		// TODO Auto-generated method stub
		Tool.toolLog(TAG + " onTick ...");
	}

	private void init()
	{
		// AudioRecord �õ�¼����С�������Ĵ�С
		m_in_buf_size = AudioRecord.getMinBufferSize(8000,
				AudioFormat.CHANNEL_CONFIGURATION_MONO,
				AudioFormat.ENCODING_PCM_16BIT);
		// ʵ����������Ƶ����
		m_in_rec = new AudioRecord(MediaRecorder.AudioSource.MIC, 8000,
				AudioFormat.CHANNEL_CONFIGURATION_MONO,
				AudioFormat.ENCODING_PCM_16BIT, m_in_buf_size);
		// ʵ����һ���ֽ����飬����Ϊ��С�������ĳ���
		m_in_bytes = new byte[m_in_buf_size];
		// ʵ����һ��������������ֽ�����
		m_in_q = new LinkedList<byte[]>();

		// AudioTrack �õ�������С�������Ĵ�С
		m_out_buf_size = AudioTrack.getMinBufferSize(8000,
				AudioFormat.CHANNEL_CONFIGURATION_MONO,
				AudioFormat.ENCODING_PCM_16BIT);
		// ʵ����������Ƶ����
		m_out_trk = new AudioTrack(AudioManager.STREAM_MUSIC, 8000,
				AudioFormat.CHANNEL_CONFIGURATION_MONO,
				AudioFormat.ENCODING_PCM_16BIT, m_out_buf_size,
				AudioTrack.MODE_STREAM);
		// ʵ����һ������Ϊ������С�����С���ֽ�����
		m_out_bytes = new byte[m_out_buf_size];
	}
	
	class recordSound implements Runnable
	{
		@Override
		public void run()
		{
			Tool.toolLog(TAG + " ........recordSound run()......");
			byte[] bytes_pkg;
			// ��ʼ¼��
			m_in_rec.startRecording();

			while (flag)
			{
				m_in_rec.read(m_in_bytes, 0, m_in_buf_size);
				bytes_pkg = m_in_bytes.clone();
				Tool.toolLog(TAG +  "........recordSound bytes_pkg==" + bytes_pkg.length);
				if (m_in_q.size() >= 2)
				{
					m_in_q.removeFirst();
				}
				m_in_q.add(bytes_pkg);
			}
		}

	}
	
	class playRecord implements Runnable
	{
		@Override
		public void run()
		{
			// TODO Auto-generated method stub
			Tool.toolLog(TAG + " ........playRecord run()......");
			byte[] bytes_pkg = null;
			// ��ʼ����
			m_out_trk.play();

			while (flag)
			{
				try
				{
					m_out_bytes = m_in_q.getFirst();
					bytes_pkg = m_out_bytes.clone();
					m_out_trk.write(bytes_pkg, 0, bytes_pkg.length);
				} catch (Exception e)
				{
					e.printStackTrace();
				}
				Message sMsg = new Message();
				sMsg.what = 0x01;
				mHandler.sendMessage(sMsg);
			}
		}
	}
	
	private Handler mHandler = new Handler(){
		
		@Override
		public void handleMessage(Message msg) {
			if (msg.what == 0x01) {
				text_cen_zone.setText("Record and play Ok!");
				bt_left.setEnabled(true);
			}
		}
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
	
}


