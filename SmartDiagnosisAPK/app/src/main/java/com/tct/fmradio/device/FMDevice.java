/******************************************************************************/
/*                                                               Date:11/2012 */
/*                                PRESENTATION                                */
/*                                                                            */
/*       Copyright 2012 TCL Communication Technology Holdings Limited.        */
/*                                                                            */
/* This material is company confidential, cannot be reproduced in any form    */
/* without the written permission of TCL Communication Technology Holdings    */
/* Limited.                                                                   */
/*                                                                            */
/* -------------------------------------------------------------------------- */
/*  Author :  (fan.hu)                                                        */
/*  Email  :   fan.hu@tcl-mobile.com                                          */
/*  Role   :  FMRadio                                                         */
/*  Reference documents :                                                     */
/* -------------------------------------------------------------------------- */
/*  Comments : Interface of FM device. It will be implemented by different    */
/*             FM hardware like:                                              */
/*                 brcm-src/com/tct/fmradio/device                         */
/*                 AND                                                        */
/*                 qcom-src/com/tct/fmradio/device                         */
/*  File     : src/com/tct/fmradio/device/FMDevice.java                    */
/*  Labels   :                                                                */
/* -------------------------------------------------------------------------- */
/* ========================================================================== */

package com.tct.fmradio.device;

public interface FMDevice {
    public static final int MIN_FREQUENCY = 87500; // in KHz
    public static final int MAX_FREQUENCY = 108000; // in KHz

    /**
     * power on FM
     * 
     * @return true: success, false: failed
     */
    public boolean powerOn();

    /**
     * power off FM
     * 
     * @return true: success, false: failed
     */
    public boolean powerOff();

    /**
     * get status of power on/off
     * 
     * @return true: power on, false: power off
     */
    public boolean isPowerOn();

    /**
     * set frequency
     * 
     * @param frequencyKHz
     *            frequency in KHz
     * @return true: success, false: failed
     */
    public boolean setFrequency(int frequencyKHz);

    /**
     * get frequency
     * 
     * @return frequency in KHz
     */
    public int getFrequency();

    /**
     * audio will route to speaker
     * 
     * @return true: success, false: failed
     */
    public boolean enableSpeaker();

    /**
     * audio will route to headset
     * 
     * @return true: success, false: failed
     */
    public boolean disableSpeaker();

    /**
     * get whether speaker is currently used
     * 
     * @return true: speaker used, false: headset used
     */
    public boolean isSpeakerEnabled();

    /**
     * Asynchronous function. Seek up
     * 
     * @return true if Seek Initiate succeeded, false if Search Initiate failed.
     */
    public boolean seekUp();

    /**
     * Asynchronous function. Seek down
     * 
     * @return true : Seek Initiate succeeded <br>
     *         false : seek Initiate failed.
     */
    public boolean seekDown();

    /**
     * Asynchronous function. Seek all
     * 
     * @return true if Seek all Initiate succeeded, false if Search Initiate
     *         failed.
     */
    public boolean seekAll();

    /**
     * abort seek
     * 
     * @return true: success, false: failed
     */
    public boolean abortSeek();

    /**
     * mute
     * 
     * @return true: success, false: failed
     */
    public boolean mute();

    /**
     * unmute
     * 
     * @return true: success, false: failed
     */
    public boolean unmute();

    /**
     * get mute status
     * 
     * @return true: muted, false: not muted
     */
    public boolean isMuted();

    /**
     * set NFL (Noise Floor Level)
     * 
     * @param NFL
     *            level
     * @return true: success, false: failed
     */
    public boolean setNFL(int NFL);

    /**
     * get NFL (Noise Floor Level)
     * 
     * @return current level
     */
    public int getNFL();

    /**
     * enable RDS
     * 
     * @return true: success, false: failed
     */
    public boolean enableRDS();

    /**
     * disable RDS
     * 
     * @return true: success, false: failed
     */
    public boolean disableRDS();

    /**
     * get RDS enabled status
     * 
     * @return true: enabled, false: disabled
     */
    public boolean isRDSEnabled();

    /**
     * get RDS info: Program Service
     * 
     * @return text of Program Service
     */
    public String getRdsPS();

    /**
     * get RDS info: Access Frequency
     * 
     * @return a list of Access Frequency
     */
    public int[] getRdsAF();

    /**
     * get RDS info: Radio Text
     * 
     * @return text of Radio Text
     */
    public String getRdsRT();

    public boolean isA2dpAvailable();

    public void handleA2dpConnection(boolean connected);

    /**
     * only use in MTK.
     */
    public void closeDevice();

    /**
     * only use in MTK.
     */
    public void onSearchListComplete();

    /**
     * only use in MTK.
     */
    public void onSeekComplete();
    
	//PR857620 In FM, "Station info" doesn't function at all by bing.wang.hz 2014.12.09 begin
    /**
     * only use in MTK.
     */
    public boolean isRDSsupport();
    
    /**
     * only use in MTK.
     */
    public int rdsset(boolean aRdson);
    
    /**
     * only use in MTK.
     */
    public short readrds();
    
    /**
     * only use in MTK.
     */
    public int activeAF();
    
    /**
     * get RDS info: Program Service
     * 
     * @return text of Program Service
     */
    public String getMtkRdsPS();

    /**
     * get RDS info: Radio Text
     * 
     * @return text of Radio Text
     */
    public String getMtkRdsRT();
	//PR857620 In FM, "Station info" doesn't function at all by bing.wang.hz 2014.12.09 end
    
    /**
     * only use in QCT
     * @param bEnable true for enable AF, false for disable AF
     * @return true if enable successfully
     */
    public boolean enableAutoAF(boolean bEnable);
    
    /**
     * for MTK
     * */
    public void onA2dpConnectent(boolean a2dpConnected);
}
