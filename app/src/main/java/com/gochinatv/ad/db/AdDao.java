package com.gochinatv.ad.db;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.gochinatv.ad.tools.LogCat;

import java.util.ArrayList;

/**
 * Created by fq_mbp on 15/12/24.
 */
public class AdDao implements DBConstants {

    private static AdDao dao;
    private Context context;


    public static final String FLAG_DOWNLOAD_UNFINISH = "0";

    public static final String FLAG_DOWNLOAD_FINISHED = "1";

    /**
     * 使用单例和同步来操作数据库
     */
    private AdDao(Context context) {
        this.context = context;
    }

    public static AdDao getInstance(Context context) {
        if (dao == null) {
            dao = new AdDao(context);
        }
        return dao;
    }

    private SQLiteDatabase getConnection() {
        SQLiteDatabase sqLiteDatabase = null;
        try {
            sqLiteDatabase = new DBHelper(context).getWritableDatabase();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return sqLiteDatabase;
    }

    /**
     * 插入广告视频
     *
     * @param videoAdBeans
     */
    public synchronized void insertAd(ArrayList<VideoAdBean> videoAdBeans) {
        SQLiteDatabase database = getConnection();
        database.beginTransaction();
        try {
            for (VideoAdBean videoAdBean : videoAdBeans) {
                Object[] bindArgs = {videoAdBean.videoId, videoAdBean.videoName,
                        videoAdBean.videoIndex, videoAdBean.videoStartTime,
                        videoAdBean.videoEndTime, videoAdBean.videoPath, videoAdBean.videoUrl, videoAdBean.isDownloadFinish};
                database.execSQL(SQL_INSERT, bindArgs);
            }
            database.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            database.endTransaction();
            if (null != database) {
                database.close();
            }
        }

    }


    public synchronized boolean isAdded(String vid) {
        SQLiteDatabase database = getConnection();
        database.beginTransaction();
        Cursor cursor = null;
        boolean isAdded = false;
        try {

            String sql = "select * from " + DBBASE_TABLE_NAME + " where " + video_id +" = ?";

            cursor = database.rawQuery(sql, new String[]{vid});
            if(cursor.moveToNext()){
                isAdded = true;
            }
            database.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            database.endTransaction();
            if (null != database) {
                database.close();
            }

            if (null != cursor) {
                cursor.close();
            }
        }
        return isAdded;
    }


    public synchronized void insertAd(VideoAdBean videoAdBean) {
        SQLiteDatabase database = getConnection();
        database.beginTransaction();
        try {
            Object[] bindArgs = {videoAdBean.videoId, videoAdBean.videoName,
                    videoAdBean.videoIndex, videoAdBean.videoStartTime,
                    videoAdBean.videoEndTime, videoAdBean.videoPath, videoAdBean.videoUrl, videoAdBean.isDownloadFinish};
            database.execSQL(SQL_INSERT, bindArgs);
            database.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            database.endTransaction();
            if (null != database) {
                database.close();
            }
        }

    }

    public synchronized void delete(String videoId) {
        SQLiteDatabase database = getConnection();
        database.beginTransaction();
        try {
            database.delete(DBBASE_TABLE_NAME, video_id + " = ?", new String[]{videoId});
            database.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            database.endTransaction();
            if (null != database) {
                database.close();
            }
        }
    }


    public synchronized ArrayList<VideoAdBean> queryVideoAds() {
        ArrayList<VideoAdBean> videoAdBeans = new ArrayList<>();
        SQLiteDatabase database = getConnection();
        database.beginTransaction();
        Cursor cursor = null;
        try {
            cursor = database.rawQuery(SQL_QUERY, null);
            int count = cursor.getCount();
            if (count > 0) {
                videoAdBeans = new ArrayList<>(count);
                while (cursor.moveToNext()) {
                    VideoAdBean videoAdBean = new VideoAdBean();
                    videoAdBean.videoId = cursor.getString(1);
                    videoAdBean.videoName = cursor.getString(2);
                    videoAdBean.videoIndex = cursor.getString(3);
                    videoAdBean.videoStartTime = cursor.getString(4);
                    videoAdBean.videoEndTime = cursor.getString(5);
                    videoAdBean.videoPath = cursor.getString(6);
                    videoAdBean.videoUrl = cursor.getString(7);
                    videoAdBean.isDownloadFinish = cursor.getString(8);
                    videoAdBeans.add(videoAdBean);
                }
            }

            database.setTransactionSuccessful();

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            database.endTransaction();
            if (null != database) {
                database.close();
            }

            if (null != cursor) {
                cursor.close();
            }
        }
        return videoAdBeans;
    }

    public synchronized void updateVideoAds(VideoAdBean videoAdBean) {
        SQLiteDatabase database = getConnection();
        database.beginTransaction();
        try {

            String sql = "update " + DBBASE_TABLE_NAME + " set "
                    + video_id  + " =?, "
                    + video_name  + " =?, "
                    + video_index  + " =?, "
                    + video_start_time  + " =?, "
                    + video_end_time  + " =?, "
                    + video_path  + " =?, "
                    + video_url  + " =?, "
                    + video_downloaded  + " =? "
                    + " where " + video_id + " = ?";
            Object[] bingArgs = {videoAdBean.videoId, videoAdBean.videoName, videoAdBean.videoIndex, videoAdBean.videoStartTime, videoAdBean.videoEndTime, videoAdBean.videoPath, videoAdBean.videoUrl, videoAdBean.isDownloadFinish};
            database.execSQL(sql, bingArgs);
            database.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            database.endTransaction();
            if (null != database) {
                database.close();
            }
        }
    }


    private void d() {
        SQLiteDatabase database = getConnection();
        database.beginTransaction();
        try {


            database.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (null != database) {
                database.close();
            }
        }
    }

}
