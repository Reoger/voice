package reoger.hut.voice.view;

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.os.SystemClock;
import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.MotionEvent;

/**
 * Created by 24540 on 2017/6/17.
 *
 */

public class MyDrawable extends Drawable {

    private Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private int mAlpha = 255;
    private float mRipplePointX, mRipplePointY;
    private float mRippleRadius;

    private int mRippleColor;


    private float progress = 0;

    public MyDrawable() {
        //防抖动
        mPaint.setDither(true);
        //抗锯齿
        mPaint.setAntiAlias(true);

        mRippleRadius = 200;

        setRippleColor(0x330000ff);
    }

    private void setRippleColor(int color) {
        //不建议直接设置颜色，一般会先将其保存起来
        mRippleColor = color;
        onColorOrAlphaChange();
    }

    private void onColorOrAlphaChange() {
        mPaint.setColor(mRippleColor);

        if (mAlpha != 255) {
            int alpha = mPaint.getAlpha();
            int realAlpha = (int) (alpha * (mAlpha / 255f));
            mPaint.setAlpha(realAlpha);
        }
    }

    @Override
    public void draw(@NonNull Canvas canvas) {
//        canvas.drawColor(android.support.v7.appcompat.R.color.notification_icon_bg_color);
        canvas.drawCircle(mRipplePointX, mRipplePointY, mRippleRadius, mPaint);

    }

    @Override
    public void setAlpha(@IntRange(from = 0, to = 255) int alpha) {

    }

    @Override
    public void setColorFilter(@Nullable ColorFilter colorFilter) {
        //设置滤镜

    }


    @Override
    public int getOpacity() {
        int alpha = mPaint.getAlpha();
        if (alpha == 255)//不透明
            return PixelFormat.OPAQUE;
        else if (alpha == 0)//全透明
            return PixelFormat.TRANSPARENT;
        return PixelFormat.TRANSLUCENT;//半透明
    }

    public void setOnTouchEvent(MotionEvent event) {
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                actionDown(event.getX(), event.getY());
                break;

        }
    }

    private void actionDown(float x, float y) {
        mRipplePointX = x;
        mRipplePointY = y;
        mRippleRadius = 0;
        progress = 0;
        unscheduleSelf(mEnterRunnable);
        scheduleSelf(mEnterRunnable, SystemClock.uptimeMillis());
    }


    private Runnable mEnterRunnable = new Runnable() {
        @Override
        public void run() {
            if(progress>=1)
                return;

            progress +=0.01f;
            mRippleRadius = progress*200;
            invalidateSelf();
            scheduleSelf(this, SystemClock.uptimeMillis() + 16);
        }
    };
}
