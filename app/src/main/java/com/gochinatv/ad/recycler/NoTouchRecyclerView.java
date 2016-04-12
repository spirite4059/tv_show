package com.gochinatv.ad.recycler;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.MotionEvent;

/**
 * Created by fq_mbp on 16/1/18.
 */
public class NoTouchRecyclerView extends RecyclerView {

    private boolean isScrollable = true;

    public NoTouchRecyclerView(Context context) {
        this(context, null);
    }

    public NoTouchRecyclerView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (isScrollable == false) {
            return false;
        } else {
            //return super.onTouchEvent(ev);
            return true;
        }

    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (isScrollable == false) {
            return false;
        } else {
            //return super.onInterceptTouchEvent(ev);
            return true;
        }

    }

    public boolean isScrollable() {
        return isScrollable;
    }

    public void setScrollable(boolean isScrollable) {
        this.isScrollable = isScrollable;
    }

}
