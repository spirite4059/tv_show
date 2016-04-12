package com.gochinatv.ad.base;

import android.app.Application;
import android.text.TextUtils;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.gochinatv.ad.tools.DataUtils;
import com.gochinatv.ad.tools.LogCat;
import com.nostra13.universalimageloader.cache.disc.impl.UnlimitedDiscCache;
import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.cache.memory.impl.LRULimitedMemoryCache;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.nostra13.universalimageloader.core.download.BaseImageDownloader;
import com.nostra13.universalimageloader.utils.StorageUtils;
import com.umeng.analytics.AnalyticsConfig;
import com.umeng.analytics.MobclickAgent;

import java.io.File;

/**
 * Created by fq_mbp on 16/1/28.
 */
public class MyApplication extends Application{

    private File cacheDir = null;


    @Override
    public void onCreate() {
        super.onCreate();
        Fresco.initialize(this);
        try {
            String mac = TextUtils.isEmpty(DataUtils.getMacAddress(this)) ? "" : DataUtils.getMacAddress(this);
            mac = mac.replace(":", "");
            LogCat.e("mac: " + mac);
            AnalyticsConfig.setChannel(mac);
        } catch (Exception e) {
            e.printStackTrace();
        }

        MobclickAgent.openActivityDurationTrack(false);
        MobclickAgent.setCatchUncaughtExceptions(true);
        MobclickAgent.setDebugMode(true);

        cacheDir = StorageUtils.getOwnCacheDirectory(this, "imageloader/Cache");
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(this)
                // .memoryCacheExtraOptions(480, 800)
                // // max width, max height，即保存的每个缓存文件的最大长宽
                // .discCacheExtraOptions(480, 800, CompressFormat.JPEG, 75,
                // null)
                // Can slow ImageLoader, use it carefully (Better don't use
                // it)/设置缓存的详细信息，最好不要设置这个
                .threadPoolSize(3)
                        // 线程池内加载的数量
                .threadPriority(Thread.NORM_PRIORITY - 2).denyCacheImageMultipleSizesInMemory()
                .memoryCache(new LRULimitedMemoryCache(10 * 1024 * 1024))
                        // You can pass your own memory cache
                        // implementation/你可以通过自己的内存缓存实现
                .memoryCacheSize(10 * 1024 * 1024)
                .discCacheSize(30 * 1024 * 1024).discCacheFileNameGenerator(new Md5FileNameGenerator())
                        // 将保存的时候的URI名称用MD5 加密
                .tasksProcessingOrder(QueueProcessingType.FIFO).discCacheFileCount(300)
                        // 缓存的文件数量
                .discCache(new UnlimitedDiscCache(cacheDir))
                        // 自定义缓存路径
                .imageDownloader(new BaseImageDownloader(this, 5 * 1000, 15 * 1000)) // connectTimeout
                .writeDebugLogs() // Remove for release app
                .build();// 开始构建
        ImageLoader.getInstance().init(config); // 初始化

    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        ImageLoader image = ImageLoader.getInstance();
        image.clearMemoryCache();
    }


}
