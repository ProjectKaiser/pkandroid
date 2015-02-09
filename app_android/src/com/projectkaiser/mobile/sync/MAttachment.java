package com.projectkaiser.mobile.sync;

import java.io.Serializable;
import java.io.InputStream;

public class MAttachment extends MFile implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4886393700925015642L;

	Long m_creator;
	
	Long m_created;
	
	String m_name;
		
	InputStream m_body;

	public Long getCreator() {
		return m_creator;
	}

	public void setCreator(Long creator) {
		m_creator = creator;
	}

	public Long getCreated() {
		return m_created;
	}

	public void setCreated(Long created) {
		m_created = created;
	}

	public String getName() {
		return m_name;
	}

	public void setName(String Name) {
		m_name = Name;
	}

	public InputStream getBody() {
		return m_body;
	}

	public void setBody(InputStream body) {
		m_body = body;
	}
}