package com.projectkaiser.app_android.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class PkTasksDbHelper extends SQLiteOpenHelper {

	public PkTasksDbHelper(Context context) {
		super(context, PkTasksDb.DB_NAME, null, PkTasksDb.VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		for (int i = 1; i<=PkTasksDb.VERSION; i++)
			PkTasksDb.execVersion(db, i);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int from, int to) {
		for (int i = from + 1; i <= to; i++)
			PkTasksDb.execVersion(db, i);
	}

}
