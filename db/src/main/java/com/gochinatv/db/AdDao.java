package com.gochinatv.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.okhtttp.response.AdDetailResponse;

import java.util.ArrayList;

/**
 * Created by fq_mbp on 15/12/24.
 */
public class AdDao implements IDBConstants, DaoOperationInterface {

    private static AdDao dao;
    private Context context;

    /**
     * 使用单例和同步来操作数据库
     */
    private AdDao(Context context) {
        this.context = context;
    }

    public static AdDao getInstance(Context context) {
        if (dao == null) {
            synchronized (AdDao.class) {
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
    public synchronized boolean insert(boolean isToday, AdDetailResponse adDetailResponse) {
        if (adDetailResponse == null) {
            return false;
        }
        boolean temp = false;
        SQLiteDatabase database = getConnection();
        // 先查看是否存在当前的视频记录

        Cursor cursor = null;
        boolean flag = false;
        try {
            database.beginTransaction();
            cursor = database.rawQuery((isToday ? SQL_QUERY_ID : SQL_QUERY_ID_TM), new String[]{String.valueOf(adDetailResponse.adVideoId)});
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
        // 如果存在，直接返回插入成功
        if (flag) {
            return true;
        }
        if (database == null) {
            return false;
        }

        database.beginTransaction();
//        String SQL_INSERT = "insert into " + DBBASE_TD_VIDEOS_TABLE_NAME + " (" + adVideoId
//                + ", " + adVideoName + ", " + adVideoUrl + ", "
//                + videoPath + ", " + adVideoIndex + ", "
//                + adVideoLength +  ") values (?,?,?,?,?,?,?)";
        try {

            ContentValues contentValues = new ContentValues();
            contentValues.put("adVideoId", adDetailResponse.adVideoId);
            contentValues.put("adVideoName", adDetailResponse.adVideoName);
            contentValues.put("adVideoUrl", adDetailResponse.adVideoUrl);
            contentValues.put("videoPath", adDetailResponse.videoPath);
            contentValues.put("adVideoIndex", adDetailResponse.adVideoIndex);
            contentValues.put("adVideoLength", adDetailResponse.adVideoLength);

            database.insert((isToday ? DBBASE_TD_VIDEOS_TABLE_NAME : DBBASE_TM_VIDEOS_TABLE_NAME), null, contentValues);
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
        return temp;
    }

    @Override
    public void insertAll(boolean isToday, ArrayList<AdDetailResponse> adDetailResponses) {
        if (adDetailResponses == null || adDetailResponses.size() == 0) {
            return;
        }
        SQLiteDatabase database = getConnection();
        if (database == null) {
            return;
        }
        Cursor cursor = null;
        boolean flag = false;
        try {
            // 先查看是否存在当前的视频记录
            for (AdDetailResponse adDetailResponse : adDetailResponses) {
                try {
                    database.beginTransaction();
                    cursor = database.rawQuery((isToday ? SQL_QUERY_ID : SQL_QUERY_ID_TM), new String[]{String.valueOf(adDetailResponse.adVideoId)});
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
                // 如果存在，直接返回插入成功
                if (flag) {
                    continue;
                }
                if (database == null) {
                    break;
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

                    database.insert((isToday ? DBBASE_TD_VIDEOS_TABLE_NAME : DBBASE_TM_VIDEOS_TABLE_NAME), null, contentValues);
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
        }finally {
            if (null != database) {
                database.close();
            }

        }
    }

    @Override
    public synchronized boolean delete(boolean isToday, int id) {
        SQLiteDatabase database = null;
        boolean flag = false;
        try {
            database = getConnection();
            database.beginTransaction();
            int temp = database.delete((isToday ? DBBASE_TD_VIDEOS_TABLE_NAME : DBBASE_TM_VIDEOS_TABLE_NAME), "adVideoId = ?", new String[]{String.valueOf(id)});
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

    @Override
    public synchronized boolean query(boolean isToday, int id) {
        SQLiteDatabase database = null;
        Cursor cursor = null;
        boolean flag = false;
        try {

            database = getConnection();
            database.beginTransaction();
            cursor = database.rawQuery((isToday ? SQL_QUERY_ID : SQL_QUERY_ID_TM), new String[]{String.valueOf(id)});
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

            if (null != database) {
                database.close();
            }
        }
        return flag;
    }

    public synchronized AdDetailResponse queryDetail(boolean isToday, int id) {
        SQLiteDatabase database = null;
        Cursor cursor = null;
        AdDetailResponse adDetailResponse = null;
        try {
            database = getConnection();
            database.beginTransaction();
            cursor = database.rawQuery((isToday ? SQL_QUERY_ID : SQL_QUERY_ID_TM), new String[]{String.valueOf(id)});
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

    // 创建数据表
//    String SQL_CREATE_TODAY_VIDEO_LIST = "CREATE TABLE IF NOT EXISTS " + DBBASE_TD_VIDEOS_TABLE_NAME + " ("
//            + "_id INTEGER PRIMARY KEY AUTOINCREMENT, "
//            + adVideoId + " int, "
//            + adVideoName + " VARCHAR, "
//            + adVideoUrl + " VARCHAR, "
//            + videoPath + " VARCHAR, "
//            + adVideoIndex + " int, "
//            + adVideoLength + " long"
//            + ")";
    @Override
    public ArrayList<AdDetailResponse> queryAll(boolean isToday) {
        ArrayList<AdDetailResponse> adDetailResponses = null;
        SQLiteDatabase database = null;
        Cursor cursor = null;
        try {
            adDetailResponses = new ArrayList<>();
            database = getConnection();
            database.beginTransaction();
            cursor = database.rawQuery((isToday ? SQL_QUERY : SQL_QUERY_TM), null);
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

    @Override
    public synchronized boolean update(boolean isToday, String fileName, String column, String value) {
        SQLiteDatabase database = null;
        boolean flag = false;
        try {
            database = getConnection();
            Cursor cursor = null;
            try {
                database.beginTransaction();
                cursor = database.rawQuery((isToday ? SQL_QUERY_NAME : SQL_QUERY_NAME_TM), new String[]{fileName});
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
            // 如果存在，直接返回插入成功
            if (!flag) {
                return false;
            }
            if (database == null) {
                return false;
            }


            database.beginTransaction();
            ContentValues updatedValues = new ContentValues();
            updatedValues.put(column, value);
            int temp = database.update((isToday ? DBBASE_TD_VIDEOS_TABLE_NAME : DBBASE_TM_VIDEOS_TABLE_NAME), updatedValues, adVideoName + " = ?", new String[]{fileName});
            if (temp == 0) {
                flag = true;
            }
            database.setTransactionSuccessful();
            database.endTransaction();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {

            if (null != database) {
                database.close();
            }
        }
        return flag;
    }



    public synchronized boolean update(boolean isToday, int vid, String column, String value) {
        SQLiteDatabase database = null;
        boolean flag = false;
        try {
            database = getConnection();
            Cursor cursor = null;
            try {
                database.beginTransaction();
                cursor = database.rawQuery((isToday ? SQL_QUERY_ID : SQL_QUERY_ID_TM), new String[]{String.valueOf(vid)});
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
            // 如果存在，直接返回插入成功
            if (!flag) {
                return false;
            }
            if (database == null) {
                return false;
            }


            database.beginTransaction();
            ContentValues updatedValues = new ContentValues();
            updatedValues.put(column, value);
            int temp = database.update((isToday ? DBBASE_TD_VIDEOS_TABLE_NAME : DBBASE_TM_VIDEOS_TABLE_NAME), updatedValues, adVideoId + " = ?", new String[]{String.valueOf(vid)});
            if (temp == 0) {
                flag = true;
            }
            database.setTransactionSuccessful();
            database.endTransaction();
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
