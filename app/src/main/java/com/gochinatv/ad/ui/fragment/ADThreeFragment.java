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
import com.okhtttp.OkHttpUtils;
import com.okhtttp.response.AdImgResponse;
import com.okhtttp.response.AdThreeDataResponse;
import com.tools.HttpUrls;

import java.util.ArrayList;

/**
 * Created by zfy on 2016/3/16.
 */
public class ADThreeFragment extends BaseFragment {

    //private SliderLayout mDemoSlider;
    private RecycleUpAnimationView linearLayout;
    private ImageView adThreeBg;
    private ArrayList<AdImgResponse> imgResponses;


    @Override
    protected View initLayout(LayoutInflater inflater, ViewGroup container) {
        return inflater.inflate(R.layout.fragment_ad_three,container,false);
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
        linearLayout.setItemHeight((int) Math.floor(height/2));
        initData();



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
                    hideBGImage();
                } else {
                    hideBGImage();
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





    @Override
    public void onStop() {


        super.onStop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
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


}
