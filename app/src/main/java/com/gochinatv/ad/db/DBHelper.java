package com.gochinatv.ad.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.gochinatv.ad.tools.LogCat;

public class DBHelper extends SQLiteOpenHelper implements DBConstants {


	public DBHelper(Context context) {
		// CursorFactory设置为null,使用默认值
		super(context, DBBASE_NAME, null, DBBASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(SQL_CREAT);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL(SQL_DROP);
		onCreate(db);
	}

}
