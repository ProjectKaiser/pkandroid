package com.projectkaiser.app_android.rpc;

import com.projectkaiser.mobile.sync.MBasicRequest;
import com.projectkaiser.mobile.sync.MCreateRequestEx;
import com.projectkaiser.mobile.sync.MSynchronizeRequestEx;

public interface IAppRPC {
	
	String login(MBasicRequest request);

	String create(MCreateRequestEx request);

	String syncronize(MSynchronizeRequestEx request);
	
}
