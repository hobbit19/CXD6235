package com.mediatek.fmradio;

import java.util.ArrayList;
import java.lang.reflect.Field;

import android.media.AudioManager;

import android.content.Context;
import android.provider.Settings;
import com.android.fmradio.FmNative;
import com.mediatek.fmradio.FmRadioNative;
import android.media.AudioPatch;
import android.media.AudioTrack;
import android.media.AudioPortConfig;
import android.media.AudioPort;
import android.media.AudioRecord;
import android.media.AudioSystem;
import android.media.AudioDevicePortConfig;
import android.media.AudioDevicePort;
import android.media.AudioMixPort;
import android.media.AudioFormat;
import android.media.MediaPlayer;

import com.tcl.autotest.tool.Tool;



public class MTKFMReceiver {

    // context
    private Context mContext = null;
    private final static String TAG = "MTKFMReceiver";
    private boolean mIsMTKEnhancePatchOn = false;
    private boolean mIsDeviceOpen = false;
    private boolean mIsPowerUp = false;
    private AudioPatch mAudioPatch = null;
    private AudioManager mAudioManager;
    private AudioTrack mAudioTrack = null;
    private boolean mIsRender = false;
    // default station frequency
    public static final int DEFAULT_STATION = 87500;
    public static int mCurrentStation = DEFAULT_STATION;
    // convert rate
    public static final int CONVERT_RATE = 1000;
    AudioDevicePort mAudioSource = null;
    AudioDevicePort mAudioSink = null;
    private AudioRecord mAudioRecord = null;
    // must check AudioSystem.usage when google upgrade
    private static final int FOR_PROPRIETARY = 1;
    public static final int DEFAULT_FM_TURNER = 1998;
    private static final int SAMPLE_RATE = 44100;
    private static final int CHANNEL_CONFIG = AudioFormat.CHANNEL_CONFIGURATION_STEREO;
    private static final int AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT;
    private static final int RECORD_BUF_SIZE = AudioRecord.getMinBufferSize(SAMPLE_RATE,
            CHANNEL_CONFIG, AUDIO_FORMAT);
    private Object mRenderLock = new Object();
    private Thread mRenderThread = null;
    private MediaPlayer mFMPlayer = null;

    public MTKFMReceiver(Context context) {
        mAudioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
    }

    public boolean powerUpFM(float frequency) {
        //Defect293180 by bing.wang.hz 2015.06.02 begin
        if (mIsMTKEnhancePatchOn) {
            //mForcedUseForMedia = isSpeaker ? AudioSystem.FORCE_SPEAKER : AudioSystem.FORCE_NONE;
            createAudioPatch();//add by bing.wang.hz for FMRadio MTK L1 adapter
        }
        //Defect293180 by bing.wang.hz 2015.06.02 end
        AudioManager am = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
        boolean isHeadsetOn = am.isWiredHeadsetOn();

        if (!isHeadsetOn) {

            return false;
        }

        if (!isDeviceOpen()) {
            openDevice();
        }
        if (mIsPowerUp) {
            Tool.toolLog(TAG + "<<< MTKReceiver.powerUp: already power up" + mIsPowerUp);
            return true;
        }
        long time = System.currentTimeMillis();
        Tool.toolLog(TAG + "performance test. service native power up start:" + time);
        if (mIsMTKEnhancePatchOn) {
            if (!FmNative.powerUp(frequency)) {
                Tool.toolLog(TAG + "Error: powerup failed.");
                return false;
            }
        }
        else {
            if (!FmRadioNative.powerUp(frequency)) {
                Tool.toolLog(TAG + "Error: powerup failed.");
                return false;
            }
        }

        time = System.currentTimeMillis();
        Tool.toolLog(TAG + "performance test. service native power up end:" + time);
        mIsPowerUp = true;
        initDevice(frequency);

        return mIsPowerUp;
    }
    private synchronized void createAudioPatch() {
        if (mAudioPatch == null) {
            ArrayList<AudioPatch> patches = new ArrayList<AudioPatch>();
            mAudioManager.listAudioPatches(patches);
            if (isPatchMixerToEarphone(patches)) {
                stopAudioTrack();
                stopRender();
                createAudioPatchByEarphone();
            } else if (isPatchMixerToSpeaker(patches)) {
                stopAudioTrack();
                stopRender();
                createAudioPatchBySpeaker();
            } else {
                startRender();
            }
        }
    }

    public boolean isDeviceOpen() {
        Tool.toolLog(TAG +"isDeviceOpen: " + mIsDeviceOpen);
        return mIsDeviceOpen;
    }

    /**
     * open FM device, should be call before power up
     *
     * @return true if FM device open, false FM device not open
     */
    public boolean openDevice() {
        Tool.toolLog(TAG + ">>> MTKReceiver.openDevice");

        if (!mIsDeviceOpen) {
            if (mIsMTKEnhancePatchOn) {
                mIsDeviceOpen = FmNative.openDev();
            }
            else {
                mIsDeviceOpen = FmRadioNative.openDev();
            }
        }
        Tool.toolLog(TAG +"<<< MTKReceiver.openDevice: " + mIsDeviceOpen);
        return mIsDeviceOpen;
    }

    public boolean initDevice(float frequency) {
        if (!isDeviceOpen()) {
            openDevice();
        }
        Tool.toolLog(TAG + ">>> MTKReceiver.initDevice: " + frequency);

        mCurrentStation = computeStation(frequency);
//        setSpeakerPhoneOn(mIsSpeakerUsed);

        enableFmAudio(true);


        // Start the RDS thread if RDS is supported.
        // if (isRDSSupported()) {
        // Log.d(TAG, "RDS is supported. Start the RDS thread.");
        // startRDSThread();
        // }

        if (!isAntennaAvailable()) {
            // Antenna not ready, try short antenna
            if (switchAntenna(1) != 0) {
                Tool.toolLog(TAG + "Error while trying to switch to short antenna: ");
            }
            // add tune because FMTx has power up, antenna has switch to FMTx,
            // call tune to make switch antenna effective
            if (mIsMTKEnhancePatchOn) {
                FmNative.tune(computeFrequency(mCurrentStation));
            }
            else {
                FmRadioNative.tune(computeFrequency(mCurrentStation));
            }

        }
        // setRDS(true);
//        setMute(false);

        Tool.toolLog(TAG + "<<< MTKReceiver.initDevice: " + mIsPowerUp);
        return mIsPowerUp;
    }

    // Make sure patches count will not be 0
    private boolean isPatchMixerToEarphone(ArrayList<AudioPatch> patches) {
        int deviceCount = 0;
        int deviceEarphoneCount = 0;
        for (AudioPatch patch : patches) {
            AudioPortConfig[] sources = patch.sources();
            AudioPortConfig[] sinks = patch.sinks();
            Tool.toolLog(TAG + "isPatchMixerToEarphone, sinks num: " + sinks.length);
            if (sinks.length > 1) {
                continue;
            }
            AudioPortConfig sourceConfig = sources[0];
            AudioPortConfig sinkConfig = sinks[0];
            AudioPort sourcePort = sourceConfig.port();
            AudioPort sinkPort = sinkConfig.port();
            Tool.toolLog(TAG + "isPatchMixerToEarphone " + sourcePort + " ====> " + sinkPort);
            if (sourcePort instanceof AudioMixPort && sinkPort instanceof AudioDevicePort) {
                deviceCount++;
                int type = ((AudioDevicePort) sinkPort).type();
                if (type == AudioSystem.DEVICE_OUT_WIRED_HEADSET ||
                        type == AudioSystem.DEVICE_OUT_WIRED_HEADPHONE) {
                    deviceEarphoneCount++;
                }
            }
        }
        if (deviceEarphoneCount == 1 && deviceCount == deviceEarphoneCount) {
            return true;
        }
        return false;
    }
    private void stopAudioTrack() {
        if (mAudioTrack.getPlayState() == AudioTrack.PLAYSTATE_PLAYING) {
            mAudioTrack.stop();
        }
    }
    private synchronized void stopRender() {
        Tool.toolLog(TAG + "stopRender");
        mIsRender = false;
    }

    private synchronized void createAudioPatchByEarphone() {
        Tool.toolLog(TAG + "createAudioPatchByEarphone");
        if (mAudioPatch != null) {
            Tool.toolLog(TAG + "createAudioPatch, mAudioPatch is not null, return");
            return;
        }

//        if (mIsSpeakerUsed) {
//            // audio system config has been modified by others,
//            // so need update this state.
//            mIsSpeakerUsed = false;
//            //notifySpeakerModeChange();
//        }
        mAudioSource = null;
        mAudioSink = null;
        ArrayList<AudioPort> ports = new ArrayList<AudioPort>();
        mAudioManager.listAudioPorts(ports);
        for (AudioPort port : ports) {
            if (port instanceof AudioDevicePort) {
                int type = ((AudioDevicePort) port).type();
                String name = AudioSystem.getOutputDeviceName(type);
                if (type == AudioSystem.DEVICE_IN_FM_TUNER) {
                    mAudioSource = (AudioDevicePort) port;
                } else if (type == AudioSystem.DEVICE_OUT_WIRED_HEADSET ||
                        type == AudioSystem.DEVICE_OUT_WIRED_HEADPHONE) {
                    mAudioSink = (AudioDevicePort) port;
                }
            }
        }
        if (mAudioSource != null && mAudioSink != null) {
            AudioDevicePortConfig sourceConfig = (AudioDevicePortConfig) mAudioSource
                    .activeConfig();
            AudioDevicePortConfig sinkConfig = (AudioDevicePortConfig) mAudioSink.activeConfig();
            AudioPatch[] audioPatchArray = new AudioPatch[] {null};
            mAudioManager.createAudioPatch(audioPatchArray,
                    new AudioPortConfig[] {sourceConfig},
                    new AudioPortConfig[] {sinkConfig});
            mAudioPatch = audioPatchArray[0];
        }
    }
    private boolean isPatchMixerToSpeaker(ArrayList<AudioPatch> patches) {
        int deviceCount = 0;
        int deviceEarphoneCount = 0;
        for (AudioPatch patch : patches) {
            AudioPortConfig[] sources = patch.sources();
            AudioPortConfig[] sinks = patch.sinks();
            Tool.toolLog(TAG + "isPatchMixerToSpeaker, sinks num: " + sinks.length);
            if (sinks.length > 1) {
                continue;
            }
            AudioPortConfig sourceConfig = sources[0];
            AudioPortConfig sinkConfig = sinks[0];
            AudioPort sourcePort = sourceConfig.port();
            AudioPort sinkPort = sinkConfig.port();
            Tool.toolLog(TAG + "isPatchMixerToSpeaker " + sourcePort + " ====> " + sinkPort);
            if (sourcePort instanceof AudioMixPort && sinkPort instanceof AudioDevicePort) {
                deviceCount++;
                int type = ((AudioDevicePort) sinkPort).type();
                if (type == AudioSystem.DEVICE_OUT_SPEAKER) {
                    deviceEarphoneCount++;
                }
            }
        }
        if (deviceEarphoneCount == 1 && deviceCount == deviceEarphoneCount) {
            return true;
        }
        return false;
    }
    private synchronized void createAudioPatchBySpeaker() {
        Tool.toolLog(TAG + "createAudioPatchBySpeaker");
        if (mAudioPatch != null) {
            Tool.toolLog(TAG + "createAudioPatch, mAudioPatch is not null, return");
            return;
        }

//        if (!mIsSpeakerUsed) {
//            // audio system config has been modified by others,
//            // so need update this state.
//            mIsSpeakerUsed = true;
//            //notifySpeakerModeChange();
//        }

        mAudioSource = null;
        mAudioSink = null;
        ArrayList<AudioPort> ports = new ArrayList<AudioPort>();
        mAudioManager.listAudioPorts(ports);
        for (AudioPort port : ports) {
            if (port instanceof AudioDevicePort) {
                int type = ((AudioDevicePort) port).type();
                String name = AudioSystem.getOutputDeviceName(type);
                if (type == AudioSystem.DEVICE_IN_FM_TUNER) {
                    mAudioSource = (AudioDevicePort) port;
                } else if (type == AudioSystem.DEVICE_OUT_SPEAKER) {
                    mAudioSink = (AudioDevicePort) port;
                }
            }
        }
        if (mAudioSource != null && mAudioSink != null) {
            AudioDevicePortConfig sourceConfig = (AudioDevicePortConfig) mAudioSource
                    .activeConfig();
            AudioDevicePortConfig sinkConfig = (AudioDevicePortConfig) mAudioSink.activeConfig();
            AudioPatch[] audioPatchArray = new AudioPatch[] {null};
            mAudioManager.createAudioPatch(audioPatchArray,
                    new AudioPortConfig[] {sourceConfig},
                    new AudioPortConfig[] {sinkConfig});
            mAudioPatch = audioPatchArray[0];
        }
    }

    private synchronized void startRender() {
        Tool.toolLog(TAG + "startRender " + AudioSystem.getForceUse(FOR_PROPRIETARY));

        // need to create new audio record and audio play back track,
        // because input/output device may be changed.
        if (mAudioRecord != null && mAudioRecord.getRecordingState()
                == AudioRecord.RECORDSTATE_RECORDING) {
            mAudioRecord.stop();
        }
        if (mAudioTrack != null) {
            stopAudioTrack();
        }
        initAudioRecordSink();

        mIsRender = true;
        synchronized (mRenderLock) {
            mRenderLock.notify();
        }
    }
    /**
     * Open or close FM Radio audio
     *
     * @param enable true, open FM audio; false, close FM audio;
     */
    private void enableFmAudio(boolean enable) {
        if (enable) {
            if (/*(mPowerStatus != POWER_UP) || !mIsAudioFocusHeld*/!isPowerUp()) {
                Tool.toolLog(TAG + "enableFmAudio, current not available return.mIsAudioFocusHeld:"
                        + isPowerUp());
                return;
            }

            startAudioTrack();
            createAudioPatch();
        } else {
            releaseAudioPatch();
            stopRender();
        }
    }
    /**
     * whether antenna available
     *
     * @return true, antenna available; false, antenna not available
     */
    private boolean isAntennaAvailable() {
//        return FeatureOption.MTK_MT519X_FM_SUPPORT ? true
//                : ((AudioManager) mContext
//                .getSystemService(Context.AUDIO_SERVICE))
//                .isWiredHeadsetOn();
        return true;
    }
    public static float computeFrequency(int station) {
        return (float) station / CONVERT_RATE;
    }

    /**
     * compute station value with given frequency
     *
     * @param frequency
     *            station frequency
     * @return station value
     */
    public static int computeStation(float frequency) {
        return (int) (frequency * CONVERT_RATE);
    }

    /*
     * need native support whether antenna support interface.
     */
    // return (0, success; 1 failed; 2 not support)
    public int switchAntenna(int antenna) {
        Tool.toolLog(TAG + ">>> MTKReceiver.switchAntenna:" + antenna);
        /*
         * if (!mIsPowerUp) { // If not even powered up, just return and do
         * nothing Log.w(TAG, "ACTION_HEADSET_PLUG: FM is not powerup!!");
         * return 0; }
         */

        // if fm not powerup, switchAntenna will flag whether has earphone
        int ret;
        if (mIsMTKEnhancePatchOn) {
            ret = FmNative.switchAntenna(antenna);
        }
        else {
            ret = FmRadioNative.switchAntenna(antenna);
        }
        Tool.toolLog(TAG + "<<< MTKReceiver.switchAntenna: " + ret);
        return ret;
    }
    // This function may be called in different threads.
    // Need to add "synchronized" to make sure mAudioRecord and mAudioTrack are the newest.
    // Thread 1: onCreate() or startRender()
    // Thread 2: onAudioPatchListUpdate() or startRender()
    private synchronized void initAudioRecordSink() {
        int fmTuner = DEFAULT_FM_TURNER;
        try {
            Class<?> audioSource = Class.forName("android.media.MediaRecorder$AudioSource");
            Field field = audioSource.getField("FM_TUNER");
            fmTuner = field.getInt(DEFAULT_FM_TURNER);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        mAudioRecord = new AudioRecord(fmTuner,
                SAMPLE_RATE, CHANNEL_CONFIG, AUDIO_FORMAT, RECORD_BUF_SIZE);
        mAudioTrack = new AudioTrack(AudioManager.STREAM_MUSIC,
                SAMPLE_RATE, CHANNEL_CONFIG, AUDIO_FORMAT, RECORD_BUF_SIZE, AudioTrack.MODE_STREAM);
    }
    /**
     * whether FM is power up
     *
     * @return true, power up; false, power down.
     */
    public boolean isPowerUp() {
        return mIsPowerUp;
    }

    private void startAudioTrack() {
        if (mAudioTrack.getPlayState() == AudioTrack.PLAYSTATE_STOPPED) {
            ArrayList<AudioPatch> patches = new ArrayList<AudioPatch>();
            mAudioManager.listAudioPatches(patches);
            mAudioTrack.play();
        }
    }

    private synchronized void releaseAudioPatch() {
        if (mAudioPatch != null) {
            Tool.toolLog(TAG + "releaseAudioPatch");
            mAudioManager.releaseAudioPatch(mAudioPatch);
            mAudioPatch = null;
        }
        mAudioSource = null;
        mAudioSink = null;
    }

    public boolean powerDownFM() {
        Tool.toolLog(TAG + ">>> MTKReceiver.powerDown");

        if (!mIsPowerUp) {
            Tool.toolLog(TAG + "Error: device is already power down.");
            return true;
        }
//        setMute(true);

        enableFmAudio(false);

        // setRDS(false);
        if (mIsMTKEnhancePatchOn) {
            if (!FmNative.powerDown(0)) {
                Tool.toolLog(TAG + "Error: powerdown failed.");
                return false;
            }
        }
        else {
            if (!FmRadioNative.powerDown(0)) {
                Tool.toolLog(TAG + "Error: powerdown failed.");
                return false;
            }
        }

        mIsPowerUp = false;
        Tool.toolLog(TAG + "<<< MTKReceiver.powerDown: true");

        return true;
    }

    /**
     * close FM device
     *
     * @return true if close FM device success, false close FM device failed
     */
    public boolean closeDevice() {
        Tool.toolLog(TAG + ">>> MTKReceiver.closeDevice");
        boolean isDeviceClose = false;
        if (mIsDeviceOpen) {
            if (mIsMTKEnhancePatchOn) {
                isDeviceClose = FmNative.closeDev();
            }
            else {
                isDeviceClose = FmRadioNative.closeDev();
            }

            mIsDeviceOpen = !isDeviceClose;
        }
        //Defect293180 by bing.wang.hz 2015.06.02 begin
        Tool.toolLog(TAG + "<<< MTKReceiver.closeDevice: " + isDeviceClose);

        //add by bing.wang.hz for FMRadio MTK L1 adapter BEGIN
        if (mIsMTKEnhancePatchOn) {
            exitRenderThread();
            releaseAudioPatch();
//            unregisterAudioPortUpdateListener();
        }
        else {
            if (null != mFMPlayer) {
                mFMPlayer.release();
                mFMPlayer = null;
            }
        }
        //Defect293180 by bing.wang.hz 2015.06.02 end
        //add by bing.wang.hz for FMRadio MTK L1 adapter END

        return !mIsDeviceOpen;
    }
    private synchronized void exitRenderThread() {
        stopRender();
        mRenderThread.interrupt();
        mRenderThread = null;
    }


}