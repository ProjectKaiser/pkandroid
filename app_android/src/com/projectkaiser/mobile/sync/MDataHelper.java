package com.projectkaiser.mobile.sync;

import android.content.Context;

import com.projectkaiser.app_android.settings.SessionManager;

public class MDataHelper {

	SessionManager m_sm;
	Context m_ctx;

	String m_connId;

	public MDataHelper(Context ctx, String connId) {
		m_sm = SessionManager.get(ctx);
		m_ctx = ctx;
		m_connId = connId;
	}

	public MMyProject findProject(Long id) {
		for (MMyProject p : m_sm.getMyProjects(m_connId).getItems())
			if (id.equals(p.getId()))
				return p;
		return null;
	}

	public MMyProject findProjectByFolder(Long id) {
		for (MMyProject p : m_sm.getMyProjects(m_connId).getItems()) {
			if (id.equals(p.getId()))
			{
				return p;
			}
			for (MFolder f : p.getFolders(m_ctx))
				if (id.equals(f.getId()))
					return p;
		}
		return null;
	}

	public Boolean projectHasFolders(Long pid) {
		for (MMyProject p : m_sm.getMyProjects(m_connId).getItems()) {
			if (pid.equals(p.getId()))
			{
				return p.getFolders(m_ctx).size() > 0;
			}
		}
		return false;
	}

	public MTeamMember findMember(MMyProject p, Long userId) {
		for (MTeamMember m : p.getTeam())
			if (userId.equals(m.getId()))
				return m;
		return null;
	}

	public MFolder findFolder(Long id, MMyProject p) {
		if (id != null) {
			for (MFolder f : p.getFolders(m_ctx))
				if (id.equals(f.getId()))
					return f;
		}
		return null;
	}
}
