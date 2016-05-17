package com.avtechlabs.prettyseekbar;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by adhithyan-3592 on 17/05/16.
 */

public class PrettySeekBar extends View{
    private int outerCircleFillColor, innerCircleFillColor;
    private Paint paint;
    TypedArray array;

    public PrettySeekBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs){
        paint = new Paint();

        array = context.getTheme().obtainStyledAttributes(attrs, R.styleable.PrettySeekBar, 0, 0);

        try{
            outerCircleFillColor = array.getInteger(R.styleable.PrettySeekBar_outerCircleFillColor, R.color.outer);
            innerCircleFillColor = array.getInteger(R.styleable.PrettySeekBar_innerCircleFillColor, R.color.inner);
        }finally {
            array.recycle();
        }
    }

    @Override
    protected void onDraw(Canvas canvas){
        int viewWidthHalf = this.getMeasuredWidth() / 2;
        int viewHeightHalf = this.getMeasuredHeight() / 2;

        int outerCircleRadius = (viewWidthHalf > viewHeightHalf) ? (viewHeightHalf / 2) : (viewWidthHalf / 2);
        int innerCircleRadius = (int) (outerCircleRadius * 0.8);

        paint.setAntiAlias(true);

        paint.setStyle(Paint.Style.FILL);
        paint.setColor(outerCircleFillColor);
        canvas.drawCircle(viewWidthHalf, viewHeightHalf, outerCircleRadius, paint);

        paint.setStyle(Paint.Style.STROKE);
        //paint.setColor(innerCircleFillColor);
        canvas.drawCircle(viewWidthHalf, viewHeightHalf, innerCircleRadius, paint);
    }
}
