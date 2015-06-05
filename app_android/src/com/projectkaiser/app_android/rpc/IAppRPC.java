package com.projectkaiser.app_android.rpc;

import com.projectkaiser.mobile.sync.MBasicRequest;
import com.projectkaiser.mobile.sync.MCreateRequestEx;
import com.projectkaiser.mobile.sync.MSynchronizeRequestEx;

public interface IAppRPC {
	
	String rpc_login(MBasicRequest request);

	String rpc_create(MCreateRequestEx request);

	String rpc_syncronize(MSynchronizeRequestEx request);
	
}
