package com.tcl.manucase;

import com.tcl.autotest.AllMainActivity;
import com.tcl.autotest.ExecuteTest;
import com.tcl.autotest.autoRunActivity;
import com.tcl.autotest.tool.Tool;
import com.tcl.autotest.utils.DownTimeCallBack;
import com.tcl.autotest.utils.FinishThread;
import com.tcl.autotest.utils.Msg;
import com.tcl.autotest.utils.Test;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import com.tcl.autotest.R;
import com.tcl.autotest.utils.TestCountDownTimer;

/**
 * creat by nanbing.zou for A5A INFINI at 2017-11-23
 */

public class GsensorCallibrationTest extends Test implements SensorEventListener,DownTimeCallBack {

    private static final String TAG = "GsensorCallibrationTest";
    private SensorManager mSensorManager;
    private Sensor gsensor;
    private int div;
    private int num = 0;
    private String displayStr;
    private String gsensorStr;
    private int x_rawdata;
    private int y_rawdata;
    private int z_rawdata;
    private boolean hasSendMsg = false;
    float x_frawdata;
    float y_frawdata;
    float z_frawdata;
    private TestCountDownTimer testCountDownTimer = null;
    private final String RAW_SENSOR_DATA_PATH = "/sys/bus/platform/drivers/gsensor/sensorrawdata";
    private final String ACCACTIVE_PATH = "/sys/class/misc/m_acc_misc/accactive";

    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            pass();
        }
    };

    public GsensorCallibrationTest(ID id, String name) {
        super(id, name);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if ((num ++) % 15 != 0) {
            return;
        }

        try {
            updateDisplay(event);
        } catch (Exception e) {
            e.printStackTrace();
            displayStr = "G-sensor Correct error";
            text_cen_zone.setText(displayStr);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public void setUp() {
        Tool.toolLog(TAG + "_start_test");
        String accactivestr = readFile(ACCACTIVE_PATH);
        if(accactivestr == null){
            failed();
            return;
        }
        div = Integer.parseInt(accactivestr);
        mSensorManager = (SensorManager) getContext().getSystemService(Context.SENSOR_SERVICE);
        gsensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        if(gsensor != null)
            mSensorManager.registerListener(this, gsensor, SensorManager.SENSOR_DELAY_NORMAL);
        testCountDownTimer = new TestCountDownTimer(10*SECOND, SECOND, this);
        testCountDownTimer.start();
    }

    @Override
    public void initView() {
        mLayout = (LinearLayout) View.inflate(mContext, R.layout.base_screen, null);
        text_top_zone = (TextView)mLayout.findViewById(R.id.text_top_zone);
        text_top_zone.setText(mName);
        text_cen_zone = (TextView)mLayout.findViewById(R.id.text_cen_zone);
        displayStr = "*********************************" +
                "\nBefore Gsensor Calibrate" +
                "\n pls keep MS Horizontal !!!" +
                "\n*********************************\n\n\n" ;

        text_cen_zone.setText(displayStr);
        mContext.setContentView(mLayout);
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
        if(mSensorManager != null)
        {
            mSensorManager.unregisterListener(this, gsensor);
            mSensorManager = null;
        }
        if (testCountDownTimer != null) {
            testCountDownTimer.cancel();
            testCountDownTimer = null;
        }

    }


    private String readFile(String path) {
        BufferedReader readbuffer = null;
        String bufferValue = null;
        try {
            File file = new File(path);
            if (file.exists()) {
                readbuffer = new BufferedReader(new FileReader(file));
                bufferValue = readbuffer.readLine();
            }
        } catch (IOException e) {
        } finally {
            try {
                if (readbuffer != null)
                    readbuffer.close();
            } catch (IOException e) {
            }
        }
        return bufferValue;
    }

    private void updateDisplay(SensorEvent event) {
        //Log.d("yuduan.xie", "updateDisplay()");

        float Vx = event.values[0];
        float Vy = event.values[1];
        float Vz = event.values[2];

        String sensorResult = readFile(RAW_SENSOR_DATA_PATH);

        String[] tmp = sensorResult.split(";");

        x_rawdata = Integer.parseInt(tmp[0]);
        y_rawdata = Integer.parseInt(tmp[1]);
        z_rawdata = Integer.parseInt(tmp[2]);

        x_frawdata = (float)x_rawdata / div;
        y_frawdata = (float)y_rawdata / div;
        z_frawdata = (float)z_rawdata / div;

        gsensorStr = "Vx : " + Vx + "\nVy : " + Vy + "\nVz : " + Vz;

        if(x_frawdata > -1.5 && x_frawdata < 1.5 && y_frawdata > -3.0 && y_frawdata < 3.0 && z_frawdata > 8.3 && z_frawdata < 11.3)
        {
            if (Vx > -0.5 && Vx < 0.5 && Vy > -0.5 && Vy < 0.5 && Vz > 9.3 && Vz < 10.3) {

                text_cen_zone.setText(gsensorStr);
                if(!hasSendMsg){
                    hasSendMsg = true;
                    mHandler.sendEmptyMessageDelayed(0,2000);
                }

            } else //if(Vx > -1.5 && Vx < 1.5 && Vy > -3.0 && Vy < 3.0 && Vz > 8.3 && Vz < 11.3)
            {
                //
            }
        }
        else {
            if(!hasSendMsg) {
                hasSendMsg = true;
                failed();
            }
        }
    }

    @Override
    public void onFinish() {
        failed();
    }

    @Override
    public void onTick() {

    }
}
