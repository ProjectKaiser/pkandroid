package com.projectkaiser.app_android.jsonrpc.auth;

public class PlainAuthScheme extends AuthScheme {

	String m_userName;
	
	String m_password;

	public String getUserName() {
		return m_userName;
	}

	public void setUserName(String userName) {
		m_userName = userName;
	}

	public String getPassword() {
		return m_password;
	}

	public void setPassword(String password) {
		m_password = password;
	}
	
}
