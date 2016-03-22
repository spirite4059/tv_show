package com.gochinatv.ad;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.os.Environment;
import android.os.StatFs;
import android.text.format.Formatter;

import com.gochinatv.ad.tools.LogCat;

import java.io.File;

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

//        ft.add(R.id.root_main, new VideoPlayFragment());
//        ft.add(R.id.root_main, new ADTwoFragment());
//        ft.add(R.id.root_main, new ADThreeFragment());
//        ft.add(R.id.root_main, new ADFourFragment());
//        ft.commit();

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

        File path = Environment.getExternalStorageDirectory();
        StatFs statFs = new StatFs(path.getPath());
        long blockSize = statFs.getBlockSize();
        long totalBlocks = statFs.getFreeBlocks();
        long availableBlocks = statFs.getAvailableBlocks();

        //计算SD卡的空间大小
        long totalsize = blockSize * totalBlocks;
        long availablesize = availableBlocks * blockSize;
        LogCat.e("totalsize: " + totalsize);
        LogCat.e("availablesize: " + availablesize);
        //转化为可以显示的字符串
        String totalsize_str = Formatter.formatFileSize(this, totalsize);
        String availablesize_strString = Formatter.formatFileSize(this, availablesize);


        LogCat.e("totalsize_str: " + totalsize_str);
        LogCat.e("availablesize_strString: " + availablesize_strString);
    }



}
