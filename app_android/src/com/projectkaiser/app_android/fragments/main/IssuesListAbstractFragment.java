package com.projectkaiser.app_android.fragments.main;

import java.util.ArrayList;
import java.util.List;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.AbsListView;

import com.projectkaiser.app_android.misc.*;
import com.projectkaiser.app_android.MainActivity;
import com.projectkaiser.app_android.R;
import com.projectkaiser.app_android.adapters.IssuesArrayAdapter;
import com.projectkaiser.app_android.bl.events.IGlobalAppEventsListener;
import com.projectkaiser.app_android.bl.events.IGlobalAppEventsProvider;
import com.projectkaiser.app_android.settings.SrvConnectionId;
import com.projectkaiser.mobile.sync.MIssue;

public abstract class IssuesListAbstractFragment extends Fragment implements
		IGlobalAppEventsListener, SwipeRefreshLayout.OnRefreshListener {

	private View m_rootView;
	private PKTaskListType mTaskListType;
	protected LocalRemovedItem removedItem = null;

	protected final void refresh() {
		IssuesArrayAdapter adapter = new IssuesArrayAdapter(getRootView().getContext(), getIssuesList());
		removedItem = null;
		getListView().setAdapter(adapter);
		((MainActivity)getActivity()).initTaskMenu();
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
	protected abstract boolean CompleteItem(int position);

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		m_rootView = inflater.inflate(R.layout.fragment_issues, container, false);
		
		getProgressLayout().setOnRefreshListener(this);
		try {
			if (getActivity() != null) {
				((IGlobalAppEventsProvider) getActivity()).register(this);
			}
		} catch (ClassCastException e) {
			throw new ClassCastException(getActivity().toString()
					+ " must implement IMainMenuCmdProvider");
		}
/*		
		getListView().setOnScrollListener(new OnScrollListener() {
			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
			}

			@Override
			public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
			    boolean enable = false;
			    if(view != null && view.getChildCount() > 0){
			        // check if the first item of the list is visible
			        boolean firstItemVisible = view.getFirstVisiblePosition() == 0;
			        // check if the top of the first item is visible
			        boolean topOfFirstItemVisible = view.getChildAt(0).getTop() == 0;
			        // enabling or disabling the refresh layout
			        enable = firstItemVisible && topOfFirstItemVisible;
			    }
			    getProgressLayout().setEnabled(enable);
			}
		});
*/

		getListView().setOnItemClickListener(
				new AdapterView.OnItemClickListener() {
					@Override
					public void onItemClick(AdapterView<?> parentView,
							View childView, int position, long id) {
						if (removedItem!=null) {
							if (removedItem.dismissPosition==position) return;
						}
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
