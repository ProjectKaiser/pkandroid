package com.projectkaiser.app_android;

import org.apache.log4j.Level;

import android.app.Application;
import android.os.Environment;

import com.projectkaiser.app_android.misc.ErrorReporter;

import de.mindpipe.android.logging.log4j.LogConfigurator;

public class AppAndroid extends Application {

	
	@Override
	public void onCreate() {

        final LogConfigurator logConfigurator = new LogConfigurator();
/*
        logConfigurator.setFileName(Environment.getExternalStorageDirectory() + String.format("/Android/data/%s/files/",
				getApplicationContext().getPackageName()) + "/pklog.txt");
        logConfigurator.setRootLevel(Level.DEBUG);
        // Set log level of a specific logger
        logConfigurator.setLevel("org.apache", Level.ERROR);
        logConfigurator.setFilePattern("%d - [%p::%c] - %m%n");
        logConfigurator.configure();
*/		
		
		ErrorReporter.bindReporter(getApplicationContext());
		super.onCreate();
	}

}
