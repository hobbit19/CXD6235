package com.tcl.autotest.cmd;

import com.tcl.autotest.manuRunActivity;
import com.tcl.autotest.tool.Tool;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class ManuCommand extends BroadcastReceiver {

	private static final String TAG = "ManuCommand";
	String manucmd = "com.tcl.autotest.cmd.manu";
	String action;
	
	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
		action = intent.getAction();
		if(action.equals(manucmd)){
			Tool.toolLog(TAG + " manucmd .......");
			
			Tool.toolLog(TAG + " context " + context);
			new manuStartApp(context).start();
		}
	}

	class manuStartApp extends Thread{
		
		Context manuContext;
		Intent mi;
		
		public manuStartApp(Context mContext){
			manuContext = mContext.getApplicationContext();
		}
		
		@Override
		public void run(){
			mi = new Intent(manuContext,manuRunActivity.class);
			mi.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			this.manuContext.startActivity(mi);
		}
	}
}
