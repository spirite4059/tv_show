package com.gochinatv.ad.screenshot;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.download.tools.LogCat;
import com.gochinatv.ad.tools.DataUtils;

import java.io.File;

/**
 * Created by fq_mbp on 16/5/13.
 */
public class SystemScreenShotPolicy implements VideoGrab{


    @Override
    public Bitmap getVideoGrab(File fileVideo, long duration, int width, int height) {
        Bitmap bitmap = null;
        try {
            String path = takeScreenShot();
            Thread.sleep(10 * 1000);
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = 4; /*图片长宽方向缩小倍数 另外，为了节约内存我们还可以使用下面的几个字段：*/
            options.inDither=false;    /*不进行图片抖动处理*/
            bitmap = BitmapFactory.decodeFile(path, options);
        }catch (Exception e){
            e.printStackTrace();
            LogCat.e("screenShot", "系统截图方案出错............");
        }
        return bitmap;
    }

    @Override
    public String getFileName() {
        String fileName = "screenShot_" + fileNameTime + ".png";
        return fileName;
    }

    @Override
    public boolean isNeedCacheImageLocal() {
        return false;
    }

    private String fileNameTime;
    private String takeScreenShot() {
        File fileDir = new File(DataUtils.getScreenShotDirectory());
        if(!fileDir.exists()){
            fileDir.mkdirs();
            LogCat.e(DataUtils.getScreenShotDirectory() + "路径不存在，需要创建.........");
        }
        fileNameTime =  DataUtils.getFormatTime(System.currentTimeMillis(), "yyyyMMddhhmmss");
        String mSavedPath = DataUtils.getScreenShotDirectory() + "screenShot_" + fileNameTime + ".png";
        try {
            Runtime.getRuntime().exec("screencap -p " + mSavedPath);
        } catch (Exception e) {
            e.printStackTrace();
            LogCat.e("screenShot", "系统截图过程中出现异常.............");
        }
        return mSavedPath;
    }
}
