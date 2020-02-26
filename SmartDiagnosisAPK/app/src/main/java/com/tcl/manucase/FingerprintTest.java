package com.tcl.manucase;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.PackageParser;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Build;
import android.os.CancellationSignal;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.support.annotation.RequiresApi;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.tcl.autotest.AllMainActivity;
import com.tcl.autotest.ExecuteTest;
import com.tcl.autotest.autoRunActivity;
import com.tcl.autotest.tool.Tool;
import com.tcl.autotest.utils.FinishThread;
import com.tcl.autotest.utils.Info;
import com.tcl.autotest.utils.Msg;
import com.tcl.autotest.utils.Test;
import com.tcl.autotest.utils.DownTimeCallBack;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import com.tcl.autotest.R;
import com.tcl.autotest.utils.TestCountDownTimer;

/**
 * creat by nanbing.zou for A5A INFINI at 2017-11-23
 */

public class FingerprintTest extends Test implements DownTimeCallBack {
    private static final String TAG = "FingerprintTest";
    private static final String FP_PATH = "/sys/class/deviceinfo/device_info/fp";
    private static final String FP_PATH2 = "/sys/class/deviceinfo/device_info/FP";
    private static final String GOODIX = "goodix";
    private static final String SYNA = "syna";
    private static final String FOCAL = "FocalTech";
    private static final String FOCAL2 = "focaltech";
    private static final String SUNWAVE = "SW";
    private static final String SUNWAVE2 = "sunwave";


    private IntentFilter filter;
    private FocalFinger mFocalFinger;

    String mDisplay = "";

    private String mstr = "";
    private TestCountDownTimer testCountDownTimer = null;
    public FingerprintTest(ID id, String name) {
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
//        testCountDownTimer = new TestCountDownTimer(10*SECOND, SECOND, this);
//        testCountDownTimer.start();
        ComponentName toActivity = null;
        String fp = readFile(FP_PATH);
        if(fp == null){
            fp = readFile(FP_PATH2);
        }
        if(fp == null){
            fp =  SystemProperties.get("sys.tct.fingerprint","null");
        }
        Tool.toolLog(TAG + "fp:"+fp);
        if(AllMainActivity.deviceName.contains("A3A_PLUS")){
            testFingerBySelf();
        } else {
//            if (fp == null) {
//                failed();
//                return;
//            }
            if (fp.toLowerCase().contains(GOODIX)) {
                toActivity = new ComponentName("com.jrdcom.mmitest",
                        "com.goodix.gf3266.HomeActivity");

                if (AllMainActivity.deviceName.toLowerCase().contains("mickey6")) {
                    toActivity = new ComponentName("com.jrdcom.mmitest",
                            "com.gftest.goodix.mickey6.HomeActivity");
                }
            } else if (fp.contains(SUNWAVE) || fp.toLowerCase().contains(SUNWAVE2)) {
                toActivity = new ComponentName("com.swfp.factory", "com.swfp.activity.DetectActivity");
            } else if (fp.toLowerCase().contains(SYNA)) {
                toActivity = new ComponentName("com.jrdcom.mmitest",
                        "com.jrdcom.mmitest.tests.FingerPrintActivitySyna");
            } else if (fp.contains(FOCAL) || fp.toLowerCase().contains(FOCAL2)) {
                toActivity = new ComponentName("com.jrdcom.mmitest", "com.focal.idol5.FingerprintTestActivityS");
                if (AllMainActivity.deviceName.contains("VFD720")) {
                    toActivity = new ComponentName("com.focaltech.fingerprint.activity", "com.focaltech.fingerprint.activity.FingerprintMainActivity");
                }

            } else {
                if (AllMainActivity.deviceName.equals("Edison_CKT")) {
                    toActivity = new ComponentName("com.focaltech.fingerprintmmitest",
                            "com.focaltech.fingerprintmmitest.FingerprintMMItest");
                }
                Tool.toolLog(TAG + "name:" + AllMainActivity.deviceName);
                if (AllMainActivity.deviceName.contains("Venice")
                        || AllMainActivity.deviceName.contains("Morgan_4G")) {


                    toActivity = new ComponentName("com.focaltech.fingerprint", "com.focaltech.fingerprint.activity.FingerprintMainActivity");
                }
                if (AllMainActivity.deviceName.contains("VFD720")
                        || AllMainActivity.deviceName.equals(Info.CODE_NAME_14)
                        || AllMainActivity.deviceName.equals("A3A")
                        || AllMainActivity.deviceName.equals("U5A_PLUS_4G")
                        ) {
                    toActivity = new ComponentName("com.focaltech.fingerprint.activity", "com.focaltech.fingerprint.activity.FingerprintMainActivity");
                }

                if (AllMainActivity.deviceName.equals("A3A_XL_3G")) {
                    toActivity = new ComponentName("com.focal.fingerprint", "com.focal.fingerprint.FingerprintTestActivityS");
                }
                if (AllMainActivity.deviceName.contains("A5X")
                        || AllMainActivity.deviceName.equals("Tokyo")
                        || AllMainActivity.deviceName.equals("5028A")
                        || AllMainActivity.deviceName.equals("5028D")) {
                    toActivity = new ComponentName("com.swfp.factory", "com.swfp.activity.DetectActivity");
                }

                if (AllMainActivity.deviceName.equals(Info.CODE_NAME_19)) {
                    toActivity = new ComponentName("com.fpsensor_sample.fpSensorExtensionSvc2", "com.fpsensor.sensortesttool.sensorTestActivity");
                }
            }

            if (toActivity != null) {
                mIntent.setComponent(toActivity);
                if (AllMainActivity.deviceName.equals("A3A_XL_3G")) {
                    getContext().startActivity(mIntent);
                }else if (AllMainActivity.deviceName.equals(Info.CODE_NAME_19)){
                    startWINTECH = true;
                    mIntent.putExtra("config_autoexit", true);
                    mIntent.putExtra("config_autotest", true);
                    mIntent.putExtra("config_autoexit_delay_time", 500);
                    mIntent.putExtra("config_supportTouchTest", true);
                    mIntent.putExtra("config_showcapturedImg", true);
                    mIntent.putExtra("config_savecapturedImg", false);
                    getContext().startActivityForResult(mIntent,0);
                }

                else {
                    getContext().startActivityForResult(mIntent, 0);
                }

            } else {
//            text_cen_zone.setText("the wrong fingerprint ic information");
                failed();
            }
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
        if(AllMainActivity.deviceName.equals("A3A_XL_3G")){
            mFocalFinger = new FocalFinger();
            filter = new IntentFilter("com.focal.pfingerprinttest");
            filter.addAction("com.swfp.factory.action.test_result");
            mContext.registerReceiver(mFocalFinger,filter);
        }
        if (AllMainActivity.deviceName.equals("A3A_XL_4G")){
            mFocalFinger = new FocalFinger();
            filter = new IntentFilter("com.swfp.factory");
            filter.addAction("com.swfp.factory.action.test_result");
            mContext.registerReceiver(mFocalFinger,filter);
        }


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
        if(mFocalFinger != null) {
            mContext.unregisterReceiver(mFocalFinger);
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
        Tool.toolLog(TAG+"requestCode:"+requestCode+"resultCode:"+resultCode);
        Tool.toolLog(TAG+"data:"+data);
        if(AllMainActivity.deviceName.contains("VFD720")
                || AllMainActivity.deviceName.equals(Info.CODE_NAME_14)
                || AllMainActivity.deviceName.equals("A3A")
                || AllMainActivity.deviceName.equals("Venice")
                || AllMainActivity.deviceName.equals("Morgan_4G")
                || AllMainActivity.deviceName.equals("A3A_XL_4G")){
            Tool.toolLog(TAG+"data:"+data);
            if (data != null) {
                int result = data.getIntExtra("value", 1);
                Tool.toolLog(TAG+"result:"+result);
                if (result == 0) {//pass
                    pass();
                } else if (result == 1) {//fail
                    failed();
                }
            }
            else{
                Tool.toolLog(TAG + "data is null");
                //pass();
            }
        } else if (AllMainActivity.deviceName.equals(Info.CODE_NAME_19) && startWINTECH) {
            startWINTECH = false;
            int temp = isTestOK();
            if (temp == 1) {
                pass();
            } else if (temp == 2) {
                failed();
            }
        }
        else {
            if (resultCode == 1) { // pass
                pass();
            } else if (resultCode == 0) { // fail
                failed();
            }else {
                if (data != null) {
                    int result = data.getIntExtra("value", 1);
                    if (result == 0) {//pass
                        pass();
                    } else if (result == 1) {//fail
                        failed();
                    }
                }
            }
        }


    }
    public class FocalFinger extends BroadcastReceiver {
        public void onReceive(Context context, Intent intent) {
            String result = "No result!";
            result = intent.getStringExtra("fpresult");
            if(result == null){
                result = intent.getStringExtra("test_result");
            }
            Tool.toolLog(TAG+"result = " + result);
            result = result.toLowerCase();
            if ("pass".equals(result)) {
               pass();
            } else {
                if(AllMainActivity.deviceName.equals("A3A_XL_4G")){
                    if("false".equals(result)){
                        pass();
                    }else {
                        failed();
                    }
                }else {
                    failed();
                }
            }
        }
    }

    public byte[] getBytes(long auth) {
        byte[] arrayOfByte = new byte[28];
        arrayOfByte[0] = (byte)(byte)(int)(auth & 0xFF); arrayOfByte[1] = (byte)(byte)(int)(auth >> 8 & 0xFF);
        arrayOfByte[2] = (byte)(byte)(int)(auth >> 16 & 0xFF);
        arrayOfByte[3] = (byte)(byte)(int)(auth >> 24 & 0xFF);
        arrayOfByte[4] = (byte)(byte)(int)(auth >> 32 & 0xFF);
        arrayOfByte[5] = (byte)(byte)(int)(auth >> 40 & 0xFF);
        arrayOfByte[6] = (byte)(byte)(int)(auth >> 48 & 0xFF);
        arrayOfByte[7] = (byte)(byte)(int)(auth >> 56 & 0xFF);
        arrayOfByte[24] = (byte)0; arrayOfByte[25] = (byte)0;
        arrayOfByte[26] = (byte)0; arrayOfByte[27] = (byte)2;
        return arrayOfByte;
    }

@RequiresApi(api = Build.VERSION_CODES.M)
private void testFingerBySelf() {
    int permissionCheck = mContext.checkSelfPermission(Manifest.permission.MANAGE_FINGERPRINT);
    if(permissionCheck != PackageManager.PERMISSION_GRANTED){
        Tool.toolLog(TAG+"no permission" );
        failed();
        return;
    }

    FingerprintManager mFingerprintManager =
            (FingerprintManager) mContext.getSystemService(Context.FINGERPRINT_SERVICE);

//    long auth = mFingerprintManager.preEnroll();

//    byte[] arrayOfByte = getBytes(auth);
//    byte[] mToken = new  byte[arrayOfByte.length];
//    for (int i = 0; i < arrayOfByte.length; i++) {
//        mToken[i] = (byte) arrayOfByte[i];
//    }

//    mFingerprintManager.enroll(mToken, null, 0, UserHandle.myUserId(),new FingerprintManager.EnrollmentCallback() {
//        @Override
//        public void onEnrollmentError(int errMsgId, CharSequence errString) {
//            super.onEnrollmentError(errMsgId, errString);
//            Tool.toolLog(TAG+"onEnrollmentError" );
//        }
//
//        @Override
//        public void onEnrollmentHelp(int helpMsgId, CharSequence helpString) {
//            super.onEnrollmentHelp(helpMsgId, helpString);
//            Tool.toolLog(TAG+"onEnrollmentHelp" );
//        }
//
//        @Override
//        public void onEnrollmentProgress(int remaining) {
//            super.onEnrollmentProgress(remaining);
//            Tool.toolLog(TAG+"onEnrollmentProgress" );
//            pass();
//        }
//    });
    CancellationSignal mCancellationSignal = new CancellationSignal();
    mFingerprintManager.authenticate(null, mCancellationSignal, 0, new FingerprintManager.AuthenticationCallback() {
        @Override
        public void onAuthenticationError(int errorCode, CharSequence errString) {
            super.onAuthenticationError(errorCode, errString);
            Tool.toolLog(TAG+"onAuthenticationError" );
            mCancellationSignal.cancel();
            failed();
        }

        @Override
        public void onAuthenticationHelp(int helpCode, CharSequence helpString) {
            super.onAuthenticationHelp(helpCode, helpString);
            Tool.toolLog(TAG+"onAuthenticationHelp" );
            mCancellationSignal.cancel();
            pass();
        }

        @Override
        public void onAuthenticationSucceeded(FingerprintManager.AuthenticationResult result) {
            super.onAuthenticationSucceeded(result);
            Tool.toolLog(TAG+"onAuthenticationSucceeded" );
            mCancellationSignal.cancel();
            pass();
        }

        @Override
        public void onAuthenticationFailed() {
            super.onAuthenticationFailed();
            Tool.toolLog(TAG+"onAuthenticationFailed,get finger info, pass" );
            mCancellationSignal.cancel();
            pass();
        }
    },null);
}
    private static final String FINGER_WINGTECH = "persist.sys.finger.ispass";
    private boolean startWINTECH = false;
    private int isTestOK() {

        String wtBase = SystemProperties.get(FINGER_WINGTECH, null);
        Log.d("weili", "wtBase is: " + wtBase);
        if (TextUtils.isEmpty(wtBase)) {
            return 0;
        }
        if(wtBase.equals("1")){
            return 1;
        }else if(wtBase.equals("2")){
            return 2;
        }else{
            return 0;
        }
    }
}
