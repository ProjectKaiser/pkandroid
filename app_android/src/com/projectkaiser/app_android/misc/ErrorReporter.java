package com.projectkaiser.app_android.misc;

import android.content.Context;

public class ErrorReporter {

	private ErrorReporter() {
	}

	/**
	 * Apply error reporting to a specified application context
	 * 
	 * @param context
	 *            context for which errors are reported (used to get package
	 *            name)
	 */
	public static void bindReporter(Context context) {
		Thread.setDefaultUncaughtExceptionHandler(ExceptionHandler
				.inContext(context));
	}

}
