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
/*  Comments : Interface for async event of FM device                         */
/*  File     : src/com/tct/fmradio/device/FMDeviceListener.java            */
/*  Labels   :                                                                */
/* -------------------------------------------------------------------------- */
/* ========================================================================== */
/*     Modifications on Features list / Changes Request / Problems Report     */
/* -------------------------------------------------------------------------- */
/*    date   |        author        |         Key          |     comment      */
/* ----------|----------------------|----------------------|----------------- */
/* 12/04/2012|(Junjie.Qian)         |PR- 355153            |launch music play */
/*           |                      |                      |er popup force cl */
/*           |                      |                      |ose while search  */
/*           |                      |                      |the channel       */
/* ----------|----------------------|----------------------|----------------- */
/* 06/20/2013|Changzhuan.Huang      |PR-472089             |long press display*/
/*           |                      |                      |next channel      */
/* ----------|----------------------|----------------------|----------------- */
/******************************************************************************/

package com.tct.fmradio.device;

public interface FMDeviceListener {


    //[BUGFIX]-Add-BEGIN by TCTNB.(Junjie.Qian),12/04/2012,355153,
    //launch music player popup force close while search the channel
    /**
     * Called when seek action abort
     *
     */
    public void onSeekAbort();
    //[BUGFIX]-Add-END by TCTNB.(Junjie.Qian)

    /**
     * Called when seek action completed
     * @param frequencyKHz frequency in KHz
     */
    public void onSeekComplete(int frequencyKHz);

    /**
     * Called when seek all action completed
     * @param list a list of frequency in KHz
     */
    public void onSeekAllComplete(int[] list);

    //[BUGFIX]-Add-BEGIN by TCTNJ.(changzhuan.huang),06/20/2013,PR472089,
    //Can't displays next channel in FM Radio main screen when long press next/previous frequency
    /**
     * Called when tune status changed
     * @param frequencyKHz frequency in KHz
     */
    public void onTuneStatusChanged(int frequencyKHz);
    //[BUGFIX]-Add-END by TCTNJ.(changzhuan.huang)

    /**
     * Called when new PS info received
     * @param psText PS text
     */
    public void onRdsPS(String psText);

    /**
     * Called when new AF info received
     * @param list AF list
     */
    public void onRdsAF(int[] list);

    /**
     * Called when new RT info received
     * @param rtText re text
     */
    public void onRdsRT(String rtText);
    
    /**
     * called when a2dp connected
     * */
    public void onA2dpConnectent(boolean a2dpConnected);
}
