package com.gochinatv.ad.tools;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.lang.reflect.Method;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

@SuppressLint({ "NewApi", "SimpleDateFormat", "DefaultLocale" })
public class DataUtils {


	public static int dpToPx(Resources res, int dp) {
		return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, res.getDisplayMetrics());
	}

	public static int pxToDp(Resources res, int px) {
		return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_PX, px, res.getDisplayMetrics());
	}


	/**
	 * MD5单向加密，32位，用于加密密码，因为明文密码在信道中传输不安全，明文保存在本地也不安全
	 * 
	 * @param str
	 * @return
	 */
	public static String md5(String str) {
		MessageDigest md5 = null;
		try {
			md5 = MessageDigest.getInstance("MD5");
		} catch (Exception e) {
			e.printStackTrace();
			return "";
		}
		char[] charArray = str.toCharArray();
		byte[] byteArray = new byte[charArray.length];
		for (int i = 0; i < charArray.length; i++) {
			byteArray[i] = (byte) charArray[i];
		}
		byte[] md5Bytes = md5.digest(byteArray);
		StringBuffer hexValue = new StringBuffer();
		for (int i = 0; i < md5Bytes.length; i++) {
			int val = ((int) md5Bytes[i]) & 0xff;
			if (val < 16) {
				hexValue.append("0");
			}
			hexValue.append(Integer.toHexString(val));
		}
		return hexValue.toString();
	}


	/**
	 * 获取应用版本号
	 * 
	 * @author zlk
	 * @param context
	 * @return
	 * @throws Exception
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
	 * 获取运营商代号:IMSI号前面3位460是国家，紧接着后面2位00 02是中国移动，01是中国联通，03是中国电信。
	 * 
	 * @param context
	 * @return
	 */
	public static String getSIMName(Context context) {
		TelephonyManager telManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
		String operator = telManager.getSimOperator();
		return operator == null ? "" : operator;
	}



	/**
	 * 获取系统ndk版本号
	 *
	 * @return
	 */
	public static int getSystemSDKVersion() {
		return Build.VERSION.SDK_INT;
	}


	/**
	 * 获取当前应用版本号
	 *
	 * @return
	 * @throws NameNotFoundException
	 */
	public static double getAppVersion(Context context) throws NameNotFoundException {
		PackageManager packageManager = context.getPackageManager();
		// getPackageName()是你当前类的包名，0代表是获取版本信息
		PackageInfo packInfo = packageManager.getPackageInfo(context.getPackageName(), 0);
		int version = packInfo.versionCode;
		return version;
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
	 * 判断是否存在sdcard
	 *
	 * @return
	 */
	public static boolean isExistSDCard() {
		if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
			return true;
		} else
			return false;
	}

	/**
	 * 获取sdcard的容量
	 *
	 * @return
	 */
	public long getSDAllSize() {
		// 取得SD卡文件路径
		File path = Environment.getExternalStorageDirectory();
		StatFs sf = new StatFs(path.getPath());
		// 获取单个数据块的大小(Byte)
		long blockSize = sf.getAvailableBlocks();
		// 获取所有数据块数
		long allBlocks = sf.getBlockCount();
		// 返回SD卡大小
		// return allBlocks * blockSize; //单位Byte
		// return (allBlocks * blockSize)/1024; //单位KB
		return (allBlocks * blockSize) / 1024 / 1024; // 单位MB
	}



	public static String getTimeLengthStrByMillis(float totalDuration) {
		float seconds = totalDuration / 1000;
		float hour = seconds / 3600;
		float hourMod = seconds % 3600;

		float minute = hourMod / 60;
		float second = -1;
		second = hourMod % 60;
		String hourStr = hour < 10 ? "0" + hour : String.valueOf(hour);
		String minuteStr = minute < 10 ? "0" + minute : String.valueOf(minute);
		String secondStr = second < 10 ? "0" + second : String.valueOf(second);
		StringBuilder time = new StringBuilder();
		time.append(hourStr);
		time.append(":");
		time.append(minuteStr);
		time.append(":");
		time.append(secondStr);
		return time.toString();
	}



	public static String getWIFIMacAddress(Context context) {
		WifiManager wifi = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
		WifiInfo info = wifi.getConnectionInfo();
		return info.getMacAddress();
	}

	public static boolean checkEthernet(Context context) {
		ConnectivityManager conn = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = conn.getNetworkInfo(ConnectivityManager.TYPE_ETHERNET);
		if (networkInfo == null) {
			return false;
		}
		return networkInfo.isConnected();
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



	public static boolean isNetworkConnected(Context context) {
		if (context != null) {
			ConnectivityManager mConnectivityManager = (ConnectivityManager) context
					.getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo mNetworkInfo = mConnectivityManager.getActiveNetworkInfo();
			if (mNetworkInfo != null) {
				return mNetworkInfo.isAvailable();
			}
		}
		return false;
	}








	public static Map<String, String> getVideoErrorResponse(Context context){
		Map<String, String> paramsMap = new HashMap<String, String>();
		// 品牌
		String brand = null;
		// 机型
		String clienttype = null;
		// 设备号
		String did = null;
		// MAC地址
		String mac = null;
		// 软件版本号
		String sfv = null;
		// sdk版本号
		String sdkv = null;
		// 渠道
		String source = null;
		//
		String time = null;

		try {
			brand = TextUtils.isEmpty(Build.BRAND) ? "" : URLEncoder.encode(Build.BRAND, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}

		try {
			clienttype = TextUtils.isEmpty(Build.MODEL) ? "" : URLEncoder.encode(Build.MODEL, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}

		try {
			did = TextUtils.isEmpty(Build.ID) ? "" : URLEncoder.encode(Build.ID, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}

		try {
			mac = TextUtils.isEmpty(getMacAddress(context)) ? "" : getMacAddress(context);
		} catch (Exception e) {
			e.printStackTrace();
		}


		try {
			sfv = TextUtils.isEmpty(getAppVersionName(context)) ? "" : getAppVersionName(context);
		} catch (Exception e) {
			e.printStackTrace();
		}



		try {
			sdkv = TextUtils.isEmpty(Build.VERSION.RELEASE) ? "" : URLEncoder.encode(Build.VERSION.RELEASE, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}

		try {
			ApplicationInfo appInfo = context.getPackageManager().getApplicationInfo(context.getPackageName(),
					PackageManager.GET_META_DATA);
			source = TextUtils.isEmpty(appInfo.metaData.getString("UMENG_CHANNEL")) ? "" : URLEncoder.encode(appInfo.metaData.getString("UMENG_CHANNEL"), "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}

		try {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddhhmmss");
			Date date = new Date();
			date.setTime(System.currentTimeMillis());
			String timeStr = sdf.format(date);
			time = TextUtils.isEmpty(timeStr) ? "" : URLEncoder.encode(timeStr, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}

		// 品牌 例：XiaoMi
		paramsMap.put("brand", brand);
		// 机型	例：Note Lite
		paramsMap.put("clienttype", clienttype);
		// 设备号
		paramsMap.put("did", did);
		// mac	当前使用的网卡mac地址	例：0F126C2DFAE3
		paramsMap.put("mac", mac);
		// 软件版本名
		paramsMap.put("sfv", sfv);
		// sdk 版本
		paramsMap.put("sdkv", sdkv);
		// 渠道
		paramsMap.put("source", source);
		// 当前时间
		paramsMap.put("time", time);
		// 接口版本
		paramsMap.put("iv", "1.0.3");

		return paramsMap;



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

	/**
	 * Detects and toggles immersive mode (also known as "hidey bar" mode).
	 */
	public static void hideNavigationBar(Activity activity) {

		// The UI options currently enabled are represented by a bitfield.
		// getSystemUiVisibility() gives us that bitfield.
		int uiOptions = activity.getWindow().getDecorView().getSystemUiVisibility();
		int newUiOptions = uiOptions;
		boolean isImmersiveModeEnabled =
				((uiOptions | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY) == uiOptions);
		if (isImmersiveModeEnabled) {
			LogCat.e("video", "Turning immersive mode mode off. ");
		} else {
			LogCat.e("video", "Turning immersive mode mode on.");
		}

		// Navigation bar hiding:  Backwards compatible to ICS.
		if (Build.VERSION.SDK_INT >= 14) {
			newUiOptions ^= View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
		}

		// Status bar hiding: Backwards compatible to Jellybean
		if (Build.VERSION.SDK_INT >= 16) {
			newUiOptions ^= View.SYSTEM_UI_FLAG_FULLSCREEN;
		}

		// Immersive mode: Backward compatible to KitKat.
		// Note that this flag doesn't do anything by itself, it only augments the behavior
		// of HIDE_NAVIGATION and FLAG_FULLSCREEN.  For the purposes of this sample
		// all three flags are being toggled together.
		// Note that there are two immersive mode UI flags, one of which is referred to as "sticky".
		// Sticky immersive mode differs in that it makes the navigation and status bars
		// semi-transparent, and the UI flag does not get cleared when the user interacts with
		// the screen.
		if (Build.VERSION.SDK_INT >= 18) {
			newUiOptions ^= View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
		}

		activity.getWindow().getDecorView().setSystemUiVisibility(newUiOptions);
	}



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
	 *获取总RAM大小,并且判断是否大于1G
	 */
	public static boolean islowMemory(Context context) {
		String str1 = "/proc/meminfo";// 系统内存信息文件
		String str2;
		String[] arrayOfString;
		long initial_memory = 0;
		boolean isLowMemory = false;
		long memory = 1024*1024;//1G
		try {
			FileReader localFileReader = new FileReader(str1);
			BufferedReader localBufferedReader = new BufferedReader(
					localFileReader, 8192);
			str2 = localBufferedReader.readLine();// 读取meminfo第一行，系统总内存大小

			arrayOfString = str2.split("\\s+");
			for (String num : arrayOfString) {
				Log.i(str2, num + "\t");
			}

			initial_memory = Integer.valueOf(arrayOfString[1]).intValue();// 获得系统总内存，单位是KB，
			localBufferedReader.close();

		} catch (IOException e) {
		}
		//System.out.println((initial_memory/(1024*1024)));
		LogCat.e("TotalMemory:" + (initial_memory));
		if(initial_memory>memory){
			isLowMemory = false;
		}else{
			LogCat.e("内存小于1G");
			isLowMemory = true;
		}
		LogCat.e("isLowMemory:" + isLowMemory);
		return isLowMemory;

	}


	public static String getRawVideoUri(Context context, int resId){
		StringBuilder sb = new StringBuilder("android.resource://");
		sb.append(context.getPackageName());
		sb.append("/");
		sb.append(resId);
		return sb.toString();
	}


	public static void installApk(Context context, String filePath) {
		File file = new File(filePath);
		if (file.exists()) {
			Log.e("TAG", "installApk……" + filePath);
			Intent intent = new Intent();
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			intent.setAction(Intent.ACTION_VIEW); // 浏览网页的Action(动作)
			String type = "application/vnd.android.package-archive";
			intent.setDataAndType(Uri.fromFile(file), type); // 设置数据类型
			context.startActivity(intent);
		} else {
			Log.e("TAG", "uninstallApk……");
		}

	}


	public static String getSdCardOldFileDirectory(){
		File file = Environment.getExternalStorageDirectory();
		return (file.getAbsolutePath() + File.separator + Constants.FILE_OLD_DIRECTORY);
	}


	public static String getSdCardFileDirectory(){
		File file = Environment.getExternalStorageDirectory();
		return (file.getAbsolutePath() + File.separator + Constants.FILE_DIRECTORY);
	}


	public static String getScreenShotDirectory(){
		String rootPath = getSdCardFileDirectory() + Constants.FILE_DIRECTORY_SCREEN_SHOT;
		return rootPath;
	}

	public static String getVideoDirectory(){
		String rootPath = getSdCardFileDirectory() + Constants.FILE_DIRECTORY_VIDEO;
		return rootPath;
	}

	public static String getCacheDirectory(){
		String rootPath = getSdCardFileDirectory() + Constants.FILE_DIRECTORY_CACHE;
		return rootPath;
	}

	public static String getPrepareVideoDirectory(){
		String rootPath = getSdCardFileDirectory() + Constants.FILE_DIRECTORY_PRE_VIDEO;
		return rootPath;
	}

	public static String getApkDirectory(){
		String rootPath = getSdCardFileDirectory() + Constants.FILE_DIRECTORY_APK;
		return rootPath;
	}

	public static String getLogDirectory(){
		String rootPath = getSdCardFileDirectory() + Constants.FILE_DIRECTORY_LOG;
		return rootPath;
	}


	/**
	 * 从指定文件中读取内容
	 * @param file
	 * @return
	 */
	public static String readFileFromSdCard(File file){
		BufferedReader bufferedReader = null;
		StringBuffer sb = new StringBuffer();
		try{
			bufferedReader = new BufferedReader(new FileReader(file));
			String readLine;
			while ((readLine = bufferedReader.readLine()) != null){
				sb.append(readLine);
			}

		}catch (Exception e){
			e.printStackTrace();
		}finally {
			if(bufferedReader != null){
				try {
					bufferedReader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return String.valueOf(sb.toString());
	}


	/**
	 * 获取设备分辨率--宽--widthPixels
	 */
	public static int getDisplayMetricsWidth(Activity context){
		DisplayMetrics metrics = new DisplayMetrics();
		context.getWindowManager().getDefaultDisplay().getMetrics(metrics);
		//metrics.density
		//LogCat.e("metrics.density  "+ metrics.density +" metrics.densityDpi "+metrics.densityDpi);
         return metrics.widthPixels;
	}


	/**
	 * 获取设备分辨率--高--heightPixels
	 */
	public static int getDisplayMetricsHeight(Activity context){
		DisplayMetrics metrics = new DisplayMetrics();
		context.getWindowManager().getDefaultDisplay().getMetrics(metrics);
		if(checkDeviceHasNavigationBar(context)){
			//适配手机
			return metrics.heightPixels + getNavigationBarHeight(context);
		}else{
			//适配电视棒
			return metrics.heightPixels;
		}

	}

	/**
	 * 获取设备分辨率--高--heightPixels
	 */
	public static int getDisplayMetricsHeightNormal(Activity context){
		DisplayMetrics metrics = new DisplayMetrics();
		context.getWindowManager().getDefaultDisplay().getMetrics(metrics);
		return metrics.heightPixels;
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
	 * NavigationBar的高度
	 * @param activity
	 * @return
	 */
	public static int getNavigationBarHeight(Activity activity) {
		Resources resources = activity.getResources();
		int resourceId = resources.getIdentifier("navigation_bar_height",
				"dimen", "android");
		//获取NavigationBar的高度
		int height = resources.getDimensionPixelSize(resourceId);

		return height;
	}

	/**
	 * 开启验证应用服务
	 */
	public static void startAppServer(Context context){
		Settings.Global.putInt(context.getContentResolver(), "package_verifier_enable", 0);
	}


	/**
	 * 队列比较
	 * @param <T>
	 * @param a
	 * @param b
	 * @return
	 */
	public static <T extends Comparable<T>> boolean compare(List<T> a, List<T> b) {
		if(a.size() != b.size())
			return false;
		Collections.sort(a);
		Collections.sort(b);
		for(int i=0;i<a.size();i++){
			if(!a.get(i).equals(b.get(i)))
				return false;
		}
		return true;
	}


	/**
	 * 随机指定范围内N个不重复的数
	 * 最简单最基本的方法
	 * @param min 指定范围最小值
	 * @param max 指定范围最大值
	 * @param n 随机数个数
	 */
	public static int[] randomCommon(int min, int max, int n){
		int len = max-min+1;

		if(max < min || n > len){
			return null;
		}

		//初始化给定范围的待选数组
		int[] source = new int[len];
		for (int i = min; i < min+len; i++){
			source[i-min] = i;
		}

		int[] result = new int[n];
		Random rd = new Random();
		int index = 0;
		for (int i = 0; i < result.length; i++) {
			//待选数组0到(len-2)随机一个下标
			index = Math.abs(rd.nextInt() % len--);
			//将随机到的数放入结果集
			result[i] = source[index];
			//将待选数组中被随机到的数，用待选数组(len-1)下标对应的数替换
			source[index] = source[len];
		}
		return result;
	}


	public static  void writeFileToSdcard(String filePath, String fileName, String msg){
		BufferedWriter bw = null;
		try {
			File file = new File(filePath);
			if(!file.exists()){
				file.mkdirs();
			}

			File cacheFile = new File(filePath, fileName);
			if(!cacheFile.exists()){
				cacheFile.createNewFile();
			}
			//第二个参数意义是说是否以append方式添加内容
			bw = new BufferedWriter(new FileWriter(cacheFile, false));
			bw.write(msg);
			bw.flush();
			LogCat.e("缓存文件成功......");
		} catch (Exception e) {
			e.printStackTrace();
		}finally {
			try {
				if (bw != null) {
					bw.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}


	public static String getThrowableMsg(Throwable ex){
		StringBuffer sb = new StringBuffer();
		Writer writer = new StringWriter();
		PrintWriter printWriter = new PrintWriter(writer);
		ex.printStackTrace(printWriter);
		Throwable cause = ex.getCause();
		while (cause != null) {
			cause.printStackTrace(printWriter);
			cause = cause.getCause();
		}
		printWriter.close();
		String result = writer.toString();
		sb.append(result); //将写入的结果
		return sb.toString();
	}

//	private float dpFromPx(float px)
//	{
//		return px / this.getContext().getResources().getDisplayMetrics().density;
//	}
//
//	private float pxFromDp(float dp)
//	{
//		return dp * this.getContext().getResources().getDisplayMetrics().density;
//	}




}
