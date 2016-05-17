package com.download.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.download.tools.LogCat;

import java.util.ArrayList;

/**
 * Created by fq_mbp on 16/4/26.
 */
public class DLDao implements IDBConstants {

    public synchronized static SQLiteDatabase getConnection(Context context) {
        SQLiteDatabase sqLiteDatabase = null;
        try {
            sqLiteDatabase = new IDBHelper(context).getWritableDatabase();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return sqLiteDatabase;
    }


    /**
     * 插入数据
     *
     * @param context
     * @param downloadInfo
     * @return
     */
    public static synchronized boolean insert(Context context, DownloadInfo downloadInfo) {
        if (downloadInfo == null) {
            return false;
        }

        boolean temp = false;
        SQLiteDatabase database = getConnection(context);
        // 先查看是否存在当前的视频记录
        if (database != null) {
            try {
                database.beginTransaction();

                ContentValues contentValues = new ContentValues();
                contentValues.put(tid, downloadInfo.tid);
                contentValues.put(tname, downloadInfo.tname);
                contentValues.put(turl, downloadInfo.turl);
                contentValues.put(tlength, downloadInfo.tlength);
                contentValues.put(startPos, downloadInfo.startPos);
                contentValues.put(endPos, downloadInfo.endPos);

                database.insert(DBBASE_DOWNLOAD_TABLE_NAME, null, contentValues);
                database.setTransactionSuccessful();
                temp = true;
                LogCat.e("sql写入成功......");
            } catch (Exception e) {
                e.printStackTrace();
                LogCat.e("sql写入失败......");
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

    private synchronized static boolean query(String url, SQLiteDatabase database) {
        Cursor cursor = null;
        boolean flag = false;
        try {
            database.beginTransaction();
            cursor = database.rawQuery(SQL_QUERY_DOWNLOAD_BY_URL, new String[]{url});
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

    private  synchronized static boolean query(int tid, SQLiteDatabase database) {
        Cursor cursor = null;
        boolean flag = false;
        try {
            database.beginTransaction();
            cursor = database.rawQuery(SQL_QUERY_DOWNLOAD_BY_ID, new String[]{String.valueOf(tid)});
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

    public synchronized static ArrayList<DownloadInfo> queryAll(Context context, String fileName) {
        Cursor cursor = null;
        ArrayList<DownloadInfo> arrayList = null;
        SQLiteDatabase database = getConnection(context);
        try {
            database.beginTransaction();
            cursor = database.rawQuery(SQL_QUERY_DOWNLOAD_BY_NAME, new String[]{fileName});
            if (cursor != null) {
                arrayList = new ArrayList<>(cursor.getCount());

                while (cursor.moveToNext()) {
                    DownloadInfo downloadInfo = new DownloadInfo();
                    downloadInfo.tid = cursor.getInt(1);
                    downloadInfo.tname = cursor.getString(2);
                    downloadInfo.turl = cursor.getString(3);
                    downloadInfo.tlength = cursor.getLong(4);
                    downloadInfo.startPos = cursor.getLong(5);
                    downloadInfo.endPos = cursor.getLong(6);
                    arrayList.add(downloadInfo);
                }
            }
            database.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (database != null) {
                database.endTransaction();
                database.close();
            }
            if (cursor != null) {
                cursor.close();
            }
        }
        return arrayList;
    }

    public synchronized static ArrayList<DownloadInfo> queryAll(Context context) {
        Cursor cursor = null;
        ArrayList<DownloadInfo> arrayList = null;
        SQLiteDatabase database = getConnection(context);
        try {
            database.beginTransaction();
            cursor = database.rawQuery(SQL_QUERY_DOWNLOAD, null);
            if (cursor != null) {
                arrayList = new ArrayList<>(cursor.getCount());

                while (cursor.moveToNext()) {
                    DownloadInfo downloadInfo = new DownloadInfo();
                    downloadInfo.tid = cursor.getInt(1);
                    downloadInfo.tname = cursor.getString(2);
                    downloadInfo.turl = cursor.getString(3);
                    downloadInfo.tlength = cursor.getLong(4);
                    downloadInfo.startPos = cursor.getLong(5);
                    downloadInfo.endPos = cursor.getLong(6);
                    arrayList.add(downloadInfo);
                }
            }
            database.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (database != null) {
                database.endTransaction();
                database.close();
            }
            if (cursor != null) {
                cursor.close();
            }
        }
        return arrayList;
    }

//    public static ArrayList<DownloadInfo> queryByName(Context context, String fileName) {
//        Cursor cursor = null;
//        ArrayList<DownloadInfo> arrayList = null;
//        SQLiteDatabase database = getConnection(context);
//        try {
//            database.beginTransaction();
//            cursor = database.rawQuery(SQL_QUERY_DOWNLOAD_BY_NAME, new String[]{fileName});
//            LogCat.e(DLDao.class, "DLDao -> queryByName........cursor.getCount()........." + cursor.getCount());
//            if (cursor != null) {
//                arrayList = new ArrayList<>(cursor.getCount());
//                while (cursor.moveToNext()) {
//                    DownloadInfo downloadInfo = new DownloadInfo();
//                    downloadInfo.tid = cursor.getInt(1);
//                    downloadInfo.tname = cursor.getString(2);
//                    downloadInfo.turl = cursor.getString(3);
//                    downloadInfo.tlength = cursor.getLong(4);
//                    downloadInfo.startPos = cursor.getLong(5);
//                    downloadInfo.endPos = cursor.getLong(6);
//                    arrayList.add(downloadInfo);
//                }
//            }
//            database.setTransactionSuccessful();
//        } catch (Exception e) {
//            e.printStackTrace();
//        } finally {
//            if (database != null) {
//                database.endTransaction();
//                database.close();
//            }
//            if (cursor != null) {
//                cursor.close();
//            }
//        }
//        return arrayList;
//    }


    public synchronized static boolean queryByName(Context context, String fileName) {
        Cursor cursor = null;
        SQLiteDatabase database = getConnection(context);
        try {
            database.beginTransaction();
            cursor = database.rawQuery(SQL_QUERY_DOWNLOAD_BY_NAME, new String[]{fileName});
            LogCat.e(DLDao.class, "DLDao -> queryByName........cursor.getCount()........." + cursor.getCount());
            if (cursor != null) {
                return cursor.moveToNext();
            }
            database.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (database != null) {
                database.endTransaction();
                database.close();
            }
            if (cursor != null) {
                cursor.close();
            }
        }
        return false;
    }


    public static synchronized boolean update(Context context, String url, long endPos) {
        SQLiteDatabase database = null;
        boolean flag = false;
        try {
            database = getConnection(context);

            flag = query(url, database);

            // 如果存在，直接返回插入成功
            if (flag) {
                database.beginTransaction();
                ContentValues updatedValues = new ContentValues();
                updatedValues.put(DLDao.endPos, endPos);
                int temp = database.update(DBBASE_DOWNLOAD_TABLE_NAME, updatedValues, turl + " = ?", new String[]{url});
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


    private static synchronized void delete(SQLiteDatabase database) {
        try {
            database.beginTransaction();
            database.delete(DBBASE_DOWNLOAD_TABLE_NAME, null, null);
            database.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (database != null) {
                database.endTransaction();
            }
        }
    }


    public static synchronized boolean delete(Context context) {
        LogCat.e("video", "DLDao------delete........");
        SQLiteDatabase database = null;
        boolean flag = false;
        try {
            database = getConnection(context);
            database.beginTransaction();
            int temp = database.delete(DBBASE_DOWNLOAD_TABLE_NAME, null, null);
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

    public static synchronized boolean delete(SQLiteDatabase database, int tid) {
        boolean flag = false;
        try {
            database.beginTransaction();
            int temp = database.delete(DBBASE_DOWNLOAD_TABLE_NAME, tid + " = ?", new String[]{String.valueOf(tid)});
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

        }
        return flag;
    }


    public static synchronized boolean updateOut(SQLiteDatabase database, int tid, long endPos) {
        if (database == null) {
            return false;
        }
        boolean flag = false;
        database.beginTransaction();
        ContentValues updatedValues = new ContentValues();
        updatedValues.put(DLDao.startPos, endPos);
        int temp = database.update(DBBASE_DOWNLOAD_TABLE_NAME, updatedValues, DLDao.tid + " = ?", new String[]{String.valueOf(tid)});
        if (temp == 0) {
            flag = true;
        }
        database.setTransactionSuccessful();
        database.endTransaction();
        return flag;
    }


    public static void closeDB(SQLiteDatabase database) {
        if (database != null) {
            database.close();
        }
    }


}
