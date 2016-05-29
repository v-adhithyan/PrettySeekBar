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

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.avtechlabs.prettyseekbar.R.color.white;

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

    private float progressSweepAngle, progressStartAngle;

    //maximum progress
    private int maxProgress = 100;
    private RectF rectF = new RectF();

    private int i = -1, radiusIncrementValue = 1;
    private Bitmap image = null;
    AtomicBoolean makeProgress = new AtomicBoolean(false);
    int makeProgressTime = (int)(((double)maxProgress/(double)360) * 1000);
    private int currentProgress;
    public int ms = 0;

    //variables to store points on the circle, assuming 360 points in circle.
    private int[] x;
    private int[] y;


    //variable to keep track of current angle
    private int point = 270;

    private int startAngle = 270;

    private int rotation = 270;

    private boolean pause = true;

    public boolean mEnabled = true;

    private int mTranslateX, mTranslateY;

    private double touchAngle;

    private OnPrettySeekBarChangeListener onPrettySeekBarChangeListener;

    private static int INVALID_PROGRESS_VALUE = -1;

    int circleMaxX = Integer.MIN_VALUE, circleMaxY = Integer.MIN_VALUE, circleMinX = Integer.MAX_VALUE, circleMinY = Integer.MAX_VALUE;

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
                        Thread.sleep(200);
                        postInvalidate();

                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        /**
         *  This thread sleeps before incrementing the progress hand.
         */


        Thread progressUpdaterThread = new Thread(new Runnable() {
            @Override
            public void run() {
                //Log.d("Adhithyan", "maxprogress:" + maxProgress);
                while(true){
                    try {
                        Thread.sleep(makeProgressTime);

                        //Log.d("Adhithyan", "maxprogress:" + maxProgress);
                        //Log.d("Adhithyan", "currentProgress:" + currentProgress);
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

        progressSweepAngle = 360 / maxProgress;

        x = new int[360];
        y = new int[360];


        //calculateRadius(this.getMeasuredWidth()/2, this.getMeasuredHeight()/2);
        //calculatePointsOnCircle();

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec){
        final int height = getDefaultSize(getSuggestedMinimumHeight(), heightMeasureSpec);
        final int width = getDefaultSize(getSuggestedMinimumHeight(), widthMeasureSpec);
        final int min = Math.min(width, height);
        float top = 0;
        float left = 0;

        mTranslateX = (int)(width * 0.5f);
        mTranslateY = (int)(width * 0.5f);

        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onDraw(Canvas canvas){
            int viewWidthHalf = this.getMeasuredWidth() / 2;
            int viewHeightHalf = this.getMeasuredHeight() / 2;
            int min = Math.min(viewWidthHalf, viewHeightHalf);

            calculateRadius(viewWidthHalf, viewHeightHalf);

            int diameter = min - getPaddingLeft();
            rectF.set(viewWidthHalf, viewHeightHalf, (int) (2 * 3.14 * (outerCircleRadius)), (int) (2 * 3.14 * innerCircleRadius));

            canvas.drawCircle(viewWidthHalf, viewHeightHalf, outerCircleRadius, outerCirclePainter);
            canvas.drawCircle(viewWidthHalf, viewHeightHalf, innerCircleRadius, innerCirclePainter);
            //canvas.drawArc(left, top, right, bottom, start, sweep, center, paint);

            if(makeProgress.get() == true && !pause){
                point = (point + 1 == 360) ? 0 : point + 1;
                makeProgress.set(false);
                //Log.d("Adhithyan", "point:" + point);
            }
            canvas.drawLine(viewWidthHalf, viewHeightHalf, x[point],   y[point], progressPainter);

            //incrementSweepAngle();

            if(image != null) {
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


    public int setMaxProgress(int maxProgress){
        this.maxProgress = maxProgress;
        double time = ((double)maxProgress / (double)360) * 1000;

        currentProgress = 0;
        //Log.d("Adhithyan", time + " tiem");
        makeProgressTime = (int)time;
        //Log.d("Adhithyan", "makeprogess:" + makeProgressTime);
        return makeProgressTime;
    }


    private void incrementSweepAngle(){
        progressStartAngle += progressSweepAngle;
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
                case MotionEvent.ACTION_DOWN:
                    //Log.d("Adhithayn", "down");
                    onStartTrackingTouch();
                    updateOnTouch(event);
                    break;
                case MotionEvent.ACTION_MOVE:
                    //Log.d("Adhithayn", "move");
                    updateOnTouch(event);
                    break;
                case MotionEvent.ACTION_UP:
                    //Log.d("Adhithayn", "up");
                    onStopTrackingTouch();
                    setPressed(false);
                    this.getParent().requestDisallowInterceptTouchEvent(false);
                    break;
                case MotionEvent.ACTION_CANCEL:
                    //Log.d("Adhithayn", "cancel");
                    onStopTrackingTouch();
                    setPressed(false);
                    this.getParent().requestDisallowInterceptTouchEvent(false);
                    break;
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
        //Log.d("Adhithayn", "start tracking touch");
        if(onPrettySeekBarChangeListener != null){
            onPrettySeekBarChangeListener.onStopTrackingTouch(this);
        }
    }

    private void updateOnTouch(MotionEvent event){
        //Log.d("Adhithayn", "update on touch");
        boolean ignoreTouch = ignoreTouch(event.getX(), event.getY());
        if(ignoreTouch){
            return;
        }
        setPressed(true);
        touchAngle = getTouchDegrees(event.getX(), event.getY());

        //Log.d("Adhithayn", "touch angle:" + touchAngle);
        onProgressRefresh((int)(touchAngle), true);

    }

    private boolean ignoreTouch(float xPos, float yPos){
        int x = Math.round(xPos), y = Math.round(yPos);

        if(x >= circleMinX && x <= circleMaxX){
            if(y >= circleMinY && y <= circleMaxY)
                return false;
        }
        
        return true;
    }

    private double getTouchDegrees(float xPos, float yPos){
        float x = xPos - mTranslateX;
        float y = yPos - mTranslateY;

        double angle = Math.toDegrees(Math.atan2(y, x) + (Math.PI / 2) - Math.toRadians(rotation));

        if(angle < 0){
            angle = angle + 360;
        }

        angle -= startAngle;

        //Log.d("Adhithayn", "tocuh dress:" + angle);

        return angle;
    }

    private int getProgressForAngle(double angle){
        int touchProgress = (int) Math.round(angle);

        touchProgress = (touchProgress < 0) ? INVALID_PROGRESS_VALUE : touchProgress;
        touchProgress = (touchProgress > 360) ? INVALID_PROGRESS_VALUE : touchProgress;

        //Log.d("Adhithayn", "tocuh progress for angle:" + touchProgress);
        return touchProgress;
    }

    private void onProgressRefresh(int angle, boolean fromUser){
        updateProgress(angle, fromUser);
    }

    private void updateProgress(int angle, boolean fromUser){
        if(onPrettySeekBarChangeListener != null){
            onPrettySeekBarChangeListener.onProgressChanged(this, angle, fromUser);
        }

        angle = (angle < 0) ? -angle : angle;
        point = angle;

        //Log.d("Adhithayn", "update progress for angle:" + point);
        //invalidate();
    }

    private void setPointRange(int x, int y){
        circleMaxX = Math.max(x, circleMaxX);
        circleMaxY = Math.max(y, circleMaxY);

        circleMinX = Math.min(x, circleMinX);
        circleMinY = Math.min(y, circleMinY);
    }
}
