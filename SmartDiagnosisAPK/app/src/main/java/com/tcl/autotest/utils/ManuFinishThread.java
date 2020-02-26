package com.tcl.autotest.utils;

import java.util.ArrayList;

import android.os.Handler;
import android.os.Message;

import com.tcl.autotest.AllMainActivity;
import com.tcl.autotest.AutoAdapter;
import com.tcl.autotest.AutoViewHolder;
import com.tcl.autotest.MyAdapter;
import com.tcl.autotest.ViewHolder;
import com.tcl.autotest.manuRunActivity;
import com.tcl.autotest.tool.Tool;

public class ManuFinishThread extends Thread {

	int msginfo;
	
	public ManuFinishThread(int msginfo){
//		Tool.toolLog("show text ... ");
		this.msginfo = msginfo;
	}
	
	public void run(){
		Message msg = new Message();
		msg.what = this.msginfo;
		handler.sendMessage(msg);
	}
	
	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
//			ViewHolder vh;
			if(msg.what == 0x01){
//				Tool.toolLog("handleMessage pass");
//				vh = MyAdapter.getViewHolder();
//				vh.re_tv.setText("PASS");
				MyAdapter.refresh_flag = true;
			}else if(msg.what == 0x02){
//				Tool.toolLog("handleMessage fail");
//				vh = MyAdapter.getViewHolder();
//				vh.re_tv.setText("FAIL");
				MyAdapter.refresh_flag = false;
			}
			
			Tool.toolLog("AllMainActivity.mainAllTest " + AllMainActivity.mainAllTest + 
					" AllMainActivity.manufileFlag " + AllMainActivity.manufileFlag);
			if(AllMainActivity.mainAllTest && !AllMainActivity.manufileFlag){
				Tool.toolLog("nbnbnb 33333333");
				AllMainActivity.boolList.add(MyAdapter.refresh_flag);
			}else {
				manuRunActivity.abl.add(MyAdapter.refresh_flag);
			}
			
		}
	};
}


