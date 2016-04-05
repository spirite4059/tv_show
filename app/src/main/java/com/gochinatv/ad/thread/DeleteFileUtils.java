package com.gochinatv.ad.thread;

import android.text.TextUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by fq_mbp on 16/3/17.
 */
public class DeleteFileUtils {

    private static DeleteFileUtils instance;

    // 线程池
    private static ExecutorService executorService;

    private static final int MAX_THREAD_NUMBER = 5;

    private DeleteFileUtils() {
    }

    public static DeleteFileUtils getInstance() {
        if (instance == null) {
            instance = new DeleteFileUtils();
            executorService = Executors.newFixedThreadPool(MAX_THREAD_NUMBER);
        }

        return instance;
    }


    /**
     * 删除文件或文件夹
     *
     * @param filePath
     * @return true：文件成功删除或不存在，反正文件删除失败
     */
    public void deleteFile(final String filePath) {
        if (TextUtils.isEmpty(filePath)) {
            return;
        }
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                File file = new File(filePath);
                if(!file.exists()){
                    return;
                }
                // 如果是目录全删
                deleteDir(file);
            }
        });
    }

    /**
     * 删除多个文件
     * @param filePaths
     * @return true：文件成功删除或不存在，反正文件删除失败
     */
    public void deleteFiles(final ArrayList<String> filePaths) {
        if(filePaths == null || filePaths.size() == 0){
            return;
        }
        for(String filePath : filePaths){
            deleteFile(filePath);
        }
    }


    /**
     * 递归删除目录下的所有文件及子目录下所有文件
     *
     * @param dir 将要删除的文件目录
     * @return boolean Returns "true" if all deletions were successful.
     * If a deletion fails, the method stops attempting to
     * delete and returns "false".
     */
    public boolean deleteDir(File dir) {
        if (dir.exists() && dir.isDirectory()) {
            String[] children = dir.list();
            //递归删除目录中的子目录下
            for (int i = 0; i < children.length; i++) {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
        }
        // 目录此时为空，可以删除
        return dir.delete();
    }


}
