package com.projectkaiser.app_android.fragments.main;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;

import com.projectkaiser.app_android.R;
import com.projectkaiser.app_android.bl.events.IGlobalAppEventsProvider;
import com.projectkaiser.app_android.fragments.main.IRetainedFragment;

public class NoConnectionFragment extends Fragment implements 
		IRetainedFragment{
	/**
	 * Returns a new instance of this fragment for the given section number.
	 */
    private IssueListData data;
	
	@Override
    public IssueListData getData() {
        return data;
    }

	public static NoConnectionFragment newInstance() {
		NoConnectionFragment fragment = new NoConnectionFragment();
		fragment.data = new IssueListData();
		return fragment;
	}

	public NoConnectionFragment() {
	}
	
	View m_rootView;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		
		m_rootView = inflater.inflate(R.layout.no_connection_fragment, container,
				false);
		
        Button btnConfigure = (Button)m_rootView.findViewById(R.id.btnConfigureConnection);
		btnConfigure.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) {
		        try {
		            ((IGlobalAppEventsProvider) getActivity()).newConnectionRequested();
		        } catch (ClassCastException e) {
		            throw new ClassCastException(getActivity().toString()
		                    + " must implement IMainMenuCmdProvider");
		        }	
			}
		});
		
        return m_rootView;
	}
	
}
