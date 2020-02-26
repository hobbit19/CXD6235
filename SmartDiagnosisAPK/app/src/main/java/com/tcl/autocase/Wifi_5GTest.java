package com.tcl.autocase;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.text.TextUtils;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.tcl.autotest.AllMainActivity;
import com.tcl.autotest.ExecuteTest;
import com.tcl.autotest.autoRunActivity;
import com.tcl.autotest.tool.Tool;
import com.tcl.autotest.utils.FinishThread;
import com.tcl.autotest.utils.Msg;
import com.tcl.autotest.utils.Test;
import com.tcl.autotest.utils.DownTimeCallBack;
import com.tcl.autotest.utils.TestCountDownTimer;

import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import com.tcl.autotest.R;
import static com.tcl.autocase.WifiTest.isAdhoc;

/**
 * Created by user on 18-4-10.
 */

public class Wifi_5GTest extends Test implements DownTimeCallBack {
    private final static String TAG = "Wifi5GTest";
    private TestCountDownTimer testCountDownTimer = null;
    private String text = "";
    private static final String ADHOC_CAPABILITY = "[IBSS]";
    private WifiManager mWifiManager;

    String name = "";

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (action.equals(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)) {
                handleScanResultsAvailable();
            }
        }
    };


    public Wifi_5GTest(ID id, String name) {
        super(id, name);
        this.name = name;
    }

    @Override
    public int getId() {
        return super.getId();
    }


    private void initWifiFunction() {
        IntentFilter mIntentFilter = new IntentFilter();
        mIntentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        getContext().registerReceiver(mReceiver, mIntentFilter);
        mWifiManager = (WifiManager) getContext().getSystemService(
                Context.WIFI_SERVICE);
        if (null == mWifiManager) {
            text_cen_zone.setText("WIFI : Fail");
        } else {
            mWifiManager.setWifiEnabled(true);
            text_cen_zone.setText("Detecting WIFI network...");
            mWifiManager.startScan();
        }
    }

    @Override
    public void setUp() {
        Test.gettag = TAG;
        Test.state = null;
        Tool.toolLog(TAG + "_start_test");

        testCountDownTimer = new TestCountDownTimer(20*SECOND, SECOND, this);
        testCountDownTimer.start();
    }

    @Override
    public void initView() {
        mLayout = (LinearLayout) View.inflate(mContext, R.layout.base_screen,null);
        text_top_zone = (TextView) mLayout.findViewById(R.id.text_top_zone);
        text_cen_zone = (TextView) mLayout.findViewById(R.id.text_cen_zone);

        text_top_zone.setText(mName);
        text_cen_zone.setText(text);

        mContext.setContentView(mLayout);

        initWifiFunction();
    }

    @Override
    public void finish() {

    }

    @Override
    public void destroy() {
        getContext().unregisterReceiver(mReceiver);
        mWifiManager.setWifiEnabled(false);
        if (testCountDownTimer != null) {
            testCountDownTimer.cancel();
            testCountDownTimer = null;
        }
    }

    @Override
    public void timeout() {
        super.timeout();
    }


    public String getmContextTag() {
        // TODO Auto-generated method stub
        return TAG;
    }

    public void setmContextTag() {
        // TODO Auto-generated method stub
    }
    @Override
    public void onTick() {
        // TODO Auto-generated method stub
//		Tool.toolLog(TAG + " onTick ...");
    }

    @Override
    public void onFinish() {
        // TODO Auto-generated method stub
        Tool.toolLog(TAG + " onFinish ...");
        onFail();
    }


    private static boolean isAdhoc(ScanResult scanResult) {
        return scanResult.capabilities.contains(ADHOC_CAPABILITY);
    }

    private static String convertToQuotedString(String string) {
        if (TextUtils.isEmpty(string)) {
            return "";
        }
        final int lastPos = string.length() - 1;
        if (lastPos < 0
                || (string.charAt(0) == '"' && string.charAt(lastPos) == '"')) {
            return string;
        }
        return "\"" + string + "\"";
    }

    private void handleScanResultsAvailable() {
        List<ScanResult> list = mWifiManager.getScanResults();
        if (list == null) {
            return;
        }
        Comparator<ScanResult> comp = new ScanResultcomparator();
        Collections.sort(list, comp);
        String networkList = null;
        for (int i = list.size() - 1; i >= 0; i--) {
            final ScanResult scanResult = list.get(i);
            if (scanResult == null || isAdhoc(scanResult)
                    || TextUtils.isEmpty(scanResult.SSID)) {
                continue;
            }
            final String ssid = convertToQuotedString(scanResult.SSID);
            String rssi = Integer.toString(scanResult.level);
            if (ssid.startsWith("\"NVRAM WARNING: Err")) {
                continue;
            }
            if (name.contains("5G")){
                if (scanResult.frequency > 5000) {//frequency > 5000, is 5G; add by hanlei.yin
				/* add SSID into network list and display on screen */
                    if (null == networkList && scanResult.frequency > 5000) {
                        networkList = ssid + "," + rssi + "\n";
                    } else {
                        if (networkList.indexOf(ssid) < 0) {
                            networkList += ssid + "," + rssi + "\n";
                        }
                    }
                }
            }else {
                if (scanResult.frequency > 2000) {//frequency > 2000, is 2.4G;
				/* add SSID into network list and display on screen */
                    if (null == networkList && scanResult.frequency > 2000) {
                        networkList = ssid + "," + rssi + "\n";
                    } else {
                        if (networkList.indexOf(ssid) < 0) {
                            networkList += ssid + "," + rssi + "\n";
                        }
                    }
                }
            }
        }
        if (null != networkList) {
            onSucess();
            text_cen_zone.setText(networkList);
            text_cen_zone.setTextSize(21);
        } else {
            text_cen_zone.setText("Too low signal to find a Wifi!");
            onFail();
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

class ScanResultcomparator implements Comparator<ScanResult> {
    @Override
    public int compare(ScanResult o1, ScanResult o2) {
        return o1.level - o2.level;

    }
}
