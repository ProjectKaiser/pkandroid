package com.projectkaiser.mobile.sync;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class MDigestedArray<T> implements Serializable {
	
	private static final long serialVersionUID = 8233328271496667198L;

	String m_digest;
	
	List<T> m_items = new ArrayList<T>();

	public String getDigest() {
		return m_digest;
	}

	public void setDigest(String digest) {
		m_digest = digest;
	}

	public List<T> getItems() {
		return m_items;
	}

	public void setItems(List<T> items) {
		m_items = items;
	}

}
