package com.httputils.http.service;

import android.content.Context;

import com.httputils.http.HttpUtils;
import com.httputils.http.OnRequestListener;
import com.httputils.http.response.AlbumListByLabelResponse;
import com.httputils.http.response.DetailResponse;
import com.httputils.http.response.FocusListResponse;
import com.httputils.http.response.LiveChannelDataResponse;
import com.httputils.http.response.LiveTopicResponse;
import com.httputils.http.response.MenuResponse;
import com.httputils.http.response.TopicListResponse;
import com.httputils.http.response.UpdateResponse;
import com.httputils.http.response.VideoDetailListResponse;
import com.httputils.http.response.YoutubeUrlDataResponse;

import java.util.Map;

/**
 * Created by zfy on 2015/8/18.
 */
public class AlbnumHttpService {

    /**
     * 升级接口
     */
    public static final String URL_CHECK_UPDATE = "http://apk.gochinatv.com/api/queryApkUpdateVersion";
    /**
     * 升级状态
     */
    public static final String URL_UPDATE_STATUS = "http://apk.gochinatv.com/api/saveUpdateLog";
    /**
     * webview播放youtube视频前缀
     */
    public static final String URL_WEBVIEW_YOUTUBE = "http://corp.gochinatv.com/cn/androidPlayer/player.html?youtubePlayId=";


    /**
     * 频道最热专辑列表,
     */
  //  public static final String URL_ALBNUM_LIST_BY_LABEL = NEW_BASE_HTTP_URL + "/album_v1/albumListByLabel.json";

    /**
     * 频道最热专辑列表,
     */
   // public static final String URL_ALBNUM_LIST_BY_INDEX = NEW_BASE_HTTP_URL + "/album_v1/albumListByIndex.json";
    /**
     * 新接口
     */
    private final static String NEW_BASE_HTTP_URL = "http://api.vego.tv";
    /**
     * 内容焦点图，推荐
     */
   // public static final String URL_FOCUS_LIST = NEW_BASE_HTTP_URL + "/focus_v1/focusList";
    /**
     * 频道导航列表，分类
     */
    //public static final String URL_MEUMLIST = NEW_BASE_HTTP_URL + "/menu_v1/menuList";
    /**
     * 推荐专辑列表，
     */
   // public static final String URL_GET_VIDEO_RECOMMEND_LIST = NEW_BASE_HTTP_URL + "/album_v1/recommendation";
    /**
     * 专辑详情页，
     */
    public static final String URL_ALBNUM_DETAIL = NEW_BASE_HTTP_URL + "/album_v1/albumDetail";

    /**
     * 视频列表,
     */
    public static final String URL_VIDEO_LIST = NEW_BASE_HTTP_URL + "/video_v1/videoList";

    public static final String URL_VIDEO_LIST_TEST = "http://210.14.158.187" + "/video_v1/videoList";

    /**
     * 视频播放信息,
     */
    public static final String URL_VIDEO_INFO = NEW_BASE_HTTP_URL + "/video_v1/playinfo";

    /**
     * 直播列表，
     */
    public static final String URL_LIVE_LIST = NEW_BASE_HTTP_URL + "/live_v1/getLiveList";

    /**
     * 检索导航(条件)，
     */
    public static final String URL_ALBUM_RETRIEVE = NEW_BASE_HTTP_URL + "/album_v1/getRetrieve";

    /**
     * 检索导航（视频），
     */
 //   public static final String URL_VIDEO_RETRIEVE = NEW_BASE_HTTP_URL + "/album_v1/retrieveList";

    /**
     * 专题列表接口（视频），
     */
    public static final String URL_TOPIC_LIST = NEW_BASE_HTTP_URL + "/focus_v1/focusTypeList";

    /**
     * 专题详情接口（视频），
     */
    public static final String URL_TOPIC_DETAIL = NEW_BASE_HTTP_URL + "/focus_v1/focusList";


    // ===============================获取youtube真正播放地址==================================
    /**
     * 获取youtube真正播放地址
     */
    public static final String URL_GET_YOUTUBE_URL = NEW_BASE_HTTP_URL + "/h5/video_v1/videoDetail.json";



    /**
     * 上传无法播放地址
     */
    public static final String URL_UPDATE_ERROR_LOG = "http://na.uas.vego.tv/usrAction/errorLogServlet";


//    /**
//     * 首页的推荐
//     */
//    public static void doHttpFocusList(Context context,Map<String,String> map,OnRequestListener<FocusListResponse> listener){
//        HttpUtils.getInstance(context).doHttpGet(FocusListResponse.class,URL_FOCUS_LIST, map,listener,"focus");
//    }

    /**
     * 首页的分类
     */
//    public static void doHttpMeumList(Context context,Map<String,String> map,OnRequestListener<MenuResponse> listener){
//        HttpUtils.getInstance(context).doHttpGet(MenuResponse.class,URL_MEUMLIST,map,listener,"meum");
//    }

    /**
     * 升级接口
     */
    public static void doHttpUpdateApk(Context context,Map<String,String> map,OnRequestListener<UpdateResponse> listener){
        HttpUtils.getInstance(context).doHttpGet(UpdateResponse.class,URL_CHECK_UPDATE,map,listener,"update");
    }

    /**
     * 升级反馈
     */
    public static void doHttpUpdateFeedback(Context context,Map<String,String> map,OnRequestListener<UpdateResponse> listener){
        HttpUtils.getInstance(context).doHttpGet(UpdateResponse.class,URL_UPDATE_STATUS,map,listener,"back");
    }



    /**
     * 频道最热专辑列表,分类进去
     */
//    public static void doHttpAlbnumList(Context context,String url,Map<String,String> map,OnRequestListener<AlbumListByLabelResponse> listener){
//        HttpUtils.getInstance(context).doHttpGet(AlbumListByLabelResponse.class,url,map,listener,"hot");
//    }

    /**
     * 频道最热专辑列表,分类进去--带header去过滤版权问题
     */
    public static void doHttpAlbnumListWithHeader(Context context,String url,Map<String,String> header,Map<String,String> map,OnRequestListener<AlbumListByLabelResponse> listener){
        HttpUtils.getInstance(context).doHttpGet(AlbumListByLabelResponse.class,url,header,map,listener,"hot");
    }



    /**
     * 推荐专辑列表
     */
//    public static void doHttpRecommentAlbnumList(Context context,Map<String,String> header,Map<String,String> map,OnRequestListener<AlbumListByLabelResponse> listener){
//        HttpUtils.getInstance(context).doHttpGet(AlbumListByLabelResponse.class,URL_GET_VIDEO_RECOMMEND_LIST,header,map,listener,"recomment");
//    }


    /**
     * 专辑详情
     */
    public static void doHttpAlbnumDetails(Context context,Map<String,String> map,OnRequestListener<DetailResponse> listener){
        HttpUtils.getInstance(context).doHttpGet(DetailResponse.class,URL_ALBNUM_DETAIL,map,listener,"details");
    }


    boolean isTest;

    /**
     * 专辑集数列表
     */
    public static void doHttpAlbnumEpisodesList(Context context,Map<String,String> map, boolean isTest, OnRequestListener<VideoDetailListResponse> listener, String tag){
        HttpUtils.getInstance(context).doHttpGet(VideoDetailListResponse.class, isTest?URL_VIDEO_LIST_TEST:URL_VIDEO_LIST,map,listener, tag);
    }

    /**
     * vip影片和检索影片
     */
//    public static void doHttpVipAndSearchAlbumList(Context context,Map<String,String> header,Map<String,String> map,OnRequestListener<AlbumListByLabelResponse> listener){
//        HttpUtils.getInstance(context).doHttpGet(AlbumListByLabelResponse.class,URL_VIDEO_RETRIEVE,header,map,listener,"vip");
//    }


    /**
     * 专题列表
     */
    public static void doHttpTopicList(Context context, Map<String, String> map, OnRequestListener<TopicListResponse> listener) {
        HttpUtils.getInstance(context).doHttpGet(TopicListResponse.class, URL_TOPIC_LIST, map, listener, "topicList");
    }


    /**
     * 专题详情
     */
    public static void doHttpTopicDetail(Context context, Map<String, String> map, OnRequestListener<FocusListResponse> listener) {
        HttpUtils.getInstance(context).doHttpGet(FocusListResponse.class, URL_TOPIC_DETAIL, map, listener, "topic");
    }


    /**
     * 轮播
     */
    public static void doHttpCarousel(Context context, OnRequestListener<LiveChannelDataResponse> listener) {
        HttpUtils.getInstance(context).doHttpGet(LiveChannelDataResponse.class, URL_LIVE_LIST, listener, "carousel");
    }


    /**
     * 获取youtobe真实地址
     */
    public static void doHttpYoutubeUrl(Context context, Map<String, String> map, OnRequestListener<YoutubeUrlDataResponse> listener) {
        HttpUtils.getInstance(context).doHttpGet(YoutubeUrlDataResponse.class, URL_GET_YOUTUBE_URL, map, listener, "youtube");
    }

    /**
     * 上传播放失败日志
     */
    public static void doHttpPlayVideoError(Context context, Map<String, String> map, OnRequestListener<String> listener){
        HttpUtils.getInstance(context).doHttpPost(URL_UPDATE_ERROR_LOG, map, listener, "error");

    }


    /**
     * 取消请求
     */
    public static void doHttpCancelRequests(Context context, String tag){
        HttpUtils.getInstance(context).cancelPendingRequests(tag);

    }




    /*******************  阅兵新需要**************************/
    public static final  String URL_GET_SERVER_TIME = "http://ec2-54-148-190-90.us-west-2.compute.amazonaws.com:8080/data.jsp";

    public static void doHttpLiveTopic(Context context,OnRequestListener<LiveTopicResponse> listener){
        HttpUtils.getInstance(context).doHttpGet(LiveTopicResponse.class,URL_GET_SERVER_TIME,listener,"liveTopic");
    }



    /************************************  正版与聚合版新接口  **************************************/

    public static final String BASE_HTTP_URL = "http://cloudapi.vego.tv";


    /**
     * 首页的分类
     */
    public static final String URL_MEUMLIST = BASE_HTTP_URL + "/device-tv/tv/iv1/home/menu.json";

    public static void doHttpMeumList(Context context,Map<String,String> map,OnRequestListener<MenuResponse> listener){
        HttpUtils.getInstance(context).doHttpGet(MenuResponse.class,URL_MEUMLIST,map,listener,"meum");
    }


    /**
     * 首页的推荐
     */
    public static final String URL_FOCUS_LIST = BASE_HTTP_URL + "/device-tv/tv/iv1/subject/subject.json";

    public static void doHttpFocusList(Context context,Map<String,String> map,OnRequestListener<FocusListResponse> listener){
        HttpUtils.getInstance(context).doHttpGet(FocusListResponse.class,URL_FOCUS_LIST, map,listener,"focus");
    }




    /**
     * 频道专辑列表,分类进去
     */
    public static final String URL_ALBNUM_LIST = BASE_HTTP_URL + "/device-tv/tv/iv1/home/videos.json";

    public static void doHttpAlbnumList(Context context,Map<String,String> map,OnRequestListener<AlbumListByLabelResponse> listener){
        HttpUtils.getInstance(context).doHttpGet(AlbumListByLabelResponse.class,URL_ALBNUM_LIST,map,listener,"hot");
    }



    /**
     * 推荐专辑列表--猜你喜欢
     */
    public static final String URL_GET_VIDEO_RECOMMEND_LIST = BASE_HTTP_URL + "/device-tv/tv/iv1/home/recommendations.json";

    public static void doHttpRecommentAlbnumList(Context context,Map<String,String> map,OnRequestListener<AlbumListByLabelResponse> listener){
        HttpUtils.getInstance(context).doHttpGet(AlbumListByLabelResponse.class,URL_GET_VIDEO_RECOMMEND_LIST,map,listener,"recomment");
    }


    /**
     * vip影片和检索影片
     */
    public static final String URL_VIDEO_RETRIEVE = BASE_HTTP_URL + "/device-tv/tv/iv1/search/list.json";

    public static void doHttpVipAndSearchAlbumList(Context context,Map<String,String> map,OnRequestListener<AlbumListByLabelResponse> listener){
        HttpUtils.getInstance(context).doHttpGet(AlbumListByLabelResponse.class,URL_VIDEO_RETRIEVE,map,listener,"vip");
    }


    public static void cancleHttp(Context context){
        HttpUtils.getInstance(context).cancelPendingRequests("episodes");
    }
}
