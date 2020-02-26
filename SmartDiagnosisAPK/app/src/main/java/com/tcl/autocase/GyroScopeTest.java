package com.tcl.autocase;

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

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * creat by nanbing.zou for A5A INFINI at 2017-11-23
 */

public class GyroScopeTest extends Test implements SensorEventListener,DownTimeCallBack {

    private final static String TAG = "GyroScopeTest";
    private Sensor mGyroScopeSensor;
    private SensorManager mSensorManager;
    protected  LinearLayout mLayout = null;
    protected TextView text_top_zone = null;
    protected TextView text_cen_zone = null;
    private double timestamp;
    private static final double R2D = 180.0f / (double) Math.PI;
    private static final double NS2S = 1.0f / 1000000000.0f;
    private double integratedValues[] = new double[3];
    private double lastValues[] = new double[3];
    private boolean hasSentMsg = false;
    private TestCountDownTimer testCountDownTimer = null;


    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            onSucess();
        }
    };

    @Override
    public void setUp() {

        testCountDownTimer = new TestCountDownTimer(20*SECOND, SECOND, this);
        testCountDownTimer.start();
        Tool.toolLog(TAG + "_start_test");
    }

    public GyroScopeTest(ID id, String name, Boolean updateFlag, long tout) {
        super(id, name, updateFlag, tout);
    }

    @Override
    public void initView() {
        mLayout = (LinearLayout)View.inflate(mContext, R.layout.base_screen, null);
        text_top_zone = (TextView)mLayout.findViewById(R.id.text_top_zone);
        text_top_zone.setText(mName);
        text_cen_zone = (TextView)mLayout.findViewById(R.id.text_cen_zone);
        mContext.setContentView(mLayout);
        testGyroScope();

    }

    @Override
    public void finish() {
        if(testCountDownTimer!=null){
            testCountDownTimer.cancel();
        }

    }

    private void testGyroScope() {
        timestamp = 0;
        integratedValues[0] = 0;
        integratedValues[1] = 0;
        integratedValues[2] = 0;
        mSensorManager = (SensorManager) getContext().getSystemService(
                Context.SENSOR_SERVICE);
        mGyroScopeSensor = mSensorManager
                .getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        if (mGyroScopeSensor != null) {
            boolean isListenerOK = mSensorManager.registerListener(this,
                    mGyroScopeSensor, 500000);
            if (!isListenerOK) {
                String failString = "register listener for GyroScopeSensor "
                        + mGyroScopeSensor.getName() + " failed";
                text_cen_zone.setText(failString);
            } else {
                text_cen_zone.setText("Support GyroScope!");
            }
        } else {
            text_cen_zone.setText("Not Support GyroScope!");
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
        if (mGyroScopeSensor != null) {
            mSensorManager.unregisterListener(this, mGyroScopeSensor);
        }
        if (testCountDownTimer != null) {
            testCountDownTimer.cancel();
            testCountDownTimer = null;
        }

    }

    @Override
    public void timeout() {
        super.timeout();
    }

    @Override
    public void pass() {
        super.pass();
    }

    @Override
    public void failed() {
        super.failed();
    }

    @Override
    public void handleMessage(Message msg) {
        super.handleMessage(msg);
    }

    @Override
    public long getTimeOut() {
        return super.getTimeOut();
    }

    @Override
    public View getLayout() {
        return super.getLayout();
    }

    @Override
    public Activity getContext() {
        return super.getContext();
    }

    @Override
    public int getId() {
        return super.getId();
    }


    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.values[0] != 0 || event.values[1] != 0
                || event.values[2] != 0) {
            if (timestamp == 0) {
                timestamp = event.timestamp;
                lastValues[0] = event.values[0];
                lastValues[1] = event.values[1];
                lastValues[2] = event.values[2];
            } else {
                String cenTextStr = "BIST test: PASS\n";
                final double dT = (event.timestamp - timestamp) * NS2S;
                integratedValues[0] += (event.values[0] + lastValues[0]) / 2.0f
                        * dT;
                integratedValues[1] += (event.values[1] + lastValues[1]) / 2.0f
                        * dT;
                integratedValues[2] += (event.values[2] + lastValues[2]) / 2.0f
                        * dT;
                cenTextStr += (String
                        .format("Vx: %f\nVy: %f\nVz: %f\n\ndx: %.2f\ndy: %.2f\ndz: %.2f",
                                event.values[0], event.values[1],
                                event.values[2], integratedValues[0] * R2D,
                                integratedValues[1] * R2D, integratedValues[2]
                                        * R2D));
                timestamp = event.timestamp;
                lastValues[0] = event.values[0];
                lastValues[1] = event.values[1];
                lastValues[2] = event.values[2];
                text_cen_zone.setText(cenTextStr);
                if(!hasSentMsg) {
                    hasSentMsg = true;
                    mHandler.sendEmptyMessageDelayed(0, 2000);
                }
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }




    @Override
    public void onFinish() {
        onFail();

    }

    @Override
    public void onTick() {

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
        mContext.setResult(Test.RESULT.FAILED.ordinal());
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
