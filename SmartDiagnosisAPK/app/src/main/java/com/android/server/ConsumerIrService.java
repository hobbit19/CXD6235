package com.android.server;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.IConsumerIrService;
import android.os.RemoteException;
import android.os.PowerManager;

import com.tcl.autotest.tool.Tool;

public class ConsumerIrService extends IConsumerIrService.Stub{

    private final Context mContext;
    private final PowerManager.WakeLock mWakeLock;
    private static final String TAG = "ConsumerIrService";
    private static final int MAX_XMIT_TIME = 2000000; /* in microseconds */
    private final boolean mHasNativeHal;
    private static native boolean halOpen();
    private static native int halTransmit(int carrierFrequency, int[] pattern);
    private static native int[] halGetCarrierFrequencies();
    private final Object mHalLock = new Object();

    ConsumerIrService(Context context){
        mContext = context;
        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,TAG);
        mHasNativeHal = halOpen();
        if(mContext.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CONSUMER_IR)){
            if (!mHasNativeHal){
                Tool.toolLog(TAG + "FEATURE_CONSUMER_IR present, but no IR HAL loaded!");
            }else if(mHasNativeHal){
                Tool.toolLog(TAG + "IR HAL present, but FEATURE_CONSUMER_IR is not set!");
            }
        }
    }
    @Override
    public boolean hasIrEmitter() throws RemoteException {
        return mHasNativeHal;
    }

    @Override
    public void transmit(String packageName, int carrierFrequency, int[] pattern) throws RemoteException {
        if (mContext.checkCallingOrSelfPermission(Manifest.permission.TRANSMIT_IR) != PackageManager.PERMISSION_GRANTED){
            Tool.toolLog(TAG + " Requires TRANSMIT_IR permission ");
        }
        long totalXmitTime = 0;
        for (int slice : pattern){
            if (slice <= 0){
                Tool.toolLog(TAG + " Non-positive IR slice ");
            }
            totalXmitTime += slice;
        }
        if (totalXmitTime > MAX_XMIT_TIME){
            Tool.toolLog(TAG + " IR pattern too long ");
        }
        Tool.toolLog(TAG + "start halTransmit ");
        synchronized (mHalLock){
            int err = halTransmit(carrierFrequency,pattern);
            if (err<0){
                Tool.toolLog(TAG+" Error transmitting: " + err);
            }
        }
        Tool.toolLog(TAG + " end halTransmit ");
    }

    @Override
    public int[] getCarrierFrequencies() throws RemoteException {
        if(mContext.checkCallingOrSelfPermission(Manifest.permission.TRANSMIT_IR) != PackageManager.PERMISSION_GRANTED){
            Tool.toolLog(TAG + " Requires TRANSMIT_IR permission ");
        }
        synchronized (mHalLock){
            return halGetCarrierFrequencies();
        }

    }
}