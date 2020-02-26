package com.tcl.manucase;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.tcl.autotest.R;
import com.tcl.autotest.tool.Tool;
import com.tcl.autotest.utils.Test;
import com.tcl.autotest.utils.TestCountDownTimer;

/**
 * Created by user on 18-12-19.
 */

public class HeadSetTest extends Test {
    private final static String TAG = "HeadSetTest";
    String text = "";
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if(intent.hasExtra("state")){
	              /*  if(intent.getIntExtra("state", 0)==0){
	                    //Toast.makeText(context, "headset not connected", Toast.LENGTH_LONG).show();
	                	text = "headset not connected\n";
						text_cen_zone.setText(text);
						finish();
						onFail();

	                } */
                if(intent.getIntExtra("state", 0)==1){
                    //Toast.makeText(context, "headset  connected", Toast.LENGTH_LONG).show();
                    text = "headset  connected\n";
                    text_cen_zone.setText(text);
                    bt_left.setEnabled(true);


                }
            }
        }
    };
    public HeadSetTest(ID id, String name) {
        super(id, name);
    }


    @Override
    public void setUp() {
        // TODO Auto-generated method stub
        Tool.toolLog(TAG + "_start_test");
        Log.d(TAG, " 33333 3_start_test");
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.intent.action.HEADSET_PLUG");
        mContext.registerReceiver(mReceiver, filter);
        text = "begin searching...\n";
        text = "Waiting ...";
    }

    @Override
    public void initView() {
        mLayout = (LinearLayout) View.inflate(mContext, R.layout.base_screen_view,null);

        text_top_zone = (TextView) mLayout.findViewById(R.id.text_top_zone);
        text_cen_zone = new TextView(mContext);
        text_cen_zone.setGravity(Gravity.CENTER);
        text_top_zone.setText(mName);
        text_cen_zone.setText(text);
        bt_left = (Button) mLayout.findViewById(R.id.bt_left);
        bt_right = (Button) mLayout.findViewById(R.id.bt_right);
        bt_left.setText(R.string.pass);
        bt_right.setText(R.string.fail);
        bt_left.setVisibility(View.VISIBLE);
        bt_right.setVisibility(View.VISIBLE);
        bt_left.setEnabled(false);
        bt_left.setOnClickListener(pass_listener);
        bt_right.setOnClickListener(failed_listener);
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
        if(mReceiver != null){
            mContext.unregisterReceiver(mReceiver);
        }

        finish();
    }
}
