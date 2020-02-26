package com.tcl.autotest.cmd;

import java.io.IOException;

import com.tcl.autotest.AutotestMainActivity;
import com.tcl.autotest.autoRunActivity;
import com.tcl.autotest.tool.Tool;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

public class AutoCommand extends BroadcastReceiver {

	private static final String TAG = "AutoCommand";
	String autocmd = "com.tcl.autotest.cmd.auto";
	String action;//cmd.exe /c adb shell 
	String prog = "/system/bin/am start -n com.tcl.autotest/.autoRunActivity"; 
	Intent action_intent;
	
	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
		action = intent.getAction();
		if(action.equals(autocmd)){
			Tool.toolLog(TAG + " auto .........");
//			try {
//				Process p = Runtime.getRuntime().exec(prog);
////				p.waitFor();
//			} catch (Exception e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//			
			
//			action_intent = new Intent();
//			action_intent.setClass(context,autoRunActivity.class);
//			action_intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//			context.startActivity(action_intent);
//			new autoRunActivity().StartAutoActivity();
			
			Tool.toolLog(TAG + " context " + context);
			new autoStartApp(context).start();
		}
	}
	
	class autoStartApp extends Thread{
		Context newContext;
		Intent ai;
		Bundle mBundle = new Bundle();
		
		public autoStartApp(Context mContext){
			newContext = mContext.getApplicationContext();
			Tool.toolLog(TAG + " newContext " + newContext);
		}
		
		@Override
		public void run(){
			ai = new Intent(newContext,autoRunActivity.class);
			ai.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			ai.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
			mBundle.putBoolean("autocmdstart", true);
			
			ai.putExtras(mBundle);
			this.newContext.startActivity(ai);
			
		}
		
	}

}
