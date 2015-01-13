/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */ 
package com.projectkaiser.mobile.sync;

import java.io.Serializable;

public class MLoginResponse implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1140628315575722798L;

	String m_sessionId;

	Long m_userId;
	
	String m_userName;
	
	String m_serverName;
	
	public String getServerName() {
		return m_serverName;
	}

	public void setServerName(String serverName) {
		m_serverName = serverName;
	}

	public String getSessionId() {
		return m_sessionId;
	}

	public void setSessionId(String sessionId) {
		m_sessionId = sessionId;
	}

	public Long getUserId() {
		return m_userId;
	}

	public void setUserId(Long userId) {
		m_userId = userId;
	}

	public String getUserName() {
		return m_userName;
	}

	public void setUserName(String userName) {
		m_userName = userName;
	}


	
}
