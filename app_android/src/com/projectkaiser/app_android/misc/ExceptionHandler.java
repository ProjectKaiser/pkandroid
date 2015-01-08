package com.projectkaiser.app_android.misc;

import java.lang.Thread.UncaughtExceptionHandler;

import org.apache.log4j.Logger;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

@SuppressLint("SimpleDateFormat")
public class ExceptionHandler implements UncaughtExceptionHandler {

	private String versionName = "0";
	private int versionCode = 0;
	private final Logger log = Logger.getLogger(ExceptionHandler.class);
	private final Thread.UncaughtExceptionHandler previousHandler;

	private ExceptionHandler(Context context, boolean chained) {

		PackageManager mPackManager = context.getPackageManager();
		PackageInfo mPackInfo;
		try {
			mPackInfo = mPackManager
					.getPackageInfo(context.getPackageName(), 0);
			versionName = mPackInfo.versionName;
			versionCode = mPackInfo.versionCode;
		} catch (PackageManager.NameNotFoundException e) {
			// ignore
		}
		if (chained)
			previousHandler = Thread.getDefaultUncaughtExceptionHandler();
		else
			previousHandler = null;
	}

	static ExceptionHandler inContext(Context context) {
		return new ExceptionHandler(context, true);
	}

	static ExceptionHandler reportOnlyHandler(Context context) {
		return new ExceptionHandler(context, false);
	}

	@Override
	public void uncaughtException(Thread thread, Throwable exception) {
		
		StringBuilder reportBuilder = new StringBuilder();
		reportBuilder
				.append(String.format("Version: %s (%d), ", versionName,
						versionCode)).append(thread.toString());

		log.error(reportBuilder, exception);
		
		if (previousHandler != null)
			previousHandler.uncaughtException(thread, exception);
	}

}
