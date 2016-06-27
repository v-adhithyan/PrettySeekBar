package com.avtechlabs.prettyseekbar;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by adhithyan-3592 on 17/05/16.
 */

public class PrettySeekBar extends View{
    //----- variable and function declaration starts here -----
    //the outer circle color
    private int outerCircleFillColor;

    //the inner circle color
    private int innerCircleFillColor;

    //the radius of outer circle
    private int outerCircleRadius;

    //the radius of inner circle
    private int innerCircleRadius;

    //to parse the layout from client
    private TypedArray array;

    //paint to  draw outer circle
    private Paint outerCirclePainter;

    //paint draw inner circle
    private Paint innerCirclePainter;

    //paint to draw the clock hand or (the progress updater)
    private Paint progressPainter;

    //variable to hold maximum progress which also helps in calculating sleep time before moving clock hand
    //default is 100
    //can be set via layout using namespace:maxProgress
    //or
    //programatically set using setMaxProgress(progressValue) function
    private int maxProgress = 100;

    //looping variable
    private int i = -1;

    //during zoom in and zoom out, the radius value is adjusted and then circle is redrawn.This variable is used for that.
    private int radiusIncrementValue = 1;

    //clock hand moving can be paused dynamically. this variable is used to achieve the foresaid functionality
    AtomicBoolean makeProgress = new AtomicBoolean(false);

    //sleep time before moving clock hand
    int makeProgressTime = (int)(((double)maxProgress/(double)360) * 1000);

    //clock starts animating from 270 degress.
    private int point = 270;

    //the additional variable to stop moving clock hand, when it completes one cycle i.e., (270 + 360_
    private int currentProgress = 270;

    //variables to store points on the circle, assuming 360 points in circle.
    //arrays to hold the points on the circle.
    //this is used to draw a line from center of circle to a point on a circle
    //the length of line is equal to radius of circle.
    private int[] x;
    private int[] y;

    //dynamically pause the clock hand moving
    private boolean pause = true;

    //variables to track current min and max x,y point on the circle
    //so that any touch outside those points will be marked invalid and touch will be ignored.
    private int circleMaxX = Integer.MIN_VALUE;
    private int circleMaxY = Integer.MIN_VALUE;
    private int circleMinX = Integer.MAX_VALUE;
    private int circleMinY = Integer.MAX_VALUE;

    //to pause the zoom in and zoom out functionality
    private boolean pauseZoom = false;

    //listener for seekbar
    public interface  OnPrettySeekBarChangeListener{
        void onProgressChanged(PrettySeekBar prettySeekBar, int progress, boolean touched);

        void onStartTrackingTouch(PrettySeekBar seekBar);

        void onStopTrackingTouch(PrettySeekBar seekBar);
    }

    public boolean mEnabled = true;

    private OnPrettySeekBarChangeListener onPrettySeekBarChangeListener;

    //----- variable and function declaration ends here -----


    public PrettySeekBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);

        /**
         *  The zoom in and zoom out effect of circle is achieved by this thread.
         */

        Thread zoomerThread = new Thread(new Runnable() {
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
         *  The analog clock like animation is achieved by this thread.
         */


        Thread clockHandMoverThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while(true){
                    try {
                        Thread.sleep(makeProgressTime);


                        if(currentProgress < 630){
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

        //start zo
        zoomerThread.start();
        clockHandMoverThread.start();
    }

    //called from above constructor
    private void init(Context context, AttributeSet attrs){
        //getting layout from calling activity
        array = context.getTheme().obtainStyledAttributes(attrs, R.styleable.PrettySeekBar, 0, 0);

        //from layout parse the attributes
        //only 3 attributes are available as of now and they are optional
        //List of attributes and their default values
        //outer circle fill color defaults to black
        //inner circle fill color defaults to white
        //max progress defaults to 100

        try{
            outerCircleFillColor = array.getInteger(R.styleable.PrettySeekBar_outerCircleFillColor, R.color.black);
            innerCircleFillColor = array.getInteger(R.styleable.PrettySeekBar_innerCircleFillColor, R.color.white);
            maxProgress = array.getInteger(R.styleable.PrettySeekBar_maxProgress, 100);

        }finally {
            array.recycle();
        }

        //create outer circle paint object with outer circle fill color
        outerCirclePainter = new Paint();
        outerCirclePainter.setStyle(Paint.Style.FILL);
        outerCirclePainter.setAntiAlias(true);
        outerCirclePainter.setColor(outerCircleFillColor);

        //create inner circle paint object with inner circle fill color.
        innerCirclePainter = new Paint();
        innerCirclePainter.setStyle(Paint.Style.FILL);
        innerCirclePainter.setAntiAlias(true);
        innerCirclePainter.setColor(innerCircleFillColor);

        //create the clock hand object with outer circle fill color.
        progressPainter = new Paint();
        progressPainter.setStyle(Paint.Style.STROKE);
        progressPainter.setAntiAlias(true);
        progressPainter.setStrokeCap(Paint.Cap.BUTT);
        progressPainter.setStrokeWidth(10);
        progressPainter.setColor(outerCircleFillColor);

        //initialize array to hold points on circle.
        //length is 360, since 360 points on circle. (2PI)
        x = new int[360];
        y = new int[360];

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec){
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    //draws both circles and the clock hand
    @Override
    protected void onDraw(Canvas canvas){
        int viewWidthHalf = this.getMeasuredWidth() / 2;
        int viewHeightHalf = this.getMeasuredHeight() / 2;

        //calculate outer and inner circle radius before drawing it.
        calculateRadius(viewWidthHalf, viewHeightHalf);
        canvas.drawCircle(viewWidthHalf, viewHeightHalf, outerCircleRadius, outerCirclePainter);
        canvas.drawCircle(viewWidthHalf, viewHeightHalf, innerCircleRadius, innerCirclePainter);

        //we need x and y co-ordinates to draw line
        //we already have 360 pair of x and y points on the circle in x[] and y[]
        //so, we just need to draw a pair of x and y
        //the point is initialized to 270
        //because if point is initialized to 0, the clock hand will be drawn horizontally, since center of circle is taken to be origin, x[0] and y[0] will draw a horizontal hand from center of circle.
        //hence, we are setting the point to 270 and drawing the vertical hand and making it appear as in real world.
        //if makeProgress is paused we don't need to move clock hand, since we can pause the clock hand progress from main activity using pauseProgress() and makeProgress methods
        //so we are checking if make progress is allowed and incrementing point and redrawing the layout so that clock hand moving animation is achieved.
        if(makeProgress.get() && !pause){
                point = (point + 1 == 360) ? 0 : point + 1;
                makeProgress.set(false);
        }
        canvas.drawLine(viewWidthHalf, viewHeightHalf, x[point], y[point], progressPainter);

    }

    //before zoom in or zoom out,circle radius has to be calculated. This function is used to calculate the radius and redraw circles which when run in a thread achieves the zoom effect.
    //zoom out will happen 10 times
    //zoom in will happen 10 times

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

        //since the outer circle radius changes, we need current points on circle. so we are calculating current points on circle using this method
        calculatePointsOnCircle(viewWidthHalf, viewHeightHalf);

    }

    //set max progress from calling activity
    //max progress will be used to calculate sleep time
    //clock hand animater thread will sleep to above time before moving clock hand.
    //we are doing this to evenly achieve the move animation so that it completes one rotation within max progress time i.e., 360 degrees.
    public int setMaxProgress(int maxProgress){
        this.maxProgress = maxProgress;
        double time = ((double)maxProgress / (double)360) * 1000;

        makeProgressTime = (int)time;
        return makeProgressTime;
    }

    //calculating points on outer circle so that clock hand can be drawn from center to a point
    private void calculatePointsOnCircle(int xorigin, int yorigin){
        double angle = 0d;


        for(int j = 0; j < 360; j++){
            x[j] = (int) ((xorigin) + (outerCircleRadius * Math.cos(angle * Math.PI / 180)));
            y[j] = (int) ((yorigin) + (outerCircleRadius * Math.sin(angle * Math.PI / 180)));

            //set the max and min point range, so that touch outside these points can be ignored.
            setPointRange(x[j], y[j]);

            angle += 1d;
        }
    }

    //call from a activity to dynamically pause clock hand
    public void pauseProgress(){
        pause = true;
    }

    //call from a activity to dynamically start clock hand if paused previously
    public void makeProgress(){
        pause = false;
    }

    //handle touch event.Currently moving along the circle is only supported.
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

    //transfer the listener back to activity if attached.
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

    //in case of touch event and if its a move along the circle, this function will be notified.

    private void updateOnTouch(MotionEvent event){
        //if touch is outside the max and min point of circle i.e if touch is not within the circle, ignore and return
        boolean ignoreTouch = ignoreTouch(event.getX(), event.getY());
        if(ignoreTouch){
            return;
        }
        setPressed(true);

        //if the touch is along the circle, update the progress
        updateProgress(event.getX(), event.getY());

    }

    //checks if the current touch is outside the outer circle. if so the touch is ignored.
    private boolean ignoreTouch(float xPos, float yPos){
        int x = Math.round(xPos), y = Math.round(yPos);

        if(x >= circleMinX && x <= circleMaxX){
            if(y >= circleMinY && y <= circleMaxY)
                return false;
        }
        
        return true;
    }

    //get the co-ordinates from touch.
    //iterate the points on the circle.
    //if a point matches, move the clock hand to that point and update the progress value and notify any listeners
    private void updateProgress(float xPos, float yPos){
        pauseZoom = true;

        for(int i=0; i<360; i++){
            int xdiff = Math.abs((int)(x[i] - xPos));
            int ydiff = Math.abs((int)(y[i] - yPos));
            if(xdiff <= 10 && ydiff <= 10) {
                point = i;
                updateCurrentProgress(i);
                if(!makeProgress.get())
                    makeProgress.set(true);
                invalidate();
                break;
            }
        }

        pauseZoom = false;
    }

    //set the max and min point ranges of the circle, so that any values outside these ranges can be ignored when touched.
    private void setPointRange(int x, int y){
        circleMaxX = Math.max(x, circleMaxX);
        circleMaxY = Math.max(y, circleMaxY);

        circleMinX = Math.min(x, circleMinX);
        circleMinY = Math.min(y, circleMinY);
    }

    //get the matching point for touch and move the clock hand
    private void updateCurrentProgress(int point){
        //since clock starts animating from point 270, we are adjusting relative to it
        int adjustToStart = (point <= 270) ? (point + 90) : (point - 270);
        adjustToStart = (adjustToStart == 360) ? 0 : adjustToStart;
        int actualProgress = (adjustToStart * makeProgressTime) / 1000;

        currentProgress = 270 + adjustToStart;
        onProgressChanged(this, actualProgress, true);
    }

}
