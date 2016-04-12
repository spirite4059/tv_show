package com.gochinatv.ad.ui.fragment;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.gochinatv.ad.R;
import com.gochinatv.ad.base.BaseFragment;
import com.gochinatv.ad.tools.DataUtils;
import com.gochinatv.ad.tools.LogCat;
import com.gochinatv.ad.ui.view.RecycleUpAnimationView;
import com.okhtttp.OkHttpCallBack;
import com.okhtttp.response.AdImgResponse;
import com.okhtttp.response.AdThreeDataResponse;
import com.okhtttp.service.ADHttpService;

import java.util.ArrayList;

/**
 * Created by zfy on 2016/4/5.
 */
public class TestFragment extends BaseFragment {


    private RecycleUpAnimationView linearLayout;
    private ImageView adThreeBg;
    private ArrayList<AdImgResponse> imgResponses;

    @Override
    protected View initLayout(LayoutInflater inflater, ViewGroup container) {




        return inflater.inflate(R.layout.fragment_test,container,false);
    }

    @Override
    protected void initView(View rootView) {
        linearLayout = (RecycleUpAnimationView) rootView.findViewById(R.id.ad_three_lin);
        adThreeBg = (ImageView) rootView.findViewById(R.id.ad_three_bg_image);
    }

    @Override
    protected void init() {
        double width = (float) (DataUtils.getDisplayMetricsWidth(getActivity())*0.16875f);
        double height = (float) (DataUtils.getDisplayMetricsHeight(getActivity())*0.6f);
        linearLayout.setItemWidth((int) Math.floor(width));
        linearLayout.setItemHeight((int) Math.floor(height / 2));
        initData();
    }

    @Override
    protected void bindEvent() {

    }


    protected void initData() {


        ADHttpService.doHttpGetImageADInfo(getActivity(), new OkHttpCallBack<AdThreeDataResponse>() {
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
                LogCat.e(" 广告图片接口有数据 1111111 ");
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
                    hideBGImage();
                    LogCat.e(" 广告图片接口有数据 2222222 ");
                } else {
                    hideBGImage();
                    LogCat.e(" 广告图片接口有数据 33333333 ");
                }


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


    /**
     * 隐藏BG图
     */
    private void hideBGImage(){
        if(adThreeBg != null){
            ObjectAnimator animator = new ObjectAnimator().ofFloat(adThreeBg, "alpha", 1.0f, 0f).setDuration(3000);
            animator.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {
                    LogCat.e(" 广告图片接口有数据 44444444 ");
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







}
