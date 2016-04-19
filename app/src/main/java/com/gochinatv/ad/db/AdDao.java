package com.gochinatv.ad.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.httputils.http.response.AdDetailResponse;

/**
 * Created by fq_mbp on 15/12/24.
 */
public class AdDao implements IDBConstants , DaoOperationInterface{

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
        if (dao == null){
            synchronized (AdDao.class){
                if (dao == null) {
                    dao = new AdDao(context);
                }
            }
        }
        return dao;
    }

    private SQLiteDatabase getConnection() {
        SQLiteDatabase sqLiteDatabase = null;
        try {
            sqLiteDatabase = new IDBHelper(context).getWritableDatabase();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return sqLiteDatabase;
    }

    @Override
    public synchronized boolean insert(AdDetailResponse adDetailResponse) {
        if(adDetailResponse == null){
            return false;
        }
        boolean temp = false;
        SQLiteDatabase database = getConnection();
        // 先查看是否存在当前的视频记录

        Cursor cursor = null;
        boolean flag = false;
        try {
            database.beginTransaction();
            cursor = database.rawQuery(SQL_QUERY_ID, new String[]{String.valueOf(adDetailResponse.adVideoId)});
            if(cursor != null && cursor.moveToNext()){
                flag = true;
            }
            database.setTransactionSuccessful();
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            if(database != null){
                database.endTransaction();
            }
            if(cursor != null){
                cursor.close();
            }
        }
        // 如果存在，直接返回插入成功
        if(flag){
            return true;
        }
        if(database == null){
            return false;
        }

        database.beginTransaction();
        try {
            Object[] bindArgs = {adDetailResponse.adVideoId, adDetailResponse.adVideoName,
                    adDetailResponse.adVideoUrl, adDetailResponse.videoPath,
                    adDetailResponse.adVideoIndex, adDetailResponse.adVideoLength, adDetailResponse.adVideoId};
            database.execSQL(SQL_INSERT, bindArgs);
            database.setTransactionSuccessful();
            temp = true;
        } catch (Exception e) {
            e.printStackTrace();
            temp = false;
        } finally {
            if(database != null){
                database.endTransaction();
            }
            if (null != database) {
                database.close();
            }
        }
        return temp;
    }

    @Override
    public synchronized boolean delete(int id) {
        SQLiteDatabase database = null;
        boolean flag = false;
        try {
            database = getConnection();
            database.beginTransaction();
            int temp =  database.delete(DBBASE_TABLE_NAME, "adVideoId = ?", new String[]{String.valueOf(id)});
            if(temp == 0){
                flag = true;
            }
            database.setTransactionSuccessful();
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            if(database != null){
                database.endTransaction();
            }
            if (null != database) {
                database.close();
            }
        }
        return flag;
    }

    @Override
    public synchronized boolean query(int id) {
        SQLiteDatabase database = null;
        Cursor cursor = null;
        boolean flag = false;
        try {
            database = getConnection();
            database.beginTransaction();
            cursor = database.rawQuery(SQL_QUERY_ID, new String[]{String.valueOf(id)});
            if(cursor != null && cursor.moveToNext()){
                flag = true;
            }
            database.setTransactionSuccessful();
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            if(database != null){
                database.endTransaction();
            }
            if(cursor != null){
                cursor.close();
            }

            if (null != database) {
                database.close();
            }
        }
        return flag;
    }

    @Override
    public synchronized boolean update(int id, long length) {
        SQLiteDatabase database = null;
        boolean flag = false;
        try {
            database = getConnection();
            Cursor cursor = null;
            try {
                database.beginTransaction();
                cursor = database.rawQuery(SQL_QUERY_ID, new String[]{String.valueOf(id)});
                if(cursor != null && cursor.moveToNext()){
                    flag = true;
                }
                database.setTransactionSuccessful();
            }catch (Exception e){
                e.printStackTrace();
            }finally {
                if(database != null){
                    database.endTransaction();
                }
                if(cursor != null){
                    cursor.close();
                }
            }
            // 如果存在，直接返回插入成功
            if(!flag){
                return false;
            }
            if(database == null){
                return false;
            }


            database.beginTransaction();
            ContentValues updatedValues = new ContentValues();
            updatedValues.put("adVideoLength", length);
            int temp = database.update(DBBASE_TABLE_NAME, updatedValues, "adVideoId = ?", new String[]{String.valueOf(id)});
            if(temp == 0){
                flag = true;
            }
            database.setTransactionSuccessful();
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            if(database != null){
                database.endTransaction();
            }

            if (null != database) {
                database.close();
            }
        }
        return flag;
    }



}
