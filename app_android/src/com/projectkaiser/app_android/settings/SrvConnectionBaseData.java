package com.projectkaiser.app_android.settings;

public class SrvConnectionBaseData {

	String m_sessionId;

	String m_userName;
 
	Long m_userId;
	
	String m_serverName;

	public String getSessionId() {
		return m_sessionId;
	}

	public void setSessionId(String sessionId) {
		m_sessionId = sessionId;
	}

	public String getUserName() {
		return m_userName;
	}

	public void setUserName(String userName) {
		m_userName = userName;
	}

	public Long getUserId() {
		return m_userId;
	}

	public void setUserId(Long userId) {
		m_userId = userId;
	}

	public String getServerName() {
		return m_serverName;
	}

	public void setServerName(String serverName) {
		m_serverName = serverName;
	}

	
}
