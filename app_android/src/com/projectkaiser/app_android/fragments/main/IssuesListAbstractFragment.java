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
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;

import com.projectkaiser.app_android.R;
import com.projectkaiser.app_android.adapters.IssuesArrayAdapter;
import com.projectkaiser.app_android.bl.events.IGlobalAppEventsListener;
import com.projectkaiser.app_android.bl.events.IGlobalAppEventsProvider;
import com.projectkaiser.app_android.settings.SrvConnectionId;
import com.projectkaiser.mobile.sync.MIssue;

public abstract class IssuesListAbstractFragment extends Fragment implements IGlobalAppEventsListener, SwipeRefreshLayout.OnRefreshListener {
	
	private View m_rootView;
	
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
		if (isFiltersSupported())
			m_rootView.findViewById(R.id.rgFilters).setVisibility(View.GONE);

		final ListView list = (ListView)m_rootView.findViewById(R.id.lvInbox);		
		list.setEmptyView(m_rootView.findViewById(R.id.pbIssues));
		
		IssuesArrayAdapter adapter = new IssuesArrayAdapter(m_rootView.getContext(), new ArrayList<MIssue>());
		list.setAdapter(adapter);
		
		getProgressLayout().setRefreshing(true);

		m_progressShown = true;
	}
	
	protected final void hideProgress() {
		getProgressLayout().setRefreshing(false);
		if (!m_progressShown)
			return;
		m_rootView.findViewById(R.id.pbIssues).setVisibility(View.GONE);
		m_rootView.findViewById(R.id.emptyText).setVisibility(View.GONE);
		
		if (isFiltersSupported())
			m_rootView.findViewById(R.id.rgFilters).setVisibility(View.VISIBLE);

		final ListView list = (ListView)m_rootView.findViewById(R.id.lvInbox);
		
		list.setEmptyView(m_rootView.findViewById(R.id.emptyText));
	}
	
	protected final ListView getListView() {
		return (ListView)m_rootView.findViewById(R.id.lvInbox); 
	}
	
	protected final View getRootView() {
		return m_rootView;
	}
	
	protected void onIssueClick(int position) {
		
	} 
	
	protected SwipeRefreshLayout getProgressLayout() {
		return (SwipeRefreshLayout)m_rootView.findViewById(R.id.swrInbox);
	}
	
	protected boolean isFiltersSupported() {
		return false;
	}
	
	protected boolean isClosedTasksSelected() {
		RadioButton rbnClosed = (RadioButton)m_rootView.findViewById(R.id.rbnClosed);
		return rbnClosed.isChecked();
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		m_rootView = inflater.inflate(R.layout.fragment_issues, container,
				false);
		
		getProgressLayout().setOnRefreshListener(this);
		
		RadioGroup rgFilters = (RadioGroup)m_rootView.findViewById(R.id.rgrFilters);
		if (isFiltersSupported()) {
			rgFilters.setOnCheckedChangeListener(new OnCheckedChangeListener() {
				@Override
				public void onCheckedChanged(RadioGroup arg0, int index) {
					refresh();
				}
			});
		} else
			rgFilters.setVisibility(View.GONE);
        
        try {
            ((IGlobalAppEventsProvider) getActivity()).register(this);
        } catch (ClassCastException e) {
            throw new ClassCastException(getActivity().toString()
                    + " must implement IMainMenuCmdProvider");
        }	

        getListView().setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parentView, View childView, int position,
					long id) {
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
		 ((IGlobalAppEventsProvider) getActivity()).syncRequested();		
	}
}
