package com.tcl.autotest.utils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import com.tcl.autotest.AllMainActivity;
import com.tcl.autotest.ExecuteManuTest;
import com.tcl.autotest.ExecuteTest;
import com.tcl.autotest.autoRunActivity;
import com.tcl.autotest.manuRunActivity;
import com.tcl.autotest.tool.Tool;

import android.R.integer;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;


public class Msg {

	private static final String TAG = "Msg";
	public static String textPath;
	public static String textName;
	static File mPath = Environment.getExternalStorageDirectory();
	
	public static void sendMsg(String TAG,Activity mContext,Intent i,Bundle bundle){
		Log.d(TAG, " 33333 sendMsg: " + mContext);
//		Bundle bundle = new Bundle();
//		bundle.putString("AutotestMainActivity.Test", "AutotestMainActivity.Finish");
		i.putExtras(bundle);
		mContext.sendBroadcast(i);
	}
	
	//manuTest.txt
	//autoTest.txt
	public static void deleteOldFile(String fileName){
		 
		File mFile = new File(mPath.getPath() + File.separator + "AutoTestRecord");
		
		if(!mFile.exists()){
			return;
		}
		
		if(mFile.isDirectory()){
			File mTxt = new File(mFile.toString() + "/" + fileName + ".txt");
			if(mTxt.exists()){
				mTxt.delete();
			}
		}
	}
	
	public static void createFolder(){
		File mFile = new File(mPath.getPath() + File.separator + "AutoTestRecord");
//		Tool.toolLog(TAG + " " + mFile.getPath());
		
		if(mFile.exists() && !mFile.isDirectory() || !mFile.exists()){
//			Log.i(TAG,TAG + " create directory");
			mFile.mkdir();
		}
	}
	
	public static void createFile(String fileName){
//		File mPath = Environment.getExternalStorageDirectory(); 
//		Log.i(TAG,"mPath " + mPath);
		File mFile = new File(mPath.getPath() + File.separator + "AutoTestRecord");
//		Tool.toolLog(TAG + " " + mFile.getPath());
		
		if(mFile.exists() && !mFile.isDirectory() || !mFile.exists()){
//			Log.i(TAG,TAG + " create directory");
			mFile.mkdir();
		}
		
		//Create file testRecord.txt
		File mTxt = new File(mFile.toString() + "/" + fileName + ".txt");
		
		try {
//			Log.i(TAG,TAG + " create txt file");
			if(!mTxt.exists()){
				mTxt.createNewFile();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		textPath = mFile.toString() + "/" + fileName + ".txt";
		textName = fileName;
//		Tool.toolLog(TAG + " textName " + textName);
	}
	
	public static String getFilePath(){
		
		return textPath;
	}

	public static String getAllItemsFilePath(){
		File mFile = new File(mPath.getPath() + File.separator + "AutoTestRecord");
		
		return mFile.toString() + File.separator + AllMainActivity.all_items_file_text + ".txt";
	}
	
	public static String getAutoFilePath(){
		File mFile = new File(mPath.getPath() + File.separator + "AutoTestRecord");
		
		return mFile.toString() + File.separator + autoRunActivity.auto_file_text + ".txt";
	}

	public static String getManuFilePath(){
		File mFile = new File(mPath.getPath() + File.separator + "AutoTestRecord");
		
		return mFile.toString() + File.separator + manuRunActivity.filetxt + ".txt";
	}
	
	public static String getFileName(){
		
		return textName;
	}
	
	//Wirte File to Text
	public static void writeFileText(String Model,String keyWord) throws IOException{
		FileWriter fileWriter = null;
		fileWriter = new FileWriter(getFilePath(),true);
			
		fileWriter.write(Model + " -> " + keyWord + "\n");
//		Tool.toolLog(Model + " -> " + keyWord);
		
		fileWriter.flush();
		fileWriter.close();
	}
	
	public static void sendManuMessage(String tag,Activity mContext){
		Intent intent;
		Bundle bundle;
		intent = new Intent(manuRunActivity.ManuFinishSignal);
		bundle = new Bundle();
		bundle.putString("manuRunActivity.Test", "manuRunActivity.Finish");
		sendMsg(tag,mContext,intent,bundle);
	}
	
	public static void sendAutoMessage(String tag,Activity mContext,String result){
		Intent intent;
		Bundle bundle;
		intent = new Intent(autoRunActivity.AutoFinishSignal);
		bundle = new Bundle();
		bundle.putString("autoRunActivity.Test", "autoRunActivity.Finish");
		bundle.putString("autoRunActivity.Result", result);
		sendMsg(tag,mContext,intent,bundle);
	}
	
	public static void exitWithException(Activity mContext,String tag,int t,boolean flag,String result){
		Tool.sleepTimes(t);
		Tool.toolLog(tag + "_onFinish_Exception");
		Log.d(tag, "33333 _onFinish_Exception");
		
		if(flag){
			sendAutoMessage(tag, mContext,result);
		}else{
			sendManuMessage(tag, mContext);
		}
		
		mContext.finish();
	}
	
	
	public static void exitWithSuccessTest(Activity mContext,final String tag,int t,  boolean flag, String result){
		Tool.sleepTimes(t);
		Tool.toolLog(tag + "_onFinish_SuccessTest");
		if(flag){
			sendAutoMessage(tag, mContext,result);
		}else{
			sendManuMessage(tag, mContext);
		}
		
		mContext.finish();
	}
	
	
	public static void WriteModelResult(Activity mActivity,String fileName,String result){
		Tool.toolLog(TAG + " mActivity " + mActivity.getLocalClassName());
		Test Model = null;
		if(mActivity.getLocalClassName().contains("ExecuteTest")){
			Model = TestList.getAutoTestList()[ExecuteTest.temppositon];
		}else if(mActivity.getLocalClassName().contains("ExecuteManuTest")){
			Model = TestList.getTestList()[ExecuteManuTest.temppositon];
		}
		
		Tool.toolLog("Model " + Model);
		
		createFile(fileName);
		try {
			writeFileText(Model.toString(), result);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
}
