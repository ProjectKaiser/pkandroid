package com.projectkaiser.app_android.bl.obj;

import com.projectkaiser.mobile.sync.MIssue;

public class MRemoteNotSyncedIssue extends MIssue {

	private static final long serialVersionUID = 8299045820244702555L;

	private Long m_folderId;
	
	private Long m_assigneeId;
	
	private Long m_responsibleId;
	
	private String m_srvConnId;
	
	private String m_failure;

	public String getFailure() {
		return m_failure;
	}

	public void setFailure(String failure) {
		m_failure = failure;
	}

	public Long getFolderId() {
		return m_folderId;
	}

	public void setFolderId(Long folderId) {
		m_folderId = folderId;
	}

	public Long getAssigneeId() {
		return m_assigneeId;
	}

	public void setAssigneeId(Long assigneeId) {
		m_assigneeId = assigneeId;
	}

	public Long getResponsibleId() {
		return m_responsibleId;
	}

	public void setResponsibleId(Long responsibleId) {
		m_responsibleId = responsibleId;
	}

	public String getSrvConnId() {
		return m_srvConnId;
	}

	public void setSrvConnId(String srvConnId) {
		m_srvConnId = srvConnId;
	}
	
}
