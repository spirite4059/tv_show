package com.gochinatv.ad.ui.fragment;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.daimajia.slider.library.Indicators.PagerIndicator;
import com.daimajia.slider.library.SliderLayout;
import com.daimajia.slider.library.SliderTypes.BaseSliderView;
import com.daimajia.slider.library.SliderTypes.TextSliderView;
import com.daimajia.slider.library.Tricks.ViewPagerEx;
import com.gochinatv.ad.R;
import com.gochinatv.ad.base.BaseFragment;
import com.gochinatv.ad.tools.Constants;
import com.gochinatv.ad.tools.DataUtils;
import com.gochinatv.ad.tools.LogCat;
import com.gochinatv.ad.tools.SharedPreference;
import com.okhtttp.OkHttpCallBack;
import com.okhtttp.response.ADTwoOtherDataResponse;
import com.okhtttp.response.ADTwoOtherResponse;
import com.okhtttp.response.LayoutResponse;
import com.okhtttp.service.ADHttpService;
import com.squareup.picasso.Picasso;
import com.umeng.analytics.MobclickAgent;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by zfy on 2016/3/16.
 */
public class ADTwoFragment extends BaseFragment implements BaseSliderView.OnSliderClickListener, ViewPagerEx.OnPageChangeListener {


    private SliderLayout mDemoSlider;
    private ImageView imageView;
    //布局参数
    private LayoutResponse layoutResponse;


    //请求接口
    private int getWebADTime = 14400000;//请求接口的间隔
    private ArrayList<ADTwoOtherDataResponse> dataResponses;//广告数据集
    private int taotalSize;//广告总个数
    private int cycleTextTime = 5000;//每隔多长去滚动，默认：10 （秒）
    //是否是第一次网络请求
    private boolean isFirstDoHttp = true;
    //请求文字广告接口的定时器
    private int getTextADTime = 14400000;//每隔多长去请求接口，默认：4 （小时）== 14400000 毫秒
    private Timer getTextADTimer;

    private boolean isCycleState = false;//滚动的状态

    private ArrayList<BaseSliderView> sliderViewList;

    private boolean isStartSliderView;//是否启动过slider

//    private DisplayImageOptions options;
//    private ImageLoader imageLoader;

    private Picasso mPicasso;

    //请求接口刷新数据前的position
    private int oldPosition;



    @Override
    protected View initLayout(LayoutInflater inflater, ViewGroup container) {
        LogCat.e("width: " + DataUtils.getDisplayMetricsWidth(getActivity()) + " height:" + DataUtils.getDisplayMetricsHeight(getActivity()));

        RelativeLayout linearLayout = (RelativeLayout) inflater.inflate(R.layout.fragment_ad_two, container, false);
        if (layoutResponse != null) {

            if (!TextUtils.isEmpty(layoutResponse.adWidth) && !TextUtils.isEmpty(layoutResponse.adHeight)
                    && !TextUtils.isEmpty(layoutResponse.adTop) && !TextUtils.isEmpty(layoutResponse.adLeft)) {

                String widthStr = layoutResponse.adWidth;
                String heightStr = layoutResponse.adHeight;
                String topStr = layoutResponse.adTop;
                String leftStr = layoutResponse.adLeft;

                //动态布局
                RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
                double width = (float) (DataUtils.getDisplayMetricsWidth(getActivity()) * (Float.parseFloat(widthStr)));
                double height = (float) (DataUtils.getDisplayMetricsHeight(getActivity()) * (Float.parseFloat(heightStr)));
                double top = (float) (DataUtils.getDisplayMetricsHeight(getActivity()) * (Float.parseFloat(topStr)));
                double left = (float) (DataUtils.getDisplayMetricsWidth(getActivity()) * (Float.parseFloat(leftStr)));

                params.width = (int) Math.floor(width);
                params.height = (int) Math.floor(height);
                params.topMargin = (int) Math.floor(top);

                params.leftMargin = (int) Math.floor(left);
                linearLayout.setLayoutParams(params);
                LogCat.e(" 广告二布局 width: " + params.width + " height: " + params.height + " top: " + params.topMargin + " left: " + params.leftMargin);

            }
        }

        return linearLayout;

    }

    @Override
    protected void initView(View rootView) {
        mDemoSlider = (SliderLayout) rootView.findViewById(R.id.slider);
        imageView = (ImageView) rootView.findViewById(R.id.ad_two_img);
    }

    @Override
    protected void init() {
         mPicasso = Picasso.with(getActivity());
        //请求接口
        doGetTextAD();
    }


    @Override
    protected void bindEvent() {

    }

    @Override
    public void onStop() {
        super.onStop();

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mDemoSlider != null) {
            mDemoSlider.stopAutoCycle();
        }

        if (getTextADTimer != null) {
            getTextADTimer.cancel();
            getTextADTimer = null;
        }

    }


    /**
     * 请求接口
     */
    private int reTryTimes;

    private void doGetTextAD() {
        if (!DataUtils.isNetworkConnected(getActivity())) {
            return;
        }
        ADHttpService.doHttpGetWebADInfo(getActivity(), new OkHttpCallBack<ADTwoOtherResponse>() {
            @Override
            public void onSuccess(String url, ADTwoOtherResponse response) {
                if (!isAdded()) {
                    return;
                }
                LogCat.e(" 广告四 url " + url);
                if (response == null || !(response instanceof ADTwoOtherResponse)) {
                    LogCat.e("ADTwoFragment", "请求文字接口失败");
                    doError();
                    return;
                }

                if (response.data == null || !(response.data instanceof ArrayList)) {
                    LogCat.e("ADTwoFragment", "请求文字接口失败");
                    doError();
                    return;
                }

                if (dataResponses != null && dataResponses.size() > 0) {
                    LogCat.e("ADTwoFragment", " 清空之前的广告 ");
                    dataResponses.clear();//先清除之前的
                }
                dataResponses = response.data;
                taotalSize = dataResponses.size();
                //cycleTextTime = response.adTextInterval;//设置滚动间隔
                cycleTextTime = 1000;
                if (taotalSize == 0) {
                    //没有广告，停止滚动，显示内置图片
                    showImageViewAnimation();
                    mDemoSlider.pauseAutoCycle();//不滚动
                    isCycleState = false;
                } else if (taotalSize == 1) {
                    //只有1条广告，停止滚动
                    mDemoSlider.pauseAutoCycle();//不滚动
                    showImageView();
                    hideImageViewAnimation();
                    isCycleState = false;
                } else if (taotalSize > 1) {
                    LogCat.e("ADTwoFragment", "onPageSelected  position:  广告个数大于1   111111" );

                    //设置上次的持续时间
                    if(oldPosition >taotalSize-1){
                        oldPosition = 0;
                    }
                    if(dataResponses.get(oldPosition).adImageInterval<1000){
                        mDemoSlider.setDuration(2000);
                    }else {
                        mDemoSlider.setDuration(dataResponses.get(oldPosition).adImageInterval);
                    }

                    mDemoSlider.pauseAutoCycle();//不滚动
                    showImageView();
                    hideImageViewAnimation();
                    isCycleState = true;

                }


                if (isFirstDoHttp) {
                    if (getTextADTimer == null) {
                        getTextADTimer = new Timer();
                        getTextADTimer.schedule(new TimerTask() {
                            @Override
                            public void run() {
                                doGetTextAD();
                            }
                        }, 30000, 30000);
                    }
                }
                isFirstDoHttp = false;


            }

            @Override
            public void onError(String url, String errorMsg) {
                doError();
            }

            private void doError() {
                if (isAdded()) {
                    // 做不升级处理, 继续请求广告视频列表
                    reTryTimes++;
                    if (reTryTimes > 4) {
                        reTryTimes = 0;
                        LogCat.e("ADTwoFragment", "文字广告接口已连续请求3次，不在请求");
                    } else {
                        LogCat.e("ADTwoFragment", "进行第 " + reTryTimes + " 次重试请求。。。。。。。");
                        doGetTextAD();
                    }
                }
            }

        });

    }

    /**
     * 防止刷新数据出现黑屏，先让imageview显示出来再慢慢消失
     */
    private void showImageView() {
        imageView.setAlpha(1.0F);
        imageView.setVisibility(View.VISIBLE);
    }


    /**
     * 初始化sliderView数据
     */
    private void initSliderViewData() {

        if (dataResponses != null && dataResponses.size() > 0) {

            mDemoSlider.setIndicatorVisibility(PagerIndicator.IndicatorVisibility.Invisible);//不显示圆点
            mDemoSlider.setPresetTransformer(SliderLayout.Transformer.Accordion);
            mDemoSlider.setDuration(cycleTextTime);
            mDemoSlider.addOnPageChangeListener(this);

            if (sliderViewList != null) {
                if (sliderViewList.size() > 0) {
                    sliderViewList.clear();
                }
            } else {
                sliderViewList = new ArrayList<>();
            }

            for (ADTwoOtherDataResponse data : dataResponses) {
                TextSliderView textSliderView = new TextSliderView(getActivity());
                textSliderView
                        .image(data.adImageUrl)
                        .setScaleType(BaseSliderView.ScaleType.Fit);
                sliderViewList.add(textSliderView);
            }
            mDemoSlider.refreshSilderAdaper(sliderViewList);
            if(dataResponses.size() == 1){
                mDemoSlider.pauseAutoCycle();
            }else{
                mDemoSlider.startAutoCycle();
            }

        }

    }


    @Override
    public void onResume() {
        super.onResume();
        SharedPreference sharedPreference = SharedPreference.getSharedPreferenceUtils(getActivity());
        boolean isHasMac = sharedPreference.getDate(Constants.SHARE_KEY_UMENG, false);
        if (isHasMac) {
            MobclickAgent.onPageStart("ADTwoFragment");
        }
    }


    @Override
    public void onPause() {
        super.onPause();
        SharedPreference sharedPreference = SharedPreference.getSharedPreferenceUtils(getActivity());
        boolean isHasMac = sharedPreference.getDate(Constants.SHARE_KEY_UMENG, false);
        if (isHasMac) {
            LogCat.e("mac", "umeng可以使用。。。。。");
            MobclickAgent.onPageEnd("ADTwoFragment");
        }
    }


    public LayoutResponse getLayoutResponse() {
        return layoutResponse;
    }

    /**
     * 设置布局参数
     *
     * @param layoutResponse
     */
    public void setLayoutResponse(LayoutResponse layoutResponse) {
        this.layoutResponse = layoutResponse;
    }


    public int getGetWebADTime() {
        return getWebADTime;
    }

    /**
     * 设置间隔时间
     *
     * @param getWebADTime
     */
    public void setGetWebADTime(int getWebADTime) {
        this.getWebADTime = getWebADTime;
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
        LogCat.e("ADTwoFragment", "onPageSelected  position:  " + position);
        if(position > dataResponses.size()-1){
            position = 0;
        }
        if(!TextUtils.isEmpty(dataResponses.get(position).adImageUrl)){
            mPicasso.load(dataResponses.get(position).adImageUrl).into(imageView);
        }


        if(dataResponses.get(position).adImageInterval>1000){
            mDemoSlider.setDuration(dataResponses.get(position).adImageInterval);

        }else{
            mDemoSlider.setDuration(2000);
        }

        //记录上次的位置
        oldPosition = position;


    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    @Override
    public void onSliderClick(BaseSliderView slider) {

    }


    /**
     * 隐藏imageview动画
     */
    private void hideImageViewAnimation(){
        ObjectAnimator animator = ObjectAnimator.ofFloat(imageView,"alpha",1.0f,0f).setDuration(5000);
        animator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                if (!isCycleState) {
                    //之前未滚动,开始滚动
                    initSliderViewData();
                } else {
                    //之前已经滚动了，刷新数据
                    initSliderViewData();

                }
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                imageView.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                imageView.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });

        animator.start();

    }

    /**
     * 显示imageview动画
     */
    private void showImageViewAnimation(){
        ObjectAnimator animator = ObjectAnimator.ofFloat(imageView,"alpha",0.2f,1.0f).setDuration(1000);
        animator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                imageView.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animator animation) {

            }

            @Override
            public void onAnimationCancel(Animator animation) {
            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        animator.start();
    }


}
