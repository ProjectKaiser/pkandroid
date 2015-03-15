package com.projectkaiser.app_android.fragments.main;

import java.util.ArrayList;
import java.util.List;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import com.projectkaiser.app_android.misc.*;
import com.projectkaiser.app_android.R;
import com.projectkaiser.app_android.adapters.IssuesArrayAdapter;
import com.projectkaiser.app_android.bl.events.IGlobalAppEventsListener;
import com.projectkaiser.app_android.bl.events.IGlobalAppEventsProvider;
import com.projectkaiser.app_android.settings.SrvConnectionId;
import com.projectkaiser.mobile.sync.MIssue;
import com.projectkaiser.app_android.misc.SwipeDismissListViewTouchListener;

public abstract class IssuesListAbstractFragment extends Fragment implements
		IGlobalAppEventsListener, SwipeRefreshLayout.OnRefreshListener {

	private View m_rootView;
	private PKTaskListType mTaskListType;
	
	protected final void refresh() {
		IssuesArrayAdapter adapter = new IssuesArrayAdapter(getRootView().getContext(), getIssuesList());
		getListView().setAdapter(adapter);
	}

	protected abstract List<MIssue> getIssuesList();
	boolean m_progressShown = false;

	protected String getConnectionId() {
		return getArguments().getString(SrvConnectionId.ARG);
	}

	protected final void showProgress() {
		m_rootView.findViewById(R.id.pbIssues).setVisibility(View.GONE);
		m_rootView.findViewById(R.id.emptyText).setVisibility(View.GONE);
		if (isFiltersSupported()) {
			// TODO: Make Edit button visible
		}

		final ListView list = (ListView) m_rootView.findViewById(R.id.lvInbox);
		list.setEmptyView(m_rootView.findViewById(R.id.pbIssues));

		IssuesArrayAdapter adapter = new IssuesArrayAdapter(
				m_rootView.getContext(), new ArrayList<MIssue>());
		list.setAdapter(adapter);

		getProgressLayout().setRefreshing(true);

		m_progressShown = true;
	}

	public void setTaskListType(PKTaskListType _tlType) {
		mTaskListType = _tlType;
		refresh();
		return;
	}

	protected final void hideProgress() {
		getProgressLayout().setRefreshing(false);
		if (!m_progressShown)
			return;
		m_rootView.findViewById(R.id.pbIssues).setVisibility(View.GONE);
		m_rootView.findViewById(R.id.emptyText).setVisibility(View.GONE);

		if (isFiltersSupported()) {
			if (getActivity() != null) {
				getActivity().findViewById(R.id.action_active_tasks).setVisibility(
						View.VISIBLE);
			}
		}

		final ListView list = (ListView) m_rootView.findViewById(R.id.lvInbox);

		list.setEmptyView(m_rootView.findViewById(R.id.emptyText));
	}

	protected final ListView getListView() {
		return (ListView) m_rootView.findViewById(R.id.lvInbox);
	}

	protected final View getRootView() {
		return m_rootView;
	}

	protected void onIssueClick(int position) {

	}

	protected SwipeRefreshLayout getProgressLayout() {
		return (SwipeRefreshLayout) m_rootView.findViewById(R.id.swrInbox);
	}

	protected boolean isFiltersSupported() {
		return false;
	}

	protected boolean isClosedTasksSelected() {
		return (mTaskListType == PKTaskListType.CLOSED);
	}

	protected abstract boolean DeleteItem(int position);
	
	private class MyDismissCallbacks implements SwipeDismissListViewTouchListener.DismissCallbacks {
		public void onDismiss(ListView listView, int[] reverseSortedPositions) {
			IssuesArrayAdapter adapter = new IssuesArrayAdapter(getRootView().getContext(), getIssuesList());
            for (int position : reverseSortedPositions) {
            	// TODO: make delete but need possibility to restore
            	//DeleteItem(position);
            	// adapter.remove(adapter.getItem(position));
            }
            //refresh();
            //adapter.notifyDataSetChanged();
        }		
		public boolean canDismiss(int position){
			return true;
		}
	}
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		m_rootView = inflater.inflate(R.layout.fragment_issues, container,
				false);

		SwipeDismissListViewTouchListener touchListener =
				          new SwipeDismissListViewTouchListener(
				        		  getListView(),
				                  new MyDismissCallbacks());
		
		getListView().setOnTouchListener(touchListener);
		getListView().setOnScrollListener(touchListener.makeScrollListener());
		
		getProgressLayout().setOnRefreshListener(this);
		try {
			if (getActivity() != null) {
				((IGlobalAppEventsProvider) getActivity()).register(this);
			}
		} catch (ClassCastException e) {
			throw new ClassCastException(getActivity().toString()
					+ " must implement IMainMenuCmdProvider");
		}

		getListView().setOnItemClickListener(
				new AdapterView.OnItemClickListener() {
					@Override
					public void onItemClick(AdapterView<?> parentView,
							View childView, int position, long id) {
						onIssueClick(position);
					}
				});

		getListView().setEmptyView(m_rootView.findViewById(R.id.emptyText));

		hideProgress();
		refresh();
		return m_rootView;
	}

	@Override
	public void onRefresh() {
		if(getActivity()!=null){
			((IGlobalAppEventsProvider) getActivity()).syncRequested();
		}
	}
}
