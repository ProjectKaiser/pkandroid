package com.projectkaiser.app_android.services;

import java.util.ArrayList;

import com.projectkaiser.app_android.bl.obj.SelectedIssuesFolder;

import android.content.Context;

public class PkAlarmManager {
	
	public static boolean bNewEditActivity = false;
	
	public static void NewEditStarted(){
		bNewEditActivity = true;
	}
	public static void NewEditStopped(){
		bNewEditActivity = false;
	}
	
	public static String GetFolderName(String source_name){
		//--Start Temporary solution: replace Руководитель to Корневой проект
		if (source_name.contains("Руководитель")) {
			return "Корневой проект";
		} else if (source_name.contains("Managed by")) {
			return "Root project";
		} else
	   //--End Temporary solution: replace Руководитель to Корневой проект
			return source_name; 
	}
	
	public static void activityStarted(Context ctx) {
//		int n = (new SessionManager(ctx)).incActivityCounter(1);
//		SyncAlarmReceiver receiver = new SyncAlarmReceiver();
//		if (receiver.isAlarmEnabled(ctx))
//			receiver.cancelAlarm(ctx);	
	}

	public static void activityStopped(Context ctx) {
//		int n = (new SessionManager(ctx)).incActivityCounter(-1); 
//		if (n == 0) {
//			SyncAlarmReceiver receiver = new SyncAlarmReceiver();
//			if (!receiver.isAlarmEnabled(ctx))
//				receiver.setAlarm(ctx);
//		}
	}

}
