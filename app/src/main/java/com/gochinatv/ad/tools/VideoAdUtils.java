package com.gochinatv.ad.tools;

import android.content.Context;
import android.text.TextUtils;

import com.download.db.DLDao;
import com.gochinatv.ad.thread.DeleteFileUtils;
import com.gochinatv.db.AdDao;
import com.okhtttp.response.AdDetailResponse;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by fq_mbp on 16/4/15.
 */
public class VideoAdUtils {


    /**
     * 更新数据表的所有数据
     */
    public static synchronized void updateSqlVideoList(Context context, boolean isToday, ArrayList<AdDetailResponse> downloadVideos, ArrayList<AdDetailResponse> deleteVideos) {
        LogCat.e("video", "更新数据库.........");
        // 删除所有删除列表的video
        String tableName = getTableName(isToday);
        AdDao.deleteAll(context, tableName, deleteVideos);
        // 对于已经存在的数据，不做修改
        // 对于要下载的数据，加入数据表
        AdDao.insertAll(context, tableName, downloadVideos);

        ArrayList<AdDetailResponse> sqlList = AdDao.queryAll(context, tableName);
        if (sqlList != null) {
            LogCat.e("video", "查询下插入后的个数： " + sqlList.size());
            for (AdDetailResponse adDetailResponse : sqlList) {
                LogCat.e("video", "数据表video： " + adDetailResponse.adVideoName + ", length: " + adDetailResponse.adVideoLength + ", " + adDetailResponse.videoPath);
            }
        }

    }


    public static String getTableName(boolean isToday) {
        String table = null;
        if (isToday)
            table = AdDao.DBBASE_TD_VIDEOS_TABLE_NAME;
        else
            table = AdDao.DBBASE_TM_VIDEOS_TABLE_NAME;
        return table;
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
                int index = name.lastIndexOf(Constants.FILE_DOWNLOAD_EXTENSION);
                name = name.substring(0, index);
                boolean isDownload = DLDao.queryByName(context, name);
                // 线程数变了
                if (isDownload) {
                    LogCat.e("video", "当前文件正在下载, 不算在本地缓存文件内容中。。。。。" + name);
                    continue;
                }

                // 文件下载失败
                final int HEADER_FILE_LENGTH = 1024 * 1024 * 1;
                if (file.length() < HEADER_FILE_LENGTH) {
                    DeleteFileUtils.getInstance().deleteFile(file.getAbsolutePath());
                    continue;
                }
                AdDetailResponse videoAdBean = new AdDetailResponse();

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


    public static void updateVideoPath(boolean isToday, Context context, String fileName, String path) {
        String tableName = getTableName(isToday);


        AdDao.update(context, tableName, fileName, AdDao.videoPath, path);
        AdDetailResponse adDetailResponse = AdDao.queryDetail(context, tableName, AdDao.adVideoName, fileName);
        if (adDetailResponse != null) {
            LogCat.e("video", "查询修改后的大小： " + adDetailResponse.videoPath);
        }
    }


    /**
     * 得到今日或明日的播放列表
     *
     * @return
     */
    public static synchronized ArrayList<AdDetailResponse> getCacheList(Context context, boolean isToday) {
        ArrayList<AdDetailResponse> cacheTomorrowList = AdDao.queryAll(context, getTableName(isToday));
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
        for (int i = 0; i < prepareDownloadLists.size(); i++) {
            AdDetailResponse prepareVideo = prepareDownloadLists.get(i);
            for (int j = 0; j < downloadsSize; j++) {
                AdDetailResponse downloadVideo = downloadLists.get(j);
                if (!TextUtils.isEmpty(prepareVideo.adVideoName) && prepareVideo.adVideoName.equals(downloadVideo.adVideoName)) {
                    prepareDownloadLists.remove(i);
                    LogCat.e("video", "剔除重复下载的视频：" + prepareVideo.adVideoName);
                    --i;
                    break;
                }
            }
        }
        LogCat.e("video", "------------------------------");
        for (AdDetailResponse adDetailResponse : prepareDownloadLists) {
            LogCat.e("video", "视频：" + adDetailResponse.adVideoName);
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
        DeleteFileUtils.getInstance().deleteDir(new File(oldPath));
    }

    /**
     * 删除旧文件目录
     */
    public static void deleteScreenShotDir() {
        String oldPath = DataUtils.getScreenShotDirectory();
        LogCat.e("video", "清空旧文件目录(gochinatv)....." + oldPath);
        DeleteFileUtils.getInstance().deleteDir(new File(oldPath));
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
     *
     * @param cacheTodayList
     */
    public static ArrayList<AdDetailResponse> checkFileLength(Context context, ArrayList<AdDetailResponse> localVideoList, ArrayList<AdDetailResponse> cacheTodayList) {
        ArrayList<AdDetailResponse> deleteVideos = new ArrayList<>();
        if (cacheTodayList != null && cacheTodayList.size() != 0) {
            LogCat.e("video", "检测到播放列表, 开始检测文件完整性......");
            for (int i = 0; i < localVideoList.size(); i++) {
                AdDetailResponse localVideo = localVideoList.get(i);
                boolean isHasCache = false;
                for (AdDetailResponse cacheVideo : cacheTodayList) {
                    if (!TextUtils.isEmpty(localVideo.adVideoName) && localVideo.adVideoName.equals(cacheVideo.adVideoName)) {
                        isHasCache = true;
                        if (cacheVideo.adVideoLength != 0 && cacheVideo.adVideoLength != localVideo.adVideoLength) {
                            // 如果文件正在下载，则忽略
                            boolean isDownloading = DLDao.queryByName(context, localVideo.adVideoName);
                            if (isDownloading) {
                                LogCat.e("video", "当前文件正在下载中，不做额外处理.......");
                            } else {
                                deleteVideos.add(localVideo);
//                                localVideoList.remove(localVideo);
//                                DeleteFileUtils.getInstance().deleteFile(localVideo.videoPath);
                                LogCat.e("video", "由于文件不完整，需要删除的文件是......." + localVideo.adVideoName);
                            }
                            break;
                        }
                    }
                }

                if(!isHasCache){
                    deleteVideos.add(localVideo);
//                    localVideoList.remove(localVideo);
//                    DeleteFileUtils.getInstance().deleteFile(localVideo.videoPath);
                    LogCat.e("video", "由于当前文件不在cache表中，无法正确验证完整性，所以删除文件.........." + localVideo.adVideoName);
                }


            }
        } else {

            for (int i = 0; i < localVideoList.size(); i++) {
                AdDetailResponse localVideo = localVideoList.get(i);
                deleteVideos.add(localVideo);
                LogCat.e("video", "缓存表为空，清空所有缓存视频.........." + localVideo.adVideoName);
//                if (localVideo.adVideoLength == 0) {
//                    --i;
//                    localVideoList.remove(localVideo);
//                    DeleteFileUtils.getInstance().deleteFile(localVideo.videoPath);

//                }
//                --i;
//                localVideoList.remove(localVideo);
//                DeleteFileUtils.getInstance().deleteFile(localVideo.videoPath);
            }
        }
        return deleteVideos;
    }

    public static void recordStartTime(Context context) {
        SharedPreference sharedPreference = SharedPreference.getSharedPreferenceUtils(context);
        sharedPreference.saveDate(Constants.SHARE_KEY_DURATION, System.currentTimeMillis());
    }

    public static String computeTime(long time) {
        String str = null;
        // 计算离开的时候总时长
        try {
            long hour = time / (60 * 60 * 1000);
            String hourStr = String.valueOf(hour);
            if (hour < 10) {
                hourStr = "0" + hourStr;
            }
            long min = ((time / (60 * 1000)) - hour * 60);
            String minStr = String.valueOf(min);
            if (hour < 10) {
                minStr = "0" + minStr;
            }
            long second = (time / 1000 - hour * 60 * 60 - min * 60);
            String secondStr = String.valueOf(second);
            if (hour < 10) {
                secondStr = "0" + secondStr;
            }
            str = hour + ":" + min + ":" + secondStr;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return str;
    }

}
