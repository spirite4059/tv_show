package com.gochinatv.ad.ui.fragment;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.gochinatv.ad.R;
import com.gochinatv.ad.base.BaseFragment;
import com.gochinatv.ad.tools.DataUtils;
import com.gochinatv.ad.tools.LogCat;
import com.okhtttp.OkHttpCallBack;
import com.okhtttp.response.ADTwoResponse;
import com.okhtttp.response.LayoutResponse;
import com.okhtttp.service.ADHttpService;

import java.util.Timer;
import java.util.TimerTask;

/**
 *
 * Created by zfy on 2016/3/16.
 */
public class ADTwoFragment extends BaseFragment {


    //webView
    private WebView webView;

    public LayoutResponse getLayoutResponse() {
        return layoutResponse;
    }

    public void setLayoutResponse(LayoutResponse layoutResponse) {
        this.layoutResponse = layoutResponse;
    }

    //布局参数
    private LayoutResponse layoutResponse;

    //开始默认显示图片
    private ImageView imageView;

    //请求文字广告接口的定时器
    private int getTextaWebTime = 5;//每隔多长去请求接口，默认：5 （分钟）
    private Timer getWebADTimer;

    @Override
    protected View initLayout(LayoutInflater inflater, ViewGroup container) {
        LogCat.e("width: " + DataUtils.getDisplayMetricsWidth(getActivity()) + " height:" + DataUtils.getDisplayMetricsHeight(getActivity()));

        RelativeLayout linearLayout = (RelativeLayout) inflater.inflate(R.layout.fragment_ad_two, container, false);
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
                linearLayout.setLayoutParams(params);
                LogCat.e(" 广告四布局 width: "+params.width+" height: "+params.height+" top: "+params.topMargin+" left: "+params.leftMargin);

            }
        }

        return linearLayout;

    }

    @Override
    protected void initView(View rootView) {
        //mDemoSlider = (SliderLayout) rootView.findViewById(R.id.slider);
        webView = (WebView) rootView.findViewById(R.id.webview);
        imageView = (ImageView) rootView.findViewById(R.id.ad_two_img);
    }

    @Override
    protected void init() {
        doGetWebAD();
    }

    @Override
    protected void bindEvent() {

    }

    @Override
    public void onStop() {
//        if(mDemoSlider != null){
//            mDemoSlider.stopAutoCycle();
//        }

        if(getWebADTimer != null){
            getWebADTimer.cancel();
            getWebADTimer = null;
        }

        super.onStop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    /**
     * 请求web广告
     */
    private  int reTryTimesTwo;
    private void doGetWebAD(){

        ADHttpService.doHttpGetWebADInfo(getActivity(), new OkHttpCallBack<ADTwoResponse>() {
            @Override
            public void onSuccess(String url, ADTwoResponse response) {
                if(!isAdded()){
                    return;
                }

                if (response == null || !(response instanceof ADTwoResponse)) {
                    LogCat.e("请求web接口失败");
                    doError();
                    return;
                }

                if(!TextUtils.isEmpty(response.adWebUrl)){
                    webView.loadUrl(response.adWebUrl);
                    webFinishLinster();

                    getWebADTimer = new Timer();
                    getWebADTimer.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            doGetWebAD();
                        }
                    }, getTextaWebTime * 60000, getTextaWebTime * 60000);

                }
            }

            private void doError() {
                if (isAdded()) {
                    // 做不升级处理, 继续请求广告视频列表
                    reTryTimesTwo++;
                    if (reTryTimesTwo > 4) {
                        reTryTimesTwo = 0;
                        LogCat.e("web广告接口已连续请求3次，不在请求");
                    } else {
                        LogCat.e("进行第 " + reTryTimesTwo + " 次重试请求。。。。。。。");
                        doGetWebAD();
                    }
                }
            }

            @Override
            public void onError(String url, String errorMsg) {
                doError();
            }
        });

    }


    /**
     * web监听
     */
    private void webFinishLinster(){

        webView.setWebViewClient(new WebViewClient() {


            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                LogCat.e("web加载完成 url " + url);
                //开启动画，显示webview
                showWebView();

            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                // TODO Auto-generated method stub
                //返回值是true的时候控制去WebView打开，为false调用系统浏览器或第三方浏览器
                view.loadUrl(url);
                return true;
            }


            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                super.onReceivedError(view, request, error);
            }
        });


    }

    /**
     * 显示webVeiw
     */
    private void showWebView(){

        ObjectAnimator animator = new ObjectAnimator().ofFloat(imageView,"alpha",1.0f,0.0f).setDuration(2000);
        animator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

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






}
