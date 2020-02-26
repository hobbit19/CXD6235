package com.tcl.autotest.utils;

import com.tcl.autotest.AllMainActivity;
import com.tcl.autotest.tool.Tool;

import android.content.Context;
import android.graphics.Rect;
import android.util.DisplayMetrics;
import android.view.WindowManager;

import java.lang.reflect.Method;

public class Lcd {
	WindowManager wm;
	public Lcd(Context mContext) {
		 wm = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
		screenWidth  = wm.getDefaultDisplay().getWidth();
		if(AllMainActivity.deviceName.contains("Pepito")){
			screenHeight = getRealMetrics().heightPixels;
		}else {
			screenHeight = wm.getDefaultDisplay().getHeight();
		}

		screenHeight = screenHeight-AllMainActivity.statusBarHeight;
	}

	private int screenWidth, screenHeight;

	public int width() {
		return screenWidth;
	}

	public int height() {
		return screenHeight;
	}
	protected DisplayMetrics getRealMetrics() {
		DisplayMetrics dm = new DisplayMetrics();
		@SuppressWarnings("rawtypes")
		Class c;
		try {
			c = Class.forName("android.view.Display");
			@SuppressWarnings("unchecked")
			Method method = c.getMethod("getRealMetrics", DisplayMetrics.class);
			method.invoke(wm.getDefaultDisplay(), dm);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return dm;
	}
}
