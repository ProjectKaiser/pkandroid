/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */ 
package com.projectkaiser.mobile.sync;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class MIssue extends MNamedFile implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 879969120668583997L;

	String m_description;
	
	Integer m_priority;
	
	Long m_dueDate;
	
	Integer m_budget;
	
	Long m_created;
	
	Long m_modified;
	
	int m_state;
	
	public int getState() {
		return m_state;
	}

	public void setState(int state) {
		m_state = state;
	}

	public Long getCreated() {
		return m_created;
	}

	public void setCreated(Long created) {
		m_created = created;
	}

	public Long getModified() {
		return m_modified;
	}

	public void setModified(Long modified) {
		m_modified = modified;
	}

	List<MComment> m_comments = new ArrayList<MComment>();
	List<MAttachment> m_attachments = new ArrayList<MAttachment>();
	
	public String getDescription() {
		return m_description;
	}

	public void setDescription(String description) {
		m_description = description;
	}

	public Integer getPriority() {
		return m_priority;
	}

	public void setPriority(Integer priority) {
		m_priority = priority;
	}

	public Long getDueDate() {
		return m_dueDate;
	}

	public void setDueDate(Long dueDate) {
		m_dueDate = dueDate;
	}

	public Integer getBudget() {
		return m_budget;
	}

	public void setBudget(Integer budget) {
		m_budget = budget;
	}

	public List<MComment> getComments() {
		return m_comments;
	}

	public void setComments(List<MComment> comments) {
		m_comments = comments;
	}
	
	public List<MAttachment> getAttachments() {
		return m_attachments;
	}

	public void setAttachments(List<MAttachment> Attachments) {
		m_attachments = Attachments;
	}
}
