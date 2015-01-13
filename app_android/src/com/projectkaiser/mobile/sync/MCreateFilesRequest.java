package com.projectkaiser.mobile.sync;

import java.util.ArrayList;
import java.util.List;

public class MCreateFilesRequest {

	List<MIssue> m_newTasks = new ArrayList<MIssue>();
	
	List<MComment> m_newComments = new ArrayList<MComment>();

	public List<MIssue> getNewTasks() {
		return m_newTasks;
	}

	public void setNewTasks(List<MIssue> newTasks) {
		m_newTasks = newTasks;
	}

	public List<MComment> getNewComments() {
		return m_newComments;
	}

	public void setNewComments(List<MComment> newComments) {
		m_newComments = newComments;
	}

}
