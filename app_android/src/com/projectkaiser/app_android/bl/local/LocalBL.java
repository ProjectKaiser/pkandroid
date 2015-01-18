package com.projectkaiser.app_android.bl.local;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.projectkaiser.app_android.bl.obj.MNewComment;
import com.projectkaiser.app_android.bl.obj.MRemoteNonSyncedComment;
import com.projectkaiser.app_android.bl.obj.MRemoteNotSyncedIssue;
import com.projectkaiser.app_android.consts.State;
import com.projectkaiser.app_android.db.PkTasksDb;
import com.projectkaiser.app_android.db.PkTasksDbHelper;
import com.projectkaiser.mobile.sync.MComment;
import com.projectkaiser.mobile.sync.MCreateRequestEx;
import com.projectkaiser.mobile.sync.MLocalIssue;
import com.projectkaiser.mobile.sync.MRemoteIssue;

public class LocalBL implements ILocalBL {

	PkTasksDbHelper m_helper;

	private final Logger log = Logger.getLogger(LocalBL.class);

	private static final String TAG = "LocalBL";

	public LocalBL(Context ctx) {
		m_helper = new PkTasksDbHelper(ctx);
	}

	@Override
	public ArrayList<MLocalIssue> getLocalTasks(TasksFilter filter) {
		SQLiteDatabase db = m_helper.getReadableDatabase();

		String[] projection = { PkTasksDb.F_ID, PkTasksDb.F_NAME,
				PkTasksDb.F_DESCRIPTION, PkTasksDb.F_PRIORITY,
				PkTasksDb.F_DUE_DATE, PkTasksDb.F_CREATED,
				PkTasksDb.F_MODIFIED, PkTasksDb.F_BUDGET, PkTasksDb.F_STATE };

		String selection = null;
		String sortOrder = null;
		String limit = null;
		
		switch (filter) {
		case ACTIVE:
			selection = PkTasksDb.F_STATE + "!=" + State.TERMINATED;
			sortOrder = PkTasksDb.F_PRIORITY + " DESC";
			break;
		case CLOSED:
			selection = PkTasksDb.F_STATE + "=" + State.TERMINATED;			
			sortOrder = PkTasksDb.F_MODIFIED + " DESC";
			limit = "300";
			break;
		}
		
		String[] selectionArgs = null;

		Cursor c = db.query(PkTasksDb.T_LOCAL_TASKS, projection, // The columns
																	// to return
				selection, // The columns for the WHERE clause
				selectionArgs, // The values for the WHERE clause
				null, // don't group the rows
				null, // don't filter by row groups
				sortOrder, // The sort order
				limit
				);

		ArrayList<MLocalIssue> tasks = new ArrayList<MLocalIssue>();
		if (c.moveToFirst())
			do {
				MLocalIssue task = new MLocalIssue();
				task.setId(c.getLong(c.getColumnIndexOrThrow(PkTasksDb.F_ID)));
				task.setName(c.getString(c
						.getColumnIndexOrThrow(PkTasksDb.F_NAME)));
				task.setDescription(c.getString(c
						.getColumnIndexOrThrow(PkTasksDb.F_DESCRIPTION)));
				task.setPriority(c.getInt(c
						.getColumnIndexOrThrow(PkTasksDb.F_PRIORITY)));
				task.setDueDate(c.getLong(c
						.getColumnIndexOrThrow(PkTasksDb.F_DUE_DATE)));
				task.setCreated(c.getLong(c
						.getColumnIndexOrThrow(PkTasksDb.F_CREATED)));
				task.setModified(c.getLong(c
						.getColumnIndexOrThrow(PkTasksDb.F_MODIFIED)));
				task.setBudget(c.getInt(c
						.getColumnIndexOrThrow(PkTasksDb.F_BUDGET)));
				task.setState(c.getInt(c
						.getColumnIndexOrThrow(PkTasksDb.F_STATE)));
				tasks.add(task);
			} while (c.moveToNext());

		return tasks;
	}

	@Override
	public List<MRemoteNotSyncedIssue> getNotSyncedTasks() {

		SQLiteDatabase db = m_helper.getReadableDatabase();

		String[] projection = { PkTasksDb.F_ID, PkTasksDb.F_NAME,
				PkTasksDb.F_DESCRIPTION, PkTasksDb.F_PRIORITY,
				PkTasksDb.F_DUE_DATE, PkTasksDb.F_CREATED,
				PkTasksDb.F_MODIFIED, PkTasksDb.F_BUDGET, PkTasksDb.F_STATE,
				PkTasksDb.F_FOLDER_ID, PkTasksDb.F_ASSIGNEE_ID, PkTasksDb.F_FAILURE,
				PkTasksDb.F_RESPONSIBLE_ID, PkTasksDb.F_SERVER_CONNECTION_ID };

		String selection = PkTasksDb.F_STATE + "!=" + State.TERMINATED; // +
																		// " AND "
																		// +
																		// PkTasksDb.F_IS_ACTIVE
																		// +
																		// "=1";

		String[] selectionArgs = null;

		String sortOrder = PkTasksDb.F_PRIORITY + " DESC";

		Cursor c = db.query(PkTasksDb.T_NOT_SYNCED_TASKS, projection, // The
																		// columns
																		// to
																		// return
				selection, // The columns for the WHERE clause
				selectionArgs, // The values for the WHERE clause
				null, // don't group the rows
				null, // don't filter by row groups
				sortOrder // The sort order
				);

		ArrayList<MRemoteNotSyncedIssue> tasks = new ArrayList<MRemoteNotSyncedIssue>();
		if (c.moveToFirst())
			do {
				MRemoteNotSyncedIssue task = new MRemoteNotSyncedIssue();
				task.setId(c.getLong(c.getColumnIndexOrThrow(PkTasksDb.F_ID)));
				task.setName(c.getString(c
						.getColumnIndexOrThrow(PkTasksDb.F_NAME)));
				task.setDescription(c.getString(c
						.getColumnIndexOrThrow(PkTasksDb.F_DESCRIPTION)));
				task.setPriority(c.getInt(c
						.getColumnIndexOrThrow(PkTasksDb.F_PRIORITY)));
				task.setDueDate(c.getLong(c
						.getColumnIndexOrThrow(PkTasksDb.F_DUE_DATE)));
				task.setCreated(c.getLong(c
						.getColumnIndexOrThrow(PkTasksDb.F_CREATED)));
				task.setModified(c.getLong(c
						.getColumnIndexOrThrow(PkTasksDb.F_MODIFIED)));
				task.setBudget(c.getInt(c
						.getColumnIndexOrThrow(PkTasksDb.F_BUDGET)));
				task.setState(c.getInt(c
						.getColumnIndexOrThrow(PkTasksDb.F_STATE)));
				task.setFolderId(c.getLong(c
						.getColumnIndexOrThrow(PkTasksDb.F_FOLDER_ID)));
				task.setAssigneeId(c.getLong(c
						.getColumnIndexOrThrow(PkTasksDb.F_ASSIGNEE_ID)));
				task.setResponsibleId(c.getLong(c
						.getColumnIndexOrThrow(PkTasksDb.F_RESPONSIBLE_ID)));
				task.setSrvConnId(c.getString(c
						.getColumnIndexOrThrow(PkTasksDb.F_SERVER_CONNECTION_ID)));
				task.setFailure(c.getString(c
						.getColumnIndexOrThrow(PkTasksDb.F_FAILURE)));
				tasks.add(task);
			} while (c.moveToNext());

		return tasks;
	}

	@Override
	public MRemoteNotSyncedIssue addTask(MRemoteNotSyncedIssue task) {
		SQLiteDatabase db = m_helper.getWritableDatabase();
		db.beginTransaction();
		try {

			long time = (new Date()).getTime();

			ContentValues values = new ContentValues();
			values.put(PkTasksDb.F_NAME, task.getName());
			values.put(PkTasksDb.F_DESCRIPTION, task.getDescription());
			values.put(PkTasksDb.F_PRIORITY, task.getPriority());
			values.put(PkTasksDb.F_DUE_DATE, task.getDueDate());
			values.put(PkTasksDb.F_CREATED, time);
			values.put(PkTasksDb.F_MODIFIED, time);
			values.put(PkTasksDb.F_BUDGET, task.getBudget());
			values.put(PkTasksDb.F_STATE, task.getState());
			values.put(PkTasksDb.F_FOLDER_ID, task.getFolderId());
			values.put(PkTasksDb.F_ASSIGNEE_ID, task.getAssigneeId());
			values.put(PkTasksDb.F_RESPONSIBLE_ID, task.getResponsibleId());
			values.put(PkTasksDb.F_SERVER_CONNECTION_ID, task.getSrvConnId());

			task.setId(db.insert(PkTasksDb.T_NOT_SYNCED_TASKS, null, values));

			db.setTransactionSuccessful();
		} finally {
			db.endTransaction();
		}
		return task;
	}

	@Override
	public void deleteTask(MLocalIssue task) {
		SQLiteDatabase db = m_helper.getWritableDatabase();
		db.beginTransaction();
		try {

			int commentsDeleted = db.delete(PkTasksDb.T_LOCAL_COMMENTS,
					PkTasksDb.F_ID_LOCAL_TASKS + "=?", new String[] { task
							.getId().toString() });

			db.delete(PkTasksDb.T_LOCAL_TASKS, "id=?", new String[] { task
					.getId().toString() });
			db.setTransactionSuccessful();

			log.debug("Local task <" + task.getId() + "> deleted with "
					+ commentsDeleted + " comments");
		} finally {
			db.endTransaction();
		}
	}
	
	@Override
	public void deleteNonSyncedTasks(String connectionId) {
		for (MRemoteNotSyncedIssue nst : getNotSyncedTasks()) {
			if (connectionId.equals(nst.getSrvConnId())) {
				deleteTask(nst);
				log.debug("Non-synced task <"+nst.getId().toString()+"> deleted when removing connection <"+connectionId+">");
			}
		}
	}

	@Override
	public void deleteTask(MRemoteNotSyncedIssue task) {
		SQLiteDatabase db = m_helper.getWritableDatabase();
		db.beginTransaction();
		try {

			int commentsDeleted = db.delete(PkTasksDb.T_NOT_SYNCED_COMMENTS,
					PkTasksDb.F_ID_NS_TASKS + "=?", new String[] { task.getId()
							.toString() });

			db.delete(PkTasksDb.T_NOT_SYNCED_TASKS, "id=?", new String[] { task
					.getId().toString() });
			db.setTransactionSuccessful();

			log.debug("Non-synced task <" + task.getId() + "> deleted with "
					+ commentsDeleted + " comments");
		} finally {
			db.endTransaction();
		}
	}

	@Override
	public MRemoteNotSyncedIssue updateTask(MRemoteNotSyncedIssue task) {
		SQLiteDatabase db = m_helper.getWritableDatabase();
		db.beginTransaction();
		try {

			long time = (new Date()).getTime();

			ContentValues values = new ContentValues();
			values.put(PkTasksDb.F_NAME, task.getName());
			values.put(PkTasksDb.F_DESCRIPTION, task.getDescription());
			values.put(PkTasksDb.F_PRIORITY, task.getPriority());
			values.put(PkTasksDb.F_DUE_DATE, task.getDueDate());
			values.put(PkTasksDb.F_MODIFIED, time);
			values.put(PkTasksDb.F_BUDGET, task.getBudget());
			values.put(PkTasksDb.F_STATE, task.getState());
			values.put(PkTasksDb.F_FOLDER_ID, task.getFolderId());
			values.put(PkTasksDb.F_ASSIGNEE_ID, task.getAssigneeId());
			values.put(PkTasksDb.F_RESPONSIBLE_ID, task.getResponsibleId());
			values.put(PkTasksDb.F_SERVER_CONNECTION_ID, task.getSrvConnId());
			values.put(PkTasksDb.F_FAILURE, (String)null);

			db.update(PkTasksDb.T_NOT_SYNCED_TASKS, values, "id=?",
					new String[] { task.getId().toString() });

			db.setTransactionSuccessful();
		} finally {
			db.endTransaction();
		}
		return task;
	}

	@Override
	public void completeTask(MLocalIssue task) {
		SQLiteDatabase db = m_helper.getWritableDatabase();
		db.beginTransaction();
		try {
			ContentValues values = new ContentValues();
			values.put(PkTasksDb.F_STATE, task.getState()==State.TERMINATED?State.RUNNING:State.TERMINATED);
			db.update(PkTasksDb.T_LOCAL_TASKS, values, "id=?",
					new String[] { task.getId().toString() });
			db.setTransactionSuccessful();
		} finally {
			db.endTransaction();
		}
	}
	
	@Override
	public void handleCreateResult(MCreateRequestEx request, List<Object> commentsRes, List<Object> tasksRes) {
		for (int i=0; i<request.getNewComments().size(); i++) {
			MNewComment c = request.getNewComments().get(i); 
			Object o = commentsRes.get(i);
			if (o instanceof Long || o instanceof Integer)
				deleteNewComment(c);
			else
				setCommentFailure(c, o.toString());
		}
		
		for (int i=0; i<request.getNewIssues().size(); i++) {
			MRemoteNotSyncedIssue task = request.getNewIssues().get(i);
			Object o = tasksRes.get(i);
			if (o instanceof Long || o instanceof Integer)
				deleteNewTask(task);
			else
				setTaskFailure(task, o.toString());
		}
		
	}

	public void deleteNewComment(MNewComment comment) {
		SQLiteDatabase db = m_helper.getWritableDatabase();
		db.beginTransaction();
		try {
			db.delete(PkTasksDb.T_NOT_SYNCED_COMMENTS, "id=?",
					new String[] { comment.getId().toString() });
			log.debug("New comment <" + comment.getId()
					+ "> deleted after successful sync");
			db.setTransactionSuccessful();
		} finally {
			db.endTransaction();
		}
	}
	
	public void deleteNewTask(MRemoteNotSyncedIssue task) {
		SQLiteDatabase db = m_helper.getWritableDatabase();
		db.beginTransaction();
		try {
			db.delete(PkTasksDb.T_NOT_SYNCED_TASKS, "id=?",
					new String[] { task.getId().toString() });
			log.debug("New task <" + task.getId()
					+ "> deleted after successful sync");
			db.setTransactionSuccessful();
		} finally {
			db.endTransaction();
		}
	}

	@Override
	public MLocalIssue addTask(MLocalIssue task) {
		SQLiteDatabase db = m_helper.getWritableDatabase();
		db.beginTransaction();
		try {

			long time = (new Date()).getTime();

			ContentValues values = new ContentValues();
			values.put(PkTasksDb.F_NAME, task.getName());
			values.put(PkTasksDb.F_DESCRIPTION, task.getDescription());
			values.put(PkTasksDb.F_PRIORITY, task.getPriority());
			values.put(PkTasksDb.F_DUE_DATE, task.getDueDate());
			values.put(PkTasksDb.F_CREATED, time);
			values.put(PkTasksDb.F_MODIFIED, time);
			values.put(PkTasksDb.F_BUDGET, task.getBudget());
			values.put(PkTasksDb.F_STATE, task.getState());

			task.setId(db.insert(PkTasksDb.T_LOCAL_TASKS, null, values));

			db.setTransactionSuccessful();
		} finally {
			db.endTransaction();
		}
		return task;
	}

	@Override
	public MLocalIssue updateTask(MLocalIssue task) {
		SQLiteDatabase db = m_helper.getWritableDatabase();
		db.beginTransaction();
		try {

			long time = (new Date()).getTime();

			ContentValues values = new ContentValues();
			values.put(PkTasksDb.F_NAME, task.getName());
			values.put(PkTasksDb.F_DESCRIPTION, task.getDescription());
			values.put(PkTasksDb.F_PRIORITY, task.getPriority());
			values.put(PkTasksDb.F_DUE_DATE, task.getDueDate());
			values.put(PkTasksDb.F_MODIFIED, time);
			values.put(PkTasksDb.F_BUDGET, task.getBudget());
			values.put(PkTasksDb.F_STATE, task.getState());

			db.update(PkTasksDb.T_LOCAL_TASKS, values, "id=?",
					new String[] { task.getId().toString() });

			db.setTransactionSuccessful();
		} finally {
			db.endTransaction();
		}
		return task;
	}
	
	public void setCommentFailure(MNewComment comment, String failure) {
		SQLiteDatabase db = m_helper.getWritableDatabase();
		db.beginTransaction();
		try {

			ContentValues values = new ContentValues();
			values.put(PkTasksDb.F_FAILURE, failure);

			db.update(PkTasksDb.T_NOT_SYNCED_COMMENTS, values, "id=?",
					new String[] { comment.getId().toString() });

			db.setTransactionSuccessful();
		} finally {
			db.endTransaction();
		}
	}
	
	public void setTaskFailure(MRemoteNotSyncedIssue task, String failure) {
		SQLiteDatabase db = m_helper.getWritableDatabase();
		db.beginTransaction();
		try {

			ContentValues values = new ContentValues();
			values.put(PkTasksDb.F_FAILURE, failure);

			db.update(PkTasksDb.T_NOT_SYNCED_TASKS, values, "id=?",
					new String[] { task.getId().toString() });

			db.setTransactionSuccessful();
		} finally {
			db.endTransaction();
		}
	}

	@Override
	public MComment addComment(MLocalIssue task, MComment comment) {
		SQLiteDatabase db = m_helper.getWritableDatabase();
		db.beginTransaction();
		try {

			long time = (new Date()).getTime();

			ContentValues values = new ContentValues();
			values.put(PkTasksDb.F_ID_LOCAL_TASKS, task.getId());
			values.put(PkTasksDb.F_DESCRIPTION, comment.getDescription());
			values.put(PkTasksDb.F_CREATED, time);

			comment.setId(db.insert(PkTasksDb.T_LOCAL_COMMENTS, null, values));

			db.setTransactionSuccessful();
		} finally {
			db.endTransaction();
		}
		return comment;
	}

	@Override
	public MComment addComment(MRemoteNotSyncedIssue task, MComment comment) {
		SQLiteDatabase db = m_helper.getWritableDatabase();
		db.beginTransaction();
		try {

			long time = (new Date()).getTime();

			ContentValues values = new ContentValues();
			values.put(PkTasksDb.F_ID_NS_TASKS, task.getId());
			values.put(PkTasksDb.F_DESCRIPTION, comment.getDescription());
			values.put(PkTasksDb.F_CREATED, time);

			comment.setId(db.insert(PkTasksDb.T_NOT_SYNCED_COMMENTS, null,
					values));

			db.setTransactionSuccessful();
		} finally {
			db.endTransaction();
		}
		return comment;
	}

	@Override
	public MComment addComment(MRemoteIssue task, MComment comment) {
		SQLiteDatabase db = m_helper.getWritableDatabase();
		db.beginTransaction();
		try {

			long time = (new Date()).getTime();

			ContentValues values = new ContentValues();
			values.put(PkTasksDb.F_ID_REMOTE_TASKS, task.getId());
			values.put(PkTasksDb.F_DESCRIPTION, comment.getDescription());
			values.put(PkTasksDb.F_CREATED, time);

			comment.setId(db.insert(PkTasksDb.T_NOT_SYNCED_COMMENTS, null,
					values));

			db.setTransactionSuccessful();
		} finally {
			db.endTransaction();
		}
		return comment;
	}

	@Override
	public List<MComment> getComments(MLocalIssue task) {
		List<MComment> comments = new ArrayList<MComment>();

		SQLiteDatabase db = m_helper.getReadableDatabase();

		String[] projection = { PkTasksDb.F_ID, PkTasksDb.F_DESCRIPTION,
				PkTasksDb.F_CREATED, };

		String selection = PkTasksDb.F_ID_LOCAL_TASKS + "="
				+ String.valueOf(task.getId()); // + " AND " +
												// PkTasksDb.F_IS_ACTIVE + "=1";

		Cursor c = db.query(PkTasksDb.T_LOCAL_COMMENTS, projection, // The
																	// columns
																	// to return
				selection, // The columns for the WHERE clause
				null, // The values for the WHERE clause
				null, // don't group the rows
				null, // don't filter by row groups
				PkTasksDb.F_CREATED // The sort order
				);

		if (c.moveToFirst())
			do {
				MComment comment = new MComment();
				comment.setId(c.getLong(c.getColumnIndexOrThrow(PkTasksDb.F_ID)));
				comment.setDescription(c.getString(c
						.getColumnIndexOrThrow(PkTasksDb.F_DESCRIPTION)));
				comment.setCreated(c.getLong(c
						.getColumnIndexOrThrow(PkTasksDb.F_CREATED)));
				comments.add(comment);
			} while (c.moveToNext());

		return comments;
	}

	@Override
	public List<MComment> getComments(MRemoteIssue task) {
		List<MComment> comments = new ArrayList<MComment>();

		comments.addAll(task.getComments());

		SQLiteDatabase db = m_helper.getReadableDatabase();

		String[] projection = { PkTasksDb.F_ID, PkTasksDb.F_DESCRIPTION,
				PkTasksDb.F_CREATED, PkTasksDb.F_FAILURE };

		String selection = PkTasksDb.F_ID_REMOTE_TASKS + "="
				+ String.valueOf(task.getId()); // + " AND " +
												// PkTasksDb.F_IS_ACTIVE + "=1";

		Cursor c = db.query(PkTasksDb.T_NOT_SYNCED_COMMENTS, projection, // The
																			// columns
																			// to
																			// return
				selection, // The columns for the WHERE clause
				null, // The values for the WHERE clause
				null, // don't group the rows
				null, // don't filter by row groups
				PkTasksDb.F_CREATED // The sort order
				);

		if (c.moveToFirst())
			do {
				MRemoteNonSyncedComment comment = new MRemoteNonSyncedComment();
				comment.setId(c.getLong(c.getColumnIndexOrThrow(PkTasksDb.F_ID)));
				comment.setDescription(c.getString(c
						.getColumnIndexOrThrow(PkTasksDb.F_DESCRIPTION)));
				comment.setCreated(c.getLong(c
						.getColumnIndexOrThrow(PkTasksDb.F_CREATED)));
				comment.setFailure(c.getString(c
						.getColumnIndexOrThrow(PkTasksDb.F_FAILURE)));
				comments.add(comment);
			} while (c.moveToNext());

		return comments;
	}

	@Override
	public List<MComment> getComments(MRemoteNotSyncedIssue task) {
		List<MComment> comments = new ArrayList<MComment>();

		SQLiteDatabase db = m_helper.getReadableDatabase();

		String[] projection = { PkTasksDb.F_ID, PkTasksDb.F_DESCRIPTION,
				PkTasksDb.F_CREATED, PkTasksDb.F_FAILURE };

		String selection = PkTasksDb.F_ID_NS_TASKS + "="
				+ String.valueOf(task.getId()); // + " AND " +
												// PkTasksDb.F_IS_ACTIVE + "=1";

		Cursor c = db.query(PkTasksDb.T_NOT_SYNCED_COMMENTS, projection, // The
																			// columns
																			// to
																			// return
				selection, // The columns for the WHERE clause
				null, // The values for the WHERE clause
				null, // don't group the rows
				null, // don't filter by row groups
				PkTasksDb.F_CREATED // The sort order
				);

		if (c.moveToFirst())
			do {
				MRemoteNonSyncedComment comment = new MRemoteNonSyncedComment();
				comment.setId(c.getLong(c.getColumnIndexOrThrow(PkTasksDb.F_ID)));
				comment.setDescription(c.getString(c
						.getColumnIndexOrThrow(PkTasksDb.F_DESCRIPTION)));
				comment.setCreated(c.getLong(c
						.getColumnIndexOrThrow(PkTasksDb.F_CREATED)));
				comment.setFailure(c.getString(c
						.getColumnIndexOrThrow(PkTasksDb.F_FAILURE)));
				comments.add(comment);
			} while (c.moveToNext());

		return comments;
	}

	@Override
	public List<MNewComment> getNewComments() {

		List<MNewComment> comments = new ArrayList<MNewComment>();

		SQLiteDatabase db = m_helper.getReadableDatabase();

		String[] projection = { PkTasksDb.F_ID, PkTasksDb.F_DESCRIPTION,
				PkTasksDb.F_CREATED, PkTasksDb.F_ID_REMOTE_TASKS,
				PkTasksDb.F_ID_NS_TASKS, };

		Cursor c = db.query(PkTasksDb.T_NOT_SYNCED_COMMENTS, projection, // The
																			// columns
																			// to
																			// return
				null, // The columns for the WHERE clause
				null, // The values for the WHERE clause
				null, // don't group the rows
				null, // don't filter by row groups
				PkTasksDb.F_CREATED // The sort order
				);

		if (c.moveToFirst())
			do {
				MNewComment comment = new MNewComment();
				comment.setId(c.getLong(c.getColumnIndexOrThrow(PkTasksDb.F_ID)));
				if (!c.isNull(c
						.getColumnIndexOrThrow(PkTasksDb.F_ID_REMOTE_TASKS))) {
					comment.setNonSyncedTask(false);
					comment.setTaskId(c.getLong(c
							.getColumnIndexOrThrow(PkTasksDb.F_ID_REMOTE_TASKS)));
				} else {
					comment.setNonSyncedTask(true);
					comment.setTaskId(c.getLong(c
							.getColumnIndexOrThrow(PkTasksDb.F_ID_NS_TASKS)));
				}
				comment.setDescription(c.getString(c
						.getColumnIndexOrThrow(PkTasksDb.F_DESCRIPTION)));
				comments.add(comment);
			} while (c.moveToNext());

		return comments;

	}

}
