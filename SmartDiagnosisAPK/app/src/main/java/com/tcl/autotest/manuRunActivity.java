package com.tcl.autotest;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

import com.tcl.autotest.tool.Tool;
import com.tcl.autotest.utils.FinishThread;
import com.tcl.autotest.utils.ManuFinishThread;
import com.tcl.autotest.utils.Msg;

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
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;

public class manuRunActivity extends Activity {
	
	private static final String TAG = "manuRunActivity";
	public static final String ManuFinishSignal  = "ManuTest_Finish_Signal";
	ListView manulv;
	public static ArrayList<String> manulist;
	private static ArrayList<Integer> checkedList = new ArrayList<Integer>();
	public static ArrayList<Boolean> abl = new ArrayList<Boolean>();
	private MyAdapter mAdapter;
	ViewHolder holder;
	DataManuSevice broadcastmanutestfinish = null;
	boolean Finish_Flag = false;
	boolean reg_flag = false;
	public static int indexForResume = 0;
	public int totalForChecked = 0;
	public static String filetxt = "manuTest";
	public static boolean Over_test = true;
	HorizontalScrollView manulayout;
	
	public static int index_resume_manu_global = -1;
	public static boolean manrefresh_bool = true;
	
	public static boolean socketManuStart = false;
	//Add by Jianke.Zhang 01/29
//	Button btn_start;
//	Button btn_select_items;
//	Button btn_clear_items;
//	Button btn_result;
	ImageButton btn_start;
	ImageButton btn_select_items;
	ImageButton btn_clear_items;
	ImageButton btn_result;
	//End
	
	private boolean isRunCase = false;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		//second_manutest_activity
		setContentView(R.layout.copy_second_manutest_activity);
		
		manulv = (ListView) findViewById(R.id.manu_lv);
		
		//Add by Jianke.Zhang for Button 01/29
//		btn_start = (Button) findViewById(R.id.start);
		btn_start = (ImageButton) findViewById(R.id.start);
		btn_start.setClickable(true);
//		btn_start.setBackgroundColor(Color.WHITE);
		
//		btn_select_items = (Button) findViewById(R.id.select);
//		btn_clear_items = (Button) findViewById(R.id.clear);
//		btn_result = (Button) findViewById(R.id.result);
		btn_select_items = (ImageButton) findViewById(R.id.select);
		btn_clear_items = (ImageButton) findViewById(R.id.clear);
		btn_result = (ImageButton) findViewById(R.id.result);
		
		btn_start.setOnClickListener(listener);
		btn_select_items.setOnClickListener(listener);
		btn_clear_items.setOnClickListener(listener);
		btn_result.setOnClickListener(listener);
		//End
		
		initManuData();
		
		manulv.setOnItemClickListener(new OnItemClickListener(){

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				// TODO Auto-generated method stub
//				Tool.toolLog("onItemClick ... arg2 " + arg2);
				holder = (ViewHolder) arg1.getTag();
				// Change CheckBox Status
				holder.cb.toggle();
				// get CheckBox Status
				MyAdapter.getIsSelected().put(arg2, holder.cb.isChecked());
				if(holder.cb.isChecked()){
					totalForChecked++;
				}else{
					totalForChecked--;
				}
//				Tool.toolLog(TAG + " --> totalForChecked " + totalForChecked);
			}});
		
		
		//Begin Jianke.Zhang Add 2015/01/14  Register broardcast for Test Finish!	
		broadcastmanutestfinish = new DataManuSevice();
		IntentFilter receiverManuStart = new IntentFilter(ManuFinishSignal);
		this.registerReceiver(broadcastmanutestfinish, receiverManuStart);
		//End
		//fixedListView();
		
		Tool.toolLog(TAG + " 20150701 onCreate menu");
	}
	
	//Add by Jianke.Zhang 01/29
	OnClickListener listener = new View.OnClickListener(){

		@Override
		public void onClick(View arg0) {
			// TODO Auto-generated method stub
			switch(arg0.getId()){
				case R.id.start:
//					Tool.toolLog("button manustart");
					StartManuActivity();
					break;
				case R.id.select:
					selectAll();
					break;
				case R.id.clear:
					clearAll();
					break;
				case R.id.result:
					showManuLogDialog();
					break;
				default:
					break;
			}
			
		}};
	//End
	
//	private void fixedListView(){ 
//		ViewGroup.LayoutParams params = manulv.getLayoutParams(); 
//		manulayout = (HorizontalScrollView) this.findViewById(R.id.manu_item_layout); 
//		params.height = manulayout.getHeight(); //ÐèÒªÉèÖÃµÄlistviewµÄ¸ß¶È£¬Äã¿ÉÒÔÉèÖÃ³ÉÒ»¸ö¶¨Öµ£¬Ò²¿ÉÒÔÉèÖÃ³ÉÆäËûÈÝÆ÷µÄ¸ß¶È£¬Èç¹ûÊÇÆäËûÈÝÆ÷¸ß¶È£¬ÄÇÃ´²»ÒªÔÚoncreateÖÐÖ´ÐÐ£¬ÐèÒª×öÑÓÊ±´¦Àí£¬·ñÔò¸ß¶ÈÎª0 
//		manulv.setLayoutParams(params); 
//	}
		
	private ListView initManuData() {
		// TODO Auto-generated method stub
//		Tool.toolLog("initManuData");
		manulist = new ArrayList<String>();
		initData();
		
		return manulv;
	}
	
	public void initData() {
		// init Adapter data
		initList();
		// Case MyAdapter
		mAdapter = new MyAdapter(manulist, this);
		manulv.setAdapter(mAdapter);
	}
	
	private void initList() {
		//----1----
		
		
		AllMainActivity ama = new AllMainActivity();
		manulist = ama.refreshManual();
		
		/*if(AllMainActivity.testItemsMap.get("TracabilityTest").equals("true")){
			manulist.add("TRACABILITY");
		}
		
		if(AllMainActivity.testItemsMap.get("Tp1Test").equals("true")){
			manulist.add("TP1");
		}
		
		if(AllMainActivity.testItemsMap.get("Tp2Test").equals("true")){
			manulist.add("TP2");
		}
		
		if(AllMainActivity.testItemsMap.get("PointTest").equals("true")){
			manulist.add("POINT");
		}
		
		if(AllMainActivity.testItemsMap.get("LcdAllColorTest").equals("true")){
			manulist.add("LCD ALLCOLOR");
		}
		
		manulist.add("LCD MIRERGB");
		manulist.add("LCD BLACK");
		manulist.add("LCD GRAY");
		manulist.add("LCD GRAYLEVEL");
		manulist.add("LCD WHITE");
		
		//----2----
		if(AllMainActivity.testItemsMap.get("KeyPadTest").equals("true")){
			manulist.add("KEYPAD");
		}
		
//		manulist.add("HEADSET");
		
		//----3----
		if(AllMainActivity.testItemsMap.get("USBTest").equals("true")){
			manulist.add("USB");
		}
		
		if(AllMainActivity.testItemsMap.get("CompassTest").equals("true")){
			manulist.add("COMPASS");
		}
			
		//----4----
		if(AllMainActivity.testItemsMap.get("GSensorTest").equals("true")){
			manulist.add("GSENSOR");
		}
		
		if(AllMainActivity.testItemsMap.get("LightSensorTest").equals("true")){
			manulist.add("LIGHT SENSOR");
		}
		
		if(AllMainActivity.testItemsMap.get("AlsPsTest").equals("true")){
			manulist.add("PROXIMITY");
		}
		
		if(AllMainActivity.testItemsMap.get("BatteryTempTest").equals("true")){
			manulist.add("BATTERY TEMP");
		}
		
		if(AllMainActivity.testItemsMap.get("GPSSensorTest").equals("true")){
			manulist.add("GPS");
		}
		
		if(AllMainActivity.testItemsMap.get("CarrierSignalTest").equals("true")){
			manulist.add("CARR SIGNAL");
		}
		
		if(AllMainActivity.testItemsMap.get("ChargerLEDTest").equals("true")){
			manulist.add("CHARGER LED");
		}
		
		if(AllMainActivity.testItemsMap.get("MICTest").equals("true")){
			manulist.add("MIC");
		}
		//FM Test
		if(true){
			if(AllMainActivity.testItemsMap.get("FMTest").equals("true")){
			manulist.add("FM");
		}
		}
		//manulist.add("FACTORY RESET");
*/		
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		Tool.toolLog("manu onCreateOptionsMenu");
//		getMenuInflater().inflate(R.menu.activity_manutest_main, menu);
		
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		switch (item.getItemId()) {
			case R.id.manustart:
//				Tool.toolLog("manustart");
				StartManuActivity();
				break;
			case R.id.select_all:
				selectAll();
				break;
			case R.id.clear_all:
				clearAll();
				break;
			case R.id.test_show:
				showManuLogDialog();
				break;
			default:
				break;
		}

		return super.onOptionsItemSelected(item);
	}
	
	private void StartManuActivity() {
		SimpleDateFormat sDateFormat = new SimpleDateFormat("hh:mm:ss");       
		String date = sDateFormat.format(new java.util.Date()); 
		Tool.toolLog("StartManuActivity_manutest " + date);
		//09/29
		socketManuStart = true;
		
		//Add by Jianke.Zhang 01/26
		if(broadcastmanutestfinish == null){
			//Begin Jianke.Zhang Add 2015/01/14  Register broardcast for Test Finish!	
			broadcastmanutestfinish = new DataManuSevice();
			IntentFilter receiverManuStart = new IntentFilter(ManuFinishSignal);
			this.registerReceiver(broadcastmanutestfinish, receiverManuStart);
			//End
		}
		Msg.deleteOldFile(filetxt);
		Msg.createFile(filetxt);
		//End
		btn_start.setEnabled(false);
		btn_start.setClickable(false);
//		btn_start.setBackgroundColor(Color.GRAY);
		AllMainActivity.manufileFlag = true;
		AllMainActivity.mainAllTest = false;
		
		new Thread(){
			public void run(){
				isRunCase = true;
				RunCases();
				isRunCase = false;
			}
		}.start();
	}
	
	public void RunCases(){
//		MyAdapter.refresh_flag = false;
		checkedList.clear();
		abl.clear();
		indexForResume = 0;
//		Tool.toolLog(" =========== " + broadcastmanutestfinish);
		
		
		for(int i=0;i<manulist.size();i++){
			if(MyAdapter.getIsSelected().get(i)){
				Tool.toolLog("manulist checkedList--> " + manulist.size());
				
//				Tool.toolLog("indexForResume " + indexForResume);
				//0304
				Tool.toolLog(TAG + " manrefresh_bool " + manrefresh_bool);
				/*while(true){
					if(!manrefresh_bool){
						Tool.toolLog(TAG + " 888888888");
						manrefresh_bool = true;
						break;
					}
				}*/
				Tool.sleepTimes(10);
				checkedList.add(i); 
//				Tool.toolLog("checkedList--> "+i);
//				MyAdapter.setItemPosition(i);
				reg_flag = true;
				runCase(i);
				indexForResume++;
				waitForFinished();
			}
		}
		SetButtonUse();
		
//		Tool.toolLog("reg_flag " + reg_flag);
		if(reg_flag){
			Tool.toolLog(TAG + " finish all the test!");
			this.unregisterReceiver(broadcastmanutestfinish);
			broadcastmanutestfinish = null;
			reg_flag = false;
//			Over_test = false;
		}else{
			Tool.toolLog(TAG + " Choose no items!");
		}
		
	}
	
	public void runCase(int caseIndex) {
		Tool.toolLog(TAG + " start test position: " + caseIndex);
//		Over_test = true;
		
		Intent intent = new Intent(Intent.ACTION_MAIN, null);
		intent.putExtra("position", caseIndex);
		intent.setComponent(new ComponentName("com.tcl.autotest",
				"com.tcl.autotest.ExecuteManuTest"));
		this.startActivity(intent);
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
//				new ManuFinishThread(0x02).start();
				break;
			}	
		}
	}
	
	private void selectAll() {

		for (int i = 0; i < manulist.size(); i++) {
			MyAdapter.getIsSelected().put(i, true);
			totalForChecked = manulist.size();
		}
		dataChanged();
	}

	private void clearAll() {
		for (int i = 0; i < manulist.size(); i++) {
			MyAdapter.getIsSelected().put(i, false);
			totalForChecked = 0;
		}
		dataChanged();
	}

	private void dataChanged() {
		// Notify listView Refresh
		mAdapter.notifyDataSetChanged();
	}
	
	
	public void showManuLogDialog(){
		String wholeName = Msg.getManuFilePath();
		Tool.toolLog(TAG + " manu 1111111111 wholeName " + wholeName);
		Intent intent = new Intent();
		intent.setClass(this, ShowManuRcdsActivity.class);
		Bundle bundle = new Bundle();
		bundle.putString("RCD_FILE_MANU_NAME", wholeName);
		intent.putExtras(bundle);
		startActivity(intent);
	}
	
	public class DataManuSevice extends BroadcastReceiver {
		
		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			Bundle bundle = intent.getExtras();
			if(bundle.getString("manuRunActivity.Test").contains("manuRunActivity.Finish")){
//				Tool.toolLog("manuRunActivity Receive message!!!!!!!");
				Finish_Flag = true;
			}		
		}
	
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		index_resume_manu_global++;
		//indexForResume != 0 && (indexForResume == totalForChecked)
		if(true){
			//Tool.toolLog("onResume ... " + index_resume_manu_global);
			//manulv.setAdapter(mAdapter);
			mAdapter.notifyDataSetChanged();
		}
//		manulv.invalidate();
	}
	
	
	static public ArrayList<Integer> getCheckedList(){
		return checkedList;
	}
	
	static public ArrayList<Boolean> getResultList(){
		return abl;
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
	
	public void onBackPressed() {
		if(isRunCase){
			Tool.toolLog(TAG + " onBackPressed");
		}else{
			super.onBackPressed();
		}
	}
	
//	@Override
//	public boolean dispatchKeyEvent(KeyEvent event) {
//		// TODO Auto-generated method stub
//		
//		int keycode = event.getKeyCode();
//		Tool.toolLog(TAG + " keycode " + keycode);
//		switch (keycode){
//			case KeyEvent.KEYCODE_HOME:
//				Tool.toolLog(TAG + " KEYCODE_HOME");
//				break;
//			default:
//				return super.dispatchKeyEvent(event);
//		}
//		
//		return true;
//	}
	
//	private void onKeyTouch() {
//		// TODO Auto-generated method stub
//		Tool.toolLog(TAG + " onKeyTouch ");
//	}
}
