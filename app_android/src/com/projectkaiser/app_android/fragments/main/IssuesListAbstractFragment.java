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
import com.projectkaiser.app_android.MainActivity;
import com.projectkaiser.app_android.R;
import com.projectkaiser.app_android.adapters.IssuesArrayAdapter;
import com.projectkaiser.app_android.bl.events.IGlobalAppEventsListener;
import com.projectkaiser.app_android.bl.events.IGlobalAppEventsProvider;
import com.projectkaiser.app_android.settings.SrvConnectionId;
import com.projectkaiser.mobile.sync.MIssue;
import com.projectkaiser.app_android.fragments.main.IssueListData;
import com.projectkaiser.app_android.fragments.main.IRetainedFragment;

public abstract class IssuesListAbstractFragment extends Fragment implements
		IGlobalAppEventsListener, SwipeRefreshLayout.OnRefreshListener, IRetainedFragment {

	private View m_rootView;
	private PKTaskListType mTaskListType;
	protected LocalRemovedItem removedItem = null;
    private IssueListData statedata;

    public void initStateData(){
    	statedata = new  IssueListData();   	
    }
    
	@Override
	public IssueListData getData() {
        return statedata;
    }

	protected final void refresh() {
		ListView lv = getListView();
		if (lv == null || getIssuesList()==null)
			return;
		IssuesArrayAdapter adapter = new IssuesArrayAdapter(getRootView()
				.getContext(), getIssuesList());
		removedItem = null;
		lv.setAdapter(adapter);
	}

	
	protected abstract List<MIssue> getIssuesList();
	boolean m_progressShown = false;

	protected String getConnectionId() {
		return getArguments().getString(SrvConnectionId.ARG);
	}

	protected final void showProgress() {
		m_rootView.findViewById(R.id.pbIssues).setVisibility(View.GONE);
		m_rootView.findViewById(R.id.emptyText).setVisibility(View.GONE);

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
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // retain this fragment
        setRetainInstance(true);
    }
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		m_rootView = inflater.inflate(R.layout.fragment_issues, container,
				false);
		
		getProgressLayout().setOnRefreshListener(this);
		try {
			if (getActivity() != null) {
				((IGlobalAppEventsProvider) getActivity()).register(this);
			}
		} catch (ClassCastException e) {
			throw new ClassCastException(getActivity().toString()
					+ " must implement IMainMenuCmdProvider");
		}

		ListView lv = getListView();
		if (lv != null) {
			lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> parentView,
						View childView, int position, long id) {
					if (removedItem != null) {
						if (removedItem.dismissPosition == position)
							return;
					}
					onIssueClick(position);
				}
			});

			lv.setEmptyView(m_rootView.findViewById(R.id.emptyText));

			hideProgress();
			refresh();
		}
		return m_rootView;
	}

	@Override
	public void onRefresh() {
		if (getActivity() != null) {
			((IGlobalAppEventsProvider) getActivity()).syncRequested();
		}
	}
}
