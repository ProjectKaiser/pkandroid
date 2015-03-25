package com.projectkaiser.app_android.fragments.main;

import java.util.ArrayList;
import java.util.List;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Button;
import android.widget.Toast;
import android.view.View.OnClickListener;
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

public class LocalTasksFragment extends IssuesListAbstractFragment implements
		IGlobalAppEventsListener {
	private MIssue justCompletedIssue = null;
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
		((TextView) getRootView().findViewById(R.id.emptyText))
				.setText(getString(R.string.no_local_issues));
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

	private void showToast(MIssue m_detail, String initText) {
		justCompletedIssue = m_detail;
		Toast toastTask = Toast.makeText(getActivity().getApplicationContext(),
				initText, Toast.LENGTH_LONG);
		toastTask.show();
	}

	@Override
	protected boolean CompleteItem(int position) {
		MIssue m_details = m_tasks.get(position);
		if (m_details instanceof MLocalIssue) {
			BL.getLocal(getActivity().getApplicationContext()).completeTask(
					(MLocalIssue) m_details);
			if (m_details.getState() == 0) {
				showToast(m_details, getString(R.string.jcompleted));
			} else {
				showToast(m_details, getString(R.string.jresumed));
			}
			return true;
		} else
			return false;

	}

	@Override
	protected boolean DeleteItem(int position) {
		MIssue m_details = m_tasks.get(position);
		if (m_details instanceof MLocalIssue)
			BL.getLocal(getActivity().getApplicationContext()).deleteTask(
					(MLocalIssue) m_details);
		else if (m_details instanceof MRemoteNotSyncedIssue)
			BL.getLocal(getActivity().getApplicationContext()).deleteTask(
					(MRemoteNotSyncedIssue) m_details);
		return true;
	}

	@Override
	protected List<MIssue> getIssuesList() {
		if (getActivity() == null)
			return null;
		ILocalBL bl = BL.getLocal(getActivity().getApplicationContext());
		m_tasks = new ArrayList<MIssue>();
		for (MLocalIssue li : bl
				.getLocalTasks(isClosedTasksSelected() ? TasksFilter.CLOSED
						: TasksFilter.ACTIVE))
			m_tasks.add(li);
		if (!isClosedTasksSelected()) {
			for (MRemoteNotSyncedIssue nsi : bl.getNotSyncedTasks())
				m_tasks.add(nsi);
		}
		return m_tasks;
	}

	@Override
	protected void onIssueClick(int position) {
		Intent i = new Intent(getRootView().getContext(),
				EditIssueActivity.class);
		i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		i.putExtra(MIssue.class.getName(), m_tasks.get(position));
		i.putExtra("SRVNAME", "");
		startActivity(i);
	}

}
