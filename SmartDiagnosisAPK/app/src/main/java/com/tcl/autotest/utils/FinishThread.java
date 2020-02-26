package com.tcl.autotest.utils;

import android.R.integer;
import android.os.Handler;
import android.os.Message;

import com.tcl.autocase.FlashLEDTest;
import com.tcl.autocase.FrontFlashLEDTest;
import com.tcl.autocase.MemorycardTest;
import com.tcl.autotest.AllMainActivity;
import com.tcl.autotest.AutoAdapter;
import com.tcl.autotest.AutoViewHolder;
import com.tcl.autotest.MyAdapter;
import com.tcl.autotest.ViewHolder;
import com.tcl.autotest.autoRunActivity;
import com.tcl.autotest.tool.Tool;
import com.tcl.manucase.CameraTest;

public class FinishThread extends Thread {

	int msginfo;
	int index;
	public FinishThread(int msginfo,int index){
//		Tool.toolLog("show text ... ");
		this.msginfo = msginfo;
		this.index = index;
	}
	
	public void run(){
		Message msg = new Message();
		msg.what = this.msginfo;
		handler.sendMessage(msg);
		CameraTest.sysCamera = true;
		FlashLEDTest.sysFlash = true;
		FrontFlashLEDTest.sysFlash = true;
		MemorycardTest.sysSdCard = true;
	}
	
	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
//			AutoViewHolder avh;
			if(msg.what == 0x01){
//				Tool.toolLog("auto handleMessage pass");
//				avh = AutoAdapter.getViewHolder();
//				avh.tvResult.setText("PASS");
				AutoAdapter.refresh_flag = true;
			}else if(msg.what == 0x02){
//				Tool.toolLog("auto handleMessage fail");
//				avh = AutoAdapter.getViewHolder();
//				avh.tvResult.setText("FAIL");
				AutoAdapter.refresh_flag = false;
			}
//			autoRunActivity.boolList.add(AutoAdapter.refresh_flag);
			Tool.toolLog("AllMainActivity.mainAllTest " + AllMainActivity.mainAllTest + 
					" AllMainActivity.autofileFlag " + AllMainActivity.autofileFlag);
			if(AllMainActivity.mainAllTest && !AllMainActivity.autofileFlag){
				Tool.toolLog("nbnbnb 1111111111");
				AllMainActivity.boolList.add(index, AutoAdapter.refresh_flag);
			}else {
				Tool.toolLog("nbnbnb 2222222222");
				autoRunActivity.boolList.add(index, AutoAdapter.refresh_flag);
			}

		}
	};
}


