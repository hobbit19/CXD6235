package com.tcl.autocase;


import android.content.Context;
import android.hardware.SerialManager;
import com.tcl.autotest.utils.SerialPort;

import android.os.Handler;
import android.os.Message;
import android.view.View;
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

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import android.hardware.ConsumerIrManager;


public class IRTest extends Test implements DownTimeCallBack {
	private final static String TAG = "IRTest";
	String text = "";
    private TestCountDownTimer testCountDownTimer = null;
	public static final int BAUDRATE = 230400;
	public static  String SERIALPORT = "ttyMT1";
	private String _portName = "";
	protected SerialPort mSerialPort;
	/** The _ file input stream. */
	private FileInputStream _FileInputStream;
	public static byte[] NEVO_CMD_WAKEUP = new byte[]{(byte) 0x0};

	//Morgan 4G
	int Freq = 38000;
	private int[] pattern = { 4048, 4048, 528, 2000, 528, 2000, 528, 2000,
								528, 2000, 528, 1008, 528, 1008, 528, 2000, 528, 1008, 528, 2000,
								528, 1008, 528, 2000, 528, 1008, 528, 1008, 528, 1008, 528, 1008,
								528, 1008, 528, 2000, 528, 2000, 528, 1008, 528, 2000, 528, 1008,
								528, 2000, 528, 1008, 528, 2000, 480, 40000 };
	ConsumerIrManager irSend;
	//Morgan 4G

	/** The _ file output stream. */
	private FileOutputStream _FileOutputStream;

	public IRTest(ID id, String name) {
		super(id, name);
		// TODO Auto-generated constructor stub

	}

	public IRTest(ID id, String name, Boolean updateFlag) {
		super(id, name, updateFlag);
		// TODO Auto-generated constructor stub
	}

	public IRTest(ID id, String name, Boolean updateFlag, long timeout) {
		super(id, name, updateFlag, timeout);
		// TODO Auto-generated constructor stub
	}


	@Override
	public void setUp() {
		// TODO Auto-generated method stub
		Test.gettag = TAG;
		Test.state = null;
		Tool.toolLog(TAG + "_start_test");
		if(AllMainActivity.deviceName.equals("Morgan_4G")){
			irServiceSend();
			myHandler.sendEmptyMessage(0);
			testCountDownTimer = new TestCountDownTimer(20*SECOND, SECOND, this);
			testCountDownTimer.start();
			return;
		}
		try {
			initSerialPort();
		} catch (IOException e) {
			e.printStackTrace();
		}
		startSendKey();

		testCountDownTimer = new TestCountDownTimer(20*SECOND, SECOND, this);
		testCountDownTimer.start();
	}
	private void irServiceSend(){
		irSend = (ConsumerIrManager) mContext.getSystemService(Context.CONSUMER_IR_SERVICE);
		if (irSend==null){
			Tool.toolLog(TAG+" Cannot find Consumer IR ");
			return;
		}
		if(!irSend.hasIrEmitter()){
			Tool.toolLog(TAG + " Missing IR emitter ");
			return;
		}
		Tool.toolLog(TAG +" begin irSend.transmit ");
		irSend.transmit(Freq,pattern);
		Tool.toolLog(TAG + " end irSend.transmit ");
	}

	private void initSerialPort()throws SecurityException, IOException {
		File serialPortFile = null;
		this._portName = "/dev/"+SERIALPORT;

		Tool.toolLog(TAG +"initSerialPort"+"open serial port: " + this._portName +  " - " + BAUDRATE);
		try
		{
			serialPortFile = new File(this._portName);
		}catch(Exception ex){
			ex.printStackTrace();
		}

		mSerialPort = new SerialPort(serialPortFile, BAUDRATE,0);
		if(serialPortFile != null && serialPortFile.canRead() && serialPortFile.canWrite()){
			try	{
				Tool.toolLog(TAG+"serialPortFile can be execute");
				_FileInputStream = mSerialPort.getInputStream();
				_FileOutputStream = mSerialPort.getOutputStream();
				//IRBlasterTestActivity.Singleton.output("Serial port opened successful");
			}catch(Exception ex){
				ex.printStackTrace();
				Tool.toolLog(TAG+"serialPortFile Exception"+ex);
			}
		}else{
			Tool.toolLog(TAG+"serialPortFile no permission");
			throw new IOException("no permission on read/write serial port!");
		}
	}
	private void startSendKey() {
		new Thread() {
			@Override
			public void run() {
				super.run();
				try {
					// wakeup
					writeBytes(NEVO_CMD_WAKEUP);
					Thread.sleep(50);
					// send data
					writeBytes(new byte[] { 0x40, 0x41, 0x42, 0x43, 0x00,
							0x08, 0x01, 0x00, 0x03, (byte) 0x2a, 0x01,
							(byte) 0x80, 0x00, 0x00 });
				} catch (InterruptedException e) {
				}
			}
		}.start();

	}
	public boolean writeBytes(byte[] buffer) {
		String str = getString(buffer);
		Tool.toolLog(TAG +"Buffer: "+ str);
		boolean success = false;
			try	{
				Tool.toolLog(TAG +"write try");
				if(_FileOutputStream != null){
					Tool.toolLog(TAG +"write before");
					_FileOutputStream.write(buffer);
					Tool.toolLog(TAG +"write after");
					success = true;
					myHandler.sendEmptyMessage(0);//onSucess();
				}
			}catch(Exception ex){
				ex.printStackTrace();
				Tool.toolLog(TAG +"write Exception");
			}finally{
				try {
					if(_FileOutputStream != null)
						_FileOutputStream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

		return success;
	}
	public String getString(byte[] data) {
		String text = "";

		if (data != null) {
			int count = 0;
			StringBuilder sb = new StringBuilder();
			for (byte b : data) {
				sb.append("0x");
				sb.append(Integer.toHexString((int) (b & 0x00FF)));
				sb.append(" ");
				count++;
				if (count >= 10) {
					count = 0;
					text += sb.toString();
					sb = new StringBuilder();
				}
			}
			text += sb.toString();
		}

		return text;
	}


	public Handler myHandler = new Handler(){
		@Override
		public void handleMessage(Message msg) {

			switch (msg.what){
				case 0:
					onSucess();
					break;
				case 1:
					onFail();
					break;
			}

			super.handleMessage(msg);
		}
	};

	@Override
	public void initView() {
		// TODO Auto-generated method stub
		mLayout = (LinearLayout) View.inflate(mContext, R.layout.base_screen,null);

//		bt_left = (Button) mLayout.findViewById(R.id.bt_left);
//		bt_right = (Button) mLayout.findViewById(R.id.bt_right);
		text_top_zone = (TextView) mLayout.findViewById(R.id.text_top_zone);
		text_cen_zone = (TextView) mLayout.findViewById(R.id.text_cen_zone);

		text_top_zone.setText(mName);
		text_cen_zone.setText(text);

//		bt_left.setText(R.string.pass);
//		bt_right.setText(R.string.fail);
//		bt_right.setEnabled(false);
//		bt_left.setEnabled(false);
//		bt_left.setOnClickListener(pass_listener);
//		bt_right.setOnClickListener(failed_listener);
		//PR661395-yinbin-zhang-20140516 end

		mContext.setContentView(mLayout);
//		Tool.toolLog(TAG + " BluetoothTest 0000000000");
	}

	public void finish() {
		// TODO Auto-generated method stub
//		Tool.toolLog(TAG + " BluetoothTest finish");

		if(testCountDownTimer!=null){
			testCountDownTimer.cancel();
		}
	}

	//public static String state = null ;
//	@Override
	public void onSucess() {
		// TODO Auto-generated method stub
//		Tool.toolLog(TAG + " onFinish");
		Test.state = "Pass";
		Tool.toolLog(TAG + " Test.state " + Test.state);
		FinishThread  finishThread = new FinishThread(0x01,ExecuteTest.temppositon);
		finishThread.start();
		try {
			finishThread.join();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		/*Jianke
		 * ���Ҳ����Ҫͬ��*/
		Msg.exitWithSuccessTest(mContext, TAG, 10,true,"Pass");
		//Write data into file
		if(AllMainActivity.mainAllTest && !AllMainActivity.autofileFlag){
			Msg.WriteModelResult(mContext,AllMainActivity.all_items_file_text, "PASS");
		}else{
			Msg.WriteModelResult(mContext,autoRunActivity.auto_file_text, "PASS");
		}
		//End
	}

	public void onFail(){
//		Tool.toolLog(TAG + " onFail");
		//Begin Add By Jianke.Zhang 2015/01/14
		//Write data into file
		Test.state = "Fail";
		mContext.setResult(RESULT.FAILED.ordinal());
		Tool.toolLog(TAG + " index 8882 -> " + ExecuteTest.temppositon);
		int double_test;
		if(AllMainActivity.mainAllTest){
			double_test = AllMainActivity.double_check_for_auto[ExecuteTest.temppositon]++;
			//AllMainActivity.mainAllTest = false;
		}else {
			double_test = autoRunActivity.double_check[ExecuteTest.temppositon]++;
		}
		Tool.toolLog(TAG + " double_test 9992-> " + double_test);
	
		FinishThread tFinishThread = new FinishThread(0x02,ExecuteTest.temppositon);
		tFinishThread.start();
		try {
			tFinishThread.join();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		/*Jianke
		 * ���Ҳ����Ҫͬ��*/
		if(double_test==1){
			Msg.exitWithException(mContext,TAG,50,true,"Pass");
			if(AllMainActivity.mainAllTest && !AllMainActivity.autofileFlag){
				Msg.WriteModelResult(mContext,AllMainActivity.all_items_file_text, "FAIL");
			}else {
				Msg.WriteModelResult(mContext,autoRunActivity.auto_file_text, "FAIL");
			}
		}else{
			Msg.exitWithException(mContext,TAG,50,true,"Fail");
		}
		//End
	}
	
	@Override
	public void onTick() {
		// TODO Auto-generated method stub
//		Tool.toolLog(TAG + " onTick ...");
	}

	@Override
	public void onFinish() {
		// TODO Auto-generated method stub
		Tool.toolLog(TAG + " onFinish ...");
		onFail();
	}

	public String getmContextTag() {
		// TODO Auto-generated method stub
		return TAG;
	}

	public void setmContextTag() {
		// TODO Auto-generated method stub
	}
	
	@Override
	public void destroy() {
	    finish();
	}
	
}
