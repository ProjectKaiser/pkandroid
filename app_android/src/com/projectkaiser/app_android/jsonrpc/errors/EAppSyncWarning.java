package com.projectkaiser.app_android.jsonrpc.errors;

import android.content.Context;

import com.projectkaiser.app_android.R;

public class EAppSyncWarning extends RuntimeException {

	private static final long serialVersionUID = 3912438960061724435L;

	private int mCode = -1;  
	public EAppSyncWarning() {
		super();
	}

	public EAppSyncWarning(int code, String msg) {
		super(msg);
		mCode = code;
	}
	
	public String GetErrorText(Context ctx){
		if (mCode == 0){
			return ctx.getString(R.string.validation_srv_fnout) + " " 
			  + this.getMessage() + " " 
			  + ctx.getString(R.string.validation_srv_end); 
		} else if (mCode == 1){
			return ctx.getString(R.string.validation_app_fnout) + " " 
					  + this.getMessage() + " " 
					  + ctx.getString(R.string.validation_app_end);
		} else {
			return ctx.getString(R.string.validation_srv_bad);
		}
	}
}
