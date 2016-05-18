package com.avtechlabs.prettyseekbar;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import java.util.LinkedList;
import java.util.List;

import static com.avtechlabs.prettyseekbar.R.color.white;

/**
 * Created by adhithyan-3592 on 17/05/16.
 */

public class PrettySeekBar extends View{
    private int outerCircleFillColor, innerCircleFillColor, progressColor;
    private int outerCircleRadius, innerCircleRadius;
    private int imageLeftPos, imageRightPos, imageTopPos, imageBottomPos;
    private TypedArray array;
    private Paint paint, outerCirclePainter, innerCirclePainter, progressPainter;
    private float progressSweepAngle, progressStartAngle = 0f;
    private int maxProgress;
    private RectF rectF = new RectF();
    private int i = -1, radiusIncrementValue = 1;
    private Bitmap image = null;

    private int[] x;
    private int[] y;
    private int point = 0;


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
        array = context.getTheme().obtainStyledAttributes(attrs, R.styleable.PrettySeekBar, 0, 0);

        try{
            outerCircleFillColor = array.getInteger(R.styleable.PrettySeekBar_outerCircleFillColor, R.color.outer);
            innerCircleFillColor = array.getInteger(R.styleable.PrettySeekBar_innerCircleFillColor, R.color.inner);
            maxProgress = array.getInteger(R.styleable.PrettySeekBar_maxProgress, 100);
        }finally {
            array.recycle();
        }

        paint = new Paint();

        outerCirclePainter = new Paint();
        outerCirclePainter.setStyle(Paint.Style.FILL);
        outerCirclePainter.setAntiAlias(true);
        outerCirclePainter.setColor(outerCircleFillColor);

        innerCirclePainter = new Paint();
        innerCirclePainter.setStyle(Paint.Style.FILL);
        innerCirclePainter.setAntiAlias(true);
        innerCirclePainter.setColor(innerCircleFillColor);

        progressPainter = new Paint();
        progressPainter.setStyle(Paint.Style.STROKE);
        progressPainter.setAntiAlias(true);
        progressPainter.setStrokeCap(Paint.Cap.BUTT);
        progressPainter.setStrokeWidth(10);
        progressPainter.setColor(outerCircleFillColor);

        progressSweepAngle = 360 / maxProgress;

        x = new int[360];
        y = new int[360];

        //calculateRadius(this.getMeasuredWidth()/2, this.getMeasuredHeight()/2);
        //calculatePointsOnCircle();
    }

    @Override
    protected void onDraw(Canvas canvas){
        int viewWidthHalf = this.getMeasuredWidth() / 2;
        int viewHeightHalf = this.getMeasuredHeight() / 2;
        int min = Math.min(viewWidthHalf, viewHeightHalf);

        calculateRadius(viewWidthHalf, viewHeightHalf);

        int diameter = min - getPaddingLeft();
        rectF.set(viewWidthHalf, viewHeightHalf, (int)(2 * 3.14 * (outerCircleRadius)), (int)(2 * 3.14 * innerCircleRadius));

        canvas.drawCircle(viewWidthHalf, viewHeightHalf, outerCircleRadius, outerCirclePainter);
        canvas.drawCircle(viewWidthHalf, viewHeightHalf, innerCircleRadius, innerCirclePainter);
        //canvas.drawArc(left, top, right, bottom, start, sweep, center, paint);

        canvas.drawLine(viewWidthHalf, viewHeightHalf, x[point],   y[point], progressPainter);
        point = (point + 1 == 360) ? 0 : point + 1;
        //incrementSweepAngle();

        if(image != null){
            canvas.drawBitmap(image, viewWidthHalf, viewHeightHalf, paint);
        }


    }

    private void calculateRadius(int viewWidthHalf, int viewHeightHalf){

        if(i == -1){
            outerCircleRadius = (viewWidthHalf > viewHeightHalf) ? (viewHeightHalf / 2) : (viewWidthHalf / 2);
            innerCircleRadius = (int) (outerCircleRadius * 0.8);

            //int length = (int) (innerCircleRadius * 3.14 * 2);
            //imageLeftPos = imageTopPos = length;

            //imageBottomPos = length + length;
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

        calculatePointsOnCircle(viewWidthHalf, viewHeightHalf);

    }

    public void setImageResource(Bitmap image){
        this.image = image;
    }

    public void setMaxProgress(int maxProgress){
        this.maxProgress = maxProgress;
    }

    private void incrementSweepAngle(){
        progressStartAngle += progressSweepAngle;
    }

    private void calculatePointsOnCircle(int xorigin, int yorigin){
        double angle = 0d;


        for(int j = 0; j < 360; j++){
            x[j] = (int) ((xorigin) + (outerCircleRadius * Math.cos(angle * Math.PI / 180)));
            y[j] = (int) ((yorigin) + (outerCircleRadius * Math.sin(angle * Math.PI / 180)));

            angle += 1d;
        }
    }
}
