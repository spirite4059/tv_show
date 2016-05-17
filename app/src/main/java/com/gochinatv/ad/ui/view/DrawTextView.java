package com.gochinatv.ad.ui.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.widget.TextView;

/**
 * Created by ulplanet on 2016/5/17.
 */
public class DrawTextView extends TextView {

    private String text = "qqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwqssssssssssssssssssssssssssssss";

//    public Paint getPaint() {
//        return paint;
//    }


    private Paint paint = getPaint();



    public DrawTextView(Context context) {
        super(context);
        init();
    }


    public DrawTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public DrawTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }


    private void init(){

        paint = new Paint(Paint.ANTI_ALIAS_FLAG);

        paint.setColor(Color.WHITE);
        paint.setTextSize(24);

    }



    @Override
    public void draw(Canvas canvas) {
        canvas.drawText(text,0,0,paint);
    }
}
