package com.tcl.autocase;

import java.io.IOException;

import com.tcl.autotest.AllMainActivity;
import com.tcl.autotest.ExecuteTest;
import com.tcl.autotest.R;
import com.tcl.autotest.autoRunActivity;
import com.tcl.autotest.tool.Tool;
import com.tcl.autotest.utils.DownTimeCallBack;
import com.tcl.autotest.utils.FinishThread;
import com.tcl.autotest.utils.Msg;
import com.tcl.autotest.utils.Test;
import com.tcl.autotest.utils.TestCountDownTimer;
import com.tcl.autotest.utils.TracabilityStruct;


//import com.jrdcom.utils.JRDRapi;
//import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.telephony.TelephonyManager;
//import com.mediatek.telephony.TelephonyManagerEx;
import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

//import com.mediatek.common.featureoption.FeatureOption;

public class SIMTest extends Test implements DownTimeCallBack {

	private final static String TAG = "SIMTest";
	private final int GEMINI_SIM_1 = 0;
	private final int GEMINI_SIM_2 = 1;
	private TracabilityStruct mTStruct;
	TelephonyManager mTm = null;
	boolean bSIMCardOk = false;
	// private JRDRapi remote;
	// private int mOprtMode;
	private TestCountDownTimer testCountDownTimer = null;

	String mDisplaySIMTestString = "";
	String mDisplaySIM1TestString = "";
	String mDisplaySIM2TestString = "";

	boolean sim1State=false;
	
	public SIMTest(ID id, String name) {
		super(id, name);
		// TODO Auto-generated constructor stub
		try {
			mTStruct = new TracabilityStruct();
		} catch (Exception e) {
			Log.d(TAG, e.toString());
		}
	}

	private boolean isSingleSim() {
		boolean single = false;
		String shortcode = mTStruct.getItem(TracabilityStruct.ID.SHORT_CODE_I)
				.toString();
		
//		String[] sbt = new String[bt.length];
//		System.arraycopy(bt, 0, sbt, 0, bt.length);
//		for(byte b : bt){
//			Tool.toolLog(TAG + " b " + b);
//		}
//		for(String st : sbt){
//			Tool.toolLog(TAG + " st " + st);
//		}
//		Tool.toolLog(TAG + " shortcode " + shortcode);
		// PR475446-yancan-zhu-20130621 begin
		if (shortcode.startsWith("7FJ") || shortcode.startsWith("8FJ")
				|| shortcode.startsWith("9FJ")) {
			// PR475446-yancan-zhu-20130621 end
			single = true;/* Single sim 7FJ,8F,9FJ */
		}
		// PR490336-yancan-zhu-20130717 begin
		if (shortcode.startsWith("AFJ")) {
			// PR490336-yancan-zhu-20130717 end
			single = false;/* Dual sim AFJ */
		}
		return single;
	}

	@Override
	public void setUp() {
		// TODO Auto-generated method stub
		Tool.toolLog(TAG + "_start_test");
		testCountDownTimer = new TestCountDownTimer(SECOND*5, SECOND, this);

		// TODO Auto-generated method stub
		mTm = (TelephonyManager) mContext
				.getSystemService(Context.TELEPHONY_SERVICE);
		// Log.d(TAG, "current oprt_mode = " +
		// JRDRapi.OprtMode.valToString(mOprtMode));

		if (/*FeatureOption.MTK_GEMINI_SUPPORT == true && */!isSingleSim()) {
			// boolean sim1State = mTm.hasIccCardGemini(GEMINI_SIM_1);
			// boolean sim2State = mTm.hasIccCardGemini(GEMINI_SIM_2);
//			boolean sim1State = TelephonyManagerEx.getDefault().hasIccCard(
//					GEMINI_SIM_1);
//			boolean sim2State = TelephonyManagerEx.getDefault().hasIccCard(
//					GEMINI_SIM_2);
			sim1State = mTm.hasIccCard();
			if (sim1State == true)
				mDisplaySIM1TestString = "SIM1 card is detected";
			else
				mDisplaySIM1TestString = "SIM1 card is missing";

//			if (sim2State == true)
//				mDisplaySIM2TestString = "SIM2 card is detected";
//			else
//				mDisplaySIM2TestString = "SIM2 card is missing";

			mDisplaySIMTestString = mDisplaySIM1TestString + "\n"
					+ mDisplaySIM2TestString;
			if (sim1State /*&& sim2State*/) {
				bSIMCardOk = true;
			}

		} else {
			boolean simState = mTm.hasIccCard();
			if (simState == true)
				mDisplaySIMTestString = "SIM card is detected";
			else
				mDisplaySIMTestString = "SIM card is missing";
			if (simState == true) {
				bSIMCardOk = true;
			}
		}
	}

	@Override
	public void initView() {
		// TODO Auto-generated method stub
		mLayout = (LinearLayout) View.inflate(mContext, R.layout.base_screen,
				null);

//		bt_left = (Button) mLayout.findViewById(R.id.bt_left);
//		bt_right = (Button) mLayout.findViewById(R.id.bt_right);
		text_top_zone = (TextView) mLayout.findViewById(R.id.text_top_zone);
		text_cen_zone = (TextView) mLayout.findViewById(R.id.text_cen_zone);

		text_top_zone.setText(mName);
		text_cen_zone.setText(mDisplaySIMTestString);
		//PR661395-yinbin-zhang-20140519 begin
		/*bt_left.setText(R.string.fail);
		bt_right.setText(R.string.pass);
		if (!bSIMCardOk) {
			bt_right.setEnabled(false);
		}

		bt_left.setOnClickListener(failed_listener);
		bt_right.setOnClickListener(pass_listener);*/
//		bt_left.setText(R.string.pass);
//		bt_right.setText(R.string.fail);
		if (!bSIMCardOk) {
//			bt_left.setEnabled(false);
		}

//		bt_left.setOnClickListener(pass_listener);
//		bt_right.setOnClickListener(failed_listener);
		//PR661395-yinbin-zhang-20140519 end

		mContext.setContentView(mLayout);
//		if (bSIMCardOk) {
			testCountDownTimer.start();
//		}

		mHandler.sendEmptyMessageDelayed(0, SECOND);	
		
	}

	public Handler mHandler = new Handler(){
		
		@Override
		public void handleMessage(Message msg) {
			
			if (sim1State == true){
				//Add by Jianke.Zhang 02/02
			    FinishThread thread = new FinishThread(0x01,ExecuteTest.temppositon);
			    thread.start();
			    try {
	                thread.join();
	            } catch (InterruptedException e) {
	                // TODO Auto-generated catch block
	                e.printStackTrace();
	            }
			    Msg.exitWithSuccessTest(mContext,TAG,20,true,"Pass");
				
				//Write data into file
				if(AllMainActivity.mainAllTest && !AllMainActivity.autofileFlag){
					Msg.WriteModelResult(mContext,AllMainActivity.all_items_file_text, "PASS");
				}else{
					Msg.WriteModelResult(mContext,autoRunActivity.auto_file_text, "PASS");
				}
				//End
			}
		}
	};
	
	
	@Override
	public void onTick() {
		// TODO Auto-generated method stub
		// try {
		// remote = new JRDRapi(mContext);
		// } catch (IOException e) {
		// Log.e(TAG, "JRDRapi() error" + e);
		// }
		// remote.setOnlineMode();
		// mOprtMode = remote.getOprtMode();
		//Tool.toolLog(TAG + " onTick ...");
	}

	@Override
	public void onFinish() {
		// TODO Auto-generated method stub
//		mContext.setResult(Test.RESULT.PASS.ordinal());
		Tool.toolLog(TAG + " onFinish ... ");
            {
			mContext.setResult(Test.RESULT.FAILED.ordinal());
			Tool.toolLog(TAG + " index 888 -> " + ExecuteTest.temppositon);
			int double_test;
			if(AllMainActivity.mainAllTest){
				double_test = AllMainActivity.double_check_for_auto[ExecuteTest.temppositon]++;
				//AllMainActivity.mainAllTest = false;
			}else {
				double_test = autoRunActivity.double_check[ExecuteTest.temppositon]++;
			}
			Tool.toolLog(TAG + " double_test 999-> " + double_test);
			FinishThread finishThread=	new FinishThread(0x02,ExecuteTest.temppositon);
			finishThread.start();
			try {
                finishThread.join();
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
			if(double_test==1){
				//Add by Jianke.Zhang 02/02
				
				Msg.exitWithException(mContext,TAG,20,true,"Pass");
				
				//Write data into file 06/17 bug
				if(AllMainActivity.mainAllTest && !AllMainActivity.autofileFlag){
					Msg.WriteModelResult(mContext,AllMainActivity.all_items_file_text, "FAIL");
				}else{
					Msg.WriteModelResult(mContext,autoRunActivity.auto_file_text, "FAIL");
				}
				//End
			}else{
				Msg.exitWithException(mContext,TAG,20,true,"Fail");
				
			}
			
		}
	}

	@Override
	public void finish() {
		// TODO Auto-generated method stub
		Tool.toolLog(TAG + " finish");
	}

	public String getmContextTag() {
		// TODO Auto-generated method stub
		return TAG;
	}

	public void setmContextTag() {
		// TODO Auto-generated method stub
	}

	@Override
	public void destroy() {
		// TODO Auto-generated method stub
		if (testCountDownTimer != null) {
			testCountDownTimer.cancel();
			testCountDownTimer = null;
		}
	}

}
