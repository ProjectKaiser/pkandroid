package com.projectkaiser.app_android.jsonrpc.errors;

public class EJsonException extends EAppException {

	private static final long serialVersionUID = -7680995785772001885L;
	
	public EJsonException(Throwable e) {
		super(e);
	}
	
	public EJsonException(String message) {
		super(message);
	}

}
