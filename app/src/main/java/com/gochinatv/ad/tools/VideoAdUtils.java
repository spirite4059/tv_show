package com.gochinatv.ad.tools;

import android.content.Context;
import android.text.TextUtils;

import com.download.DLUtils;
import com.gochinatv.ad.thread.CacheVideoListThread;
import com.gochinatv.ad.thread.DeleteFileUtils;
import com.gochinatv.db.AdDao;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.okhtttp.response.AdDetailResponse;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by fq_mbp on 16/4/15.
 */
public class VideoAdUtils {

    /**
     * 缓存视频
     * @param fileName
     * @param cachePlayVideoLists
     */
    public static synchronized void cacheVideoList(String fileName, ArrayList<AdDetailResponse> cachePlayVideoLists) {
        LogCat.e("video", "开始文件缓存........." + fileName);
        new CacheVideoListThread(cachePlayVideoLists, DataUtils.getCacheDirectory(), fileName).start();

    }

    /**
     * 缓存视频
     * @param cachePlayVideoLists
     */
    public static synchronized void cacheTDVideoList(Context context, ArrayList<AdDetailResponse> cachePlayVideoLists) {
        LogCat.e("video", "将今日列表存入数据库........." + cachePlayVideoLists.size());
        AdDao.getInstance(context).insertAll(true, cachePlayVideoLists);
        LogCat.e("video", "查询下插入后的个数： " + AdDao.getInstance(context).queryAll(true).size());
    }

    /**
     * 清空数据表的所有数据
     */
    public static synchronized void cleanSqlVideoList(Context context) {
        LogCat.e("video", "清空数据库........." );
        AdDao.getInstance(context).deleteAll(true);
        AdDao.getInstance(context).deleteAll(false);
        LogCat.e("video", "查询下插入后的个数： " + AdDao.getInstance(context).queryAll(true).size());
    }

    /**
     * 缓存视频
     * @param cachePlayVideoLists
     */
    public static synchronized void cacheTMVideoList(Context context, ArrayList<AdDetailResponse> cachePlayVideoLists) {
        LogCat.e("video", "将今日列表存入数据库........." + cachePlayVideoLists.size());
        AdDao.getInstance(context).insertAll(false, cachePlayVideoLists);
        LogCat.e("video", "查询下插入后的个数： " + AdDao.getInstance(context).queryAll(false).size());
    }


    /**
     * 得到本地的缓存视频的列表
     *
     * @return
     */
    public static ArrayList<AdDetailResponse> getLocalVideoList(Context context) {
        ArrayList<AdDetailResponse> localVideos = new ArrayList<>();
        File fileVideo = new File(DataUtils.getVideoDirectory());
        if (fileVideo.exists() && fileVideo.isDirectory()) {
            localVideos.addAll(getLocalList(context, fileVideo));
        }
        return localVideos;
    }

    /**
     * 返回当前的本地video视频列表信息集合
     *
     * @param videoFiles
     * @return
     */
    private static ArrayList<AdDetailResponse> getLocalList(Context context, File videoFiles) {
        ArrayList<AdDetailResponse> adDetailResponses = new ArrayList<>();
        File[] files = videoFiles.listFiles();
        for (File file : files) {
            if (file.isFile()) {
                String name = file.getName();
                // 正在下载的文件不能算到本地缓存列表中
                if (DLUtils.init(context).downloading(file.getAbsolutePath())) {
                    LogCat.e("video", "当前文件正在下载。。。。。");
                    continue;
                }
                // 文件下载失败
                final int HEADER_FILE_LENGTH = 1024 * 1024 * 10;
                if (file.length() < HEADER_FILE_LENGTH) {
                    DeleteFileUtils.getInstance().deleteFile(file.getAbsolutePath());
                    continue;
                }
                AdDetailResponse videoAdBean = new AdDetailResponse();
                int index = name.lastIndexOf(Constants.FILE_DOWNLOAD_EXTENSION);
                name = name.substring(0, index);
                videoAdBean.adVideoName = name;
                videoAdBean.videoPath = file.getAbsolutePath();
                videoAdBean.adVideoLength = file.length();
                adDetailResponses.add(videoAdBean);
                LogCat.e("video", "本地缓存视频：" + videoAdBean.adVideoName + "  " + videoAdBean.adVideoLength);
            } else {
                DeleteFileUtils.getInstance().deleteFile(file.getAbsolutePath());
            }
        }
        return adDetailResponses;
    }


    public static void updateVideoPath(boolean isToday, Context context, int vid, String path){
        AdDao.getInstance(context).update(isToday, vid, AdDao.videoPath, path);
        if( AdDao.getInstance(context).query(isToday, vid)){
            LogCat.e("video", "查询修改后的大小： " + AdDao.getInstance(context).queryDetail(isToday, vid).videoPath);
        }


    }


    /**
     * 得到明日的播放列表
     *
     * @return
     */
    public static synchronized ArrayList<AdDetailResponse> getCacheList(String fileName) {
        ArrayList<AdDetailResponse> cacheTomorrowList = new ArrayList<>();
        File cacheFile = new File(DataUtils.getCacheDirectory() + fileName);
        if (cacheFile.exists() && cacheFile.isFile()) {
            String json = DataUtils.readFileFromSdCard(cacheFile);
            if (!TextUtils.isEmpty(json)) {
                Gson gson = new Gson();
                LogCat.e("video", "缓存列表已经找到........");
                cacheTomorrowList = gson.fromJson(json, new TypeToken<ArrayList<AdDetailResponse>>() {
                }.getType());
                // TODO 以后删除
                LogCat.e("video", "缓存播放列表内容........");
                for (AdDetailResponse adDetailResponse : cacheTomorrowList) {
                    LogCat.e("video", "视频名称：" + adDetailResponse.adVideoName + ", 文件大小：" + adDetailResponse.adVideoLength);
                }

            }
        }
        return cacheTomorrowList;
    }

    /**
     * 得到今日或明日的播放列表
     *
     * @return
     */
    public static synchronized ArrayList<AdDetailResponse> getCacheList(Context context, boolean isToday) {
        ArrayList<AdDetailResponse> cacheTomorrowList = AdDao.getInstance(context).queryAll(isToday);
        return cacheTomorrowList;
    }



    /**
     * 根据本地缓存视频列表和明日播放列，得出当前的视频播放列表
     *
     * @param localVideoList
     * @param cacheTomorrowList
     * @return
     */
    public static ArrayList<AdDetailResponse> getPlayVideoList(ArrayList<AdDetailResponse> localVideoList, ArrayList<AdDetailResponse> cacheTomorrowList) {
        ArrayList<AdDetailResponse> cachePlayVideos = new ArrayList<>();
        int localLength = localVideoList.size();
        if (localLength < 2) {
            // 此时无需匹对列表，直接将预置片放入缓存播放列表
            LogCat.e("video", "如果本地缓存视频文件不足2个，直接播放预置片");
        } else {
            // 匹对列表
            LogCat.e("video", "开始根据明日表查找可以播放的视频");
            for (AdDetailResponse localVideo : localVideoList) {
                for (AdDetailResponse cacheVideo : cacheTomorrowList) {
                    if (!TextUtils.isEmpty(localVideo.adVideoName) && localVideo.adVideoName.equals(cacheVideo.adVideoName)) {
                        cacheVideo.videoPath = localVideo.videoPath;
                        LogCat.e("video", "缓存视频：" + cacheVideo.adVideoName);
                        cachePlayVideos.add(cacheVideo);
                        break;
                    }
                }
            }
            // 匹配后结果不满足2个，仍然要播放预告片
            if (cachePlayVideos.size() < 2) {
                cachePlayVideos.addAll(localVideoList);
            }
        }
        return cachePlayVideos;
    }



    /**
     * 提出重复下载的视频 最终获得明日下载列表
     */
    public static ArrayList<AdDetailResponse> reconnectedPrepare(ArrayList<AdDetailResponse> downloadLists, ArrayList<AdDetailResponse> prepareDownloadLists) {
        ArrayList<AdDetailResponse> tomorrowList = new ArrayList<>();
        int downloadsSize = downloadLists.size();
        for(int i = 0; i < prepareDownloadLists.size(); i++){
            AdDetailResponse prepareVideo = prepareDownloadLists.get(i);
            for(int j = 0; j < downloadsSize; j++){
                AdDetailResponse downloadVideo = downloadLists.get(j);
                if(!TextUtils.isEmpty(prepareVideo.adVideoName) && prepareVideo.adVideoName.equals(downloadVideo.adVideoName)){
                    prepareDownloadLists.remove(i);
                    LogCat.e("video", "剔除重复下载的视频：" + prepareVideo.adVideoName);
                    --i;
                    break;
                }
            }
        }
        LogCat.e("video", "------------------------------");
        LogCat.e("video", "最终的明日下载列表.......");
        for(AdDetailResponse adDetailResponse : prepareDownloadLists){
            LogCat.e("video", "明日下载视频：" + adDetailResponse.adVideoName);
        }
        LogCat.e("video", "------------------------------");
        tomorrowList.addAll(prepareDownloadLists);
        return tomorrowList;

    }

    /**
     * 删除旧文件目录
     */
    public static void deleteOldDir() {
        String oldPath = DataUtils.getSdCardOldFileDirectory();
        LogCat.e("video", "清空旧文件目录(gochinatv)....." + oldPath);
        DeleteFileUtils.getInstance().deleteDir(new File(DataUtils.getSdCardOldFileDirectory()));
    }


    /**
     * 根据本地缓存视频列表和今日播放列表，得到需要下载的视频列表
     *
     * @param localVideoList
     * @param todayVideoList
     * @return
     */
    public static ArrayList<AdDetailResponse> getDownloadList(ArrayList<AdDetailResponse> localVideoList, ArrayList<AdDetailResponse> todayVideoList) {
        ArrayList<AdDetailResponse> downloadList = new ArrayList<>();
        // 今天播放列表的视频不在本地缓存列表中，说明当前视频需要下载
        boolean isNeedDl;
        for (AdDetailResponse todayResponse : todayVideoList) {
            isNeedDl = true;
            for (AdDetailResponse localVideoResponse : localVideoList) {
                // 如果当前视频在本地缓存中，改视频不需要下载，反之下载
                if (!TextUtils.isEmpty(todayResponse.adVideoName) && todayResponse.adVideoName.equals(localVideoResponse.adVideoName)) {
                    isNeedDl = false;
                    break;
                }
            }
            if (isNeedDl) {
                downloadList.add(todayResponse);
                LogCat.e("video", "需下载的视频：" + todayResponse.adVideoName + ", " + todayResponse.adVideoId);
            }
        }
        return downloadList;
    }

    /**
     * 根据本地缓存视频列表和今日播放列表，得到需要下载的删除文件列表
     *
     * @param localVideoList
     * @param todayVideoList
     * @return
     */
    public static ArrayList<AdDetailResponse> getTodayPlayVideoList(ArrayList<AdDetailResponse> localVideoList, ArrayList<AdDetailResponse> todayVideoList) {
        // 播放列表的内容为本地缓存视频在今日播放列表中
        ArrayList<AdDetailResponse> playList = new ArrayList<>();
        for (AdDetailResponse localVideoResponse : localVideoList) {
            for (AdDetailResponse todayResponse : todayVideoList) {
                // 本地缓存视频在今日播放列表中，说明当前视频可以直接播放
                if (!TextUtils.isEmpty(todayResponse.adVideoName) && todayResponse.adVideoName.equals(localVideoResponse.adVideoName)) {
                    todayResponse.videoPath = localVideoResponse.videoPath;
                    LogCat.e("video", "播放列表：" + todayResponse.adVideoName);
                    playList.add(todayResponse);
                    break;
                }
            }
        }
        return playList;
    }

    /**
     * 根据本地缓存视频列表和今日播放列表，得到需要下载的删除文件列表
     *
     * @param localVideoList
     * @param todayVideoList
     * @return
     */
    public static ArrayList<AdDetailResponse> getDeleteList(ArrayList<AdDetailResponse> localVideoList, ArrayList<AdDetailResponse> todayVideoList) {
        // 删除文件为在本地缓存视频列表，而不在今日播放列表的内容
        ArrayList<AdDetailResponse> deleteList = new ArrayList<>();
        boolean isNeedDel;
        for (AdDetailResponse localVideoResponse : localVideoList) {
            isNeedDel = true;
            for (AdDetailResponse todayResponse : todayVideoList) {
                // 今天播放列表的视频不在本地缓存列表中，说明当前视频需要下载
                if (!TextUtils.isEmpty(todayResponse.adVideoName) && todayResponse.adVideoName.equals(localVideoResponse.adVideoName)) {
                    isNeedDel = false;
                    break;
                }
            }
            if (isNeedDel) {
                LogCat.e("video", "需要删除的视频：" + localVideoResponse.adVideoName);
                deleteList.add(localVideoResponse);
            }
        }
        return deleteList;
    }

    /**
     * 检测localVideoList列表文件完整性
     * 根据今明两日的文件列表检查localVideoList列表文件的完整性
     * @param cacheTodayList
     */
    public static void checkFileLength(ArrayList<AdDetailResponse> localVideoList, ArrayList<AdDetailResponse> cacheTodayList) {
        if (cacheTodayList != null && cacheTodayList.size() != 0) {
            LogCat.e("video", "检测到播放列表, 开始检测文件完整性......");
            for (int i = 0; i < localVideoList.size(); i++) {
                AdDetailResponse localVideo = localVideoList.get(i);
                for (AdDetailResponse cacheVideo : cacheTodayList) {
                    if (!TextUtils.isEmpty(localVideo.adVideoName) && localVideo.adVideoName.equals(cacheVideo.adVideoName)) {
                        if (cacheVideo.adVideoLength != 0 && cacheVideo.adVideoLength != localVideo.adVideoLength) {
                            --i;
                            localVideoList.remove(localVideo);
                            DeleteFileUtils.getInstance().deleteFile(localVideo.videoPath);
                            LogCat.e("video", "由于文件不完整，需要删除的文件是......." + localVideo.adVideoName);
                        }
                    }
                }
            }
        } else {
            for (int i = 0; i < localVideoList.size(); i++) {
                AdDetailResponse localVideo = localVideoList.get(i);
                if (localVideo.adVideoLength == 0) {
                    --i;
                    localVideoList.remove(localVideo);
                    DeleteFileUtils.getInstance().deleteFile(localVideo.videoPath);
                    LogCat.e("video", "由于文件大小==0，需要删除的文件是......." + localVideo.adVideoName);
                }
            }
        }
    }

    public static void recordStartTime(Context context) {
        SharedPreference sharedPreference = SharedPreference.getSharedPreferenceUtils(context);
        sharedPreference.saveDate(Constants.SHARE_KEY_DURATION, System.currentTimeMillis());
    }

    public static void computeTime(Context context) {
        SharedPreference sharedPreference = SharedPreference.getSharedPreferenceUtils(context);
        // 计算离开的时候总时长
        try {
            long startLong = sharedPreference.getDate(Constants.SHARE_KEY_DURATION, 0L);
            if (startLong != 0) {
                long duration = System.currentTimeMillis() - startLong;
                if (duration > 0) {
                    long day = duration / (24 * 60 * 60 * 1000);
                    long hour = (duration / (60 * 60 * 1000) - day * 24);
                    long min = ((duration / (60 * 1000)) - day * 24 * 60 - hour * 60);
                    long s = (duration / 1000 - day * 24 * 60 * 60 - hour * 60 * 60 - min * 60);
                    String str = day + "天  " + hour + "时" + min + "分" + s + "秒";

                    LogCat.e("video", str);

                    LogCat.e("video", "上报开机时长。。。。。。。。");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            sharedPreference.saveDate(Constants.SHARE_KEY_DURATION, 0);
        }
    }

}
