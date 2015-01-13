/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */ 
package com.projectkaiser.mobile.sync;

import java.io.Serializable;

public class MFile implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 7292163508164580417L;
	Long m_id;
	
	public Long getId() {
		return m_id;
	}

	public void setId(Long id) {
		m_id = id;
	}

}
