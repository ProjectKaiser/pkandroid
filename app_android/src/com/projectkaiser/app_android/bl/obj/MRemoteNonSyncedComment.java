package com.projectkaiser.app_android.bl.obj;

import com.projectkaiser.mobile.sync.MComment;

public class MRemoteNonSyncedComment extends MComment {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5035954228684190044L;
	
	String m_failure;

	public String getFailure() {
		return m_failure;
	}

	public void setFailure(String failure) {
		m_failure = failure;
	}

}
