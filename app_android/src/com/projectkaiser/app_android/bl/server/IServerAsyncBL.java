package com.projectkaiser.app_android.bl.server;

import com.projectkaiser.app_android.async.AsyncCallback;
import com.projectkaiser.mobile.sync.BatchRequest;
import com.projectkaiser.mobile.sync.MBasicRequest;
import com.projectkaiser.mobile.sync.MSynchronizeResponseEx;

public interface IServerAsyncBL {
	
	void login(MBasicRequest request, AsyncCallback<String> data);

	void synchronize(BatchRequest request, AsyncCallback<MSynchronizeResponseEx> response);
	
}
