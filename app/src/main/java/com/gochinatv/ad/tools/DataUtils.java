package com.gochinatv.ad.tools;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.content.res.Resources.NotFoundException;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.StateListDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.nostra13.universalimageloader.cache.memory.MemoryCacheAware;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.umeng.analytics.MobclickAgent;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URL;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Enumeration;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SuppressLint({ "NewApi", "SimpleDateFormat", "DefaultLocale" })
public class DataUtils {
	/**
	 * 网络状态
	 */
	public static boolean networkflag = false;
	/**
	 * 上次网络连接类型
	 */
	public static int oldnetworktype = -1;
	/**
	 * 网络连接类型 网线==9；wifi===1
	 */
	public static int networktype = -1;

	/**
	 * 邮箱正则验证
	 */
	public static final String verifyEmail = "[A-Z0-9a-z._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,4}";
	/**
	 * 手机号码正则验证
	 */
	public static final String verifymobile = "^((13[0-9])|(15[^4,\\D])|(18[0,5-9]))\\d{8}$";

	public static int dpToPx(Resources res, int dp) {
		return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, res.getDisplayMetrics());
	}

	public static int pxToDp(Resources res, int px) {
		return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_PX, px, res.getDisplayMetrics());
	}

	/**
	 * 下载图片
	 * 
	 * @param imageUrl
	 * @return
	 */
	public static Bitmap downImage(String imageUrl) {
		try {
			URL url = new URL(imageUrl);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setDoInput(true);
			conn.connect();
			InputStream is = conn.getInputStream();
			int length = (int) conn.getContentLength();
			if (length != -1) {
				byte[] imgData = new byte[length];
				byte[] temp = new byte[512];
				int readLen = 0;
				int destPos = 0;
				while ((readLen = is.read(temp)) > 0) {
					System.arraycopy(temp, 0, imgData, destPos, readLen);
					destPos += readLen;
				}
				Bitmap bitmap = BitmapFactory.decodeByteArray(imgData, 0, imgData.length);
				return bitmap;
			}
		} catch (MalformedURLException e) {
			e.printStackTrace();
			return null;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
		return null;
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

	public static void uMengStatistics(Context context, String title, String msg) {
		HashMap<String, String> mapAlbumName = new HashMap<String, String>();
		mapAlbumName.put(title, msg);
		MobclickAgent.onEvent(context, title, mapAlbumName);
	}

	/**
	 * 判断字符串是否为"null",如果是返回空串
	 * 
	 * @param str
	 * @return
	 */
	public static String getStrIsNull(String str) {
		if ("null".equals(str) || TextUtils.isEmpty(str)) {
			str = "";
		}
		return str;
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
	 * 保留小数的后两位，不进行四会五入计算
	 * 
	 * @param j
	 * @return
	 */
	public static float getFloatTwo(float j) {
		String sj = String.valueOf(j);
		if (TextUtils.isEmpty(sj) == false && sj.contains(".")) {
			String[] sjs = sj.split("\\.");
			if (sjs.length > 1) {
				if (sjs[1].length() > 3) {
					sjs[1] = sjs[1].substring(0, 2);
				}
				sj = sjs[0] + "." + sjs[1];
				j = Float.valueOf(sj);
			}
		}
		return j;
	}

	/**
	 * 手机号 地址格式
	 * 
	 * @param phone
	 * @return
	 */
	public static boolean isPhone(String phone) {
		Pattern p = Pattern.compile("^1[3-8]+\\d{9}");
		Matcher m = p.matcher(phone);
		return m.matches();
	}

	/**
	 * 是否是正确的密码格式
	 * 
	 * @param pwd
	 * @return
	 */
	public static boolean isRightPwd(String pwd) {
		Pattern p = Pattern.compile("[A-Za-z0-9]+");
		Matcher m = p.matcher(pwd);
		return m.matches();
	}

	public static StringBuffer getCPSID(Context context) {
		StringBuffer buffer = new StringBuffer();
		buffer.append("C_Android_");
		buffer.append(Build.MANUFACTURER);// 设备名称
		buffer.append("_");
		buffer.append(DataUtils.getVersionName(context));// 应用版本号
		buffer.append("_");
		buffer.append(DataUtils.getSIMName(context));// 运营商
		buffer.append("_");
		buffer.append(Build.VERSION.RELEASE);// 系统版本号
		return buffer;
	}

	/**
	 * 邮箱 地址格式
	 *
	 * @param email
	 * @return
	 */
	public static boolean isEmail(String email) {
		// [A-Z0-9a-z._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,4}
		String check = "^([a-z0-9A-Z]+[+%_-|\\.]?)+[a-z0-9A-Z]*@([a-z0-9A-Z]+(-[a-z0-9A-Z]+)?\\.)+[a-zA-Z]{2,4}$";
		Pattern regex = Pattern.compile(check);
		Matcher matcher = regex.matcher(email);
		return matcher.matches();
	}

	// 安装apk文件
	public static void installApk(Context context, String filename) {
		File file = new File(filename);
		Intent intent = new Intent();
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.setAction(Intent.ACTION_VIEW); // 浏览网页的Action(动作)
		String type = "application/vnd.android.package-archive";
		intent.setDataAndType(Uri.fromFile(file), type); // 设置数据类型
		context.startActivity(intent);
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
	 * 取得订单列表页时间查询条件的开始日期
	 *
	 * @param type
	 *            1:最近一周;非1:最近一个月
	 * @return
	 */
	public static String getSearchOrderStartDateStr(String type) {
		Date now = new Date();
		Calendar lastDate = Calendar.getInstance();
		lastDate.setTime(now);
		if ("1".equals(type)) { // 最近一星期
			lastDate.add(Calendar.DATE, -7);
		} else { // 最近一个月
			lastDate.add(Calendar.MONTH, -1);
		}
		SimpleDateFormat yyyymmddFormat = new SimpleDateFormat("yyyy-MM-dd");
		return yyyymmddFormat.format(lastDate.getTime());
	}

	/**
	 * 取得订单列表页时间查询条件的开始日期
	 *
	 * @return
	 */
	public static String getOrderCurrentMonthStr() {
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
		GregorianCalendar gc = (GregorianCalendar) Calendar.getInstance();
		gc.set(Calendar.DAY_OF_MONTH, 1);
		return df.format(gc.getTime());
	}

	// /**
	// * 根据pid cookieUid 生成微信支付请求url
	// */
	// public static String getWXPaymentUrl(String pid, String cookieUid) {
	// if (TextUtils.isEmpty(pid)) {
	// return null;
	// }
	// StringBuilder sbUrl = new StringBuilder(HttpUrl.PRE_WX_PAYMENT_URL);
	// sbUrl.append("pid=");
	// sbUrl.append(pid);
	// sbUrl.append("&cps_id=FL_TV2wap");
	// if (!TextUtils.isEmpty(cookieUid)) {
	// sbUrl.append("_");
	// sbUrl.append(cookieUid);
	// }
	// return sbUrl.toString();
	// }

	/**
	 * 处理价格格式
	 *
	 * @param price
	 * @return
	 */
	public static String editPriceStr(String price) {
		if (TextUtils.isEmpty(price)) {
			LogCat.e("price is null");
			return null;
		}
		// 处理字符串
		String currentStr = (String) price;
		// 1、判断是否还有 “.”
		if (currentStr.contains(".")) {
			// 再判断是否含有 “.0” 或者 “.00”
			if (currentStr.contains(".00")) {
				return currentStr;
			}
			if (currentStr.contains(".0")) {
				return (currentStr + "0");
			}
		} else {
			if ("-".equals(currentStr)) {
				return ("0.00");
			}
			return (currentStr + ".00");
		}

		return null;
	}

	// /**
	// * 获取APP最新版本号
	// *
	// * @author ren
	// * @return
	// */
	// public static UpdateInfo getAppStoreVersion() {
	//
	// String url =
	// "http://thirds.wangjiu.com/app_store/query/appStoreVersion.json?type=4";
	// String json;
	// try {
	//
	// json = HttpUtils.doGET(url);
	//
	// if (TextUtils.isEmpty(json)) {
	// return null;
	// }
	// Log.e("wj", "json:" + json);
	// UpdateInfo updateInfo = null;
	//
	// JSONObject jsonObject = new JSONObject(json);
	//
	// String message = jsonObject.getString("message");
	// if (!"success".equals(message)) {
	// return null;
	// }
	//
	// JSONObject jsonArr = jsonObject.getJSONArray("result")
	// .optJSONObject(0);
	// if (jsonArr != null) {
	// updateInfo = new UpdateInfo();
	// updateInfo.setForceupdate(jsonArr.getString("FORCEUPDATE"));
	// updateInfo.setUpdateurl(jsonArr.getString("UPDATEURL"));
	// updateInfo.setVersionnum(jsonArr.getString("VERSIONNUM"));
	// updateInfo.setVersiondescription(jsonArr
	// .getString("VERSIONDESCRIPTION"));
	// }
	//
	// return updateInfo;
	//
	// } catch (ClientProtocolException e) {
	// e.printStackTrace();
	// } catch (IOException e) {
	// e.printStackTrace();
	// } catch (JSONException e) {
	// e.printStackTrace();
	// }
	//
	// return null;
	// }

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
	public static boolean ExistSDCard() {
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

	/**
	 * 获取购物车商品数量
	 */
	// public static int getShoppingCardCount(Context context) {
	// SharedPreferences share =
	// context.getSharedPreferences(Constants.SHOPPING_CARD_COUNT_SHARE,
	// Context.MODE_PRIVATE);
	// return share.getInt(Constants.SHOPPING_CARD_COUNT, 0);
	// };

	public static Map<String, Object> parseJSON2Map(String jsonStr) {
		Map<String, Object> map = new HashMap<String, Object>();
		try {
			// 最外层解析
			JSONObject json = new JSONObject(jsonStr);
			Iterator<String> iterator = json.keys();
			while (iterator.hasNext()) {
				Object key = iterator.next();
				if (null == key)
					continue;
				Object value = json.get(key.toString());
				// 如果内层还是数组的话，继续解析
				if (value instanceof JSONArray) {
					List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
					JSONArray array = (JSONArray) value;
					for (int i = 0; i < array.length(); i++) {
						if (array.get(i) instanceof JSONObject) {
							JSONObject json2 = (JSONObject) array.get(i);
							list.add(parseJSON2Map(json2.toString()));
						}
					}
					map.put(key.toString(), list);
				} else if (value instanceof JSONObject) {
					map.put(key.toString(), parseJSON2Map(value.toString()));
				} else {
					map.put(key.toString(), value);
				}
			}
		} catch (JSONException e) {
			LogCat.e("解析json出错!!!");
		}

		return map;
	}

	/**
	 * @author needle
	 * @param endTimeStr
	 * @param startTimeStr
	 * @return
	 *
	 *         根据结束和开始时间字符串，获得闪购活动信息息
	 */
	public static String getActivityTimeInfo(String endTimeStr, String startTimeStr) {
		// 结束时间
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date endAt;
		Date startAt;
		try {
			endAt = sdf.parse(endTimeStr);
			startAt = sdf.parse(startTimeStr);
		} catch (ParseException e) {
			return "";
		}

		// 开始时间和当前时间相比较
		long startMillis = startAt.getTime();
		// 活动未开始，返回：还差多长时间活动开始
		long millis = startMillis - System.currentTimeMillis();
		if (millis > 0) {
			return "-" + getTimeLengthStrByMillis(millis);
		}

		// 结束时间和当前时间相比较
		long endMillis = endAt.getTime();
		millis = endMillis - System.currentTimeMillis();
		// 闪购结束，等于0时，刚结束
		if (millis <= 0) {
			return "";// 活动结束
		}

		// 已经开始，还未结束，返回：还差多长时间结束
		return getTimeLengthStrByMillis(millis);
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

	/**
	 * 秒杀列表获取倒计时时间 获取 时 分 秒
	 *
	 * @param
	 * @return
	 */
	public static String getTimeFormatBySeconds(float second) {
		if (second == 0) {
			return "00:00";
		}
		int hour = (int) (second / 3600); // !小时
		int minute = (int) (second % 3600 / 60);// !分钟
		int seconds = (int) (second % 60); // !秒
		if (hour == 0) {
			return String.format("%1$02d:%2$02d", minute, seconds);
		} else if (minute == 0) {
			return String.format("%1$02d:%2$02d", 0, seconds);
		}
		return String.format("%1$02d:%2$02d:%3$02d", hour, minute, seconds);
	}


	/**
	 * 秒杀列表获取倒计时时间 获取 时 分 秒,毫秒
	 *
	 * @param
	 * @return
	 */
	public static String getTimeFormatByMinSeconds(long misecond) {
		if (misecond == 0) {
			return "00分00秒000毫秒";
		}
//		Date date = new Date(second);
//		SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss.SSS");
//		return sdf.format(date).toString();


		Integer ss = 1000;
		Integer mi = ss * 60;
		Integer hh = mi * 60;
		Integer dd = hh * 24;

		Long day = misecond / dd;
		Long hour = (misecond - day * dd) / hh;
		Long minute = (misecond - day * dd - hour * hh) / mi;
		Long second = (misecond - day * dd - hour * hh - minute * mi) / ss;
		Long milliSecond = misecond - day * dd - hour * hh - minute * mi - second * ss;

		StringBuffer sb = new StringBuffer();
		if(day > 0) {
			sb.append(day+":");
		}
		if(hour > 0) {
			sb.append(hour+"时");
		}
		if(minute > 0) {
			sb.append(minute+"分");
		}
		if(second > 0) {
			sb.append(second+"秒");
		}
		if(milliSecond > 0) {
			sb.append(milliSecond+"毫秒");
		}
		return sb.toString();


	}



	public static byte[] bmpToByteArray(final Bitmap bmp, final boolean needRecycle) {
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		bmp.compress(CompressFormat.PNG, 100, output);
		if (needRecycle) {
			bmp.recycle();
		}

		byte[] result = output.toByteArray();
		try {
			output.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return result;
	}

	public static String getStartTimeFormat() {
		return "2010-01-01";
	}

	public static String getCurTimeFormat() {
		Date date = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
		return sdf.format(date);
	}

	/**
	 * 图形处理 进行缩放
	 */
	public static Bitmap computeBitmap(Context context, int resId, int width, int height) {
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		options.inPreferredConfig = Bitmap.Config.ARGB_8888; // 默认是Bitmap.Config.ARGB_8888
		/* 下面两个字段需要组合使用 */
		options.inPurgeable = true;
		options.inInputShareable = true;
		Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), resId, options);
		/* 计算得到图片的高度 */
		/* 这里需要主意，如果你需要更高的精度来保证图片不变形的话，需要自己进行一下数学运算 */
		options.outWidth = width;
		options.outHeight = height;
		/* 这样才能真正的返回一个Bitmap给你 */
		options.inJustDecodeBounds = false;
		bitmap = BitmapFactory.decodeResource(context.getResources(), resId, options);
		return bitmap;
	}

	/**
	 * 回收bitmap
	 */
	public static void recyleBitmap(Bitmap bmp) {
		if (bmp != null) {
			bmp.recycle();
			bmp = null;
			System.gc();
			System.gc();
		}
	}

	/**
	 * 去掉金钱后面的“.00”或者“.0”
	 */
	public static String getSimpleMoneyStr(String money) {
		if (TextUtils.isEmpty(money)) {
			return "0";
		}
		if (money.contains(".")) {
			money = money.substring(0, money.indexOf("."));
		}
		return money;
	}

	/**
	 * 判断字符串是否为空或是否是“null”
	 *
	 * @param str
	 * @return
	 */
	public static boolean isHasNullStr(String str) {
		return TextUtils.isEmpty(str) || "null".equals(str);
	}

	/**
	 * 在金钱后面加上“.00”
	 */
	public static String getSimpleMoneyZeroStr(String money) {
		LogCat.e("getSimpleMoneyZeroStr()----> " + money);
		if (TextUtils.isEmpty(money)) {
			return "0.00";
		}
		if (!money.contains(".")) {
			money += ".00";
		} else {
			String[] strs = money.split("\\.");
			LogCat.e("getSimpleMoneyZeroStr()----> " + strs.length);
			if (strs.length != 0) {
				if (strs[1].length() == 1) {
					strs[1] += "0";
				}
				money = strs[0] + "." + strs[1];
			}
		}
		return money;
	}

	/**
	 * 获取两位数时间字符串
	 */
	public static String getDoubleTimeStr(int time) {
		String timeStr = null;
		if (time < 10) {
			timeStr = "0" + time;
		} else {
			return String.valueOf(time);
		}
		return timeStr;
	}

	/**
	 * 添加背景bitmap
	 */
	@SuppressWarnings("deprecation")
	public static void setBackgroudBitmap(Context context, View view, Bitmap bmp) {
		if (view == null || (bmp != null && bmp.isRecycled())) {
			return;
		}
		int sdk = getSystemSDKVersion();
		if (sdk < 16) {
			view.setBackgroundDrawable(new BitmapDrawable(context.getResources(), bmp));
		} else {
			view.setBackground(new BitmapDrawable(context.getResources(), bmp));
		}

	}

	@SuppressWarnings("deprecation")
	public static void setBackgroudBitmap(Context context, View view, int resId) {
		if (resId == 0 || view == null) {
			return;
		}

		Bitmap bmp = readBitMap(context, resId);
		if (bmp == null || bmp.isRecycled()) {
			LogCat.e("图片不存在或已经回收");
			return;
		}
		int sdk = getSystemSDKVersion();
		if (sdk < 16) {
			view.setBackgroundDrawable(new BitmapDrawable(context.getResources(), bmp));
		} else {
			view.setBackground(new BitmapDrawable(context.getResources(), bmp));
		}
	}

	// readBitmapSize
	/**
	 * 获取设备唯一标识
	 */
	// public static String getDeviceUniqueIdentification(Activity context) {
	// String mac = getLocalMacAddress();
	// if (TextUtils.isEmpty(mac)) {
	// mac = getWIFIMacAddress(context);
	// }
	// StringBuilder uniqueKey = new StringBuilder("wangjiu.tv");
	// uniqueKey.append(mac);
	// uniqueKey.append(System.currentTimeMillis());
	// return com.wangjiu.tv.utils.MD5.digest(uniqueKey.toString());
	// }

	public static String generateString(int length) {
		String allChar = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
		StringBuffer sb = new StringBuffer();
		Random random = new Random();
		for (int i = 0; i < length; i++) {
			sb.append(allChar.charAt(random.nextInt(allChar.length())));
		}
		return sb.toString();
	}

	private static String loadFileAsString(String filePath) throws IOException {
		StringBuffer fileData = new StringBuffer(1000);
		BufferedReader reader = new BufferedReader(new FileReader(filePath));
		char[] buf = new char[1024];
		int numRead = 0;
		while ((numRead = reader.read(buf)) != -1) {
			String readData = String.valueOf(buf, 0, numRead);
			fileData.append(readData);
		}
		reader.close();
		return fileData.toString();
	}

	/** Get the STB MacAddress */

	public static String getLocalMacAddress() {
		try {
			return loadFileAsString("/sys/class/net/eth0/address").toUpperCase().substring(0, 17);
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	public static String getLocalIpAddress() {
		try {
			for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
				NetworkInterface intf = en.nextElement();
				for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
					InetAddress inetAddress = enumIpAddr.nextElement();
					if (!inetAddress.isLoopbackAddress()) {
						return inetAddress.getHostAddress().toString();
					}
				}
			}
		} catch (SocketException ex) {
			System.out.println("WifiPreference IpAddress" + ex.toString());
		}
		return null;
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

	/**
	 * 以最省内存的方式读取本地资源的图片
	 *
	 * @param context
	 * @param resId
	 * @return
	 */
	public static Bitmap readBitMap(Context context, int resId) {
		BitmapFactory.Options opt = new BitmapFactory.Options();
		opt.inPreferredConfig = Bitmap.Config.ARGB_8888;
		// opt.inSampleSize = 2;
		opt.inJustDecodeBounds = false;// 这里一定要将其设置回false，因为之前我们将其设置成了true
		opt.inPurgeable = true;
		opt.inInputShareable = true;
		opt.inDither = false;
		opt.inPurgeable = true;
		// 获取资源图片
		InputStream is = context.getResources().openRawResource(resId);
		return BitmapFactory.decodeStream(is, null, opt);
	}

	/**
	 * 以最省内存的方式读取本地资源的图片
	 *
	 * @param context
	 * @param resId
	 * @return
	 */
	public static Bitmap readBitmapSize(Context context, int resId) {
		BitmapFactory.Options opt = new BitmapFactory.Options();
		opt.inPreferredConfig = Bitmap.Config.RGB_565;
		opt.inJustDecodeBounds = false;// 这里一定要将其设置回false，因为之前我们将其设置成了true
		opt.inPurgeable = true;
		opt.inInputShareable = true;
		opt.inDither = false;
		opt.inPurgeable = true;
		opt.inSampleSize = 2;
		// 获取资源图片
		InputStream is = context.getResources().openRawResource(resId);
		return BitmapFactory.decodeStream(is, null, opt);
	}

	public static int getBitmapSize(Bitmap data) {
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB_MR1) {
			return data.getRowBytes() * data.getHeight();
		} else {
			return data.getByteCount();
		}
	}

	public static boolean isNetworkConnected(Context context) {
		if (context != null) {
			ConnectivityManager mConnectivityManager = (ConnectivityManager) context
					.getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo mNetworkInfo = mConnectivityManager.getActiveNetworkInfo();
			if (mNetworkInfo != null) {
				DataUtils.networkflag = mNetworkInfo.isAvailable();
				return DataUtils.networkflag;
			}
		}
		return false;
	}

	/**
	 * 获取网络状态
	 *
	 * @param context
	 * @return
	 */
	public static void setNetworkStatus(Context context) {
		ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo[] networks = cm.getAllNetworkInfo();
		for (int i = 0; i < networks.length; i++) {
			if (networks[i].getState() == NetworkInfo.State.CONNECTED) {
				if (DataUtils.networktype == -1) {
					DataUtils.networktype = networks[i].getType();
					DataUtils.networkflag = true;
					DataUtils.oldnetworktype = DataUtils.networktype;
				}
			}
		}
	}

	/**
	 * 图片地址重定义
	 *
	 * @param url
	 *            图片地址
	 * @param suffix
	 *            图片后缀
	 */
	// public static String getImgUrl(String url, String suffix) {
	// if (url == null || "".equals(url))
	// return null;
	// // Random i = new Random();
	// // int j = i.nextInt(10);
	// if (url.contains("http://")) {
	// String[] strs = url.split(".com/");
	// if (strs.length == 2) {
	// url = HttpUrl.PRE_IMAGE_URL + strs[1] + suffix;
	// // url = "http://img" + j + ".wangjiu.com/" + strs[1] + suffix;
	// }
	//
	// } else {
	// url = HttpUrl.PRE_IMAGE_URL + url + suffix;
	// // url = "http://img" + j + ".wangjiu.com/" + url + suffix;
	// }
	// return url;
	// }

	/**
	 * 获取当天的最后时间 例如：2014-07-07 23:59
	 */
	public static String getCalenderEndDate() {
		// 结束时间
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		Date endAt = new Date();
		String endTime = sdf.format(endAt) + " 23:59";
		return endTime;
	}

	public static boolean isCacheBitmapByUrl(String url) {
		if (TextUtils.isEmpty(url)) {
			return false;
		}
		ImageLoader imageLoader = ImageLoader.getInstance();
		MemoryCacheAware<String, Bitmap> cache = imageLoader.getMemoryCache();
		Collection<String> keys = cache.keys();
		String[] urls = new String[keys.size()];
		keys.toArray(urls);
		boolean isHasCache = false;
		for (String urlStr : urls) {
			if (!TextUtils.isEmpty(urlStr)) {
				if (urlStr.contains(url)) {
					isHasCache = true;
					break;
				}
			}

		}
		return isHasCache;

	}

	/**
	 * 验证
	 *
	 * @param regex
	 *            正则表达式
	 * @param srcString
	 *            要验证的字符串
	 */
	public static boolean validateRegex(String regex, String srcString) {
		Pattern p = Pattern.compile(regex);
		Matcher m = p.matcher(srcString);
		if (!m.matches()) {
			return false;
		} else {
			return true;
		}
	}

	private static int lineEndIndex;

	public static int ellipisizeTextView(final TextView tv, final int line) {
		final ViewTreeObserver observer = tv.getViewTreeObserver(); // textAbstract为TextView控件
		observer.addOnGlobalLayoutListener(new OnGlobalLayoutListener() {

			@SuppressWarnings("deprecation")
			@Override
			public void onGlobalLayout() {
				if (observer.isAlive()) {
					observer.removeGlobalOnLayoutListener(this);
				}
				if (tv.getLineCount() > line) {
					lineEndIndex = tv.getLayout().getLineEnd(line - 1); // 设置第六行打省略号
					tv.setText(tv.getText().subSequence(0, lineEndIndex - 1) + "…");
				}
			}
		});
		return lineEndIndex;
	}

	/**
	 * 去掉金钱后面的“.00”或者“.0”
	 */
	public static String getSimpleMoeyStr(String money) {
		if (TextUtils.isEmpty(money)) {
			return null;
		}
		if (money.contains(".")) {
			money = money.substring(0, money.indexOf("."));
		}
		return money;
	}

	public static Date getTodayStartTime() {
		Calendar todayStart = Calendar.getInstance();
		todayStart.set(Calendar.HOUR_OF_DAY, 0);
		todayStart.set(Calendar.MINUTE, 0);
		todayStart.set(Calendar.SECOND, 0);
		todayStart.set(Calendar.MILLISECOND, 0);
		return todayStart.getTime();
	}

	public static Date getTodayEndTime() {
		Calendar todayEnd = Calendar.getInstance();
		todayEnd.set(Calendar.HOUR_OF_DAY, 23);
		todayEnd.set(Calendar.MINUTE, 59);
		todayEnd.set(Calendar.SECOND, 59);
		todayEnd.set(Calendar.MILLISECOND, 999);
		return todayEnd.getTime();
	}

	/**
	 * 倒计时时间
	 *
	 * @param millisecond
	 * @return
	 */
	public static String getCountdownTime(long millisecond) {

		long second = millisecond / 1000;

		long days = second / 60 / 60 / 24;
		long hours = (second / 60 / 60) % 24;
		long minutes = (second / 60) % 60;
		long seconds = second % 60;
		return String.format("%1$02d天%2$02d时%3$02d分%4$02d秒", days, hours, minutes, seconds);
	}

	/**
	 * 秒杀列表获取倒计时时间 获取 时 分 秒
	 *
	 * @param millisecond
	 * @return
	 */
	public static String getSeckillHMSTime(long millisecond) {
		long second = millisecond / 1000;
		long hours = (second / 60 / 60) % 24;
		long minutes = (second / 60) % 60;
		long seconds = second % 60;
		return String.format("%1$02d:%2$02d:%3$02d", hours, minutes, seconds);
	}

	/**
	 * 秒杀列表获取倒计时时间 获取 时 分 秒
	 *
	 * @param millisecond
	 * @return
	 */
	public static String getSeckillHMTime(long millisecond) {
		long second = millisecond / 1000;
		long hours = (second / 60 / 60) % 24;
		long minutes = (second / 60) % 60;
		return String.format("%1$02d:%2$02d", hours, minutes);
	}

	/**
	 * 判断时间状态
	 *
	 * @param
	 * @return -1：已经结束；0：正在进行；1：即将开始
	 */
	public static int getSeckillTimeStatus(long startTime, long endTime) {
		long currentTime = System.currentTimeMillis();
		int status = -1;
		if (currentTime > endTime) {
			status = -1;
		} else if (currentTime < startTime) {
			status = 1;
		} else {
			status = 0;
		}

		return status;
	}

	/**
	 * 获取格式化时间 yyyy-MM-dd HH:mm:ss
	 *
	 * @param
	 * @return
	 */
	public static String getFormatTime(String format, Date date) {
		SimpleDateFormat sdf = new SimpleDateFormat(format);
		return sdf.format(date);
	}

	public static Date getFormatTimeDate(String format, String time) throws ParseException {
		SimpleDateFormat sdf = new SimpleDateFormat(format);
		return sdf.parse(time);
	}

	/**
	 * 获取格式化时间 yyyy-MM-dd HH:mm:ss
	 *
	 * @param
	 * @return
	 */
	public static String getCurrentFormatTime(String format) {
		Calendar calendar = Calendar.getInstance(Locale.getDefault());
		SimpleDateFormat sdf = new SimpleDateFormat(format);
		return sdf.format(calendar.getTime());
	}

	public static String getCurrentFormatTime(String format, Locale locale) {
		Calendar calendar = Calendar.getInstance(locale);
		SimpleDateFormat sdf = new SimpleDateFormat(format);
		return sdf.format(calendar.getTime());
	}


	public static void setTextColorSelector(Resources res, TextView view, int colorId) throws XmlPullParserException,
			IOException {
		XmlPullParser xrp = res.getXml(colorId);
		ColorStateList csl = ColorStateList.createFromXml(res, xrp);
		view.setTextColor(csl);

	}

	public static String getFormatJsonStr(String jsonp) {
		if (TextUtils.isEmpty(jsonp)) {
			return null;
		}
		if (jsonp.contains("jsonp")) {
			return jsonp.replace("jsonp", "json");
		}
		return jsonp;

	}

	/**
	 *
	 * @param res
	 * @param normalResId
	 *            常态
	 * @param focusedResId
	 *            获取焦点
	 * @param checkedResId
	 *            被选中
	 * @return
	 */
	public static StateListDrawable getSelectorByDrawable(Resources res, int normalResId, int focusedResId,
			int checkedResId) {
		StateListDrawable drawable = new StateListDrawable();
		// 常态
		drawable.addState(new int[] { -android.R.attr.state_focused, -android.R.attr.state_checked },
				res.getDrawable(normalResId));
		// 获取焦点状态
		drawable.addState(new int[] { android.R.attr.state_focused }, res.getDrawable(focusedResId));
		// 被选中的状态
		drawable.addState(new int[] { -android.R.attr.state_focused, android.R.attr.state_checked },
				res.getDrawable(checkedResId));
		return drawable;
	}

	/**
	 * 得到当前全球性时间 alendar
	 */
	public static String getLocaleCalendarDate() {
		Calendar calendar = Calendar.getInstance(Locale.getDefault());
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
		return dateFormat.format(calendar.getTime());
	}

	/**
	 * 得到当前时间的前一天日期
	 */
	public static String getBeforeCalendarDate() {
		Calendar calendar = Calendar.getInstance(Locale.getDefault());
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
		calendar.add(Calendar.DATE, -1);
		return dateFormat.format(calendar.getTime());
	}

	/**
	 * 判断当前系统语言是否为中文
	 */
	public static boolean isZH(Context context) {

		try {
			ApplicationInfo appInfo = context.getPackageManager().getApplicationInfo(context.getPackageName(),
					PackageManager.GET_META_DATA);

			if("amazon".equals(appInfo.metaData.getString("UMENG_CHANNEL"))){
				return true;
			}else {
				Locale locale = context.getResources().getConfiguration().locale;
				String language = locale.getLanguage();
				if (language.endsWith("zh"))
					return true;
				else
					return false;
			}

		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}


		return false;
	}

//	/**
//	 * 判断当前系统语言是否为中文--亚马逊盒子特用
//	 */
//	public static boolean isZH(Context context) {
//
//		return true;
//	}



	/**
	 * 判断时间是否是字符串:是：true；否：false
	 */
	public static boolean showTimeIsString(String time) {
		if (time.length() < 5) {
			return true;
		} else {
			if (time.contains("-")) {
				return true;
			} else {
				return false;
			}
		}

	}

	/**
	 * 读取raw文件夹下的json文件
	 *
	 * @param context
	 * @param intResource
	 * @return
	 */
	public static String readFile(Context context, int intResource) {
		Resources res = context.getResources();
		InputStream in = null;
		BufferedReader br = null;
		StringBuffer sb = new StringBuffer();
		sb.append("");
		try {
			in = res.openRawResource(intResource);
			String str;
			br = new BufferedReader(new InputStreamReader(in, "UTF-8"));
			while ((str = br.readLine()) != null) {
				sb.append(str);
			}
		} catch (NotFoundException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (in != null) {
					in.close();
				}
				if (br != null) {
					br.close();
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return sb.toString();
	}

	/**
	 * 生成二维码
	 */
//	public static Bitmap createImage(String str, int size) {
//		if(TextUtils.isEmpty(str) || size <= 0){
//			return null;
//		}
//		try {
//			Hashtable<EncodeHintType, String> hints = new Hashtable<EncodeHintType, String>();
//			hints.put(EncodeHintType.CHARACTER_SET, "utf-8");
//			BitMatrix matrix = new MultiFormatWriter().encode(str, BarcodeFormat.QR_CODE, 300, 300);
//			int width = matrix.getWidth();
//			int height = matrix.getHeight();
//			int[] pixels = new int[width * height];
//
//			for (int y = 0; y < height; y++) {
//				for (int x = 0; x < width; x++) {
//					if (matrix.get(x, y)) {
//						pixels[y * width + x] = 0xff000000;
//					} else {
//						pixels[y * width + x] = 0xfff1f1f1;
//					}
//				}
//			}
//			Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
//			bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
//			return bitmap;
//
//		} catch (WriterException e) {
//			e.printStackTrace();
//		}
//		return null;
//	}

	/**
	 * 生成Did，唯一标示
	 */
	public static String getDID(Context context){
		String wifiMac = getWIFIMacAddress(context);
	    if(TextUtils.isEmpty(wifiMac)){
	    	String localMac = getLocalMacAddress();
	    	if(!TextUtils.isEmpty(localMac)){
	    		return localMac.replaceAll(":", "");
	    	}else{
	    		return null;
	    	}
	    }else{
	    	//return "bc307d3a8de8";
	    	return wifiMac.replaceAll(":", "");
	    }
	}

	/**
	 * url加密
	 * @param paramString
	 * @return
	 */
	public static String toURLEncoded(String paramString) {
        if (paramString == null || paramString.equals("")) {
            return "";
        }
        try
        {
            String str = new String(paramString.getBytes(), "UTF-8");
            str = URLEncoder.encode(str, "UTF-8");
            return str;
        }
        catch (Exception localException) {
        }
        return "";
    }

	/**
     * 将实体类转换成json字符串
     * @param
     * @param o 实体类型
     * @return
     */
    public static String getJsonStringByEntity(Object o) {
            String strJson = "";
            Gson gson = new Gson();
            strJson = gson.toJson(o);
            return strJson;
    }


	/**
	 * 时间处理方法:套餐时间
	 */
    public static String getTimeFromOrder(String time) {
    	StringBuffer buffer  = new StringBuffer();
    	buffer.append(time.substring(0, 4)+"-");
    	buffer.append(time.substring(4, 6)+"-");
    	buffer.append(time.substring(6, 8));
		return buffer.toString();

    }

	/**
	 * 处理string得到数字
	 */
	public static String getNumFromString(String str) {
		String regEx="[^0-9]";
		Pattern p = Pattern.compile(regEx);
		Matcher m = p.matcher(str.trim());
		return  m.replaceAll("").trim();

	}


//	/**
//	 * 去掉时间格式，得到yyyyMMddhhmmss
//	 */
//	public static String getTimeWithNoFormat(Context context) {
//		long timeDifference = VegoSharedPreference.getSharedPreferenceUtils(context).getDate(Constants.VEGO_LOG_TIME, Long.valueOf(0));//取出时间差
//		Calendar calendar = Calendar.getInstance(Locale.getDefault());
//		calendar.setTimeInMillis(System.currentTimeMillis()- timeDifference);
//		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//		String time = sdf.format(calendar.getTime());
//		LogCat.e("getTimeWithNoFormat "  + time.replaceAll("-","").replaceAll(" ","").replaceAll(":", "").trim());
//		return time.replaceAll("-","").replaceAll(" ","").replaceAll(":", "").trim();
//	}


	/**
	*两个时间向减，得到播放时间
	 */
	public static String getPlayTime(long start,long stop){
		long time = stop-start;
		int playTime = (int)time/1000;
		return String.valueOf(playTime);
	}

	/**
	 *两个时间向减，得到播放时间
	 */
	public static String getBufferTime(long start,long stop){
		long time = stop-start;
		int playTime = (int)time;
		return String.valueOf(playTime);
	}


	/**
	 * post请求的header
	 */
	public static  Map<String,String> getPostHeaderMap(){
		Map<String,String> header = new HashMap<String,String>();
		header.put("Accept", "application/json");
		header.put("Content-Type","application/json; charset=UTF-8");
		return header;
	}


//	/**
//	 * 设置语言
//	 */
//	public static void switchLanguage(Context context){
//		try {
//
//
//			ApplicationInfo appInfo = context.getPackageManager().getApplicationInfo(context.getPackageName(),
//					PackageManager.GET_META_DATA);
//			//设置语言类型
//			Resources resources = context.getResources();
//			Configuration configuration = resources.getConfiguration();
//			DisplayMetrics dm =  resources.getDisplayMetrics();
//            List<String> channelName = new ArrayList<String>();
//			channelName.add(Constants.VEGO_CHANNEL_AMAZON);
//			channelName.add(Constants.VEGO_CHANNEL_MI);
//			channelName.add(Constants.VEGO_CHANNEL_WANGXUN);
//			channelName.add(Constants.VEGO_CHANNEL_WUKONG);
//			channelName.add(Constants.VEGO_CHANNEL_SHAFA);
//			channelName.add(Constants.VEGO_CHANNEL_TVPAD);
//			channelName.add(Constants.VEGO_CHANNEL_JINGLING);
//			channelName.add(Constants.VEGO_CHANNEL_G1);
//			channelName.add(Constants.VEGO_CHANNEL_JIEKE);
//			channelName.add(Constants.VEGO_CHANNEL_GOOGLE);
//			channelName.add(Constants.VEGO_CHANNEL_JIAHE);
//			channelName.add(Constants.VEGO_CHANNEL_GLWIZ);
//			channelName.add(Constants.VEGO_CHANNEL_ZHIXIANG);
//
//
//			if(channelName.contains(appInfo.metaData.getString(Constants.VEGO_TAG_UMENG_CHANNEL))
//					&& configuration.locale != Locale.SIMPLIFIED_CHINESE){
//				LogCat.e("amazon change language zh");
//				configuration.locale = Locale.SIMPLIFIED_CHINESE;
//			}
//
//			resources.updateConfiguration(configuration,dm);
//
//		} catch (NameNotFoundException e) {
//			e.printStackTrace();
//		}
//
//
//	}



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



	private static String getMacAddress(Context context) {
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
	 *获取设备的user-agent
	 */
	public static String getUserAgentInfo(Context context){
		WebView webview;
		webview = new WebView(context);
		WebSettings settings = webview.getSettings();
		String userAgentString = settings.getUserAgentString();
        return userAgentString;
	}

//	/**
//	 *获取渠道名称
//	 */
//	public static String getUMENGChannelName(Context context){
//		String chinnel = "";
//		try {
//			ApplicationInfo appInfo = context.getPackageManager().getApplicationInfo(context.getPackageName(),
//					PackageManager.GET_META_DATA);
//			if(appInfo != null){
//				 chinnel = appInfo.metaData.getString(Constants.VEGO_TAG_UMENG_CHANNEL);
//				try {
//					chinnel = URLEncoder.encode(chinnel,"utf-8");
//				} catch (UnsupportedEncodingException e) {
//					e.printStackTrace();
//				}
//			}
//
//		} catch (NameNotFoundException e) {
//			e.printStackTrace();
//		}
//        return chinnel;
//
//	}


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
		LogCat.e("TotalMemory:"+(initial_memory));
		if(initial_memory>memory){
			isLowMemory = false;
		}else{
			LogCat.e("内存小于1G");
			isLowMemory = true;
		}
		LogCat.e("isLowMemory:"+isLowMemory);
		return isLowMemory;

	}


//	/**
//	 * 是否是正式渠道
//	 */
//
//	public static boolean isCOAChannel(Context context){
//
//		String name = getUMENGChannelName(context);
//
//		if(!TextUtils.isEmpty(name)){
//			List<String> channelName = new ArrayList<String>();
//			channelName.add(Constants.VEGO_CHANNEL_GOOGLE);
//			channelName.add(Constants.VEGO_CHANNEL_COOCAA);
//			channelName.add(Constants.VEGO_CHANNEL_EOSTEK);
//			channelName.add(Constants.VEGO_CHANNEL_JCG);
//			channelName.add(Constants.VEGO_CHANNEL_SONIQ);
//
//			if(channelName.contains(name)){
//				return true;
//			}else{
//				return false;
//			}
//		}else{
//			return false;
//		}
//
//	}



	public static String getRawVideoUri(Context context, int resId){
		StringBuilder sb = new StringBuilder("android.resource://");
		sb.append(context.getPackageName());
		sb.append("/");
		sb.append(resId);
		return sb.toString();
	}



}
