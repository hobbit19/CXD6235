package android.hardware;

import android.annotation.SystemService;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.ServiceManager;
import android.os.RemoteException;

import com.tcl.autotest.tool.Tool;


@SystemService(Context.CONSUMER_IR_SERVICE)
public final class ConsumerIrManager{
    private static final String TAG = "ConsumerIr";
    private final String mPackageName;
    private final IConsumerIrService mService;

    public ConsumerIrManager(Context context) throws ServiceManager.ServiceNotFoundException {
        mPackageName = context.getPackageName();
        mService = IConsumerIrService.Stub.asInterface(ServiceManager.getServiceOrThrow(Context.CONSUMER_IR_SERVICE));
    }
    public boolean hasIrEmitter(){
        if(mService == null){
            Tool.toolLog(TAG + "no consumer ir service.");
            return false;
        }
        try{
            return mService.hasIrEmitter();
        }catch (RemoteException e){
            Tool.toolLog(TAG + " throw e.rethrowFromSystemServer(); " +e.toString());
        }
        return false;
    }
    public void transmit(int carrierFrequency, int[] pattern){
        if(mService == null){
            Tool.toolLog(TAG + "no consumer ir service.");
        }
        try{
            mService.transmit(mPackageName,carrierFrequency,pattern);
        }catch (RemoteException e){
            Tool.toolLog(TAG + " throw e.rethrowFromSystemServer(); " +e.toString());
        }
    }
    public final class CarrierFrequencyRange{
        private final int mMinFrequency;
        private final int mMaxFrequency;
        public CarrierFrequencyRange(int min, int max){
            mMinFrequency = min;
            mMaxFrequency = max;
        }
        public int getMinFrequency(){
            return mMinFrequency;
        }
        public int getMaxFrequency(){
            return mMaxFrequency;
        }
    }
    public CarrierFrequencyRange[] getCarrierFrequencies(){
        if(mService == null){
            Tool.toolLog(TAG + "no consumer ir service.");
            return null;
        }
        try{
            int freqs[] = mService.getCarrierFrequencies();
            CarrierFrequencyRange[] range = new CarrierFrequencyRange[freqs.length / 2];
            for (int i = 0; i < freqs.length; i += 2){
                range[i / 2] = new CarrierFrequencyRange(freqs[i], freqs[i+1]);
            }
            return range;
        }catch (RemoteException e){
            Tool.toolLog(TAG + " throw e.rethrowFromSystemServer(); " +e.toString());
        }
        return null;
    }
}