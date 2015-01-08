/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */ 
package com.projectkaiser.mobile.sync;

import java.util.ArrayList;
import java.util.List;

import com.projectkaiser.app_android.bl.obj.MNewComment;
import com.projectkaiser.app_android.bl.obj.MRemoteNotSyncedIssue;


public class MCreateRequestEx extends MBasicRequest {
	
	List<MNewComment> m_newComments = new ArrayList<MNewComment>();
	
	List<MRemoteNotSyncedIssue> m_newIssues = new ArrayList<MRemoteNotSyncedIssue>();

	public List<MNewComment> getNewComments() {
		return m_newComments;
	}

	public void setNewComments(List<MNewComment> newComments) {
		m_newComments = newComments;
	}

	public List<MRemoteNotSyncedIssue> getNewIssues() {
		return m_newIssues;
	}

	public void setNewIssues(List<MRemoteNotSyncedIssue> newIssues) {
		m_newIssues = newIssues;
	}

}
