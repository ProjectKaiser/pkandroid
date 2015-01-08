package com.projectkaiser.mobile.sync;

public class BatchRequest {
	
	MSynchronizeRequestEx m_syncRequest;
	
	MCreateRequestEx m_createRequest;

	public MSynchronizeRequestEx getSyncRequest() {
		return m_syncRequest;
	}

	public void setSyncRequest(MSynchronizeRequestEx syncRequest) {
		m_syncRequest = syncRequest;
	}

	public MCreateRequestEx getCreateRequest() {
		return m_createRequest;
	}

	public void setCreateRequest(MCreateRequestEx createRequest) {
		m_createRequest = createRequest;
	}

}
