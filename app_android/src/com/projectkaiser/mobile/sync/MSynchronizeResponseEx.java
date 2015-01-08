package com.projectkaiser.mobile.sync;

import java.util.ArrayList;
import java.util.List;

public class MSynchronizeResponseEx {
	
	String m_data;
	
	List<Object> m_taskRes = new ArrayList<Object>();

	List<Object> m_commentRes = new ArrayList<Object>();

	public String getData() {
		return m_data;
	}

	public void setData(String data) {
		m_data = data;
	}

	public List<Object> getTaskRes() {
		return m_taskRes;
	}

	public void setTaskRes(List<Object> taskRes) {
		m_taskRes = taskRes;
	}

	public List<Object> getCommentRes() {
		return m_commentRes;
	}

	public void setCommentRes(List<Object> commentRes) {
		m_commentRes = commentRes;
	}

}
