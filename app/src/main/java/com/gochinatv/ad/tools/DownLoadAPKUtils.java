package com.gochinatv.ad.tools;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;

import com.download.tools.ToolUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by zfy on 2016/5/9.
 */
public class DownLoadAPKUtils {

    private int progress;

    public static final String ACTION = "gochinatv.utils.DownLoadFileUtils";

    private OnDownLoadProgressListener onDownLoadProgressListener;

    private OnDownLoadFinishListener onDownLoadFinishListener;

    private OnDownLoadErrorListener onDownLoadErrorListener;

    private final String DOWNLOAD_APK_NAME = Constants.FILE_APK_NAME;

    private final String DOWNLOAD_APK_DIC = "DOWNLOAD_APK_DIC";

    private int count = 0;

    // 是否终止下载
    private boolean isInterceptDownload = false;

    private String apkFile;

    /**
     * 检测sdcard是否存在
     *
     * @return
     */
    public boolean checkSD() {
        if (android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED)) {
            return true;
        } else
            return false;
    }

    private Handler handler = new Handler(){

        @Override
        public void dispatchMessage(Message msg) {
            super.dispatchMessage(msg);
            switch (msg.what) {
                case 0:
                    if (onDownLoadFinishListener != null) {
                        onDownLoadFinishListener.onDownLoadFinish(apkFile);
                    }
                    break;

                case 1:
                    if (onDownLoadProgressListener != null) {
                        onDownLoadProgressListener.onDownLoadProgress(progress, apkFile);
                    }
                    break;
                case 2:
                    if(onDownLoadErrorListener != null){
                        if(msg != null && msg.obj != null){
                            Exception e = (Exception) msg.obj;
                            onDownLoadErrorListener.onDownLoadFinish(e);
                        }
                    }
                    break;

                default:
                    break;
            }

        }
    };

    /**
     *
     * @param context
     *            是否显示进度条
     * @param urlDownload
     *            下载地址
     */
    public void downLoad(Context context, final String urlDownload) {
        if(context == null){
            LogCat.e(" context == null 无法下载");
            return;
        }
        // 文件路径检查
        if (TextUtils.isEmpty(urlDownload)) {
            LogCat.e(" 下载的url为空无法下载");
            return;
        }
        // sdcard 检查
        if (!ToolUtils.isExistSDCard()) {
            LogCat.e("SD卡不存在无法下载。。。。。");
            return;
        }

        //重置下载进度
        count = 0;
        progress = 0;
        isInterceptDownload = false;

        final Context appContext = context.getApplicationContext();
        ExecutorService pool = Executors.newCachedThreadPool();
        pool.execute(new Runnable() {

            @Override
            public void run() {
                LogCat.e("APKdownload","apk开始下载…");
                InputStream is = null;
                FileOutputStream fos = null;
                try {
                    URL url = new URL(urlDownload);
                    // 打开连接
                    URLConnection con = url.openConnection();
                    //设置超时时间
                    con.setConnectTimeout(30000);
                    con.setReadTimeout(30000);
                    // 输入流
                    is = con.getInputStream();

                    final int contentLength = con.getContentLength();

//                    File folder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
//                    if (!folder.exists() || !folder.isDirectory()) {
//                        folder.mkdirs();
//                    }
                    File folder = new File(DataUtils.getApkDirectory());
                    if(!folder.exists()|| !folder.isDirectory()){
                        folder.mkdirs();
                    }

                    // 下载服务器中新版本软件（写文件）
                    apkFile = folder.getAbsolutePath() + File.separator + DOWNLOAD_APK_NAME;
                    File ApkFile = new File(apkFile);
                    // 如果已经存在 则删除
                    if(ApkFile.exists()){
                        ApkFile.delete();
                        ApkFile.createNewFile();
                    }
                    fos = new FileOutputStream(ApkFile);
                    byte buf[] = new byte[2048];

                    long startTime = System.currentTimeMillis();

                    do {
                        int numRead = is.read(buf);
                        count += numRead;
                        // 更新进度条
                        progress = count;

                        long currentTime = System.currentTimeMillis();
                        if(currentTime - startTime >= 1000){
                            Message msg = handler.obtainMessage();
                            msg.what = 1;
                            msg.arg1 = progress;
                            handler.sendMessage(msg);
                            startTime = System.currentTimeMillis();
                        }


                        if (numRead <= 0) {
                            LogCat.e("APKdownload","apk下载完成");
                            // 发送 一个无序广播
                            Intent intent = new Intent(ACTION);
                            //intent.putExtra(DOWNLOAD_APK_DIC, apkFile);
                            appContext.sendBroadcast(intent);
                            Message msg1 = new Message();
                            msg1.what = 0;
                            handler.sendMessage(msg1);
                            break;
                        }
                        fos.write(buf, 0, numRead);
                        // 当点击取消时，则停止下载
                    } while (!isInterceptDownload);

                } catch (MalformedURLException e) {
                    isInterceptDownload = true;
                    e.printStackTrace();
                    Message msg = new Message();
                    msg.what = 2;
                    msg.obj = e;
                    handler.sendMessage(msg);
                } catch (IOException e) {
                    isInterceptDownload = true;
                    e.printStackTrace();
                    Message msg = new Message();
                    msg.obj = e;
                    msg.what = 2;
                    handler.sendMessage(msg);
                } catch(Exception e){
                    isInterceptDownload = true;
                    Message msg = new Message();
                    msg.obj = e;
                    msg.what = 2;
                    handler.sendMessage(msg);
                } finally {
                    try {
                        if (is != null) {
                            is.close();
                        }
                        if (fos != null) {
                            fos.close();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }
                // 取消了升级，要删除文件
                if (isInterceptDownload == true) {
                    if(!TextUtils.isEmpty(apkFile)){
                        File file = new File(apkFile);
                        if(file != null && file.exists()){
                            file.delete();
                        }
                    }

                }
            }
        });

    }

    public void stopDownLoad() {
        LogCat.e("APKdownload","主动中断了apk的下载");
        isInterceptDownload = true;
    }

    public interface OnDownLoadProgressListener {
        void onDownLoadProgress(int progress, String fileName);
    }

    public void setOnDownLoadProgressListener(OnDownLoadProgressListener onDownLoadProgressListener) {
        this.onDownLoadProgressListener = onDownLoadProgressListener;
    }

    public interface OnDownLoadFinishListener {
        void onDownLoadFinish(String fileName);
    }

    public void setOnDownLoadFinishListener(OnDownLoadFinishListener onDownLoadFinishListener) {
        this.onDownLoadFinishListener = onDownLoadFinishListener;
    }

    public interface OnDownLoadErrorListener {
        void onDownLoadFinish(Exception e);
    }

    public void setOnDownLoadErrorListener(OnDownLoadErrorListener onDownLoadErrorListener) {
        this.onDownLoadErrorListener = onDownLoadErrorListener;
    }


}
