package com.gochinatv.ad.ui.view;

import android.content.Context;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.widget.TextView;

import com.gochinatv.ad.tools.LogCat;

/**
 * Created by ulplanet on 2016/5/18.
 */

public class MarqueeTextView extends TextView implements Runnable {


    private boolean isStopping = true;

    private int scrollX;

    private int textWidth;

    private int viewWidth;

    private boolean isMeasureText;


    public MarqueeTextView(Context context) {
        this(context, null);
    }

    public MarqueeTextView(Context context, AttributeSet attrs) {
        super(context, attrs);

    }

    public  boolean isStart;

    /**
     * 获取文字宽度
     */
    private void getTextWidth() {
        Paint paint = this.getPaint();
        String str = this.getText().toString();
        textWidth = (int) paint.measureText(str);
    }

    private void scrollToX(int x) {
        scrollTo(x, 0);
        //LogCat.e("ADFourFragment"," 开启左右滑动555555555555555555555  scrollToX  : "+x);
    }

    @Override
    public void setText(CharSequence text, BufferType type) {
        super.setText(text, type);
//        if(!TextUtils.isEmpty(text)){
//            getTextWidth();
//            LogCat.e("ADFourFragment"," setText   viewWidth:" + getWidth() + "       textWidth:  "+ textWidth +  "         " +text);
//            if(textWidth > viewWidth){
//                scrollX = 0;
//                removeCallbacks(this);
//                isStopping = false;
//                postDelayed(this, 1000);
//            }
//        }else{
//            LogCat.e("ADFourFragment"," setText   text为空  …………………………………………………………………………");
//        }
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
        if (getScrollX() >= textWidth) {
            int scrollReStartX = -(getWidth());
            scrollToX(scrollReStartX);
            scrollX = scrollReStartX;
        }
        postDelayed(this, 50);
    }

    public void startScroll() {
//        if(!isStopping){
//            removeCallbacks(this);
//            LogCat.e("ADFourFragment","removeCallbacks 之前的滑动！！！！！！！！！！！！！！！！！");
//        }
        removeCallbacks(this);
        getTextWidth();
        isStopping = false;
        scrollX = 0;
        postDelayed(this, 2000);
    }


    public void stopScroll() {
//        if(!isStopping){
//            removeCallbacks(this);
//            LogCat.e("ADFourFragment","removeCallbacks 之前的滑动！！！！！！！！！！！！！！！！！");
//        }
        removeCallbacks(this);
        isStopping = true;
        // 恢复初始状态
        if( scrollX != 0){
            scrollX = 0;
            //scrollToX(0);
            LogCat.e("ADFourFragment"," 还原之前已经滑动的距离！！！！！！！！！！！！！！");
        }

    }

    /**
     * 取消上一次的滚动
     */
//    public void stopScroll(){
//        scrollX = 0;
//
//        removeCallbacks(this);
//        isStopping = true;
//    }



    public void setViewWidth(int width) {
        this.viewWidth = width;

    }

    private int getViewWidth() {
        int width = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
        int height = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
        this.measure(width,height);
        //int height=this.getMeasuredHeight();
        return this.getMeasuredWidth();
    }




}