package com.projectkaiser.app_android.fragments.main;

import java.util.ArrayList;
import java.util.List;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.projectkaiser.app_android.EditIssueActivity;
import com.projectkaiser.app_android.R;
import com.projectkaiser.app_android.bl.BL;
import com.projectkaiser.app_android.bl.events.AppEvent;
import com.projectkaiser.app_android.bl.events.DataSyncStarted;
import com.projectkaiser.app_android.bl.events.IGlobalAppEventsListener;
import com.projectkaiser.app_android.bl.events.LocalTaskAdded;
import com.projectkaiser.app_android.bl.events.RefreshLists;
import com.projectkaiser.app_android.bl.local.ILocalBL;
import com.projectkaiser.app_android.bl.local.TasksFilter;
import com.projectkaiser.app_android.bl.obj.MRemoteNotSyncedIssue;
import com.projectkaiser.mobile.sync.MIssue;
import com.projectkaiser.mobile.sync.MLocalIssue;


public class LocalTasksFragment extends IssuesListAbstractFragment implements IGlobalAppEventsListener {
	/**
	 * Returns a new instance of this fragment for the given section number.
	 */
	
	List<MIssue> m_tasks = null;
	
	public static LocalTasksFragment newInstance() {
		LocalTasksFragment fragment = new LocalTasksFragment();
		return fragment;
	}
	
	@Override
	protected boolean isFiltersSupported() {
		return true;
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View v = super.onCreateView(inflater, container, savedInstanceState); 
		((TextView)getRootView().findViewById(R.id.emptyText)).setText(getString(R.string.no_local_issues));
		return v; 
	}
	
	@Override
	public void onEvent(AppEvent event) {
		if (event instanceof DataSyncStarted) {
			showProgress();
		} else if (event instanceof RefreshLists) {
			hideProgress();
			refresh();
		} else if (event instanceof LocalTaskAdded)
			refresh();
	}
	
	@Override
	protected List<MIssue> getIssuesList() {
		if (getActivity()==null)  return null;
		ILocalBL bl = BL.getLocal(getActivity().getApplicationContext());
		m_tasks = new ArrayList<MIssue>(); 
		for (MLocalIssue li: bl.getLocalTasks( isClosedTasksSelected()?TasksFilter.CLOSED:TasksFilter.ACTIVE )) 
			m_tasks.add(li);
		if (!isClosedTasksSelected()) {
			for (MRemoteNotSyncedIssue nsi: bl.getNotSyncedTasks())
				m_tasks.add(nsi);
		}
		return m_tasks;
	}

	@Override
	protected void onIssueClick(int position) {
        Intent i = new Intent(getRootView().getContext(), EditIssueActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        i.putExtra(MIssue.class.getName(), m_tasks.get(position));
		startActivity(i);	
	}
	
	
}
