package com.projectkaiser.app_android.bl;

import android.content.Context;

import com.projectkaiser.app_android.bl.local.ILocalBL;
import com.projectkaiser.app_android.bl.local.LocalBL;
import com.projectkaiser.app_android.bl.server.IServerAsyncBL;
import com.projectkaiser.app_android.bl.server.ServerAsyncBLImpl;

public class BL {
	
	static ILocalBL m_localBL = null;
	
	static IServerAsyncBL m_serverBL = null;
	
	public static ILocalBL getLocal(Context ctx) {
		if (m_localBL == null)
			m_localBL = new LocalBL(ctx);
		return m_localBL;
	}
	
	public static IServerAsyncBL getServer(Context ctx) {
		if (m_serverBL == null)
			m_serverBL = new ServerAsyncBLImpl();
		return m_serverBL;
	}

}
