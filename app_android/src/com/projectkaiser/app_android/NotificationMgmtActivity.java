package com.projectkaiser.app_android;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.projectkaiser.app_android.adapters.ConnectionsAdapter;
import com.projectkaiser.app_android.settings.SessionManager;

public class NotificationMgmtActivity extends ActionBarActivity {

	List<String> m_delay = new ArrayList<String>();
	private int due_task_value = -1;
	
	SessionManager m_sessionManager = null;
	
	private void turnOffDue() {
		m_sessionManager.setShowDueTask(due_task_value);
	}
	
	private void update(){
		due_task_value = m_sessionManager.getShowDueTask();
	}
	
	@Override
	public void onBackPressed() {
    	Intent i = new Intent(getApplicationContext(), MainActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        getApplicationContext().startActivity(i);
        finish();
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		m_sessionManager = SessionManager.get(this);
//		setContentView(R.layout.notification_settings_activity);
		update();
	}

}
