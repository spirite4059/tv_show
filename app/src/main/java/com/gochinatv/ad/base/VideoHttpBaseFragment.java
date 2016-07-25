package com.gochinatv.ad.base;

/**
 * Created by fq_mbp on 16/3/17.
 */
public abstract class VideoHttpBaseFragment extends BaseFragment {


    private boolean isTest = true;

//    protected abstract void onGetVideoListSuccessful(VideoDetailListResponse response, String turl);

//    protected abstract void onGetVideoListFailed(String errorMsg, String turl);

    protected abstract void onGetVideoPathSuccessful(String path);

    protected abstract void onGetVideoPathFailed(String path);

//    protected abstract void onGetVideoListSuccess(AdVideoListResponse response, String turl);




//    protected abstract void onUpgradeSuccessful(UpdateResponse.UpdateInfoResponse updateInfo);
//
//    /**
//     * 请求视频列表数
//     */
//    protected void doHttpGetEpisode() {
//        Map<String, String> map = new HashMap<>();
//        map.put("albumId", "63272");    // 测试用
////        map.put("albumId", "66371");
////        map.put("albumId", "67282");  // 小视频
//        map.put("videoType", "1");
//        map.put("serialType", "1");
//
//        String turl = isTest ? HttpUrls.URL_VIDEO_LIST_TEST : HttpUrls.URL_VIDEO_LIST;
//
//        OkHttpUtils.getInstance().doHttpGet(turl, map, new OkHttpCallBack<VideoDetailListResponse>() {
//            @Override
//            public void onSuccess(String turl, VideoDetailListResponse response) {
//                LogCat.e("onSuccess........" + turl);
//                onGetVideoListSuccessful(response, turl);
//            }
//
//            @Override
//            public void onError(String turl, String errorMsg) {
//                LogCat.e("onDownloadFileError........");
//                onGetVideoListFailed(errorMsg, turl);
//            }
//        });
//    }






//    protected void doHttpGetCdnPath(final String vid) {
//        LogCat.e("获取cdn的真是地址。。。。。。。" + vid);
//        Map<String, String> turl = new HashMap();
//        turl.put("turl", HttpUrls.SECURITY_CHAIN_URL + vid);
//
//        OkHttpUtils.getInstance().doHttpGet(HttpUrls.HTTP_URL_CDN_PATH, turl, new OkHttpCallBack<CdnPathResponse>() {
//            @Override
//            public void onSuccess(String turl, CdnPathResponse response) {
//                if (!isAdded()) {
//                    return;
//                }
//                LogCat.e("onSuccess。。。。。。。" + turl);
//                if (response == null || !(response instanceof CdnPathResponse)) {
//                    LogCat.e("cdn地址请求成功 数据错误1。。。。。。。");
//                    onGetVideoPathFailed(turl);
//                    return;
//                }
//                if (response.data == null) {
//                    LogCat.e("cdn地址请求成功 数据错误2。。。。。。。");
//                    onGetVideoPathFailed(turl);
//                    return;
//                }
//
//
//                if (TextUtils.isEmpty(response.data.turl)) {
//                    LogCat.e("cdn地址为空。。。。。。。");
//                    onGetVideoPathFailed(turl);
//                    return;
//                }
//
//                onGetVideoPathSuccessful(response.data.turl);
//
//            }
//
//            @Override
//            public void onError(String turl, String errorMsg) {
//                if (!isAdded()) {
//                    return;
//                }
//                LogCat.e("cdn地址获取失败。。。。。。。" + turl);
//                onGetVideoPathFailed(turl);
//            }
//        });
//
//    }












}
