package com.tcl.manucase;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.tcl.autotest.AllMainActivity;
import com.tcl.autotest.tool.Tool;
import com.tcl.autotest.utils.Test;
import com.tcl.autotest.R;

public class HallTest extends Test{
    private final static String TAG = "HallTest";
    FrameLayout fl = null;
    public  int HALL_COLSED = 292;
    public  int HALL_OPEN = 293;

    private boolean hasClose = false;
    private boolean hasOpen = false;

    public HallTest(ID id,String name){
        super(id,name);
    }

    @Override
    public void initView() {
        bt_left = (Button) mLayout.findViewById(R.id.bt_left);
        bt_right = (Button) mLayout.findViewById(R.id.bt_right);
        text_top_zone = (TextView) mLayout.findViewById(R.id.text_top_zone);
        text_cen_zone = (TextView) mLayout.findViewById(R.id.text_cen_zone);

        text_top_zone.setText(mName);
        text_cen_zone.setText("close : not tested\nopen : not tested");
        bt_left.setText(R.string.pass);
        bt_right.setText(R.string.fail);
        bt_left.setOnClickListener(pass_listener);
        bt_right.setOnClickListener(failed_listener);
        bt_left.setEnabled(false);
        fl.addView(mLayout);
        mContext.setContentView(fl);




    }



    @Override
    public void setUp() {
        Tool.toolLog(TAG + "_start_test");
        fl = new FrameLayout(mContext);
        mLayout = (LinearLayout) View.inflate(mContext, R.layout.manu_base_screen, null);

        if (AllMainActivity.deviceName.equals("E8")){
            HALL_COLSED = 292;
            HALL_OPEN = 293;
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        Tool.toolLog(TAG + "onKeyDown Keycode== " + keyCode);
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        Tool.toolLog(TAG + "onKeyUp keyCode== " + keyCode);
        return super.onKeyUp(keyCode, event);
    }

    @Override
    public boolean onKeyTouch(KeyEvent event) {
        int KeyCode = event.getKeyCode();
        Message msg = new Message();
        Tool.toolLog(TAG + " onKeyTouch KeyCode== " + KeyCode);
        if (KeyCode == HALL_COLSED) {
            msg.what = HALL_COLSED;
            mHander.sendMessage(msg);
        }
        if (KeyCode == HALL_OPEN) {
            msg.what = HALL_OPEN;
            mHander.sendMessage(msg);
        }

        return super.onKeyTouch(event);
    }
    private Handler mHander = new Handler(){
        @Override
        public void dispatchMessage(Message msg) {
            if (msg.what == HALL_COLSED) {
                Tool.toolLog(TAG + " dispatchMesage HALL_COLSED ");
                hasClose = true;
            }
            if(msg.what == HALL_OPEN){
                    Tool.toolLog(TAG + " dispatchMessage HALL_OPEN ");
                    hasOpen = true;
            }
            String display = "close : " + (hasClose ? "OK" : "not tested") +
                    "\nopen : " + (hasOpen ? "OK" : "not tested");
            text_cen_zone.setText(display);
            if (hasClose && hasOpen){
                bt_left.setEnabled(true);
                mContext.setResult(RESULT.PASS.ordinal());
            }
            super.dispatchMessage(msg);
        }
    };


    @Override
    public void destroy() {

    }

    @Override
    public void setmContextTag() {

    }

    @Override
    public String getmContextTag() {

        return TAG;
    }

    @Override
    public void finish() {

    }
}