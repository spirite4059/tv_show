package com.gochinatv.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class IDBHelper extends SQLiteOpenHelper implements IDBConstants {


	public IDBHelper(Context context) {
		// CursorFactory设置为null,使用默认值
		super(context, DBBASE_NAME, null, DBBASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(SQL_CREATE_TODAY_VIDEO_LIST);
		db.execSQL(SQL_CREATE_TOMORROW_VIDEO_LIST);
		db.execSQL(SQL_CREATE_DOWNLOAD_LIST);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL(SQL_DROP);
		db.execSQL(SQL_DROP_TM);
		db.execSQL(SQL_DROP_DOWNLOAD_TABLE);
		onCreate(db);
	}

}
