package com.download.tools;

import android.os.Environment;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.httputils.http.response.AdDetailResponse;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by fq_mbp on 16/4/8.
 */
public class ToolUtils {

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




    public static synchronized ArrayList<AdDetailResponse> getCacheList() {
        ArrayList<AdDetailResponse> cacheTomorrowList = new ArrayList<>();
        File cacheFile = new File(ToolUtils.getCacheDirectory() + Constants.FILE_CACHE_TD_NAME);
        if (cacheFile.exists() && cacheFile.isFile()) {
            String json = ToolUtils.readFileFromSdCard(cacheFile);
            if (!TextUtils.isEmpty(json)) {
                Gson gson = new Gson();
                LogCat.e("缓存的列表已经找到........");
                cacheTomorrowList = gson.fromJson(json, new TypeToken<ArrayList<AdDetailResponse>>() {
                }.getType());
                // TODO 以后删除
                LogCat.e("播放列表内容........");
                for (AdDetailResponse adDetailResponse : cacheTomorrowList) {
                    LogCat.e("视频名称：" + adDetailResponse.adVideoName);
                }

            }
        }
        return cacheTomorrowList;
    }

    public static String getCacheDirectory(){
        String rootPath = getSdCardFileDirectory() + Constants.FILE_DIRECTORY_CACHE;
        return rootPath;
    }

    private static String getSdCardFileDirectory(){
        File file = Environment.getExternalStorageDirectory();
        return (file.getAbsolutePath() + File.separator + Constants.FILE_DIRECTORY);
    }


    /**
     * 从指定文件中读取内容
     * @param file
     * @return
     */
    private static String readFileFromSdCard(File file){
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


    public static File createFile(String path, String fileName) {
        File file = new File(path);
        // 如果SD卡目录不存在创建
        if (!file.exists()) {
            file.mkdirs();
        }

        File apkFile = new File(path, fileName);
        if (!apkFile.exists()) {
            try {
                apkFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return apkFile;
    }
}
