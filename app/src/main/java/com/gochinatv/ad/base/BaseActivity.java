package com.gochinatv.ad.base;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.gochinatv.ad.tools.Constants;
import com.gochinatv.ad.tools.DataUtils;
import com.gochinatv.ad.tools.LogCat;
import com.gochinatv.ad.tools.UmengUtils;
import com.okhtttp.OkHttpCallBack;
import com.okhtttp.OkHttpUtils;
import com.okhtttp.response.ADDeviceDataResponse;
import com.okhtttp.response.UpdateResponse;
import com.okhtttp.service.ADHttpService;
import com.tools.HttpUrls;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by fq_mbp on 16/5/28.
 */
public class BaseActivity extends Activity{

//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//
//    }

    /**
     * 检查是否有版本更新
     */

    protected void onUpdateSuccess(UpdateResponse response){

    }

    protected void onUpdateError(String errorMsg){

    }





    protected void doHttpUpdate(Context context) {
        Map<String, String> map = getUpdateRequestParams(context);
        OkHttpUtils.getInstance().
                doHttpGet(HttpUrls.URL_CHECK_UPDATE, map, new OkHttpCallBack<UpdateResponse>() {
                    @Override
                    public void onSuccess(String url, UpdateResponse response) {
                        LogCat.e("onSuccess adVideoUrl: " + url);
                        if (isFinishing()) {
                            return;
                        }

                        if (response == null) {
                            LogCat.e("升级数据出错，无法正常升级1。。。。。");
                            onUpdateError("升级数据出错，无法正常升级。。。。。");
                            return;
                        }
                        onUpdateSuccess(response);

                    }

                    @Override
                    public void onError(String url, String errorMsg) {
                        LogCat.e("请求接口出错，无法升级。。。。。" + url);
                        if (isFinishing()) {
                            return;
                        }
                        onUpdateError(errorMsg);
                    }
                });

    }

    @NonNull
    private Map<String, String> getUpdateRequestParams(Context context) {
        Map<String, String> map = new HashMap<>();
        map.put("platformId", String.valueOf("22"));
        if (!TextUtils.isEmpty(android.os.Build.MODEL)) {
            // 不为空
            try {
                map.put("modelNumber", URLEncoder.encode(android.os.Build.MODEL, "utf-8")); // 型号
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
                map.put("modelNumber", android.os.Build.MODEL); // 型号
            }
        }
        try {
            ApplicationInfo appInfo = context.getPackageManager().getApplicationInfo(context.getPackageName(),
                    PackageManager.GET_META_DATA);
            if (appInfo != null) {
                String brand = appInfo.metaData.getString("UMENG_CHANNEL");
                if (TextUtils.isEmpty(brand)) {
                    String name = Constants.isTest ? "ctTest" : "chinarestaurant";//如果是测试：ctTest；否则：chinarestaurant
                    map.put("brandNumber", name); // 品牌
                } else {
                    map.put("brandNumber", brand); // 品牌
                }
            }
        } catch (PackageManager.NameNotFoundException e1) {
            e1.printStackTrace();
            String name = Constants.isTest ? "ctTest" : "chinarestaurant";//如果是测试：ctTest；否则：chinarestaurant
            map.put("brandNumber", name); // 品牌
        }
        return map;
    }


    protected void onGetDeviceInfoSuccess(ADDeviceDataResponse response){

    }

    protected void onGetDeviceInfoError(String msg){

    }

    /**
     * 请求广告体接口---布局大小
     */

    //布局数据
    //布局形式——1：一屏；4：4屏
    protected void doGetDeviceInfo(Context context) {
        ADHttpService.doHttpGetDeviceInfo(context, new OkHttpCallBack<ADDeviceDataResponse>() {
            @Override
            public void onSuccess(String url, ADDeviceDataResponse response) {
                LogCat.e("doGetDeviceInfo url:  " + url);
                if (isFinishing()) {
                    return;
                }
                if (response == null) {
                    LogCat.e("请求广告体接口失败");
                    onGetDeviceInfoError("请求广告体接口失败 response = null");
                    return;
                }

                if (!"0".equals(response.status)) {
                    LogCat.e("请求广告体接口失败 status = 1");
                    onGetDeviceInfoError("请求广告体接口失败 status = 1");
                    return;
                }

                onGetDeviceInfoSuccess(response);
                UmengUtils.onEvent(BaseActivity.this, UmengUtils.UMENG_APP_START_TIME, DataUtils.getFormatTime(response.currentTime));
            }



            @Override
            public void onError(String url, String errorMsg) {
                LogCat.e("请求广告体接口失败。。。。。" + url);
                if (isFinishing()) {
                    return;
                }

                onGetDeviceInfoError(errorMsg);

            }
        });
    }


}
