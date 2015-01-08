package com.projectkaiser.app_android;

import java.lang.Thread.UncaughtExceptionHandler;

import android.app.AlertDialog;
import android.content.Context;

@Deprecated  
public class ErrorHandler implements UncaughtExceptionHandler {
	
	Context m_ctx;
	
	String m_activityName;

	public static void errorPopup(Context ctx, Throwable e, String source) {
	    AlertDialog.Builder messageBox = new AlertDialog.Builder(ctx);
	    messageBox.setTitle(source);
	    messageBox.setMessage(e.getClass().getName()+": "+e.getMessage());
	    messageBox.setCancelable(false);
	    messageBox.setNeutralButton("OK", null);
	    messageBox.show();
	}
	
	public ErrorHandler(Context ctx, String activityName) {
		m_ctx = ctx;
		m_activityName = activityName;
	}
	
	@Override
	public void uncaughtException(Thread thread, Throwable ex) {
		errorPopup(m_ctx, ex, m_activityName);
		
	}
	
}
