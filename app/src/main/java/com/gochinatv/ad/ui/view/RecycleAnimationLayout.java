package com.gochinatv.ad.ui.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
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

/**
 * Created by zfy on 2016/4/13.
 */
public class RecycleAnimationLayout extends LinearLayout {



    private Scroller mScroller;
    private LayoutInflater layoutInflater;
    private DisplayImageOptions options;
    private ImageLoader imageLoader;


    private int itemWidth;//item的宽
    private int itemHeight;//item的高

    private ArrayList<AdImgResponse> imgResponses;//数据集合
    private int position = 2;//当前是imgResponses的位置
    private int duration = 2;//动画时间（秒）



//    private Timer recycleTimer;//动画计时器
    private int secondTime = 5;//每隔多少秒执行一次动画(秒)


    private boolean isRecycle;//是否处于滚动状态

    private boolean isViewUp;//view是否滚动到上面去了

    private static  Handler handler;

    private Runnable recycleRunnable;

    private int oldDataSize;

    public RecycleAnimationLayout(Context context) {
        super(context);

    }

    public RecycleAnimationLayout(Context context, AttributeSet attrs) {
        super(context, attrs);

    }

    public RecycleAnimationLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

    }

    private void init(){
        mScroller = new Scroller(getContext());
        LogCat.e("RecycleAnimationLayout","11111111111111111111 ");
        layoutInflater = LayoutInflater.from(getContext());
        imageLoader = ImageLoader.getInstance();
        options = new DisplayImageOptions.Builder().cacheInMemory(false).cacheOnDisc(true)
                .imageScaleType(ImageScaleType.IN_SAMPLE_POWER_OF_2)
                .showImageOnLoading(R.drawable.ad_three_loading1)
                .showImageOnFail(R.drawable.ad_three_loading1).bitmapConfig(Bitmap.Config.RGB_565)
                .displayer(new FadeInBitmapDisplayer(1000, true, false, false)).build();

        handler = new Handler(Looper.getMainLooper());
    }

    /**
     * 初始化数据，并加载view
     * @param list
     */
    public void initView(ArrayList<AdImgResponse> list){
        if(list == null ||  list.size()==0){
            return;
        }
        init();
        this.imgResponses = list;
        int size = imgResponses.size();
        oldDataSize = size;
        LogCat.e("RecycleAnimationLayout","图片广告的个数 seiz : " + size);
        if(size == 2){
            for(int i=0;i<2;i++){
                View view = layoutInflater.inflate(R.layout.itme_ad_three, this, false);
                LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) view.getLayoutParams();
                params.width = itemWidth;
                params.height = itemHeight;
                view.setLayoutParams(params);
                TextView name = (TextView)view.findViewById(R.id.ad_three_text_name);
                name.setText(imgResponses.get(i).adImgName);
                TextView price = (TextView)view.findViewById(R.id.ad_three_text_price);
                if(!TextUtils.isEmpty(imgResponses.get(i).adImgPrice)){
                    price.setText(imgResponses.get(i).adImgPrice + "元");
                }else{
                    price.setText(imgResponses.get(i).adImgPrice);
                }

                ImageView pic = (ImageView)view.findViewById(R.id.ad_three_img);
                if("localPicture".equals(imgResponses.get(i).adImgUrl)){
                    imageLoader.displayImage("drawable://" + R.drawable.ad_three_loading1,pic,options);
                }else {
                    imageLoader.displayImage(imgResponses.get(i).adImgUrl,pic,options);
                }

                this.addView(view);
            }
            //留着复用
            View view1 = layoutInflater.inflate(R.layout.itme_ad_three, this, false);
            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) view1.getLayoutParams();
            params.width = itemWidth;
            params.height = itemHeight;
            view1.setLayoutParams(params);
            this.addView(view1);

        }else if(size >2){
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
            LogCat.e("RecycleAnimationLayout","2222222222222222 ");
            isRecycle = true;
            recycleRunnable = new RecycleRunnable();
            handler.postDelayed(recycleRunnable,secondTime*1000);


        }else{
            return;
        }

    }


    private class RecycleRunnable implements Runnable{
        @Override
        public void run() {
            scrollUp();
            position++;
            handler.postDelayed(recycleRunnable,secondTime*1000);
        }
    }

    /**
     * 向上滚动
     */
    private void scrollUp(){
        LogCat.e("RecycleAnimationLayout","开始向上滚动 scrollUp ");
        isViewUp = true;//将其置为滑动上面去了
        mScroller.startScroll(mScroller.getFinalX(), mScroller.getFinalY(), 0, itemHeight, duration * 1000);
        invalidate();


    }
    /**
     * 向下滚动，恢复初始位置
     */
    private void scrollDown(){
        LogCat.e("RecycleAnimationLayout","开始向下滚动 scrollDown ");
        isViewUp = false;//将其置为恢复原来状态了
        mScroller.startScroll(mScroller.getFinalX(), mScroller.getFinalY(), 0, -itemHeight, 0);
        invalidate();

    }

    @Override
    public void computeScroll() {
        // 调用startScroll的时候scroller.computeScrollOffset()返回true，
        if(mScroller != null){
            if(mScroller.computeScrollOffset()){
                //这里调用View的scrollTo()完成实际的滚动
                scrollTo(mScroller.getCurrX(), mScroller.getCurrY());
                //必须调用该方法，否则不一定能看到滚动效果
                postInvalidate();
            }else{
                //滑动完成
                if(isViewUp){
                    moveViewToBottom();
                }
            }
        }
        super.computeScroll();
    }


    /**
     * 将最上面一个view移动最下面复用,translationY
     */
    private void moveViewToBottom(){
        LogCat.e("RecycleAnimationLayout","将最顶的view移动动到最低 ");
        scrollDown();
        final View reuseView = this.getChildAt(0);
        LinearLayout.LayoutParams params4 = (LinearLayout.LayoutParams) reuseView.getLayoutParams();
        params4.width = itemWidth;
        params4.height = itemHeight;
        reuseView.setLayoutParams(params4);
        this.removeViewAt(0);
        this.addView(reuseView);
        LogCat.e("RecycleAnimationLayout"," 子view个数 getChildCount:  " + this.getChildCount());
        if (position > imgResponses.size()-1) {
            position = 0;
        }
        TextView name = (TextView) reuseView.findViewById(R.id.ad_three_text_name);
        name.setText(imgResponses.get(position).adImgName);
        TextView price = (TextView) reuseView.findViewById(R.id.ad_three_text_price);
        price.setText(imgResponses.get(position).adImgPrice + "元");
        ImageView pic = (ImageView)reuseView.findViewById(R.id.ad_three_img);
        imageLoader.displayImage(imgResponses.get(position).adImgUrl, pic, options);
        LogCat.e("RecycleAnimationLayout"," moveViewToBottom  position: " + position + "   菜名：   " + imgResponses.get(position).adImgName);

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
            if(size != oldDataSize){
                //刷新数据
                for(int i=0; i<2;i++){
                    View reuseView = this.getChildAt(i);
                    TextView name = (TextView)reuseView.findViewById(R.id.ad_three_text_name);
                    name.setText(imgResponses.get(i).adImgName);
                    TextView price = (TextView)reuseView.findViewById(R.id.ad_three_text_price);
                    if(!TextUtils.isEmpty(imgResponses.get(i).adImgPrice)){
                        price.setText(imgResponses.get(i).adImgPrice + "元");
                    }else{
                        price.setText(imgResponses.get(i).adImgPrice);
                    }
                    ImageView pic = (ImageView)reuseView.findViewById(R.id.ad_three_img);
                    if("localPicture".equals(imgResponses.get(i).adImgUrl)){
                        imageLoader.displayImage("drawable://" + R.drawable.ad_three_loading1,pic,options);
                    }else {
                        imageLoader.displayImage(imgResponses.get(i).adImgUrl,pic,options);
                    }
                }
            }else{
                LogCat.e("RecycleAnimationLayout"," 数据集合大小一样，不刷新数据 ");
            }


        }else if(size >2){

            if(isRecycle){
                //之前有滚动，此时不做任何操作
                isRecycle = true;
            }else{
                //之前没有滚动，开始滚动
                isRecycle = true;
                //先将第3个图片添加到第3个view里
                int last = this.getChildCount()-1;
                View reuseView = this.getChildAt(last);
                if(last<imgResponses.size()){
                    TextView name = (TextView)reuseView.findViewById(R.id.ad_three_text_name);
                    name.setText(imgResponses.get(last).adImgName);
                    TextView price = (TextView)reuseView.findViewById(R.id.ad_three_text_price);
                    if(!TextUtils.isEmpty(imgResponses.get(last).adImgPrice)){
                        price.setText(imgResponses.get(last).adImgPrice + "元");
                    }else{
                        price.setText(imgResponses.get(last).adImgPrice);
                    }
                    ImageView pic = (ImageView)reuseView.findViewById(R.id.ad_three_img);
                    if("localPicture".equals(imgResponses.get(last).adImgUrl)){
                        imageLoader.displayImage("drawable://" + R.drawable.ad_three_loading1,pic,options);
                    }else {
                        imageLoader.displayImage(imgResponses.get(last).adImgUrl,pic,options);
                    }
                }
                //开启滚动
                if(handler == null){
                    handler = new Handler(Looper.getMainLooper());
                }
                if(recycleRunnable == null){
                    recycleRunnable = new RecycleRunnable();
                }
                handler.postDelayed(recycleRunnable,secondTime*1000);

            }

        }else if(size == 0){
            //要停在滚动
            stopRecycleAnimation();
        }
        oldDataSize = size;
    }




    public int getItemHeight() {
        return itemHeight;
    }

    /**
     * 设置item的高
     * @param itemHeight
     */
    public void setItemHeight(int itemHeight) {
        LogCat.e("RecycleAnimationLayout"," itemHeight: "+ itemHeight);
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
        LogCat.e("RecycleAnimationLayout"," itemWidth: "+ itemWidth);
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
        LogCat.e("RecycleAnimationLayout"," 停在了广告三的滚动 ");
        if(isRecycle && isViewUp){
            LogCat.e("RecycleAnimationLayout"," 将父view的位置恢复了 ");
            scrollDown();//恢复到原来的位置
        }

        if(handler != null && recycleRunnable != null) {
            position = 2;//重置为2
            isRecycle = false;//将滚动状态置为:false
            handler.removeCallbacks(recycleRunnable);
        }

    }

    /**
     * 当页面销毁时，调用
     */
    public void destoryRecycleAnimation(){
        LogCat.e("RecycleAnimationLayout"," 取消了广告三的滚动 ");
        isRecycle = false;//将滚动状态置为:false

        position = 2;//重置为2
        isRecycle = false;//将滚动状态置为:false
        if(handler != null && recycleRunnable != null) {
            handler.removeCallbacks(recycleRunnable);
            recycleRunnable = null;
            handler = null;
        }

    }
}
