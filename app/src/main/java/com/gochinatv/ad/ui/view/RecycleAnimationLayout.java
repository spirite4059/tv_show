package com.gochinatv.ad.ui.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Scroller;
import android.widget.TextView;

import com.gochinatv.ad.R;
import com.gochinatv.ad.tools.LogCat;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.okhtttp.response.AdImgResponse;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by zfy on 2016/4/13.
 */
public class RecycleAnimationLayout extends LinearLayout {



    private Context mContext;
    private Scroller mScroller;
    private LayoutInflater layoutInflater;
    private DisplayImageOptions options;
    private ImageLoader imageLoader;


    private int itemWidth;//item的宽
    private int itemHeight;//item的高

    private ArrayList<AdImgResponse> imgResponses;//数据集合
    private int position = 2;//当前是imgResponses的位置
    private int duration = 2;//动画时间（秒）


    private Timer scrollDownTimer;//计时器--将位置还原
    private int scrollDownDuration = 5;//每隔多少秒执行一次动画(秒)

    private Timer recycleTimer;//动画计时器
    private int secondTime = 15;//每隔多少秒执行一次动画(秒)


    private boolean isRecycle;//是否处于滚动状态

    public RecycleAnimationLayout(Context context) {
        super(context);
        init(context);
    }

    public RecycleAnimationLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mContext = context;
        init(context);
    }

    public RecycleAnimationLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context){
        this.mContext = context;
        mScroller = new Scroller(context);
        layoutInflater = LayoutInflater.from(mContext);
        imageLoader = ImageLoader.getInstance();
        options = new DisplayImageOptions.Builder().cacheInMemory(false).cacheOnDisc(true)
                .imageScaleType(ImageScaleType.IN_SAMPLE_POWER_OF_2)
                .showImageOnLoading(R.drawable.ad_three_loading1)
                .showImageOnFail(R.drawable.ad_three_loading1).bitmapConfig(Bitmap.Config.RGB_565)
                .displayer(new FadeInBitmapDisplayer(1000, true, false, false)).build();
    }

    /**
     * 初始化数据，并加载view
     * @param list
     */
    public void initView(ArrayList<AdImgResponse> list){
        this.imgResponses = list;
        int seiz = imgResponses.size();
        LogCat.e("图片广告的个数 seiz : " + seiz);
        if(seiz == 2){
            for(int i=0;i<2;i++){
                View view = layoutInflater.inflate(R.layout.itme_ad_three, this, false);
                LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) view.getLayoutParams();
                params.width = itemWidth;
                params.height = itemHeight;
                view.setLayoutParams(params);
                TextView name = (TextView)view.findViewById(R.id.ad_three_text_name);
                name.setText(imgResponses.get(i).adImgName);
                TextView price = (TextView)view.findViewById(R.id.ad_three_text_price);
                price.setText(imgResponses.get(i).adImgPrice+"元");
                ImageView pic = (ImageView)view.findViewById(R.id.ad_three_img);
                imageLoader.displayImage(imgResponses.get(i).adImgUrl,pic,options);
                this.addView(view);
            }
            //留着复用
            View view1 = layoutInflater.inflate(R.layout.itme_ad_three, this, false);
            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) view1.getLayoutParams();
            params.width = itemWidth;
            params.height = itemHeight;
            view1.setLayoutParams(params);
            this.addView(view1);

        }else if(seiz >2){
            for(int i=0;i<3;i++){
                View view = layoutInflater.inflate(R.layout.itme_ad_three, this, false);
                LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) view.getLayoutParams();
                params.width = itemWidth;
                params.height = itemHeight;
                view.setLayoutParams(params);
                TextView name = (TextView)view.findViewById(R.id.ad_three_text_name);
                name.setText(imgResponses.get(i).adImgName);
                TextView price = (TextView)view.findViewById(R.id.ad_three_text_price);
                price.setText(imgResponses.get(i).adImgPrice+"元");
                ImageView pic = (ImageView)view.findViewById(R.id.ad_three_img);
                imageLoader.displayImage(imgResponses.get(i).adImgUrl, pic, options);
                this.addView(view);
            }
            isRecycle = true;
            if(recycleTimer == null){
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
                },secondTime*1000,secondTime*1000);
            }


        }else{
            return;
        }

    }

    Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            if (msg.what == 1) {
                scrollUp();
            }else if(msg.what == 2){
                moveViewToBottom();
            }
            super.handleMessage(msg);
        }
    };

    /**
     * 向上滚动
     */
    private void scrollUp(){
        mScroller.startScroll(mScroller.getFinalX(), mScroller.getFinalY(), 0, itemHeight, duration * 1000);
        invalidate();

         if(scrollDownTimer == null) {
             scrollDownTimer = new Timer();
             scrollDownTimer.schedule(new TimerTask() {
                 @Override
                 public void run() {
                     Message message = new Message();
                     message.what = 2;
                     handler.sendMessage(message);
                 }
             },(duration+scrollDownDuration)*1000);
         }else{
             scrollDownTimer.schedule(new TimerTask() {
                 @Override
                 public void run() {
                     Message message = new Message();
                     message.what = 2;
                     handler.sendMessage(message);
                 }
             },(duration+scrollDownDuration)*1000);
         }

    }
    /**
     * 向下滚动，恢复初始位置
     */
    private void scrollDown(){
        mScroller.startScroll(mScroller.getFinalX(), mScroller.getFinalY(), 0, -itemHeight, 0);
        invalidate();

    }

    @Override
    public void computeScroll() {
        // 调用startScroll的时候scroller.computeScrollOffset()返回true，
        if(mScroller.computeScrollOffset()){
            //这里调用View的scrollTo()完成实际的滚动
            scrollTo(mScroller.getCurrX(), mScroller.getCurrY());
            //必须调用该方法，否则不一定能看到滚动效果
            postInvalidate();

        }
        super.computeScroll();
    }


    /**
     * 将最上面一个view移动最下面复用,translationY
     */
    private void moveViewToBottom(){
        LogCat.e("将最顶的view移动动到最低 ");
        scrollDown();
        final View reuseView = this.getChildAt(0);
        LinearLayout.LayoutParams params4 = (LinearLayout.LayoutParams) reuseView.getLayoutParams();
        params4.width = itemWidth;
        params4.height = itemHeight;
        reuseView.setLayoutParams(params4);
        this.removeViewAt(0);
        this.addView(reuseView);
        LogCat.e(" 子view个数 getChildCount:  " + this.getChildCount());
        if (position > imgResponses.size()-1) {
            position = 0;
        }
        TextView name = (TextView) reuseView.findViewById(R.id.ad_three_text_name);
        name.setText(imgResponses.get(position).adImgName);
        TextView price = (TextView) reuseView.findViewById(R.id.ad_three_text_price);
        price.setText(imgResponses.get(position).adImgPrice + "元");
        ImageView pic = (ImageView)reuseView.findViewById(R.id.ad_three_img);
        imageLoader.displayImage(imgResponses.get(position).adImgUrl, pic, options);
        LogCat.e(" moveViewToBottom  position: " + position + "   菜名：   " + imgResponses.get(position).adImgName);

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
        int size = imgResponses.size();
        if(size == 2){
            //要停在滚动
            stopRecycleAnimation();
        }else if(size >2){
            if(scrollDownTimer == null){
                //之前没有滚动，开始滚动



            }else{
                //之前有滚动，此时不做任何操作
            }

        }else if(size == 0){
            //要停在滚动
            stopRecycleAnimation();
            //

        }




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
     * 停止滚动动画
     */
    public void stopRecycleAnimation(){
        position = 2;//重置为2
        isRecycle = false;//将滚动状态置为:false
        if(recycleTimer != null ){
            recycleTimer.cancel();
            if(handler != null){
                handler.removeMessages(1);
            }

        }
        if(scrollDownTimer != null) {
            scrollDownTimer.cancel();
            if(handler != null){
                handler.removeMessages(2);
            }
        }
    }

    /**
     * 当页面销毁时，调用
     */
    public void destoryRecycleAnimation(){
        if(recycleTimer != null ){
            LogCat.e(" 取消了广告三的滚动 ");
            recycleTimer.cancel();
            recycleTimer = null;
            if(handler != null){
                handler.removeMessages(1);
            }
        }
        if(scrollDownTimer != null) {
            scrollDownTimer.cancel();
            scrollDownTimer = null;
            if(handler != null){
                handler.removeMessages(2);
            }
        }

    }
}
