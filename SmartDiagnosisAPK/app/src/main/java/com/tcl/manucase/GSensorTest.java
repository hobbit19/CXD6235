package com.tcl.manucase;


import com.tcl.autotest.AllMainActivity;
import com.tcl.autotest.R;
import com.tcl.autotest.tool.Tool;
import com.tcl.autotest.utils.Lcd;
import com.tcl.autotest.utils.Test;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class GSensorTest extends Test implements SensorEventListener {

	private static String TAG = "GSensorTest";
	
	float mAngleToYaxis;
	float mAngleToXaxis;
	float mAngleToZaxis;
	private View mView;
	private Button btnPcba;
	private boolean init;
	private boolean PCBA = false;
	LinearLayout addview;

	private boolean isPixi34 = false;
	
	private enum position {
		UNDEF, UP, DOWN, LEFT, RIGHT, FACE_UP, FACE_DOWN
	};

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

	private Sensor gsensor;

	Bitmap mBitmap;
	public GSensorTest(ID id, String name) {
		super(id, name);
	}

	private void setPCBAButton() {
		btnPcba = new Button(mContext);
		btnPcba.setText("PCBA");
		btnPcba.setPadding(5, 10, 5, 10);
		btnPcba.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				PCBA = true;
				new AlertDialog.Builder(mContext)
						.setTitle("PCBA")
						.setMessage(
								"x = " + Float.toString(mAngleToXaxis) + '\n'
										+ "y = "
										+ Float.toString(mAngleToYaxis) + '\n'
										+ "z = "
										+ Float.toString(mAngleToZaxis) + '\n'
										+ "G-Sensor test OK!")
						.setPositiveButton("PASS",
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int which) {
										mContext.setResult(Test.RESULT.PASS
												.ordinal());
										mContext.finish();
									}
								})
						.setNegativeButton("FAIL",
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int which) {
										mContext.setResult(Test.RESULT.FAILED
												.ordinal());
										mContext.finish();
									}
								}).show();
			}
		});
	}

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
		}
		mPositionChecked = POS_BIT_ALL & ~POS_BIT_UP;
		gsensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
//		Tool.toolLog(TAG + " gsensor " + gsensor);
		
		if (gsensor != null) {
			Tool.toolLog(TAG + " GSensor opened : " + gsensor.getName());
			if (!mSensorManager.registerListener(this, gsensor,
					SensorManager.SENSOR_DELAY_NORMAL)) {
				Tool.toolLog(TAG + " register listener for sensor " + gsensor.getName()
						+ " failed");
			}
		}
		mView = new PositionView(mContext);
		mView.setPadding(5, 25, 5, 10);
//		setPCBAButton();

	}

	@Override
	public void initView() {
		// TODO Auto-generated method stub
		mLayout = (LinearLayout) View.inflate(mContext,
				R.layout.base_screen_view, null);

		bt_left = (Button) mLayout.findViewById(R.id.bt_left);
		bt_right = (Button) mLayout.findViewById(R.id.bt_right);
		text_top_zone = (TextView) mLayout.findViewById(R.id.text_top_zone);

		addview = (LinearLayout) mLayout.findViewById(R.id.addview);
		//addview.addView(btnPcba);
		addview.addView(mView);

		text_top_zone.setText(mName);
		// PR661395-yinbin-zhang-20140516 begin
		/*
		 * bt_left.setText(R.string.fail); bt_right.setText(R.string.pass);
		 */
		bt_left.setText(R.string.pass);
		bt_right.setText(R.string.fail);
		// PR661395-yinbin-zhang-20140516 end
		bt_left.setEnabled(false);
		bt_right.setEnabled(true);
		bt_right.setOnClickListener(failed_listener);
		
		mContext.setContentView(mLayout);

	}

	@Override
	public void destroy() {
		// TODO Auto-generated method stub
		if (mSensorManager != null) {
			mSensorManager.unregisterListener(this, gsensor);
			mSensorManager=null;
		}
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		// TODO Auto-generated method stub
		position mOldPosition = mPosition;

//		Tool.toolLog(TAG + " onSensorChanged: (" + event.values[0] + ", "
//				+ event.values[1] + ", " + event.values[2] + ")");

		DDDVect mDir = new DDDVect(event.values[0], event.values[1],
				event.values[2]);

		mAngleToYaxis = mDir.getYAngle();
		mAngleToXaxis = mDir.getXAngle();
		mAngleToZaxis = mDir.getZAngle();

//		Tool.toolLog(TAG + " mAngleToZaxis " + mAngleToZaxis);
		
		if(isPixi34){
			if ( mAngleToYaxis < 35 ){
				mPosition = position.UP;
				mPositionChecked |= POS_BIT_UP;
			}else if ( mAngleToYaxis >= 145 ){
				mPosition = position.DOWN;
				mPositionChecked |= POS_BIT_DOWN;
			}else if ( mAngleToXaxis < 35 ){
				mPosition = position.LEFT;
				mPositionChecked |= POS_BIT_LEFT;
			}else if ( mAngleToXaxis >= 145 ){
				mPosition = position.RIGHT;
				mPositionChecked |= POS_BIT_RIGHT;
			}else if (mAngleToZaxis > 60) {
				mPosition = position.FACE_DOWN;
				mPositionChecked |= POS_BIT_FACE_DOWN;
			} else if (mAngleToZaxis < 20) {
				mPosition = position.FACE_UP;
				mPositionChecked |= POS_BIT_FACE_UP;
			}else{
				mPosition = position.UNDEF;
			}
		}else{
			if (mAngleToYaxis < 10) {
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
			}
		}

		if (mOldPosition != mPosition && !PCBA) {
//			Tool.toolLog(TAG +  " mPositionChecked  ====================  "
//					+ mPositionChecked);
//			Tool.toolLog(TAG + " handest position changed :" + mPosition
//					+ " (checked = " + mPositionChecked + ")");
//			Tool.toolLog(TAG + " V : " + mAngleToYaxis + " deg , H : " + mAngleToXaxis
//					+ " deg");
			if (!init) {
				mPositionChecked = POS_BIT_UP;
				init = true;
			}
			if (mPositionChecked != POS_BIT_ALL) {
				mView = new PositionView(mContext);
				addview.setPadding(0, 50, 0, 50);
				addview.removeAllViews();
				addview.addView(mView);
			} else{
				destroy();
				mLayout = (LinearLayout) View.inflate(mContext,
						R.layout.manu_base_screen, null);

				bt_left = (Button) mLayout.findViewById(R.id.bt_left);
				bt_right = (Button) mLayout.findViewById(R.id.bt_right);
				text_top_zone = (TextView) mLayout
						.findViewById(R.id.text_top_zone);
				text_cen_zone = (TextView) mLayout
						.findViewById(R.id.text_cen_zone);

				text_top_zone.setText(mName);
				text_cen_zone.setText("All positions checked!");
				// PR661395-yinbin-zhang-20140516 begin
				
				/* bt_left.setText(R.string.fail);
				 * bt_right.setText(R.string.pass);
				 * 
				 * bt_left.setOnClickListener(failed_listener);
				 * bt_right.setOnClickListener(pass_listener);
				 */
				 
				bt_left.setText(R.string.pass);
				bt_right.setText(R.string.fail);
				bt_left.setEnabled(true);
				bt_right.setEnabled(false);
				
				bt_left.setOnClickListener(pass_listener);
				bt_right.setOnClickListener(failed_listener);
				// PR661395-yinbin-zhang-20140516 begin

				mContext.setContentView(mLayout);
			}
		}

//		Tool.toolLog(TAG + " V : " + mAngleToYaxis + " deg , H : " + mAngleToXaxis
//				+ " deg");
	}

	private class PositionView extends View {
		private Lcd lcd = new Lcd(mContext);;
		private Paint mPaint = new Paint();

		public PositionView(Context context) {
			super(context);
			mBitmap = android.graphics.BitmapFactory.decodeResource(
					getResources(), R.drawable.phone);
		}

		@Override
		protected void onDraw(Canvas canvas) {
			Paint paint = mPaint;

			canvas.drawColor(Color.WHITE);
			int length = 100, offset = 60, border = 10;

			paint.setAntiAlias(true);
			paint.setColor(Color.BLACK);
			paint.setStyle(Paint.Style.FILL);
			paint.setStrokeWidth(4);

			int w = canvas.getWidth();
			int h = canvas.getHeight();
			int cx = w / 2;
			int cy = h / 2 - 40;

			canvas.translate(cx, cy);

			/* UP LINE */
			if ((mPositionChecked & POS_BIT_UP) == 0 && !init) {
				canvas.drawLine(0, -offset, 0, -(length + offset), paint);
				canvas.drawLine(-border, -(offset + length - border), 0,
						-(length + offset), paint);
				canvas.drawLine(border, -(offset + length - border), 0,
						-(length + offset), paint);
				Matrix m = new Matrix();
				m.setTranslate(10, -length);
				paint.setColor(Color.BLUE);
				canvas.drawBitmap(mBitmap, m, paint);
				canvas.drawText("put the handset UP and towards up",
						-lcd.width() / 2 + border, 0, paint);
			}

			/* wtchen: Face up testing */
			if ((mPositionChecked & POS_BIT_FACE_UP) == 0) {
				canvas.drawLine(-50, -offset * 2, -50,
						-(length / 2 + offset * 2), paint);
				canvas.drawLine(-50 - border,
						-(offset * 2 + length / 2 - border), -50,
						-(offset * 2 + length / 2), paint);
				canvas.drawLine(-50 + border,
						-(offset * 2 + length / 2 - border), -50,
						-(offset * 2 + length / 2), paint);
				canvas.drawText("face up", -50 + border, -offset * 2 - border,
						paint);
			}

			if ((mPositionChecked & POS_BIT_FACE_DOWN) == 0) {
				canvas.drawLine(50, -offset * 2, 50,
						-(length / 2 + offset * 2), paint);
				canvas.drawLine(50 - border, -(offset * 2 + border), 50,
						-(offset * 2), paint);
				canvas.drawLine(50 + border, -(offset * 2 + border), 50,
						-(offset * 2), paint);
				canvas.drawText("face down", 50 + border, -offset * 2 - border,
						paint);
			}

			/* DOWN LINE */
			if ((mPositionChecked & POS_BIT_DOWN) == 0) {
				Matrix m = new Matrix();
				m.setTranslate(10, -length);
				m.postRotate(180);
				canvas.drawLine(0, offset, 0, length + offset, paint);
				canvas.drawLine(-border, (offset + length - border), 0, length
						+ offset, paint);
				canvas.drawLine(border, (offset + length - border), 0, length
						+ offset, paint);
				canvas.drawText("down", 10, length, paint);
				canvas.drawBitmap(mBitmap, m, null);

			}

			/* LEFT LINE */
			if ((mPositionChecked & POS_BIT_LEFT) == 0) {
				Matrix m = new Matrix();
				m.setTranslate(border, -length);
				m.postRotate(270);

				canvas.drawBitmap(mBitmap, m, paint);

				canvas.drawLine(-offset, 0, -(length + offset), 0, paint);
				canvas.drawLine(-(offset + length - border), -border,
						-(length + offset), 0, paint);
				canvas.drawLine(-(offset + length - border), border,
						-(length + offset), 0, paint);
				canvas.drawText("left", -length, 15, paint);
				canvas.drawBitmap(mBitmap, m, null);

			}

			/* RIGHT LINE */
			if ((mPositionChecked & POS_BIT_RIGHT) == 0) {

				Matrix m = new Matrix();
				m.setTranslate(-mBitmap.getWidth() - border, -length);
				m.postRotate(90);

				canvas.drawBitmap(mBitmap, m, paint);

				canvas.drawLine(offset, 0, (length + offset), 0, paint);
				canvas.drawLine((offset + length - border), -border,
						(length + offset), 0, paint);
				canvas.drawLine((offset + length - border), border,
						(length + offset), 0, paint);
				canvas.drawText("right", length, 15, paint);
			}

		}

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
