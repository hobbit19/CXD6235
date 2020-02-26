package com.tcl.autocase;

import android.os.Handler;
import android.os.PowerManager;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

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

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
/**
 * creat by nanbing.zou for A5A INFINI at 2017-11-23
 */

public class LCDBacklightTest extends Test implements DownTimeCallBack{
	private static final String TAG = "LCDBacklightTest";
	String text = "Is " + mName + " Backlight flashing?";
	int mFlag = 1;
	PowerManager pm = null;
	private boolean Flag = false;
	private TestCountDownTimer testCountDownTimer = null;
	private List<String> LCDbl_project = new ArrayList();
	private void addProject(List<String> project){
		project.add("A5A_INFINI");
		project.add("Curie");
		project.add("Faraday");
		project.add("PIXI4-4C_GO");
		project.add("5006");
		project.add("FERMI_ATT");
		project.add("A3A_10_4G");
		project.add("Wright");
		project.add("EasyTAB8TMO");
		project.add("Venice");
		project.add("E8");
		project.add("Morgan_4G");
		project.add("VFD730");
		project.add("U5A_PLUS_4G");
		project.add("Pixi4PlusPower");
		project.add("A3A_XL_4G");
		project.add("ELSA6P");
		project.add("VFD500");
		project.add("mickey6");
		project.add("Hulk_7_GL_WIFI");
		project.add("Hulk_7_KIDS_WIFI");
		project.add("Benz");
		project.add("5007A");
		project.add("5028A");
		project.add("5028D");
		project.add("Seoul_GL_CAN");
		project.add("Seoul");
		project.add("Seoul_TF");
		project.add("Tokyo_TF");
		project.add("Tokyo");
		project.add("Tokyo_Lite");
		project.add("5029Y");
		project.add("5129Y");
		project.add("TokyoPro");
		project.add("Tokyo_CAN");

	}
	public LCDBacklightTest(ID id, String name) {
		super(id, name);
		// TODO Auto-generated constructor stub
	}

	private Handler mHandler = new Handler();
	private Runnable runnable = new Runnable() {
	    @Override
	    public void run() {
	        // TODO Auto-generated method stub
	    	try{
	    		if(mFlag%2!=0) {
		    			setbacklight(true);
		    	} else {
		    			setbacklight(false);
		    	}
		    	mFlag++;
		    	mHandler.postDelayed(this, 500);
		    	if(mFlag>=4) {
		    		if(!Flag)
		    		{
		    			try {
		    				Thread.sleep(1500);
						} catch (Exception e) {
							// TODO: handle exception
							e.printStackTrace();
						}
		    		}
		    		//Add by Jianke.Zhang 01/27
		    		FinishThread tFinishThread = new FinishThread(0x01,ExecuteTest.temppositon);
		    		tFinishThread.start();
		    		tFinishThread.join();
		    		Msg.exitWithSuccessTest(mContext, TAG, 20,true,"Pass");
		    		if(AllMainActivity.mainAllTest && !AllMainActivity.autofileFlag){
		    			Msg.WriteModelResult(mContext,AllMainActivity.all_items_file_text, "PASS");
		    		}else {
		    			Msg.WriteModelResult(mContext,autoRunActivity.auto_file_text, "PASS");
					}
		    		mHandler.removeCallbacks(runnable);
		    	}
	    	}catch(Exception e){
	    		Tool.toolLog(TAG + " -> " + e.getMessage());
	    		mContext.setResult(RESULT.FAILED.ordinal());
				Tool.toolLog(TAG + " index 888 -> " + ExecuteTest.temppositon);
				int double_test;
				if(AllMainActivity.mainAllTest){
					double_test = AllMainActivity.double_check_for_auto[ExecuteTest.temppositon]++;
				}else {
					double_test = autoRunActivity.double_check[ExecuteTest.temppositon]++;
				}
				Tool.toolLog(TAG + " double_test 999-> " + double_test);
				FinishThread tFinishThread = new FinishThread(0x02,ExecuteTest.temppositon);
				tFinishThread.start();
				try {
					tFinishThread.join();
				} catch (InterruptedException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				if(double_test==1){
		    		Msg.exitWithException(mContext,TAG,50,true,"Pass");
		    		if(AllMainActivity.mainAllTest && !AllMainActivity.autofileFlag){
		    			Msg.WriteModelResult(mContext,AllMainActivity.all_items_file_text, "FAIL");
		    		}else {
		    			Msg.WriteModelResult(mContext,autoRunActivity.auto_file_text, "FAIL");
					}
				}else {
		    		Msg.exitWithException(mContext,TAG,50,true,"Fail");
				}

	    		mHandler.removeCallbacks(runnable);
	    	}
	    	//PR661408-yinbin-zhang-20140516 begin
	    }
	};

	@Override
	public void setUp() {
		addProject(LCDbl_project);
		// TODO Auto-generated method stub
		Tool.toolLog(TAG + "_start_test");
		if(mHandler!=null && !(LCDbl_project.contains(AllMainActivity.deviceName))) {
		  mFlag = 1;
		  mHandler.postDelayed(runnable,500);
		}else {
			testCountDownTimer = new TestCountDownTimer(10*SECOND, SECOND, this);
			testCountDownTimer.start();
		}

	}

	@Override
	public void initView() {
		// TODO Auto-generated method stub
//		Tool.toolLog(TAG + " initView ... ");
		mLayout = (LinearLayout)View.inflate(mContext,R.layout.base_screen, null);
		text_top_zone = (TextView)mLayout.findViewById(R.id.text_top_zone);
		text_top_zone.setText(mName);
		mContext.setContentView(mLayout);
	}
	
	
    public  void writeLcdFile(String filename, int value) throws IOException {
    	FileWriter writer = new FileWriter(filename);
//    	Tool.toolLog(TAG + " -> 3333333333333");
        writer.write(new Integer(value).toString());
        writer.close();
    }

    private String lcdPath = "/sys/class/leds/lcd-backlight/brightness";
    public void setbacklight(Boolean flag) throws IOException {

        if (flag) {
//        	Tool.toolLog(TAG + " -> 11111111111111");
            writeLcdFile(lcdPath, 255);
//            Tool.toolLog(TAG + " backlight on !");
        } else {
            writeLcdFile(lcdPath, 102);
//            Tool.toolLog(TAG + " backlight off !");
        }
    }

	int index = 0;
	int[] backLightArray = {1, 255};
	private void setBrightness(int brightness)
	{
		 		WindowManager.LayoutParams  lp = getContext().getWindow().getAttributes();
		 		lp.screenBrightness =  (Float)Float.valueOf(brightness) * 1f/255f;
		 		getContext().getWindow().setAttributes(lp);
	}


	@Override
	public void finish() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onFinish() {
		// TODO Auto-generated method stub
//		Tool.toolLog(TAG + " onFinish");
	}

	@Override
	public void onTick() {
		// TODO Auto-generated method stub
		if(LCDbl_project.contains(AllMainActivity.deviceName)){
			setBrightness(backLightArray[index]);
			index = 1 - index;
			mFlag++;
			Tool.toolLog(TAG + "onTick:"+mFlag);
			if(mFlag > 5){
				FinishThread tFinishThread = new FinishThread(0x01,ExecuteTest.temppositon);
				tFinishThread.start();
				try {
					tFinishThread.join();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				Msg.exitWithSuccessTest(mContext, TAG, 20,true,"Pass");
				if(AllMainActivity.mainAllTest && !AllMainActivity.autofileFlag){
					Msg.WriteModelResult(mContext,AllMainActivity.all_items_file_text, "PASS");
				}else {
					Msg.WriteModelResult(mContext,autoRunActivity.auto_file_text, "PASS");
				}
			}
		}

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
			setBrightness(129);
        }
	}
	
}
