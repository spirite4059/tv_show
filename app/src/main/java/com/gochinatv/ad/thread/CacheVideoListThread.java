package com.gochinatv.ad.thread;

import android.text.TextUtils;

import com.gochinatv.ad.tools.LogCat;
import com.google.gson.Gson;
import com.httputils.http.response.AdDetailResponse;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by fq_mbp on 16/3/25.
 */
public class CacheVideoListThread extends Thread{

    private ArrayList<AdDetailResponse> cacheVideos;

    private String filePath;

    private String fileName;

    public CacheVideoListThread(ArrayList<AdDetailResponse> cacheVideos, String filePath, String fileName){
        this.cacheVideos = cacheVideos;
        this.filePath = filePath;
        this.fileName = fileName;
    }

    @Override
    public void run() {
        if(cacheVideos == null || cacheVideos.size() == 0){
            LogCat.e("无缓存内容，禁止创建缓存文件......");
            return;
        }
        Gson gson = new Gson();
        String json = gson.toJson(cacheVideos);
        if(TextUtils.isEmpty(json)){
            LogCat.e("gson转换对象失败，禁止创建缓存文件......");
            return;
        }
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
            bw.write(json);
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
}
