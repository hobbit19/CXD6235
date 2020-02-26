package com.tcl.autocase;

import android.content.Context;
import android.graphics.Bitmap;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.tcl.autotest.AllMainActivity;
import com.tcl.autotest.ExecuteTest;
import com.tcl.autotest.R;
import com.tcl.autotest.autoRunActivity;
import com.tcl.autotest.tool.Tool;
import com.tcl.autotest.utils.DownTimeCallBack;
import com.tcl.autotest.utils.FinishThread;
import com.tcl.autotest.utils.Msg;
import com.tcl.autotest.utils.Test;
import com.tcl.autotest.utils.TestCountDownTimer;


public class GSensorTestAuto extends Test implements SensorEventListener,DownTimeCallBack{

	private LinearLayout psview;
	private static String TAG = "GSensorTest";
	private String text = "";
	float mAngleToYaxis;
	float mAngleToXaxis;
	float mAngleToZaxis;
	float mOldAngleToYaxis;
	float mOldAngleToXaxis;
	float mOldAngleToZaxis;
	private View mView;
	private Button btnPcba;
	private boolean init;
	private boolean PCBA = false;
	LinearLayout addview;
	private Handler mhandler = new Handler();
	private TextView data;
	private TextView text_cen_zone;
	private Runnable runnable = null;
	private boolean isPixi34 = false;
	private boolean isStop = false;
	boolean isGensensor = false;
	int mSecon = 10;
	private enum position {
		UNDEF, UP, DOWN, LEFT, RIGHT, FACE_UP, FACE_DOWN
	};
	private TestCountDownTimer testCountDownTimer = null;
	private int POS_BIT_UP = 0x1;
	private int POS_BIT_DOWN = 0x2;
	private int POS_BIT_LEFT = 0x4;
	private int POS_BIT_RIGHT = 0x8;
	private int POS_BIT_FACE_DOWN = 0x10;
	private int POS_BIT_FACE_UP = 0x20;
	private int POS_BIT_ALL = POS_BIT_UP | POS_BIT_DOWN | POS_BIT_LEFT
			| POS_BIT_RIGHT | POS_BIT_FACE_DOWN | POS_BIT_FACE_UP;
	private SensorManager mSensorManager;

	private position mPosition = position.UNDEF;
	int mPositionChecked = 0;
	position mOldPosition;
	private Sensor gsensor;
    boolean isChangeStop = false;
	Bitmap mBitmap;
	public GSensorTestAuto(ID id, String name) {
		super(id, name);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// TODO Auto-generated method stub
		
	}
	class DDDVect {
		float Vx;
		float Vy;
		float Vz;

		DDDVect(float x, float y, float z) {
			Vx = x;
			Vy = y;
			Vz = z;
		}

		public float getYAngle() {
			return getAngle(Vy);
		}

		public float getXAngle() {
			return getAngle(Vx);
		}

		public float getZAngle() {
			return getAngle(Vz);
		}

		private float getAngle(float ref) {
			return (float) Math.toDegrees(Math.acos(ref
					/ Math.sqrt(Vx * Vx + Vy * Vy + Vz * Vz)));
		}

	}
	@Override
	public void onSensorChanged(SensorEvent event) {
		// TODO Auto-generated method stub
		//if(!isStop)
		//{
		
	
		
		if(!isChangeStop)
		{
			Log.v("qinhao", "onSensorChanged");
		
		 mOldPosition = mPosition;

//		Tool.toolLog(TAG + " onSensorChanged: (" + event.values[0] + ", "
//				+ event.values[1] + ", " + event.values[2] + ")");
		 mOldAngleToYaxis = mAngleToYaxis;
		 mOldAngleToXaxis = mAngleToXaxis;
		 mOldAngleToZaxis = mAngleToZaxis;
		DDDVect mDir = new DDDVect(event.values[0], event.values[1],
				event.values[2]);

		mAngleToYaxis = mDir.getYAngle();
		mAngleToXaxis = mDir.getXAngle();
		mAngleToZaxis = mDir.getZAngle();
			/*if (mAngleToYaxis < 10) {
				mPosition = position.UP;
				mPositionChecked |= POS_BIT_UP;
			} else if (mAngleToYaxis > 170) {
				mPosition = position.DOWN;
				mPositionChecked |= POS_BIT_DOWN;
			} else if (mAngleToXaxis < 10) {
				mPosition = position.LEFT;
				mPositionChecked |= POS_BIT_LEFT;
			} else if (mAngleToXaxis > 170) {
				mPosition = position.RIGHT;
				mPositionChecked |= POS_BIT_RIGHT;
			} else if (mAngleToZaxis > 170) {
				mPosition = position.FACE_DOWN;
				mPositionChecked |= POS_BIT_FACE_DOWN;
			} else if (mAngleToZaxis < 10) {
				mPosition = position.FACE_UP;
				mPositionChecked |= POS_BIT_FACE_UP;
			} else {
				mPosition = position.UNDEF;
			}*/
		    
			StringBuilder ps_info = new StringBuilder();
			ps_info.append("GSensor detected");
			ps_info.append("\n\n\n" + "Xaxis:" + mAngleToXaxis);
			ps_info.append("\n\n\n" + "Yaxis:" + mAngleToYaxis);
			ps_info.append("\n" + "Zaxis:" + mAngleToZaxis);
			text = ps_info.toString();
			text_cen_zone.setText(text);
		
		if (mOldAngleToYaxis != mAngleToYaxis||mOldAngleToXaxis!= mAngleToXaxis||mOldAngleToZaxis!=mAngleToZaxis) {

			isChangeStop = true;   
				mContext.setContentView(mLayout);
				
				mHandler.sendEmptyMessageDelayed(0, SECOND);
				//isStop = true;
			//}
		}
		}
		}

public Handler mHandler = new Handler(){
		
		public void handleMessage(android.os.Message msg) {	
			
			
				FinishThread tFinishThread = new FinishThread(0x01,ExecuteTest.temppositon);
				tFinishThread.start();
				try {
					tFinishThread.join();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				Msg.exitWithSuccessTest(mContext,TAG,10,true,"Pass");
				
				//Write data into file
				if(AllMainActivity.mainAllTest && !AllMainActivity.autofileFlag){
					Msg.WriteModelResult(mContext,AllMainActivity.all_items_file_text, "PASS");
				}else{
					Msg.WriteModelResult(mContext,autoRunActivity.auto_file_text, "PASS");
				
				//End
			}
		};
	};


	@Override
	public void setUp() {
		// TODO Auto-generated method stub
		Tool.toolLog(TAG + "_start_test");
		if(AllMainActivity.deviceName.endsWith("Pixi3-4")){
			isPixi34 = true;
		}
		init = false;
		if (mSensorManager == null) {
			mSensorManager = (SensorManager) mContext
					.getSystemService(Context.SENSOR_SERVICE);
			Log.v("qinhao", "mSensorManager");
		}
		mPositionChecked = POS_BIT_ALL & ~POS_BIT_UP;
		gsensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
//		Tool.toolLog(TAG + " gsensor " + gsensor);
		Log.v("qinhao", "mSensorManager11111111111"+gsensor);
		if (gsensor != null) {
			Tool.toolLog(TAG + " GSensor opened : " + gsensor.getName());
			if (!mSensorManager.registerListener(this, gsensor,
					SensorManager.SENSOR_DELAY_NORMAL)) {
				Tool.toolLog(TAG + " register listener for sensor " + gsensor.getName()
						+ " failed");
			}
			text = "Opening.....";
			
		}
	
		testCountDownTimer = new TestCountDownTimer(SECOND*mSecon, SECOND, this);
		
		
		/*mView = new PositionView(mContext);
		mView.setPadding(5, 25, 5, 10);*/
	}

	@Override
	public void initView() {
		// TODO Auto-generated method stub
		mLayout = (LinearLayout) View.inflate(mContext,
				R.layout.psensor_base_screen_view, null);

		text_top_zone = (TextView) mLayout.findViewById(R.id.text_top_zone);
		text_cen_zone = new TextView(mContext);
		text_cen_zone.setGravity(Gravity.CENTER);
	
		psview = (LinearLayout) mLayout.findViewById(R.id.addview);
		psview.setGravity(Gravity.CENTER);
		psview.addView(text_cen_zone);
		//addview = (LinearLayout) mLayout.findViewById(R.id.addview);
		//addview.addView(btnPcba);
		//addview.addView(mView);

		text_top_zone.setText(mName);

		mContext.setContentView(mLayout);
		
		if (gsensor == null)
		{
			Log.v("qinhao", "gsensor:"+gsensor);
			isGensensor = true;
			text_cen_zone.setText("Gensensor is Brokean!!");
			mSecon = 2;
		}
	
		testCountDownTimer.start();
		

	}

	@Override
	public void finish() {
		// TODO Auto-generated method stub
		
	}

	@Override
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
		if (mSensorManager != null) {
			mSensorManager.unregisterListener(this, gsensor);
			mSensorManager=null;
		}
		
		if (mhandler != null) {
			mhandler.removeCallbacks(runnable);
		}
		if (testCountDownTimer != null) {
			testCountDownTimer.cancel();
		}
		isStop = false;
		isGensensor = false;
		isChangeStop = false;
		mSecon = 10;
	}

	@Override
	public void onFinish() {
		// TODO Auto-generated method stub
		Log.v("qinhao", "isGensensor:"+isGensensor);
		if (isChangeStop||isGensensor){mContext.setResult(Test.RESULT.FAILED.ordinal());
		Tool.toolLog(TAG + " index 888 -> " + ExecuteTest.temppositon);
		int double_test;
		if(AllMainActivity.mainAllTest){
			double_test = AllMainActivity.double_check_for_auto[ExecuteTest.temppositon]++;
			//AllMainActivity.mainAllTest = false;
		}else {
			double_test = autoRunActivity.double_check[ExecuteTest.temppositon]++;
		}
		Tool.toolLog(TAG + " double_test 999-> " + double_test);
		FinishThread finishThread=	new FinishThread(0x02,ExecuteTest.temppositon);
		finishThread.start();
		try {
            finishThread.join();
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
		if(double_test==1){
			//Add by Jianke.Zhang 02/02
			
			Msg.exitWithException(mContext,TAG,20,true,"Pass");
			
			//Write data into file 06/17 bug
			if(AllMainActivity.mainAllTest && !AllMainActivity.autofileFlag){
				Msg.WriteModelResult(mContext,AllMainActivity.all_items_file_text, "FAIL");
			}else{
				Msg.WriteModelResult(mContext,autoRunActivity.auto_file_text, "FAIL");
			}
			//End
		}else{
			Msg.exitWithException(mContext,TAG,20,true,"Fail");
			
		}
		}
	}

	@Override
	public void onTick() {
		// TODO Auto-generated method stub
		
	}

}
