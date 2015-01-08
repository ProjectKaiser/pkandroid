package com.projectkaiser.app_android.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class SyncBootReceiver extends BroadcastReceiver {

    SyncAlarmReceiver alarm = new SyncAlarmReceiver();
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED"))
        {
            alarm.setAlarm(context);
        }
    }

}
