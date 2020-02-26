package com.tcl.autotest.tool;

import com.tcl.autotest.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.WindowManager;
import android.widget.FrameLayout;
import static android.view.WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL;
import static android.view.WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED;
import static android.view.WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
import static android.view.WindowManager.LayoutParams.TYPE_SYSTEM_ERROR;

public class HomeKeyLocker {
	
	private static final String TAG = "HomeKeyLocker";
	
	private OverlayDialog mOverlayDialog;

    public void lock(Activity activity) {
        if (mOverlayDialog == null) {
        	Tool.toolLog(TAG + " show 3333333333");
            mOverlayDialog = new OverlayDialog(activity);
            mOverlayDialog.show();
        }
    }

    public void unlock() {
        if (mOverlayDialog != null) {
        	Tool.toolLog(TAG + " dismiss 444444444444");
            mOverlayDialog.dismiss();
            mOverlayDialog = null;
        }
    }

    private static class OverlayDialog extends AlertDialog {

		public OverlayDialog(Activity activity) {
            super(activity, R.style.OverlayDialog);
            WindowManager.LayoutParams params = getWindow().getAttributes();
            params.type = TYPE_SYSTEM_ERROR;
            params.dimAmount = 0.0F; // transparent
            params.width = 0;
            params.height = 0;
            params.gravity = Gravity.BOTTOM;
            getWindow().setAttributes(params);
            getWindow().setFlags(FLAG_SHOW_WHEN_LOCKED | FLAG_NOT_TOUCH_MODAL, 0xffffff);
            setOwnerActivity(activity);
            setCancelable(false);
        }

        public final boolean dispatchTouchEvent(MotionEvent motionevent) {
        	Tool.toolLog(TAG + " dispatchTouchEvent 55555555555");
            return true;
        }

        protected final void onCreate(Bundle bundle) {
            super.onCreate(bundle);
            Tool.toolLog(TAG + " onCreate HomeLock");
            FrameLayout framelayout = new FrameLayout(getContext());
            framelayout.setBackgroundColor(0);
            setContentView(framelayout);
        }
    }
}
