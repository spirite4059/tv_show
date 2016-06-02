package com.gochinatv.ad.ui.fragment;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.app.FragmentTransaction;
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
import com.gochinatv.ad.tools.SharedPreference;
import com.gochinatv.ad.ui.view.RecycleAnimationLayout;
import com.okhtttp.OkHttpCallBack;
import com.okhtttp.response.AdImgResponse;
import com.okhtttp.response.AdThreeDataResponse;
import com.okhtttp.service.ADHttpService;
import com.umeng.analytics.MobclickAgent;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by zfy on 2016/4/22.
 */
public class ADThreeLocalFragment extends BaseFragment {

    private RecycleAnimationLayout linearLayout;
    //private RecycleUpAnimationView linearLayout;
    private ImageView adThreeBg;
    private ArrayList<AdImgResponse> imgServerResponses;//来自服务器的数据集合

    private ArrayList<AdImgResponse> imgLocalResponses;//本地的数据集合

    private ArrayList<AdImgResponse> imgFinalResponses;//最终显示的数据集合


//    public LayoutResponse getLayoutResponse() {
//        return layoutResponse;
//    }
//
//    public void setLayoutResponse(LayoutResponse layoutResponse) {
//        this.layoutResponse = layoutResponse;
//    }
//
//    //布局参数
//    private LayoutResponse layoutResponse;

    //轮询间隔请求接口
    private int getImgADTime = 3*60*1000;//默认的是3个分钟
    private Timer getImgADTimer;//定时器
    private boolean isFirstDoHttp = true;//是否第一次请求

    private double width,height;


    @Override
    protected View initLayout(LayoutInflater inflater, ViewGroup container) {

        RelativeLayout layout = (RelativeLayout) inflater.inflate(R.layout.fragment_ad_three,container,false);

        String widthStr = null;
        String heightStr = null;
        String topStr = null;
        String leftStr = null;
        if(layoutResponse != null){
            widthStr = TextUtils.isEmpty(layoutResponse.adWidth)?"0.16689":layoutResponse.adWidth;
            heightStr = TextUtils.isEmpty(layoutResponse.adHeight)?"0.6":layoutResponse.adHeight;
            topStr = TextUtils.isEmpty(layoutResponse.adTop)?"0.4":layoutResponse.adTop;
            leftStr = TextUtils.isEmpty(layoutResponse.adLeft)?"0.8331":layoutResponse.adLeft;
        }else{
            //默认布局
            widthStr = "0.16689";
            heightStr = "0.6";
            topStr = "0.4";
            leftStr = "0.83315";
        }

        //动态布局
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,RelativeLayout.LayoutParams.WRAP_CONTENT);
        width = (float) (DataUtils.getDisplayMetricsWidth(getActivity())*(Float.parseFloat(widthStr)));
        height = (float) (DataUtils.getDisplayMetricsHeight(getActivity())*(Float.parseFloat(heightStr)));
        double top = (float) (DataUtils.getDisplayMetricsHeight(getActivity())*(Float.parseFloat(topStr)));
        double left = (float) (DataUtils.getDisplayMetricsWidth(getActivity())*(Float.parseFloat(leftStr)));

        params.width = (int) Math.round(width);
        params.height = (int) Math.round(height);
        params.topMargin = (int) Math.round(top);
        params.leftMargin = (int) Math.round(left);
        layout.setLayoutParams(params);
        LogCat.e("ADThreeLocalFragment"," 广告三布局 width: " + params.width + " height: " + params.height + " top: " + params.topMargin + " left: " + params.leftMargin);

        return layout;
    }

    @Override
    protected void initView(View rootView) {
        linearLayout = (RecycleAnimationLayout) rootView.findViewById(R.id.ad_three_lin);
        adThreeBg = (ImageView) rootView.findViewById(R.id.ad_three_bg_image);
    }

    @Override
    protected void init() {
        getImgADTime = Constants.isImageTest? 30*1000:3*60*1000;
        //double width = (float) (DataUtils.getDisplayMetricsWidth(getActivity())*0.16875f);
        //double height = (float) (DataUtils.getDisplayMetricsHeight(getActivity())*0.6f);
//        if(width == 0){
//            width = (float) (DataUtils.getDisplayMetricsWidth(getActivity())*0.16875f);
//        }
//        if(height == 0){
//            height = (float) (DataUtils.getDisplayMetricsHeight(getActivity())*0.6f);
//        }
        linearLayout.setItemWidth((int) Math.round(width));
        linearLayout.setItemHeight((int) Math.round(height / 2));
        LogCat.e("ADThreeLocalFragment","width: " + (int) Math.round(width) + "     height:" + (int) Math.round(height / 2));

        //准备好本地数据集合
        imgLocalResponses = new ArrayList<>();
        for(int i= 0;i<4;i++){
            AdImgResponse adImgResponse = new AdImgResponse();
            adImgResponse.adImgName = "";
            adImgResponse.adImgPrice = "";
            adImgResponse.adImgUrl = String.valueOf(i);
            adImgResponse.adImgId = i-10;
            adImgResponse.isFromServer = false;
            imgLocalResponses.add(adImgResponse);
        }

        initData();

    }


    protected void initData() {
        if(!DataUtils.isNetworkConnected(getActivity())){
            return;
        }
        ADHttpService.doHttpGetImageADInfo(getActivity(), new OkHttpCallBack<AdThreeDataResponse>() {
            @Override
            public void onSuccess(String url, AdThreeDataResponse response) {
                if (!isAdded()) {
                    return;
                }
                LogCat.e("ADThreeLocalFragment", " 广告三 url " + url);
                if (response == null || !(response instanceof AdThreeDataResponse)) {
                    //再次请求
                    initData();
                    return;
                }

                if (response.data == null || !(response.data instanceof ArrayList)) {
                    //再次请求
                    LogCat.e("ADThreeLocalFragment"," !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
                    initData();
                    return;
                }
                imgServerResponses = response.data;
                //设置滚动间隔
                if (response.adImgInterval != 0) {
                    linearLayout.setSecondTime(response.adImgInterval);
                }

                int totalSize = imgServerResponses.size();
                if (totalSize == 0) {
                    LogCat.e("ADThreeLocalFragment","  totalSize == 0 来自服务器广告图片总数为 0 ，全部轮询本地图片");
                    if(imgFinalResponses == null){
                        imgFinalResponses = new ArrayList<AdImgResponse>();
                    }
                    if(imgFinalResponses.size()>0){
                        imgFinalResponses.clear();
                    }

                    if(imgLocalResponses == null ){
                        imgLocalResponses = new ArrayList<>();
                        for(int i= 0;i<4;i++){
                            AdImgResponse adImgResponse = new AdImgResponse();
                            adImgResponse.adImgName = "";
                            adImgResponse.adImgPrice = "";
                            adImgResponse.adImgUrl = String.valueOf(i);
                            adImgResponse.adImgId = i-10;
                            adImgResponse.isFromServer = false;
                            imgLocalResponses.add(adImgResponse);
                        }
                    }
                    imgFinalResponses.addAll(imgLocalResponses);
                    if(isFirstDoHttp){
                        //第一次请求
                        hideBGImage();
                    }else{
                        linearLayout.setImgResponses(imgFinalResponses);//设置数据
                    }

                } else if(totalSize >=4)  {
                    LogCat.e("ADThreeLocalFragment","  totalSize >=4  全部轮询服务器图片广告");
                    //当服务器图片个数大于或等于4，全部轮询服务器的广告
                    if(imgFinalResponses == null){
                        imgFinalResponses = new ArrayList<AdImgResponse>();
                    }
                    if(imgFinalResponses.size()>0){
                        imgFinalResponses.clear();
                    }
                    imgFinalResponses.addAll(imgServerResponses);

                    if (isFirstDoHttp) {
                        //第一次请求
                        hideBGImage();
                    } else {
                        linearLayout.setImgResponses(imgFinalResponses);//设置数据
                    }
                }else{
                    LogCat.e("ADThreeLocalFragment"," 来自服务器广告图片总数为： "+totalSize);
                    //当服务器图片广告个数大于0但是不足4个，此时随机取本地来补充
                    if(imgFinalResponses == null){
                        imgFinalResponses = new ArrayList<AdImgResponse>();
                    }
                    if(imgFinalResponses.size()>0){
                        imgFinalResponses.clear();
                    }
                    imgFinalResponses.addAll(imgServerResponses);

                    if(imgLocalResponses == null ){
                        imgLocalResponses = new ArrayList<>();
                        for(int i= 0;i<4;i++){
                            AdImgResponse adImgResponse = new AdImgResponse();
                            adImgResponse.adImgName = "";
                            adImgResponse.adImgPrice = "";
                            adImgResponse.adImgUrl = String.valueOf(i);
                            adImgResponse.adImgId = i-10;
                            adImgResponse.isFromServer = false;
                            imgLocalResponses.add(adImgResponse);
                        }
                    }

                    int needAmount = 4-totalSize;//需要needAmount个本地图片来填充
                    LogCat.e("ADThreeLocalFragment", " 本地图片补充的 needAmount ：" + needAmount);
                    int [] needArray = DataUtils.randomCommon(0, 3, needAmount);
                    LogCat.e("ADThreeLocalFragment", " 本地图片补充的 needAmount #################### ：" + needAmount);


                    if(needArray != null){
                        for(int i=0;i<needArray.length;i++){
                            LogCat.e("ADThreeLocalFragment", " 本地图片补充的：" + needArray[i]);
                            if(needArray[i]<imgLocalResponses.size()){
                                imgFinalResponses.add(imgLocalResponses.get(needArray[i]));
                            }
                        }

                        if (isFirstDoHttp) {
                            //第一次请求
                            hideBGImage();
                        } else {
                            linearLayout.setImgResponses(imgFinalResponses);//设置数据
                        }
                    }

                }

                if (isFirstDoHttp) {
                    if (getImgADTimer == null) {
                        LogCat.e("ADThreeLocalFragment"," 开启广告三的轮询接口 ！！！！");
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
                if(linearLayout != null){
                    linearLayout.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            LogCat.e("ADThreeLocalFragment"," 广告三的接口 ！！！！onError ");
                            initData();
                        }
                    },2000);
                }

            }
        });

    }





    @Override
    public void onStop() {
        super.onStop();
//        if(linearLayout != null ){
//            linearLayout.destoryRecycleAnimation();
//        }
//
//        if(getImgADTimer != null){
//            getImgADTimer.cancel();
//            getImgADTimer = null;
//        }

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
            ObjectAnimator animator = new ObjectAnimator().ofFloat(adThreeBg, "alpha", 1.0f, 0f).setDuration(2000);
            animator.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {
                    linearLayout.initView(imgFinalResponses);//填充RecycleUpAnimationView中子view
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

//    /**
//     * 隐藏BG图
//     */
//    private void hideBGImageTwo(){
//        if(adThreeBg != null){
//            ObjectAnimator animator = new ObjectAnimator().ofFloat(adThreeBg, "alpha", 1.0f, 0f).setDuration(2000);
//            animator.addListener(new Animator.AnimatorListener() {
//                @Override
//                public void onAnimationStart(Animator animation) {
//                    linearLayout.setImgResponses(imgFinalResponses);//设置数据
//                }
//
//                @Override
//                public void onAnimationEnd(Animator animation) {
//                    adThreeBg.setVisibility(View.GONE);
//                }
//
//                @Override
//                public void onAnimationCancel(Animator animation) {
//                    adThreeBg.setVisibility(View.GONE);
//                }
//
//                @Override
//                public void onAnimationRepeat(Animator animation) {
//
//                }
//            });
//            animator.start();
//        }
//    }


    @Override
    public void onResume() {
        super.onResume();
        SharedPreference sharedPreference = SharedPreference.getSharedPreferenceUtils(getActivity());
        boolean isHasMac = sharedPreference.getDate(Constants.SHARE_KEY_UMENG, false);
        if(isHasMac){
            MobclickAgent.onPageStart("ADThreeLocalFragment");
        }
    }


    @Override
    public void onPause() {
        super.onPause();
        SharedPreference sharedPreference = SharedPreference.getSharedPreferenceUtils(getActivity());
        boolean isHasMac = sharedPreference.getDate(Constants.SHARE_KEY_UMENG, false);
        if(isHasMac){
            MobclickAgent.onPageEnd("ADThreeLocalFragment");
        }
    }

    /**
     * 恢复滚动
     */
    public void recoveryRollingAnimation(){
        if(linearLayout != null){
            linearLayout.recoveryRollingAnimation();
        }
    }

    /**
     * 暂停滚动
     */
    public void puaseRollingAnimation(){
        if(linearLayout != null ){
            linearLayout.puaseRecycleAnimation();
        }
    }


    @Override
    public void doHttpRequest() {
        initData();

    }

    @Override
    public void removeFragment() {
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.remove(this);
        ft.commit();
    }
}
