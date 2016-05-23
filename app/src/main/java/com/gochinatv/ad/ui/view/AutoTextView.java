package com.gochinatv.ad.ui.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Camera;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.FrameLayout;
import android.widget.TextSwitcher;
import android.widget.ViewSwitcher;

import com.gochinatv.ad.R;
import com.gochinatv.ad.tools.Constants;

/**
 * Created by zfy on 2016/3/17.
 */
public class AutoTextView extends TextSwitcher implements
        ViewSwitcher.ViewFactory{


    private float mHeight;
    private Context mContext;
    //mInUp,mOutUp分别构成向下翻页的进出动画
    private Rotate3dAnimation mInUp;
    private Rotate3dAnimation mOutUp;

    //mInDown,mOutDown分别构成向下翻页的进出动画
    private Rotate3dAnimation mInDown;
    private Rotate3dAnimation mOutDown;



    //控件的width
    private int viewWidth;
    //显示文字的长度
    private int textWidth;
    //Paint
    private Paint mPaint;


    //是否停止滑动
    //private boolean isStopping = true;

    //滑动的距离
    //private int scrollX;

    MarqueeTextView textView;

    public AutoTextView(Context context) {
        this(context, null);
        // TODO Auto-generated constructor stub
    }

    public AutoTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        // TODO Auto-generated constructor stub
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.auto3d);
        mHeight = a.getDimension(R.styleable.auto3d_textSize, R.dimen.d_24sp);//24

        a.recycle();
        mContext = context;
        init();
    }

    private void init() {
        // TODO Auto-generated method stub
        setFactory(this);
        mInUp = createAnim(-90, 0 , true, true);
        mOutUp = createAnim(0, 90, false, true);
        mInDown = createAnim(90, 0 , true , false);
        mOutDown = createAnim(0, -90, false, false);
        //TextSwitcher主要用于文件切换，比如 从文字A 切换到 文字 B，
        //setInAnimation()后，A将执行inAnimation，
        //setOutAnimation()后，B将执行OutAnimation
        setInAnimation(mInUp);
        setOutAnimation(mOutUp);
    }

    private Rotate3dAnimation createAnim(float start, float end, boolean turnIn, boolean turnUp){
        final Rotate3dAnimation rotation = new Rotate3dAnimation(start, end, turnIn, turnUp);
        rotation.setDuration(800);
        rotation.setFillAfter(false);
        rotation.setInterpolator(new AccelerateInterpolator());
        return rotation;
    }

    //这里返回的TextView，就是我们看到的View
    @Override
    public View makeView() {

         textView = new MarqueeTextView(mContext);
        // TODO Auto-generated method stub
        //TextView textView = new TextView(mContext);
        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        textView.setLayoutParams(lp);
        textView.setTextColor(Color.WHITE);
        if(Constants.isPhone){
            //适配手机
            textView.setTextSize(12);//mHeight
        }else{
            ///适配电视棒
            textView.setTextSize(mHeight);//mHeight
        }
        textView.setSingleLine();
        textView.setEllipsize(TextUtils.TruncateAt.MARQUEE);
        if(mPaint == null){
            mPaint = textView.getPaint();

        }
        textView.setViewWidth(viewWidth);
        return textView;
    }
    //定义动作，向下滚动翻页
    public void previous(){
        if(getInAnimation() != mInDown){
            setInAnimation(mInDown);
        }
        if(getOutAnimation() != mOutDown){
            setOutAnimation(mOutDown);
        }
    }
    //定义动作，向上滚动翻页
    public void next(){
        stopScroll();//停止滑动并且复位
        if(getInAnimation() != mInUp){
            setInAnimation(mInUp);
        }
        if(getOutAnimation() != mOutUp){
            setOutAnimation(mOutUp);
        }
    }


    @Override
    public void setText(CharSequence text) {
        super.setText(text);
        //获取文字宽度
        if(mPaint != null){
            textWidth = (int) mPaint.measureText((String)text);
            //LogCat.e("ADFourFragment"," viewWidth:" + viewWidth +"   textWidth:"+ textWidth);
        }
        if(textWidth > viewWidth){
            //LogCat.e("ADFourFragment"," 开启左右滑动1111111111111111111");
            startScroll();
        }else {
            stopScroll();
        }

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


    /**
     * 设置view的宽度
     * @param viewWidth
     */
    public void setViewWidth(int viewWidth) {
        this.viewWidth = viewWidth-this.getPaddingLeft() - this.getPaddingRight();
    }

    public int getViewWidth() {
        return viewWidth;
    }


    public void stopScroll(){
        if(textView != null){
            textView.stopScroll();
        }
    }

    public void startScroll(){
        if(textView != null){
            //LogCat.e("ADFourFragment"," 开启左右滑动222222222222222222222");
            textView.startScroll();
        }
    }


//    public void stopScroll() {
//        isStopping = true;
//        // 恢复初始状态
//        scrollX = 0;
//        scrollToX(0);
//        removeCallbacks(this);
//    }
//
//
//    private void scrollToX(int x) {
//        scrollTo(x, 0);

//    if (!isStopping) {
//        scrollX += 2;// 滚动速度
//        scrollTo(scrollX, 0);
//        if (scrollX > textWidth) {
//            scrollTo(0, 0);
//            scrollX = 0;
//        }
//        postDelayed(this, 50);
//    }


//    }





}
