package com.tcl.autotest.tool;

import com.tcl.autotest.AllMainActivity;
import com.tcl.autotest.ExecuteTest;
import com.tcl.autotest.autoRunActivity;
import com.tcl.autotest.utils.FinishThread;
import com.tcl.autotest.utils.Msg;
import com.tcl.autotest.utils.Test;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;

public class Tool {
	
	private static final String TAG = "LogTool";
	public static boolean Seprate_Finish_Flag = false;
	public static void userMakeToast(Context mContext,String tabId){
		Toast.makeText(mContext, tabId, Toast.LENGTH_SHORT).show();
	}
	
	public static void toolLog(String showWord){
		Log.e(TAG,TAG + " -> " + showWord);
	}
	
	public static void sleepTimes(int t){
		try {
			Thread.sleep(100*t);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	private void waitForSeprateFinished(){
		int count = 0;
		while(true){
			count++;
			if(Seprate_Finish_Flag ){
				Seprate_Finish_Flag = false;
				break;
			}else{
				if(count%5 == 0){
					Tool.toolLog(TAG + " sleep " + count);
				}
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
			if(count > 1000){
				break;
			}	
		}
	}
	
	public static void success(Activity mContext,String tag){
		
		mContext.setResult(Test.RESULT.PASS.ordinal());
		FinishThread tFinishThread = new FinishThread(0x01,ExecuteTest.temppositon);
		tFinishThread.start();
		try {
			tFinishThread.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		Msg.exitWithSuccessTest(mContext,tag, 10,true,"Pass");
		
		if(AllMainActivity.mainAllTest && !AllMainActivity.autofileFlag){
			Msg.WriteModelResult(mContext,AllMainActivity.all_items_file_text, "PASS");
		}else {
			Msg.WriteModelResult(mContext,autoRunActivity.auto_file_text, "PASS");
		}	
	}
	
	public static void fail(Activity mContext,String tag){
		
		mContext.setResult(Test.RESULT.FAILED.ordinal());
		Tool.toolLog(tag + " index 8887 -> " + ExecuteTest.temppositon);
		int double_test;
		if(AllMainActivity.mainAllTest){
			double_test = AllMainActivity.double_check_for_auto[ExecuteTest.temppositon]++;
		}else {
			double_test = autoRunActivity.double_check[ExecuteTest.temppositon]++;
		}
		Tool.toolLog(tag + " double_test 9997 -> " + double_test);
		FinishThread tFinishThread = new FinishThread(0x02,ExecuteTest.temppositon);
		tFinishThread.start();
		try {
			tFinishThread.join();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if(double_test==1){
			Msg.exitWithException(mContext,tag,20,true,"Pass");
			//Write data into file
			if(AllMainActivity.mainAllTest && !AllMainActivity.autofileFlag){
				Msg.WriteModelResult(mContext,AllMainActivity.all_items_file_text, "FAIL");
			}else {
				Msg.WriteModelResult(mContext,autoRunActivity.auto_file_text, "FAIL");
			}
			//End
		}else{
			Msg.exitWithException(mContext,tag,20,true,"Fail");
		}
	}
	public void aaa()
	{
		int a =30;
		for(int i=0;i<12;i++)
		{
			
		}
		
	}
	
}
