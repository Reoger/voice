package reoger.hut.voice.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.MotionEvent;

/**
 * Created by 24540 on 2017/6/17.
 *
 */

public class RippleButton extends android.support.v7.widget.AppCompatButton {

    private MyDrawable myDrawable;

    public RippleButton(Context context) {
        this(context,null );
    }

    public RippleButton(Context context, AttributeSet attrs) {
        this(context, attrs,0);
    }

    public RippleButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        myDrawable = new MyDrawable();
        myDrawable.setCallback(this);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        myDrawable.draw(canvas);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        myDrawable.setOnTouchEvent(event);
//        invalidate();
        return true;
    }

    //验证Drawable
    @Override
    protected boolean verifyDrawable(@NonNull Drawable who) {
        return who == myDrawable || super.verifyDrawable(who);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        myDrawable.setBounds(0,0,getWidth(),getHeight());
    }
}
