package com.projectkaiser.mobile.sync;

public class MRemoteIssue extends MIssue {

	private static final long serialVersionUID = -942063667023193523L;

	String m_path;

	String m_assigneeName;

	String m_responsibleName;

	String m_statusName;

	Long m_modifier;

	public Long getModifier() {
		return m_modifier;
	}

	public void setModifier(Long modifier) {
		m_modifier = modifier;
	}

	public String getPath() {
		return m_path;
	}

	public void setPath(String path) {
		m_path = path;
	}

	public String getAssigneeName() {
		return m_assigneeName;
	}

	public void setAssigneeName(String assigneeName) {
		m_assigneeName = assigneeName;
	}

	public String getResponsibleName() {
		return m_responsibleName;
	}

	public void setResponsibleName(String responsibleName) {
		m_responsibleName = responsibleName;
	}

	public String getStatusName() {
		return m_statusName;
	}

	public void setStatusName(String statusName) {
		m_statusName = statusName;
	}

}
