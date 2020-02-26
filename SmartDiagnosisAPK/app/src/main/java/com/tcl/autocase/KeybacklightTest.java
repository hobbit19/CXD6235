package com.tcl.autocase;

import android.os.Handler;
import android.os.PowerManager;
import android.view.View;
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

import java.io.FileWriter;
import java.io.IOException;


public class KeybacklightTest extends Test implements DownTimeCallBack{
	private static final String TAG = "KeybacklightTest";
	String text = "Is " + mName + " Backlight flashing?";
	int mFlag = 1;
	PowerManager pm = null;
	//PR661408-yinbin-zhang-20140516 begin
	private FrameLayout mFrameLayout;
	private LinearLayout mLinearLayout;
	private Button retestButton;
	private boolean Flag = false;
	//PR661408-yinbin-zhang-20140516 end

	public KeybacklightTest(ID id, String name) {
		super(id, name);
		// TODO Auto-generated constructor stub
	}

	private Handler mHandler = new Handler();
	private Runnable runnable = new Runnable() {
	    @Override
	    public void run() {
	        // TODO Auto-generated method stub
//	    	Tool.toolLog(TAG + " run 5554545");
	    	try{
	    		if(mFlag%2!=0) {
		    		//if(mId == Test.ID.BACKLIGHT.ordinal()){
		    			setbacklight(true);
		    		//}else{
		    			setKpdlight(true);
		    		//}
		    	} else {
		    		//if(mId == Test.ID.BACKLIGHT.ordinal()){
		    			setbacklight(false);
		    		//}else{
		    			setKpdlight(false);
		    		//}
		    	}
		    	mFlag++;
		    	mHandler.postDelayed(this, 500);
		    	//PR661408-yinbin-zhang-20140516 begin
		    	/*if(mFlag>=7) {
		    		mHandler.removeCallbacks(runnable);
		    	}*/
//		    	Tool.toolLog(TAG + " mFlag -> " + mFlag);
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
		    		//End
//		    		mHandler.removeCallbacks(runnable);
//		    		Tool.toolLog(TAG + " Finish Test keypad light and lcd light!");
		    		//Add by Jianke.Zhang 01/27 Write data
		    		if(AllMainActivity.mainAllTest && !AllMainActivity.autofileFlag){
		    			Msg.WriteModelResult(mContext,AllMainActivity.all_items_file_text, "PASS");
		    		}else {
		    			Msg.WriteModelResult(mContext,autoRunActivity.auto_file_text, "PASS");
					}
		    		//End
//		    		bt_left.setEnabled(true);
//		    		bt_right.setEnabled(true);
//		    		retestButton.setEnabled(true);
//		    		Tool.toolLog(TAG + " finally ... 88888888");
		    		mHandler.removeCallbacks(runnable);
		    	}
	    	}catch(Exception e){
	    		Tool.toolLog(TAG + " -> " + e.getMessage());
	    		mContext.setResult(RESULT.FAILED.ordinal());
				Tool.toolLog(TAG + " index 888 -> " + ExecuteTest.temppositon);
				int double_test;
				if(AllMainActivity.mainAllTest){
					double_test = AllMainActivity.double_check_for_auto[ExecuteTest.temppositon]++;
					//AllMainActivity.mainAllTest = false;
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
	    		
//	    		Tool.toolLog(TAG + " finally ... 99999999");
	    		mHandler.removeCallbacks(runnable);
	    	}
	    	//PR661408-yinbin-zhang-20140516 begin
	    }
	};

	@Override
	public void setUp() {
		// TODO Auto-generated method stub
//		Tool.toolLog(TAG + " setUp ... ");
		Tool.toolLog(TAG + "_start_test");
//		pm = (PowerManager) mContext.getSystemService(Context.POWER_SERVICE);
		if(mHandler!=null) {
//		  Tool.toolLog(TAG + " mFlag ============ " + mFlag);
		  mFlag = 1;
		  mHandler.postDelayed(runnable,500);
		}
	}

	@Override
	public void initView() {
		// TODO Auto-generated method stub
//		Tool.toolLog(TAG + " initView ... ");
		mLayout = (LinearLayout)View.inflate(mContext,R.layout.base_screen, null);

//		bt_left = (Button) mLayout.findViewById(R.id.bt_left);
//		bt_right = (Button) mLayout.findViewById(R.id.bt_right);
		text_top_zone = (TextView)mLayout.findViewById(R.id.text_top_zone);
//		text_cen_zone = (TextView)mLayout.findViewById(R.id.text_cen_zone);

		text_top_zone.setText(mName);
		mContext.setContentView(mLayout);
//		text_cen_zone.setText(text);
		//PR661408-yinbin-zhang-20140516 begin
		/*bt_left.setText(R.string.fail); 
		bt_right.setText(R.string.pass);
		
		bt_left.setOnClickListener(failed_listener);
		bt_right.setOnClickListener(pass_listener);
		
		mContext.setContentView(mLayout);*/
		
//		bt_left.setText(R.string.pass); 
//		bt_right.setText(R.string.fail);
//		
//		bt_left.setOnClickListener(pass_listener);
//		bt_right.setOnClickListener(failed_listener);
		
//		mFrameLayout = new FrameLayout(mContext);
//		mLinearLayout = new LinearLayout(mContext);
//		mLinearLayout.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
//		mLinearLayout.setGravity(Gravity.BOTTOM);
//		retestButton = new Button(mContext);
//		retestButton.setText("Retest");
//		LinearLayout.LayoutParams mLayoutParams = new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
//		mLayoutParams.bottomMargin = 80;
//		mLinearLayout.addView(retestButton,mLayoutParams);
//		retestButton.setOnClickListener(new OnClickListener() {
//			
//			@Override
//			public void onClick(View v) {
//				// TODO Auto-generated method stub
//				Flag = true;
//				mFlag = 2;
//				mHandler.postDelayed(runnable, 500);
//			}
//		});
//		
//		mFrameLayout.addView(mLinearLayout);
//		mFrameLayout.addView(mLayout);
//		mContext.setContentView(mFrameLayout);
//		
//		bt_left.setEnabled(false);
//		bt_right.setEnabled(false);
//		retestButton.setEnabled(false);
		//PR661408-yinbin-zhang-20140516 end
	}
	
	
    public  void writeLcdFile(String filename, int value) throws IOException {
    	FileWriter writer = new FileWriter(filename);
//    	Tool.toolLog(TAG + " -> 3333333333333");
        writer.write(new Integer(value).toString());
        writer.close();
    }
	
    public void setKpdlight(Boolean flag) throws IOException {
        if (flag) {
//        	Tool.toolLog(TAG + " -> 22222222222222");
            writeLcdFile("/sys/class/leds/button-backlight/brightness", 255);
//            Tool.toolLog(TAG + " Kpdlight on !");
        } else {
            writeLcdFile("/sys/class/leds/button-backlight/brightness", 0);
//            Tool.toolLog(TAG + " Kpdlight off !");
        }
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
		//PR661408-yinbin-zhang-20140516 begin
//		Tool.toolLog(TAG + " -> finish");
//		setbacklight(false);
//		setKpdlight(false);
		//PR661408-yinbin-zhang-20140516 end
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
