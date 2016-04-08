package com.download.tools;

import android.os.Environment;

/**
 * Created by fq_mbp on 16/4/8.
 */
public class ToolUtils {

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
}
