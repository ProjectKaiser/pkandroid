package com.projectkaiser.app_android.async;

public interface AsyncCallback<T> {

	void onSuccess(T callback);
	void onFailure(Throwable e);
	
}
