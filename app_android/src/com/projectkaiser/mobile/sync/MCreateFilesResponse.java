package com.projectkaiser.mobile.sync;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class MCreateFilesResponse implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3632533038782687269L;

	List<Object> m_commentRes = new ArrayList<Object>();
	
	List<Object> m_taskRes = new ArrayList<Object>();

	public List<Object> getCommentRes() {
		return m_commentRes;
	}

	public void setCommentRes(List<Object> commentRes) {
		m_commentRes = commentRes;
	}

	public List<Object> getTaskRes() {
		return m_taskRes;
	}

	public void setTaskRes(List<Object> taskRes) {
		m_taskRes = taskRes;
	}
	
}
