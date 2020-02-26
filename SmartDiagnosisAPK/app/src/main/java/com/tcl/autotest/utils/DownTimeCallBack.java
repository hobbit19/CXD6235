package com.tcl.autotest.utils;

import android.content.Intent;

public interface DownTimeCallBack {
	public static final long SECOND = 1000;
	public void onFinish();
	public void onTick();
	
}
