package com.gochinatv.ad.ui.fragment;

import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.gochinatv.ad.R;
import com.gochinatv.ad.base.BaseFragment;
import com.gochinatv.ad.tools.DataUtils;
import com.gochinatv.ad.tools.LogCat;
import com.gochinatv.ad.ui.view.AutoTextView;
import com.okhtttp.OkHttpCallBack;
import com.okhtttp.response.ADFourResponse;
import com.okhtttp.response.ADTextRseponse;
import com.okhtttp.response.LayoutResponse;
import com.okhtttp.service.ADHttpService;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by zfy on 2016/3/16.
 */
public class ADFourFragment extends BaseFragment {

    //滚动控件
    private AutoTextView autoTextView;

    //private boolean isCycleText;
    //循环滚动定时器
    private int cycleTextTime = 10;//每隔多长去滚动，默认：10 （秒）
    private Timer cycleTextTimer;
    //文字广告集合
    private ArrayList<ADTextRseponse> textData;
    //文字集合总数
    private int taotalSize;

    //请求文字广告接口的定时器
    private int getTextADTime = 14400000;//每隔多长去请求接口，默认：4 （小时）== 14400000 毫秒
    private Timer getTextADTimer;

    public LayoutResponse getLayoutResponse() {
        return layoutResponse;
    }

    public void setLayoutResponse(LayoutResponse layoutResponse) {
        this.layoutResponse = layoutResponse;
    }

    private LayoutResponse layoutResponse;

    //是否是第一次网络请求
    private boolean isFirstDoHttp = true;


    //本地数据
    private ArrayList<String> textList;

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
        autoTextView = (AutoTextView) rootView.findViewById(R.id.auto_textview);
    }

    @Override
    protected void init() {
        autoTextView.setText("2016/03/31 19:00 Tuesday,Washington DC      内容合作Contact us:Service@gochinatv.com");
        textList = new ArrayList<>();
        textList.add("2016/03/31 19:00 Tuesday,Washington DC");
        textList.add("内容合作Contact us:Service@gochinatv.com");
        textList.add("2016/03/31 19:00 Tuesday,Washington DC      内容合作Contact us:Service@gochinatv.com");
        if(textList.size()>1){
            LogCat.e("ADFourFragment 第一次开启滚动 ");
            i = 0;
            cycleTextTimer = new Timer();
            cycleTextTimer.schedule(new CycleText(), cycleTextTime * 1000, cycleTextTime * 1000);
        }


//        //得到轮询请求接口间隔
//        getTextADTime = (int) SharedPreference.getSharedPreferenceUtils(getActivity()).getDate("pollInterval",Long.valueOf(14400000));
//        LogCat.e(" getTextADTime : "+ getTextADTime);
//        doGetTextAD();//请求接口
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
        LogCat.e(" 取消广告四的滚动 ");
        if(cycleTextTimer != null){
            cycleTextTimer.cancel();
            cycleTextTimer = null;
        }

        if(getTextADTimer != null){
            getTextADTimer.cancel();
            getTextADTimer = null;
        }
        super.onDestroy();
    }

    int i = 0;
    private class CycleText extends TimerTask{
        @Override
        public void run() {
            // 定义一个消息传过去
            Message msg = new Message();
            msg.what = i++;
            handler.sendMessage(msg);
        }
    }

    Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            int index = msg.what;
            if(index > textList.size()-1){
                index = 0;
                i = 1;
            }
            //LogCat.e("i :"+ i +" adVideoIndex :"+index + textList.get(index));
            autoTextView.next();
            autoTextView.setText(textList.get(index));
            super.handleMessage(msg);
        }
    };


    /**
     * 请求文字广告
     */
    private int reTryTimesTwo;
    private void doGetTextAD(){
        ADHttpService.doHttpGetTextADInfo(getActivity(), new OkHttpCallBack<ADFourResponse>() {
            @Override
            public void onSuccess(String url, ADFourResponse response) {
                if(!isAdded()){
                    return;
                }
                LogCat.e(" 广告四 url " + url);
                if (response == null || !(response instanceof ADFourResponse)) {
                    LogCat.e("请求文字接口失败");
                    doError();
                    return;
                }

                if(response.data == null || !(response.data instanceof ArrayList)){
                    LogCat.e("请求文字接口失败");
                    doError();
                    return;
                }
                if(textData != null &&textData.size()>0){
                    LogCat.e("ADFourFragment 清空之前的文字广告 ");
                    textData.clear();//先清除之前的
                }
                textData = response.data;
                taotalSize = textData.size();
                cycleTextTime = response.adTextInterval;//设置滚动间隔
                if(taotalSize >0){
                    cycleTextAD();
                }

                if(isFirstDoHttp){
                    if(getTextADTimer == null){
                        getTextADTimer = new Timer();
                        getTextADTimer.schedule(new TimerTask() {
                            @Override
                            public void run() {
                                doGetTextAD();
                            }
                        },getTextADTime,getTextADTime);
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
                        LogCat.e("文字广告接口已连续请求3次，不在请求");
                    } else {
                        LogCat.e("进行第 " + reTryTimesTwo + " 次重试请求。。。。。。。");
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


    /**
     * 开启文字滚动
     */
    private void cycleTextAD(){


        if(taotalSize == 1){
            //当只有一个广告,不开启滚动
            //如果之前的文字广告是滚动的就停止
            if(cycleTextTimer != null){
                LogCat.e("ADFourFragment 取消滚动 ");
                cycleTextTimer.cancel();
            }

            autoTextView.next();
            autoTextView.setText(textData.get(0).adTextStr);

        }else if(taotalSize >1){
            //当有多个广告,开启滚动
            //如果之前的文字广告是滚动的就继续，不是就开启滚动
            if(cycleTextTimer != null){
                LogCat.e("ADFourFragment 再次开启滚动 ");
                i = 0;
                cycleTextTimer.schedule(new CycleText(),cycleTextTime*1000,cycleTextTime*1000);
            }else{
                LogCat.e("ADFourFragment 第一次开启滚动 ");
                i = 0;
                cycleTextTimer = new Timer();
                cycleTextTimer.schedule(new CycleText(),cycleTextTime*1000,cycleTextTime*1000);
            }


        }

    }

}
