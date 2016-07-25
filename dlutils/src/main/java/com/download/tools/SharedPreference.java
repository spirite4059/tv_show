package com.download.tools;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class SharedPreference {

	private static SharedPreference instance;
	
	private SharedPreferences sp;
	
	private Context context;

	private String VEGO_SHAREDPREFERENCE = "DL_SHAREDPREFERENCE";
	
	public static SharedPreference getSharedPreferenceUtils(Context context){
		if(instance == null)
			instance = new SharedPreference();
		instance.context = context;
		return instance;
	}
	
	/**
	 *  保存数据
	 * @return
	 */ 
	public void saveDate(String key,String value){
		if(sp == null)
		   sp = context.getSharedPreferences(VEGO_SHAREDPREFERENCE,Context.MODE_PRIVATE);
		Editor editor = sp.edit();  
		editor.putString(key, value);  
		editor.commit();
	}
	
	/**
	 *  保存数据
	 * @param key
	 * @return
	 */ 
	public void saveDate(String key,long value){
		if(sp == null)
		   sp = context.getSharedPreferences(VEGO_SHAREDPREFERENCE,Context.MODE_PRIVATE);
		Editor editor = sp.edit();  
		editor.putLong(key, value);  
		editor.commit();
	}
	
	/**
	 *  保存数据
	 * @return
	 */ 
	public void saveDate(String key,boolean value){
		
		if(sp == null)
			 sp = context.getSharedPreferences(VEGO_SHAREDPREFERENCE, Context.MODE_PRIVATE);
	    Editor editor = sp.edit();    
		editor.putBoolean(key, value);  
		editor.commit();
	} 
	
	/**
	 *  取出数据
	 * @return
	 */
	public String getDate(String key,String defValue){
		
		if(sp == null)
			 sp = context.getSharedPreferences(VEGO_SHAREDPREFERENCE, Context.MODE_PRIVATE);
		return sp.getString(key, defValue);
	}
	/**
	 *  取出数据
	 * @return
	 */
	public boolean getDate(String key,boolean defValue){
		
		if(sp == null)
			sp = context.getSharedPreferences(VEGO_SHAREDPREFERENCE, Context.MODE_PRIVATE);
		return sp.getBoolean(key, defValue);
	}
	
	/**
	 *  取出数据
	 * @return
	 */
	public long getDate(String key,Long defValue){
		
		if(sp == null)
			sp = context.getSharedPreferences(VEGO_SHAREDPREFERENCE, Context.MODE_PRIVATE);
		return sp.getLong(key, defValue);
	}
	
//	/**
//	 * 添加单个cook值
//	 * @param key
//	 * @param value
//	 */
//	public void addCookie(String key, String value) {
//		if(spCooks == null)
//			 spCooks = context.getSharedPreferences(Constants.WANGJIU_PHONE_COOKS, Context.MODE_APPEND);
//		SharedPreferences.Editor mEditor = spCooks.edit();
//		mEditor.putString(key, value);  
//        mEditor.commit(); 
//	}
//	
//	//获取cook
//	public String getCookie(String key) {
//		if(spCooks == null)
//			 spCooks = context.getSharedPreferences(Constants.WANGJIU_PHONE_COOKS, Context.MODE_APPEND);
//		return spCooks.getString(key, "");
//	}
//	
//	
//	/**
//	 * 获取全部cook数据
//	 * @return
//	 */
//	public Map<String, String> getSLinkdataCookie() {
//		
//		Map<String, String> cookieMap = new HashMap<String, String>();
//		String slinkdata = this.getCookie(Constants.COOKIE_S_LINKDATA);
//		String[] keyAndValues = slinkdata.split("&");
//		for (String keyAndValue : keyAndValues){
//			String[] arr = keyAndValue.split("=");
//			String value = arr.tlength < 2 ? "" : arr[1];
//			cookieMap.put(arr[0], value);
//		}
//		return cookieMap;
//	}
//	
//	/**
//	 * 根据Key获取全部cook数据
//	 * @return
//	 */
//	public Map<String, String> getSLinkdataCookie(String key) {
//		
//		Map<String, String> cookieMap = new HashMap<String, String>();
//		String slinkdata = this.getCookie(key);
//		String[] keyAndValues = slinkdata.split("&");
//		for (String keyAndValue : keyAndValues){
//			if(TextUtils.isEmpty(keyAndValue)){
//				continue;
//			}
//			String[] arr = keyAndValue.split("=");
//			String value = arr.tlength < 2 ? "" : arr[1];
//			cookieMap.put(arr[0], value);
//		}
//		return cookieMap;
//	}
//	
//	/**
//	 * 多项添加cook
//	 * @param cookieMap
//	 * @return
//	 */
//	public boolean addCookies(Map<String, Object> cookieMap) {
//		if (cookieMap == null) return false;
//		if(spCooks == null)
//			 spCooks = context.getSharedPreferences(Constants.WANGJIU_PHONE_COOKS, Context.MODE_APPEND);
//		
//		SharedPreferences.Editor mEditor = spCooks.edit();
//		Iterator<Entry<String, Object>> it = cookieMap.entrySet().iterator();
//		while (it.hasNext()) {
//			Entry<String, Object> entry = it.next();
//			mEditor.putString(entry.getKey(), String.valueOf(entry.getValue()));
//		}
//        mEditor.commit();
//		return true;
//	}
	
}
