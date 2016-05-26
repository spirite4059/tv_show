package com.gochinatv.ad;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.NetworkInfo.State;

/**
 * Created by ulplanet on 2016/5/26.
 */
public class NetworkBroadcastReceiver extends BroadcastReceiver {

    public NetworkInfo.State wifiState = null;
    public NetworkInfo.State ethernetState = null;
    public  String ACTION = "android.net.conn.CONNECTIVITY_CHANGE";



    private NetworkChageLinstener networkChageLinstener;

    @Override
    public void onReceive(Context context, Intent intent) {

        if(intent.getAction().equals(ACTION)){
            ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            wifiState = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState();
            ethernetState = cm.getNetworkInfo(ConnectivityManager.TYPE_ETHERNET).getState();

            if (wifiState != null && ethernetState != null && State.CONNECTED != wifiState && State.CONNECTED == ethernetState) {
                //Toast.makeText(context, "有线网络连接成功！", Toast.LENGTH_SHORT).show();

                if(networkChageLinstener != null){
                    networkChageLinstener.networkChage(true);
                }

            } else if (wifiState != null && ethernetState != null && NetworkInfo.State.CONNECTED == wifiState && State.CONNECTED != ethernetState) {
                //Toast.makeText(context, "无线网络连接成功！", Toast.LENGTH_SHORT).show();
                if(networkChageLinstener != null){
                    networkChageLinstener.networkChage(true);
                }

            } else if (wifiState != null && ethernetState != null && State.CONNECTED != wifiState && State.CONNECTED != ethernetState) {
                //Toast.makeText(context, "设备没有任何网络...", Toast.LENGTH_SHORT).show();

                if(networkChageLinstener != null){
                    networkChageLinstener.networkChage(false);
                }

            }


        }



    }


    interface NetworkChageLinstener{
        void networkChage(boolean hasNetwork);
    }



    public NetworkChageLinstener getNetworkChageLinstener() {
        return networkChageLinstener;
    }

    public void setNetworkChageLinstener(NetworkChageLinstener networkChageLinstener) {
        this.networkChageLinstener = networkChageLinstener;
    }




}
