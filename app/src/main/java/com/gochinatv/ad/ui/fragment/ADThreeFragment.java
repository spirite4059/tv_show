package com.gochinatv.ad.ui.fragment;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.gochinatv.ad.R;
import com.gochinatv.ad.base.BaseFragment;
import com.gochinatv.ad.tools.Constants;
import com.gochinatv.ad.tools.DataUtils;
import com.gochinatv.ad.tools.LogCat;
import com.gochinatv.ad.ui.view.RecycleAnimationLayout;
import com.okhtttp.OkHttpCallBack;
import com.okhtttp.response.AdImgResponse;
import com.okhtttp.response.AdThreeDataResponse;
import com.okhtttp.response.LayoutResponse;
import com.okhtttp.service.ADHttpService;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by zfy on 2016/3/16.
 */
public class ADThreeFragment extends BaseFragment {

    //private SliderLayout mDemoSlider;

    private RecycleAnimationLayout linearLayout;
    //private RecycleUpAnimationView linearLayout;
    private ImageView adThreeBg;
    private ArrayList<AdImgResponse> imgResponses;

    public LayoutResponse getLayoutResponse() {
        return layoutResponse;
    }

    public void setLayoutResponse(LayoutResponse layoutResponse) {
        this.layoutResponse = layoutResponse;
    }

    //布局参数
    private LayoutResponse layoutResponse;

    //轮询间隔请求接口
    private int getImgADTime = 3*60*1000;//默认的是3个分钟
    private Timer getImgADTimer;//定时器
    private boolean isFirstDoHttp = true;//是否第一次请求




    @Override
    protected View initLayout(LayoutInflater inflater, ViewGroup container) {

        RelativeLayout layout = (RelativeLayout) inflater.inflate(R.layout.fragment_ad_three,container,false);

        if(layoutResponse != null){

            if(!TextUtils.isEmpty(layoutResponse.adWidth) && !TextUtils.isEmpty(layoutResponse.adHeight)
                    && !TextUtils.isEmpty(layoutResponse.adTop) && !TextUtils.isEmpty(layoutResponse.adLeft)){

                String widthStr = layoutResponse.adWidth;
                String heightStr = layoutResponse.adHeight;
                String topStr = layoutResponse.adTop;
                String leftStr = layoutResponse.adLeft;

                //动态布局
                RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,RelativeLayout.LayoutParams.WRAP_CONTENT);
                double width = (float) (DataUtils.getDisplayMetricsWidth(getActivity())*(Float.parseFloat(widthStr)));
                double height = (float) (DataUtils.getDisplayMetricsHeight(getActivity())*(Float.parseFloat(heightStr)));
                double top = (float) (DataUtils.getDisplayMetricsHeight(getActivity())*(Float.parseFloat(topStr)));
                double left = (float) (DataUtils.getDisplayMetricsWidth(getActivity())*(Float.parseFloat(leftStr)));

                params.width = (int) Math.floor(width);
                params.height = (int) Math.floor(height);
                params.topMargin = (int) Math.floor(top);

                params.leftMargin = (int) Math.floor(left);
                layout.setLayoutParams(params);
                LogCat.e(" 广告三布局 width: "+params.width+" height: "+params.height+" top: "+params.topMargin+" left: "+params.leftMargin);

            }
        }


        return layout;
    }

    @Override
    protected void initView(View rootView) {
        linearLayout = (RecycleAnimationLayout) rootView.findViewById(R.id.ad_three_lin);
        adThreeBg = (ImageView) rootView.findViewById(R.id.ad_three_bg_image);
    }

    @Override
    protected void init() {
        getImgADTime = Constants.isImageTest? 20*1000:3*60*1000;
        double width = (float) (DataUtils.getDisplayMetricsWidth(getActivity())*0.16875f);
        double height = (float) (DataUtils.getDisplayMetricsHeight(getActivity())*0.6f);
        linearLayout.setItemWidth((int) Math.floor(width));
        linearLayout.setItemHeight((int) Math.floor(height / 2));
        LogCat.e("width: " + (int) Math.floor(width) + "     height:" + (int) Math.floor(height / 2));
        initData();



    }


    protected void initData() {

        ADHttpService.doHttpGetImageADInfo(getActivity(), new OkHttpCallBack<AdThreeDataResponse>() {
            @Override
            public void onSuccess(String url, AdThreeDataResponse response) {
                if (!isAdded()) {
                    return;
                }
                LogCat.e("RecycleAnimationLayout"," 广告三 url " + url);
                if (response == null || !(response instanceof AdThreeDataResponse)) {
                    //再次请求
                    initData();
                    return;
                }

                if (response.data == null || !(response.data instanceof ArrayList)) {
                    //再次请求
                    LogCat.e(" !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
                    initData();
                    return;
                }
                imgResponses = response.data;
                //设置滚动间隔
                if (response.adImgInterval != 0) {
                    linearLayout.setSecondTime(response.adImgInterval);
                }

                int totalSize = imgResponses.size();
                if (totalSize == 0) {
                    LogCat.e(" totalSize == 0  totalSize == 0 没有广告图片");
                    if (!isFirstDoHttp) {
                        if (adThreeBg != null && adThreeBg.getVisibility() == View.GONE) {
                            //没有数据显示背景图
                            adThreeBg.setVisibility(View.VISIBLE);
                            adThreeBg.setAlpha(1.0f);
                            //停在滚动
                            linearLayout.stopRecycleAnimation();
                        }
                    }
                } else if (totalSize == 1) {
                    AdImgResponse adImgResponse = new AdImgResponse();
                    adImgResponse.adImgName = "";
                    adImgResponse.adImgPrice = "";
                    adImgResponse.adImgUrl = "localPicture";
                    imgResponses.add(adImgResponse);
                    if(isFirstDoHttp){
                        //第一次请求
                        hideBGImage();
                    }else{
                        if(adThreeBg != null && adThreeBg.getVisibility() == View.VISIBLE){
                            hideBGImageTwo();
                        }else{
                            linearLayout.setImgResponses(imgResponses);//设置数据
                        }
                    }

                } else {
                    if(isFirstDoHttp){
                        //第一次请求
                        hideBGImage();
                    }else{
                        if(adThreeBg != null && adThreeBg.getVisibility() == View.VISIBLE){
                            hideBGImageTwo();
                        }else{
                            linearLayout.setImgResponses(imgResponses);//设置数据
                        }
                    }
                }

                if (isFirstDoHttp) {
                    if (getImgADTimer == null) {
                        LogCat.e(" 开启广告三的轮询接口 ！！！！");
                        getImgADTimer = new Timer();
                        getImgADTimer.schedule(new TimerTask() {
                            @Override
                            public void run() {
                                initData();
                            }
                        }, getImgADTime, getImgADTime);
                    }
                }
                isFirstDoHttp = false;
            }

            @Override
            public void onError(String url, String errorMsg) {
                if (!isAdded()) {
                    return;
                }
                initData();
            }
        });

    }





    @Override
    public void onStop() {
        super.onStop();
        if(linearLayout != null ){
            linearLayout.destoryRecycleAnimation();
        }

        if(getImgADTimer != null){
            getImgADTimer.cancel();
            getImgADTimer = null;
        }

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(linearLayout != null ){
            linearLayout.destoryRecycleAnimation();
        }

        if(getImgADTimer != null){
            getImgADTimer.cancel();
            getImgADTimer = null;
        }
    }

    @Override
    protected void bindEvent() {

    }


    /**
     * 隐藏BG图
     */
    private void hideBGImage(){
        if(adThreeBg != null){
            ObjectAnimator animator = new ObjectAnimator().ofFloat(adThreeBg, "alpha", 1.0f, 0f).setDuration(3000);
            animator.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {
                    linearLayout.initView(imgResponses);//填充RecycleUpAnimationView中子view
                    //linearLayout.setImgResponses(imgResponses);//设置数据
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    adThreeBg.setVisibility(View.GONE);
                }

                @Override
                public void onAnimationCancel(Animator animation) {
                    adThreeBg.setVisibility(View.GONE);
                }

                @Override
                public void onAnimationRepeat(Animator animation) {

                }
            });
            animator.start();
        }
    }

    /**
     * 隐藏BG图
     */
    private void hideBGImageTwo(){
        if(adThreeBg != null){
            ObjectAnimator animator = new ObjectAnimator().ofFloat(adThreeBg, "alpha", 1.0f, 0f).setDuration(3000);
            animator.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {
                    linearLayout.setImgResponses(imgResponses);//设置数据
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    adThreeBg.setVisibility(View.GONE);
                }

                @Override
                public void onAnimationCancel(Animator animation) {
                    adThreeBg.setVisibility(View.GONE);
                }

                @Override
                public void onAnimationRepeat(Animator animation) {

                }
            });
            animator.start();
        }
    }

}
