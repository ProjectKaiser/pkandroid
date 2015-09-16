package com.projectkaiser.app_android.bl.events;

public class LocalTaskRevert extends AppEvent {
	private int m_position=0;
	
	public LocalTaskRevert(int position){
		m_position = position;
	}
	
	public int getPosition(){
		return m_position;
	}
}
