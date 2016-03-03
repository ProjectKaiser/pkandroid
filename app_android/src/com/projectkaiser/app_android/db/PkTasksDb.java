package com.projectkaiser.app_android.db;

import android.database.sqlite.SQLiteDatabase;

public class PkTasksDb {

	public final static int VERSION = 1;
	
	public static final String DB_NAME = "pk_tasks";

	public static final String T_LOCAL_TASKS = "local_tasks";
	
	public static final String T_LOCAL_COMMENTS = "local_comments";
	
	public static final String T_NOT_SYNCED_TASKS = "ns_tasks";
	
	public static final String T_NOT_SYNCED_COMMENTS = "ns_comments";
	
	public static final String F_ID = "id";
	
	public static final String F_NAME = "name";
	
	public static final String F_FAILURE = "failure";

	public final static String F_SERVER_CONNECTION_ID = "server_conn_id";

	public static final String F_ID_LOCAL_TASKS = "id_local_tasks";

	public static final String F_ID_NS_TASKS = "id_ns_tasks";

	public static final String F_ID_REMOTE_TASKS = "id_remote_tasks";

	public final static String F_FOLDER_ID = "folder_id";

	public final static String F_DESCRIPTION = "description";

	public final static String F_PRIORITY = "priority";

	public final static String F_DUE_DATE = "due_date";

	public final static String F_BUDGET = "budget";

	public final static String F_CREATED = "created";

	public final static String F_MODIFIED = "modified";

	public final static String F_ASSIGNEE_ID = "assignee_id";

	public final static String F_RESPONSIBLE_ID = "responsible_id";

	public final static String F_STATE = "state";

	//public final static String F_UNREAD = "unread";

	public static void execVersion(SQLiteDatabase db, int version) {
		switch (version) {
		case 1:
			version1(db);
			break;
		}
	}

	private static void version1(SQLiteDatabase db) {
		db.beginTransaction();
        try {
        	StringBuilder s = new StringBuilder(); 
        	
        	////////////////////////////////////////////
        	//   Local tasks
        	//
        	s.append("create table ");
        	s.append(T_LOCAL_TASKS);
        	s.append("(");
        	s.append(F_ID + " integer PRIMARY KEY autoincrement, ");
        	s.append(F_NAME + " text, ");
        	s.append(F_DESCRIPTION + " text, ");
//        	s.append(F_IS_ACTIVE + " integer, ");
        	s.append(F_PRIORITY + " integer, ");
        	s.append(F_DUE_DATE + " integer, ");
        	s.append(F_CREATED + " integer, ");
        	s.append(F_MODIFIED + " integer, ");
        	s.append(F_BUDGET + " integer, ");
        	s.append(F_STATE + " integer); ");
        	//s.append(F_UNREAD + " integer); ");
        	db.execSQL(s.toString());
        	
        	////////////////////////////////////////////
        	//   Local comments
        	//
        	s = new StringBuilder();
        	s.append("create table ");
        	s.append(T_LOCAL_COMMENTS);
        	s.append("(");
        	s.append(F_ID + " integer PRIMARY KEY autoincrement, ");
//        	s.append(F_IS_ACTIVE + " integer, ");
        	s.append(F_ID_LOCAL_TASKS + " integer, ");
        	s.append(F_DESCRIPTION + " text, ");
        	s.append(F_CREATED + " integer, ");
        	s.append("FOREIGN KEY("+F_ID_LOCAL_TASKS+") REFERENCES "+T_LOCAL_TASKS+"("+F_ID+")); ");        	
        	db.execSQL(s.toString());
        	
        	////////////////////////////////////////////
        	//   Not Synced Tasks
        	//
        	s = new StringBuilder();
        	s.append("create table ");
        	s.append(T_NOT_SYNCED_TASKS);
        	s.append("(");
        	s.append(F_ID + " integer PRIMARY KEY autoincrement, ");
        	s.append(F_NAME + " text, ");
//        	s.append(F_IS_ACTIVE + " integer, ");
        	s.append(F_DESCRIPTION + " text, ");
        	s.append(F_PRIORITY + " integer, ");
        	s.append(F_DUE_DATE + " integer, ");
        	s.append(F_CREATED + " integer, ");
        	s.append(F_MODIFIED + " integer, ");
        	s.append(F_BUDGET + " integer, ");
        	s.append(F_FOLDER_ID + " integer, ");
        	s.append(F_ASSIGNEE_ID + " integer, ");
        	s.append(F_RESPONSIBLE_ID + " integer, ");
        	s.append(F_SERVER_CONNECTION_ID + " text, ");
        	s.append(F_FAILURE + " text, ");
        	s.append(F_STATE + " integer); ");
        	//s.append(F_UNREAD + " integer); ");
        	db.execSQL(s.toString());
        	
        	////////////////////////////////////////////
        	//   Local comments
        	//
        	s = new StringBuilder();
        	s.append("create table ");
        	s.append(T_NOT_SYNCED_COMMENTS);
        	s.append("(");
        	s.append(F_ID + " integer PRIMARY KEY autoincrement, ");
        	s.append(F_ID_NS_TASKS + " integer, ");
        	s.append(F_ID_REMOTE_TASKS + " integer, ");
//        	s.append(F_IS_ACTIVE + " integer, ");
        	s.append(F_DESCRIPTION + " text, ");
        	s.append(F_CREATED + " integer, ");
        	s.append(F_FAILURE + " text, ");
        	s.append("FOREIGN KEY("+F_ID_NS_TASKS+") REFERENCES "+T_NOT_SYNCED_TASKS+"("+F_ID+"));");        	
        	db.execSQL(s.toString());
        	
        	
        	db.setTransactionSuccessful();
        } finally {
        	db.endTransaction();
        }
	}

}
