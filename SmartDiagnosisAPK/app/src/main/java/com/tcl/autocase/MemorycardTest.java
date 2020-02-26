package com.tcl.autocase;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

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

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Environment;
import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class MemorycardTest extends Test implements DownTimeCallBack {
	private static final String TAG = "MemorycardTest";
	String text = "";
	private boolean mSdInserted = false;
	private int flag = 1;// PR661437-yinbin-zhang-20140519
	private String content = "ABCED";// PR661437-yinbin-zhang-20140519
	StorageManager storagemanager = null;
	private TestCountDownTimer testCountDownTimer = null;
	private String sdpath = "/storage/sdcard1/sdtest.txt";//"/mnt/sdcard2/sdtest.txt";
	public static boolean sysSdCard = true;
	private int sdkversion;
	public MemorycardTest(ID id, String name) {
		super(id, name);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void setUp() {
		// TODO Auto-generated method stub
		Tool.toolLog(TAG + "_start_test");
		//"/mnt/sdcard2";
		String mountPoint = "/storage/sdcard1";
		testCountDownTimer = new TestCountDownTimer(3*SECOND, SECOND, this);
		if (storagemanager == null) {
			storagemanager = (StorageManager) mContext
					.getSystemService(Context.STORAGE_SERVICE);
		}

		// Add by Jianke.Zhang 01/30
	/*	Class<?> c = null;
		Method[] methods = null;
		String sm = null;
		try {
			c = Class.forName(storagemanager.getClass().getName());
			Method m;
			m = c.getDeclaredMethod("getVolumeState",java.lang.String.class);
//			Tool.toolLog(TAG + " mmmm " + m);
			m.setAccessible(true);
			sm = (String) m.invoke(storagemanager, mountPoint);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Tool.toolLog(TAG + " eeeeeeeee " + e.getMessage());
		} 

		if (sm
				.equals(Environment.MEDIA_MOUNTED)) {
			mSdInserted = true;
		} else {
			mSdInserted = false;
		}

		
		if (mSdInserted) {
			text = "SD card already mounted";
		} else {
			// text = "No SD card";
			text = "SD Card can not mounted";// PR661437-yinbin-zhang-20140519
		}*/
		
		sdkversion=android.os.Build.VERSION.SDK_INT;
    	Log.v("qinhao", "33333 sdkversion: "+sdkversion);
    	if (sdkversion >= 23) {
    	//if (sdkversion >= 16) {
    		try {
    			Method getVolumeList = storagemanager.getClass().getMethod("getVolumeList");
				Class<?> storageVolumeClazz = Class.forName("android.os.storage.StorageVolume");
				Method getPath = storageVolumeClazz.getMethod("getPath");
				Method isRemovable = storageVolumeClazz.getMethod("isRemovable");
				Method getState = null;

				//if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT) {
				if (Build.VERSION.SDK_INT > 19) {
					try {
						getState = storageVolumeClazz.getMethod("getState");
					} catch (NoSuchMethodException e) {
						e.printStackTrace();
					}
				}
	    		
				Object invokeVolumeList = getVolumeList.invoke(storagemanager);
				int length = Array.getLength(invokeVolumeList);
				ArrayList<StorageBean> list = new ArrayList<StorageBean>();
				Log.d(TAG, "33333 storageVolumeClazz length: " + length);
				
				boolean hasMountedSdcard = false;
				for (int i=0; i<length; i++) {
					Object storageVolume = Array.get(invokeVolumeList, i);
					String path = (String) getPath.invoke(storageVolume);
					boolean removable = (Boolean) isRemovable.invoke(storageVolume);
					String state = null;
					if (getState != null) {
						state = (String) getState.invoke(storageVolume);
						if (removable) {
							if (Environment.MEDIA_MOUNTED.equals(state)) {
								Log.d(TAG, "33333 external storage: " + path + " is mounted");
								long totalSize = StorageUtils.getTotalSize(path);
								long availableSize = StorageUtils.getAvailableSize(path);
								Log.d(TAG, "33333 external storage totalSize: " + totalSize + ", availableSize: " + availableSize);
								
								hasMountedSdcard = true;
							} else {
								Log.d(TAG, "33333 external storage: " + path + " is NOT mounted");
							}
						}
					} else {
						Log.d(TAG, "33333 getState() method is null for path: " + path + ", removable: " + removable);
					}
					
					//Tool.toolLog(TAG + "33333 storageVolumeClazz path: " + path + ", removable: " + removable);
				}

				if (hasMountedSdcard) {
					sysSdCard = false;
		    		Log.i("qinhao", "33333 sysSdCard:"+sysSdCard);
		    		text = "SD card already mounted";
				} else {
					Log.i("qinhao", "33333 sysSdCard:"+sysSdCard);
		    		text = "SD Card can not mounted";

		    		if (sdkversion >= 24){
						hasSDCard();
					}
				}
    		} catch (ClassNotFoundException e) {
				e.printStackTrace();
			} catch (NoSuchMethodException e) {
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}
	    	/*if(sdcard1()){
	    		sysSdCard = false;
	    		Log.i("qinhao", "sdcard11:"+sysSdCard);
	    		text = "SD card already mounted";
	    	}else{
	    		Log.i("qinhao", "sdcard12:"+sysSdCard);
	    		text = "SD Card can not mounted";
	    	}*/
    	}else if(sdkversion<23&&sdkversion>=20){
	    	if(sdcard2()){
	    		sysSdCard = false;
	    		Log.i("qinhao", "sdcard21:"+sysSdCard);
	    		text = "SD card already mounted";
	    	}else{
	    		Log.i("qinhao", "sdcard22:"+sysSdCard);
	    		text = "SD Card can not mounted";
	    	}
    	}else if(sdkversion<=19){
	    	if(sdcard3()){
	    		Log.i("qinhao", "sdcard3:"+sysSdCard);
	    		sysSdCard = false;
	    		Log.i("qinhao", "sdcard31:"+sysSdCard);
	    		text = "SD card already mounted";
	    	}else{
	    		Log.i("qinhao", "sdcard32:"+sysSdCard);
	    		text = "SD Card can not mounted";
	    	}
    	}
	}


	//7.0以上版本
	@RequiresApi(api = Build.VERSION_CODES.N)
	public void hasSDCard() {
		try {
			StorageManager storageManager = (StorageManager) mContext.getSystemService(Context.STORAGE_SERVICE);
// 7.0才有的方法
			List<StorageVolume> storageVolumes = storageManager.getStorageVolumes();
			Class<?> volumeClass = Class.forName("android.os.storage.StorageVolume");
			Method getPath = volumeClass.getDeclaredMethod("getPath");
			Method isRemovable = volumeClass.getDeclaredMethod("isRemovable");
			getPath.setAccessible(true);
			isRemovable.setAccessible(true);
			for (int i = 0; i < storageVolumes.size(); i++) {
				StorageVolume storageVolume = storageVolumes.get(i);
				String mPath = (String) getPath.invoke(storageVolume);
				Boolean isRemove = (Boolean) isRemovable.invoke(storageVolume);
				Log.d(TAG, "mPath is === " + mPath + "isRemoveble == " + isRemove);
				if (isRemove) {
					sysSdCard = false;
				}
			}
		}catch (Exception e){
			Log.d(TAG,"e == "+e.getMessage());
		}

	}
	
	//ֻ�����ڰ�׿6.0��ϵͳ
    private boolean sdcard1(){
    	//12-05 14:31:01.646: I/qinhao(10484): pathString1111:/mnt/runtime/write/sdcard1

    	boolean b=false;
    	String pathString = getPath();
    	Log.i("qinhao", "33333 path: "+pathString);
    	if(pathString==null)
    	{
    		pathString = getSDCardPath();
    		if(pathString!="NOSDCard")
    		{
    			b= true;
    		}
    		else {
    			b=false;
			}
    	}
    	Log.i("qinhao", "33333 path: "+pathString);
    	if(pathString.contains("-"))
    	{
    		b=true;
    	}
    	if(pathString.equals("/mnt/runtime/write/4C83-9126"))
    	{
    		b=true;
    	}
    	if(pathString.equals("/mnt/runtime/write/4885-18FE")||pathString.equals("/mnt/runtime/write/7399-1602")||pathString.equals("/mnt/runtime/write/A44E-69BA")||pathString.equals("/mnt/runtime/write/4C83-9126")||pathString.equals("/mnt/runtime/write/emulated")||pathString.equals("/mnt/runtime/write/sdcard1")){
    		b=true;
    	}
    	return b;
    }
    //��׿5.1
    private boolean sdcard2(){
    	boolean b=false;
    	String pathString=getPath();
    	Log.i("qinhao", "pathString1111:"+pathString);
    	//01-06 12:36:44.420: I/qinhao(6325): pathString1111:/mnt/media_rw/sdcard0

    	if(pathString==null)
    	{
    		pathString = getSDCardPath();
    		if(pathString!="NOSDCard")
    		{
    			b= true;
    		}
    		else {
    			b=false;
			}
    	}
    	else if(pathString.equals("/mnt/media_rw/sdcard1")||pathString.equals("/mnt/media_rw/sdcard0")
    			||pathString.equals("/mnt/shell/emulated")){
    		b=true;
    	}
    	return b;
    }
    //��׿4.4��
    private boolean sdcard3(){
    	boolean b=false;
    	String pathString=getPath();
    	Log.i("qinhao", "pathString333:"+pathString);
    	if(pathString==null){
    		pathString = getSDCardPath();
    		if(pathString!="NOSDCard")
    		{
    			b= true;
    		}
    		else {
    			b=false;
			}
    	}else if(pathString.equals("/mnt/media_rw/sdcard0")||pathString.equals("/mnt/media_rw/sdcard1")||pathString.equals("/mnt/shell/emulated")){
    		b=true;
    	}
    	return b;
    }
	public String getPath() {
		String sdcard_path = null;
		String sd_default = Environment.getExternalStorageDirectory()
				.getAbsolutePath();
		Log.i("qinhao", "33333 sd_default: " + sd_default);
		if (sd_default.endsWith("/")) {
			sd_default = sd_default.substring(0, sd_default.length() - 1);
		}

		try {
			Runtime runtime = Runtime.getRuntime();
			Process proc = runtime.exec("mount");
			InputStream is = proc.getInputStream();
			InputStreamReader isr = new InputStreamReader(is);
			String line;
			BufferedReader br = new BufferedReader(isr);
			while ((line = br.readLine()) != null) {

				if (line.contains("secure"))
					continue;
				if (line.contains("asec"))
					continue;
				if (line.contains("fat") && line.contains("/mnt/")) {
					String columns[] = line.split(" ");
					if (columns != null && columns.length > 1) {
						if (sd_default.trim().equals(columns[1].trim())) {
							Log.i("qinhao", "33333 continue 11");
							continue;
						}
						sdcard_path = columns[1];
						Log.i("qinhao", "33333 11 sdcard_path: " + sdcard_path);
					}
				} else if (line.contains("fuse") && line.contains("/mnt/")) {
					String columns[] = line.split(" ");
					if (columns != null && columns.length > 1) {
						if (sd_default.trim().equals(columns[1].trim())) {
							Log.i("qinhao", "33333 continue 22");
							continue;
						}
						sdcard_path = columns[1];
						Log.i("qinhao", "33333 22 sdcard_path: " + sdcard_path);
					}
				}
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return sdcard_path;
	}
	
    //这个方法只是支持一部分机型，
    public static String getSDCardPath() {
        String cmd = "cat /proc/mounts";
        Runtime run = Runtime.getRuntime();// 返回与当前 Java 应用程序相关的运行时对象
        try {
            Process p = run.exec(cmd);// 启动另一个进程来执行命令
            BufferedInputStream in = new BufferedInputStream(p.getInputStream());
            BufferedReader inBr = new BufferedReader(new InputStreamReader(in));

            String lineStr;
            while ((lineStr = inBr.readLine()) != null) {
                // 获得命令执行后在控制台的输出信息
                if (lineStr.contains("sdcard")
                        && lineStr.contains(".android_secure")) {
                    String[] strArray = lineStr.split(" ");
                    if (strArray != null && strArray.length >= 5) {
                        String result = strArray[1].replace("/.android_secure",
                                "");
                        return result;//有sd会返回这个
                    }
                }
                // 检查命令是否执行失败。
                if (p.waitFor() != 0 && p.exitValue() == 1) {
                    // p.exitValue()==0表示正常结束，1：非正常结束
                }
            }
            inBr.close();
            in.close();
        } catch (Exception e) {

            return e.toString();
        }

        return "NOSDCard";//sd没有sd卡会返回这个字符串
    }
	
	@Override
	public void initView() {
		// TODO Auto-generated method stub
		mLayout = (LinearLayout) View.inflate(mContext, R.layout.base_screen,
				null);

//		bt_left = (Button) mLayout.findViewById(R.id.bt_left);
//		bt_right = (Button) mLayout.findViewById(R.id.bt_right);
		text_top_zone = (TextView) mLayout.findViewById(R.id.text_top_zone);
		text_cen_zone = (TextView) mLayout.findViewById(R.id.text_cen_zone);

		text_top_zone.setText(mName);
		text_cen_zone.setText(text);
//		bt_left.setText(R.string.pass);
//		bt_right.setText(R.string.fail);
//		bt_left.setEnabled(false);// PR713420-yinbin-zhang-20140623
//
//		bt_left.setOnClickListener(pass_listener);
//		bt_right.setOnClickListener(failed_listener);

		mContext.setContentView(mLayout);
//		 if (mSdInserted) {
			testCountDownTimer.start();
//		 }
	}

	@Override
	public void finish() {
		// TODO Auto-generated method stub
		Tool.toolLog(TAG + " finish");
//		if (testCountDownTimer != null) {
//			testCountDownTimer.cancel();
//		}
	}

	@Override
	public void onFinish() {
		// TODO Auto-generated method stub
		// PR661437-yinbin-zhang-20140519 begin
		// mContext.setResult(Test.RESULT.PASS.ordinal());
		// mContext.finish();
		// bt_left.setEnabled(true);//PR713420-yinbin-zhang-20140623
		// PR661437-yinbin-zhang-20140519 end
		// PR713420-yinbin-zhang-20140623 begin
//		Tool.toolLog(TAG +  "onFinish ...");

		Log.i("qinhao", "sysSdCard:"+sysSdCard);
		if(!sysSdCard){
			if (testCountDownTimer != null) {
				testCountDownTimer.cancel();
			}
			
			new FinishThread(0x01,ExecuteTest.temppositon).start();
			sysSdCard = true;
                Msg.exitWithSuccessTest(mContext, TAG, 20, true,"Pass");
			
			
			//Write data into file
			if(AllMainActivity.mainAllTest && !AllMainActivity.autofileFlag){
				Msg.WriteModelResult(mContext,AllMainActivity.all_items_file_text, "PASS");
			}else {
				Msg.WriteModelResult(mContext,autoRunActivity.auto_file_text, "PASS");
			}
		}
		else
		{
			if (testCountDownTimer != null) {
				testCountDownTimer.cancel();
			}
			mContext.setResult(Test.RESULT.FAILED.ordinal());
			Tool.toolLog(TAG + " index 8881 -> " + ExecuteTest.temppositon);
			int double_test;
			if(AllMainActivity.mainAllTest){
				double_test = AllMainActivity.double_check_for_auto[ExecuteTest.temppositon]++;
				//AllMainActivity.mainAllTest = false;
			}else {
				double_test = autoRunActivity.double_check[ExecuteTest.temppositon]++;
			}
			Tool.toolLog(TAG + " double_test 9991-> " + double_test);
			if(double_test==1){
				new FinishThread(0x02,ExecuteTest.temppositon).start();
				//Wait for flag refresh 07/15
			/*	while(true){
					if(sysSdCard){
						sysSdCard = false;
						break;
					}
				}*/
				Msg.exitWithException(mContext, TAG,20,true,"Pass");
				
				//Write data into file 06/17 bug
				if(AllMainActivity.mainAllTest && !AllMainActivity.autofileFlag){
					Msg.WriteModelResult(mContext,AllMainActivity.all_items_file_text, "FAIL");
				}else {
					Msg.WriteModelResult(mContext,autoRunActivity.auto_file_text, "FAIL");
				}
				//End
			}else{
				new FinishThread(0x02,ExecuteTest.temppositon).start();
				//Wait for flag refresh 07/15
				/*while(true){
					if(sysSdCard){
						sysSdCard = false;
						break;
					}
				}*/
				Msg.exitWithException(mContext, TAG,20,true,"Fail");
				
			}
			
		
		}
		/*
		text = "";
		mWrite(content);
		String string = mRead();
		if (string.length() == 5) {
			text = "SD Test OK";
			text_cen_zone.setText(text);
			try {
				Thread.sleep(500);
			} catch (Exception e) {
				e.printStackTrace();
			}
//			bt_left.setEnabled(true);
//			mContext.setResult(Test.RESULT.PASS.ordinal());
			//Add by Jianke.Zhang 02/04
			if (testCountDownTimer != null) {
				testCountDownTimer.cancel();
			}
			
			new FinishThread(0x01,ExecuteTest.temppositon).start();
			//Wait for flag refresh 07/15
			while(true){
				if(sysSdCard){
					sysSdCard = false;
					break;
				}
			}
			Msg.exitWithSuccessTest(mContext, TAG, 20, true,"Pass");
			
			
			//Write data into file
			if(AllMainActivity.mainAllTest && !AllMainActivity.autofileFlag){
				Msg.WriteModelResult(mContext,AllMainActivity.all_items_file_text, "PASS");
			}else {
				Msg.WriteModelResult(mContext,autoRunActivity.auto_file_text, "PASS");
			}
			//End
		} else {
			text = "SD Read Fail";
			text_cen_zone.setText(text);
			//Add by Jianke.Zhang 02/04
			if (testCountDownTimer != null) {
				testCountDownTimer.cancel();
			}
			mContext.setResult(Test.RESULT.FAILED.ordinal());
			Tool.toolLog(TAG + " index 8881 -> " + ExecuteTest.temppositon);
			int double_test;
			if(AllMainActivity.mainAllTest){
				double_test = AllMainActivity.double_check_for_auto[ExecuteTest.temppositon]++;
				//AllMainActivity.mainAllTest = false;
			}else {
				double_test = autoRunActivity.double_check[ExecuteTest.temppositon]++;
			}
			Tool.toolLog(TAG + " double_test 9991-> " + double_test);
			if(double_test==1){
				new FinishThread(0x02,ExecuteTest.temppositon).start();
				//Wait for flag refresh 07/15
				while(true){
					if(sysSdCard){
						sysSdCard = false;
						break;
					}
				}
				Msg.exitWithException(mContext, TAG,20,true,"Pass");
				
				//Write data into file 06/17 bug
				if(AllMainActivity.mainAllTest && !AllMainActivity.autofileFlag){
					Msg.WriteModelResult(mContext,AllMainActivity.all_items_file_text, "FAIL");
				}else {
					Msg.WriteModelResult(mContext,autoRunActivity.auto_file_text, "FAIL");
				}
				//End
			}else{
				new FinishThread(0x02,ExecuteTest.temppositon).start();
				//Wait for flag refresh 07/15
				while(true){
					if(sysSdCard){
						sysSdCard = false;
						break;
					}
				}
				Msg.exitWithException(mContext, TAG,20,true,"Fail");
				
			}
			
		}*/
		// PR713420-yinbin-zhang-20140623 end
	}

	@Override
	public void onTick() {
		// TODO Auto-generated method stub
		//Tool.toolLog(TAG + " onTick");
	}

	// PR661437-yinbin-zhang-20140519 begin
	public void mWrite(String content) {
		try {
			//  /storage/sdcard1/sdtest.txt
			File file = new File(sdpath);
//			Tool.toolLog(TAG + " sdpath -> " + sdpath);
			//Change by Jianke.Zhang 02/03
//			RandomAccessFile raFile = new RandomAccessFile(file, "rw");
			if(file.exists()){
				file.delete();
			}
//			Tool.toolLog(TAG + " 11111111 ...");
			file.createNewFile();
			FileWriter fileWriter = null;
//			Tool.toolLog(TAG + " 121212 " + file.getAbsolutePath());
			fileWriter = new FileWriter(file.getAbsolutePath(),true);
			fileWriter.write(content/*.getBytes()*/);
//			Tool.toolLog(TAG + " 131313 " + file.getAbsolutePath());
			
			fileWriter.flush();
			fileWriter.close(); 
			//End
//			Tool.toolLog(TAG + " raFile ...");
			
//			raFile.seek(0);
//			raFile.write(content.getBytes());
//			raFile.close();
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
//			Toast.makeText(mContext, "mWrite Exception!!", Toast.LENGTH_SHORT)
//					.show();
			Tool.toolLog(TAG + " Exception " + e.getMessage());
			
		}
	}

	public String mRead() {
		StringBuilder sBuilder = null;
		try {
			FileInputStream fis = new FileInputStream(sdpath);
			BufferedReader bReader = new BufferedReader(new InputStreamReader(
					fis));
			sBuilder = new StringBuilder("");
			String lin = null;
			while ((lin = bReader.readLine()) != null) {
				sBuilder.append(lin);
			}
			bReader.close();

		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
//			Tool.toolLog(TAG + " mRead ...");
		}

		if (sBuilder != null) {
			return sBuilder.toString();
		} else {
			return "";
		}

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
		
	}

//	@Override
//	public void pass() {
//		// TODO Auto-generated method stub
//		switch (flag) {
//		case 1:
//			text = "";
//			mWrite(content);
//			String string = mRead();
//			if (string.length() == 5) {
//				text = "SD Test OK";
//				text_cen_zone.setText(text);
//				// PR713420-yinbin-zhang-20140623 begin
//				try {
//					Thread.sleep(1000);
//				} catch (Exception e) {
//					e.printStackTrace();
//				}
//				mContext.setResult(Test.RESULT.PASS.ordinal());
//				mContext.finish();
//				// PR713420-yinbin-zhang-20140623 end
//			} else {
//				text = "SD Read Fail";
//				text_cen_zone.setText(text);
//			}
//			flag++;
//			break;
//
//		default:
//			super.pass();
//			break;
//		}
//	}
	// PR661437-yinbin-zhang-20140519 end

}
