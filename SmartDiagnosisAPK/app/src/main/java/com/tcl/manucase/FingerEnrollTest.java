package com.tcl.manucase;

import android.content.ComponentName;
import android.content.Intent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.tcl.autotest.R;
import com.tcl.autotest.tool.Tool;
import com.tcl.autotest.utils.DownTimeCallBack;
import com.tcl.autotest.utils.Test;
import com.tcl.autotest.utils.TestCountDownTimer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

/**
 * creat by nanbing.zou for A5A INFINI at 2017-11-23
 */

public class FingerEnrollTest extends Test implements DownTimeCallBack {
    private static final String TAG = "FingerprintTest";
    private static final String FP_PATH = "/sys/class/deviceinfo/device_info/fp";
    private static final String GOODIX = "goodix";
    private static final String SYNA = "syna";
    private static final String FOCAL = "FocalTech";
    String mDisplay = "";

    private String mstr = "";
    private TestCountDownTimer testCountDownTimer = null;
    public FingerEnrollTest(ID id, String name) {
        super(id, name);
    }

    @Override
    public void onFinish() {
        try {
            Runtime.getRuntime().exec("input keyevent 4 ");
        } catch (IOException e) {
            e.printStackTrace();
        }
        failed();
    }

    @Override
    public void onTick() {

    }

    @Override
    public void setUp() {
        Tool.toolLog(TAG + "_start_test");
        Intent mIntent = new Intent();
        testCountDownTimer = new TestCountDownTimer(10*SECOND, SECOND, this);
        testCountDownTimer.start();
        ComponentName toActivity = null;
        String fp = readFile(FP_PATH);
        if(fp == null){
            failed();
            return;
        }
        if (fp.contains(GOODIX)) {
            toActivity = new ComponentName("com.jrdcom.mmitest",
                    "com.goodix.gf3266.HomeActivity");
        } else if (fp.contains(SYNA)) {
            toActivity = new ComponentName("com.jrdcom.mmitest",
                    "com.jrdcom.mmitest.tests.FingerPrintActivitySyna");
        }else if (fp.contains(FOCAL))
        {
            toActivity = new ComponentName("com.jrdcom.mmitest", "com.focal.idol5.FingerprintTestActivityS");
        }
        if (toActivity != null) {
            mIntent.setComponent(toActivity);
            getContext().startActivityForResult(mIntent, 0);
        } else {
            text_cen_zone.setText("the wrong fingerprint ic information");
            failed();
        }

    }

    @Override
    public void initView() {
        mLayout = (LinearLayout) View.inflate(mContext,
                R.layout.base_screen, null);

        text_top_zone = (TextView) mLayout.findViewById(R.id.text_top_zone);
        text_cen_zone = (TextView) mLayout.findViewById(R.id.text_cen_zone);
        text_top_zone.setText(mName);
        mContext.setContentView(mLayout);

    }

    @Override
    public void finish() {

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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == 1) { // pass
            pass();
        } else if (resultCode == 0) { // fail
            failed();
        }
    }


}
