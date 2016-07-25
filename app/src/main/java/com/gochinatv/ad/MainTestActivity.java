//package com.gochinatv.ad;
//
//import android.app.FragmentManager;
//import android.app.FragmentTransaction;
//import android.os.Bundle;
//
//import com.gochinatv.ad.base.BaseActivity;
//import com.gochinatv.ad.ui.fragment.ADFourFragment;
//import com.gochinatv.ad.ui.fragment.ADThreeFragment;
//import com.httputils.http.response.UpdateResponse;
//import com.httputils.http.response.VideoDetailListResponse;
//
///**
// * Created by zfy on 2016/3/17.
// */
//public class MainTestActivity extends BaseActivity {
//
//
//    //private LinearLayout adOneLayout,adTwoLayout,adThreeLayout,adFourLayout;
//
//
//
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main_test);
//        initView();
//        init();
//    }
//
//
//    private void initView(){
////        adOneLayout = (LinearLayout) findViewById(R.id.ad_one);
////        adTwoLayout = (LinearLayout) findViewById(R.id.ad_two);
////        adThreeLayout = (LinearLayout) findViewById(R.id.ad_three);
////        adFourLayout = (LinearLayout) findViewById(R.id.ad_four);
//
//    }
//
//
//    private void init(){
//
//        //添加图片广告
//        ADThreeFragment fragmentThree = new  ADThreeFragment();
//        FragmentManager fm3 = getFragmentManager();
//        FragmentTransaction ft3 = fm3.beginTransaction();
//        ft3.add(R.id.ad_three,fragmentThree);
//        ft3.commit();
//
//
//       //添加文本广告
//        ADFourFragment fragmentFour = new  ADFourFragment();
//        FragmentManager fm4 = getFragmentManager();
//        FragmentTransaction ft4 = fm4.beginTransaction();
//        ft4.add(R.id.ad_four,fragmentFour);
//        ft4.commit();
//
//
//
//
//    }
//
//
//
//    @Override
//    protected void onSuccessFul(VideoDetailListResponse response, String turl) {
//
//    }
//
//    @Override
//    protected void onFailed(String errorMsg, String turl) {
//
//    }
//
//    @Override
//    protected void onUpdateSuccess(UpdateResponse.UpdateInfoResponse updateInfo) {
//
//    }
//
//
//
//
//}
