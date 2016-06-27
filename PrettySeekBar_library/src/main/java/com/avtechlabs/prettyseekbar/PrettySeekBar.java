package com.avtechlabs.prettyseekbar;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;


import java.util.concurrent.atomic.AtomicBoolean;


/**
 * Created by adhithyan-3592 on 17/05/16.
 */

public class PrettySeekBar extends View{
    //the colors of outer circle and inner circle
    private int outerCircleFillColor, innerCircleFillColor;

    //the radius of outer circle and inner circle
    private int outerCircleRadius, innerCircleRadius;

    private TypedArray array;

    //objects that draw on screen
    private Paint paint, outerCirclePainter, innerCirclePainter, progressPainter;

    //maximum progress
    private int maxProgress = 100;
    private RectF rectF = new RectF();

    private int i = -1, radiusIncrementValue = 1;
    private Bitmap image = null;
    AtomicBoolean makeProgress = new AtomicBoolean(false);
    int makeProgressTime = (int)(((double)maxProgress/(double)360) * 1000);
    private int currentProgress;

    //variables to store points on the circle, assuming 360 points in circle.
    private int[] x;
    private int[] y;


    //variable to keep track of current angle
    private int point = 270;

    private boolean pause = true;

    public boolean mEnabled = true;

    private OnPrettySeekBarChangeListener onPrettySeekBarChangeListener;

    int circleMaxX = Integer.MIN_VALUE, circleMaxY = Integer.MIN_VALUE, circleMinX = Integer.MAX_VALUE, circleMinY = Integer.MAX_VALUE;

    private boolean pauseZoom = false;

    public interface  OnPrettySeekBarChangeListener{
        void onProgressChanged(PrettySeekBar prettySeekBar, int progress, boolean touched);

        void onStartTrackingTouch(PrettySeekBar seekBar);

        void onStopTrackingTouch(PrettySeekBar seekBar);
    }

    public PrettySeekBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);

        /**
         *  The zoom in and zoom out effect of circle is achieved by this thread.
         */

        Thread animationThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while(true){
                    try {
                        while(pauseZoom)
                            Thread.sleep(100);
                        Thread.sleep(200);
                        postInvalidate();

                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        /**
         *  The clock like animation is achieved by this thread.
         */


        Thread progressUpdaterThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while(true){
                    try {
                        Thread.sleep(makeProgressTime);


                        if(currentProgress < 360){
                            if(!pause){
                                makeProgress.set(true);
                                currentProgress++;
                            }

                        }else{
                            break;
                        }


                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        animationThread.start();
        progressUpdaterThread.start();
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

        x = new int[360];
        y = new int[360];

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec){
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onDraw(Canvas canvas){
            int viewWidthHalf = this.getMeasuredWidth() / 2;
            int viewHeightHalf = this.getMeasuredHeight() / 2;

            calculateRadius(viewWidthHalf, viewHeightHalf);

            rectF.set(viewWidthHalf, viewHeightHalf, (int) (2 * 3.14 * (outerCircleRadius)), (int) (2 * 3.14 * innerCircleRadius));

            canvas.drawCircle(viewWidthHalf, viewHeightHalf, outerCircleRadius, outerCirclePainter);
            canvas.drawCircle(viewWidthHalf, viewHeightHalf, innerCircleRadius, innerCirclePainter);

            if(makeProgress.get() && !pause){
                    point = (point + 1 == 360) ? 0 : point + 1;
                makeProgress.set(false);
            }
        canvas.drawLine(viewWidthHalf, viewHeightHalf, x[point], y[point], progressPainter);


            if(image != null) {
                canvas.drawBitmap(image, viewWidthHalf, viewHeightHalf, paint);
            }
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

        calculatePointsOnCircle(viewWidthHalf, viewHeightHalf);

    }

    public int setMaxProgress(int maxProgress){
        this.maxProgress = maxProgress;
        double time = ((double)maxProgress / (double)360) * 1000;

        currentProgress = 0;
        makeProgressTime = (int)time;
        return makeProgressTime;
    }

    private void calculatePointsOnCircle(int xorigin, int yorigin){
        double angle = 0d;


        for(int j = 0; j < 360; j++){
            x[j] = (int) ((xorigin) + (outerCircleRadius * Math.cos(angle * Math.PI / 180)));
            y[j] = (int) ((yorigin) + (outerCircleRadius * Math.sin(angle * Math.PI / 180)));

            setPointRange(x[j], y[j]);

            angle += 1d;
        }
    }

    public void pauseProgress(){
        pause = true;
    }

    public void makeProgress(){
        pause = false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event){
        if(mEnabled){
            this.getParent().requestDisallowInterceptTouchEvent(true);

            switch(event.getAction()){
                /*case MotionEvent.ACTION_DOWN:
                    Log.d("Adhithyan", "down");
                    onStartTrackingTouch();
                    updateOnTouch(event);
                    break;*/
                case MotionEvent.ACTION_MOVE:
                    //Log.d("Adhithyan", "move");
                    onStartTrackingTouch();
                    updateOnTouch(event);
                    break;
                /*case MotionEvent.ACTION_UP:
                    Log.d("Adhithyan", "up");
                    onStopTrackingTouch();
                    setPressed(false);
                    this.getParent().requestDisallowInterceptTouchEvent(false);
                    break;
                case MotionEvent.ACTION_CANCEL:
                    Log.d("Adhithyan", "cancel");
                    onStopTrackingTouch();
                    setPressed(false);
                    this.getParent().requestDisallowInterceptTouchEvent(false);
                    break;*/
            }
            return true;
        }
        return false;
    }

    public void setOnPrettySeekBarChangeListener(OnPrettySeekBarChangeListener listener){
        onPrettySeekBarChangeListener = listener;
    }

    private void onStartTrackingTouch(){
        if(onPrettySeekBarChangeListener != null){
            onPrettySeekBarChangeListener.onStartTrackingTouch(this);
        }
    }

    private void onStopTrackingTouch(){
        if(onPrettySeekBarChangeListener != null){
            onPrettySeekBarChangeListener.onStopTrackingTouch(this);
        }
    }

    private void onProgressChanged(PrettySeekBar prettySeekBar, int progress, boolean touched){
        if(onPrettySeekBarChangeListener != null){
            onPrettySeekBarChangeListener.onProgressChanged(this, progress, true);
        }
    }

    private void updateOnTouch(MotionEvent event){
        boolean ignoreTouch = ignoreTouch(event.getX(), event.getY());
        if(ignoreTouch){
            return;
        }
        setPressed(true);

        updateProgress(event.getX(), event.getY());


    }

    private boolean ignoreTouch(float xPos, float yPos){
        int x = Math.round(xPos), y = Math.round(yPos);

        if(x >= circleMinX && x <= circleMaxX){
            if(y >= circleMinY && y <= circleMaxY)
                return false;
        }
        
        return true;
    }

    private void updateProgress(float xPos, float yPos){
        pauseZoom = true;

        for(int i=0; i<360; i++){
            int xdiff = Math.abs((int)(x[i] - xPos));
            int ydiff = Math.abs((int)(y[i] - yPos));
            if(xdiff <= 10 && ydiff <= 10) {
                point = i;
                currentProgress = i;
                updateCurrentProgress(i);
                if(!makeProgress.get())
                    makeProgress.set(true);
                invalidate();
                break;
            }
        }

        pauseZoom = false;
    }

    private void setPointRange(int x, int y){
        circleMaxX = Math.max(x, circleMaxX);
        circleMaxY = Math.max(y, circleMaxY);

        circleMinX = Math.min(x, circleMinX);
        circleMinY = Math.min(y, circleMinY);
    }

    private void updateCurrentProgress(int point){
        //since clock starts animating from point 270, we are adjusting relative to it
        int adjustToStart = (point <= 270) ? (point + 90) : (point - 270);
        adjustToStart = (adjustToStart == 360) ? 0 : adjustToStart;
        int actualProgress = (adjustToStart * makeProgressTime) / 1000;

        onProgressChanged(this, actualProgress, true);
    }
}
