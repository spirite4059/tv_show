package com.gochinatv.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

import com.okhtttp.response.AdDetailResponse;

import java.util.ArrayList;

/**
 * Created by fq_mbp on 15/12/24.
 */
public class AdDao implements IDBConstants {


    private static SQLiteDatabase getConnection(Context context) {
        SQLiteDatabase sqLiteDatabase = null;
        try {
            sqLiteDatabase = new IDBHelper(context).getWritableDatabase();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return sqLiteDatabase;
    }


    /**
     * 查询操作
     * @param context
     * @param id
     * @return
     */
    private static synchronized boolean queryById(Context context, SQLiteDatabase database, String tableName, int id) {
        if(context == null || database == null || id <= 0){
            return false;
        }
        Cursor cursor = null;
        boolean flag = false;
        try {
            database.beginTransaction();
            String sql = "select * from " + tableName + " where " + adVideoId + " = ?";
            cursor = database.rawQuery(sql, new String[]{String.valueOf(id)});
            if (cursor != null && cursor.moveToNext()) {
                flag = true;
            }
            database.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (database != null) {
                database.endTransaction();
            }
            if (cursor != null) {
                cursor.close();
            }
        }
        return flag;
    }

    /**
     * 查询操作
     * @param context
     * @param name
     * @return
     */
    private static synchronized boolean queryByName(Context context, SQLiteDatabase database, String tableName, String name) {
        if(context == null || database == null || TextUtils.isEmpty(name)){
            return false;
        }
        Cursor cursor = null;
        boolean flag = false;
        try {
            database.beginTransaction();
            String sql = "select * from " + tableName + " where " + adVideoName + " = ?";
            cursor = database.rawQuery(sql, new String[]{name});
            if (cursor != null && cursor.moveToNext()) {
                flag = true;
            }
            database.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (database != null) {
                database.endTransaction();
            }
            if (cursor != null) {
                cursor.close();
            }
        }
        return flag;
    }


    public static synchronized boolean insert(Context context, String tableName, AdDetailResponse adDetailResponse) {
        if (adDetailResponse == null) {
            return false;
        }
        boolean temp = false;
        SQLiteDatabase database = getConnection(context);

        // 先查看是否存在当前的视频记录
        boolean flag = queryByName(context, database, tableName, adDetailResponse.adVideoName);

        // 如果存在，直接返回插入成功
        if (!flag && database != null) {
            try {
                database.beginTransaction();
                ContentValues contentValues = new ContentValues();
                contentValues.put("adVideoId", adDetailResponse.adVideoId);
                contentValues.put("adVideoName", adDetailResponse.adVideoName);
                contentValues.put("adVideoUrl", adDetailResponse.adVideoUrl);
                contentValues.put("videoPath", adDetailResponse.videoPath);
                contentValues.put("adVideoIndex", adDetailResponse.adVideoIndex);
                contentValues.put("adVideoLength", adDetailResponse.adVideoLength);

                database.insert(tableName, null, contentValues);
                database.setTransactionSuccessful();
                temp = true;
            } catch (Exception e) {
                e.printStackTrace();
                temp = false;
            } finally {
                if (database != null) {
                    database.endTransaction();
                }
                if (null != database) {
                    database.close();
                }
            }
        }
        return temp;
    }

    public static void insertAll(Context context, String tableName, ArrayList<AdDetailResponse> adDetailResponses) {
        if (adDetailResponses == null || adDetailResponses.size() == 0) {
            return;
        }
        SQLiteDatabase database = getConnection(context);
        if (database == null) {
            return;
        }
        try {
            // 先查看是否存在当前的视频记录
            for (AdDetailResponse adDetailResponse : adDetailResponses) {

                boolean flag = queryByName(context, database, tableName, adDetailResponse.adVideoName);
                if(flag){
                    continue;
                }
                database.beginTransaction();
                try {
                    ContentValues contentValues = new ContentValues();
                    contentValues.put("adVideoId", adDetailResponse.adVideoId);
                    contentValues.put("adVideoName", adDetailResponse.adVideoName);
                    contentValues.put("adVideoUrl", adDetailResponse.adVideoUrl);
                    contentValues.put("videoPath", adDetailResponse.videoPath);
                    contentValues.put("adVideoIndex", adDetailResponse.adVideoIndex);
                    contentValues.put("adVideoLength", adDetailResponse.adVideoLength);

                    database.insert(tableName, null, contentValues);
                    database.setTransactionSuccessful();
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    if (database != null) {
                        database.endTransaction();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (null != database) {
                database.close();
            }

        }
    }

    public static synchronized boolean deleteById(Context context, String tableName, int id) {
        SQLiteDatabase database = null;
        boolean flag = false;
        try {
            database = getConnection(context);
            database.beginTransaction();
            int temp = database.delete(tableName, "adVideoId = ?", new String[]{String.valueOf(id)});
            if (temp == 0) {
                flag = true;
            }
            database.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (database != null) {
                database.endTransaction();
            }
            if (null != database) {
                database.close();
            }
        }
        return flag;
    }

    public static synchronized boolean deleteAll(Context context, String tableName) {
        SQLiteDatabase database = null;
        boolean flag = false;
        try {
            database = getConnection(context);
            database.beginTransaction();
            database.delete(tableName, null, null);
            database.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (null != database) {
                database.endTransaction();
                database.close();
            }
        }
        return flag;
    }


    public static synchronized boolean deleteAll(Context context, String table, ArrayList<AdDetailResponse> adDetailResponses) {
        SQLiteDatabase database = null;
        boolean flag = false;
        try {
            database = getConnection(context);
            database.beginTransaction();
            for(AdDetailResponse adDetailResponse : adDetailResponses){
                 database.delete(table, adVideoId + " = ?", new String[]{String.valueOf(adDetailResponse.adVideoId)});
            }
            database.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (null != database) {
                database.endTransaction();
                database.close();
            }
        }
        return flag;
    }






    public static synchronized AdDetailResponse queryDetailById(Context context, String tableName, int id) {
        return queryDetail(context, tableName, adVideoId, String.valueOf(id));
    }

    public static synchronized AdDetailResponse queryDetail(Context context, String tableName, String column, String value) {
        if(context == null || TextUtils.isEmpty(tableName) || TextUtils.isEmpty(tableName) || TextUtils.isEmpty(value)){
            return null;
        }
        SQLiteDatabase database = null;
        Cursor cursor = null;
        AdDetailResponse adDetailResponse = null;
        try {
            database = getConnection(context);
            database.beginTransaction();

            String sql = "select * from " + tableName + " where " + column + " = ?";

            cursor = database.rawQuery(sql, new String[]{String.valueOf(value)});
            if (cursor != null && cursor.moveToFirst()) {
                adDetailResponse = new AdDetailResponse();
                adDetailResponse.adVideoId = cursor.getInt(1);
                adDetailResponse.adVideoName = cursor.getString(2);
                adDetailResponse.adVideoUrl = cursor.getString(3);
                adDetailResponse.videoPath = cursor.getString(4);
                adDetailResponse.adVideoIndex = cursor.getInt(5);
                adDetailResponse.adVideoLength = cursor.getLong(6);
            }
            database.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (database != null) {
                database.endTransaction();
            }
            if (cursor != null) {
                cursor.close();
            }

            if (null != database) {
                database.close();
            }
        }
        return adDetailResponse;
    }


    public static ArrayList<AdDetailResponse> queryAll(Context context, String tableName) {
        ArrayList<AdDetailResponse> adDetailResponses = null;
        SQLiteDatabase database = null;
        Cursor cursor = null;
        try {
            adDetailResponses = new ArrayList<>();
            database = getConnection(context);
            database.beginTransaction();

            String sql = "select * from " + tableName;
            cursor = database.rawQuery(sql, null);
            while (cursor.moveToNext()) {
                AdDetailResponse adDetailResponse = new AdDetailResponse();
//                LogCat.e("adVideoId: " + cursor.getInt(1));
//                LogCat.e("adVideoName: " + cursor.getString(2));
//                LogCat.e("adVideoUrl: " + cursor.getString(3));
//                LogCat.e("videoPath: " + cursor.getString(4));
//                LogCat.e("adVideoIndex: " + cursor.getInt(5));
//                LogCat.e("adVideoLength: " + cursor.getLong(6));
//                LogCat.e("=======================");
                adDetailResponse.adVideoId = cursor.getInt(1);
                adDetailResponse.adVideoName = cursor.getString(2);
                adDetailResponse.adVideoUrl = cursor.getString(3);
                adDetailResponse.videoPath = cursor.getString(4);
                adDetailResponse.adVideoIndex = cursor.getInt(5);
                adDetailResponse.adVideoLength = cursor.getLong(6);
                adDetailResponses.add(adDetailResponse);
            }
            database.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (database != null) {
                database.endTransaction();
            }
            if (cursor != null) {
                cursor.close();
            }

            if (null != database) {
                database.close();
            }
        }
        return adDetailResponses;
    }



    public static synchronized boolean update(Context context, String tableName, int vid, String column, String colunmValue) {
        SQLiteDatabase database = null;
        boolean flag = false;
        try {
            database = getConnection(context);
            flag = queryById(context, database, tableName, vid);
            // 如果存在，直接返回插入成功
            if (flag) {
                database.beginTransaction();
                ContentValues updatedValues = new ContentValues();
                updatedValues.put(column, colunmValue);
                int temp = database.update(tableName, updatedValues, adVideoId + " = ?", new String[]{String.valueOf(vid)});
                if (temp == 0) {
                    flag = true;
                }
                database.setTransactionSuccessful();
                database.endTransaction();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {

            if (null != database) {
                database.close();
            }
        }
        return flag;
    }

    public static synchronized boolean update(Context context, String tableName, String fileName, String column, String colunmValue) {
        SQLiteDatabase database = null;
        boolean flag = false;
        try {
            database = getConnection(context);
            flag = queryByName(context, database, tableName, fileName);
            // 如果存在，直接返回插入成功
            if (flag) {
                database.beginTransaction();
                ContentValues updatedValues = new ContentValues();
                updatedValues.put(column, colunmValue);
                int temp = database.update(tableName, updatedValues, adVideoName + " = ?", new String[]{fileName});
                if (temp == 0) {
                    flag = true;
                }
                database.setTransactionSuccessful();
                database.endTransaction();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {

            if (null != database) {
                database.close();
            }
        }
        return flag;
    }


}
