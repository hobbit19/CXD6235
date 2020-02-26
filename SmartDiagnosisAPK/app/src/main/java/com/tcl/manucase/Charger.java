package com.tcl.manucase;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;

import com.tcl.autotest.R;
import com.tcl.autotest.tool.Tool;
import com.tcl.autotest.utils.DownTimeCallBack;
import com.tcl.autotest.utils.Test;
import com.tcl.autotest.utils.TestCountDownTimer;


public class Charger extends Test implements DownTimeCallBack {

	private static String TAG = "ChargerTest";
	String text = "";
	static String sb;
	private TestCountDownTimer testCountDownTimer = null;
	private boolean mRegistered = false;
	IntentFilter inf = null;
	private PlugStateReceiever ChargerRcv = null;
	private boolean ChargerStatus = false;
	private float f1;
	private int f2;
	private boolean flag = false;// PR833244-yinbin.zhang-20141107

	public Charger(ID id, String name, Boolean updateFlag, long timeout) {
		super(id, name, updateFlag, timeout);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void setUp() {
		// TODO Auto-generated method stub
		Tool.toolLog(TAG + "_start_test");
		inf = new IntentFilter();
		inf.addAction(Intent.ACTION_UMS_DISCONNECTED);
		inf.addAction(Intent.ACTION_UMS_CONNECTED);
		inf.addAction(Intent.ACTION_BATTERY_CHANGED);

		testCountDownTimer = new TestCountDownTimer(SECOND * 2, SECOND, this);

		if (!mRegistered) {
			ChargerRcv = new PlugStateReceiever();
			mContext.registerReceiver(ChargerRcv, inf);
			mRegistered = true;
		}

		ShowStatus(ChargerStatus);
		flag = false;// PR833244-yinbin.zhang-20141107
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
		// PR661395-yinbin-zhang-20140519 begin
		/*
		 * bt_left.setText(R.string.fail); bt_right.setText(R.string.pass);
		 * bt_right.setEnabled(false);
		 * 
		 * bt_left.setOnClickListener(failed_listener);
		 * bt_right.setOnClickListener(pass_listener);
		 */
		bt_left.setText(R.string.pass);
		bt_right.setText(R.string.fail);
		bt_left.setEnabled(false);

		bt_left.setOnClickListener(pass_listener);
		bt_right.setOnClickListener(failed_listener);
		// PR661395-yinbin-zhang-20140519 end
		mContext.setContentView(mLayout);
	}

	@Override
	public void timeout() {
		// TODO Auto-generated method stub
		if (!flag) {
			text_cen_zone.setText("Charger Time out.");
			bt_right.setEnabled(true);
		}// PR833244-yinbin.zhang-20141107
		finish();
	}

	@Override
	public void destroy() {
		// TODO Auto-generated method stub
		ChargerStatus = false;
		if (mRegistered) {
			mContext.unregisterReceiver(ChargerRcv);
			mRegistered = false;
		}
		if (testCountDownTimer != null) {
			testCountDownTimer.cancel();
		}
	}

	@Override
	public void onFinish() {
		// TODO Auto-generated method stub
		// PR661428-yinbin-zhang-20140519 begin
		/*
		 * text = "Charger : OK \n\n "+"Please remove Charger cable \n";
		 * text_cen_zone.setText(text);
		 */
		bt_left.setEnabled(true);
		// PR661428-yinbin-zhang-20140519 end
	}

	@Override
	public void onTick() {
		// TODO Auto-generated method stub

	}

	private void ShowStatus(boolean usbStatus) {
		text = "Please insert Charger cable.\n\n";
		if (ChargerStatus) {
			// PR661428-yinbin-zhang-20140519 begin
			// text = text + "Charger : OK\n";
			text = "";
			// PR661428-yinbin-zhang-20140519 end
			// add for reading ChargerVoltage FG_Current---Begin
			sb = readFromPath("/sys/class/power_supply/battery/ChargerVoltage");
			// PR661428-yinbin-zhang-20140519 begin
			// text = text + "\nVoltage :  " +sb;
			// text = text + "\nCharge Voltage is:  " +sb;
			sb = sb.substring(0, sb.length() - 1);
			f1 = ((float) Integer.valueOf(sb)) / 1000.00f;
			text = text + "\nCharge Voltage is:  " + String.valueOf(f1) + "V";
			// PR661428-yinbin-zhang-20140519 end
			// sb=readFromPath("/sys/bus/platform/devices/mt6320-fgadc/FG_Current");
			// sb=readFromPath("/sys/devices/platform/battery/FG_Battery_CurrentConsumption");
			sb = readFromPath("/sys/devices/platform/battery_meter/FG_Current");
			// PR661428-yinbin-zhang-20140419 begin
			// text = text + "Current :  " +sb +"\n";
			// text = text + "\nCharge Current is:  " +sb +"\n";
			sb = sb.substring(0, sb.length() - 1);
			f2 = Integer.valueOf(sb) / 10;
			text = text + "\nCharge Current is:  " + String.valueOf(f2) + "mA"
					+ "\n";
			text = text + "Charger : OK \n\n "
					+ "Please remove Charger cable \n";// PR833244-yinbin.zhang-20141107
			flag = true;// PR833244-yinbin.zhang-20141107
			// PR661428-yinbin-zhang-20140519 end
			// add for reading ChargerVoltage FG_Current---End
		} else {
			text = text + "Charger : unknown\n";
		}
	}

	private String readFromPath(String path) {
		File file = new File(path);
		InputStreamReader ois = null;
		StringBuilder sb = new StringBuilder();
		try {
			ois = new InputStreamReader(new FileInputStream(file));
			int num;
			char[] buf = new char[1024];
			while ((num = ois.read(buf)) > 0)
				sb.append(buf, 0, num);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			if (ois != null) {
				try {
					ois.close();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
			if (ois != null) {
				try {
					ois.close();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		}
		return sb.toString();
	}

	public class PlugStateReceiever extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {

			String action = intent.getAction();
//			Tool.toolLog(TAG + " intent received 111: " + action);
			if (action.equals(Intent.ACTION_BATTERY_CHANGED)) {
				int mBatteryPlug = intent.getIntExtra(
						BatteryManager.EXTRA_PLUGGED, -1);
//				Tool.toolLog(TAG + " intent received 222: " + mBatteryPlug);

				if (mBatteryPlug == BatteryManager.BATTERY_PLUGGED_AC
						&& !ChargerStatus) {
					ChargerStatus = true;
					ShowStatus(ChargerStatus);
					text_cen_zone.setText(text);
					testCountDownTimer.start();

				} else if (mBatteryPlug != BatteryManager.BATTERY_PLUGGED_AC
						&& ChargerStatus) {
					// PR661428-yinbin-zhang-20140519 begin
					// bt_right.setEnabled(true);
					bt_left.setEnabled(true);
					// PR661428-yinbin-zhang-20140519 end
					// PR480725-yancan-zhu-20130702 begin
					// mContext.setResult(Test.RESULT.PASS.ordinal());
					// mContext.finish();
					// PR480725-yancan-zhu-20130702 eng
				}
			}
		}
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
