package com.projectkaiser.app_android.bl.local;

import java.util.List;

import com.projectkaiser.app_android.bl.obj.MNewComment;
import com.projectkaiser.app_android.bl.obj.MRemoteNotSyncedIssue;
import com.projectkaiser.mobile.sync.MComment;
import com.projectkaiser.mobile.sync.MCreateRequestEx;
import com.projectkaiser.mobile.sync.MLocalIssue;
import com.projectkaiser.mobile.sync.MRemoteIssue;

public interface ILocalBL {
	
	List<MLocalIssue> getLocalTasks(TasksFilter filter);
	List<MRemoteNotSyncedIssue> getNotSyncedTasks();
	
	MLocalIssue addTask(MLocalIssue task);
	MRemoteNotSyncedIssue addTask(MRemoteNotSyncedIssue task);
	
	MLocalIssue updateTask(MLocalIssue task);
	MRemoteNotSyncedIssue updateTask(MRemoteNotSyncedIssue task);	
	
	void deleteTask(MLocalIssue task);
	void deleteTask(MRemoteNotSyncedIssue task);
	void deleteNonSyncedTasks(String connectionId);
	void handleCreateResult(MCreateRequestEx request, List<Object> commentsRes, List<Object> tasksRes);
	
	void completeTask(MLocalIssue task);
	
	MComment addComment(MLocalIssue task, MComment comment);
	MComment addComment(MRemoteIssue task, MComment comment);
	MComment addComment(MRemoteNotSyncedIssue task, MComment comment);
	
	List<MComment> getComments(MLocalIssue task);
	List<MComment> getComments(MRemoteIssue task);
	List<MComment> getComments(MRemoteNotSyncedIssue task);
		
	List<MNewComment> getNewComments();
}
