package com.projectkaiser.app_android.fragments.main;

import java.util.ArrayList;
import java.util.List;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.projectkaiser.app_android.R;
import com.projectkaiser.app_android.ViewIssueActivity;
import com.projectkaiser.app_android.bl.events.AppEvent;
import com.projectkaiser.app_android.bl.events.RefreshLists;
import com.projectkaiser.app_android.bl.events.DataSyncStarted;
import com.projectkaiser.app_android.bl.events.IGlobalAppEventsListener;
import com.projectkaiser.app_android.settings.SessionManager;
import com.projectkaiser.mobile.sync.MIssue;
import com.projectkaiser.mobile.sync.MRemoteIssue;


public class InboxFragment extends IssuesListAbstractFragment implements IGlobalAppEventsListener {
	
	List<MIssue> m_issues;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View v = super.onCreateView(inflater, container, savedInstanceState); 
		((TextView)getRootView().findViewById(R.id.emptyText)).setText(getString(R.string.no_remote_issues));
		return v; 
	}

	@Override
	protected boolean DeleteItem(int position){
		return false;
	}

	@Override
	protected List<MIssue> getIssuesList() {
		m_issues = new ArrayList<MIssue>();
		
		SessionManager sm = SessionManager.get(this);
		for (MRemoteIssue ri: sm.getIssues(getConnectionId()).getItems()) 
			m_issues.add(ri);
		
		return m_issues;
	}
	
	
	@Override
	public void onEvent(AppEvent event) {
		if (event instanceof DataSyncStarted) {
			showProgress();
		} else if (event instanceof RefreshLists) {
			hideProgress();
			refresh();
		}
	}
	
	@Override
	protected void onIssueClick(int position) {
        Intent i = new Intent(getRootView().getContext(), ViewIssueActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        i.putExtra(MIssue.class.getName(),  m_issues.get(position));
        getRootView().getContext().startActivity(i);
	}
	
}
