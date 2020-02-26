package com.tcl.manucase;

import java.util.ArrayList;

import android.graphics.Color;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.tcl.autotest.R;
import com.tcl.autotest.manuRunActivity;
import com.tcl.autotest.tool.Tool;
import com.tcl.autotest.utils.DownTimeCallBack;
import com.tcl.autotest.utils.ManuFinishThread;
import com.tcl.autotest.utils.Msg;
import com.tcl.autotest.utils.RectView;
import com.tcl.autotest.utils.Test;
import com.tcl.autotest.utils.TestCountDownTimer;
import com.tcl.autotest.utils.Test.ID;

public class LcdAllColorTest extends Test implements DownTimeCallBack{

	private static final String TAG = "LcdAllColorTest";
	final static ArrayList alt = new ArrayList();
	int[] mColors = { Color.RED, Color.GREEN, Color.BLUE };
	static int[] mColors1 = { Color.BLACK };
	static int[] mColors2 = { Color.GRAY };
	private final static int MaxRows = 16;
	static int[] mColors3 = new int[MaxRows];
	static int[] mColors4 = { Color.WHITE };
	private int index = 0;
//	static FrameLayout f;
//	static RectView r;
//	static LinearLayout m;

	static FrameLayout fl ;
	static RectView rv = null;
	private TestCountDownTimer testCountDownTimer = null;
	
	public LcdAllColorTest(ID id, String name) {
		super(id, name);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void setUp() {
		// TODO Auto-generated method stub
		Tool.toolLog(TAG + "_start_test");
		testCountDownTimer = new TestCountDownTimer(25*SECOND, SECOND, this);
	}

	@Override
	public void initView() {
		// TODO Auto-generated method stub
		fl = new FrameLayout(mContext);

		mLayout = (LinearLayout) View.inflate(mContext,
				R.layout.manu_base_screen, null);

		bt_left = (Button) mLayout.findViewById(R.id.bt_left);
		bt_right = (Button) mLayout.findViewById(R.id.bt_right);
		bt_left.setText(R.string.pass);
		bt_right.setText(R.string.fail);
		bt_left.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {

				if(index > 4){
					pass();
					bt_left.setEnabled(false);
					return;
				}
				cHandler.sendEmptyMessage(0x20);
			}
		});
		bt_right.setOnClickListener(failed_listener);
		bt_left.setVisibility(View.INVISIBLE);
		bt_right.setVisibility(View.INVISIBLE);

		testCountDownTimer.start();
		
		rv = new RectView(mContext, mColors);
		//Tool.sleepTimes(20);
		changeIdle();
		//startPreviewColor();
        cHandler.sendEmptyMessageDelayed(0x20, 500);
		showKey();
		hideNavigationBar();
		// new LcdBlackTest(Test.ID.LCD_BLACK, "LCD BLACK");
		// new LcdGreyTest(Test.ID.LCD_GREYCHART, "LCD GREYCHART");
		// new LcdGreyChartTest(Test.ID.LCD_LEVEL, "LCD GRAYLEVEL");
		// new LcdWhiteTest(Test.ID.LCD_WHITE, "LCD WHITE");
	}

	@Override
	public void destroy() {
		// TODO Auto-generated method stub
		//Tool.toolLog(TAG + " finish ");
		if (testCountDownTimer != null) {
			testCountDownTimer.cancel();
		}
	}

	public  Handler cHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			index++;
			if (msg.what == 0x20) {
				if(index == 6){
					showKey();
					return;
				}
				
//				Tool.toolLog(TAG + " alt " + alt);
//				f = (FrameLayout) alt.get(0);
//				r = (RectView) alt.get(1);
//				m = (LinearLayout) alt.get(2);
//				f.addView(r);
//				f.addView(m);
//				mContext.setContentView(f);
				changeIdle();
//				Tool.toolLog(TAG + " 3333333333");
				startPreviewColor();
			} 
		}
	};

	public void showKey(){
		bt_left.setVisibility(View.VISIBLE);
		bt_right.setVisibility(View.VISIBLE);
	}
	
	public void changeIdle(){
		if (mLayout != null) {
			fl.removeView(rv);
			fl.removeView(mLayout);
//			Tool.toolLog(TAG + " 444444444444");
		}
		fl.addView(rv);
		fl.addView(mLayout);
		mContext.setContentView(fl);
	}
	
	public  void startPreviewColor(/*FrameLayout fl, RectView rv,
			LinearLayout mLayout*/) {
//		Tool.toolLog(TAG + " 66666666666666 " + index);
		if(index == 1){
//			Tool.toolLog(TAG + " 343434343434"); 
			rv = new RectView(mContext, mColors1);
		}else if(index == 2){
			rv = new RectView(mContext, mColors2);
		}else if(index == 3){
			for (int i = 0; i < mColors3.length; i++) {
				mColors3[i] = Color.BLACK + 0x111111 * i;
			}
			rv = new RectView(mContext, mColors3);
		}else if(index == 4){
			rv = new RectView(mContext, mColors4);
		}
//		alt.clear();
//		alt.add(fl);
//		alt.add(rv);
//		alt.add(mLayout);
		
		//new Thread() {
			//public void run() {
				//Message msg = new Message();
				//msg.what = 0x20;
//				msg.obj = alt;
//				cHandler.sendEmptyMessageDelayed(0x20, 1000);
			//}
		//}.start();
	}

	@Override
	public void onFinish() {
		// TODO Auto-generated method stub
		Tool.toolLog(TAG + " onFinish ");
	}

	@Override
	public void onTick() {
		// TODO Auto-generated method stub
		Tool.toolLog(TAG + " all color onTick ");
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
