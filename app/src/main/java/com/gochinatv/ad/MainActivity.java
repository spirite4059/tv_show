package com.gochinatv.ad;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;

import com.gochinatv.ad.ui.fragment.ADFourFragment;
import com.gochinatv.ad.ui.fragment.ADThreeFragment;
import com.gochinatv.ad.ui.fragment.ADTwoFragment;
import com.gochinatv.ad.ui.fragment.VideoPlayFragment;

/**
 * Created by fq_mbp on 16/3/17.
 */
public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FragmentManager fm = getFragmentManager();

        FragmentTransaction ft = fm.beginTransaction();

        ft.add(R.id.root_main, new VideoPlayFragment());
        ft.add(R.id.root_main, new ADTwoFragment());
        ft.add(R.id.root_main, new ADThreeFragment());
        ft.add(R.id.root_main, new ADFourFragment());
        ft.commit();

//        //添加图片广告
//        ADThreeFragment fragmentThree = new  ADThreeFragment();
//        FragmentManager fm3 = getFragmentManager();
//        FragmentTransaction ft3 = fm3.beginTransaction();
//        ft3.add(R.id.ad_three,fragmentThree);
//        ft3.commit();
//
//
//        //添加文本广告
//        ADFourFragment fragmentFour = new  ADFourFragment();
//        FragmentManager fm4 = getFragmentManager();
//        FragmentTransaction ft4 = fm4.beginTransaction();
//        ft4.add(R.id.ad_four,fragmentFour);
//        ft4.commit();


    }
}
