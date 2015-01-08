package com.projectkaiser.app_android;

import org.apache.log4j.Logger;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

public class ActivityMgr {
	
	Activity m_current;
	
	private final Logger log = Logger.getLogger(ActivityMgr.class);

	public final static ActivityMgr get(Activity current) {
		return new ActivityMgr(current);
	}
	
	protected ActivityMgr(Activity current) {
		m_current = current;
	}
	
	public ActivityMgr startMain() {
		log.debug("starting main activity");
		Context ctx = m_current.getApplicationContext();
		Intent i = new Intent(ctx, MainActivity.class);
		i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		ctx.startActivity(i);
		return this;
	}
	
	public ActivityMgr finishMe() {
		m_current.finish();
		return this;
	}

}
