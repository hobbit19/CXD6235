package com.tcl.manucase;

import android.content.Context;
import android.util.AttributeSet;

import com.tcl.autotest.R;
import com.tcl.manucase.CameraTest.FocusIndicator;

public class FocusIndicatorRotateLayout extends RotateLayout implements FocusIndicator {
	private int mState;
	private static final int STATE_IDLE = 0;
	private static final int STATE_FOCUSING = 1;
	private static final int STATE_FINISHING = 2;

	private Runnable mDisappear = new Disappear();
	private Runnable mEndAction = new EndAction();
	private static final int SCALING_UP_TIME = 1000;
	private static final int SCALING_DOWN_TIME = 200;
	private static final int DISAPPEAR_TIMEOUT = 200;

	public FocusIndicatorRotateLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	private void setDrawable(int resid) {
		mChild.setBackgroundDrawable(getResources().getDrawable(resid));
	}

	@Override
	public void showStart() {
		if (mState == STATE_IDLE) {
			setDrawable(R.drawable.ic_focus_focusing);
			animate().withLayer().setDuration(SCALING_UP_TIME).scaleX(1.5f)
					.scaleY(1.5f);
			mState = STATE_FOCUSING;
		}
	}

	@Override
	public void showSuccess(boolean timeout) {
		if (mState == STATE_FOCUSING) {
			setDrawable(R.drawable.ic_focus_focused);
			animate().withLayer().setDuration(SCALING_DOWN_TIME).scaleX(1f)
					.scaleY(1f).withEndAction(timeout ? mEndAction : null);
			mState = STATE_FINISHING;
		}
	}

	@Override
	public void showFail(boolean timeout) {
		if (mState == STATE_FOCUSING) {
			setDrawable(R.drawable.ic_focus_failed);
			animate().withLayer().setDuration(SCALING_DOWN_TIME).scaleX(1f)
					.scaleY(1f).withEndAction(timeout ? mEndAction : null);
			mState = STATE_FINISHING;
		}
	}

	@Override
	public void clear() {
		animate().cancel();
		removeCallbacks(mDisappear);
		mDisappear.run();
		setScaleX(1f);
		setScaleY(1f);
	}

	private class EndAction implements Runnable {
		@Override
		public void run() {
			// Keep the focus indicator for some time.
			postDelayed(mDisappear, DISAPPEAR_TIMEOUT);
		}
	}

	private class Disappear implements Runnable {
		@Override
		public void run() {
			mChild.setBackgroundDrawable(null);
			mState = STATE_IDLE;
		}
	}

	public boolean isFocusing() {
		return mState != STATE_IDLE;
	}
}
