package com.projectkaiser.app_android.jsonrpc.errors;

import com.projectkaiser.app_android.R;

import android.content.Context;

public class EServerOutDate extends RuntimeException {
	public EServerOutDate() {
		super();
	}

	public EServerOutDate(String msg) {
		super(msg);
	}
	
	public static String GetErrorText(Context ctx, String msg){
		if (msg.equals("0")){
			return ctx.getString(R.string.validation_srv_out);
		} else if (msg.equals("1")){
			return ctx.getString(R.string.validation_app_out);
		} else if (msg.equals("2")){
			return ctx.getString(R.string.validation_srv_out);
		} else if (msg.equals("3")){
			return ctx.getString(R.string.validation_srv_app);
		} else {
			return ctx.getString(R.string.validation_app_fnout) + " " 
			  + msg + " " 
			  + ctx.getString(R.string.validation_app_end); 
		}
	}
	
}
