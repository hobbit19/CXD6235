package com.tcl.autotest.utils;

import com.tcl.autotest.tool.Tool;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.view.View;

public class RectView extends View {

	private Paint mPainter = new Paint();
	private Rect mTempRect = new Rect();
	private Lcd lcd;
	private Context mContext;

	private int[] mColors = { Color.BLACK };
	private Bitmap LcdBitmap;

	public RectView(Context context) {
		super(context);
		mContext = context;
	}

	public RectView(Context context, int[] colors) {
		super(context);
		mColors = colors;
		mContext = context;
	}

	public RectView(Context context, int color) {
		super(context);
		mColors[0] = color;
		mContext = context;
	}

	@Override
	protected void onDraw(Canvas canvas) {

//		Tool.toolLog("RectView onDraw");
		lcd = new Lcd(mContext);
		mPainter.setStyle(Paint.Style.FILL);

		if (LcdBitmap == null) {
			int rectLength = lcd.height() / mColors.length;
			int firstRectLength = lcd.height() - (mColors.length - 1)
					* rectLength;

			mPainter.setColor(mColors[0]);
			mTempRect.set(0, 0, lcd.width(), firstRectLength);
			canvas.drawRect(mTempRect, mPainter);

			for (int i = 1; i < mColors.length; i++) {
				mPainter.setColor(mColors[i]);

				mTempRect.set(0, firstRectLength + rectLength * (i - 1),
						lcd.width(), firstRectLength + rectLength * i);
				canvas.drawRect(mTempRect, mPainter);
			}
		} else {
			mPainter.setColor(Color.WHITE);
			mTempRect.set(0, 0, lcd.width(), lcd.height());
			canvas.drawRect(mTempRect, mPainter);

			for (int x = 0; x < lcd.width(); x++) {
				for (int y = (x & 1); y < lcd.height(); y += 2) {
					mPainter.setColor(Color.BLACK);
					canvas.drawPoint(x, y, mPainter);
				}
			}
		}
	}
}
