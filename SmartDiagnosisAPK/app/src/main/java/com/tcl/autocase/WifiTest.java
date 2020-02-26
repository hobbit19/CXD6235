package com.tcl.autocase;

import java.util.List;

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
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
/**
 * ��WifiTest ���a�����  onstop ��onResume����
 * @author bin.xi �Ķ��Ƚϴ�
 *
 */
public class WifiTest extends Test implements DownTimeCallBack {

    private WifiManager mWifiManager;
    private IntentFilter mIntentFilter;
    private String networkList;
    private Boolean initWifiStatus = false;
    private TestCountDownTimer testCountDownTimer = null;
    private TestCountDownTimer     mTestTimeOutCountDownTimer=null;
    private Boolean registerStatus = false;
    private FinishThread tFinishThread =null;
    /** String present in capabilities if the scan result is ad-hoc */
    private static final String ADHOC_CAPABILITY = "[IBSS]";
    private static final String TAG = "WifiTest";
   private boolean mInitWifiEnabled=false;
    public WifiTest(ID id, String name) {
        super(id, name);
        // TODO Auto-generated constructor stub
    }

    public WifiTest(ID id, String name, Boolean updateFlag) {
        super(id, name, updateFlag);
        // TODO Auto-generated constructor stub
    }

    public WifiTest(ID id, String name, Boolean updateFlag, long timeout) {
        super(id, name, updateFlag, timeout);
        // TODO Auto-generated constructor stub
    }

    private boolean wifiInit(){
        mWifiManager = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
        if(null == mWifiManager){
            return false;
        }
        if(mWifiManager.isWifiEnabled() == false){
            mWifiManager.setWifiEnabled(true);
        }
        return true;
    }

    @Override
    public void setUp() {
        // TODO Auto-generated method stub
        Tool.toolLog(TAG + "_start_test");
        Log.d(TAG, " 33333 _start_test");
        mTestTimeOutCountDownTimer =new TestCountDownTimer(60*SECOND, SECOND, new DownTimeCallBack() {
            @Override
            public void onTick() {}
            @Override
            public void onFinish() {
            	Log.d(TAG, " 33333 onFinish() timeout()");
                timeout();
                //mTestTimeOutCountDownTimer.cancel();
            }
        });
        mTestTimeOutCountDownTimer.start();
    }

    @Override
    public void initView() {
        mLayout = (LinearLayout) View.inflate(mContext, R.layout.base_screen,
                null);

        text_top_zone = (TextView) mLayout.findViewById(R.id.text_top_zone);
        text_cen_zone = (TextView) mLayout.findViewById(R.id.text_cen_zone);

        text_top_zone.setText(mName);
        text_cen_zone.setText("WIFI initializing.....");
        mContext.setContentView(mLayout);
    }

    @Override
    public void updateView() {
        // TODO Auto-generated method stub
        testCountDownTimer = new TestCountDownTimer(5 * SECOND, SECOND, this);
        testCountDownTimer.start();
    }

    public void scan() {
        if (initWifiStatus) {
            // Tool.toolLog(TAG + " scan ... ");
            text_cen_zone.setText("Detecting WIFI network...");
            mIntentFilter = new IntentFilter();
            mIntentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
            mContext.registerReceiver(mReceiver, mIntentFilter);
            registerStatus = true;
            // mWifiManager.startScanActive();
            boolean scan_bool = mWifiManager.startScan();
            // Tool.toolLog(TAG + " scan_bool = " + scan_bool);
        }
        mContext.setContentView(mLayout);
    }

    @Override
    public void finish() {
        // TODO Auto-generated method stub
        Tool.toolLog(TAG + " finish");
        Log.d(TAG, " 33333 finish");

        // if (initWifiStatus && registerStatus) {
        // initWifiStatus = false;
        // registerStatus = false;
        // mContext.unregisterReceiver(mReceiver);
        // }
        // mWifiManager.setWifiEnabled(false);
    }

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            Tool.toolLog(TAG + " action " + action);
            Log.d(TAG, " 33333 scan action: " + action);
            if (action.equals(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)) {
                // Tool.toolLog(TAG + " 111111111 ");
                handleScanResultsAvailable();
            }
        }
    };

    private void handleScanResultsAvailable() {
        List<ScanResult> list = mWifiManager.getScanResults();
        Log.d(TAG, "33333 handleScanResultsAvailable() list.size(): " + list.size());
        if (list != null) {
            for (int i = list.size() - 1; i >= 0; i--) {
                final ScanResult scanResult = list.get(i);

                if (scanResult == null) {
                    continue;
                }
                /*
                 * Ignore adhoc, enterprise-secured, or hidden networks. Hidden
                 * networks show up with empty SSID.
                 */
                if (WifiTest.isAdhoc(scanResult)
                        || TextUtils.isEmpty(scanResult.SSID)) {
                    continue;
                }

                final String ssid = WifiTest
                        .convertToQuotedString(scanResult.SSID);
                // Tool.toolLog(TAG + " ssid " + ssid);

                String rssi = Integer.toString(scanResult.level);

                if (ssid.startsWith("\"NVRAM WARNING: Err")) {
                    continue;
                }

                /* add SSID into network list and display on screen */
                if (null == networkList) {
                    networkList = ssid + "," + rssi;
                    networkList += "\n";
                } else {
                    if (networkList.indexOf(ssid) < 0) {
                        networkList += ssid + "," + rssi;
                        networkList += "\n";
                    }
                }
            }
            if (null != networkList) {
                cancelCountDownTimer();// ADD XIBIN
                text_cen_zone.setText(networkList);
                // bt_left.setEnabled(true);
                // bt_right.setEnabled(true);
                mContext.setResult(Test.RESULT.PASS.ordinal());
                // Add by Jianke.Zhang 02/02
                tFinishThread = new FinishThread(0x01,
                        ExecuteTest.temppositon);
                tFinishThread.start();
                try {
                    tFinishThread.join();
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

                // Write data into file
                if (AllMainActivity.mainAllTest
                        && !AllMainActivity.autofileFlag) {
                    Msg.WriteModelResult(mContext,
                            AllMainActivity.all_items_file_text, "PASS");
                } else {
                    Msg.WriteModelResult(mContext,
                            autoRunActivity.auto_file_text, "PASS");
                }
                // End
                if (initWifiStatus && registerStatus) {
                    initWifiStatus = false;
                    registerStatus = false;
                    mContext.unregisterReceiver(mReceiver);
                }
                mWifiManager.setWifiEnabled(mInitWifiEnabled);
                
                Msg.exitWithSuccessTest(mContext, TAG, 20, true, "Pass");
            }
        }
    }

    public static String convertToQuotedString(String string) {
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

    public static boolean isAdhoc(ScanResult scanResult) {
        return scanResult.capabilities.contains(ADHOC_CAPABILITY);
    }

    @Override
    public void onFinish() {
        // TODO Auto-generated method stub
        Tool.toolLog(TAG + " onFinish ... ");
        Log.d(TAG, " 33333 onFinish ... ");
        
        if(initWifiStatus){
            text_cen_zone.setText("Detecting WIFI network...");
            mIntentFilter = new IntentFilter();
            mIntentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
            mContext.registerReceiver(mReceiver, mIntentFilter);
            registerStatus = true;
            mWifiManager.startScan();
        }else{
            text_cen_zone.setText("WIFI initialize fail.");
            if (null != bt_left)
            	bt_left.setEnabled(true);
            if (null != bt_right)
            	bt_right.setEnabled(true);
        }
        
        mContext.setContentView(mLayout);

    }

    @Override
    public void onTick() {
        // TODO Auto-generated method stub
        // Tool.toolLog(TAG + " onTick ... ");
         initWifiStatus = wifiInit();
    }

    public void timeout() {
        // TODO Auto-generated method stub
        Tool.toolLog("timeout ...");
        
        text_cen_zone.setText("WIFI Time out.");
        // bt_left.setEnabled(true);
        // bt_right.setEnabled(false);
        // Add by Jianke.Zhang 02/02
        mContext.setResult(Test.RESULT.FAILED.ordinal());
        Tool.toolLog(TAG + " index 8887 -> " + ExecuteTest.temppositon);
        int double_test;
        if (AllMainActivity.mainAllTest) {
            double_test = AllMainActivity.double_check_for_auto[ExecuteTest.temppositon]++;
            // AllMainActivity.mainAllTest = false;
        } else {
            double_test = autoRunActivity.double_check[ExecuteTest.temppositon]++;
        }
        Tool.toolLog(TAG + " double_test 9997 -> " + double_test);
        FinishThread tFinishThread = new FinishThread(0x02,
                ExecuteTest.temppositon);
        tFinishThread.start();
        try {
            tFinishThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        tFinishThread=null;
        if (double_test == 1) {
            //Msg.exitWithException(mContext, TAG, 20, true, "Pass");
        	Msg.exitWithException(mContext, TAG, 20, true, "Pass");
        	Log.d(TAG, " 33333 double_test 9997 -> " + double_test);
            // Write data into file
            if (AllMainActivity.mainAllTest && !AllMainActivity.autofileFlag) {
                Msg.WriteModelResult(mContext,
                        AllMainActivity.all_items_file_text, "FAIL");
            } else {
                Msg.WriteModelResult(mContext, autoRunActivity.auto_file_text,
                        "FAIL");
            }
            // End
        } else {
            Msg.exitWithException(mContext, TAG, 20, true, "Fail");
        }

        if (initWifiStatus && registerStatus) {
            initWifiStatus = false;
            registerStatus = false;
            mContext.unregisterReceiver(mReceiver);
        }
        mWifiManager.setWifiEnabled(false);
        cancelCountDownTimer();
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
        cancelCountDownTimer();
       
    }

    @Override
    public void onStop() {
        cancelCountDownTimer();
    }
    @Override
    public void onResume() {
        testCountDownTimer = new TestCountDownTimer(60 * SECOND, SECOND, this);
        testCountDownTimer.start();
    }
    private void  cancelCountDownTimer(){
        if (testCountDownTimer != null) {
        	Log.d(TAG, " 33333  cancelCountDownTimer 111");
            testCountDownTimer.cancel();
            testCountDownTimer = null;
        }
        if (mTestTimeOutCountDownTimer != null) {
        	Log.d(TAG, " 33333  cancelCountDownTimer 222");
            mTestTimeOutCountDownTimer.cancel();
            mTestTimeOutCountDownTimer = null;
        }
    }
}
