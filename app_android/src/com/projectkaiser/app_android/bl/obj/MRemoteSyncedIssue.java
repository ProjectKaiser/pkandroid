package com.projectkaiser.app_android.bl.obj;

import com.projectkaiser.mobile.sync.MRemoteIssue;

public class MRemoteSyncedIssue extends MRemoteIssue {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -1774125729564804531L;
	String m_srvConnId;

	public String getSrvConnId() {
		return m_srvConnId;
	}

	public void setSrvConnId(String srvConnId) {
		m_srvConnId = srvConnId;
	}

}
