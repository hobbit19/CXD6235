package com.tcl.manucase;

import java.io.IOException;

import com.tcl.autotest.R;
import com.tcl.autotest.autoRunActivity;
import com.tcl.autotest.manuRunActivity;
import com.tcl.autotest.tool.Tool;
import com.tcl.autotest.utils.FinishThread;
import com.tcl.autotest.utils.ManuFinishThread;
import com.tcl.autotest.utils.Msg;
import com.tcl.autotest.utils.Test;
import com.tcl.autotest.utils.TracabilityStruct;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

//import com.android.internal.os.storage.ExternalStorageFormatter;

public class FactoryresetTest extends Test {

	private final static String TAG = "FactoryresetTest";
	private TracabilityStruct mTStruct;

	private static final String BROADCAST_TO_FACTORY_RESET = "android.intent.action.MASTER_CLEAR";
	private AlertDialog mAlertDialog, mReset;

	public FactoryresetTest(ID id, String name) {
		super(id, name);
		// TODO Auto-generated constructor stub
//		try {
//			mTStruct = new TracabilityStruct();
//		} catch (Exception e) {
//			Log.d(TAG, e.toString());
//		}
	}

	@Override
	public void setUp() {
		// TODO Auto-generated method stub
		Tool.toolLog(TAG + "_start_test");
	}

	@Override
	public void initView() {
		// TODO Auto-generated method stub
		mLayout = (LinearLayout) View.inflate(mContext, R.layout.screen_button,
				null);
		Button bt_reset = (Button) mLayout
				.findViewById(R.id.execute_factory_reset);
		text_top_zone = (TextView) mLayout.findViewById(R.id.text_top_zone);
		text_cen_zone = (TextView) mLayout.findViewById(R.id.text_cen_zone);
		text_top_zone.setText(mName);
		bt_reset.setText("Erase Everything......");
		bt_reset.setTextSize(22);
		bt_reset.setOnClickListener(reset_listener);
		mContext.setContentView(mLayout);
	}

	protected OnClickListener reset_listener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			AlertDialog mReset = new AlertDialog.Builder(mContext)
					.setTitle("Message")
					.setMessage("" + "Factory Reset")
					.setPositiveButton("OK",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int whichButton) {
									//Add by Jianke.Zhang 02/02
									Msg.exitWithSuccessTest(mContext, TAG, 10, false,"Pass");
									new ManuFinishThread(0x01).start();
									
									//Write data into file
									Msg.WriteModelResult(mContext,manuRunActivity.filetxt, "PASS");
									//End
									Factoryreset();
								}
							})
					.setNegativeButton("Cancel",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int whichButton) {
									//Add by Jianke.Zhang 02/02
									Msg.exitWithException(mContext, TAG, 50,false,"Pass");
									
									//Write data into file
									Msg.WriteModelResult(mContext,manuRunActivity.filetxt, "FAIL");
									//End
								}
							}).show();
		}
	};

	private void Factoryreset() {

		Intent intent = new Intent("/");
		intent.setAction(BROADCAST_TO_FACTORY_RESET);
		mContext.sendBroadcast(intent);
//		mContext.finish();

//		 Intent intent = new Intent(ExternalStorageFormatter.FORMAT_AND_FACTORY_RESET);
//		 intent.setComponent(ExternalStorageFormatter.COMPONENT_NAME);
//		 mContext.startService(intent);
	}

	@Override
	public void finish() {
		// TODO Auto-generated method stub
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
	public void destroy() {
		// TODO Auto-generated method stub
		
	}

}
