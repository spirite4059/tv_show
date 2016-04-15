package com.gochinatv.ad.ui.view;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Scroller;
import android.widget.TextView;

import com.gochinatv.ad.R;
import com.gochinatv.ad.tools.LogCat;
import com.okhtttp.response.AdImgResponse;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by zfy on 2016/3/29.
 */
public class RecycleUpAnimationView extends LinearLayout {


    private Context mContext;
    private Scroller mScroller;
    private LayoutInflater layoutInflater;
    private ObjectAnimator objectAnimator;//



    private int itemWidth;//item的宽

    private int itemHeight;//item的高

    private boolean isStopRecycleAnimation;//是否停止了滚动动画


    private int duration = 2;//动画时间（秒）


    private Timer recycleTimer;//动画计时器
    private int secondTime = 1;//每隔多少秒执行一次动画(分)


    private ArrayList<AdImgResponse> imgResponses;//数据集合

    private int position = 2;//当前是imgResponses的位置




    @Override
    protected boolean checkLayoutParams(ViewGroup.LayoutParams p) {
        return super.checkLayoutParams(p);
    }

    public RecycleUpAnimationView(Context context) {
        this(context, null);
        this.mContext = context;
        mScroller = new Scroller(mContext);
        layoutInflater = LayoutInflater.from(mContext);
    }

    public RecycleUpAnimationView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mContext = context;
        mScroller = new Scroller(mContext);
        layoutInflater = LayoutInflater.from(mContext);
    }

    public RecycleUpAnimationView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.mContext = context;
        mScroller = new Scroller(mContext);
        layoutInflater = LayoutInflater.from(mContext);
    }


    public void initView(ArrayList<AdImgResponse> list){
        this.imgResponses = list;
        int seiz = imgResponses.size();
        LogCat.e("图片广告的个数 seiz : " + seiz);
        if(seiz == 2){
            for(int i=0;i<2;i++){
                View view = layoutInflater.inflate(R.layout.itme_ad_three, this, false);
                LinearLayout.LayoutParams params = (LayoutParams) this.getLayoutParams();
                params.width = itemWidth;
                params.height = itemHeight;
                view.setLayoutParams(params);
                TextView name = (TextView)view.findViewById(R.id.ad_three_text_name);
                name.setText(imgResponses.get(i).adImgName);
                TextView price = (TextView)view.findViewById(R.id.ad_three_text_price);
                price.setText(imgResponses.get(i).adImgPrice+"元");
                this.addView(view,i);
            }

            //空view，以备复用
            View view = layoutInflater.inflate(R.layout.itme_ad_three, this, false);
            LinearLayout.LayoutParams params = (LayoutParams) this.getLayoutParams();
            params.width = itemWidth;
            params.height = itemHeight;
            view.setLayoutParams(params);
            this.addView(view,2);


        }else if(seiz >2){
            for(int i=0;i<3;i++){
                View view = layoutInflater.inflate(R.layout.itme_ad_three, this, false);
                TextView name = (TextView)view.findViewById(R.id.ad_three_text_name);
                name.setText(imgResponses.get(i).adImgName);
                TextView price = (TextView)view.findViewById(R.id.ad_three_text_price);
                price.setText(imgResponses.get(i).adImgPrice+"元");
                this.addView(view,i);
            }

            recycleTimer = new Timer();
            recycleTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    position ++;
                    LogCat.e("当前 position : " + position);
                    Message message = new Message();
                    message.what = 1;
                    handler.sendMessage(message);
                }
            },secondTime*5000,secondTime*5000);

        }else{
            return;
        }

    }




    Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            if (msg.what == 1) {
                recycleAnimation();
            }
            super.handleMessage(msg);
        }
    };



    /**
     * 向上滚动的动画
     */
    private void recycleAnimation(){

        LogCat.e(" 子view的个数："+  this.getChildCount());
        objectAnimator = new ObjectAnimator().ofFloat(this,"y",0f,-itemHeight).setDuration(duration*1000);

        objectAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                LogCat.e("#########  onAnimationStart :  动画开始了 ");
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                //动画结束后，要将最上面的view移动最下面复用
                moveViewToBottom();
                LogCat.e("######### onAnimationEnd :  动画结束了 ");
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });

        objectAnimator.start();
    }

    /**
     * 将最上面一个view移动最下面复用,translationY
     */
    private void moveViewToBottom(){
        final View reuseView = this.getChildAt(0);
        objectAnimator = new ObjectAnimator().ofFloat(reuseView,"y",-itemHeight,2*itemHeight).setDuration(1);

        objectAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                if (position > imgResponses.size()-1) {
                    position = 0;
                }
                TextView name = (TextView) reuseView.findViewById(R.id.ad_three_text_name);
                name.setText(imgResponses.get(position).adImgName);
                TextView price = (TextView) reuseView.findViewById(R.id.ad_three_text_price);
                price.setText(imgResponses.get(position).adImgPrice + "元");
                LogCat.e(" moveViewToBottom  position: " + position + "   菜名：   " +imgResponses.get(position).adImgName);
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                if (position > imgResponses.size()-1) {
                    position = 0;
                }
                TextView name = (TextView) reuseView.findViewById(R.id.ad_three_text_name);
                name.setText(imgResponses.get(position).adImgName);
                TextView price = (TextView) reuseView.findViewById(R.id.ad_three_text_price);
                price.setText(imgResponses.get(position).adImgPrice + "元");
            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        objectAnimator.start();


    }



    public ArrayList<AdImgResponse> getImgResponses() {
        return imgResponses;
    }

    /**
     * 设置数据
     * @param imgResponses
     */
    public void setImgResponses(ArrayList<AdImgResponse> imgResponses) {
        this.imgResponses = imgResponses;
    }


    /**
     * 是否停止滚动动画
     * @param isRecycleAnimation
     */
    public void setIsStopRecycleAnimation(boolean isRecycleAnimation) {
        this.isStopRecycleAnimation = isRecycleAnimation;
    }


    public int getItemHeight() {
        return itemHeight;
    }

    /**
     * 设置item的高
     * @param itemHeight
     */
    public void setItemHeight(int itemHeight) {
        LogCat.e(" itemHeight: "+ itemHeight);
        this.itemHeight = itemHeight;
    }

    public int getItemWidth() {
        return itemWidth;
    }

    /**
     *  设置item的宽
     * @param itemWidth
     */
    public void setItemWidth(int itemWidth) {
        LogCat.e(" itemWidth: "+ itemWidth);
        this.itemWidth = itemWidth;
    }

    public int getSecondTime() {
        return secondTime;
    }

    /**
     * 设置间隔时间（秒）
     * @param secondTime
     */
    public void setSecondTime(int secondTime) {
        this.secondTime = secondTime;
    }


    /**
     * 当有新数据时，要及时刷新
     * @param imgResponses
     */
    public void refreshDtaImgResponses(ArrayList<AdImgResponse> imgResponses){
        this.imgResponses = imgResponses;
        int size = imgResponses.size();
        if(size == 2){
            //此时只有2个广告，要停止轮播
            if(recycleTimer != null ){
                recycleTimer.cancel();
            }

        }else if(size >2){
            if(isStopRecycleAnimation){
                //动画停止了，再次开启
                if(recycleTimer != null){
                    //新创建timer并开启动画,将position置为2
                    position = 2;
                    recycleTimer = new Timer();
                    recycleTimer.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            position ++;
                            LogCat.e("当前 position : " + position);
                            Message message = new Message();
                            message.what = 1;
                            handler.sendMessage(message);
                        }
                    },secondTime*60000,secondTime*60000);
                }else{
                    //直接开启动画，将position置为2
                    position = 2;
                    recycleTimer.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            position ++;
                            LogCat.e("当前 position : " + position);
                            Message message = new Message();
                            message.what = 1;
                            handler.sendMessage(message);
                        }
                    },secondTime*60000,secondTime*60000);
                }


            }else{
                //动画没有停止，不做任何操作

            }

        }

    }


    /**
     * 停止滚动动画
     */
    public void stopRecycleAnimation(){
        if(recycleTimer != null ){
            recycleTimer.cancel();
        }
    }

    /**
     * 当页面销毁时，调用
     */
    public void destoryRecycleAnimation(){
        if(recycleTimer != null ){
            recycleTimer.cancel();
            recycleTimer = null;
        }

    }




}
