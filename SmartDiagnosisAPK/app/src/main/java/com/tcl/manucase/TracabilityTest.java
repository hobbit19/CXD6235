package com.tcl.manucase;

import com.tcl.autotest.R;
import com.tcl.autotest.manuRunActivity;
import com.tcl.autotest.tool.Tool;
import com.tcl.autotest.utils.ManuFinishThread;
import com.tcl.autotest.utils.Msg;
import com.tcl.autotest.utils.Test;
import com.tcl.autotest.utils.TracabilityStruct;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.telephony.TelephonyManager;

import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
//import com.mediatek.telephony.TelephonyManagerEx;


public class TracabilityTest extends Test {
	
	private final static String TAG = "TracabilityTest";
	private TracabilityStruct mTStruct;
	private StringBuffer mDisplayString = null;
	private StringBuffer mBtAdress = null; 
	private StringBuffer mWifiAdress = null;

//	TracabilityReceiver trReceiver;
	String tracabilityAction = "TAG_TracabilityTest";
	
	public TracabilityTest(ID id, String name) {
		super(id, name);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void setUp() {
		// TODO Auto-generated method stub
		Tool.toolLog(TAG + "_start_test");
		
		//Add by Jianke for register 07/13
//		trReceiver = new TracabilityReceiver();
//		IntentFilter trIntentFilter = new IntentFilter(tracabilityAction);
//		mContext.registerReceiver(trReceiver, trIntentFilter);
		//End
		
		mDisplayString = new StringBuffer();
		mBtAdress = new StringBuffer(); 
		mWifiAdress = new StringBuffer();
		try{
			mTStruct = new TracabilityStruct();
		}catch(Exception e){
			Tool.toolLog(TAG + " " + e.toString());
		}
		
		if(Test.flag == Test.MODE.AUTO){
			mDisplayString.append("PT:")
				.append(Byte.toString(mTStruct.getItem(TracabilityStruct.ID.INFO_STATUS_PARA_SYS_I)[0])) /*print dec char*/
				.append("\n")
				.append("PFT:")
				.append(Byte.toString(mTStruct.getItem(TracabilityStruct.ID.INFO_STATUS_PARA_SYS_2_I)[0])) /*print dec char*/
				.append("\n")
				.append("BW:")
				.append(Byte.toString(mTStruct.getItem(TracabilityStruct.ID.INFO_STATUS_BW_I)[0])) /*print dec char*/
				.append("\n")
				.append("MMI:")
				.append(Byte.toString(mTStruct.getItem(TracabilityStruct.ID.INFO_STATUS_MMI_TEST_I)[0])) /*print dec char*/
				.append("\n")
				.append("FT:")
				.append(Byte.toString(mTStruct.getItem(TracabilityStruct.ID.INFO_STATUS_FINAL_I)[0])) /*print dec char*/
				.append("\n");
		} else {
			for(int i=0; i<6; i++){
				mBtAdress.append(String.format("%02X", 
						0xFF&(new Byte(mTStruct.getItem(TracabilityStruct.ID.BT_ADDR_I)[i]).intValue())));
				mWifiAdress.append(String.format("%02X", 
						0xFF&new Byte(mTStruct.getItem(TracabilityStruct.ID.WIFI_ADDR_I)[i]).intValue()));
				if(i!=5){
					mBtAdress.append(":");
					mWifiAdress.append(":");
				}
			}

			//Add by Jianke.Zhang 
		   TelephonyManager tm = (TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE);
			mDisplayString.append("IMEI:")
//					.append(new TelephonyManager(mContext).getDeviceId())
//					.append(TelephonyManagerEx.getDefault().getDeviceId(0))
					.append(tm.getDeviceId())
					.append("\n")
					.append("BSN:").append(getASCStringFromTrace(TracabilityStruct.ID.SHORT_CODE_I))
					.append(getASCStringFromTrace(TracabilityStruct.ID.ICS_I))
					.append(getASCStringFromTrace(TracabilityStruct.ID.SITE_FAC_PCBA_I))
					.append(getASCStringFromTrace(TracabilityStruct.ID.LINE_FAC_PCBA_I))
					.append(getASCStringFromTrace(TracabilityStruct.ID.DATE_PROD_PCBA_I))
					.append(getASCStringFromTrace(TracabilityStruct.ID.SN_PCBA_I)).append("\n");
			mDisplayString.append("BT:").append(mBtAdress).append("\n")
					.append("WIFI:" + mWifiAdress + "\n")
					.append("CU-REF:" + getASCStringFromTraceEx(TracabilityStruct.ID.INFO_COMM_REF_I) + "\n")//PR736564-yinbin-zhang-20140723
					.append("H/S PN:" + getASCStringFromTrace(TracabilityStruct.ID.INDUS_REF_HANDSET_I) + "\n")
					.append("PTH= " + getASCStringFromTrace(TracabilityStruct.ID.INFO_PTM_I) + "\n")
					.append("PT: " +getStringFromTrace(TracabilityStruct.ID.INFO_STATUS_PARA_SYS_I) + "\n")
					.append("PFT: " + getStringFromTrace(TracabilityStruct.ID.INFO_STATUS_PARA_SYS_2_I) + "\n")
					.append("BW: " + getStringFromTrace(TracabilityStruct.ID.INFO_STATUS_BW_I) + "\n")
					.append("MMI: " + getStringFromTrace(TracabilityStruct.ID.INFO_STATUS_MMI_TEST_I) + "\n")
					.append("FT: " + getStringFromTrace(TracabilityStruct.ID.INFO_STATUS_FINAL_I) + "\n")
					.append("Date Code: " + getHDTDownloadTime(TracabilityStruct.ID.INFO_DATE_PASS_HDT_I) + "\n\n")
                                        .append("Phase: " + getPhase() + "\n");
		}
	}
	
        private String getPhase() {
		// TODO Auto-generated method stub
		String str = "";
		byte shortcode = mTStruct.getItem(TracabilityStruct.ID.SHORT_CODE_I)[3];
		if (shortcode == 'A') {
			str = "Mock up";
		} else if (shortcode == 'B') {
			str = "Proto";
		} else if (shortcode == 'C') {
			str = "PIO";
		} else if (shortcode == 'D') {
			str = "LotO";
		} else {
			str = "NA";
		}
		return str;
	}
	private String getASCStringFromTrace(TracabilityStruct.ID id){
		byte[] resArr = mTStruct.getItem(id);
		String strReturn = new String(resArr);
		if(strReturn.length() < 1){
			strReturn = "NA";
		}

		return strReturn;
	}
	
	//PR736564-yinbin-zhang-20140723 begin
	private String getASCStringFromTraceEx(TracabilityStruct.ID id){
		byte[] resArr = mTStruct.getItem(id);
		byte temp = resArr[0];
		String strReturn = "";
		if(temp == (byte)0x00)
		{
			strReturn = "NA";
		}
		else
		{
			strReturn = new String(resArr);
		}
		return strReturn;
	}	
	//PR736564-yinbin-zhang-20140723 end

	private String getStringFromTrace(TracabilityStruct.ID id){
		String strReturn = "";
		byte[] resArr = mTStruct.getItem(id);
		for(int i=0; i<resArr.length; i++) {
			strReturn += String.format("%02X", resArr[i]);
		}
		if(strReturn.length() < 1)
			strReturn = "NA";
		return strReturn;
	}

	private String getHDTDownloadTime(TracabilityStruct.ID id) {
		String dateTable = "123456789ABCDEFGHIJKLMNOPQRSTUV";
		String monTable = "EFGHIJKLMNOP";
		String yearTable = "KLMNOPQRSTUVWXYZ";

		byte[] resArr = mTStruct.getItem(id);
		
		int day = dateTable.indexOf(resArr[0]);
		int mon = monTable.indexOf(resArr[1]);
		int year = yearTable.indexOf(resArr[2]);
		
		if (-1 == day)
			return "date error";
		if (-1 == mon)
			return "month error";
		if (-1 == year)
			return "year error";

		day += 1;
		mon += 1;
		year += 2001;
		
		return String.format("%d%02d%02d", year, mon, day);
	}
	
	@Override
	public void initView() {
		// TODO Auto-generated method stub
		mLayout = (LinearLayout)View.inflate(mContext, R.layout.manu_base_screen, null);

		bt_left = (Button) mLayout.findViewById(R.id.bt_left);
		bt_right = (Button) mLayout.findViewById(R.id.bt_right);
		text_top_zone = (TextView)mLayout.findViewById(R.id.text_top_zone);
		text_cen_zone = (TextView)mLayout.findViewById(R.id.text_cen_zone);

		text_top_zone.setText(mName);
		text_cen_zone.setText(mDisplayString);
		//PR661395-yinbin-zhang-20140516 begin
		/*bt_left.setText(R.string.fail); 
		bt_right.setText(R.string.pass);
		
		bt_left.setOnClickListener(failed_listener);
		bt_right.setOnClickListener(pass_listener);
		
		mContext.setContentView(mLayout);*/
		bt_left.setText(R.string.pass); 
		bt_right.setText(R.string.fail);
		
		//Add by Jianke 07/13
//		Intent tagIntent = new Intent(tracabilityAction);
//		tagIntent.putExtra("tag", TAG);
//		Tool.toolLog(TAG + " mContext 23232" + mContext/*.getApplicationContext()*/);
//		Tool.toolLog(TAG + " tagIntent " + tagIntent.getStringExtra("tag"));
//		mContext.sendBroadcast(tagIntent);
		//End
		
		bt_left.setOnClickListener(pass_listener);
		bt_right.setOnClickListener(failed_listener);
		
		mContext.setContentView(mLayout);
		//PR661395-yinbin-zhang-20140516 end

	}

	@Override
	public void finish() {
		// TODO Auto-generated method stub
//		Tool.toolLog("finish TracabilityTest");
		//Add by Jianke.Zhang 2015/01/15 Send broadcast after finish
//		Intent tracability;
//		Bundle bundle;
//		tracability = new Intent(manuRunActivity.ManuFinishSignal);
//		bundle = new Bundle();
//		bundle.putString("manuRunActivity.Test", "manuRunActivity.Finish");
//		Msg.sendMsg(TAG,mContext,tracability,bundle);
//		
//		new ManuFinishThread(0x01).start();
		//End
//		if(trReceiver != null){
//			mContext.unregisterReceiver(trReceiver);
//			trReceiver = null;
//		}
	}
	
//	public class TracabilityReceiver extends BroadcastReceiver {
//		
//		@Override
//		public void onReceive(Context context, Intent intent) {
//			// TODO Auto-generated method stub
//			String rAction = intent.getAction();
//			if(rAction.equals(tracabilityAction)){
//				Tool.toolLog("tag 9999999999999999");
//			}		
//		}
//	
//	}

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

}
