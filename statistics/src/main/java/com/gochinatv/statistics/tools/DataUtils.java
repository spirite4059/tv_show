package com.gochinatv.statistics.tools;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Environment;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;

@SuppressLint({"NewApi", "SimpleDateFormat", "DefaultLocale"})
public class DataUtils {


    /**
     * 获取应用版本号
     *
     * @param context
     * @return
     * @throws Exception
     * @author zlk
     */
    public static String getVersionName(Context context) {
        // 获取packagemanager的实例
        PackageManager packageManager = context.getPackageManager();
        // getPackageName()是你当前类的包名，0代表是获取版本信息
        PackageInfo packInfo = null;
        try {
            packInfo = packageManager.getPackageInfo(context.getPackageName(), 0);
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }
        return packInfo == null ? "" : packInfo.versionName;
    }


    /**
     * 获取当前应用版本号
     *
     * @return
     * @throws NameNotFoundException
     */
    public static Integer getAppVersionCode(Context context) {
        PackageManager packageManager = context.getPackageManager();

        // getPackageName()是你当前类的包名，0代表是获取版本信息
        PackageInfo packInfo = null;
        try {
            packInfo = packageManager.getPackageInfo(context.getPackageName(), 0);
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }
        return packInfo == null ? 0 : packInfo.versionCode;
    }


    /**
     * 获取当前应用版本
     *
     * @return
     * @throws NameNotFoundException
     */
    public static String getAppVersionName(Context context) throws NameNotFoundException {
        PackageManager packageManager = context.getPackageManager();
        // getPackageName()是你当前类的包名，0代表是获取版本信息
        PackageInfo packInfo = packageManager.getPackageInfo(context.getPackageName(), 0);
        String versionName = packInfo.versionName;
        return versionName;
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

    public static int getAndroidOSVersion() {
        int osVersion;
        try {
            osVersion = Integer.valueOf(Build.VERSION.SDK_INT);
        } catch (NumberFormatException e) {
            osVersion = 0;
        }

        return osVersion;
    }


    //获取是否存在NavigationBar
    public static boolean checkDeviceHasNavigationBar(Context context) {
        boolean hasNavigationBar = false;
        Resources rs = context.getResources();
        int id = rs.getIdentifier("config_showNavigationBar", "bool", "android");
        if (id > 0) {
            hasNavigationBar = rs.getBoolean(id);
        }
        try {
            Class systemPropertiesClass = Class.forName("android.os.SystemProperties");
            Method m = systemPropertiesClass.getMethod("get", String.class);
            String navBarOverride = (String) m.invoke(systemPropertiesClass, "qemu.hw.mainkeys");
            if ("1".equals(navBarOverride)) {
                hasNavigationBar = false;
            } else if ("0".equals(navBarOverride)) {
                hasNavigationBar = true;
            }
        } catch (Exception e) {

        }
        return hasNavigationBar;
    }

//	public static boolean checkDeviceHasNavigationBar(Context activity) {
//		//通过判断设备是否有返回键、菜单键(不是虚拟键,是手机屏幕外的按键)来确定是否有navigation bar
////		boolean hasMenuKey = ViewConfiguration.get(activity)
////				.hasPermanentMenuKey();
//		boolean hasBackKey = KeyCharacterMap
//				.deviceHasKey(KeyEvent.KEYCODE_BACK);
//
//		if (!hasBackKey) {
//			// 做任何你需要做的,这个设备有一个导航栏
//			return true;
//		}
//		return false;
//	}


    /**
     * 向sdcard中写入文件
     * @param
     * @param content 文件内容
     */
    public static void saveToSDCard(String content){
        OutputStream out = null;
        try {
            File file = new File(Environment.getExternalStorageDirectory(), "upStartTime.txt");
            if(!file.exists()){
                file.createNewFile();
            }
            out = new FileOutputStream(file,true);
            out.write(content.getBytes());
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }



    public static String getFormatTime(long time){
        if(time <= 0){
            return "";
        }
        String timeFormat = getFormatTime(time, "yyyy-MM-dd hh:mm:ss");
        return timeFormat;
    }

    public static String getFormatTime(long time, String format){
        if(time <= 0){
            return "";
        }
        String timeFormat = "";
        try {
            SimpleDateFormat sdf = new SimpleDateFormat(format);
            timeFormat = sdf.format(new Date(time));
        }catch (Exception e){
            e.printStackTrace();
        }
        return timeFormat;
    }
}
