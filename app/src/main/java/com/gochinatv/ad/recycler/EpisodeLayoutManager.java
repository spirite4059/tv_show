package com.gochinatv.ad.recycler;

import android.content.Context;
import android.graphics.PointF;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.LinearSmoothScroller;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LinearInterpolator;


/**
 * Created by fq_mbp on 16/1/25.
 */
public class EpisodeLayoutManager extends LinearLayoutManager {

    private SmoothScroller smoothScroller;
    private int orientation;
    private boolean isSmoothNext;
    private final int SCROLL_ANIM_DURATION = 400;
    private int divider;

    public EpisodeLayoutManager(Context context, int orientation) {
        super(context, orientation, false);
        this.orientation = orientation;
    }


    @Override
    public void onMeasure(RecyclerView.Recycler recycler, RecyclerView.State state, int widthSpec, int heightSpec) {
        super.onMeasure(recycler, state, widthSpec, heightSpec);
        int widthSize = View.MeasureSpec.getSize(widthSpec);
        int heightSize = View.MeasureSpec.getSize(heightSpec);


        View v = getChildAt(0);

        if(v != null){
            int height = v.getHeight();
            heightSize = (height + divider) * 5;
        }

        setMeasuredDimension(widthSize, heightSize);


    }

    public boolean smoothScrollTop(RecyclerView recyclerView, int position) {
        if (recyclerView == null) {
            Log.e("TAG", "recyleview is null");
            return false;
        }
//        Log.e("TAG", "targetPosition : " + position);
        isSmoothNext = true;
        if (smoothScroller == null) {
            smoothScroller = new SmoothScroller(recyclerView.getContext());
        }
        smoothScroller.setTargetPosition(position);
        startSmoothScroll(smoothScroller);
        return true;
    }


    public boolean smoothScrollBottom(RecyclerView recyclerView, int position) {
        if (recyclerView == null) {
            Log.e("TAG", "recyleview is null");
            return false;
        }
//        Log.e("TAG", "targetPosition : " + position);
        isSmoothNext = false;
        if (smoothScroller == null) {
            smoothScroller = new SmoothScroller(recyclerView.getContext());
        }
        smoothScroller.setTargetPosition(position);
        startSmoothScroll(smoothScroller);

        return true;
    }


    private class SmoothScroller extends LinearSmoothScroller {

        private static final String TAG = "LinearSmoothScroller";

        protected final LinearInterpolator mLinearInterpolator = new LinearInterpolator();

        protected final DecelerateInterpolator mDecelerateInterpolator = new DecelerateInterpolator();

        protected PointF mTargetVector;
        protected PointF mPointF;
        private static final int TARGET_SEEK_SCROLL_DISTANCE_PX = 10000;
        protected int mInterimTargetDx = 0, mInterimTargetDy = 0;
        private Context context;

        public SmoothScroller(Context context) {
            super(context);
            mPointF = new PointF();

            this.context = context;
        }

        @Override
        protected void onStop() {
            super.onStop();
        }


        //        @Override
//        protected float calculateSpeedPerPixel(DisplayMetrics displayMetrics) {
//            LogCat.e("calculateSpeedPerPixel..............................");
//            return super.calculateSpeedPerPixel(displayMetrics);
//        }
//
//        @Override
//        protected int calculateTimeForDeceleration(int dx) {
//            LogCat.e("calculateTimeForDeceleration..............................");
//            return super.calculateTimeForDeceleration(dx);
//        }
//
//        @Override
//        protected int calculateTimeForScrolling(int dx) {
//            LogCat.e("calculateTimeForScrolling..............................");
//            return super.calculateTimeForScrolling(dx);
//        }
//
//        @Override
//        protected int getHorizontalSnapPreference() {
//            LogCat.e("getHorizontalSnapPreference..............................");
//            return super.getHorizontalSnapPreference();
//        }
//
//        @Override
//        protected int getVerticalSnapPreference() {
//            LogCat.e("getVerticalSnapPreference..............................");
//            return super.getVerticalSnapPreference();
//        }
//
//        @Override
//        public int calculateDtToFit(int viewStart, int viewEnd, int boxStart, int boxEnd, int snapPreference) {
//            LogCat.e("calculateDtToFit..............................");
//            return super.calculateDtToFit(viewStart, viewEnd, boxStart, boxEnd, snapPreference);
//        }
//
//        @Override
//        public int calculateDyToMakeVisible(View view, int snapPreference) {
//            LogCat.e("calculateDyToMakeVisible..............................");
//            return super.calculateDyToMakeVisible(view, snapPreference);
//        }
//
//        @Override
//        public int calculateDxToMakeVisible(View view, int snapPreference) {
//            LogCat.e("calculateDxToMakeVisible..............................");
//            return super.calculateDxToMakeVisible(view, snapPreference);
//        }

        /**
         * When the target scroll position is not a child of the RecyclerView, this method calculates
         * a direction vector towards that child and triggers a smooth scroll.
         *
         * @see #computeScrollVectorForPosition(int)
         */

        protected void updateActionForInterimTarget(Action action) {

//            Log.e("TAG", "updateActionForInterimTarget........");
            // find an interim target position
//            LogCat.e("updateActionForInterimTarget: ");


//            LogCat.e("updateActionForInterimTarget: ++++++++++++++" );
            // 计算当前是往前移动还是往后移动
            PointF scrollVector = computeScrollVectorForPosition(getTargetPosition());
            if (scrollVector == null || (scrollVector.x == 0 && scrollVector.y == 0)) {
                Log.e(TAG, "To support smooth scrolling, you should override \n"
                        + "LayoutManager#computeScrollVectorForPosition.\n"
                        + "Falling back to instant scroll");
                final int target = getTargetPosition();
                action.jumpTo(target);
                stop();
                return;
            }
            normalize(scrollVector);
            mTargetVector = scrollVector;
            // 计算移动的距离
//            Log.e("TAG", "updateActionForInterimTarget........mTargetVector.x：" + scrollVector.x);
            mInterimTargetDx = (int) (TARGET_SEEK_SCROLL_DISTANCE_PX * scrollVector.x);
            mInterimTargetDy = (int) (TARGET_SEEK_SCROLL_DISTANCE_PX * scrollVector.y);
//            Log.e("TAG", "updateActionForInterimTarget........mInterimTargetDx：" + mInterimTargetDx);
            // 计算当前的移动时间
            final int time = calculateTimeForScrolling(TARGET_SEEK_SCROLL_DISTANCE_PX);
//            Log.e("TAG", "updateActionForInterimTarget........time：" + time);
            // To avoid UI hiccups, trigger a smooth scroll to a distance little further than the
            // interim target. Since we track the distance travelled in onSeekTargetStep callback, it
            // won't actually scroll more than what we need.
//            action.update((int) (mInterimTargetDx * 1.2f)
//                    , (int) (mInterimTargetDy * 1.2f)
//                    , time, mLinearInterpolator);
//            if(){
//
//            }

            action.update((isSmoothNext ? 100 : -100), (isSmoothNext ? 100 : -100), 100, mLinearInterpolator);
        }


        @Override
        public PointF computeScrollVectorForPosition(int targetPosition) {
//            Log.e("TAG", "computeScrollVectorForPosition........" + targetPosition);
            // 判断移动的方向
//            PointF p = ScrollingLinearLayoutManager.this
//                    .computeScrollVectorForPosition(targetPosition);
//            Log.e("TAG", "computeScrollVectorForPosition.........p.x: " + p.x);
//            return p;
            if (orientation == LinearLayoutManager.HORIZONTAL) {
                mPointF.x = isSmoothNext ? 1.0f : -1.0f;
            } else {
                mPointF.y = isSmoothNext ? 1.0f : -1.0f;
            }

//            Log.e("TAG", "computeScrollVectorForPosition.........p.x: " + mPointF.x);
//            Log.e("TAG", "computeScrollVectorForPosition.........p.y: " + mPointF.y);
            return mPointF;
        }


        /**
         * {@inheritDoc}
         */
        @Override
        protected void onTargetFound(View targetView, RecyclerView.State state, Action action) {
//            Log.e("TAG", "onTargetFound........tv: ");
            final int dx = calculateDxToMakeVisible(targetView, getHorizontalSnapPreference());
            final int dy = calculateDyToMakeVisible(targetView, getVerticalSnapPreference());
            final int distance = (int) Math.sqrt(dx * dx + dy * dy);
            final int time = calculateTimeForDeceleration(distance);

//            Log.e("TAG", "onTargetFound........dx：" + dx);
//            Log.e("TAG", "onTargetFound........dy：" + dy);
//            Log.e("TAG", "onTargetFound........distance：" + distance);
//            Log.e("TAG", "onTargetFound........time：" + time);
//


            if (time > 0) {
//                action.update(-dx, -dy, time, mDecelerateInterpolator);
                action.update(-dx, -dy, SCROLL_ANIM_DURATION, mDecelerateInterpolator);
//                LogCat.e("滑动状态恢复初始");
//                action.update(isSmoothNext ? itemWidth : -itemWidth, -dy, 600, mDecelerateInterpolator);
            }
        }

        public int calculateDyToMakeVisible(View view, int snapPreference) {
            final RecyclerView.LayoutManager layoutManager = getLayoutManager();
            if (!layoutManager.canScrollVertically()) {
                return 0;
            }


            final RecyclerView.LayoutParams params = (RecyclerView.LayoutParams)
                    view.getLayoutParams();
            final int top = layoutManager.getDecoratedTop(view) - params.topMargin;
            int bottom = layoutManager.getDecoratedBottom(view) + params.bottomMargin;
            final int start = layoutManager.getPaddingTop();
            final int end = layoutManager.getHeight() - layoutManager.getPaddingBottom();
//            LogCat.e("bottom0......... " + bottom);
//            bottom = offset;
//            LogCat.e("offset......... " + offset);
//            LogCat.e("end......... " + end);

            return calculateDtToFit(top, bottom, start, end, snapPreference);
        }

        /**
         * Helper method for {@link #calculateDxToMakeVisible(View, int)} and
         * {@link #calculateDyToMakeVisible(View, int)}
         */
        public int calculateDtToFit(int viewStart, int viewEnd, int boxStart, int boxEnd, int
                snapPreference) {
//            LogCat.e("snapPreference : " +  snapPreference);
            switch (snapPreference) {
                case SNAP_TO_START:
                    return boxStart - viewStart;
                case SNAP_TO_END:
                    return boxEnd - viewEnd;
                case SNAP_TO_ANY:
                    final int dtStart = boxStart - viewStart;
//                    LogCat.e("dtStart :" + dtStart);
                    if (dtStart > 0) {
                        return dtStart;
                    }
                    final int dtEnd = boxEnd - viewEnd;
//                    LogCat.e("dtEnd :" + dtEnd);
                    if (dtEnd < 0) {
                        return dtEnd;
                    }
                    break;
                default:
                    throw new IllegalArgumentException("snap preference should be one of the"
                            + " constants defined in SmoothScroller, starting with SNAP_");
            }
            return 0;
        }

    }


    private int offset;

    public void setSmoothOffset(int offset) {
        this.offset = offset;
    }

}
