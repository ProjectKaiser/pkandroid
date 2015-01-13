/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */ 
package com.projectkaiser.mobile.sync;

import java.io.Serializable;


public class MSyncronizeResponse implements Serializable {

	private static final long serialVersionUID = -5200662369847438611L;

	MWorkingSets m_w = new MWorkingSets();

	MDigestedArray<MMyProject> m_p = new MDigestedArray<MMyProject>();

	MDigestedArray<MRemoteIssue> m_i = new MDigestedArray<MRemoteIssue>();

	public MWorkingSets getW() {
		return m_w;
	}

	public void setW(MWorkingSets w) {
		m_w = w;
	}

	public MDigestedArray<MMyProject> getP() {
		return m_p;
	}

	public void setP(MDigestedArray<MMyProject> p) {
		m_p = p;
	}

	public MDigestedArray<MRemoteIssue> getI() {
		return m_i;
	}

	public void setI(MDigestedArray<MRemoteIssue> i) {
		m_i = i;
	}

}
