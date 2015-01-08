package com.projectkaiser.app_android.settings;

public class SrvConnectionId {
	
	public final static String ARG = "SrvConnectionId";

	String m_id;

	public String getId() {
		return m_id;
	}

	public SrvConnectionId(String serverUrl, String email) {
		m_id = String.format("%s!%s", serverUrl, email);
	}

	public SrvConnectionId(String id) {
		m_id = id;
	}

	@Override
	public String toString() {
		return m_id;
	}
	
	public String prefixed(String key) {
		return String.format("%s %s", this.toString(), key);
	}
	
}
