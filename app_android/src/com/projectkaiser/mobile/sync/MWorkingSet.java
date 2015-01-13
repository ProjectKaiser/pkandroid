/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */ 
package com.projectkaiser.mobile.sync;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class MWorkingSet implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 5282257912491735282L;

	String m_name;
	
	List<Long> m_projects = new ArrayList<Long>();

	public String getName() {
		return m_name;
	}

	public void setName(String name) {
		m_name = name;
	}

	public List<Long> getProjects() {
		return m_projects;
	}

	public void setProjects(List<Long> projects) {
		m_projects = projects;
	}

}
