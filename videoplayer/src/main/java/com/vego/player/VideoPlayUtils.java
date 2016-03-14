//package com.vego.player;
//
//import android.content.Context;
//import android.net.Uri;
//import android.os.Bundle;
//import android.os.Message;
//import android.text.TextUtils;
//import android.util.SparseArray;
//
//import com.httputils.http.OnRequestListener;
//import com.httputils.http.response.CdnPathResponse;
//import com.httputils.http.response.PlayInfoResponse;
//import com.httputils.http.response.VideoDetailResponse;
//import com.httputils.http.response.YoutubeUrlDataResponse;
//import com.httputils.http.service.CDNHttpService;
//import com.httputils.http.service.YoutubeHttpService;
//
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.Map;
//
///**
// * Created by fq_mbp on 15/9/8.
// */
//public class VideoPlayUtils {
//    /**
//     * 播放需要的参数：
//     *      1.来源
//     *      2.来源对应的id
//     */
//
//    /**
//     * youtube播放控件
//     */
//    private YouTubeUtility youTubeUtility;
//    /**
//     * Dailymotion播放控件
//     */
//    private DailymotionUtils dailymotionUtils;
//
//    /**
//     * 0:解析失败；
//     */
//    private final int ERROR_TAG_FAIL_ANALYTICAL = 0;
//    /**
//     * 2：网络
//     */
//    private final int ERROR_TAG_FAIL_NET = 2;
//
//    /**
//     * 3：其他原因
//     */
//    private final int ERROR_TAG_FAIL_OTHER = 3;
//
//
//    /**
//     * 是否通过服务器去获取youtube的Url
//     */
//    private boolean isUseServerParse;
//
//    private String serverErrorMsg;
//
//    private Context context;
//
//    /**
//     * 是否使用客户端解析地址
//     */
//    private boolean isUseClientParse;
//
//    /**
//     * 是否使用dm解析地址
//     */
//    private boolean isUseDMParse;
//
//    /**
//     * 客户端解析youtube失败的状态
//     */
//    private String clientParseErrorStatus;
//
//    /**
//     * 客户端解析youtube 的原因
//     */
//    private String clientParseErrorReason;
//
//    private VideoDetailResponse videoDetailResponse;
//
//    private String dmParseErrorReason;
//    // 0 :youtube；1：dm；：2 cdn
//    private int videoType = 0;//视频源类型
//
//    // 为了取消获取视频的网络请求，不浪费多余的线程资源，设置成单利
//    private static VideoPlayUtils instance;
//
//    private VideoPlayUtils() {
//
//    }
//
//
//    public int getVideoType(){
//        return videoType;
//    }
//
//
//
//
//
//    public static synchronized VideoPlayUtils getInstance(Context context) {
//        if (instance == null) {
//            instance = new VideoPlayUtils();
//            instance.context = context;
//        }
//        return instance;
//    }
//
//    SparseArray<ArrayList<PlayInfoResponse>> videoInfos;
//
//    /**
//     * 处理播放相关
//     *
//     * @param videoDetailResponse
//     */
//    public void playVideo(VideoDetailResponse videoDetailResponse) {
//        if (videoDetailResponse == null || videoDetailResponse.playInfo == null || videoDetailResponse.playInfo.size() == 0) {
//            showErrorMsg(ERROR_TAG_FAIL_OTHER, "初始数据为 null，无法继续进行播放", "", "");
//            return;
//        }
//        // 重置数据状态
//        isUseServerParse = false;
//        isUseDMParse = false;
//        isUseClientParse = false;
//        this.videoDetailResponse = videoDetailResponse;
//        // 取消所有之前的加载状态
//        cancelAllVideoTask();
//
//        // 分离视频源
//        parseVideoSource(videoDetailResponse);
//
//
////        // 直接播放cdn
////        if(videoInfos != null && videoInfos.get(2) != null && videoInfos.get(2).size() != 0){
////            playCDNVideo(videoInfos.get(2).get(0));
////        }else {
////            showErrorMsg(ERROR_TAG_FAIL_OTHER, "没有cdn的视频信息", "cdn", videoDetailResponse.vid);
////        }
//
//        // 按优先级播放视频
//        playVideoBySiteId();
//
//    }
//
//    /**
//     * 解析视频源，并对资源进行分类
//     * @param videoDetailResponse
//     */
//    private void parseVideoSource(VideoDetailResponse videoDetailResponse) {
//        videoInfos = new SparseArray<ArrayList<PlayInfoResponse>>();
//        ArrayList<PlayInfoResponse> youtubeInfo = null;
//        ArrayList<PlayInfoResponse> dmInfo = null;
//        ArrayList<PlayInfoResponse> cdnInfo = null;
//
//        for (PlayInfoResponse playInfoResponse : videoDetailResponse.playInfo) {
//            switch (playInfoResponse.siteId) {
//                case 1:
//                case 3: // youtube
//                    // 执行youtube相关操作
//                    if (youtubeInfo == null) {
//                        youtubeInfo = new ArrayList<PlayInfoResponse>();
//                    }
//                    youtubeInfo.add(playInfoResponse);
//                    break;
//                case 2: // dailymotion
//                    if (dmInfo == null) {
//                        dmInfo = new ArrayList<PlayInfoResponse>();
//                    }
//                    dmInfo.add(playInfoResponse);
//
//                    break;
//                case 4: // facebook
//                    break;
//                case 5: // 原力
//                    break;
//
//                case 6: // CDN
//                    if (cdnInfo == null) {
//                        cdnInfo = new ArrayList<PlayInfoResponse>();
//                    }
//                    cdnInfo.add(playInfoResponse);
//                    break;
//                default:
//                    break;
//            }
//        }
//
//        videoInfos.put(0, youtubeInfo);
//        if(youtubeInfo != null){
//            LogCat.e("youtube视频源个数：" + youtubeInfo.size());
//        }
//
//        videoInfos.put(1, dmInfo);
//        if (dmInfo != null) {
//            LogCat.e("dm视频源个数：" + dmInfo.size());
//        }
//
//        videoInfos.put(2, cdnInfo);
//        if (cdnInfo != null) {
//            LogCat.e("cdn视频源个数：" + cdnInfo.size());
//        }
//    }
//
//    // 执行youtube播放
//    public void playYoutubeVideo(PlayInfoResponse playInfoResponse) {
//        LogCat.e("使用客户端解析youtube地址");
//        isUseClientParse = true;
//        videoType = 0;
//        // 重置状态
//        if (youTubeUtility == null) {
//            youTubeUtility = new YouTubeUtility();
//            bindYoutubeEvent();
//        } else {
//            youTubeUtility.cancelQueryYoutubeTask();
//        }
//        youTubeUtility.executeQueryYouTubeTask(YouTubeUtility.TYPE_YOUTUBE_VIDEO_SINGLE, playInfoResponse.remotevid);
//
//    }
//
//    // 执行youtube播放
//    private void playYoutubeVideoByServer() {
//        LogCat.e("使用服务器解析youtube地址");
//        // 重置状态
//        isUseServerParse = true;
//        videoType = 0;
//        YoutubeHttpService.cancleHttpService(context);
//        doHttpVideoUrl(videoDetailResponse.vid);
//    }
//
//
//    // Dm播放
//    private void playDmVideo(PlayInfoResponse playInfoResponse) {
//        LogCat.e("使用dm地址");
//        isUseDMParse = true;
//        videoType = 1;
//        if (dailymotionUtils == null) {
//            dailymotionUtils = new DailymotionUtils();
//            bindDmEvent();
//        } else {
//            dailymotionUtils.cancleTask();
//        }
//
//        dailymotionUtils.getDMVideoUrl(playInfoResponse.remotevid);
//    }
//
//
//    // CDN播放
//    public void playCDNVideo(PlayInfoResponse playInfoResponse) {
//        // 请求path路径
//        doHttpGetCdnPath(playInfoResponse.remotevid);
//        LogCat.e("使用cdn地址");
//    }
//
//    /**
//     * 取消所有的视频加载线程
//     */
//    private void cancelAllVideoTask() {
//        if (youTubeUtility != null) {
//            youTubeUtility.cancelQueryYoutubeTask();
//            youTubeUtility = null;
//        }
//        if (dailymotionUtils != null) {
//            dailymotionUtils.cancleTask();
//            dailymotionUtils = null;
//        }
//
//        YoutubeHttpService.cancleHttpService(context);
//    }
//
//
//    /**
//     * 根据优先级播放视频
//     */
//    private boolean playVideoBySiteId() {
//        boolean isHasOtherSource = false;
//        int length = videoInfos.size();
//        for (int i = 0; i < length; i++) {
//            ArrayList<PlayInfoResponse> videoInfo = videoInfos.get(i);
//            if(videoInfo == null){
//                continue;
//            }
//            // 使用youtube播放，但是已经没有视频源了
//            if (videoInfo.size() == 0) {
//                // 有youtube视频源，同时还没用server获取地址
//                if(i == 0 && isUseServerParse == false){
//                    LogCat.e("youtube没有备用视频源了，立即使用服务器获取播放地址");
//                    if(!TextUtils.isEmpty(videoDetailResponse.vid)){
//                        playYoutubeVideoByServer();
//                        isHasOtherSource = true;
//                    }else {
//                        LogCat.d("vrs的vid为null，无法继续查找播放地址");
//                    }
//                    break;
//                }else{
//                    if(i == 0){
//                        LogCat.e("youtube所有视频源已经无法正常播放了");
//                    }else if(i ==  1){
//                        LogCat.e("dm所有视频源已经无法正常播放了");
//                    }else {
//                        LogCat.e("cdn所有视频源已经无法正常播放了");
//                    }
//                    continue;
//                }
//            }
//            // 每次都取第一个视频，默认作为当前视频。如果当前的videoInfo没有数据了，表明改视频源已经无法播放或不存在
//            PlayInfoResponse playInfoResponse = videoInfo.get(0);
//            if (i == 0) {
//                isHasOtherSource = true;
//                playYoutubeVideo(playInfoResponse);
//                break;
//            } else if (i == 1) {
//                isHasOtherSource = true;
//                playDmVideo(playInfoResponse);
//                break;
//            } else {
//                isHasOtherSource = true;
//                playCDNVideo(playInfoResponse);
//                break;
//            }
//
//        }
//        return isHasOtherSource;
//    }
//
//    private void removeCurFailSource(int keyId) {
//        if(videoInfos != null){
//            ArrayList<PlayInfoResponse> videoInfoItem = videoInfos.get(keyId);
//            if (videoInfoItem == null || videoInfoItem.size() == 0) {
//                return;
//            }
//
//            Object object = new Object();
//            synchronized (object) {
//                videoInfoItem.remove(0);
//            }
//        }
//
//    }
//
//    // 处理youtube回调
//    private void bindYoutubeEvent() {
//        if(myHandler == null){
//            myHandler = new MyHandler();
//        }
//        if (youTubeUtility != null) {
//            // 成功
//            youTubeUtility.setOnQueryYoutubeSuccessListener(new YouTubeUtility.OnQueryYoutubeSuccessListener() {
//                @Override
//                public void onQueryYoutubeSuccess(Uri pResult) {
//                    if(myHandler != null){
//                        Message msg = myHandler.obtainMessage(1);
//                        msg.obj = pResult;
//                        myHandler.sendMessage(msg);
//                    }
//
//                }
//            });
//
//            // 失败
//            youTubeUtility.setOnQueryYoutubeErrorListener(new YouTubeUtility.OnQueryYoutubeErrorListener() {
//                @Override
//                public void onQueryYoutubeError(int errorTag, String errorMsg, String description) {
//                    if(myHandler != null){
//                        Message msg = myHandler.obtainMessage(2);
//                        myHandler.sendMessage(msg);
//                    }
//
//
//                    // 继续去解析其他的youtube视频源，如果没有其他视频源了就是用服务器解析
//                    clientParseErrorStatus += String.valueOf(errorTag) + ", ";
//                    clientParseErrorReason += description + ", ";
//
//
//                }
//            });
//        }
//    }
//
//
//    private MyHandler myHandler;
//
//    private class MyHandler extends android.os.Handler{
//        @Override
//        public void handleMessage(Message msg) {
//            super.handleMessage(msg);
//            if(msg == null){
//                return;
//            }
//            switch (msg.what){
//                case 1:
//                    Uri pResult = (Uri) msg.obj;
//                    LogCat.e("youtube客户端解析成功");
//                    if (onVideoSuccessListener != null) {
//                        String url = null;
//                        if (null != pResult) {
//                            String scheme = pResult.getScheme();
//                            if (null != scheme) {
//                                url = pResult.toString();
//                            } else {
//                                url = pResult.getPath();
//                            }
//                        }
//                        onVideoSuccessListener.onVideoSuccess(url);
//                    }
//                    break;
//                case 2:
//                    // 删除当前视频源
//                    removeCurFailSource(0);
//                    LogCat.e("目前还有的视频源 " + videoInfos.get(0).size());
//                    // 继续去解析其他的youtube视频源，如果没有其他视频源了就是用服务器解析
//                    playVideoBySiteId();
//                    break;
//
//                case 3:
//                    Bundle bundle = msg.getData();
//
//                    showErrorMsg(ERROR_TAG_FAIL_ANALYTICAL, bundle.getString("errorReason"), "2_Dailymotion", bundle.getString("dmVid"));
//                    break;
//            }
//        }
//    }
//
//
//
//    private void bindDmEvent() {
//        if (dailymotionUtils != null) {
//            dailymotionUtils.setOnSuccessListener(new DailymotionUtils.OnSuccessListener() {
//                @Override
//                public void onSuccess(String url) {
//                    LogCat.e("dm解析成功");
//                    if (onVideoSuccessListener != null) {
//                        onVideoSuccessListener.onVideoSuccess(url);
//                    }
//                }
//            });
//
//            dailymotionUtils.setOnFailListener(new DailymotionUtils.OnFailListener() {
//                @Override
//                public void onFail(String dmVid, String errorMsg) {
//                    dmParseErrorReason = errorMsg;
//                    LogCat.e("dm解析失败：" + errorMsg);
//                    // 此时表示youtube和dm都无法正常播放了，只能按照优先级依次去通过其他方式获取播放地址
//                    // 删除当前视频源
//                    removeCurFailSource(1);
//                    // 查询其他的视频源
//                    boolean isHasOtherSource = playVideoBySiteId();
//
//
//                    // 没有其他视频源的时候，表示此视频无法播放，需要上报
//                    if (isHasOtherSource == false) {
//                        String errorReason = null;
//                        if (isUseClientParse) {
//                            errorReason = "客户端解析失败，失败状态：" + clientParseErrorStatus + ", 失败原因：" + clientParseErrorReason;
//                        }
//                        if (isUseServerParse) {
//                            errorReason += "，服务器端解析失败，失败原因：" + serverErrorMsg;
//                        }
//                        errorReason += "，dm解析失败，失败原因：" + errorMsg;
//
//                        if(myHandler != null){
//                            Message msg = myHandler.obtainMessage(3);
//                            Bundle bundle = new Bundle();
//                            bundle.putString("errorReason", errorReason);
//                            bundle.putString("dmVid", dmVid);
//                            msg.setData(bundle);
//                            myHandler.sendMessage(msg);
//                        }
//                        showErrorMsg(ERROR_TAG_FAIL_ANALYTICAL, errorReason, "2_Dailymotion", dmVid);
//                    }
//                }
//            });
//        }
//    }
//
//
//    private void doHttpVideoUrl(String vid) {
//        Map<String, String> params = new HashMap<String, String>();
//        params.put("videoId", vid);
//        YoutubeHttpService.doHttpYoutube(context, params, new OnRequestListener<YoutubeUrlDataResponse>() {
//            @Override
//            public void onSuccess(YoutubeUrlDataResponse response, String url) {
//                if (response == null || response.data == null || response.data.playInfo == null || response.data.playInfo.size() == 0) {
//                    doError(ERROR_TAG_FAIL_ANALYTICAL, "服务器解析到的数据集合出错，出现了可能为空等情况");
//                    return;
//                }
//                String finalUrl = response.data.playInfo.get(0).reSourceURL;
//                LogCat.e("youtube服务端解析地址：" + finalUrl);
////                // 测试
////                finalUrl = null;
//                if (TextUtils.isEmpty(finalUrl)) {
//                    serverErrorMsg = "通过服务器获取到的地址为空";
//                    LogCat.e("youtube服务端解析失败：" + serverErrorMsg);
//                    doError(ERROR_TAG_FAIL_NET, serverErrorMsg);
//                } else {
//                    LogCat.e("youtube服务端解析成功：");
//                    if (onVideoSuccessListener != null) {
//                        onVideoSuccessListener.onVideoSuccess(finalUrl);
//                    }
//                }
//            }
//
//            @Override
//            public void onError(String errorMsg, String url) {
//                doError(ERROR_TAG_FAIL_OTHER, errorMsg);
//                LogCat.e("youtube服务端解析失败：" + errorMsg);
//            }
//
//            private void doError(int errorCode, String errorMsg) {
//                // youtube已经全部解析失败，开始尝试使用其他的视频源进行播放
//                LogCat.e("*****youtube已经全部解析失败，开始尝试使用其他的视频源进行播放******");
//                boolean isHasOtherSource = playVideoBySiteId();
//                // 查询其他的视频源
//                // 没有其他视频源的时候，表示此视频无法播放，需要上报
//                if (isHasOtherSource == false) {
//                    serverErrorMsg = errorMsg;
//                    String errorReason = null;
//                    if (isUseClientParse) {
//                        errorReason = "客户端解析失败，失败状态：" + clientParseErrorStatus + ", 失败原因：" + clientParseErrorReason;
//                    }
//                    errorReason += "，服务器端解析失败（此时已经意味着其他youtube视频源失效）失败原因：" + errorMsg;
//                    showErrorMsg(errorCode, errorReason, "1|3_youtube", "所有youtube源失效");
//                    LogCat.e("*******服务器获取地址失败，此时已经没有其他视频源了");
//                }else{
//                    LogCat.e("*******还有备用视频源可以用*******");
//                }
//            }
//
//        });
//    }
//
//    private void doHttpGetCdnPath(final String vid) {
//        CDNHttpService.doHttpGetCdnPath(context, new OnRequestListener<CdnPathResponse>() {
//
//            @Override
//            public void onSuccess(CdnPathResponse response, String url) {
//                if(context == null){
//                    return;
//                }
//                if(response == null || !(response instanceof  CdnPathResponse)){
//                    doError(ERROR_TAG_FAIL_OTHER, "服务器解析到的数据集合出错，出现了为空等情况");
//                    return;
//                }
//                if(response.data == null) {
//                    doError(ERROR_TAG_FAIL_OTHER, "解析到的data集合数据出错");
//                    return;
//                }
//                if(TextUtils.isEmpty(response.data.key)){
//                    doError(ERROR_TAG_FAIL_OTHER, "解析到的data集合key数据出错");
//                    return;
//                }
//
//
//                videoType = 2;
//                if (TextUtils.isEmpty(vid)) {
//                    String errorReason = null;
//                    if (isUseClientParse) {
//                        errorReason = "客户端解析失败，失败状态：" + clientParseErrorStatus + ", 失败原因：" + clientParseErrorReason;
//                    }
//                    if (isUseServerParse) {
//                        errorReason += "，服务器端解析失败，失败原因：" + serverErrorMsg;
//                    }
//                    if (isUseDMParse) {
//                        errorReason += "，dm解析失败，失败原因：" + dmParseErrorReason;
//                    }
//                    errorReason += "CDN的路径为空";
//                    showErrorMsg(ERROR_TAG_FAIL_OTHER, errorReason, "6_CDN", vid);
//                    return;
//                }
//                StringBuilder sbUrl = new StringBuilder(SecurityChain.SECURITY_CHAIN_URL);
//                // 添加路径
//                sbUrl.append(vid);
//                sbUrl.append("?");
//                // 添加st 有效期
//                sbUrl.append("st=");
//                String st = SecurityChain.getOutDate();
//                sbUrl.append(st);
//                // 添加token
//                sbUrl.append("&token=");
//                String token = SecurityChain.getSecurityTokey(vid, st, response.data.key);
//                VideoSharedPreference sharedPreference = VideoSharedPreference.getSharedPreferenceUtils(context);
//                sharedPreference.saveDate(VideoSharedPreference.VIDEO_CDN_TOKEN, token);
//                sbUrl.append(token);
//
//
//                if (onVideoSuccessListener != null) {
//                    onVideoSuccessListener.onVideoSuccess(sbUrl.toString());
//
//                }
//            }
//
//            @Override
//            public void onError(String errorMsg, String url) {
//                if(context == null){
//                    return;
//                }
//
//                // 先从本地获取
//                VideoSharedPreference sharedPreference = VideoSharedPreference.getSharedPreferenceUtils(context);
//                String token = sharedPreference.getDate(VideoSharedPreference.VIDEO_CDN_TOKEN, "");
//                if(TextUtils.isEmpty(token)){
//                    doError(ERROR_TAG_FAIL_NET, errorMsg);
//                }else {
//                    StringBuilder sbUrl = new StringBuilder(SecurityChain.SECURITY_CHAIN_URL);
//                    // 添加路径
//                    sbUrl.append(vid);
//                    sbUrl.append("?");
//                    // 添加st 有效期
//                    sbUrl.append("st=");
//                    String st = SecurityChain.getOutDate();
//                    sbUrl.append(st);
//                    // 添加token
//                    sbUrl.append("&token=");
//                    sbUrl.append(token);
//
//
//                    if (onVideoSuccessListener != null) {
//                        onVideoSuccessListener.onVideoSuccess(sbUrl.toString());
//
//                    }
//                }
//            }
//
//            private void doError(int tag, String errorMsg){
//                String errorReason = null;
//                if (isUseClientParse) {
//                    errorReason = "客户端解析失败，失败状态：" + clientParseErrorStatus + ", 失败原因：" + clientParseErrorReason;
//                }
//                if (isUseServerParse) {
//                    errorReason += "，服务器端解析失败，失败原因：" + serverErrorMsg;
//                }
//                if (isUseDMParse) {
//                    errorReason += "，dm解析失败，失败原因：" + dmParseErrorReason;
//                }
//                errorReason += "CDN的path获取失败，原因：" + errorMsg;
//                showErrorMsg(tag, errorReason, "6_CDN", vid);
//            }
//
//        });
//    }
//
//
//    /**
//     * 当视频无法正常播放的时候，继续查找其他视频源播放，
//     * @return false:表示没有其他视频源了；true：还有其他视频源；
//     */
//    public boolean rePlayVideo(){
//        if(videoInfos == null || videoInfos.size() == 0){
//            return false;
//        }
////        boolean hasYoutubeSource = false;
////        if(videoInfos.get(0) != null && videoInfos.size() != 0){
////            hasYoutubeSource = true;
////        }
//        LogCat.e("当前使用的解析类型videoType ：" + videoType);
//        removeCurFailSource(videoType);
////
//        if(videoInfos.get(0) == null || videoInfos.get(0).size() == 0){
//            if(isUseServerParse){
//                LogCat.e("已经使用了服务器方式获取地址");
//                return false;
//            }else{
//                return playVideoBySiteId();
//            }
//        }
//
//        if(videoInfos.get(1) == null || videoInfos.get(1).size() == 0){
//            return false;
//        }
//        if(videoInfos.get(2) == null || videoInfos.get(2).size() == 0){
//            return false;
//        }
//
//        return playVideoBySiteId();
//    }
//
//    private void showErrorMsg(int status, String msg, String third, String thirdId) {
//        if (onVideoFailListener != null) {
//            onVideoFailListener.onVideoFail(status, msg, third, thirdId);
//        }
//    }
//
//
//    private OnVideoSuccessListener onVideoSuccessListener;
//
//    public interface OnVideoSuccessListener {
//        void onVideoSuccess(String url);
//    }
//
//    public void setOnVideoSuccessListener(OnVideoSuccessListener onVideoSuccessListener) {
//        this.onVideoSuccessListener = onVideoSuccessListener;
//    }
//
//    private OnVideoFailListener onVideoFailListener;
//
//    public interface OnVideoFailListener {
//        void onVideoFail(int errorTag, String description, String third, String thirdId);
//    }
//
//    public void setOnVideoFailListener(OnVideoFailListener onVideoFailListener) {
//        this.onVideoFailListener = onVideoFailListener;
//    }
//
//}
