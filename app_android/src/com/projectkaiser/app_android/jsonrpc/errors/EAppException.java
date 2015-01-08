package com.projectkaiser.app_android.jsonrpc.errors;

public class EAppException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6441433019361624545L;
	
	public EAppException() {
		super();
	}
	
	public EAppException(Throwable e) {
		super(e);
	}

	public EAppException(String msg) {
		super(msg);
	}

}
