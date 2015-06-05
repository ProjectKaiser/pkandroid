package com.projectkaiser.app_android.jsonrpc.errors;

import com.projectkaiser.app_android.R;

import android.content.Context;

public class EServerOutDate extends RuntimeException {
	
	private static final long serialVersionUID = -1524261081104725062L;
	
	private int mCode = -1;  
	public EServerOutDate() {
		super();
	}

	public EServerOutDate(int code, String msg) {
		super(msg);
		mCode = code;
	}
	
	public String GetErrorText(Context ctx){
		if (mCode== 0){
			return ctx.getString(R.string.sync_srv_error_0);
		} else if (mCode==1){
			return ctx.getString(R.string.sync_srv_error_1);
		} else if (mCode==2){
			return ctx.getString(R.string.sync_srv_error_2);
		} else {
			return ctx.getString(R.string.sync_srv_error_3);
		}
	}
	
}
