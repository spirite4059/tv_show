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


    private boolean isStopping = false;

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


    /**
     * 获取文字宽度
     */
    private void getTextWidth() {
        Paint paint = this.getPaint();
        String str = this.getText().toString();
        textWidth = (int) paint.measureText(str);


    }

    @Override
    public void setText(CharSequence text, BufferType type) {
        super.setText(text, type);

        //startScroll();

    }

    private void scrollToX(int x) {
        scrollTo(x, 0);
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

    public void startScroll() {
        getTextWidth();
        isStopping = false;

        LogCat.e("ADFourFragment", "viewWidth:  " + viewWidth + " textWidth :  " +  textWidth);
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

}