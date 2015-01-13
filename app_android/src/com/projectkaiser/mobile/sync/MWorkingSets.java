package com.projectkaiser.mobile.sync;

public class MWorkingSets extends MDigestedArray<MWorkingSet>  {
	
	private static final long serialVersionUID = 340204315277426636L;
	
	String m_defaultWorkingSet;
	
	public String getDefaultWorkingSet() {
		return m_defaultWorkingSet;
	}

	public void setDefaultWorkingSet(String defaultWorkingSet) {
		m_defaultWorkingSet = defaultWorkingSet;
	}


}
