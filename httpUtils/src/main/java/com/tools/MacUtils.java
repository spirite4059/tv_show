package com.tools;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

/**
 * Created by fq_mbp on 16/3/29.
 */
public class MacUtils {

    public static String getMacAddress(Context context) {
        String macAddress = null;
        switch (checkNetStatus(context)) {
            case 1: // 有线
                macAddress = getLocalEthernetMacAddress();
                break;
            case 2: // 有线
                macAddress = getWifiMacAddr(context);
                break;
            default:
                break;
        }
        return macAddress;
    }
    /**
     * 获取wifi的mac
     */
    private static String getWifiMacAddr(Context context) {
        String macAddr = null;
        WifiManager wifi = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        WifiInfo info = wifi.getConnectionInfo();
        if (null != info) {
            String addr = info.getMacAddress();
            if (null != addr) {
                macAddr = addr;
            }
        }
        return macAddr;
    }



    /**
     * 获取有线mac
     *
     * @return
     */
    private static String getLocalEthernetMacAddress() {
        String mac = null;
        try {
            Enumeration localEnumeration = NetworkInterface.getNetworkInterfaces();

            while (localEnumeration.hasMoreElements()) {
                NetworkInterface localNetworkInterface = (NetworkInterface) localEnumeration.nextElement();
                String interfaceName = localNetworkInterface.getDisplayName();

                if (interfaceName == null) {
                    continue;
                }

                if (interfaceName.equals("eth0")) {
                    mac = convertToMac(localNetworkInterface.getHardwareAddress());
//                    if (mac != null && mac.startsWith("0:")) {
//                        mac = "0" + mac;
//                    }
                    break;
                }

            }
        } catch (SocketException e) {
            e.printStackTrace();
        }
        return mac;
    }

    private static String convertToMac(byte[] mac) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < mac.length; i++) {
            byte b = mac[i];
            int value = 0;
            if (b >= 0 && b <= 16) {
                value = b;
                sb.append(Integer.toHexString(value));
            } else if (b > 16) {
                value = b;
                sb.append(Integer.toHexString(value));
            } else {
                value = 256 + b;
                sb.append(Integer.toHexString(value));
            }
            if (i != mac.length - 1) {
                sb.append(":");
            }
        }
        return sb.toString();
    }


    /**
     * 检测当前网络的状态
     *
     * @param context
     * @return 有线 1； 无线 2; 无网络 0
     */
    public static int checkNetStatus(Context context) {
        ConnectivityManager conn = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = conn.getNetworkInfo(ConnectivityManager.TYPE_ETHERNET);
        // 有有线连接
        if (networkInfo != null && networkInfo.isConnected()) {
            return 1;
        }
        networkInfo = conn.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        if (networkInfo != null && networkInfo.isConnected()) {
            return 2;
        }
        return 0;
    }


}
