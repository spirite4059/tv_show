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
    private Timer flushTimer;
    private int position = 2;
    private ArrayList<AdImgResponse> imgResponses;
    private ImageView adThreeBg;

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

        super.onStop();
    }


    protected void initData() {
//        imgResponses = new ArrayList<>();
//        for (int i = 'A'; i < 'Z'; i++) {
//            AdImgResponse adImgResponse = new AdImgResponse();
//            adImgResponse.adImgName = "category " + ((int) i - 65);
//            adImgResponse.adImgPrice = String.valueOf((int) i - 65);
//            adImgResponse.adImgUrl = String.valueOf((int) i - 65);
//            imgResponses.add(adImgResponse);
//        }

//        //请求图片
        OkHttpUtils.getInstance().doHttpGet(HttpUrls.URL_GET_AD_THREE, new OkHttpCallBack<AdThreeDataResponse>() {
            @Override
            public void onSuccess(String url, AdThreeDataResponse response) {
                if(!isAdded()){
                    return;
                }
                if(response == null ||!(response instanceof AdThreeDataResponse)){
                    //再次请求
                    initData();
                }

                if(response.data == null || !(response.data instanceof ArrayList)){
                    //再次请求
                    initData();
                }
                imgResponses = response.data;
                //imgResponses.addAll(imgResponses);
                int totalSize = imgResponses.size();
                if(totalSize == 0){



                }else if(totalSize == 1){
                    AdImgResponse adImgResponse = new AdImgResponse();
                    adImgResponse.adImgName = "";
                    adImgResponse.adImgPrice = "";
                    adImgResponse.adImgUrl = "localPicture";
                    imgResponses.add(adImgResponse);
                    setAdapter(totalSize);
                }else{
                    setAdapter(totalSize);
                }

            }

            @Override
            public void onError(String url, String errorMsg) {
                if(!isAdded()){
                    return;
                }
                initData();
            }
        });




    }


    private void setAdapter(final int totalSize){

        ObjectAnimator objectAnimator = new ObjectAnimator().ofFloat(adThreeBg, "x", 0f, 300f).setDuration(3000);
        objectAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                episodeLayoutManager = new EpisodeLayoutManager(getActivity(), LinearLayoutManager.VERTICAL);
                recyclerView.setLayoutManager(episodeLayoutManager);
                EpisodeAdapter episodeAdapter = new EpisodeAdapter(getActivity(), imgResponses);
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





}
