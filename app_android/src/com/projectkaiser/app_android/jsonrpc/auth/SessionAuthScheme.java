package com.projectkaiser.app_android.jsonrpc.auth;

public class SessionAuthScheme extends AuthScheme {
	
	String m_sessionId;

	public String getSessionId() {
		return m_sessionId;
	}

	public void setSessionId(String sessionId) {
		m_sessionId = sessionId;
	}

}
