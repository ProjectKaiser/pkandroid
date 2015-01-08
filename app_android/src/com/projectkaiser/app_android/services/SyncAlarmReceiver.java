package com.projectkaiser.app_android.services;

import com.projectkaiser.app_android.settings.SessionManager;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.SystemClock;
import android.support.v4.content.WakefulBroadcastReceiver;

public class SyncAlarmReceiver extends WakefulBroadcastReceiver {
	
	public final static int SYNC_INTERVAL_MILLIS = 10*1000;


    @Override
    public void onReceive(Context context, Intent intent) {   
        Intent service = new Intent(context, SyncService.class);
        startWakefulService(context, service);
    }

    /**
     * Sets a repeating alarm that runs once a day at approximately 8:30 a.m. When the
     * alarm fires, the app broadcasts an Intent to this WakefulBroadcastReceiver.
     * @param context
     */
    public void setAlarm(Context context) {
		SessionManager sm = SessionManager.get(context);
    	AlarmManager alarmMgr = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, SyncAlarmReceiver.class);
        PendingIntent alarmIntent = PendingIntent.getBroadcast(context, 0, intent, 0);

        int intervalMin = sm.getSyncIntervalMin();
        if (intervalMin <= 0)
        	return;
        
        int intervalSeconds = intervalMin * 60;
        
        alarmMgr.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,  
        		SystemClock.elapsedRealtime(), intervalSeconds*1000, alarmIntent);
        
        ComponentName receiver = new ComponentName(context, SyncBootReceiver.class);
        PackageManager pm = context.getPackageManager();

        pm.setComponentEnabledSetting(receiver,
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP);           
    }

    public boolean isAlarmEnabled(Context context) {

        return (PendingIntent.getBroadcast(context, 0, 
        		new Intent(context, SyncAlarmReceiver.class), 
                PendingIntent.FLAG_NO_CREATE) != null);
    	
//    	ComponentName receiver = new ComponentName(context, SyncBootReceiver.class);
//        PackageManager pm = context.getPackageManager();
//        int state = pm.getComponentEnabledSetting(receiver);
//        return (state == PackageManager.COMPONENT_ENABLED_STATE_ENABLED);
    }
    
    /**
     * Cancels the alarm.
     * @param context
     */
    public void cancelAlarm(Context context) {
    	
    	AlarmManager alarmMgr = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, SyncAlarmReceiver.class);
        PendingIntent alarmIntent = PendingIntent.getBroadcast(context, 0, intent, 0);

        alarmMgr.cancel(alarmIntent);
        
        // Disable {@code SampleBootReceiver} so that it doesn't automatically restart the 
        // alarm when the device is rebooted.
        ComponentName receiver = new ComponentName(context, SyncBootReceiver.class);
        PackageManager pm = context.getPackageManager();

        pm.setComponentEnabledSetting(receiver,
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP);
    }

}
