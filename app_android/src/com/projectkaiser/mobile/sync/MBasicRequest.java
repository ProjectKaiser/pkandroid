package com.projectkaiser.mobile.sync;

import java.util.Locale;

import com.projectkaiser.app_android.jsonrpc.auth.AuthScheme;

public class MBasicRequest {

	String m_serverUrl;
	
	AuthScheme m_authScheme;
	
	Locale m_locale;

	public AuthScheme getAuthScheme() {
		return m_authScheme;
	}

	public void setAuthScheme(AuthScheme authScheme) {
		m_authScheme = authScheme;
	}

	public Locale getLocale() {
		return m_locale;
	}

	public void setLocale(Locale locale) {
		m_locale = locale;
	}

	public String getServerUrl() {
		return m_serverUrl;
	}

	public void setServerUrl(String serverUrl) {
		m_serverUrl = serverUrl;
	}

}
