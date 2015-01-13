/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */ 
package com.projectkaiser.mobile.sync;

import java.io.Serializable;

public class MNamedFile extends MFile implements Serializable {

	private static final long serialVersionUID = 5804151859788285195L;
	
	String m_name;

	public String getName() {
		return m_name;
	}

	public void setName(String name) {
		m_name = name;
	}
	
}
