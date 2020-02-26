package com.tcl.autocase;

import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.tcl.autotest.AllMainActivity;
import com.tcl.autotest.ExecuteTest;
import com.tcl.autotest.autoRunActivity;
import com.tcl.autotest.tool.Tool;
import com.tcl.autotest.utils.DownTimeCallBack;
import com.tcl.autotest.utils.FinishThread;
import com.tcl.autotest.utils.Msg;
import com.tcl.autotest.utils.Test;
import com.tcl.autotest.utils.TestCountDownTimer;
import com.tcl.autotest.utils.TracabilityStruct;
import com.tcl.autotest.R;
/**
 * Created by user on 18-4-10.
 */

public class HDCPTest extends Test implements DownTimeCallBack {

    private final static String TAG = "HDCPTest";
    private TestCountDownTimer testCountDownTimer = null;

    private TracabilityStruct mTraceabilityStruct;

    public HDCPTest(ID id, String name) {
        super(id, name);
    }

    @Override
    public void onFinish() {
        onFail();
    }

    @Override
    public void onTick() {

    }

    @Override
    public void setUp() {
        Test.gettag = TAG;
        Test.state = null;
        Tool.toolLog(TAG + "_start_test");
        testCountDownTimer = new TestCountDownTimer(10*SECOND, SECOND, this);
        testCountDownTimer.start();
    }

    @Override
    public void initView() {
        // TODO Auto-generated method stub
        mLayout = (LinearLayout) View.inflate(mContext, R.layout.base_screen,null);
        text_top_zone = (TextView) mLayout.findViewById(R.id.text_top_zone);
        text_cen_zone = (TextView) mLayout.findViewById(R.id.text_cen_zone);
        text_top_zone.setText(mName);
        mContext.setContentView(mLayout);
        testHdcp();
    }

    @Override
    public void finish() {
        if(testCountDownTimer!=null){
            testCountDownTimer.cancel();
        }
    }

    @Override
    public String getmContextTag() {
        return TAG;
    }

    @Override
    public void setmContextTag() {

    }

    @Override
    public void destroy() {
        if(testCountDownTimer!=null){
            testCountDownTimer.cancel();
        }
    }


    private void testHdcp() {
        String hdcpDisplay = getASCStringFromTrace(TracabilityStruct.ID.PPE_KPH_I);
        text_cen_zone.setText("HDCP Test : " + hdcpDisplay);
        if ("1".equals(hdcpDisplay)) {
            onSucess();
        }else {
            onFail();
        }
    }

    /*
    32 --> XX
    33 --> !
    48 --> 0
    65 --> A
    126 --> ~
    127 --> XX
*/
    private String getASCStringFromTrace(TracabilityStruct.ID id){
        try{
            mTraceabilityStruct = new TracabilityStruct();

            //TraceabilityStruct.ID.PPE_KPH_I need 1 byte
            byte[] mByte = { '5' };
            //if the TraceabilityStruct.ID.PPE_KPH_I has not value , use this to write
            //it will write the ASCII to nvram, '5' --> 53 .
            //note : it can't write byte'5' to nvram, but ASCII 53
            //mTraceabilityStruct.putItem(TraceabilityStruct.ID.PPE_KPH_I , mByte);

        } catch (Exception e){
            e.printStackTrace();
            return "0";
        }

        byte[] resArr = mTraceabilityStruct.getItem(id);    // it is the actual string from nvram
        if (resArr.length < 1) {
            return "0";
        }

        StringBuffer strReturn = new StringBuffer();
        for(int i = 0; i < resArr.length; i++) {
            strReturn.append(resArr[i]);
        }
        return strReturn.toString();
    }



    //	@Override
    public void onSucess() {
        // TODO Auto-generated method stub
//		Tool.toolLog(TAG + " onFinish");
        Test.state = "Pass";
        Tool.toolLog(TAG + " Test.state " + Test.state);
        FinishThread finishThread = new FinishThread(0x01, ExecuteTest.temppositon);
        finishThread.start();
        try {
            finishThread.join();
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
		/*Jianke
		 * ���Ҳ����Ҫͬ��*/
        Msg.exitWithSuccessTest(mContext, TAG, 10,true,"Pass");
        //Write data into file
        if(AllMainActivity.mainAllTest && !AllMainActivity.autofileFlag){
            Msg.WriteModelResult(mContext,AllMainActivity.all_items_file_text, "PASS");
        }else{
            Msg.WriteModelResult(mContext, autoRunActivity.auto_file_text, "PASS");
        }
        //End
    }

    public void onFail(){
//		Tool.toolLog(TAG + " onFail");
        //Begin Add By Jianke.Zhang 2015/01/14
        //Write data into file
        Test.state = "Fail";
        mContext.setResult(RESULT.FAILED.ordinal());
        Tool.toolLog(TAG + " index 8882 -> " + ExecuteTest.temppositon);
        int double_test;
        if(AllMainActivity.mainAllTest){
            double_test = AllMainActivity.double_check_for_auto[ExecuteTest.temppositon]++;
            //AllMainActivity.mainAllTest = false;
        }else {
            double_test = autoRunActivity.double_check[ExecuteTest.temppositon]++;
        }
        Tool.toolLog(TAG + " double_test 9992-> " + double_test);

        FinishThread tFinishThread = new FinishThread(0x02,ExecuteTest.temppositon);
        tFinishThread.start();
        try {
            tFinishThread.join();
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
		/*Jianke
		 * ���Ҳ����Ҫͬ��*/
        if(double_test==1){
            Msg.exitWithException(mContext,TAG,50,true,"Pass");
            if(AllMainActivity.mainAllTest && !AllMainActivity.autofileFlag){
                Msg.WriteModelResult(mContext,AllMainActivity.all_items_file_text, "FAIL");
            }else {
                Msg.WriteModelResult(mContext,autoRunActivity.auto_file_text, "FAIL");
            }
        }else{
            Msg.exitWithException(mContext,TAG,50,true,"Fail");
        }
        //End
    }
}
