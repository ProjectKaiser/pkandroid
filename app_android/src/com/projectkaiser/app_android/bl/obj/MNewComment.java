package com.projectkaiser.app_android.bl.obj;

public class MNewComment {
	
	Long m_id;
	

	public Long getId() {
		return m_id;
	}

	public void setId(Long id) {
		m_id = id;
	}

	Long m_taskId;
	
	String m_description;
	
	boolean m_nonSyncedTask;

	public boolean isNonSyncedTask() {
		return m_nonSyncedTask;
	}

	public void setNonSyncedTask(boolean nonSyncedTask) {
		m_nonSyncedTask = nonSyncedTask;
	}

	public Long getTaskId() {
		return m_taskId;
	}

	public void setTaskId(Long taskId) {
		m_taskId = taskId;
	}

	public String getDescription() {
		return m_description;
	}

	public void setDescription(String description) {
		m_description = description;
	}	
	

}
