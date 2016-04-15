package com.gochinatv.ad.base;

import android.text.TextUtils;

import com.gochinatv.ad.tools.LogCat;
import com.httputils.http.response.CdnPathResponse;
import com.httputils.http.response.VideoDetailListResponse;
import com.okhtttp.OkHttpCallBack;
import com.okhtttp.OkHttpUtils;
import com.okhtttp.response.AdVideoListResponse;
import com.okhtttp.service.VideoHttpService;
import com.tools.HttpUrls;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by fq_mbp on 16/3/17.
 */
public abstract class VideoHttpBaseFragment extends BaseFragment {


    private boolean isTest = true;

    protected abstract void onGetVideoListSuccessful(VideoDetailListResponse response, String url);

    protected abstract void onGetVideoListFailed(String errorMsg, String url);

    protected abstract void onGetVideoPathSuccessful(String path);

    protected abstract void onGetVideoPathFailed(String path);

    protected abstract void onGetVideoListSuccess(AdVideoListResponse response, String url);




//    protected abstract void onUpgradeSuccessful(UpdateResponse.UpdateInfoResponse updateInfo);

    /**
     * 请求视频列表数
     */
    protected void doHttpGetEpisode() {
        Map<String, String> map = new HashMap<>();
        map.put("albumId", "63272");    // 测试用
//        map.put("albumId", "66371");
//        map.put("albumId", "67282");  // 小视频
        map.put("videoType", "1");
        map.put("serialType", "1");

        String url = isTest ? HttpUrls.URL_VIDEO_LIST_TEST : HttpUrls.URL_VIDEO_LIST;

        OkHttpUtils.getInstance().doHttpGet(url, map, new OkHttpCallBack<VideoDetailListResponse>() {
            @Override
            public void onSuccess(String url, VideoDetailListResponse response) {
                LogCat.e("onSuccess........" + url);
                onGetVideoListSuccessful(response, url);
            }

            @Override
            public void onError(String url, String errorMsg) {
                LogCat.e("onDownloadFileError........");
                onGetVideoListFailed(errorMsg, url);
            }
        });
    }



    protected void doHttpGetVideoList(){
        VideoHttpService.doHttpGetVideoList(getActivity(), new OkHttpCallBack<AdVideoListResponse>() {
            @Override
            public void onSuccess(String url, AdVideoListResponse response) {
                LogCat.e("新街口成功了........*******************.");
                if (!isAdded()) {
                    return;
                }

                onGetVideoListSuccess(response, url);

            }

            @Override
            public void onError(String url, String errorMsg) {
                LogCat.e("新街口onError了.........********************");
                onGetVideoListFailed(errorMsg, url);

            }
        });

    }



    protected void doHttpGetCdnPath(final String vid) {
        LogCat.e("获取cdn的真是地址。。。。。。。" + vid);
        Map<String, String> url = new HashMap();
        url.put("url", HttpUrls.SECURITY_CHAIN_URL + vid);

        OkHttpUtils.getInstance().doHttpGet(HttpUrls.HTTP_URL_CDN_PATH, url, new OkHttpCallBack<CdnPathResponse>() {
            @Override
            public void onSuccess(String url, CdnPathResponse response) {
                if (!isAdded()) {
                    return;
                }
                LogCat.e("onSuccess。。。。。。。" + url);
                if (response == null || !(response instanceof CdnPathResponse)) {
                    LogCat.e("cdn地址请求成功 数据错误1。。。。。。。");
                    onGetVideoPathFailed(url);
                    return;
                }
                if (response.data == null) {
                    LogCat.e("cdn地址请求成功 数据错误2。。。。。。。");
                    onGetVideoPathFailed(url);
                    return;
                }


                if (TextUtils.isEmpty(response.data.url)) {
                    LogCat.e("cdn地址为空。。。。。。。");
                    onGetVideoPathFailed(url);
                    return;
                }

                onGetVideoPathSuccessful(response.data.url);

            }

            @Override
            public void onError(String url, String errorMsg) {
                if (!isAdded()) {
                    return;
                }
                LogCat.e("cdn地址获取失败。。。。。。。" + url);
                onGetVideoPathFailed(url);
            }
        });

    }












}
