package com.retrofit.tools;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.util.Log;

import com.retrofit.download.db.DLDao;
import com.retrofit.download.db.DownloadInfo;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;

/**
 * Created by fq_mbp on 16/7/21.
 */

public class Tools {


    public static String getThrowableString(Throwable ex){
        StringBuffer sb = new StringBuffer();
        Writer writer = new StringWriter();
        PrintWriter printWriter = new PrintWriter(writer);
        ex.printStackTrace(printWriter);
        Throwable cause = ex.getCause();
        while (cause != null) {
            cause.printStackTrace(printWriter);
            cause = cause.getCause();
        }
        printWriter.close();
        String result = writer.toString();
        sb.append(result); //将写入的结果
        return sb.toString();
    }

    /**
     * 检测磁盘空间
     * @param fileSize
     * @return
     */
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    public static boolean isSdCardHasSpace(long fileSize) {
        File path = Environment.getExternalStorageDirectory();
        StatFs statFs = new StatFs(path.getPath());
        long blockSizeLong = 0;
        long availableBlocksLong = 0;
        if (Build.VERSION.SDK_INT >= 18) {
            blockSizeLong = statFs.getBlockSizeLong();
            availableBlocksLong = statFs.getAvailableBlocksLong();
        } else {
            blockSizeLong = statFs.getBlockSizeLong();
            availableBlocksLong = statFs.getAvailableBlocksLong();
        }

        //计算SD卡的空间大小
        long availableSize = availableBlocksLong * blockSizeLong;
        long preSpace = 10 * 1024 * 1024; // 10M的预留空间
        if (availableSize < (fileSize + preSpace)) {
            // sdcard空间不足，无法下载当前视频
            return false;
        }
        return true;
    }


    public static File creatFile(String filePath, String fileName) throws IOException {
        File file = new File(filePath);
        if(!file.exists()){
            file.mkdirs();
        }
        File fileVideo = new File(file, fileName);
        if(!fileVideo.exists()){
            fileVideo.createNewFile();
        }
        return fileVideo;
    }

    public static boolean isFileExists(String fileName, String filePath){
        File file = new File(filePath + fileName + ".mp4");
        return file.exists();
    }





    /**
     * 判断是否存在sdcard
     *
     * @return
     */
    public static boolean isExistSDCard() {
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            return true;
        } else
            return false;
    }


    public static String checkDownloadInfo(Context context, String fileName, String filePath) {
        String range = null;
        DownloadInfo downloadInfo = DLDao.query(context);
        if (downloadInfo != null) {

            if (downloadInfo.tname.equals(fileName)) {  // 下载文件跟sql表是一个文件
                // 有数据记录,继续查找文件, 如果文件不存在或者文件大小不等于已经下载的文件大小,都要重新下载
                File file = new File(filePath + File.separator + fileName + ".mp4");
                if (file.exists()) {
                    if (file.length() - 1 == downloadInfo.startPos) { // 表示文件正确
                        // 从记录位置回复下载
                        range = "bytes=" + downloadInfo.startPos + "-" + downloadInfo.endPos;
                        Log.e("retrofit_dl", "从下载记录中回复....." + range);
                    } else {
                        // 删除记录
                        // 文件出错, 重新下载
                        DLDao.delete(context);
                    }
                } else {
                    // 删除记录
                    // 文件出错, 重新下载
                    Log.e("retrofit_dl", "下载文件不存在,删除文件sql记录.....");
                    DLDao.delete(context);
                }
            } else { // 如果不是一个文件,直接删除文件和记录
                // 删除记录
                DLDao.delete(context);
                // 删除文件
                File file = new File(filePath + File.separator + downloadInfo.tname + ".mp4");
                if (file.exists()) {
                    file.delete();
                }
            }
        } else {
            Log.e("retrofit_dl", "没有下载记录.....");
            // 以防万一,删除记录
            DLDao.delete(context);
        }
        return range;
    }


    public static String getRealPath(String url){
        url = url.replace(Constants.BASE_URL + "/", "");
        return url;
    }


}
