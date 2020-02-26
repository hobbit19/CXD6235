package com.tcl.autocase;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.IsoDep;
import android.nfc.tech.MifareClassic;
import android.nfc.tech.MifareUltralight;
import android.nfc.tech.Ndef;
import android.nfc.tech.NfcA;
import android.nfc.tech.NfcF;
import android.nfc.tech.NfcV;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.tcl.autotest.AllMainActivity;
import com.tcl.autotest.ExecuteTest;
import com.tcl.autotest.R;
import com.tcl.autotest.autoRunActivity;
import com.tcl.autotest.tool.Tool;
import com.tcl.autotest.utils.DownTimeCallBack;
import com.tcl.autotest.utils.FinishThread;
import com.tcl.autotest.utils.Msg;
import com.tcl.autotest.utils.Test;
import com.tcl.autotest.utils.Test.ID;
import com.tcl.autotest.utils.TestCountDownTimer;

public class NFCAutoTest extends Activity implements DownTimeCallBack,OnClickListener{
	private final static String TAG = "NFCAutoTest";
	String text = "";
	private TestCountDownTimer testCountDownTimer = null;
	// NFC������
		private NfcAdapter nfcAdapter = null;
		// ������ͼ
		private PendingIntent pi = null;
		// �˵�����޷���Ӧ�ʹ����Intent
		private IntentFilter tagDetected = null;
		// �ı��ؼ�
		private TextView promt = null;
		// �Ƿ�֧��NFC���ܵı�ǩ
		private boolean isNFC_support = false;



	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_nfc_demo);
		setupViews();
		initNFCData();
		 testCountDownTimer = new TestCountDownTimer(10*SECOND, SECOND, this);
		   testCountDownTimer.start();
		Log.i("qinhao", "oncreate");
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (isNFC_support == false) {
			// ����豸��֧��NFC����NFC����û��������return��
			return;
		}
		//Log.i("qinhao", "onResume");
		// ��ʼ����NFC�豸�Ƿ�����
		startNFC_Listener();

		if (NfcAdapter.ACTION_TECH_DISCOVERED.equals(this.getIntent()
				.getAction())) {
			// ע�����if�еĴ��뼸�������������Ϊ�ո�����һ�д��뿪���˼���NFC���ӣ���һ�д������Ͼ��յ���NFC���ӵ�intent�����ּ��ʺ�С
			// �����intent
			processIntent(this.getIntent());
		}
		
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		// ��ǰapp����ǰ�˽������У����ʱ����intent���͹�������ôϵͳ�ͻ����onNewIntent�ص���������intent���͹���
		// ����ֻ��Ҫ������������intent�Ƿ���NFC��ص�intent������ǣ��͵��ô�����
		if (NfcAdapter.ACTION_TECH_DISCOVERED.equals(intent.getAction())) {
			processIntent(intent);
		}
	}
	private void startNFC_Listener() {
		// ��ʼ����NFC�豸�Ƿ����ӣ�������Ӿͷ�pi��ͼ
		String[][] TECHLISTS = new String[ ] [ ] {{IsoDep.class.getName()},
                {NfcV.class.getName()}, {NfcF.class.getName()},{NfcA.class.getName()}};
		nfcAdapter.enableForegroundDispatch(this, pi,
				new IntentFilter[] { tagDetected }, TECHLISTS);
	}
	private void setupViews() {
		// �ؼ��İ�
		promt = (TextView) findViewById(R.id.promt);

		// ���ı��ؼ���ֵ��ʼ�ı�
		promt.setText("wait for NFC card....");
		// ��������д��ɾ��ť�ؼ�
	}

	private void initNFCData() {
		// ��ʼ���豸֧��NFC����
		isNFC_support = true;
		// �õ�Ĭ��nfc������
		nfcAdapter = NfcAdapter.getDefaultAdapter(getApplicationContext());
		// ��ʾ��Ϣ����
		String metaInfo = "";
		// �ж��豸�Ƿ�֧��NFC������NFC
		if (nfcAdapter == null) {
			metaInfo = " NO NFC";
			Toast.makeText(this, metaInfo, Toast.LENGTH_SHORT).show();
			isNFC_support = false;
			promt.setTextColor(Color.RED);
			promt.setText(metaInfo);
			return;
		}

		if (!nfcAdapter.isEnabled()) {
			metaInfo = "OPEN NFC IN SETTINGS FIRST!��";
			Toast.makeText(this, metaInfo, Toast.LENGTH_SHORT).show();
			isNFC_support = false;
		}

		if (isNFC_support == true) {
			init_NFC();
		} else {
			promt.setTextColor(Color.RED);
			promt.setText(metaInfo);
		}
	}
	@Override
	public void onFinish() {
		// TODO Auto-generated method stub
		Intent i = new Intent();  
		 i.setAction("com.sdt");  
           i.putExtra("result", "fail"); 
		this.sendBroadcast(i); 
		this.finish();
	}

	@Override
	public void onTick() {
		// TODO Auto-generated method stub
		
	}

	
	//�ַ�����ת��Ϊ16�����ַ���
		private String bytesToHexString(byte[] src) {
			StringBuilder stringBuilder = new StringBuilder("0x");
			if (src == null || src.length <= 0) {
				return null;
			}
			char[] buffer = new char[2];
			for (int i = 0; i < src.length; i++) {
				buffer[0] = Character.forDigit((src[i] >>> 4) & 0x0F, 16);
				buffer[1] = Character.forDigit(src[i] & 0x0F, 16);
				System.out.println(buffer);
				stringBuilder.append(buffer);
			}
			return stringBuilder.toString();
		}

		private Tag tagFromIntent;
		
		/**
		 * Parses the NDEF Message from the intent and prints to the TextView
		 */
		public void processIntent(Intent intent) {
			
			
			if (isNFC_support == false)
				return;

			// ȡ����װ��intent�е�TAG
			tagFromIntent = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
			if(tagFromIntent!=null)
			{
				//Toast.makeText(this, "�ҵ���Ƭ", Toast.LENGTH_SHORT).show();
				Intent i = new Intent();  
				//i.setAction("success");  
				 i.setAction("com.sdt");  
		            i.putExtra("result", "success"); 
				this.sendBroadcast(i); 
				//Toast.makeText(this, "fasongguangbo", Toast.LENGTH_SHORT).show();
				//onSucess();
			}

			else
			{
				//onFail();
				Intent i = new Intent();  
				 i.setAction("com.sdt");  
		            i.putExtra("result", "fail"); 
				this.sendBroadcast(i); 
			}
			this.finish();
	/*		promt.setTextColor(Color.BLUE);
			String metaInfo = "";
			metaInfo += "��ƬID��" + bytesToHexString(tagFromIntent.getId()) + "\n";
			Toast.makeText(this, "�ҵ���Ƭ", Toast.LENGTH_SHORT).show();

			// Tech List
			String prefix = "android.nfc.tech.";
			String[] techList = tagFromIntent.getTechList();

			//����NFC�������ͣ� Mifare Classic/UltraLight Info
			String CardType = "";
			for (int i = 0; i < techList.length; i++) {
				if (techList[i].equals(NfcA.class.getName())) {
					// ��ȡTAG
					NfcA mfc = NfcA.get(tagFromIntent);
					try {
						if ("".equals(CardType))
							CardType = "MifareClassic��Ƭ���� \n ��֧��NDEF��Ϣ \n";
					} catch (Exception e) {
						e.printStackTrace();
					}
				} else if (techList[i].equals(MifareUltralight.class.getName())) {
					MifareUltralight mifareUlTag = MifareUltralight
							.get(tagFromIntent);
					String lightType = "";
					// Type Info
					switch (mifareUlTag.getType()) {
					case MifareUltralight.TYPE_ULTRALIGHT:
						lightType = "Ultralight";
						break;
					case MifareUltralight.TYPE_ULTRALIGHT_C:
						lightType = "Ultralight C";
						break;
					}
					CardType = lightType + "��Ƭ����\n";

					Ndef ndef = Ndef.get(tagFromIntent);
					CardType += "������ݳߴ�:" + ndef.getMaxSize() + "\n";

				}
			}
			metaInfo += CardType;
			promt.setText(metaInfo);*/
			
			
		}
	
		private void stopNFC_Listener() {
			// ֹͣ����NFC�豸�Ƿ�����
			nfcAdapter.disableForegroundDispatch(this);
		}
		
	private void init_NFC() {
		// ��ʼ��PendingIntent������NFC�豸�����ϵ�ʱ�򣬾ͽ�����ǰActivity����
		pi = PendingIntent.getActivity(this, 0, new Intent(this, getClass())
				.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
		// �½�IntentFilter��ʹ�õ��ǵڶ��ֵĹ��˻���
		tagDetected = new IntentFilter(NfcAdapter.ACTION_TECH_DISCOVERED);
		tagDetected.addCategory(Intent.CATEGORY_DEFAULT);
	}
	

	@Override
	public void onClick(DialogInterface dialog, int which) {
		// TODO Auto-generated method stub
		
	}

}
