package com.tcl.autocase;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
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
import com.tcl.autotest.R;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

/**
 * Created by user on 18-4-10.
 */

public class HallTest extends Test implements DownTimeCallBack {

    private final static String TAG = "HallTest";
    private TestCountDownTimer testCountDownTimer = null;

    private static final String CLOSE = "0";
    private static final String OPEN = "1";
    private boolean hasClose = false;
    private boolean hasOpen = false;
    String path = "/sys/class/hall_switch/hall_switch/hall_status";
    public static final int HALL_COLSED = 292;
    public static final int HALL_OPEN = 293;
    private HallReceiver mHallReceiver = new HallReceiver();

    public HallTest(ID id, String name) {
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
    public boolean onKeyTouch(KeyEvent event) {
        int KeyCode = event.getKeyCode();
        Message msg = new Message();
        switch (KeyCode){
            case HALL_COLSED:
                Tool.toolLog(TAG + " KeyCode is HALL_COLSED ");
                msg.what = HALL_COLSED;
                mHandler.sendMessage(msg);
                break;
            case HALL_OPEN:
                Tool.toolLog(TAG + " KeyCode is HALL_OPEN ");
                msg.what = HALL_OPEN;
                mHandler.sendMessage(msg);
                break;
        }
        return true;
    }
    private Handler mHandler = new Handler(){
        @Override
        public void dispatchMessage(Message msg) {
            switch (msg.what){
                case HALL_COLSED:
                    Tool.toolLog(TAG + " dispatchMesage HALL_COLSED ");
                    hasClose = true;
                    break;
                case HALL_OPEN:
                    Tool.toolLog(TAG + " dispatchMessage HALL_OPEN ");
                    hasOpen = true;
                    break;
            }
            String display = "close : " + (hasClose ? "OK" : "not tested") +
                    "\nopen : " + (hasOpen ? "OK" : "not tested");
            text_cen_zone.setText(display);
            if(hasClose && hasOpen){
                Tool.toolLog(TAG + " onSucess ");
                mContext.setResult(RESULT.PASS.ordinal());
            }else {
                Tool.toolLog(TAG + " onFail ");
//                mContext.setResult(RESULT.FAILED.ordinal());
            }
            super.dispatchMessage(msg);
        }
    };

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

        if (AllMainActivity.deviceName.contains("E8")){
            return;
        }

        IntentFilter mIntentFilter = new IntentFilter();
        mIntentFilter.addAction("com.jrdcom.mmitest.tests_HallTest");
        getContext().registerReceiver(mHallReceiver, mIntentFilter);

        testHall();
    }

    @Override
    public void finish() {
        if(testCountDownTimer!=null){
            testCountDownTimer.cancel();
        }
    }
    private void testHall() {
        String hallResult = readFile(path);
        if (CLOSE.equals(hallResult)) {
            hasClose = true;
        } else if (OPEN.equals(hallResult) && hasClose) {
            hasOpen = true;
        }
        String display = "close : " + (hasClose ? "OK" : "not tested") +
                "\nopen : " + (hasOpen ? "OK" : "not tested");
        text_cen_zone.setText(display);
        if(hasClose && hasOpen){
            onSucess();
        }else {
            onFail();
        }
    }

    public static String readFile(String path) {
        BufferedReader readbuffer = null;
        String bufferValue = null;
        try {
            File file = new File(path);
            if (file.exists()) {
                readbuffer = new BufferedReader(new FileReader(file));
                bufferValue = readbuffer.readLine();
                Tool.toolLog(TAG+ "File node value is " + bufferValue);
            }
        } catch (IOException e) {
            Tool.toolLog(TAG+"readFileNote() throws IOException: " + e);
        } finally {
            try {
                if (readbuffer != null)
                    readbuffer.close();
            } catch (IOException e) {
                Tool.toolLog(TAG+"readFileNote() throws IOException: " + e);
            }
        }
        return bufferValue;
    }


    class HallReceiver extends BroadcastReceiver {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Tool.toolLog(TAG+"Hall changed : " + action);
            testHall();
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
        getContext().unregisterReceiver(mHallReceiver);
        if(testCountDownTimer!=null){
            testCountDownTimer.cancel();
        }
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
