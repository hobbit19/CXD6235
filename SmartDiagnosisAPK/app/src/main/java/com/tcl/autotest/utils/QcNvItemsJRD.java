/******************************************************************************
 * @file    QcNvItems.java
 * @brief   Implementation of the IQcNvItems interface functions used to get/set
 *          various NV parameters
 *
 * ---------------------------------------------------------------------------
 *  Copyright (C) 2009 QUALCOMM Incorporated.
 *  All Rights Reserved. QUALCOMM Proprietary and Confidential.
 * ---------------------------------------------------------------------------
 *
 *******************************************************************************/

package com.tcl.autotest.utils;

import java.io.IOException;

import android.util.Log;

public class QcNvItemsJRD{

	private static String LOG_TAG = "QC_NV_ITEMS_JRD";

	private static final int HEADER_SIZE = 8;

	private static final boolean enableVLog = true;

	private String TAG = "QcNvItemsJRD";

	// OEM items start at 50000
	public static final int NV_TRACABILITY_I = 50000;

	public static final int NV_TRACABILITY_1_I = 50001;

	public static final int NV_TRACABILITY_2_I = 50002;

	public static final int NV_TRACABILITY_3_I = 50003;

	public static final int NV_MMITEST_INFO_I = 50004;

	private final boolean DEBUG = false;

	public QcNvItemsJRD() {
		super();
		Log.i(TAG, "QcNvItemsJRD instance created.");
	}

	private void LOGD(String s) {
		if (DEBUG) {
			Log.d(TAG, s);
		}
	}

	public void doNvWrite(int itemId, byte[] nvItem) throws IOException {
		LOGD(java.util.Arrays.toString(nvItem));

		JRDRapi.doNvWrite(itemId, nvItem);
	}

	public byte[] doNvRead(int itemId) throws IOException {

		byte[] nvItem = new byte[512];
		JRDRapi.doNvRead(itemId, nvItem);

		return nvItem;
	}

}
