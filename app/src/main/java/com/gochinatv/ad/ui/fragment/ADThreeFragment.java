package com.gochinatv.ad.ui.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.daimajia.slider.library.SliderLayout;
import com.daimajia.slider.library.SliderTypes.BaseSliderView;
import com.daimajia.slider.library.SliderTypes.TextSliderView;
import com.daimajia.slider.library.Tricks.ViewPagerEx;
import com.gochinatv.ad.R;
import com.gochinatv.ad.base.BaseFragment;

import java.util.HashMap;

/**
 * Created by zfy on 2016/3/16.
 */
public class ADThreeFragment extends BaseFragment implements BaseSliderView.OnSliderClickListener, ViewPagerEx.OnPageChangeListener{

    //private  SimpleDraweeView draweeView;

    private SliderLayout mDemoSlider;

    @Override
    protected View initLayout(LayoutInflater inflater, ViewGroup container) {
        //View view = inflater.inflate(R.layout.fragment_ad_three,container,false);
        return inflater.inflate(R.layout.fragment_ad_three,container,false);
    }

    @Override
    protected void initView(View rootView) {
        //draweeView = (SimpleDraweeView)rootView.findViewById(R.id.drawee_view);
        mDemoSlider = (SliderLayout) rootView.findViewById(R.id.slider);
    }

    @Override
    protected void init() {
        //Uri uri = Uri.parse("https://raw.githubusercontent.com/facebook/fresco/gh-pages/static/fresco-logo.png");
        //draweeView.setImageURI(uri);
        HashMap<String,String> url_maps = new HashMap<String, String>();
        url_maps.put("Hannibal", "http://static2.hypable.com/wp-content/uploads/2013/12/hannibal-season-2-release-date.jpg");
        url_maps.put("Big Bang Theory", "http://tvfiles.alphacoders.com/100/hdclearart-10.png");
        url_maps.put("House of Cards", "http://cdn3.nflximg.net/images/3093/2043093.jpg");
        url_maps.put("Game of Thrones", "http://images.boomsbeat.com/data/images/full/19640/game-of-thrones-season-4-jpg.jpg");



        for(String name : url_maps.keySet()){
            TextSliderView textSliderView = new TextSliderView(getActivity());
            // initialize a SliderLayout
            textSliderView
                    .description(name)
                    .image(url_maps.get(name))
                    .setScaleType(BaseSliderView.ScaleType.Fit)
                    .setOnSliderClickListener(this);

            //add your extra information
            textSliderView.bundle(new Bundle());
            textSliderView.getBundle()
                    .putString("extra",name);

            mDemoSlider.addSlider(textSliderView);
        }
        mDemoSlider.setPresetTransformer(SliderLayout.Transformer.Accordion);
        mDemoSlider.setPresetIndicator(SliderLayout.PresetIndicators.Center_Bottom);
        mDemoSlider.setCustomAnimation(null);
        mDemoSlider.setDuration(4000);
        mDemoSlider.addOnPageChangeListener(this);




    }


    @Override
    public void onStop() {
        if(mDemoSlider != null){
            mDemoSlider.stopAutoCycle();
        }

        super.onStop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void bindEvent() {

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
