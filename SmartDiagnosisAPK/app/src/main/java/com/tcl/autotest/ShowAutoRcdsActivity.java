package com.tcl.autotest;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;

import com.tcl.autotest.tool.Tool;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.widget.EditText;

public class ShowAutoRcdsActivity extends Activity {

	private static final String TAG = "ShowAutoRcdsActivity";
	EditText edShowRcds;
	String wholeName;
	StringBuffer sb;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.showautorcds);

		edShowRcds = (EditText) findViewById(R.id.edShowRcds);
		edShowRcds.setCursorVisible(false);

		Intent intent = getIntent();
		Bundle bundle = intent.getExtras();

		wholeName = bundle.getString("RCD_FILE_AUTO_NAME");
		Tool.toolLog(TAG + " 13131313131 wholeName " + wholeName);
		
		sb = new StringBuffer();

		new Thread() {
			@Override
			public void run() {
				super.run();
				getTextContext();
			}
		}.start();
	}

	private void getTextContext() {
		FileInputStream fin;
		InputStreamReader insr;
		BufferedReader br;
		try {
			fin = new FileInputStream(wholeName);
			insr = new InputStreamReader(fin);
			br = new BufferedReader(insr);

			String line;
			while ((line = br.readLine()) != null) {
				sb.append(line);
				sb.append("\n");
			}

			// 通过handler得消息该消息标识0消息内容datahandler.sendMessage(msg);//发送
			Message msg = handler.obtainMessage(0, null);
			Tool.toolLog(TAG + " 1313131 " + sb.toString());
			handler.sendMessage(msg);
			br.close();
			insr.close();
			fin.close();
		} catch (Exception e) {
			e.printStackTrace();
			Tool.toolLog(TAG + " " + e.toString());
		}
	}

	public Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 0:
				Tool.toolLog(TAG + " handleMessage ... show text");
				edShowRcds.setText(sb.toString());
				edShowRcds.setTextColor(Color.RED);
				edShowRcds.invalidate();
				break;
			default:
				break;
			}
		};
	};
}
