package com.projectkaiser.app_android.bl.events;

public interface IGlobalAppEventsProvider {

	void register(IGlobalAppEventsListener listener);
	
	void syncRequested();
	
void newConnectionRequested();
	
}
