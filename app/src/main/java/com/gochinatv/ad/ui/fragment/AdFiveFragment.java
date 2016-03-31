package com.gochinatv.ad.ui.fragment;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.gochinatv.ad.R;
import com.gochinatv.ad.adapter.EpisodeAdapter;
import com.gochinatv.ad.base.BaseFragment;
import com.gochinatv.ad.recycler.EpisodeLayoutManager;
import com.gochinatv.ad.recycler.NoTouchRecyclerView;
import com.gochinatv.ad.tools.LogCat;
import com.okhtttp.OkHttpCallBack;
import com.okhtttp.OkHttpUtils;
import com.okhtttp.response.AdImgResponse;
import com.okhtttp.response.AdThreeDataResponse;
import com.tools.HttpUrls;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by fq_mbp on 16/3/29.
 */
public class AdFiveFragment extends BaseFragment {


    public RelativeLayout relEpisodeRoot;
    private NoTouchRecyclerView recyclerView;
    private EpisodeLayoutManager episodeLayoutManager;
    private EpisodeAdapter episodeAdapter;
    private Timer flushTimer;
    private int position = 2;
    private ArrayList<AdImgResponse> imgResponses;
    private ImageView adThreeBg;

    private Timer getImageTimer;//请求图片广告timer


    @Override
    protected View initLayout(LayoutInflater inflater, ViewGroup container) {
        return inflater.inflate(R.layout.fragment_ad_five, container, false);
    }

    @Override
    protected void initView(View rootView) {
        relEpisodeRoot = (RelativeLayout) rootView.findViewById(R.id.root_fragment_episode);
        recyclerView = (NoTouchRecyclerView) rootView.findViewById(R.id.recycler_episodes);
        adThreeBg = (ImageView) rootView.findViewById(R.id.ad_three_bg_image);
    }

    @Override
    protected void init() {

        initData();




    }

    @Override
    public void onStop() {
        if(flushTimer != null){
            flushTimer.cancel();
            flushTimer = null;
        }

        if(getImageTimer != null){
            getImageTimer.cancel();
            getImageTimer = null;
        }


        super.onStop();
    }


    protected void initData() {

//        //请求图片
        OkHttpUtils.getInstance().doHttpGet(HttpUrls.URL_GET_AD_THREE, new OkHttpCallBack<AdThreeDataResponse>() {
            @Override
            public void onSuccess(String url, AdThreeDataResponse response) {
                if (!isAdded()) {
                    return;
                }
                if (response == null || !(response instanceof AdThreeDataResponse)) {
                    //再次请求
                    initData();
                }

                if (response.data == null || !(response.data instanceof ArrayList)) {
                    //再次请求
                    LogCat.e(" !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
                    initData();
                }
                imgResponses = response.data;
                int totalSize = imgResponses.size();
                if (totalSize == 0) {
                    LogCat.e(" totalSize == 0  totalSize == 0 没有广告图片");

                } else if (totalSize == 1) {
                    AdImgResponse adImgResponse = new AdImgResponse();
                    adImgResponse.adImgName = "";
                    adImgResponse.adImgPrice = "";
                    adImgResponse.adImgUrl = "localPicture";
                    imgResponses.add(adImgResponse);
                    setAdapter(totalSize);
                } else {
                    setAdapter(totalSize);
                }

                //开启定时器每隔3分钟请求接口
                getImageTimer = new Timer();
                getImageTimer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        getImageRequest();
                    }
                }, 120000, 120000);
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


    private void setAdapter(final int totalSize){

        ObjectAnimator objectAnimator = new ObjectAnimator().ofFloat(adThreeBg, "alpha", 1.0f, 0f).setDuration(3000);
        objectAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                episodeLayoutManager = new EpisodeLayoutManager(getActivity(), LinearLayoutManager.VERTICAL);
                recyclerView.setLayoutManager(episodeLayoutManager);
                episodeAdapter = new EpisodeAdapter(getActivity(), imgResponses);
                recyclerView.setAdapter(episodeAdapter);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                adThreeBg.setVisibility(View.GONE);//隐藏bg
                if (totalSize > 2) {
                    flushTimer = new Timer();
                    flushTimer.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            episodeLayoutManager.smoothScrollTop(recyclerView, position);
                            position++;
                        }
                    }, 5000, 5000);
                }
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                adThreeBg.setVisibility(View.GONE);//隐藏bg
                if (totalSize > 2) {
                    flushTimer = new Timer();
                    flushTimer.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            episodeLayoutManager.smoothScrollTop(recyclerView, position);
                            position++;
                        }
                    }, 5000, 5000);
                }
            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        objectAnimator.start();




    }




    @Override
    protected void bindEvent() {

    }


    /**
     * 请求广告图片
     */
     private void getImageRequest(){
         OkHttpUtils.getInstance().doHttpGet(HttpUrls.URL_GET_AD_THREE, new OkHttpCallBack<AdThreeDataResponse>() {
             @Override
             public void onSuccess(String url, AdThreeDataResponse response) {
                 if (!isAdded()) {
                     return;
                 }
                 if (response == null || !(response instanceof AdThreeDataResponse)) {
                     //再次请求
                     getImageRequest();
                 }
                 if (response.data == null || !(response.data instanceof ArrayList)) {
                     //再次请求
                     getImageRequest();
                 }
                 LogCat.e("轮循请求图片");
                 imgResponses = response.data;
                 int totalSize = imgResponses.size();
                 if(totalSize != 0){
                     for(AdImgResponse adImgResponse :imgResponses){
                         LogCat.e("#################轮循请求图片    " +adImgResponse.adImgName );
                     }
                 }
                 if(totalSize == 0){
                     LogCat.e(" totalSize == 0  totalSize == 0 没有广告图片");
                     adThreeBg.setAlpha(1.0f);
                     adThreeBg.setVisibility(View.VISIBLE);//隐藏bg
                     if(flushTimer != null ){
                         flushTimer.cancel();
                         flushTimer = null;
                     }
                     if(episodeAdapter != null){
                         episodeAdapter.referenceData(imgResponses);
                     }

                 }else if(totalSize == 1){
                     hideBGImage();
                     AdImgResponse adImgResponse = new AdImgResponse();
                     adImgResponse.adImgName = "";
                     adImgResponse.adImgPrice = "";
                     adImgResponse.adImgUrl = "localPicture";
                     imgResponses.add(adImgResponse);
                     if(flushTimer != null ){
                         flushTimer.cancel();
                         flushTimer = null;
                     }
                     if(episodeAdapter != null){
                         episodeAdapter.referenceData(imgResponses);
                     }
                 }else{
                     hideBGImage();
                     if(episodeAdapter != null){
                         episodeAdapter.referenceData(imgResponses);
                     }
                     if(flushTimer == null && totalSize>2){
                         flushTimer = new Timer();
                         flushTimer.schedule(new TimerTask() {
                             @Override
                             public void run() {
                                 episodeLayoutManager.smoothScrollTop(recyclerView, position);
                                 position++;
                             }
                         }, 5000, 5000);
                     }else{

                     }
                 }

             }

             @Override
             public void onError(String url, String errorMsg) {
                 if (!isAdded()) {
                     return;
                 }
                 getImageRequest();
             }
         });


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
