package com.tcl.autotest;

import java.util.ArrayList;

import com.tcl.autotest.tool.Tool;
import com.tcl.autotest.utils.FinishThread;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.app.Activity;
import android.app.TabActivity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TabHost;
import android.widget.TabHost.OnTabChangeListener;

@SuppressWarnings("deprecation")
public class AutotestMainActivityFirst extends TabActivity implements
		TabHost.TabContentFactory {

	private TabHost myTabhost;
	private ArrayList<String> autolist;
	private ArrayList<String> list;
	private AutoAdapter aAdapter;
	private MyAdapter mAdapter;
	private ListView lv;
	private ListView autolv;
	private boolean tab_flag = true; //false;
	ViewHolder holder;
	//Jianke.Zhang Add Begin 2015/01/19
	public static final String FinishSignal = "Finish_Signal";
	private static final String TAG = "AutotestMainActivity";
	boolean Finish_Flag = false;
	DataSevice broadcastfinish;
	boolean isfinished = false;
	Menu menu;
	//End
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// setContentView(R.layout.activity_autotest_main);
		lv = new ListView(this);
		autolv = new ListView(this);
		
		// Show Tab view
		myTabhost = this.getTabHost();
		// LayoutInflater.from(this).inflate(R.layout.tab_test,myTabhost.getTabContentView(),true);
		myTabhost.addTab(myTabhost.newTabSpec("tab1").setIndicator("Auto"/*"Manu"*/)
				.setContent(this)); // R.id.lv
		myTabhost.addTab(myTabhost.newTabSpec("tab2").setIndicator("Manu"/*"Auto"*/)
				.setContent(this)); // R.id.TextView02

		myTabhost.setOnTabChangedListener(new OnTabChangeListener() {

			@Override
			public void onTabChanged(String tabId) {
				// TODO Auto-generated method stub
				Tool.userMakeToast(getApplicationContext(), tabId);
				if (tabId.equals("tab1")) {
					tab_flag = true; //false;
					Tool.toolLog("false " + tab_flag);
					//
//					Myinvalidate(tabId);
				}
				if (tabId.equals("tab2")) {
					tab_flag = false; //true;
					Tool.toolLog("true " + tab_flag);
//					Myinvalidate(tabId);
				}

//				isfinished = true;
			}
		});
		
		lv.setOnItemClickListener(new OnItemClickListener(){

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				// TODO Auto-generated method stub
				Tool.toolLog("onItemClick ... ");
				holder = (ViewHolder) arg1.getTag();
				// �ı�CheckBox��״̬
				holder.cb.toggle();
				// ��CheckBox��ѡ��״����¼����
				MyAdapter.getIsSelected().put(arg2, holder.cb.isChecked());
			}});
		
		//Begin Jianke.Zhang Add 2015/01/14  Register broardcast for Test Finish!	
		broadcastfinish = new DataSevice();
		IntentFilter receiverAutoStart = new IntentFilter(FinishSignal);
		AutotestMainActivityFirst.this.registerReceiver(broadcastfinish, receiverAutoStart);
		//End
		}

	//Add by Jianke.Zhang Begin 2015/01/14
	public class DataSevice extends BroadcastReceiver {
	
		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			Bundle bundle = intent.getExtras();
			if(bundle.getString("AutotestMainActivity.Test").contains("AutotestMainActivity.Finish")){
				Finish_Flag = true;
			}		
		}
	
	}
	//Add by Jianke.Zhang End 2015/01/14
	
	public void Myinvalidate(String tab){
		if(menu == null){
			return;
		}
		
		if (tab.equals("tab1")) {
			menu.setGroupVisible(R.id.auto, true);
			menu.setGroupVisible(R.id.manu, false);
//			getMenuInflater().inflate(R.menu.activity_autotest_main, menu);
			Tool.toolLog("111111111111");
		}

		if (tab.equals("tab2")) {
			menu.setGroupVisible(R.id.auto, false);
			menu.setGroupVisible(R.id.manu, true);
//			getMenuInflater().inflate(R.menu.activity_manutest_main, menu);
		}
	}
	
	private void initList(String tag) {
		if (tag.equals("tab1")) {
			autolist.add("BlueTooth");
		} else {
			list.add("����ͨѶ");
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		Tool.toolLog("onCreateOptionsMenu");
//		if(tab_flag){
//			getMenuInflater().inflate(R.menu.activity_autotest_main, menu);
//		}else{
			getMenuInflater().inflate(R.menu.activity_manutest_main, menu);
//		}
		
		return true;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
//		this.menu = menu;
		if(true){
			// TODO Auto-generated method stub
			Tool.toolLog("tab_flag " + tab_flag);
			if (tab_flag) {
				menu.setGroupVisible(R.id.auto, true);
				menu.setGroupVisible(R.id.manu, false);
//				getMenuInflater().inflate(R.menu.activity_autotest_main, menu);
			}

			if (!tab_flag) {
				menu.setGroupVisible(R.id.auto, false);
				menu.setGroupVisible(R.id.manu, true);
//				getMenuInflater().inflate(R.menu.activity_manutest_main, menu);
			}
		}

		return true;
	}

	@Override
	public View createTabContent(String tag) {
		// TODO Auto-generated method stub
		// Add ListView into TAB View
		Tool.toolLog(tag);
		
		if (tag.equals("tab1")) {
			Tool.toolLog("111111111111");
			autolist = new ArrayList<String>();
			initData("tab1", autolist);
			
			return autolv;
		}
		Tool.toolLog("22222222222");
		list = new ArrayList<String>();
		initData("tab2", list);
		
		return lv;
	}

	public void initData(String tag, ArrayList al) {
		// ΪAdapter׼������
		initList(tag);
		// ʵ�����Զ����MyAdapter
		if (tag.equals("tab1")) {
			aAdapter = new AutoAdapter(al, this);
			autolv.setAdapter(aAdapter);
		} else {
			mAdapter = new MyAdapter(al, this);
			// ��Adapter
			lv.setAdapter(mAdapter);
//			lv.invalidate();
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		switch (item.getItemId()) {
			case R.id.autostart:
				Tool.toolLog("autostart");
				StartAutoActivity();
				break;
			case R.id.start:
				new Thread(new Runnable() {
					public void run() {
						Tool.toolLog("manustart");
					}
				}).start();
				break;
			case R.id.select_all:
				selectAll(tab_flag);
				break;
			case R.id.clear_all:
				clearAll(tab_flag);
				break;
			default:
				break;
		}

		return super.onOptionsItemSelected(item);
	}

	
	private void StartAutoActivity() {
		Tool.toolLog("StartAutoActivity");
		new Thread(){
			public void run(){
				RunCases();
			}
		}.start();
		
	}
	
	public void RunCases(){
		for(int i=0;i<autolist.size();i++){
			runCase(i);
			waitForFinished();
			//Jianke 01/19
			
		}
		Tool.toolLog(TAG + " finish all the test!");
		this.unregisterReceiver(broadcastfinish);
	}
	
	public void runCase(int caseIndex) {
		Tool.toolLog("start test position: " + caseIndex);

		Intent intent = new Intent(Intent.ACTION_MAIN, null);
		intent.putExtra("position", caseIndex);
		intent.setComponent(new ComponentName("com.tcl.autotest",
				"com.tcl.autotest.ExecuteTest"));
		//Modify by Jianke.Zhang 2015/01/14
		//startActivityForResult(intent, caseIndex);
		this.startActivity(intent);
		//Add by Jianke.Zhang 2015/01/14
//		waitForFinished();
//		if (autoIndex < TestList.elementNum) {
//			runCase(autoIndex);
//		} else {
//			autoIndex = EMPTY;
//			//finalTest();
//			//Add by Jianke.Zhang 2015/01/15
//			Log.i(TAG,TAG + " finish all the test!");
//			this.unregisterReceiver(broadcastfinish);
//			//End
//		}
	}
	
	private void selectAll(boolean flag) {

		for (int i = 0; i < list.size(); i++) {
			MyAdapter.getIsSelected().put(i, true);
		}
		dataChanged(flag);
	}

	private void clearAll(boolean flag) {
		for (int i = 0; i < list.size(); i++) {
			MyAdapter.getIsSelected().put(i, false);
		}
		dataChanged(flag);
	}

	private void dataChanged(boolean flag) {
		// ֪ͨlistViewˢ��
		mAdapter.notifyDataSetChanged();
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
	
	
}
