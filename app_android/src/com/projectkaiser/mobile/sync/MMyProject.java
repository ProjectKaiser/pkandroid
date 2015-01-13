/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */ 
package com.projectkaiser.mobile.sync;

import java.util.ArrayList;
import java.util.List;

public class MMyProject extends MNamedFile {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -3371393731639863183L;

	List<MTeamMember> m_team = new ArrayList<MTeamMember>();
	
	List<MFolder> m_folders = new ArrayList<MFolder>();

	public List<MFolder> getFolders() {
		return m_folders;
	}

	public void setFolders(List<MFolder> folders) {
		m_folders = folders;
	}

	public List<MTeamMember> getTeam() {
		return m_team;
	}

	public void setTeam(List<MTeamMember> team) {
		m_team = team;
	}

	
}
