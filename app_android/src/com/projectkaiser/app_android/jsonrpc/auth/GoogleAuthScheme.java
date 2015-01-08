package com.projectkaiser.app_android.jsonrpc.auth;

public class GoogleAuthScheme extends AuthScheme {
	
	String m_token;
	
	String m_email;
	
	String m_pictureUrl;
	
	String m_displayName;

	public String getEmail() {
		return m_email;
	}

	public void setEmail(String email) {
		m_email = email;
	}

	public String getPictureUrl() {
		return m_pictureUrl;
	}

	public void setPictureUrl(String pictureUrl) {
		m_pictureUrl = pictureUrl;
	}

	public String getDisplayName() {
		return m_displayName;
	}

	public void setDisplayName(String displayName) {
		m_displayName = displayName;
	}

	public String getToken() {
		return m_token;
	}

	public void setToken(String token) {
		m_token = token;
	}

}
