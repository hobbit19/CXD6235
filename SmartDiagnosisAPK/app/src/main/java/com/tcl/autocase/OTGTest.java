package com.tcl.autocase;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.SensorEventListener;
import android.os.BatteryManager;
import android.os.Environment;
import android.os.Handler;
import android.os.storage.StorageEventListener;
import android.os.storage.StorageManager;
import android.os.storage.VolumeInfo;
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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * creat by nanbing.zou for A5A INFINI at 2017-11-23
 */
public class OTGTest extends Test implements DownTimeCallBack {

	private static String TAG = "OTGTest";
	String text = "Please insert OTG cable";
	private TestCountDownTimer testCountDownTimer = null;
	private boolean isGetOTGPlug = false;
	private StorageManager storageManager;
	private StorageEventListener storageEventListener;

	public OTGTest(ID id, String name, Boolean updateFlag, long timeout) {
		super(id, name, updateFlag, timeout);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void setUp() {
		Tool.toolLog(TAG + "_start_test");

		testCountDownTimer = new TestCountDownTimer(1*SECOND, SECOND, this);
		testCountDownTimer.start();

		storageManager = (StorageManager)getContext().getSystemService(Context.STORAGE_SERVICE);
		storageEventListener = new MyStorageEventListener();
		storageManager.registerListener(storageEventListener);
	}

	@Override
	public void initView() {
		// TODO Auto-generated method stub
		mLayout = (LinearLayout) View.inflate(mContext,
				R.layout.base_screen, null);

		text_top_zone = (TextView) mLayout.findViewById(R.id.text_top_zone);
		text_cen_zone = (TextView) mLayout.findViewById(R.id.text_cen_zone);

		text_top_zone.setText(mName);
		text_cen_zone.setText(text);


		mContext.setContentView(mLayout);
	}

	@Override
	public void destroy() {
		// TODO Auto-generated method stub
		storageManager.unregisterListener(storageEventListener);
		if (testCountDownTimer != null) {
			testCountDownTimer.cancel();
			testCountDownTimer = null;
		}
	}


	Handler mHandler = new Handler(){

		public void handleMessage(android.os.Message msg) {

			switch (msg.what){
				case 0:
					mStorageTestThread = new Thread(mStorageTestRunnable);
					mStorageTestThread.start();
					break;
				case 1:
					success();
					break;
			}


		};
	};

	private void success(){
		//�����⵽�Ѿ� ����USBֱ�ӳɹ�
		mContext.setResult(RESULT.PASS.ordinal());
		FinishThread tFinishThread = new FinishThread(0x01,ExecuteTest.temppositon);
		tFinishThread.start();
		try {
			tFinishThread.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		//����ٶ�̫�죬����û���ֽ���

		Msg.exitWithSuccessTest(mContext,TAG, 10,true,"Pass");

		if(AllMainActivity.mainAllTest && !AllMainActivity.autofileFlag){
			Msg.WriteModelResult(mContext,AllMainActivity.all_items_file_text, "PASS");
		}else {
			Msg.WriteModelResult(mContext,autoRunActivity.auto_file_text, "PASS");
		}
	}

	@Override
	public void timeout() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onFinish() {
		// TODO Auto-generated method stub
		Tool.toolLog(TAG + " onFinish ... ");
		if(!isGetOTGPlug){
			text_cen_zone.setText("OTG Time out.");
			//�����ʱ�仹û�м�⵽USB����ô��ʧ��
			mContext.setResult(RESULT.FAILED.ordinal());
			Tool.toolLog(TAG + " index 8887 -> " + ExecuteTest.temppositon);
			int double_test;
			if(AllMainActivity.mainAllTest){
				double_test = AllMainActivity.double_check_for_auto[ExecuteTest.temppositon]++;
			}else {
				double_test = autoRunActivity.double_check[ExecuteTest.temppositon]++;
			}
			Tool.toolLog(TAG + " double_test 9997 -> " + double_test);
			FinishThread tFinishThread = new FinishThread(0x02,ExecuteTest.temppositon);
			tFinishThread.start();
			try {
				tFinishThread.join();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if(double_test==1){
				Msg.exitWithException(mContext,TAG,20,true,"Pass");
				//Write data into file
				if(AllMainActivity.mainAllTest && !AllMainActivity.autofileFlag){
					Msg.WriteModelResult(mContext,AllMainActivity.all_items_file_text, "FAIL");
				}else {
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

	private boolean isUsbStorage(String path) {
		return !path.contains("emulated") && !path.contains("self");
	}

	private class MyStorageEventListener extends StorageEventListener{
		@Override
		public void onStorageStateChanged(String path, String oldState, String newState) {
			super.onStorageStateChanged(path, oldState, newState);

			if(isUsbStorage(path)){
				if (Environment.MEDIA_MOUNTED.equals(newState)){
					mStoragePath = path;
					// start storage test
					isGetOTGPlug = true;
					try {
						if(mStorageTestThread!=null && mStorageTestThread.isAlive()) {
							return;
						}
						mHandler.sendEmptyMessageDelayed(0,15000);
					}
					catch(IllegalThreadStateException e) {
					}
				} else {
					mStoragePath = "";
				}
			}
		}

		@Override
		public void onVolumeStateChanged(VolumeInfo vol, int oldState, int newState) {
			super.onVolumeStateChanged(vol, oldState, newState);
			switch(newState) {
				case VolumeInfo.STATE_MOUNTED:
					mStoragePath = vol.getPath().getAbsolutePath();
					// start storage test
					try {
						if(mStorageTestThread!=null && mStorageTestThread.isAlive()) {
							//mStorageTestThread.interrupt();
							return;
						}
						mStorageTestThread = new Thread(mStorageTestRunnable);
						mStorageTestThread.start();
					}
					catch(IllegalThreadStateException e) {
					}
					break;
				default:
					mStoragePath = "";
					break;
			}
		}
	}

	private boolean mCheckPlug1 = false;
	private boolean mCheckPlug1_RW = false;
	private String mPlug1TestStatus = "";
	private String mPlug2TestStatus = "";
	private Thread mStorageTestThread = null;
	private String mStoragePath = "";
	private Runnable mStorageTestRunnable = new Runnable() {
		@Override
		public void run() {

			if(mCheckPlug1==false){
				boolean result = new ExternStorageTest(mStoragePath).startTest();
				mCheckPlug1 = true;
				mCheckPlug1_RW = result;
				mPlug1TestStatus = (mCheckPlug1_RW ? "OK" : "NOK");

				mHandler.post(new Runnable() {
					@Override
					public void run() {
						String status;
						status = "\nPlug Status: " + (mCheckPlug1 ? "OK" : "");
						status += "\nPlug Status RW: " + mPlug1TestStatus;
						text_cen_zone.setText(status);

						if(mCheckPlug1 && mCheckPlug1_RW){
							success();
						}
					}

				});

			}

		}
	};

	private class ExternStorageTest {
		private String TAG = "OTGTest.ExternStorageTest";
		private static final byte VALUE_TEST_BYTE = 9;
		private boolean isReadDataSame;
		private String mPath;

		public ExternStorageTest(String path) {
			mPath = path;
		}

		public boolean startTest() {
			isReadDataSame = false;
			return startFileWriteReadTest();

		}


		private boolean startFileWriteReadTest() {
			String filePath = mPath+"/test.txt";
			File externalFile = null;
			FileInputStream externalFileInputStream = null;
			FileOutputStream externalFileOutputStream = null;

			try {
				externalFile = setExternalFileCreate(filePath);
				externalFileOutputStream = setExternalFileWrite(externalFile);
				Tool.toolLog(TAG+"write");
				externalFileInputStream = setExternalFileRead(externalFile);
				Tool.toolLog(TAG+"read");
				externalFile = setExternalFileDelete(externalFile);
			} catch (FileNotFoundException e) {
				Tool.toolLog(TAG+e + ", externalFile: " + externalFile);
				return false;
			} catch (IOException e) {
				Tool.toolLog(TAG+e.toString());
				return false;
			} catch (Exception e) {
				Tool.toolLog(TAG+e.toString());
				return false;
			} finally {
				try {
					externalFileInputStream.close();
					externalFileOutputStream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}

			}
			return isReadDataSame;
		}

		private File setExternalFileCreate(String path) throws IOException {
			File externalFile = new File(path);

			if (!externalFile.exists()) {
				Tool.toolLog(TAG+"before create!");
				externalFile.createNewFile();
				Tool.toolLog(TAG+"end create!");
			}
			if (externalFile.exists()) {
				Tool.toolLog(TAG+"create sicess!");
			}else
				Tool.toolLog(TAG+"create fail!");
			return externalFile;
		}

		private File setExternalFileDelete(File externalFile) throws IOException {
			if (externalFile.exists()) {
				externalFile.delete();
			}
			return externalFile;
		}

		private FileOutputStream setExternalFileWrite(File externalFile)
				throws FileNotFoundException, IOException {
			FileOutputStream externalFileOutputStream;
			externalFileOutputStream = new FileOutputStream(externalFile);
			externalFileOutputStream.write(VALUE_TEST_BYTE);
			externalFileOutputStream.close();
			return externalFileOutputStream;
		}

		private FileInputStream setExternalFileRead(File externalFile)
				throws FileNotFoundException, IOException {
			FileInputStream externalFileInputStream;
			externalFileInputStream = new FileInputStream(externalFile);
			isReadDataSame = (externalFileInputStream.read() == VALUE_TEST_BYTE);
			externalFileInputStream.close();
			return externalFileInputStream;
		}
	}

}
