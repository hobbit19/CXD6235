package com.tcl.autotest;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import com.tcl.autotest.manuRunActivity.DataManuSevice;
import com.tcl.autotest.tool.Tool;
import com.tcl.autotest.utils.FinishThread;
import com.tcl.autotest.utils.Msg;
import com.tcl.autotest.utils.SocketUtils;
import com.tcl.autotest.utils.Test;

import android.R.color;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;

public class autoRunActivity extends Activity {

	private static final String TAG = "autoRunActivity";
	ListView autolv;
	private static ArrayList<String> autolist;
	private AutoAdapter aAdapter;
	boolean Finish_Flag = false;
	DataAutoSevice broadcastfinish;
	public static final String AutoFinishSignal = "AutoTest_Finish_Signal";
	private int totalTestResume = 0;
	private int indexTestResume = 0;
	public static ArrayList<Integer> totalList = new ArrayList<Integer>();
	public static ArrayList<Boolean> boolList = new ArrayList<Boolean>();
	public static String auto_file_text = "autoTest";
	
	public static int index_resume_global = -1;
	public static boolean add_list_bool = true;
	boolean Refresh_retest = false;
	public static boolean refresh_bool = true;
	
	//Add by Jianke.Zhang 02/07 for command
	public boolean cmd_start = false;
	//End
	public static boolean socketAutoFinish = false;
	public static boolean socketAutoStart = false;
	//Add by Jianke.Zhang 01/29
//	Button btn_start;
//	Button btn_result;
	ImageButton btn_start;
	ImageButton btn_result;
	//End
	
	//2015/06/16
	public static int temp_all_value;
	public static int[] double_check;
	public static boolean auto_refresh_fail_idle = false;
	public static int refesh_times;
	//End
	private boolean isStop=false;
	
	private boolean isRunCase = false;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
//		Tool.toolLog(TAG + " savedInstanceState " + savedInstanceState);
		//second_autotest_activity
		setContentView(R.layout.copy_second_autotest_activity);
		
		//Add by Jianke.Zhang for Button 01/29
//		btn_start = (Button) findViewById(R.id.start);
		btn_start = (ImageButton) findViewById(R.id.start);
		btn_start.setClickable(true);
//		btn_start.setBackgroundColor(Color.WHITE);
		
//		btn_result = (Button) findViewById(R.id.result);
		btn_result = (ImageButton) findViewById(R.id.result);
		
		btn_start.setOnClickListener(autolistener);
		btn_result.setOnClickListener(autolistener);
		//End
		
		autolv = (ListView) findViewById(R.id.auto_lv);
		
		//07/01
//		Msg.deleteOldFile(auto_file_text);
//		Msg.createFile(auto_file_text);

		initAutoData();
//		Tool.toolLog("onCreate autoRunActivity");
		
//		totalList.clear();
//		boolList.clear();
		
		//Begin Jianke.Zhang Add 2015/01/14  Register broardcast for Test Finish!	
		broadcastfinish = new DataAutoSevice();
		IntentFilter receiverAutoStart = new IntentFilter(AutoFinishSignal);
		this.registerReceiver(broadcastfinish, receiverAutoStart);
		//End
		
		Tool.toolLog(TAG + " 20150701 onCreate auto");

	}

	OnClickListener autolistener = new View.OnClickListener() {
		
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			switch(v.getId()){
				case R.id.start:
//					Tool.toolLog("button autostart");
					//Get config file 07/01
				/*String configFilePath = "/storage/sdcard0/AutoTestRecord/" + AllMainActivity.deviceName + ".ini";
				try {
					testItemsMap = AllMainActivity.getConfigFlag(configFilePath);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}*/
				//Refresh test idle 07/01
//				initAutoData();
					StartAutoActivity();
					break;
				case R.id.result:
					showAutoLogDialog();
					break;
				default:
					break;
			}
		}
	};
	
	private ListView initAutoData() {
	// TODO Auto-generated method stub
//		Tool.toolLog("initAutoData");
		autolist = new ArrayList<String>();
		autolist = initList();
		initData(autolist);
		
		return autolv;
	}

	
	public void initData(ArrayList al) {
		//Tool.toolLog(TAG + " alwww --> " + al.size());
		//initList();
		Tool.toolLog(TAG + " al --> " + al.size());
		double_check = new int[al.size()];
		aAdapter = new AutoAdapter(al, this);
		autolv.setAdapter(aAdapter);
	}
	
	
	public ArrayList initList() {
		
		
		AllMainActivity ama = new AllMainActivity();
		autolist = ama.refreshAuto();
		
		//ArrayList<String> allautolist;
		
		
		//----1---- 07/01
		/*if(AllMainActivity.testItemsMap != null){
			Set<String> key = testItemsMap.keySet();
			for(Iterator it = key.iterator(); it.hasNext();){
				String s = (String) it.next();
				String value = (String) testItemsMap.get(s);
				Tool.toolLog(TAG + " key " + s  + " value " + value);
			}
		
		if(AllMainActivity.testItemsMap.get("BluetoothTest").equals("true")){
			autolist.add("BlueTooth");
		}
		
		//no write permission
		if(AllMainActivity.testItemsMap.get("LightTest").equals("true")){
			autolist.add("KEYPAD & LCD BACKLIGHT");
		}
		
		//----2----
		if(AllMainActivity.testItemsMap.get("CameraTest0").equals("true")){
			autolist.add("CAMERA");
		}
		if(AllMainActivity.testItemsMap.get("CameraTest1").equals("true")){
			autolist.add("CAMERA IMG FRONT");
		}
		
		//----3----
		if(AllMainActivity.testItemsMap.get("FlashLEDTest").equals("true")){
			autolist.add("CAMERA LED");
		}
		
		if(AllMainActivity.testItemsMap.get("AudioTest").equals("true")){
			autolist.add("AUDIO");
		}
		
		if(AllMainActivity.testItemsMap.get("VibratorTest").equals("true")){
			autolist.add("VIBRATOR");
		}
		
		if(AllMainActivity.testItemsMap.get("WifiTest").equals("true")){
			autolist.add("WIFI");
		}
		
		//----4----
		if(AllMainActivity.testItemsMap.get("SIMTest").equals("true")){
			autolist.add("SIM CARD");
		}
		
		//no write permission
		if(AllMainActivity.testItemsMap.get("MemorycardTest").equals("true")){
			autolist.add("SD CARD");
		}
		
		if(AllMainActivity.testItemsMap.get("CallTest").equals("true")){
			autolist.add("CALL");
		}
		
		//-----5------
		if(AllMainActivity.testItemsMap.get("USBChargerTest").equals("true")){
			autolist.add("USBCharger");
		}
		}*/
		
		return autolist;
	}
	
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		Tool.toolLog("auto onCreateOptionsMenu");
//		getMenuInflater().inflate(R.menu.activity_autotest_main, menu);
		
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		switch (item.getItemId()) {
			case R.id.autostart:
				Tool.toolLog("autostart");
				StartAutoActivity();
				break;
			case R.id.show_text:
				showAutoLogDialog();
				break;
			default:
				break;
		}

		return super.onOptionsItemSelected(item);
	}
	
	public void StartAutoActivity() {
		//key log  2015/02/27
		SimpleDateFormat sDateFormat = new SimpleDateFormat("hh:mm:ss");       
		String date = sDateFormat.format(new java.util.Date());   
		//Date d = new Date();
		
		Tool.toolLog("StartAutoActivity_autotest" + date);
		socketAutoStart = true;
		
		totalList.clear();
		boolList.clear();
		Msg.deleteOldFile(auto_file_text);
		Msg.createFile(auto_file_text);
		//Add for lock button 02/15
		btn_start.setEnabled(false);
		btn_start.setClickable(false);
//		btn_start.setBackgroundColor(Color.GRAY);
		AllMainActivity.autofileFlag = true;
		AllMainActivity.mainAllTest = false;
		//End

		new Thread(){
			public void run(){
				isRunCase = true;
				RunCases();
				isRunCase = false;
			}
		}.start();
	}
	
	public void RunCases(){
		index_resume_global = 0;
		totalTestResume = autolist.size();
		//2015/06/16 Begin
		double_check = null;
		double_check = new int[totalTestResume];
		//End
		if(broadcastfinish == null){
			//Begin Jianke.Zhang Add 2015/01/14  Register broardcast for Test Finish!	
			broadcastfinish = new DataAutoSevice();
			IntentFilter receiverAutoStart = new IntentFilter(AutoFinishSignal);
			this.registerReceiver(broadcastfinish, receiverAutoStart);
			//End
		}
		
		for(int i=0;i<autolist.size();i++){
			//wait for next item refresh 0303
			//06/16
			int test_times = 0;
			//End
//			waitForRefreshItem();
			while(true){
				Tool.sleepTimes(10);
				if(!refresh_bool&&!isStop){
					refresh_bool = true;
					break;
				}
			}
			Tool.sleepTimes(10);
			auto_refresh_fail_idle = false;
			totalList.add(i);
			runCase(i,false);
//			indexTestResume++;
//			Tool.toolLog(TAG + " 88888 " + Finish_Flag);
			waitForFinished();
			
		}
		//Add by Jianke.Zhang 02/15 for enable button
		SetButtonUse();
		//End
//		Finish_Flag = true;
		
		Tool.toolLog(TAG + " finish all the test!");
		socketAutoFinish = true;
		this.unregisterReceiver(broadcastfinish);
		broadcastfinish = null;
	}
	
	public void runCase(int caseIndex,boolean double_save) {
		Tool.toolLog("start test position: " + caseIndex);
		Intent intent = new Intent(Intent.ACTION_MAIN, null);
		intent.putExtra("position", caseIndex);
		intent.setComponent(new ComponentName("com.tcl.autotest",
				"com.tcl.autotest.ExecuteTest"));
		this.startActivityForResult(intent,caseIndex);
	}
	
	@Override
	protected void onActivityResult(final int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
//		super.onActivityResult(requestCode, resultCode, data);
		Tool.toolLog(TAG + " ---> " + resultCode + 
				" --> " + double_check[ExecuteTest.temppositon]);
		if (resultCode == Test.RESULT.FAILED.ordinal() && double_check[ExecuteTest.temppositon]<2){
			Tool.toolLog(TAG + " requestCode 6666666666 -> " + requestCode);
			//Wait for refresh fail idle 06/16
			new Thread(){
				public void run(){
//					while(true){
						//Tool.toolLog(TAG + " auto_refresh_fail_idle " + auto_refresh_fail_idle);
//						Tool.sleepTimes(3);
//						if(auto_refresh_fail_idle){
//							Tool.toolLog(TAG + " must be go here! =========== ");
//							auto_refresh_fail_idle = false;
//							break;
//						}
//					}
					Tool.sleepTimes(30);
					Tool.toolLog(TAG + " 111111111111111");
					Tool.toolLog("size0:"+boolList.size());
					boolList.remove(requestCode);
					Tool.toolLog("size1:"+boolList.size());
					runCase(requestCode,true);
					waitForFinishedRetest();
				}
			}.start();
			//End
		}
		
	}
	
	
	private void waitForFinishedRetest(){
		int count = 0;
		while(true){
			count++;
			if(Refresh_retest ){
				Refresh_retest = false;
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
	
	
	private void waitForFinished(){
		int count = 0;
		while(true){
			count++;
			if(Finish_Flag ){
				Finish_Flag = false;
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
//				new FinishThread(0x02).start();
				break;
			}	
		}
	}
	
	
	public void showAutoLogDialog(){
		String wholeName = Msg.getAutoFilePath();
		Tool.toolLog(TAG + " auto 222222 wholeName " + wholeName);
		Intent intent = new Intent();
		intent.setClass(this, ShowAutoRcdsActivity.class);
		Bundle bundle = new Bundle();
		bundle.putString("RCD_FILE_AUTO_NAME", wholeName);
		intent.putExtras(bundle);
		startActivity(intent);
	}
	
	public class DataAutoSevice extends BroadcastReceiver {
		
		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			Bundle bundle = intent.getExtras();
			//Tool.toolLog(TAG + " bundle " + bundle);
			if(bundle.getString("autoRunActivity.Test").contains("autoRunActivity.Finish")){
//				Tool.toolLog("autoRunActivity Receive message!!!!!!!");
				if(bundle.getString("autoRunActivity.Result").contains("Pass")){
					Tool.toolLog(TAG + " 4344343434");
					Finish_Flag = true;
				}else{
//					Finish_Flag = false;
					Refresh_retest = true;
				}
				
			}//test for refresh item 0303
//			else if(bundle.getString("autoRunActivity.Refresh").contains("autoRunActivity.RefreshFinish")){
//				Tool.toolLog("refresh items ok!!!!");
//				Refresh_item = true;
//			}
			
//			String action = intent.getAction();
//			if(action.equals(autoRunActivity.AutoFinishSignal)){
//				Tool.toolLog("refresh items ok!!!!");
//				Refresh_item = true;
//			}
		}
	
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		isStop=false;
		Tool.toolLog(TAG + " onResume 00000000000000");
		index_resume_global++;
		if(true/*totalTestResume != 0 && (indexTestResume == totalTestResume)*/){
//			Tool.toolLog("auto onResume ... jkkkkkkkkkkk " + index_resume_global);
			//autolv.setAdapter(aAdapter);
			aAdapter.notifyDataSetChanged();
		}

		if(index_resume_global>0){
//			refreshIdle();
		}
	}
	
	public void refreshIdle(){
		//Items Refresh 0303
		Intent intent = new Intent(autoRunActivity.AutoFinishSignal);
		Bundle bundle = new Bundle();
		bundle.putString("autoRunActivity.Refresh", "autoRunActivity.RefreshFinish");
		intent.putExtras(bundle);
		sendBroadcast(intent);
	}
	
	public void SetButtonUse(){
		Message msg = new Message();
		msg.what = 0x30;
		handler.sendMessage(msg);
	}
	
	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			if(msg.what == 0x30){
				btn_start.setEnabled(true);
				btn_start.setClickable(true);
//				btn_start.setBackgroundColor(Color.WHITE);
			}
		}
	};
	
	
	public int getSize(){
		
		return autolist.size();
	}
	@Override
	protected void onStop() {
		super.onStop();
		isStop=true;
	}
	
	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		if(isRunCase){
			Tool.toolLog(TAG + " onBackPressed ...");
		}else{
			super.onBackPressed();
		}
		
	}
	
}
