package com.tcl.manucase;

import java.io.IOException;

import com.tcl.autotest.AllMainActivity;
import com.tcl.autotest.AutotestMainActivity;
import com.tcl.autotest.ExecuteManuTest;
import com.tcl.autotest.R;
import com.tcl.autotest.manuRunActivity;
import com.tcl.autotest.tool.Tool;
import com.tcl.autotest.utils.FinishThread;
import com.tcl.autotest.utils.Lcd;
import com.tcl.autotest.utils.ManuFinishThread;
import com.tcl.autotest.utils.Msg;
import com.tcl.autotest.utils.Test;
import com.tcl.autotest.utils.TestList;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.RectF;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

public class TouchPanel extends Test {
	
	private final static String TAG = "TouchPanelTest";
	private int tolerance = 100;
	private int righttolerance = 100;
	private int rightbottom = 100;
	int margin = 10;
	Lcd lcd = null;
	
    private int displayWidth = 0;
    private int displayHeight = 0;
    private int mWidth = 100;
    private int mWidth_other = 100;
    private static /*final*/ int MINXYP = 80;
    
    private final int TOUCH_PANEL_VERTICAL = 0;
    private final int TOUCH_PANEL_HORIZONTAL = 1;
    private final int TOUCH_PANEL_OTHER = 2;
    private int mState = TOUCH_PANEL_VERTICAL;
    public final static int NOT_TESTED = 0;
    public final static int PASSED = 1;
    public final static int FAILED = 2;
    private int Result;
    private float mLastY;
    private float mLastx;
    
    private Point[] hRect1;
    private Point[] hRect2;
    private Point[] hRect3;
    private Point[] vRect1;
    private Point[] vRect2;
    private Point[] vRect3;
    private Point[] vRect_other1;
    private Point[] vRect_other_2;
    
    private int mLineUnderTest = 0;
    
    private int mGoodLinesCount = 0;
    
    public int BackupLine1Pass = 0;
    public int BackupLine2Pass = 0;
    public int BackupLine3Pass = 0;
    public int BackupLine4Pass = 0;
    public int BackupLine5Pass = 0;
    
    private MyView mMyView;
    
	/*Parallelepipede vpl1 = null;
	Parallelepipede vpl2 = null;
	Parallelepipede tl1 = null;
	Parallelepipede tl2 = null;
	Parallelepipede tl3 = null;
	Parallelepipede tl4 = null;*/
	
	float mAverageX = 0;
	float mAverageY = 0;

	int mEcartType = 0;

	/*Jianke
	 * Ϊ�˻�ȡhome����Ӧ*/
	HomeReceiver homereceiver;
	IntentFilter intentfilter;
	
	String[] arrFive = {"Yaris35_GSM"};
	String[] arrSix = {""};
	String[] arrSeven = {"YARISXL","YARIS_55"};
	String[] arrEight = {"Yaris_M_GSM","SOUL4NA","Eclipse"};
	String[] arrFwel = {"idol3","HERO2","DIABLOXPLUS","DIABLOX","idol4s_vdf","idol4s"};
	
	private void CheckPdtName(int mxWidth,int mxWidth_other,String[] arrL){
		for(int i=0;i<arrL.length;i++){
			if(AllMainActivity.deviceName.equals(arrL[i])){
				mWidth = mxWidth;
				mWidth_other = mxWidth_other;
				break;
			}
		}
	}
	
	
	public TouchPanel(ID id, String name) {
		super(id, name);
		// TODO Auto-generated constructor stub
		CheckPdtName(50,50,arrFive);
		CheckPdtName(60,60,arrSix);
		CheckPdtName(70,70,arrSeven);
		CheckPdtName(80,80,arrEight);
		CheckPdtName(140,140,arrFwel);
	}

	private void buttonOption(){
		Intent touchPanel;
		Bundle bundle;
		boolean finish_bool = false;
		
		Tool.toolLog(TAG + " result: " + result);
		Log.d(TAG, "33333 result: " + result);
		
		if(result == Test.RESULT.PASS){
			//mContext.setResult(Test.RESULT.PASS.ordinal());
			//Add by Jianke.Zhang 2015/01/15 Send broadcast after finish
			switch(mState){
			case TOUCH_PANEL_VERTICAL:
	            mState = TOUCH_PANEL_HORIZONTAL;
	            reSet();
	            break;
			case TOUCH_PANEL_HORIZONTAL:
				mState = TOUCH_PANEL_OTHER;
	            reSet();
	            break;
			default:
				finish_bool = true;
	            break;
			}
			if(finish_bool){
				ManuFinishThread tfinish = new ManuFinishThread(0x01);
				tfinish.start();
				try {
					tfinish.join();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				Msg.exitWithSuccessTest(mContext, TAG, 10, false,"Pass");
				
				
				//Write result into file
				if(AllMainActivity.mainAllTest && !AllMainActivity.manufileFlag){
					Msg.WriteModelResult(mContext, AllMainActivity.all_items_file_text, "PASS");
				}else {
					Msg.WriteModelResult(mContext, manuRunActivity.filetxt, "PASS");
				}
				//End
			}
			
		} else if(result == Test.RESULT.FAILED) {
			//mContext.setResult(Test.RESULT.FAILED.ordinal());
			//Add by Jianke 01/20
			ManuFinishThread tfinish = new ManuFinishThread(0x02);
			tfinish.start();
			try {
				tfinish.join();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			Msg.exitWithException(mContext,TAG, 10,false,"Fail");
			
			//Write result into file
			if(AllMainActivity.mainAllTest && !AllMainActivity.manufileFlag){
				Msg.WriteModelResult(mContext, AllMainActivity.all_items_file_text, "FAIL");
			}else {
				Msg.WriteModelResult(mContext, manuRunActivity.filetxt, "FAIL");
			}
			//End
		} else if(result == Test.RESULT.NOT_TEST){
			//mLayout.removeAllViews();
			//mLayout.addView(new MyView(mContext));
			reSet();
		}
	}
	
	private int testLength(){
		
		return (int) (Math.sqrt(Math.pow(lcd.height(), 2) + Math.sqrt(Math.pow(lcd.width(), 2)))*0.8);
	}
	
	@Override
	public void setUp() {
		// TODO Auto-generated method stub
		Tool.toolLog(TAG + "_start_test");
		//tolerance = AllMainActivity.manuTP1Width;
		lcd = new Lcd(mContext);
		Log.d(TAG, "33333 _start_test");
//		homereceiver = new HomeReceiver();
//		intentfilter = new IntentFilter(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
//		mContext.registerReceiver(homereceiver, intentfilter);
		
//		Log.i(TAG,TAG + " setUp");
	}
	
	@Override
	public void initView() {
		// TODO Auto-generated method stub
		
//		Log.i(TAG,TAG + " initView");
//		int start_x1 = 0;
//		int start_x2 = lcd.width() - tolerance;
//		int start_y1 = 0;
//		int start_y2 = lcd.height() - tolerance;
		
		Point size = new Point();
        getDisplaySize(size);
        displayWidth = size.x;
        displayHeight = size.y;
        Tool.toolLog(TAG + " displayWidth " + displayWidth + " displayHeight " + displayHeight);
        
		Result = NOT_TESTED;
		
		hRect1 = new Point[]{ new Point(0, 0),
                new Point(displayWidth, 0),
                new Point(displayWidth, mWidth),
                new Point(0, mWidth), };

        hRect2 = new Point[]{
                new Point(0, (displayHeight - 3 * mWidth) / 4 * 2 + mWidth),
                new Point(displayWidth, (displayHeight - 3 * mWidth) / 4 * 2
                        + mWidth),
                new Point(displayWidth, (displayHeight - 3 * mWidth) / 4 * 2
                        + mWidth * 2),
                new Point(0, (displayHeight - 3 * mWidth) / 4 * 2 + mWidth * 2), };

        hRect3 = new Point[]{
                new Point(0, (displayHeight - mWidth)),
                new Point(displayWidth, (displayHeight - mWidth)),
                new Point(displayWidth, displayHeight),
                new Point(0,displayHeight), };
        
        vRect1 = new Point[]{ new Point((displayWidth - 3 * mWidth) / 4, 0),
                new Point((displayWidth - 3 * mWidth) / 4, displayHeight),
                new Point((displayWidth - 3 * mWidth) / 4 + mWidth, displayHeight),
                new Point((displayWidth - 3 * mWidth) / 4 + mWidth, 0), };

        vRect2 = new Point[]{
                new Point((displayWidth - 3 * mWidth) / 4 * 2 + mWidth, 0),
                new Point((displayWidth - 3 * mWidth) / 4 * 2 + mWidth,
                        displayHeight),
                new Point((displayWidth - 3 * mWidth) / 4 * 2 + mWidth * 2,
                        displayHeight),
                new Point((displayWidth - 3 * mWidth) / 4 * 2 + mWidth * 2, 0), };

        vRect3 = new Point[]{
                new Point((displayWidth - 3 * mWidth) / 4 * 3 + mWidth * 2, 0),
                new Point((displayWidth - 3 * mWidth) / 4 * 3 + mWidth * 2,
                        displayHeight),
                new Point((displayWidth - 3 * mWidth) / 4 * 3 + mWidth * 3,
                        displayHeight),
                new Point((displayWidth - 3 * mWidth) / 4 * 3 + mWidth * 3, 0), };
        
        vRect_other1 = new Point[]{ new Point(0, 0),
                new Point(displayWidth - mWidth_other, displayHeight),
                new Point(displayWidth, displayHeight),
                new Point(mWidth_other, 0), };

        vRect_other_2 = new Point[]{ new Point(displayWidth - mWidth_other, 0),
                new Point(0, displayHeight),
                new Point(mWidth_other, displayHeight),
                new Point(displayWidth, 0), };
        
        
		/*Point[] p1 = {
				new Point(start_x1,0),
				new Point(start_x1, lcd.height()-75 ),
				new Point(start_x1 + tolerance, lcd.height()-75 ),
				new Point(start_x1 + tolerance, 0) };

		Point[] p2 = {
				new Point(start_x2, 0),
				new Point(start_x2, lcd.height()-75 ),
				new Point(start_x2 + tolerance, lcd.height()-75 ),
				new Point(start_x2 + tolerance, 0) };
		
		Point[] v1 = {
				new Point(start_y1, 0),
				new Point(start_y1, tolerance),
				new Point(lcd.width(),start_y1 + tolerance),
				new Point(lcd.width(), 0) };

		Point[] v2 = {
				new Point(0,start_y2-85),
				new Point(start_x1, lcd.height()-75),
				new Point(start_x2 + tolerance, lcd.height()-75),
				new Point(lcd.width(),start_y2-85) };
		
		Point[] vp1 = {
				//Jianke 08/10
				new Point(0, tolerance),
				new Point(start_x2+100, lcd.height()),
				new Point(lcd.width(),start_y2-150),
				new Point(tolerance, 0) };

		Point[] vp2 = {
				new Point(0,start_y2-300),
				new Point(tolerance, lcd.height()-300),
				new Point(lcd.width()-100, tolerance),
				new Point(start_x2-100,0) };
		
		Point[] t1 = {
				new Point(0,0),
				new Point(0, tolerance),
				new Point(tolerance, 0) };
		
		Point[] t2 = {
				new Point(lcd.width()-tolerance-100, 0),
				new Point(lcd.width()-100, 0),
				new Point(lcd.width()-100, tolerance) };

		Point[] t3 = {
				new Point(0,lcd.height()-300),
				new Point(0, lcd.height() - tolerance -300),
				new Point(tolerance, lcd.height()-300) };
		
		Point[] t4 = {
				new Point(lcd.width(), lcd.height()),
				new Point(lcd.width() - tolerance, lcd.height()),
				new Point(lcd.width(), lcd.height() - tolerance) };
		
		vpl1 = new Parallelepipede(vp1);
		vpl2 = new Parallelepipede(vp2);
		tl1 = new Parallelepipede(t1);
		tl2 = new Parallelepipede(t2);
		tl3 = new Parallelepipede(t3);
		tl4 = new Parallelepipede(t4);*/
		
		mLayout = new LinearLayout(mContext);
		mMyView = new MyView(mContext);
		mLayout.addView(mMyView);

		mContext.setContentView(mLayout);
		hideNavigationBar();
		/*vpl1.setFinish(false);
		vpl2.setFinish(false);
		tl1.setFinish(false);
		tl2.setFinish(false);
		tl3.setFinish(false);
		tl4.setFinish(false);
*/
	}
	
	@Override
	public void destroy() {
		// TODO Auto-generated method stub
		result = Test.RESULT.NOT_TEST;
		mLayout.removeAllViews();
		
//		mContext.unregisterReceiver(homereceiver);
	}
	
	public void reSet() {
        //PR981886 begin
        if(Result == FAILED)
        {
            BackupLine1Pass = mMyView.mLine1Pass;
            BackupLine2Pass = mMyView.mLine2Pass;
            BackupLine3Pass = mMyView.mLine3Pass;
            BackupLine4Pass = mMyView.mLine4Pass;
            BackupLine5Pass = mMyView.mLine5Pass;
        }
        //PR981886 end

        Result = NOT_TESTED;
        if (mMyView != null) {
          mMyView.recycleBitmap();
        }
        mLayout = new LinearLayout(mContext);
        mMyView = new MyView(mContext);
        mLayout.addView(mMyView);
        mContext.setContentView(mLayout);

        //PR981886 begin
        mMyView.mLine1Pass = BackupLine1Pass;
        mMyView.mLine2Pass = BackupLine2Pass;
        mMyView.mLine3Pass = BackupLine3Pass;
        mMyView.mLine4Pass = BackupLine4Pass;
        mMyView.mLine5Pass = BackupLine5Pass ;

        BackupLine1Pass = 0;
        BackupLine2Pass = 0;
        BackupLine3Pass = 0;
        BackupLine4Pass = 0;
        BackupLine5Pass = 0;
        //PR981886 end
    }
	
	public class MyView extends View {

		private Bitmap mBitmap;
		private Canvas mCanvas;
		private Path mPath;
		private Paint mBitmapPaint;
		private Paint mPaint;
		private AlertDialog mAlertDialog, mAlertDialogMsg, mAlertDialogEnd, mAlertDialogOK;

		private Parallelepipede pl1;
        private Parallelepipede pl2;
        private Parallelepipede pl3;

        private Parallelepipede pl4;
        private Parallelepipede pl5;
        
        private int mLine1Pass = 0;
        private int mLine2Pass = 0;
        private int mLine3Pass = 0;
        private int mLine4Pass = 0;
        private int mLine5Pass = 0;
        
        private int mMinLen = 0;
        
		public MyView(Context c) {
			super(c);
			mPaint = new Paint();
			mPaint.setAntiAlias(true);

			mPaint.setColor(0xFFFF0000);
			mPaint.setStyle(Paint.Style.STROKE);
			mPaint.setStrokeJoin(Paint.Join.BEVEL);
			mPaint.setStrokeCap(Paint.Cap.ROUND);
			mPaint.setStrokeWidth(1);
			mBitmap = Bitmap.createBitmap(lcd.width(), lcd.height(),
					Bitmap.Config.ARGB_8888);
			mCanvas = new Canvas(mBitmap);
			mPath = new Path();
			mBitmapPaint = new Paint(Paint.DITHER_FLAG);

			switch (mState) {
            case TOUCH_PANEL_VERTICAL:
                pl1 = new Parallelepipede(vRect1);
                pl2 = new Parallelepipede(vRect2);
                pl3 = new Parallelepipede(vRect3);
                mMinLen = displayHeight - 142;
                Tool.toolLog(TAG + " mMinLen " + mMinLen);
                break;
            case TOUCH_PANEL_HORIZONTAL:
                pl1 = new Parallelepipede(hRect1);
                pl2 = new Parallelepipede(hRect2);
                pl3 = new Parallelepipede(hRect3);
                mMinLen = displayWidth - 82;
                break;
            case TOUCH_PANEL_OTHER:
                pl4 = new Parallelepipede(vRect_other1);
                pl5 = new Parallelepipede(vRect_other_2);
                mMinLen = (int) Math.sqrt(displayHeight
                        *displayHeight + (displayWidth-mWidth_other)*(displayWidth-mWidth_other));
                mMinLen = mMinLen - 92;
                Tool.toolLog(TAG + " TOUCH_PANEL_OTHER " + mMinLen);
			default:
				break;
			}
			
			mAlertDialogEnd = new AlertDialog.Builder(mContext)
					.setTitle("TEST RESULT")
					.setMessage("Pen out of bounds!")

					.setPositiveButton("Reset",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int whichButton) {
									result = Test.RESULT.NOT_TEST;
									buttonOption();
								}
							})
					.setNegativeButton("FAIL",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int whichButton) {
								result = Test.RESULT.FAILED;
								buttonOption();
							}
						}).create();

			mAlertDialogOK = new AlertDialog.Builder(mContext)
					.setTitle("TEST RESULT")
					.setMessage("OK!")
					.setPositiveButton("PASS",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int whichButton) {
									result = Test.RESULT.PASS;
									buttonOption();
								}
							})
					.setNegativeButton("FAIL",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int whichButton) {
									result = Test.RESULT.FAILED;
									buttonOption();
								}
							}).create();

			mAlertDialogMsg = new AlertDialog.Builder(mContext)
					.setTitle("TEST RESULT")
					.setMessage("line too short!")
					.setNeutralButton("OK",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int whichButton) {
									//reSet();
								}
							})
					.create();
		}

		@Override
		protected void onDraw(Canvas canvas) {
//			Log.i(TAG,TAG + " onDraw");
			canvas.drawColor(Color.BLACK);

			canvas.drawBitmap(mBitmap, 0, 0, mBitmapPaint);

			switch (mState) {
            case TOUCH_PANEL_VERTICAL:
                pl1.draw(canvas);
                pl2.draw(canvas);
                pl3.draw(canvas);
                break;
            case TOUCH_PANEL_HORIZONTAL:
                pl1.draw(canvas);
                pl2.draw(canvas);
                pl3.draw(canvas);
                break;
            case TOUCH_PANEL_OTHER:
                pl4.draw(canvas);
                pl5.draw(canvas);
                break;

            default:
                break;
            }
			
			/* draw 2 parallelepipede on the screen */
			/*vpl1.draw(canvas);
			vpl2.draw(canvas);
			tl1.draw(canvas);
			tl2.draw(canvas);
			tl3.draw(canvas);
			tl4.draw(canvas);*/

			/* draw references lines on the screen */
			Paint p = new Paint();
			p.setColor(Color.WHITE);
			p.setStyle(Paint.Style.STROKE);
			p.setTextSize(20);

			//canvas.drawText("Please draw on the yellow area", lcd.width() / 2 - 200, 100, p);
			//canvas.drawText("", lcd.width() / 2 - 200, 195, p);

			// footer text
/*			canvas.drawText((result == Test.RESULT.FAILED ? "FAILED" : ""),
					lcd.width() / 2 - 20, lcd.height() - 25, p);
*/
			/* draw the current pen position */
//			Tool.toolLog(TAG + " onDraw");
			canvas.drawPath(mPath, mPaint);

		}

		private float mX, mY;
		private static final float TOUCH_TOLERANCE = 1;

		private void touch_start(float x, float y) {
			mPath.reset();
			mPath.moveTo(x, y);
			mX = x;
			mY = y;
		}

		private void touch_move(float x, float y) {
			float dx = Math.abs(x - mX);
			float dy = Math.abs(y - mY);
			if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
				mPath.quadTo(mX, mY, (x + mX) / 2, (y + mY) / 2);
				mX = x;
				mY = y;
			}
		}

		private void touch_up() {
			mPath.lineTo(mX, mY);
			mCanvas.drawPath(mPath, mPaint);
		}

		@Override
		public boolean onTouchEvent(MotionEvent event) {
			float x = event.getX();
			float y = event.getY();

			
			if (y < 0 ) {
                y = 0;
            } else if (y > displayHeight) {
                y = displayHeight;
            } else if (x < 0) {
                x = 0;
            } else if (x > displayWidth) {
                x = displayWidth;
            }
			
			// check if the point is inside the bounds drawn on the screen
			//Tool.toolLog(TAG + " x " + x + " y " + y);
			if (true)/*(x>0 && x<displayWidth && y>0 && y<displayHeight)*/ {
                switch (mState) {
                case TOUCH_PANEL_VERTICAL:
                case TOUCH_PANEL_HORIZONTAL:
                    if (pl1.includePoint(x, y)) {
                    	//Tool.toolLog(TAG + "ppppp1111111");
                        mLineUnderTest = 1;
                    } else if (pl2.includePoint(x, y)) {
                    	//Tool.toolLog(TAG + "ppppp2222222");
                        mLineUnderTest = 2;
                    } else if (pl3.includePoint(x, y)) {
                    	//Tool.toolLog(TAG + "ppppp33333333");
                        mLineUnderTest = 3;
                    } else {
                    	Tool.toolLog(TAG + " out of the inner!");
                        Result = FAILED;
                    }
                    break;
                case TOUCH_PANEL_OTHER:
                    if (pl4 != null && pl5 != null) {
                        if (pl4.includePoint(x, y)) {
                            mLineUnderTest = 4;
                        } else if (pl5.includePoint(x, y)) {
                            mLineUnderTest = 5;
                        } else {
                            Result = FAILED;
                        }
                    }
                    break;
                default:
                    break;
                }
            }
			/*if( !vpl1.includePoint(x, y) && !vpl2.includePointv(x, y)
					&& !tl1.includePointTriangle(x, y) && !tl2.includePointTriangle(x, y)
					&& !tl3.includePointTriangle(x, y) && !tl4.includePointTriangle(x, y) ) {
				Test.result = Test.RESULT.FAILED;
			}*/

//			Log.d(TAG, "x = " + x + " y = " + y);

			switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN:
//				Tool.toolLog(TAG + " ACTION_DOWN");
				touch_start(x, y);
				invalidate();
				mAverageX = x;
				mAverageY = y;
				//Tool.toolLog(TAG + " xxxxxxxxxxxxxxxxxxxxx");
				mLastY = y;
                mLastx = x;
				break;
			case MotionEvent.ACTION_MOVE:
//				Tool.toolLog(TAG + " ACTION_MOVE");
				if ((Math.abs(y-mLastY) > MINXYP || Math.abs(x-mLastx) > MINXYP)
                        && mAlertDialogMsg != null){
					mAlertDialog = mAlertDialogMsg;
					mAlertDialog.show();
					break;
				}
				touch_move(x, y);
				invalidate();
				mAverageX = (x + mAverageX) / 2;
				mAverageY = (y + mAverageY) / 2;
				//Tool.toolLog(TAG + " yyyyyyyyyyyyyyy");
				mLastY = y;
                mLastx = x;
				break;
			case MotionEvent.ACTION_UP:
				Tool.toolLog(TAG + " ACTION_UP");
				touch_up();
				invalidate();

//				Log.d(TAG, "AVERAGES : x = " + mAverageX + " y = " + mAverageY);

				/* check the length of the path */
				//Tool.toolLog(TAG + " zzzzzzzzzzzzzzzzz");
				RectF rect = new RectF(0, 0, 0, 0);
				mPath.computeBounds(rect, true);
				float mPathLength = (float) Math.sqrt(rect.height()
						* rect.height() + rect.width() * rect.width());

				Tool.toolLog(TAG + " path length is " + mPathLength);

				mAlertDialog = mAlertDialogEnd;

				/*if (Test.result == Test.RESULT.FAILED) {
					mAlertDialog.setMessage("Pen out of bounds!");
				} else if (mPathLength < testLength()) {
					mAlertDialog = mAlertDialogMsg;
				} else {
					if (vpl1.includePoint(x, y) || tl1.includePointTriangle(x, y) || tl4.includePointTriangle(x, y)) {
						vpl1.setFinish(true);
					}
					if (vpl2.includePointv(x, y) || tl2.includePointTriangle(x, y) || tl3.includePointTriangle(x, y)) {
						vpl2.setFinish(true);
					}
					if (vpl1.getFinish() && vpl2.getFinish()) {
						mAlertDialog = mAlertDialogOK;
					} else {
						mAlertDialog = null;
					}
				}

				Test.result = Test.RESULT.NOT_TEST;

				if (mAlertDialog == null) {

				} else if (!mAlertDialog.isShowing()) {
						mAlertDialog.show();
				}*/
				Tool.toolLog(TAG + " touch up ...");
				if (Result == FAILED) {
					//Tool.toolLog(TAG + " fail 1111111111");
                    mAlertDialog = mAlertDialogEnd;
                    if(mAlertDialog != null && !mAlertDialog.isShowing()){
                    	mAlertDialog.show();
                    }
                } else if (mPathLength < mMinLen) {
                	//Tool.toolLog(TAG + " short 222222222");
                    mAlertDialog = mAlertDialogMsg;
                    if(mAlertDialog != null && !mAlertDialog.isShowing()){
                    	mAlertDialog.show();
                    }
                } else {
                	Tool.toolLog(TAG + " mState ----------- " + mState);
                    switch (mState) {
                    case TOUCH_PANEL_VERTICAL:
                    case TOUCH_PANEL_HORIZONTAL:
                    	Tool.toolLog(TAG + "goooooooooooooooooooooo");
                        if (1 == mLineUnderTest) {
                            mLine1Pass = 1;
                        } else if (2 == mLineUnderTest) {
                            mLine2Pass = 1;
                        } else if (3 == mLineUnderTest) {
                            mLine3Pass = 1;
                        }
                        mGoodLinesCount = mLine1Pass + mLine2Pass + mLine3Pass;
                        Tool.toolLog(TAG + " mGoodLinesCount " + mGoodLinesCount);
                        
                        if (3 == mGoodLinesCount) {
                        	//Tool.toolLog(TAG + " 3333333333333");
                            mAlertDialog=null;
                            //onPassClick();
                            mAlertDialog = mAlertDialogOK;
                            if(mAlertDialog != null && !mAlertDialog.isShowing()){
                            	mAlertDialog.show();
                            }
                        } else {
                            mAlertDialog = null;
                        }
                        break;
                    case TOUCH_PANEL_OTHER:
                        if (4 == mLineUnderTest) {
                            mLine4Pass = 1;
                        } else if (5 == mLineUnderTest) {
                            mLine5Pass = 1;
                        }
                        mGoodLinesCount = mLine4Pass + mLine5Pass;

                        if (2 == mGoodLinesCount) {
                            mAlertDialog = null;
                            //onPassClick();
                            mAlertDialog = mAlertDialogOK;
                        } else {
                            mAlertDialog = null;
                        }
                        
                        if(mAlertDialog != null && !mAlertDialog.isShowing()){
                        	mAlertDialog.show();
                        }
                        
                        break;

                    default:
                        break;
                    }
                    
                    
                }
				break;
			}
			
			return true;
		}

		public void recycleBitmap() {
	          if (mBitmap != null) {
	              mBitmap.recycle();
	              mBitmap = null;
	            }
	        }
	}
	
	class Parallelepipede {
		private Path mPath;
		private Paint mPaint;
		private Point[] points;
		boolean isFinished = false;
		
		Parallelepipede(Point[] p) {
			points = p.clone();
			mPath = new Path();

			mPaint = new Paint();
			mPaint.setAntiAlias(true);
			mPaint.setColor(Color.YELLOW);
			mPaint.setStyle(Paint.Style.FILL);
			mPaint.setStrokeJoin(Paint.Join.BEVEL);
			mPaint.setStrokeCap(Paint.Cap.ROUND);
			mPaint.setStrokeWidth(1);
		}
		
		void setFinish(boolean b) {
			isFinished = b;
		}
		
		boolean getFinish() {
			return isFinished;
		}

		void draw(Canvas c) {
			mPath.reset();
			mPath.moveTo(points[0].x, points[0].y);
			for (int i = 1; i < points.length; i++) {
				mPath.lineTo(points[i].x, points[i].y);
			}
			mPath.close();
			c.drawPath(mPath, mPaint);
//			Tool.toolLog(TAG + " Parallelepipede");
		}
		
	    private int crossProduct(Point p1, Point p2)
	    {
	        return (p1.x * p2.y - p1.y * p2.x);
	    }

	    public boolean includePointTriangle(float x, float y) {
	    	Point p = new Point((int) x, (int) y);
	    	
	        int u1 = crossProduct( new Point(points[1].x - points[0].x, points[1].y - points[0].y),
	                             new Point(p.x - points[0].x, p.y - points[0].y));
	        int u2 = crossProduct( new Point(points[2].x - points[1].x, points[2].y - points[1].y),
	                             new Point(p.x - points[1].x, p.y - points[1].y));
	        int u3 = crossProduct( new Point(points[0].x - points[2].x, points[0].y - points[2].y),
	                             new Point(p.x - points[2].x, p.y - points[2].y));

	        if ( (u1 > 0 && u2 > 0 && u3 > 0) || (u1 < 0 && u2 < 0 && u3 < 0) )
	        {
	            /* point is inside the triangle */
	            return true;
	        }
	        else
	        {
	            return false;
	        }
	    }

		/*
		 * checks if the point (x,y) is included in the Parallelepipede
		 */

		public boolean includePoint(float x, float y) {
			Point p = new Point((int) x, (int) y);
			double d1 = distLineToPoint(points[0], points[1], p);
			double d2 = distLineToPoint(points[2], points[3], p);
			double range = distLineToPoint(points[0], points[1], points[2]);
			
			/*
			 * to be included in the shape, the distance from (x,y) to the
			 * bottom or top line should not exceed the distance between the
			 * bottom to top line
			 */
			if (Math.max(d1, d2) < range) {
				//Tool.toolLog(TAG + " ============== ");
				return true;
				
			}

			return false;
		}
		public boolean includePointv(float x, float y) {
			Point p = new Point((int) x, (int) y);
			double d1 = distLineToPoint(points[0], points[3], p);
			double d2 = distLineToPoint(points[1], points[2], p);
			double range = distLineToPoint(points[1], points[2], points[3]);
			/*
			 * to be included in the shape, the distance from (x,y) to the
			 * bottom or top line should not exceed the distance between the
			 * bottom to top line
			 */

			if (Math.max(d1, d2) < range) {
				return true;
				
			}
			
			return false;
		}
		/* computes the shortest distance form a point to a line */
		/*                                                       
		 * 
		 */
		private double distLineToPoint(Point A, Point B, Point p) {

			/*
			 * let [AB] be the segment and C the projection of C on (AB) AC * AB
			 * (Cx-Ax)(Bx-Ax) + (Cy-Ay)(By-Ay) u = ------- =
			 * ------------------------------- ||AB||^2 ||AB||^2
			 */
			double det = Math.pow(B.x - A.x, 2) + Math.pow(B.y - A.y, 2);
			if (det == 0) {
				return 0;
			}

			double u = ((p.x - A.x) * (B.x - A.x) + (p.y - A.y) * (B.y - A.y))
					/ det;

			/*
			 * The projection point P can then be found:
			 * 
			 * Px = Ax + r(Bx-Ax) Py = Ay + r(By-Ay)
			 */
			double Px = A.x + u * (B.x - A.x);
			double Py = A.y + u * (B.y - A.y);

			// Log.d(TAG,"distLineToPoint : u="+u+" Px=" +Px +" Py=" +Py);

			/* the distance to (AB) is the the [Pp] segment length */

			double distance = Math.sqrt(Math.pow(Px - p.x, 2)
					+ Math.pow(Py - p.y, 2));

			return distance;
		}

	}

	public String getmContextTag() {
		// TODO Auto-generated method stub
		return TAG;
	}

	protected void getDisplaySize(Point size) {
        mContext.getWindowManager().getDefaultDisplay().getSize(size);
    }

	@Override
	public void setmContextTag() {
		// TODO Auto-generated method stub
		
	}
	
	/*private AlertDialog mAlertDialogKeyMsg = new AlertDialog.Builder(mContext)
	.setMessage("don not touch virtual key!")
	.setNeutralButton("OK",
	new DialogInterface.OnClickListener() {
		public void onClick(DialogInterface dialog,
				int whichButton) {

			}
		})
	.create();*/
	
	private Handler mHandler = new Handler() {

		
		@Override
		public void dispatchMessage(Message msg) {
			// TODO Auto-generated method stub
			Tool.toolLog(TAG + " msg.what " + msg.what);
			Log.d(TAG, "33333 msg.what " + msg.what);
			switch (msg.what) {
			case KeyEvent.KEYCODE_VOLUME_UP:
				break;
			case KeyEvent.KEYCODE_VOLUME_DOWN:
				break;
			case KeyEvent.KEYCODE_BACK:
				Tool.toolLog(TAG + " KEYCODE_BACK");
				//mAlertDialogKeyMsg.show();
				break;
			case KeyEvent.KEYCODE_HOME:
				Tool.toolLog(TAG + " KEYCODE_HOME");
				Toast.makeText(mContext, "don not touch home key", Toast.LENGTH_LONG).show();
				//mAlertDialogKeyMsg.show();
				break;
			case KeyEvent.KEYCODE_MENU:
				Tool.toolLog(TAG + " KEYCODE_MENU");
				//mAlertDialogKeyMsg.show();
				break;
			case KeyEvent.KEYCODE_POWER:
				Tool.toolLog(TAG + " KEYCODE_POWER");
				//mAlertDialogKeyMsg.show();
				break;
			}

			//super.handleMessage(msg);
			
		}
		
	};
	
	public class HomeReceiver extends BroadcastReceiver {

		@SuppressWarnings("deprecation")
		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			String action = intent.getAction();
			if (action.equals(Intent.ACTION_CLOSE_SYSTEM_DIALOGS)) {
				String reason = intent.getStringExtra("reason");
				if (reason != null) {
					Message msg = new Message();
					if (reason.equals("homekey")) {
						//lock.disableKeyguard();
						Tool.toolLog(TAG + " homekey");
						msg.what = 0x3;
						mHandler.sendMessage(msg);
						
					} else if (reason.equals("recentapps")) {
						//lock.disableKeyguard();
						
						Tool.toolLog(TAG + " recentapps");
						msg.what = 0x52;
						mHandler.sendMessage(msg);
						
					}
				}
			}
		}
	}

	@Override
	public void finish() {
		// TODO Auto-generated method stub
		
	}
}

	