package com.projectkaiser.app_android.fragments.main;

import java.util.ArrayList;
import java.util.List;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;
import com.projectkaiser.app_android.EditIssueActivity;
import com.projectkaiser.app_android.R;
import com.projectkaiser.app_android.adapters.IssuesArrayAdapter;
import com.projectkaiser.app_android.bl.BL;
import com.projectkaiser.app_android.bl.events.*;
import com.projectkaiser.app_android.bl.local.ILocalBL;
import com.projectkaiser.app_android.bl.obj.MRemoteNotSyncedIssue;
import com.projectkaiser.app_android.consts.ActivityReq;
import com.projectkaiser.mobile.sync.MIssue;
import com.projectkaiser.mobile.sync.MLocalIssue;
import com.projectkaiser.app_android.misc.SwipeDismissListViewTouchListener;

public class LocalTasksFragment extends IssuesListAbstractFragment implements
		IGlobalAppEventsListener {
	/**
	 * Returns a new instance of this fragment for the given section number.
	 */

	List<MIssue> m_tasks = null;

	public static LocalTasksFragment newInstance() {
		LocalTasksFragment fragment = new LocalTasksFragment();
		fragment.initStateData();
		return fragment;
	}

	@Override
	protected boolean isFiltersSupported() {
		return true;
	}

	public View getViewByPosition(int pos, ListView listView) {
		final int firstListItemPosition = listView.getFirstVisiblePosition();
		final int lastListItemPosition = firstListItemPosition
				+ listView.getChildCount() - 1;

		if (pos < firstListItemPosition || pos > lastListItemPosition) {
			return listView.getAdapter().getView(pos, null, listView);
		} else {
			final int childIndex = pos - firstListItemPosition;
			return listView.getChildAt(childIndex);
		}
	}

	private class MyDismissCallbacks implements
			SwipeDismissListViewTouchListener.DismissCallbacks {
		public void onDismiss(ListView listView, int[] reverseSortedPositions) {

			IssuesArrayAdapter adapter = new IssuesArrayAdapter(getRootView()
					.getContext(), getIssuesList());
			if (m_tasks == null)
				return;
			for (int position : reverseSortedPositions) {
				int realpos = position;
				if (removedItem != null) {
					if (realpos == removedItem.dismissPosition)
						return;
					if (realpos > removedItem.dismissPosition) {
						realpos = realpos - 1;
					}
				}
				Long modif = 0L;
				if (realpos <= -1) {
					return;
				}
				View v = getViewByPosition(realpos, listView);
				int itemHeight = getViewByPosition(listView.getFirstVisiblePosition(), listView).getHeight();
				int top = (v == null) ? 0 : (v.getTop() - listView
						.getPaddingTop());

				if (m_tasks.size()<=realpos) return;
				MIssue m_details = m_tasks.get(realpos);
				if (m_details == null)
					return;
				modif = m_details.getModified();
				CompleteItem(realpos);
				removedItem = new LocalRemovedItem();
				removedItem.dismissPosition = realpos;
				removedItem.Modified = modif;
				adapter.setremovedItem(removedItem);
				getListView().setAdapter(adapter);
				// getListView().setSelection(realpos);
				if (top > listView.getBottom() - itemHeight)
					top = listView.getBottom() - itemHeight - 32;
				listView.setSelectionFromTop(realpos, top);
			}
		}

		public boolean canDismiss(int position) {
			return true;
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View v = super.onCreateView(inflater, container, savedInstanceState);
		((TextView) getRootView().findViewById(R.id.emptyText))
				.setText(getString(R.string.no_local_issues));
		SwipeDismissListViewTouchListener touchListener = new SwipeDismissListViewTouchListener(
				getListView(), new MyDismissCallbacks());
		getListView().setOnTouchListener(touchListener);
		getListView().setOnScrollListener(touchListener.makeScrollListener());
		return v;
	}

	@Override
	public void onEvent(AppEvent event) {
		if (event instanceof DataSyncStarted) {
			showProgress();
		} else if (event instanceof RefreshLists) {
			hideProgress();
			refresh();
		} else if (event instanceof LocalTaskAdded) {
			refresh();
		} else if (event instanceof LocalTaskRevert) {
			int pos = ((LocalTaskRevert) event).getPosition();
			View v = getViewByPosition(pos, getListView());
			int top = (v == null) ? 0 : (v.getTop() - getListView()
					.getPaddingTop());
			refresh();
			getListView().setSelectionFromTop(pos, top);
		}

	}

	@Override
	protected boolean CompleteItem(int position) {
		MIssue m_details = m_tasks.get(position);

		if (m_details instanceof MLocalIssue) {

			BL.getLocal(getActivity().getApplicationContext()).completeTask(
					(MLocalIssue) m_details);
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
		for (MLocalIssue li : bl.getLocalTasks(getTaskListType())) {
			m_tasks.add(li);
		}
		return m_tasks;
	}

	@Override
	protected void onIssueClick(int position) {
		Intent i = new Intent(getRootView().getContext(),
				EditIssueActivity.class);
//		i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		i.putExtra(MIssue.class.getName(), m_tasks.get(position));
		i.putExtra("SRVNAME", "");
		startActivity(i);
	}

}
