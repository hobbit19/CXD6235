package com.tcl.autotest.utils;

import com.tcl.autotest.tool.Tool;

import android.os.CountDownTimer;

public class TestCountDownTimer extends CountDownTimer{
	
	private DownTimeCallBack callBack = null;

	public TestCountDownTimer(long millisInFuture, long countDownInterval, DownTimeCallBack callBack) {
		super(millisInFuture, countDownInterval);
		// TODO Auto-generated constructor stub
		this.callBack = callBack;
	}
	
	@Override
	public void onFinish() {
		// TODO Auto-generated method stub
		callBack.onFinish();
	}

	@Override
	public void onTick(long millisUntilFinished) {
		// TODO Auto-generated method stub
		callBack.onTick();
	}

}
