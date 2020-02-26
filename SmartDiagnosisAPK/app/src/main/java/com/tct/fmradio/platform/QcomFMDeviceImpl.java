package com.tct.fmradio.platform;

import com.tct.fmradio.device.FMDevice;


public class QcomFMDeviceImpl implements FMDevice {

    @Override
    public boolean powerOn() {
        return false;
    }

    @Override
    public boolean powerOff() {
        return false;
    }

    @Override
    public boolean isPowerOn() {
        return false;
    }

    @Override
    public boolean setFrequency(int frequencyKHz) {
        return false;
    }

    @Override
    public int getFrequency() {
        return 0;
    }

    @Override
    public boolean enableSpeaker() {
        return false;
    }

    @Override
    public boolean disableSpeaker() {
        return false;
    }

    @Override
    public boolean isSpeakerEnabled() {
        return false;
    }

    @Override
    public boolean seekUp() {
        return false;
    }

    @Override
    public boolean seekDown() {
        return false;
    }

    @Override
    public boolean seekAll() {
        return false;
    }

    @Override
    public boolean abortSeek() {
        return false;
    }

    @Override
    public boolean mute() {
        return false;
    }

    @Override
    public boolean unmute() {
        return false;
    }

    @Override
    public boolean isMuted() {
        return false;
    }

    @Override
    public boolean setNFL(int NFL) {
        return false;
    }

    @Override
    public int getNFL() {
        return 0;
    }

    @Override
    public boolean enableRDS() {
        return false;
    }

    @Override
    public boolean disableRDS() {
        return false;
    }

    @Override
    public boolean isRDSEnabled() {
        return false;
    }

    @Override
    public String getRdsPS() {
        return null;
    }

    @Override
    public int[] getRdsAF() {
        return new int[0];
    }

    @Override
    public String getRdsRT() {
        return null;
    }

    @Override
    public boolean isA2dpAvailable() {
        return false;
    }

    @Override
    public void handleA2dpConnection(boolean connected) {

    }

    @Override
    public void closeDevice() {

    }

    @Override
    public void onSearchListComplete() {

    }

    @Override
    public void onSeekComplete() {

    }

    @Override
    public boolean isRDSsupport() {
        return false;
    }

    @Override
    public int rdsset(boolean aRdson) {
        return 0;
    }

    @Override
    public short readrds() {
        return 0;
    }

    @Override
    public int activeAF() {
        return 0;
    }

    @Override
    public String getMtkRdsPS() {
        return null;
    }

    @Override
    public String getMtkRdsRT() {
        return null;
    }

    @Override
    public boolean enableAutoAF(boolean bEnable) {
        return false;
    }

    @Override
    public void onA2dpConnectent(boolean a2dpConnected) {

    }
}