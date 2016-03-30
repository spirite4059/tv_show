package com.gochinatv.ad.ui.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;

import com.daimajia.slider.library.Indicators.PagerIndicator;
import com.daimajia.slider.library.SliderLayout;
import com.daimajia.slider.library.SliderTypes.BaseSliderView;
import com.daimajia.slider.library.SliderTypes.TextSliderView;
import com.daimajia.slider.library.Tricks.ViewPagerEx;
import com.gochinatv.ad.R;
import com.gochinatv.ad.base.BaseFragment;
import com.gochinatv.ad.tools.DataUtils;
import com.gochinatv.ad.tools.LogCat;

import java.util.HashMap;

/**
 *
 * Created by zfy on 2016/3/16.
 */
public class ADTwoFragment extends BaseFragment implements BaseSliderView.OnSliderClickListener, ViewPagerEx.OnPageChangeListener{


    private SliderLayout mDemoSlider;

    private WebView webView;

    @Override
    protected View initLayout(LayoutInflater inflater, ViewGroup container) {
        LogCat.e("width: " + DataUtils.getDisplayMetricsWidth(getActivity()) + " height:" + DataUtils.getDisplayMetricsHeight(getActivity()));

//        RelativeLayout relativeLayout = new RelativeLayout(getActivity());
//        Resources resources = getResources();
//        DataUtils.dpToPx(resources,500);
//
//        RelativeLayout.LayoutParams layoutParams1 = new RelativeLayout.LayoutParams(DataUtils.dpToPx(resources,500),DataUtils.dpToPx(resources,500));
//        //layoutParams1.addRule(RelativeLayout.CENTER_IN_PARENT);
//        layoutParams1.topMargin = 100;
//        layoutParams1.leftMargin = 500;
//        relativeLayout.setLayoutParams(layoutParams1);
//
//        webView = new WebView(getActivity());
//        RelativeLayout.LayoutParams layoutParams2 = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,RelativeLayout.LayoutParams.MATCH_PARENT);
//        relativeLayout.addView(webView,layoutParams2);
        return inflater.inflate(R.layout.fragment_ad_two,container,false);
        //return relativeLayout;
    }

    @Override
    protected void initView(View rootView) {
        mDemoSlider = (SliderLayout) rootView.findViewById(R.id.slider);
        //webView = (WebView) rootView.findViewById(R.id.webview);

    }

    @Override
    protected void init() {

        HashMap<String,Integer> url_maps = new HashMap<String, Integer>();
        url_maps.put("Hannibal", R.drawable.adtwo);
        url_maps.put("Big Bang Theory", R.drawable.news);
        url_maps.put("House of Cards", R.drawable.news2);
        url_maps.put("Game of Thrones", R.drawable.news3);
        for(String adVideoName : url_maps.keySet()){
            TextSliderView textSliderView = new TextSliderView(getActivity());
            // initialize a SliderLayout
            textSliderView
                    .description(adVideoName)
                    .image(url_maps.get(adVideoName))
                    .setScaleType(BaseSliderView.ScaleType.Fit)
                    .setOnSliderClickListener(this);

            //add your extra information
            textSliderView.bundle(new Bundle());
            textSliderView.getBundle()
                    .putString("extra",adVideoName);

            mDemoSlider.addSlider(textSliderView);
        }
        mDemoSlider.setPresetTransformer(SliderLayout.Transformer.Accordion);
        mDemoSlider.setPresetIndicator(SliderLayout.PresetIndicators.Center_Bottom);
        mDemoSlider.setCustomAnimation(null);
        mDemoSlider.setDuration(10000);
        mDemoSlider.addOnPageChangeListener(this);
        //当只有一张照片时，不显示小点和动画
        mDemoSlider.setIndicatorVisibility(PagerIndicator.IndicatorVisibility.Invisible);



        //webView.loadUrl("http://blog.csdn.net/woshinia/article/details/11520403");
    }

    @Override
    protected void bindEvent() {
//        webView.setWebViewClient(new WebViewClient(){
//            @Override
//            public boolean shouldOverrideUrlLoading(WebView view, String url) {
//                // TODO Auto-generated method stub
//                //返回值是true的时候控制去WebView打开，为false调用系统浏览器或第三方浏览器
//                view.loadUrl(url);
//                return true;
//            }
//        });
    }


    @Override
    public void onStop() {
//        if(mDemoSlider != null){
//            mDemoSlider.stopAutoCycle();
//        }

        super.onStop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }



    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {

    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    @Override
    public void onSliderClick(BaseSliderView slider) {

    }
}