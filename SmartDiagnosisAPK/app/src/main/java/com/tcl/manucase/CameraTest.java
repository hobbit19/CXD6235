package com.tcl.manucase;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.hardware.ICameraService;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.*;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.tcl.autotest.AllMainActivity;
import com.tcl.autotest.R;
import com.tcl.autotest.tool.Tool;
import com.tcl.autotest.utils.DownTimeCallBack;
import com.tcl.autotest.utils.ManuFinishThread;
import com.tcl.autotest.utils.Msg;
import com.tcl.autotest.utils.Test;
import com.tcl.autotest.utils.TestCountDownTimer;

import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;

public class CameraTest extends Test implements DownTimeCallBack{
	private static final String TAG = "CameraTest";
	FrameLayout fl;
	public Preview mPreview;
	View focusIndicatorLayout;
	int mZoomValue = 0;
	private final int WC = ViewGroup.LayoutParams.WRAP_CONTENT;
	int cameraId = 0;
	private static Activity tempActivity = null;
	Camera mxCamera;
	boolean  cameraStatus = false;
	public static boolean sysCamera = false;
	
	private boolean isPassBtn = false;
	
	private TestCountDownTimer testCountDownTimer = null;

	private List<String> camera_project = new ArrayList<String>();

    private String mFocus_Mode =  Parameters.FOCUS_MODE_CONTINUOUS_PICTURE;

    private CameraManager mCameraMgr;
    private CameraDevice mCameraDevice;
    private HandlerThread mCameraThread;
    private Handler mCameraHandler;

	private void addProject(List<String> project){
		project.add("Hulk_7_GL_WIFI");
		project.add("Hulk_7_KIDS_WIFI");
	}
	
	public CameraTest(ID id, String name) {
		super(id, name);
		// TODO Auto-generated constructor stub
//		Tool.toolLog(TAG + " name " + name);
		if (name.equals("CAMERA IMG FRONT")){
//			Tool.toolLog(TAG + " cameraId 111111111111 ");
			cameraId = 1;
		}else if (name.equals("CAMERA2")){
			cameraId = 2;
		}else if (name.equals("MAIN2 Camera")){
			cameraId = 2;
		}else if (name.equals("WIDE CAMERA")){
			cameraId = 3;
		}else if (name.equals("Macro CAMERA")){
			cameraId = 3;
            mFocus_Mode = Parameters.FOCUS_MODE_MACRO;
		}

		else{
			cameraId = 0;
		}
		Tool.toolLog(TAG + " cameraId " + cameraId);
		sysCamera = false;
	}

	@Override
	public void setUp() {
		// TODO Auto-generated method stub
//		Tool.toolLog("setUp  ... ");
		Tool.toolLog(TAG + cameraId + "_start_test");
		testCountDownTimer = new TestCountDownTimer(SECOND*30, SECOND, this);
		addProject(camera_project);
		Test.gettag = TAG + cameraId;
		Test.state = null;
				
		LayoutInflater inflater = (LayoutInflater) mContext
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		focusIndicatorLayout = inflater.inflate(R.layout.focus_indicator, null);
		final FocusIndicatorRotateLayout focusIndicatorRotateLayout =
				(FocusIndicatorRotateLayout) focusIndicatorLayout
				.findViewById(R.id.focus_indicator_rotate_layout);
		
		//Add by Jianke.Zhang 01/23
		//tempActivity = mContext;
		if(mPreview != null){
			fl.removeView(mPreview);
			fl.removeView(mLayout);
			fl.removeView(focusIndicatorLayout);
		}
		//End
        mCameraThread = new HandlerThread("CameraThread");
        mCameraThread.start();
        mCameraHandler = new Handler(mCameraThread.getLooper());
        mCameraMgr = (CameraManager) (getContext().getSystemService(Context.CAMERA_SERVICE));
		
		if (cameraId == 0) {
			mPreview = new CameraTest.Preview(mContext, focusIndicatorRotateLayout,
							Looper.getMainLooper());
			// mContext.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		} else {
		    if (mFocus_Mode ==Parameters.FOCUS_MODE_MACRO){
                mPreview = new CameraTest.Preview(mContext, 0, cameraId,mFocus_Mode);
            }
		    else {
                mPreview = new CameraTest.Preview(mContext, 0, cameraId);
            }
		}
		
		//Add by Jianke.Zhang 01/22
/*		while(true){
			if(cameraStatus){
				cameraStatus = false;
				break;
			}
		}*/
		
		sysCamera = false;
		/*
		new Thread(){
			public void run(){
				Tool.toolLog(TAG + " onFinish Preview");
				while(true){
					if(sysCamera){
						Tool.toolLog(TAG + " 2222222111111");
						sysCamera = false;
						break;
					}
					Tool.sleepTimes(10);
				}
				Tool.sleepTimes(20);
				try{
					mxCamera = mPreview.getCamera();
					Tool.toolLog(TAG + " mxCamera " + mxCamera);
					if(mxCamera != null || cameraStatus){
						Test.state = "Pass";
						Msg.exitWithSuccessTest(mContext, TAG+cameraId, 20,true,"Pass");
					}else{
	//					Tool.toolLog(TAG + " xyyyyy");
						Test.state = "Fail";
						mContext.setResult(Test.RESULT.FAILED.ordinal());
						Tool.toolLog(TAG + " index 8883 -> " + ExecuteTest.temppositon);
						int double_test;
						if(AllMainActivity.mainAllTest){
							double_test = AllMainActivity.double_check_for_auto[ExecuteTest.temppositon]++;
							//AllMainActivity.mainAllTest = false;
						}else {
							double_test = autoRunActivity.double_check[ExecuteTest.temppositon]++;
						}
						Tool.toolLog(TAG + " double_test 9993-> " + double_test);
						if(double_test==1){
							Msg.exitWithException(mContext, TAG+cameraId,50,true,"Pass");
						}else{
							Msg.exitWithException(mContext, TAG+cameraId,50,true,"Fail");
						}
						
					}
				}catch(Exception e){
					Tool.toolLog(TAG + " Maybe api wrong");
					return;
				}
			}
		}.start();
		*/
		//End

	}

	
	@Override
	public void initView() {
		// TODO Auto-generated method stub
		mLayout = (LinearLayout) View.inflate(mContext, R.layout.manu_base_screen,
				null);

		 bt_left = (Button) mLayout.findViewById(R.id.bt_left);
		 bt_right = (Button) mLayout.findViewById(R.id.bt_right);

		 bt_left.setText(R.string.pass);
		 bt_right.setText(R.string.fail);
		// bt_left.setAlpha(0.2f);
		// bt_right.setAlpha(0.2f);
		//
		bt_left.setOnClickListener(pass_listener);
		bt_right.setOnClickListener(failed_listener);
		bt_left.setEnabled(false);
		
		text_top_zone = (TextView) mLayout.findViewById(R.id.text_top_zone);
		text_top_zone.setText(mName);

		testCountDownTimer.start();
		
		fl = new FrameLayout(mContext);
		fl.addView(mPreview);
		fl.addView(mLayout);
		fl.addView(focusIndicatorLayout);

		mContext.setContentView(fl);
		
		sHandler.sendEmptyMessageDelayed(0x01, 3000);
	}

	Handler sHandler = new Handler(){
		
		public void handleMessage(Message msg){
			bt_left.setEnabled(true);
			isPassBtn = true;
		}
	};
	
	@Override
	public void destroy() {
		// TODO Auto-generated method stub
		Tool.toolLog(TAG + " destroy");
		if (mPreview != null && mPreview.getCamera() != null
				&& cameraStatus) {
			mPreview.getCamera().release();
			cameraStatus = false;
		}
		
		if (testCountDownTimer != null) {
			testCountDownTimer.cancel();
		}
	}

	@Override
	public void onFinish() {
		// TODO Auto-generated method stub
		Tool.toolLog(TAG + " onFinish");
		if(!isPassBtn){
			ManuFinishThread tFinishThread = new ManuFinishThread(0x02);
			tFinishThread.start();
			try {
				tFinishThread.join();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			Msg.exitWithException(mContext, TAG+cameraId,30,false,"Fail");
		}
		
	}

	@Override
	public void onTick() {
		// TODO Auto-generated method stub
		Tool.toolLog(TAG + " onTick");
	}

	public String getmContextTag() {
		// TODO Auto-generated method stub
		String tag = TAG + cameraId;
		if(cameraId == 1){
			tag = TAG + cameraId;
		}
		
		return tag;
	}

	public void setmContextTag() {
		// TODO Auto-generated method stub
	}

	@Override
	public void finish() {
		// TODO Auto-generated method stub
		
	}
	


interface FocusIndicator {
	public void showStart();

	public void showSuccess(boolean timeout);

	public void showFail(boolean timeout);

	public void clear();
}

interface Rotatable {
	// Set parameter 'animation' to true to have animation when rotation.
	public void setOrientation(int orientation, boolean animation);
}




class Preview extends SurfaceView implements SurfaceHolder.Callback {
	SurfaceHolder mHolder;
	private  Camera mCamera;

	int mZoom = 0;
	private final AutoFocusCallback mAutoFocusCallback = new AutoFocusCallback();
	private final AutoFocusMoveCallback mAutoFocusMoveCallback = new AutoFocusMoveCallback();
	private int mViewFinderWidth;
	private int mViewFinderHeight;
	private boolean mAutoFocus = false;
	private static final int FOCUS_FRAME_DELAY = 1000;
	private static final int RESET_TOUCH_FOCUS = 0;
	private Handler mHandler;
	private FocusIndicatorRotateLayout mFocusIndicatorRotateLayout;
	private String mFocus_Mode = Parameters.FOCUS_MODE_CONTINUOUS_PICTURE;
    private CaptureRequest.Builder mPreviewRequestBuilder;
    private CaptureRequest mPreviewRequest;
    private CameraCaptureSession mCaptureSession;
	private CameraDevice.StateCallback mCameraStateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(CameraDevice camera) {
            Tool.toolLog(TAG+"on camera opened, id = " + camera.getId());
            mCameraDevice = camera;
            startPreview();
        }

        @Override
        public void onClosed(CameraDevice camera) {
            Tool.toolLog(TAG+ "on camera close, id = " + camera.getId());
        }

        @Override
        public void onDisconnected(CameraDevice camera) {
            Tool.toolLog(TAG+ "on camera disconnected, id = " + camera.getId());
        }

        @Override
        public void onError(CameraDevice camera, int error) {
            Tool.toolLog(TAG+ "camera: " + camera.getId() + " error, error code = " + error);
            //leftButton.setEnabled(false);
        }
    };
    private void startPreview() {
        try {
            mPreviewRequestBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            mPreviewRequestBuilder.addTarget(getHolder().getSurface());
            mCameraDevice.createCaptureSession(Arrays.asList(getHolder().getSurface()), new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(CameraCaptureSession session) {
                    try {
                        mCaptureSession = session;
                        mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
                        mPreviewRequest = mPreviewRequestBuilder.build();

                        mCaptureSession.setRepeatingRequest(mPreviewRequest, mPreviewCallback, mCameraHandler);
                    } catch (CameraAccessException e) {
                        e.printStackTrace();
                        //leftButton.setEnabled(false);
                    }
                }

                @Override
                public void onConfigureFailed(CameraCaptureSession session) {
                    Tool.toolLog(TAG + "onConfigureFailed");
                    //leftButton.setEnabled(false);
                }
            }, mCameraHandler);
        }catch(CameraAccessException e) {
            e.printStackTrace();
        }
    }
    private CameraCaptureSession.CaptureCallback mPreviewCallback = new CameraCaptureSession.CaptureCallback() {
        @Override
        public void onCaptureCompleted(CameraCaptureSession session, CaptureRequest request, TotalCaptureResult result) {

        }

    };
    /**
     * Camera HAL device API version 2.0
     * @hide
     */
    public static final int CAMERA_HAL_API_VERSION_2_0 = 0x200;
	private static final String TAG = "MMITEST_Preview";
	
	int cameraId = 0;
	Context mContext;

	public int getPreviewWidth() {
		return mViewFinderWidth;
	}

	public int getPreviewHeight() {
		return mViewFinderHeight;
	}

	class MainHandler extends Handler {
		public MainHandler(Looper looper) {
			super(looper);
		}

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case RESET_TOUCH_FOCUS: 
				cancelAutoFocus();
				break;
			}
		}
	}

	class AutoFocusCallback implements Camera.AutoFocusCallback {
		@Override
		public void onAutoFocus(boolean focused, Camera camera) {
			doAutoFocus(focused);
			mHandler.sendEmptyMessageDelayed(RESET_TOUCH_FOCUS,
					FOCUS_FRAME_DELAY);
			mAutoFocus = true;
		}
	}

	@SuppressLint("NewApi") class AutoFocusMoveCallback implements Camera.AutoFocusMoveCallback {
		@Override
		public void onAutoFocusMoving(boolean moving, Camera camera) {
			mHandler.removeMessages(RESET_TOUCH_FOCUS);
			doMovingAutoFocus(moving);
		}
	}

	public void cancelAutoFocus() {
		resetTouchFocus();
	}

	public void resetTouchFocus() {
		// Put focus indicator to the center.
		RelativeLayout.LayoutParams p = (RelativeLayout.LayoutParams) mFocusIndicatorRotateLayout
				.getLayoutParams();
		int[] rules = p.getRules();
		rules[RelativeLayout.CENTER_IN_PARENT] = RelativeLayout.TRUE;
		p.setMargins(0, 0, 0, 0);
		mFocusIndicatorRotateLayout.clear();
	}

	public void doAutoFocus(boolean focused) {
		mHandler.removeMessages(RESET_TOUCH_FOCUS);
		if (focused) {
			mFocusIndicatorRotateLayout.showSuccess(false);
		} else {
			mFocusIndicatorRotateLayout.showFail(false);
		}
	}

	public void doMovingAutoFocus(boolean moving) {
		if (moving) {
			mFocusIndicatorRotateLayout.showStart();
		} else {
			mFocusIndicatorRotateLayout.showSuccess(true);
		}
	}
	
	Preview(Context context,
			FocusIndicatorRotateLayout focusIndicatorRotateLayout, Looper looper) {
		super(context);
		mContext = context;
		mHandler = new MainHandler(looper);
		mFocusIndicatorRotateLayout = focusIndicatorRotateLayout;
		// Install a SurfaceHolder.Callback so we get notified when the
		// underlying surface is created and destroyed.
		mHolder = getHolder();
		mHolder.addCallback(this);
		mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
	}

	Preview(Context context) {
		super(context);
		mContext = context;

		// Install a SurfaceHolder.Callback so we get notified when the
		// underlying surface is created and destroyed.
		mHolder = getHolder();
		mHolder.addCallback(this);
		mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
	}

	Preview(Context context, int scale) {
		this(context);
		mZoom = scale;
	}

	Preview(Context context, int scale, int camera) {
		this(context, scale);
		cameraId = camera;
	}
    Preview(Context context, int scale, int camera,String Focus_Mode) {
        this(context, scale);
        cameraId = camera;
        mFocus_Mode = Focus_Mode;

    }

	public  Camera getCamera() {
		return mCamera;
	}

	public void surfaceCreated(SurfaceHolder holder) {
		// The Surface has been created, acquire the camera and tell it
		// where
		// to draw.
		Tool.toolLog(TAG + " surfaceCreated");
		try {
			if (cameraId == 0) {
				mCamera = null;
				mCamera = Camera.open();
//				Tool.toolLog(TAG + " MMI Test " +  mCamera);
				//Jianke.Zhang 05/29
				if(mCamera != null){
					setCameraDisplayOrientation(mContext, cameraId, mCamera);
					cameraStatus = true;
				}else{
					Tool.toolLog(TAG + " surfaceCreated back failed ");
				}
				//End
			} else {
				Tool.toolLog(TAG + " cameraId dadadada " + Camera.getNumberOfCameras());
				if (mFocus_Mode==Parameters.FOCUS_MODE_CONTINUOUS_PICTURE) {
					mCamera = null;
					Tool.toolLog(TAG + " mCamera  1111 " + mCamera);
					mCamera = Camera.open(cameraId);
					Tool.toolLog(TAG + " mCamera 2222 " + mCamera);
					if(mCamera != null){
						setCameraDisplayOrientation(mContext, cameraId,
								mCamera);
						cameraStatus = true;
					}else{
						Tool.toolLog(TAG + " surfaceCreated front failed ");
					}
				}else{
                    mCamera = null;
                    Tool.toolLog(TAG + " macro mCamera 1111 ");
                    String cameraIdStr = String.valueOf(cameraId);
                    mCameraMgr.openCamera(cameraIdStr, mCameraStateCallback, mCameraHandler);
                    Tool.toolLog(TAG + " macro mCamera 2222 ");
                    if(mCamera != null){
                        setCameraDisplayOrientation(mContext, cameraId,
                                mCamera);
                        cameraStatus = true;
                    }else{
                        Tool.toolLog(TAG + " surfaceCreated front failed ");
                    }
                }
			}
		} catch (Exception e) {
			Tool.toolLog(TAG + " MMI Test " +  "can't open camera.");
			Tool.toolLog(TAG + " cameraId " + cameraId);
			Tool.toolLog(TAG +"Exception: "+ e);
			e.printStackTrace();
			try {
				mCamera = null;
				Tool.toolLog(TAG + " try macro mCamera 1111 ");
				String cameraIdStr = String.valueOf(cameraId);
				mCameraMgr.openCamera(cameraIdStr, mCameraStateCallback, mCameraHandler);
				Tool.toolLog(TAG + " try macro mCamera 2222 ");
				if (mCamera != null) {
					setCameraDisplayOrientation(mContext, cameraId,
							mCamera);
					cameraStatus = true;
				} else {
					Tool.toolLog(TAG + " surfaceCreated front failed ");
				}
			}catch (Exception e2) {
				Tool.toolLog(TAG+"try macro camera fail");
				Tool.toolLog(TAG + " MMI Test " +  "can't open camera.");
				Tool.toolLog(TAG + " cameraId " + cameraId);
				Tool.toolLog(TAG +"Exception: "+ e);
				e2.printStackTrace();
				return;
			}
		}
	}

	public  void setCameraDisplayOrientation(Context context,
			int cameraId, Camera camera) {
		// See android.hardware.Camera.setCameraDisplayOrientation for
		// documentation.
		Camera.CameraInfo info = new Camera.CameraInfo();
		Camera.getCameraInfo(cameraId, info);
		int degrees = getDisplayRotation(context);
		int result;
		Tool.toolLog(TAG + "degrees==" +degrees);
		Tool.toolLog(TAG+ " info.facing "+info.facing +" info.orientation  " +info.orientation );
		if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
			// result = (info.orientation + degrees) % 360;
			if(camera_project.contains(AllMainActivity.deviceName)){
				result = (info.orientation + degrees - 180) % 360;
			}else {
				result = (info.orientation - degrees + 180) % 360;
//			Tool.toolLog(TAG + " info " + "front ********  ????");
			}
		} else {
			result = (info.orientation - degrees + 360) % 360;
//			Tool.toolLog(TAG + " info " + "behind ******* ????");
		}
		Tool.toolLog(TAG + "result==" +result);
		camera.setDisplayOrientation(result);
	}

	public  int getDisplayRotation(Context context) {
		int rotation = ((WindowManager) (context
				.getSystemService(Context.WINDOW_SERVICE))).getDefaultDisplay()
				.getRotation();
		switch (rotation) {
		case Surface.ROTATION_0:
			return 0;
		case Surface.ROTATION_90:
			return 90;
		case Surface.ROTATION_180:
			return 180;
		case Surface.ROTATION_270:
			return 270;
		}
		return 0;
	}

	public void surfaceDestroyed(SurfaceHolder holder) {
		// Surface will be destroyed when we return, so stop the preview.
		// Because the CameraDevice object is not a shared resource,
		// it's very
		// important to release it when the activity is paused.
		Tool.toolLog(TAG + " surfaceDestroyed");
		if (cameraStatus /* && mCamera.previewEnabled() */) {
			try {
				int mCameraEnabled = 0;
				Camera.Parameters mParameters = mCamera.getParameters();
				mParameters.set("camera_enabled",
						String.valueOf(mCameraEnabled));
				mCamera.setParameters(mParameters);
				if (true/* mCamera.previewEnabled() */) {
					mCamera.stopPreview();
				}

				if (cameraStatus) {
					cameraStatus = false;
					mCamera.release();
				}
				
			} catch (Exception e) {
				Tool.toolLog(TAG + " MMI Test" + " can't stop preview ");
			}
			mCamera = null;
		}
	}

	public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
		// Now that the size is known, set up the camera parameters and
		// begin
		// the preview.
		Tool.toolLog(TAG + " surfaceChanged");
		if (mCamera != null) {
			try {
				mCamera.setPreviewDisplay(holder);
			} catch (Exception ex) {
				Tool.toolLog(TAG + " setPreview failed");
			}

			mViewFinderWidth = w;
			mViewFinderHeight = h;
			setCameraParameters();

			try {
				Tool.toolLog(TAG + " startPreview");
				Camera.Parameters mParameters = mCamera.getParameters();
				/*
				 * AutoFocusMoveCallback is only supported in continuous
				 * autofocus modes
				 */
//				Tool.toolLog(TAG + " xxx mCamera " + mCamera);
				mParameters
						.setFocusMode(Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
//				if (cameraId == 0) {
//					mCamera.setParameters(mParameters);
//				}
				mCamera.setAutoFocusMoveCallback(mAutoFocusMoveCallback);
				mCamera.startPreview();
//				Tool.toolLog(TAG + " xxx mFocusIndicatorRotateLayout " 
//									+ mFocusIndicatorRotateLayout);
				//Add by Jianke.Zhang 01/23
				if(mFocusIndicatorRotateLayout != null){
					mFocusIndicatorRotateLayout.showStart();
				}
				//End
			} catch (Throwable ex) {
				Tool.toolLog(TAG + " startPreview failed " + ex);
			}
		}
		
	}


	public void setZoom(int z) {
		Camera.Parameters mParameters = mCamera.getParameters();
		if (z < 0) {
			z = 0;
		} else if (z > 60) {
			z = 60;
		}
		Tool.toolLog(TAG + " set zoom value to " + z);
		mParameters.set("zoom", String.valueOf(z));
		mCamera.setParameters(mParameters);
	}

	private void setCameraParameters() {
//		Tool.toolLog(TAG + " setCameraParameters");

		final String ANDROID_QUALITY = "jpeg-quality";
		final String THUNDERST_TIMESTAMP = "thunderst_timestamp";
		final String THUNDERST_NIGHTMODE = "thunderst_nightmode";
		final String ANDROID_EFFECT = "effect";
		final String ANDROID_FLICKER_ADJ = "antibanding";
		final String PARM_PICTURE_SIZE = "picture-size";

		final String BRIGHTNESS = "luma-adaptation";
		final String WHITEBALANCE = "whitebalance";

		Camera.Parameters mParameters = mCamera.getParameters();

		mParameters.setPreviewSize(mViewFinderWidth, mViewFinderHeight);
//		Tool.toolLog(TAG + " setCameraParameter: mViewFinderWidth: " + mViewFinderWidth
//				+ " mViewFinderHeight: " + mViewFinderHeight);

		// if we don't set camera enabled, the HAL won't enable the camera
		// device
		// and we MUST disable camera when we stop camera device
		int mCameraEnabled = 1;
		//Jianke.Zhang 02/11
//		mParameters.set("camera_enabled", String.valueOf(mCameraEnabled));

		// to prevent auto clockwise rotation of 90 degree
		// mParameters.set("orientation", "portrait");
		// mParameters.set("rotation", 180);
		// SurfaceHolder.getSurface();
		// Surface.setOrientation(Display.DEFAULT_DISPLAY, Surface.ROTATION_90);
		// mParameters.set("orientation", "landscape");
		// mParameters.set("rotation", 90);
		// mParameters.setRotation(90);
		//Jianke.Zhang 02/11
//		mParameters.set("disp-rotate", 1);

		// Set picture size parameter.
		// String pictureSize = "800x600";
		// String pictureSize = "640x480";
		// mParameters.set(PARM_PICTURE_SIZE, pictureSize);
		//Jianke.Zhang 02/11
//		mParameters.setPreviewSize(640, 360);

		// Set zoom
		int currentZoomValue = 0;
		//Jianke.Zhang 02/11
//		mParameters.set("zoom", String.valueOf(currentZoomValue));

		// Set resolution
		// mParameters.set(PARM_PICTURE_SIZE, "1600x1200");
		//Jianke.Zhang 02/11
//		mCamera.setParameters(mParameters);

		// Set whitebalance
		String whiteBalance = "auto";
		//Jianke.Zhang 02/11
//		mParameters.set(WHITEBALANCE, whiteBalance);
//		mCamera.setParameters(mParameters);

		// Set brightness
		int brightness = 4;
		mParameters.set(BRIGHTNESS, brightness);

		// Set the MyCameraSettings' settings to camera device
		/*
		 * Quality 0:high 1:normal 2:basic
		 */
		String camera_quality = "1";
		String quality = "100";
		if (camera_quality.equals("1")) {
			quality = "60";
		} else if (camera_quality.equals("2")) {
			quality = "30";
		}
		mParameters.set(ANDROID_QUALITY, quality);

		/*
		 * Night mode 1:night mode 0:not night mode
		 */
		boolean nightmode = false;
		mParameters.set(THUNDERST_NIGHTMODE, nightmode ? "1" : "0");

		/*
		 * timestamp 1: with timestamp 0: without timestamp
		 */
		boolean timestamp = true;
		mParameters.set(THUNDERST_TIMESTAMP, timestamp ? "1" : "0");

		/*
		 * effect 0: None 1: Grayscale 2: Negative 3: Sepia
		 */
		String effect = "0";
		mParameters.set(ANDROID_EFFECT, effect);

		/*
		 * flicker adjustment 0: auto 1: 50 HZ 2: 60 HZ
		 */
		String flicker = "0";
		mParameters.set(ANDROID_FLICKER_ADJ, flicker);

		try {
			//Jianke.Zhang 02/11
//			mCamera.setParameters(mParameters);
		} catch (IllegalArgumentException e) {
			Tool.toolLog(TAG + "set Parameters error!!! " + e);
			e.printStackTrace();
		}
	}

	
}	
}
