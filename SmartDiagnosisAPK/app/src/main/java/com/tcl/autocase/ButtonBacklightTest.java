package com.tcl.autocase;

import android.os.Handler;
import android.os.PowerManager;
import android.view.View;
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

import java.io.FileWriter;
import java.io.IOException;

/**
 * creat by nanbing.zou for A5A INFINI at 2017-11-23
 */

public class ButtonBacklightTest extends Test implements DownTimeCallBack{
	private static final String TAG = "ButtonBacklightTest";
	String text = "Is " + mName + " Backlight flashing?";
	int mFlag = 1;
	PowerManager pm = null;
	private boolean Flag = false;

	public ButtonBacklightTest(ID id, String name) {
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
		// TODO Auto-generated method stub
		Tool.toolLog(TAG + "_start_test");
		if(mHandler!=null) {
		  mFlag = 1;
		  mHandler.postDelayed(runnable,500);
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

    
    public void setbacklight(Boolean flag) throws IOException {
        if (flag) {
//        	Tool.toolLog(TAG + " -> 11111111111111");
            writeLcdFile("/sys/class/leds/lcd-backlight/brightness", 255);
//            Tool.toolLog(TAG + " backlight on !");
        } else {
            writeLcdFile("/sys/class/leds/lcd-backlight/brightness", 102);
//            Tool.toolLog(TAG + " backlight off !");
        }
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
		
	}
	
}
