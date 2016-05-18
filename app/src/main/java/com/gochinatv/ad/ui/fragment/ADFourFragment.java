package com.gochinatv.ad.ui.fragment;

import android.os.Handler;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.gochinatv.ad.R;
import com.gochinatv.ad.base.BaseFragment;
import com.gochinatv.ad.tools.Constants;
import com.gochinatv.ad.tools.DataUtils;
import com.gochinatv.ad.tools.LogCat;
import com.gochinatv.ad.tools.SharedPreference;
import com.gochinatv.ad.ui.view.AutoTextView;
import com.okhtttp.OkHttpCallBack;
import com.okhtttp.response.ADFourResponse;
import com.okhtttp.response.ADTextRseponse;
import com.okhtttp.response.LayoutResponse;
import com.okhtttp.service.ADHttpService;
import com.umeng.analytics.MobclickAgent;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by zfy on 2016/3/16.
 */
public class ADFourFragment extends BaseFragment {

    //滚动控件
    private AutoTextView autoTextView;


    //循环滚动定时器
    private boolean isCycleState = false;//滚动的状态
    private int cycleTextTime = 1;//每隔多长去滚动，默认：10 （秒）
    private Handler cycleHandler;//定时器



    //文字广告集合
    private ArrayList<ADTextRseponse> textData;
    //文字集合总数
    private int taotalSize;


    //请求文字广告接口的定时器
    private int getTextADTime = 14400000;//每隔多长去请求接口，默认：4 （小时）== 14400000 毫秒
    private Timer getTextADTimer;


    //布局参数
    private LayoutResponse layoutResponse;

    //是否是第一次网络请求
    private boolean isFirstDoHttp = true;



    //内置的文字广告
    private String textADString = "For more information about GoChina Media, please contact clientservice@gochinatv.com.";

    //本地数据
    //private ArrayList<String> textList;

    //view的宽度
    private int viewWidth;

    @Override
    protected View initLayout(LayoutInflater inflater, ViewGroup container) {

        LinearLayout linearLayout = (LinearLayout) inflater.inflate(R.layout.fragment_ad_four, container, false);
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
                viewWidth = (int) Math.floor(width);
                params.width = (int) Math.floor(width);
                params.height = (int) Math.floor(height);
                params.topMargin = (int) Math.floor(top);

                params.leftMargin = (int) Math.floor(left);
                linearLayout.setLayoutParams(params);
                LogCat.e("ADFourFragment"," 广告四布局 width: "+params.width+" height: "+params.height+" top: "+params.topMargin+" left: "+params.leftMargin);

            }
        }

        return linearLayout;
    }

    @Override
    protected void initView(View rootView) {
        autoTextView = (AutoTextView) rootView.findViewById(R.id.auto_textview);
    }

    @Override
    protected void init() {
        autoTextView.setViewWidth(viewWidth);
        //autoTextView.setText(textADString);
//        textList = new ArrayList<>();
//        textList.add("Please call our service team at 1-877-227-5717");
//        textList.add("Interested in installation of our TV in your restaurant?");
//        textList.add("Please visit http://h5.eqxiu.com/s/LW4ukoLZ");
//        textList.add("Join our partner recruitment plan.");
//        textList.add("Win a $10 dollar referral fee for each successfully recommendation.");
//        textList.add("Please contact us via clientservice@gochinatv.com or wechat: 13034624085.");
//        textList.add("Advertising client please contact globalsales@gochinatv.com");
//        textList.add("For more information about GoChina Media, please contact clientservice@gochinatv.com.");

        cycleHandler = new Handler();

        //得到轮询请求接口间隔
        doGetTextAD();//请求接口
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
        LogCat.e("ADFourFragment"," 取消广告四的滚动 ");

        if(cycleHandler != null && runnable != null){
            cycleHandler.removeCallbacks(runnable);
            isCycleState = false;
        }


        if(getTextADTimer != null){
            getTextADTimer.cancel();
            getTextADTimer = null;
        }

        if(autoTextView != null){
            autoTextView.stopScroll();
        }

        super.onDestroy();
    }


    int i = 0;
    int index;
    Runnable runnable = new Runnable() {
        @Override
        public void run() {

            isCycleState = true;
            index = i++;
            if(index > textData.size()-1){
                index = 0;
                i = 1;
            }
            LogCat.e("ADFourFragment","index :"+index  + "     "+textData.get(index).adTextStr);
            if(!TextUtils.isEmpty(textData.get(index).adTextStr)){
                autoTextView.next();
                autoTextView.setText(textData.get(index).adTextStr);
            }

            //下一次的滚动
            if(textData.get(index).adTextInterval >1000){
                cycleTextTime = textData.get(index).adTextInterval;
            }else {
                cycleTextTime = 5000;
            }
            cycleHandler.postDelayed(runnable,cycleTextTime);

        }
    };




    /**
     * 请求文字广告
     */
    private int reTryTimesTwo;
    private void doGetTextAD(){
        if(!DataUtils.isNetworkConnected(getActivity())){
            return;
        }
        ADHttpService.doHttpGetTextADInfo(getActivity(), new OkHttpCallBack<ADFourResponse>() {
            @Override
            public void onSuccess(String url, ADFourResponse response) {
                if(!isAdded()){
                    return;
                }
                LogCat.e(" 广告四 url " + url);
                if (response == null || !(response instanceof ADFourResponse)) {
                    LogCat.e("ADFourFragment","请求文字接口失败");
                    doError();
                    return;
                }

                if(response.data == null || !(response.data instanceof ArrayList)){
                    LogCat.e("ADFourFragment","请求文字接口失败");
                    doError();
                    return;
                }
                if(textData != null &&textData.size()>0){
                    LogCat.e("ADFourFragment","ADFourFragment 清空之前的文字广告 ");
                    textData.clear();//先清除之前的
                }
                textData = response.data;
                taotalSize = textData.size();
                cycleTextTime = response.adTextInterval;//设置滚动间隔

                if(taotalSize == 0){
                    //此时没有文字广告，要停止之前的滚动
                    // 显示内置文字广告
                    autoTextView.next();
                    autoTextView.setText(textADString);

                    LogCat.e("ADFourFragment"," 停止之前的滚动 ");
                    if(cycleHandler != null && runnable != null){
                        cycleHandler.removeCallbacks(runnable);
                        isCycleState = false;
                    }

                }

                if(taotalSize == 1){
                    //此时有1条文字广告，要停止之前的滚动
                    if(!TextUtils.isEmpty(textData.get(0).adTextStr)){
                        autoTextView.next();
                        autoTextView.setText(textData.get(0).adTextStr);
                    }
                    LogCat.e("ADFourFragment","停止之前的滚动 ");
                    if(cycleHandler != null && runnable != null){
                        cycleHandler.removeCallbacks(runnable);
                        isCycleState = false;
                    }

                }

                if(taotalSize >1){
                    //有多条文字广告，开启滚动
                    if(!isCycleState){
                        LogCat.e("ADFourFragment","开启滚动 ");
                        if(cycleHandler != null && runnable != null){
                            if(textData.get(0).adTextInterval<1000){
                                cycleHandler.postDelayed(runnable,5000);
                            }else{
                                cycleHandler.postDelayed(runnable,textData.get(0).adTextInterval);
                            }
                        }
                    }
                }

                if(isFirstDoHttp){
                    if(getTextADTimer == null){
                        getTextADTimer = new Timer();
                        getTextADTimer.schedule(new TimerTask() {
                            @Override
                            public void run() {
                                doGetTextAD();
                            }
                        },30000,30000);
                    }
                }
                isFirstDoHttp = false;

            }

            private void doError() {
                if (isAdded()) {
                    // 做不升级处理, 继续请求广告视频列表
                    reTryTimesTwo++;
                    if (reTryTimesTwo > 4) {
                        reTryTimesTwo = 0;
                        // 显示内置文字广告
                        autoTextView.next();
                        autoTextView.setText(textADString);
                        LogCat.e("ADFourFragment","文字广告接口已连续请求3次，不在请求");
                    } else {
                        LogCat.e("ADFourFragment","进行第 " + reTryTimesTwo + " 次重试请求。。。。。。。");
                        doGetTextAD();
                    }
                }
            }


            @Override
            public void onError(String url, String errorMsg) {

                doError();
            }
        });

    }




    @Override
    public void onResume() {
        super.onResume();
        SharedPreference sharedPreference = SharedPreference.getSharedPreferenceUtils(getActivity());
        boolean isHasMac = sharedPreference.getDate(Constants.SHARE_KEY_UMENG, false);
        if(isHasMac){
            MobclickAgent.onPageStart("ADFourFragment");
        }
    }


    @Override
    public void onPause() {
        super.onPause();
        SharedPreference sharedPreference = SharedPreference.getSharedPreferenceUtils(getActivity());
        boolean isHasMac = sharedPreference.getDate(Constants.SHARE_KEY_UMENG, false);
        if(isHasMac){
            MobclickAgent.onPageEnd("ADFourFragment");
        }
    }



    public LayoutResponse getLayoutResponse() {
        return layoutResponse;
    }

    /**
     * 设置布局参数
     * @param layoutResponse
     */
    public void setLayoutResponse(LayoutResponse layoutResponse) {
        this.layoutResponse = layoutResponse;
    }

    /**
     * 设置请求接口的间隔
     * @param getTextADTime
     */
    public void setGetTextADTime(int getTextADTime) {
        this.getTextADTime = getTextADTime;
    }
}
