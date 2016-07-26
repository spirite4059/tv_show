package com.gochinatv.ad;

import android.net.wifi.ScanResult;
import android.os.Bundle;

import com.gochinatv.ad.base.BaseActivity;
import com.gochinatv.ad.tools.WifiAutoConnectManager;
import com.gochinatv.ad.ui.dialog.WifiFocusDialog;

import java.util.ArrayList;


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

        if(wifiAutoConnectManager == null){
            wifiAutoConnectManager = new WifiAutoConnectManager(this);
        }

        if(!wifiAutoConnectManager.wifiManager.isWifiEnabled()){
            wifiAutoConnectManager.wifiManager.setWifiEnabled(true);
        }


        wifiAutoConnectManager.wifiManager.startScan();

        wifiFocusDialog = new WifiFocusDialog(MainTestActivity.this, wifiAutoConnectManager, (ArrayList<ScanResult>) wifiAutoConnectManager.wifiManager.getScanResults());
        wifiFocusDialog.show();


    }






}
