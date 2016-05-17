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
    private int outerCircleRadius, innerCircleRadius;
    private TypedArray array;
    private Paint paint;
    private int i = -1, radiusIncrementValue = 1;


    public interface  OnPrettySeekBarChangeListener{
        void onProgressChanged(PrettySeekBar prettySeekBar, int progress, boolean touched);

        void onStartTrackingTouch(PrettySeekBar seekBar);

        void onStopTrackingTouch(PrettySeekBar seekBar);
    }

    public PrettySeekBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);

        new Thread(new Runnable() {
            @Override
            public void run() {
                while(true){
                    try {
                        Thread.sleep(150);
                        postInvalidate();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
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

        calculateRadius(viewWidthHalf, viewHeightHalf);

        paint.setAntiAlias(true);

        paint.setStyle(Paint.Style.FILL);
        paint.setColor(outerCircleFillColor);
        canvas.drawCircle(viewWidthHalf, viewHeightHalf, outerCircleRadius, paint);
        
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(innerCircleFillColor);
        canvas.drawCircle(viewWidthHalf, viewHeightHalf, innerCircleRadius, paint);

    }

    private void calculateRadius(int viewWidthHalf, int viewHeightHalf){

        if(i == -1){
            outerCircleRadius = (viewWidthHalf > viewHeightHalf) ? (viewHeightHalf / 2) : (viewWidthHalf / 2);
            innerCircleRadius = (int) (outerCircleRadius * 0.8);

            i++;
        }else{

            outerCircleRadius += radiusIncrementValue;
            innerCircleRadius = (int) (outerCircleRadius * 0.8);

            if(i == 10){
                radiusIncrementValue = -1;
            }

            if(i == 20){
                radiusIncrementValue = 1;
                i = -1;
            }

            i++;

        }

    }

    public void setImageResource(){

    }
}
