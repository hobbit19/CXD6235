package com.tcl.manucase;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.tcl.autotest.R;
import com.tcl.autotest.utils.Test;

public class StylusTest extends Test {

	protected static final String TAG = "StylusTest";
	private String text = "Pull out detected:NOK\n\nPlug in  detected:NOK";
	private String STYLUSIN = "com.jrdcom.ACTION_PEN_INJECT";
	private String STYLUSOUT = "com.jrdcom.ACTION_PEN_EJECT";
	private boolean out = false;
	private boolean in = false;
	private IntentFilter mfilter;

	public StylusTest(ID id, String name) {
		super(id, name);
		// TODO Auto-generated constructor stub
	}

	private BroadcastReceiver stylusre = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			Log.i(TAG, "get receiver");
			String action = intent.getAction();
			if (action.equals(STYLUSOUT)) {
				out = true;
				Log.i(TAG, "out receiver");
			}
			if (action.equals(STYLUSIN)) {
				Log.i(TAG, "in receiver");
				in = true;
			}
			if (out) {
				text = "Pull out detected:OK\n\n";
			} else {
				text = "Pull out detected:NOK\n\n";
			}
			if (in) {
				text += "Plug in  detected:OK";
			} else {
				text += "Plug in  detected:NOK";
			}
			text_cen_zone.setText(text);
			if (in && out) {
				bt_right.setEnabled(true);
			}
		}

	};

	@Override
	public void setUp() {
		// TODO Auto-generated method stub
		mfilter = new IntentFilter();
		mfilter.addAction(STYLUSIN);
		mfilter.addAction(STYLUSOUT);
		mContext.registerReceiver(stylusre, mfilter);
	}

	@Override
	public void initView() {
		// TODO Auto-generated method stub
		mLayout = (LinearLayout) View.inflate(mContext, R.layout.manu_base_screen,
				null);

		bt_left = (Button) mLayout.findViewById(R.id.bt_left);
		bt_right = (Button) mLayout.findViewById(R.id.bt_right);
		text_top_zone = (TextView) mLayout.findViewById(R.id.text_top_zone);
		text_cen_zone = (TextView) mLayout.findViewById(R.id.text_cen_zone);

		text_top_zone.setText(mName);
		text_cen_zone.setText(text);
		bt_right.setText(R.string.pass);
		bt_left.setText(R.string.fail);

		bt_right.setOnClickListener(pass_listener);
		bt_left.setOnClickListener(failed_listener);
		bt_right.setEnabled(false);

		mContext.setContentView(mLayout);
	}

	@Override
	public void destroy() {
		// TODO Auto-generated method stub
		if (/*in && out*/stylusre != null) {
			mContext.unregisterReceiver(stylusre);
		}
		out = false;
		in = false;
	}

	public String getmContextTag() {
		// TODO Auto-generated method stub
		return TAG;
	}

	@Override
	public void setmContextTag() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void finish() {
		// TODO Auto-generated method stub
		
	}

}
