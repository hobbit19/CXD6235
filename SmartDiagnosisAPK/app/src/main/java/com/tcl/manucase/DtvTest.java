package com.tcl.manucase;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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

public class DtvTest extends Test implements DownTimeCallBack {
    private static final String TAG = "DTVTest";
    private TestCountDownTimer testCountDownTimer = null;

    private static boolean DTV_LOOP = false;
    private final HeadInsertBroadcastReceiver mHeadsetStatus = new HeadInsertBroadcastReceiver();

    public DtvTest(ID id, String name) {
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
        testCountDownTimer = new TestCountDownTimer(10*SECOND, SECOND, this);
        testCountDownTimer.start();
    }

    @Override
    public void initView() {
        mLayout = (LinearLayout) View.inflate(mContext,
                R.layout.base_screen, null);

        text_top_zone = (TextView) mLayout.findViewById(R.id.text_top_zone);
        text_cen_zone = (TextView) mLayout.findViewById(R.id.text_cen_zone);
        text_top_zone.setText(mName);
        mContext.setContentView(mLayout);

        IntentFilter intentFilter = new IntentFilter(Intent.ACTION_HEADSET_PLUG);
        getContext().registerReceiver(mHeadsetStatus, intentFilter);
        state = INIT ;
        DTV_LOOP = false;
        run();
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
        getContext().unregisterReceiver(mHeadsetStatus);
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == 1) { // pass
            pass();
        } else if (resultCode == 0) { // fail
            failed();
        }
        DTV_LOOP = false;
    }

    private static int state;
    private final int OUT = 0, IN = 1;
    private final int INIT = 0;
    private final int HEADSET_IN = INIT + 1;
    //private final int HEADSET_DTV = INIT + 2;
    private final int HEADSET_REMOVE = INIT + 2;
    private final int HEADSET_IN_END = INIT + 3;
    class HeadInsertBroadcastReceiver extends BroadcastReceiver {

        public synchronized void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(Intent.ACTION_HEADSET_PLUG)) {
                int mPlugStatus = intent.getIntExtra("state", -1);
                if (mPlugStatus != IN) {
                    if (state == HEADSET_REMOVE){
                        pass();
                    }
                }else{
                    if(!DTV_LOOP) {
                        state++;
                        run();
                    }
                }
            }
        }
    }

    private void run() {
        switch (state) {
            case INIT:
                text_cen_zone.setText("Please insert headset");
                break;
            case HEADSET_IN:
                try {
                    Intent intent = new Intent();
                    intent.setClassName("kr.co.fci.tv.factory","kr.co.fci.tv.factory.MainActivity");
                    getContext().startActivityForResult(intent, 0);
                    DTV_LOOP = true;
                } catch (Exception e) {
                    DTV_LOOP = false;
                }
                break;
            case HEADSET_REMOVE:
                text_cen_zone.setText("Please remove headset");
                break;

        }
    }
}
