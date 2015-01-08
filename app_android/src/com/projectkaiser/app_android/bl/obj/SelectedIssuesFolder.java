package com.projectkaiser.app_android.bl.obj;

import com.projectkaiser.mobile.sync.MMyProject;
import com.projectkaiser.mobile.sync.MTeamMember;

public class SelectedIssuesFolder {
	
	Long m_id;
	
	String m_name;
	
	MMyProject m_project;
	
	String m_connectionId;
	
	public MTeamMember findTeamMember(Long userId) {
		for (MTeamMember tm:m_project.getTeam())
			if (userId.equals(tm.getId()))
				return tm;
		return null;
	}

	public String getConnectionId() {
		return m_connectionId;
	}

	public MMyProject getProject() {
		return m_project;
	}

	public void setProject(MMyProject project) {
		m_project = project;
	}

	public Long getId() {
		return m_id;
	}

	public void setId(Long id) {
		m_id = id;
	}

	public String getName() {
		return m_name;
	}

	public void setName(String name) {
		m_name = name;
	}

	public SelectedIssuesFolder(String connectionId) {
		m_connectionId = connectionId;
	}

}
