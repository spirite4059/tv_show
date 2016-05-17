package com.gochinatv.ad.ui.view;

import android.content.Context;
import android.graphics.Camera;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.TextView;

import com.gochinatv.ad.tools.LogCat;

/**
 * Created by ulplanet on 2016/5/17.
 */
public class Auto3DTextView extends TextView implements Runnable{



    private float mHeight;
    private Context mContext;
    //mInUp,mOutUp分别构成向下翻页的进出动画
    private Rotate3dAnimation mInUp;
    private Rotate3dAnimation mOutUp;

    //mInDown,mOutDown分别构成向下翻页的进出动画
    //private Rotate3dAnimation mInDown;
    //private Rotate3dAnimation mOutDown;


    private boolean isStopping = false;

    private int scrollX;

    private int textWidth;

    private int viewWidth;

    private boolean isMeasureText;




    public Auto3DTextView(Context context) {
        super(context);
        mContext = context;
        init();
    }

    public Auto3DTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        init();
    }

    public Auto3DTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        init();

    }

    /**
     * 获取文字宽度
     */
    private void getTextWidth() {
        Paint paint = this.getPaint();
        String str = this.getText().toString();
        textWidth = (int) paint.measureText(str);


    }


    private void init() {
        // TODO Auto-generated method stub
        //setFactory(this);
        mInUp = createAnim(-90, 0 , true, true);
        mOutUp = createAnim(0, 90, false, true);
        //mInDown = createAnim(90, 0 , true , false);
        //mOutDown = createAnim(0, -90, false, false);
        //TextSwitcher主要用于文件切换，比如 从文字A 切换到 文字 B，
        //setInAnimation()后，A将执行inAnimation，
        //setOutAnimation()后，B将执行OutAnimation
        this.startAnimation(mInUp);
        this.startAnimation(mOutUp);
    }


    private Rotate3dAnimation createAnim(float start, float end, boolean turnIn, boolean turnUp){
        final Rotate3dAnimation rotation = new Rotate3dAnimation(start, end, turnIn, turnUp);
        rotation.setDuration(800);
        rotation.setFillAfter(false);
        rotation.setInterpolator(new AccelerateInterpolator());
        return rotation;
    }


    //定义动作，向下滚动翻页
//    public void previous(){
//        if(getInAnimation() != mInDown){
//            setInAnimation(mInDown);
//        }
//        if(getOutAnimation() != mOutDown){
//            setOutAnimation(mOutDown);
//        }
//    }
    //定义动作，向上滚动翻页
    public void next(){
        stopScroll();//停止滑动并且复位

        //mInUp.start();
        //mOutUp.start();

        this.startAnimation(mInUp);
        //this.startAnimation(mOutUp);
//        if(getInAnimation() != mInUp){
//            setInAnimation(mInUp);
//        }
//        if(getOutAnimation() != mOutUp){
//            setOutAnimation(mOutUp);
//        }
    }


    @Override
    public void setText(CharSequence text, BufferType type) {
        super.setText(text, type);

//        if(mPaint != null){
//            textWidth = (int) mPaint.measureText((String)text);
//
//        }
        getTextWidth();
        LogCat.e("ADFourFragment"," viewWidth:" + viewWidth +"   textWidth:"+ textWidth);
        if(textWidth > viewWidth){
            LogCat.e("ADFourFragment"," 开启左右滑动############");
            isStopping = false;
            new Thread(this).start();
        }else {
            removeCallbacks(this);
        }


    }

    @Override
    public void run() {

        if (isStopping == true) {
            removeCallbacks(this);
            scrollX = 0;
            isStopping = false;
            return;
        }
        scrollX += 2;
        scrollToX(scrollX);
//        getTextWidth();
        if (getScrollX() >= textWidth) {
            int scrollReStartX = -(getWidth());
            scrollToX(scrollReStartX);
            scrollX = scrollReStartX;
        }
        postDelayed(this, 50);
    }


    private void scrollToX(int x) {
        scrollTo(x, 0);
    }


    public void startScroll() {
        getTextWidth();
        isStopping = false;
        if (viewWidth >= textWidth) {
            return;
        }

        // 开始滚动
        scrollToX(0);
        removeCallbacks(this);
        scrollX = 0;
        if (getWidth() < textWidth) {
            postDelayed(this, 1000);
        }
    }


    public void stopScroll() {
        isStopping = true;
        // 恢复初始状态
        scrollX = 0;
        scrollToX(0);
        removeCallbacks(this);

    }

    public void setViewWidth(int width) {
        viewWidth = width;

    }

    class Rotate3dAnimation extends Animation {
        private final float mFromDegrees;
        private final float mToDegrees;
        private float mCenterX;
        private float mCenterY;
        private final boolean mTurnIn;
        private final boolean mTurnUp;
        private Camera mCamera;

        public Rotate3dAnimation(float fromDegrees, float toDegrees, boolean turnIn, boolean turnUp) {
            mFromDegrees = fromDegrees;
            mToDegrees = toDegrees;
            mTurnIn = turnIn;
            mTurnUp = turnUp;
        }

        @Override
        public void initialize(int width, int height, int parentWidth, int parentHeight) {
            super.initialize(width, height, parentWidth, parentHeight);
            mCamera = new Camera();
            mCenterY = getHeight() / 2;
            mCenterX = getWidth() / 2;
            LogCat.e("ADFourFragment"," mCenterY############  " + mCenterY);

        }

        @Override
        protected void applyTransformation(float interpolatedTime, Transformation t) {
            final float fromDegrees = mFromDegrees;
            float degrees = fromDegrees + ((mToDegrees - fromDegrees) * interpolatedTime);

            final float centerX = mCenterX ;
            final float centerY = mCenterY ;
            final Camera camera = mCamera;
            final int derection = mTurnUp ? 1: -1;

            final Matrix matrix = t.getMatrix();

            camera.save();
            if (mTurnIn) {
                camera.translate(0.0f, derection *mCenterY * (interpolatedTime - 1.0f), 0.0f);
            } else {
                camera.translate(0.0f, derection *mCenterY * (interpolatedTime), 0.0f);
            }
            camera.rotateX(degrees);
            camera.getMatrix(matrix);
            camera.restore();

            matrix.preTranslate(-centerX, -centerY);
            matrix.postTranslate(centerX, centerY);
        }
    }

}
