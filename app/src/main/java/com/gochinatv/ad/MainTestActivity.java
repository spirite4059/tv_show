package com.gochinatv.ad;

import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.os.Bundle;
import android.os.Environment;

import com.gochinatv.ad.base.BaseActivity;
import com.gochinatv.ad.tools.WifiAutoConnectManager;
import com.gochinatv.ad.ui.dialog.WifiFocusDialog;

import java.io.File;
import java.io.FileOutputStream;


/**
 * Created by zfy on 2016/3/17.
 */
public class MainTestActivity extends BaseActivity {


    WifiFocusDialog wifiFocusDialog;

    WifiAutoConnectManager wifiAutoConnectManager;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_test);
        initView();
        init();
    }


    private void initView(){


    }


    private void init(){

//        if(wifiAutoConnectManager == null){
//            wifiAutoConnectManager = new WifiAutoConnectManager(this);
//        }
//
//        if(!wifiAutoConnectManager.wifiManager.isWifiEnabled()){
//            wifiAutoConnectManager.wifiManager.setWifiEnabled(true);
//        }
//
//
//        wifiAutoConnectManager.wifiManager.startScan();
//
//        wifiFocusDialog = new WifiFocusDialog(MainTestActivity.this, wifiAutoConnectManager, (ArrayList<ScanResult>) wifiAutoConnectManager.wifiManager.getScanResults());
//        wifiFocusDialog.show();




    }


    public void getBitmapsFromVideo() {
        String dataPath = Environment.getExternalStorageDirectory()+ "/testVideo.mp4";
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(dataPath);
        // 取得视频的长度(单位为毫秒)
        String time = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
        // 取得视频的长度(单位为秒)
        int seconds = Integer.valueOf(time) / 1000;
        // 得到每一秒时刻的bitmap比如第一秒,第二秒
        for (int i = 1; i <= seconds; i=i+4) {
            Bitmap bitmap = retriever.getFrameAtTime(i*1000*1000,MediaMetadataRetriever.OPTION_CLOSEST_SYNC);
            String path = Environment.getExternalStorageDirectory()+ File.separator + i + ".jpg";
            FileOutputStream fos = null;
            try {
                fos = new FileOutputStream(path);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 80, fos);
                fos.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }





}
