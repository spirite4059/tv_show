package com.gochinatv.ad.tools;

import android.content.Context;
import android.text.TextUtils;

import com.download.db.DLDao;
import com.download.db.DownloadInfo;
import com.gochinatv.ad.thread.DeleteFileUtils;
import com.gochinatv.db.AdDao;
import com.gochinatv.statistics.request.DeleteVideoRequest;
import com.gochinatv.statistics.request.VideoDownloadInfoRequest;
import com.gochinatv.statistics.request.VideoSendRequest;
import com.gochinatv.statistics.server.ErrorHttpServer;
import com.gochinatv.statistics.tools.Constant;
import com.gochinatv.statistics.tools.MacUtils;
import com.okhtttp.OkHttpCallBack;
import com.okhtttp.response.AdDetailResponse;
import com.okhtttp.response.ErrorResponse;

import java.io.File;
import java.math.BigDecimal;
import java.util.ArrayList;

/**
 * Created by fq_mbp on 16/4/15.
 */
public class VideoAdUtils {


    /**
     * 更新数据表的所有数据
     */
    public static synchronized void updateSqlVideoList(Context context, boolean isToday, ArrayList<AdDetailResponse> downloadVideos) {
        LogCat.e("video", "更新数据库.........");

        // 删除所有删除列表的video
        String tableName = getTableName(isToday);

        AdDao.deleteAll(context, tableName);
//        AdDao.deleteAll(context, tableName, deleteVideos);
        // 对于已经存在的数据，不做修改
        // 对于要下载的数据，加入数据表
        AdDao.insertAll(context, tableName, downloadVideos);

//        ArrayList<AdDetailResponse> sqlList = AdDao.queryAll(context, tableName);
//        if (sqlList != null) {
//            LogCat.e("video", "查询下插入后的个数： " + sqlList.size());
//            for (AdDetailResponse adDetailResponse : sqlList) {
//                LogCat.e("video", "数据表video： " + adDetailResponse.adVideoName + ", tlength: " + adDetailResponse.adVideoLength + ", " + adDetailResponse.videoPath);
//            }
//        }

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

        // 验证文件完整性
        // 从今明两天缓存列表中获取不存在的视频,将其删除
        // 获取今天缓存列表
        ArrayList<AdDetailResponse> cacheTodayList = VideoAdUtils.getCacheList(context, true);
        // 获取明天缓存列表
        ArrayList<AdDetailResponse> cacheTomorrowList = VideoAdUtils.getCacheList(context, false);

        return getLocalVideoList(context, cacheTodayList, cacheTomorrowList);
    }


    public static ArrayList<AdDetailResponse> getLocalVideoList(Context context, ArrayList<AdDetailResponse> todayList, ArrayList<AdDetailResponse> tomorrowList) {
        ArrayList<AdDetailResponse> localVideos = new ArrayList<>();
        File fileVideo = new File(DataUtils.getVideoDirectory());
        if (fileVideo.exists() && fileVideo.isDirectory()) {
            localVideos.addAll(getLocalList(context, fileVideo));
        }
        // 删除不再需要的视频
        deleteOutDownloadVideo(context, todayList, tomorrowList);

        LogCat.e("video", "验证文件完整性前,视频的个数......." + localVideos.size());
        // 验证文件完整性
        // 从今明两天缓存列表中获取不存在的视频,将其删除
        // 根据今日列表检查缓存文件完整性,并得到要删除的文件
        ArrayList<AdDetailResponse> deleteVideo = checkFileLength(context, todayList, localVideos);
        // 获取明天缓存列表
        // 从删除列表中提出明天要用到的视频
        removeItemFromList(deleteVideo, tomorrowList);
        // 根据明日列表来剔除今日要用到的视频
        ArrayList<AdDetailResponse> deleteVideo1 = checkFileLength(context, tomorrowList, localVideos);
        // 从删除列表中提出明天要用到的视频
        removeItemFromList(deleteVideo1, todayList);
        // 去除重复
        ArrayList<AdDetailResponse> finalVideos = dealWithDeleteVideos(deleteVideo, deleteVideo1);

        // 删除文件
        if(finalVideos != null && finalVideos.size() > 0){
            for(AdDetailResponse adDetailResponse : finalVideos){
                DeleteFileUtils.getInstance().deleteFile(adDetailResponse.videoPath);
                LogCat.e("video", "当前视频在今明两天都不用,立即删除" + adDetailResponse.adVideoName);
            }
        }
        // 从本地视频表中删除需要删除的视频
        removeItemFromList(localVideos, finalVideos);
        LogCat.e("video", "验证文件完整性后,视频的个数......." + localVideos.size());
        return localVideos;
    }

    private static void deleteOutDownloadVideo(Context context, ArrayList<AdDetailResponse> todayList, ArrayList<AdDetailResponse> tomorrowList) {
        ArrayList<DownloadInfo> downloadInfos = DLDao.queryAll(context);
        if (downloadInfos != null && downloadInfos.size() != 0) {
            String downloadName = downloadInfos.get(0).tname;
            // 正在做下载
            if(Constants.FILE_APK_NAME_SIMPAL.equals(downloadName)){
                return;
            }
            // 遍历新的视频列表,如果不在需要下载了,就删除该文件
            boolean isToday = getIntersectionResult(downloadName, todayList);
            if(!isToday){
                boolean isTomorrow = getIntersectionResult(downloadName, tomorrowList);
                if(!isTomorrow){
                    // 说明不在使用该视,需要删除
                    DLDao.delete(context);
                    // 删除文件
                    DeleteFileUtils.getInstance().deleteFile(DataUtils.getVideoDirectory() + downloadName + Constants.FILE_DOWNLOAD_EXTENSION);
                }
            }

        }
    }

    /**
     * 检测localVideoList列表文件完整性
     * 根据今明两日的文件列表检查localVideoList列表文件的完整性
     */
    public static ArrayList<AdDetailResponse> checkFileLength(Context context, ArrayList<AdDetailResponse> cacheVideoList, ArrayList<AdDetailResponse> localVideoList) {
        ArrayList<AdDetailResponse> deleteVideos = new ArrayList<>();
        if (cacheVideoList != null && cacheVideoList.size() != 0) {
//            LogCat.e("video", "检测到播放列表, 开始检测文件完整性......");
            for (int i = 0; i < localVideoList.size(); i++) {
                AdDetailResponse localVideo = localVideoList.get(i);
                boolean isHasCache = false;
                for (AdDetailResponse cacheVideo : cacheVideoList) {
                    if (!TextUtils.isEmpty(localVideo.adVideoName) && localVideo.adVideoName.equals(cacheVideo.adVideoName)) {
                        isHasCache = true;
                        if (cacheVideo.adVideoLength != 0 && cacheVideo.adVideoLength != localVideo.adVideoLength) {
                            // 如果文件正在下载，则忽略
                            boolean isDownloading = DLDao.queryByName(context, localVideo.adVideoName);
                            if (isDownloading) {
                                LogCat.e("video", "当前文件正在下载中，不做额外处理.......");
                            } else {
                                deleteVideos.add(cacheVideo);
                                LogCat.e("video", "由于文件不完整，需要删除的文件是......." + localVideo.adVideoName);
                            }
                            break;
                        }
                    }
                }
                if (!isHasCache) {
                    deleteVideos.add(localVideo);
//                    LogCat.e("video", "由于当前文件不在cache表中，无法正确验证完整性，所以将.........." + localVideo.adVideoName + " 文件加入删除列表");
                }
            }
        } else {
            for (int i = 0; i < localVideoList.size(); i++) {
                AdDetailResponse localVideo = localVideoList.get(i);
                deleteVideos.add(localVideo);
            }
        }
        return deleteVideos;
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
        final int HEADER_FILE_LENGTH = 1024 * 1024;
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
//        AdDetailResponse adDetailResponse = AdDao.queryDetail(context, tableName, AdDao.adVideoName, fileName);
//        if (adDetailResponse != null) {
//            LogCat.e("video", "查询修改后的大小： " + adDetailResponse.videoPath);
//        }
    }


    /**
     * 得到今日或明日的播放列表
     *
     * @return
     */
    public static synchronized ArrayList<AdDetailResponse> getCacheList(Context context, boolean isToday) {
        return AdDao.queryAll(context, getTableName(isToday));
    }


    /**
     * 根据本地缓存视频列表和明日播放列，得出当前的视频播放列表
     *
     * @param localVideoList
     * @return
     */
    public static ArrayList<AdDetailResponse> getPlayVideoList(Context context, ArrayList<AdDetailResponse> localVideoList) {
        ArrayList<AdDetailResponse> cachePlayVideos = new ArrayList<>();
        int localLength = localVideoList.size();
        if (localLength < 2) {
            // 此时无需匹对列表，直接将预置片放入缓存播放列表
            LogCat.e("video", "如果本地缓存视频文件不足2个，直接播放预置片");
        } else {
            // 匹对列表
            LogCat.e("video", "开始根据明日表查找可以播放的视频");
            cachePlayVideos.addAll(getIntersectionList(localVideoList, VideoAdUtils.getCacheList(context, false)));

            // 匹配后结果不满足2个，继续检查昨天的播放列表
            if (cachePlayVideos.size() < 2) {
                // 检测前天的视频是否可以播放
                cachePlayVideos.addAll(getIntersectionList(localVideoList, VideoAdUtils.getCacheList(context, true)));
            }
            // 如果还是少于2个,就将缓存列表键入到播放列表中
            if (cachePlayVideos.size() < 2) {
                cachePlayVideos.addAll(localVideoList);
            }
        }

        return cachePlayVideos;
    }


    /**
     * 提出重复下载的视频 最终获得明日下载列表
     */
    public static void reconnectedPrepare(ArrayList<AdDetailResponse> downloadLists, ArrayList<AdDetailResponse> prepareDownloadLists) {
//        ArrayList<AdDetailResponse> tomorrowList = new ArrayList<>();
//        int downloadsSize = downloadLists.size();

        removeItemFromList(prepareDownloadLists, downloadLists);


//        for (int i = 0; i < prepareDownloadLists.size(); i++) {
//            AdDetailResponse prepareVideo = prepareDownloadLists.get(i);
//            for (int j = 0; j < downloadsSize; j++) {
//                AdDetailResponse downloadVideo = downloadLists.get(j);
//                if (!TextUtils.isEmpty(prepareVideo.adVideoName) && prepareVideo.adVideoName.equals(downloadVideo.adVideoName)) {
//                    prepareDownloadLists.remove(i);
//                    LogCat.e("video", "剔除重复下载的视频：" + prepareVideo.adVideoName);
//                    --i;
//                    break;
//                }
//            }
//        }
        LogCat.e("video", "------------------------------");
        for (AdDetailResponse adDetailResponse : prepareDownloadLists) {
            LogCat.e("video", "视频：" + adDetailResponse.adVideoName);
        }
        LogCat.e("video", "------------------------------");
//        tomorrowList.addAll(prepareDownloadLists);
//        return tomorrowList;
        return;
    }

    public static ArrayList<AdDetailResponse> dealWithDeleteVideos(ArrayList<AdDetailResponse> todayVideos, ArrayList<AdDetailResponse> tomorrowVideos) {
        ArrayList<AdDetailResponse> deleteVideos = new ArrayList<>();
        deleteVideos.addAll(todayVideos);

        deleteVideos.addAll(getDifferenceSetList(deleteVideos, tomorrowVideos));
//        int tmSize = tomorrowVideos.size();
//        int deleteSize = todayVideos.size();
//        for (int i = 0; i < tmSize; i++) {
//            AdDetailResponse tomorrowVideo = tomorrowVideos.get(i);
//            boolean isContains = false;
//            for (int j = 0; j < deleteSize; j++) {
//                AdDetailResponse todayVideo = todayVideos.get(j);
//                if (!TextUtils.isEmpty(tomorrowVideo.adVideoName) && tomorrowVideo.adVideoName.equals(todayVideo.adVideoName)) {
//                    isContains = true;
//                    break;
//                }
//            }
//            if(!isContains){
//                deleteVideos.add(tomorrowVideo);
//            }
//        }

        return deleteVideos;

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
        downloadList.addAll(getDifferenceSetList(todayVideoList, localVideoList));
        // 今天播放列表的视频不在本地缓存列表中，说明当前视频需要下载

//        boolean isNeedDl;
//        for (AdDetailResponse todayResponse : todayVideoList) {
//            isNeedDl = true;
//            for (AdDetailResponse localVideoResponse : localVideoList) {
//                // 如果当前视频在本地缓存中，改视频不需要下载，反之下载
//                if (!TextUtils.isEmpty(todayResponse.adVideoName) && todayResponse.adVideoName.equals(localVideoResponse.adVideoName)) {
//                    isNeedDl = false;
//                    break;
//                }
//            }
//            if (isNeedDl) {
//                downloadList.add(todayResponse);
//                LogCat.e("video", "需下载的视频：" + todayResponse.adVideoName + ", " + todayResponse.adVideoId);
//            }
//        }
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
        deleteList.addAll(getDifferenceSetList(localVideoList, todayVideoList));
//        boolean isNeedDel;
//        for (AdDetailResponse localVideoResponse : localVideoList) {
//            isNeedDel = true;
//            for (AdDetailResponse todayResponse : todayVideoList) {
//                // 今天播放列表的视频不在本地缓存列表中，说明当前视频需要下载
//                if (!TextUtils.isEmpty(todayResponse.adVideoName) && todayResponse.adVideoName.equals(localVideoResponse.adVideoName)) {
//                    isNeedDel = false;
//                    break;
//                }
//            }
//            if (isNeedDel) {
//                LogCat.e("video", "需要删除的视频：" + localVideoResponse.adVideoName);
//                deleteList.add(localVideoResponse);
//            }
//        }
        return deleteList;
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
            long min = ((time / (60 * 1000)) - hour * 60);
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





    public static ArrayList<AdDetailResponse> getDistinctList(ArrayList<AdDetailResponse> responses) {
        ArrayList<AdDetailResponse> videoList;
        if (responses != null) {
            try {
                videoList = getDistinctVideoList(responses);
            } catch (Exception e) {
                e.printStackTrace();
                videoList = new ArrayList<>();
            }
        } else {
            videoList = new ArrayList<>();
        }
        return videoList;
    }


    private static ArrayList<AdDetailResponse> getDistinctVideoList(ArrayList<AdDetailResponse> videos) {
        ArrayList<AdDetailResponse> videoList = new ArrayList<>();
        for (AdDetailResponse adDetailResponse : videos) {
            int length = videoList.size();
            if (length == 0) {
                videoList.add(adDetailResponse);
            } else {
                boolean isHasVideo = false;
                for (int i = 0; i < length; i++) {
                    AdDetailResponse currentVideo = videoList.get(i);
                    if (currentVideo != null && !TextUtils.isEmpty(currentVideo.adVideoName) && currentVideo.adVideoName.equals(adDetailResponse.adVideoName)) {
                        isHasVideo = true;
                        break;
                    }
                }
                if (!isHasVideo) {
                    videoList.add(adDetailResponse);
                }
            }
        }
        return videoList;
    }

    public static String logProgress(long progress, long fileLength) {
        if (fileLength == 0) {
            return "";
        }
        double size = (int) (progress / 1024);
        String sizeStr;
        int s = (int) (progress * 100 / fileLength);
        if (size > 1000) {
            size = (progress / 1024) / 1024f;
            BigDecimal b = new BigDecimal(size);
            double f1 = b.setScale(1, BigDecimal.ROUND_HALF_UP).doubleValue();
            sizeStr = String.valueOf(f1 + "MB，  ");
        } else {
            sizeStr = String.valueOf((int) size + "KB，  ");
        }
        return (sizeStr + s + "%");
    }


    // 去除交集
    public static void removeItemFromList(ArrayList<AdDetailResponse> sourceList, ArrayList<AdDetailResponse> otherList) {
        for (AdDetailResponse adDetailResponse : otherList) {
            for (int i = sourceList.size() - 1; i >= 0; i--) {
                AdDetailResponse deleteVideo = sourceList.get(i);
                if (deleteVideo != null && !TextUtils.isEmpty(deleteVideo.adVideoName) && deleteVideo.adVideoName.equals(adDetailResponse.adVideoName)) {
                    // 当前视频需要使用，从删除列表删除
                    sourceList.remove(i);
                }
            }
        }
    }

    // 求交集
    public static ArrayList<AdDetailResponse> getIntersectionList(ArrayList<AdDetailResponse> sourceList, ArrayList<AdDetailResponse> otherList) {
        ArrayList<AdDetailResponse> targetList = new ArrayList<>();
        for (AdDetailResponse localVideo : sourceList) {
            for (AdDetailResponse cacheVideo : otherList) {
                if (!TextUtils.isEmpty(localVideo.adVideoName) && localVideo.adVideoName.equals(cacheVideo.adVideoName)) {
                    cacheVideo.videoPath = localVideo.videoPath;
                    LogCat.e("video", "缓存视频：" + cacheVideo.adVideoName);
                    targetList.add(cacheVideo);
                    break;
                }
            }
        }
        return targetList;
    }

    public static boolean getIntersectionResult(String target, ArrayList<AdDetailResponse> otherList) {
        if (TextUtils.isEmpty(target)){
            return  false;
        }
        boolean isContains = false;
        for (AdDetailResponse localVideo : otherList) {
            if (target.equals(localVideo.adVideoName)) {
                isContains = true;
                break;
            }
        }
        return isContains;
    }

    // 求差集
    public static ArrayList<AdDetailResponse> getDifferenceSetList(ArrayList<AdDetailResponse> sourceList, ArrayList<AdDetailResponse> otherList) {
        ArrayList<AdDetailResponse> targetList = new ArrayList<>();
        for (AdDetailResponse tomorrowVideo : sourceList) {
            boolean isContains = false;
            for (AdDetailResponse todayVideo : otherList) {
                if (!TextUtils.isEmpty(tomorrowVideo.adVideoName) && tomorrowVideo.adVideoName.equals(todayVideo.adVideoName)) {
                    isContains = true;
                    break;
                }
            }
            if (!isContains) {
                targetList.add(tomorrowVideo);
            }
        }
        return targetList;
    }



    /**
     * 上报文件下载时长
     */
    public static void sendVideoDownloadTime(Context context, int id, String name, long time) {
        if (!TextUtils.isEmpty(DataUtils.getMacAddress(context)) && DataUtils.isNetworkConnected(context)) {

            VideoDownloadInfoRequest infoRequest = new VideoDownloadInfoRequest();
            infoRequest.videoId = id;
            infoRequest.videoName = name;
            infoRequest.downloadTime = time;
            String json = MacUtils.getJsonStringByEntity(infoRequest);
            ErrorHttpServer.doStatisticsHttp(context, Constant.VIDEO_DOWNLOAD_TIME, json, new OkHttpCallBack<ErrorResponse>() {
                @Override
                public void onSuccess(String url, ErrorResponse response) {
                    LogCat.e("MainActivity", "上传视频下载时长成功");
                }

                @Override
                public void onError(String url, String errorMsg) {
                    LogCat.e("MainActivity", "上传视频下载时长失败");
                }
            });
        }
    }


    /**
     * 上报播放次数
     */
    public static void sendVideoPlayTimes(Context context, int id, String name) {
        if (!TextUtils.isEmpty(DataUtils.getMacAddress(context)) && DataUtils.isNetworkConnected(context)) {

            VideoSendRequest infoRequest = new VideoSendRequest();
            infoRequest.videoId = id;
            infoRequest.videoName = name;
            String json = MacUtils.getJsonStringByEntity(infoRequest);
            ErrorHttpServer.doStatisticsHttp(context, Constant.VIDEO_PLAY_TIMES, json, new OkHttpCallBack<ErrorResponse>() {
                @Override
                public void onSuccess(String url, ErrorResponse response) {
                    LogCat.e("MainActivity", "上传视频播放次数成功");
                }

                @Override
                public void onError(String url, String errorMsg) {
                    LogCat.e("MainActivity", "上传视频播放次数失败");
                }
            });
        }
    }


    /**
     * 上报删除视频
     */
    public static void sendDeleteVideo(Context context, ArrayList<AdDetailResponse> deleteLists) {
        if (!TextUtils.isEmpty(DataUtils.getMacAddress(context)) && DataUtils.isNetworkConnected(context)) {
            if (deleteLists == null || deleteLists.size() == 0) {
                return;
            }
            DeleteVideoRequest deleteVideoRequest = new DeleteVideoRequest();
            ArrayList<VideoSendRequest> deleteList = new ArrayList<>();
            for (AdDetailResponse delete : deleteLists) {
                VideoSendRequest infoRequest = new VideoSendRequest();
                infoRequest.videoId = delete.adVideoId;
                infoRequest.videoName = delete.adVideoName;
                deleteList.add(infoRequest);
                LogCat.e("MainActivity", "需要删除的视频：" + delete.adVideoName);
            }
            deleteVideoRequest.deleteData = deleteList;
            String json = MacUtils.getJsonStringByEntity(deleteVideoRequest);
            ErrorHttpServer.doStatisticsHttp(context, Constant.VIDEO_DELETE, json, new OkHttpCallBack<ErrorResponse>() {
                @Override
                public void onSuccess(String url, ErrorResponse response) {
                    LogCat.e("MainActivity", "上传删除视频成功");
                }

                @Override
                public void onError(String url, String errorMsg) {
                    LogCat.e("MainActivity", "上传删除视频失败");
                }
            });
        }
    }


}
