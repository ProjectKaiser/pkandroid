/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */ 
package com.projectkaiser.mobile.sync;

import java.io.Serializable;

public class MComment extends MFile implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4886393700925015642L;

	Long m_creator;
	
	Long m_created;
	
	String m_creatorName;
	
	String m_description;

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

	public String getCreatorName() {
		return m_creatorName;
	}

	public void setCreatorName(String creatorName) {
		m_creatorName = creatorName;
	}

	public String getDescription() {
		return m_description;
	}

	public void setDescription(String description) {
		m_description = description;
	}
	
}
