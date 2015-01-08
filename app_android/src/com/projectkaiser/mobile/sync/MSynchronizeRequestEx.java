/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */ 
package com.projectkaiser.mobile.sync;



public class MSynchronizeRequestEx extends MBasicRequest {
	
	String m_workingSetsDigest;
	
	String m_issuesDigest;
	
	String m_projectsDigest;

	public String getWorkingSetsDigest() {
		return m_workingSetsDigest;
	}

	public void setWorkingSetsDigest(String workingSetsdigest) {
		m_workingSetsDigest = workingSetsdigest;
	}

	public String getIssuesDigest() {
		return m_issuesDigest;
	}

	public void setIssuesDigest(String issuesDigest) {
		m_issuesDigest = issuesDigest;
	}

	public String getProjectsDigest() {
		return m_projectsDigest;
	}

	public void setProjectsDigest(String projectsDigest) {
		m_projectsDigest = projectsDigest;
	}
	


}
