/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */ 
package com.projectkaiser.mobile.sync;

import java.util.ArrayList;
import java.util.List;

import com.projectkaiser.app_android.R;

import android.content.Context;

public class MMyProject extends MNamedFile {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -3371393731639863183L;

	List<MTeamMember> m_team = new ArrayList<MTeamMember>();
	
	List<MFolder> m_folders = new ArrayList<MFolder>();

	public List<MFolder> getFolders(Context context) {
/*		
		List<MFolder> flds = new ArrayList<MFolder>(); 
		MFolder p = new MFolder();
		p.setId(-1L);
		p.setName(context.getString(R.string.no_folder));
		flds.add(p);
		flds.addAll(m_folders);
		return flds;
*/		
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
