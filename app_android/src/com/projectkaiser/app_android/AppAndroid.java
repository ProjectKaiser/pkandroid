package com.projectkaiser.app_android;

import org.apache.log4j.Level;
import android.app.Application;
import java.io.File;
import java.io.IOException;
import com.projectkaiser.app_android.misc.ErrorReporter;
import de.mindpipe.android.logging.log4j.LogConfigurator;

public class AppAndroid extends Application {

	
	@Override
	public void onCreate() {

        final LogConfigurator logConfigurator = new LogConfigurator();

        File logdir = null;
        if (getApplicationContext().getExternalFilesDir(null)==null){
            logdir = new File(getApplicationContext().getFilesDir().getAbsolutePath());
        } else {
            logdir = new File(getApplicationContext().getExternalFilesDir(null).getAbsolutePath());
        }
        if (!logdir.canWrite()){
    		// TODO: write to log 
        	System.out.println("Can't write.");		
        } 
        
        if (!logdir.exists()){ 
        	logdir.mkdirs();
        }	
        
        File logfile = new File(logdir.getPath() + "/pklog.txt");
        if (!logfile.exists()){try{
    	       logfile.createNewFile();
	       } catch (IOException e) {
  		     e.printStackTrace();
	    }}

        logConfigurator.setFileName(logfile.getPath());
        logConfigurator.setRootLevel(Level.DEBUG);
        // 	Set log level of a specific logger
        logConfigurator.setLevel("org.apache", Level.ERROR);
        logConfigurator.setFilePattern("%d - [%p::%c] - %m%n");
        logConfigurator.configure();
		
    	ErrorReporter.bindReporter(getApplicationContext());
		super.onCreate();
	}

}
