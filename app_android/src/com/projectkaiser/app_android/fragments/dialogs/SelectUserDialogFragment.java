package com.projectkaiser.app_android.fragments.dialogs;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.projectkaiser.app_android.R;
import com.projectkaiser.app_android.adapters.TeamMembersAdapter;
import com.projectkaiser.app_android.bl.obj.SelectedIssuesFolder;
import com.projectkaiser.app_android.settings.SessionManager;
import com.projectkaiser.mobile.sync.MTeamMember;

public class SelectUserDialogFragment extends DialogFragment {
	
	SelectedIssuesFolder m_folder;
	private String m_title = null;
	View m_view;

	public void setTitle( String newTitle ) {
		if (!newTitle.isEmpty()) {
			m_title = newTitle;
		}
		return;
	}
	
	public interface UsersListener {
		void nobodySelected();
		void userSelected(Long userUId);
	}

	UsersListener m_listener;

	public UsersListener getListener() {
		return m_listener;
	}

	public void setListener(UsersListener listener) {
		m_listener = listener;
	}

	public SelectUserDialogFragment(SelectedIssuesFolder folder) {
		m_folder = folder;
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {

		Dialog dialog = super.onCreateDialog(savedInstanceState);
		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		return dialog;

	}
	
	private void updateList() {
		final ListView list = (ListView)m_view.findViewById(R.id.lvUsers);
		AutoCompleteTextView textView = (AutoCompleteTextView)
                m_view.findViewById(R.id.edtUsersFilter);		

		ArrayList<MTeamMember> filtered = new ArrayList<MTeamMember>();
		String filter = textView.getText().toString();
		
		Locale l = Locale.getDefault();

		for (MTeamMember m: m_folder.getProject().getTeam()) {
			if (filter.trim().length()==0 || m.getName().toLowerCase(l).contains(filter.toLowerCase(l)))
				filtered.add(m);
		}
		
		TeamMembersAdapter adapter = new TeamMembersAdapter(m_view.getContext(), filtered);
		list.setAdapter(adapter);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		
		m_view = inflater.inflate(R.layout.dialog_user, container, false);
		final ListView list = (ListView)m_view.findViewById(R.id.lvUsers);		
		TextView txtTitle = (TextView)m_view.findViewById(R.id.txtSelectUserTitle);
		if (!m_title.isEmpty()) txtTitle.setText(m_title);

		updateList();

		List<String> users = new ArrayList<String>();
		for (MTeamMember m:m_folder.getProject().getTeam())
			users.add(m.getName());
		
		ArrayAdapter<String> aca = new ArrayAdapter<String>(this.getActivity().getApplicationContext(),
                android.R.layout.simple_dropdown_item_1line, users);
		AutoCompleteTextView textView = (AutoCompleteTextView)
                m_view.findViewById(R.id.edtUsersFilter);		
        textView.setAdapter(aca);
        textView.addTextChangedListener(new TextWatcher() {			
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
			}
			
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}
			
			@Override
			public void afterTextChanged(Editable s) {
				updateList();
				
			}
		});
        // ////////////////////////////////////////////////////
		// / Buttons

		((Button) m_view.findViewById(R.id.btnSelectMe)).setOnClickListener(
				new OnClickListener() {
					@Override
					public void onClick(View arg0) {
						SessionManager sm = SessionManager.get(SelectUserDialogFragment.this);						
						if (m_listener!=null)
							m_listener.userSelected(sm.getBaseData(m_folder.getConnectionId()).getUserId());
						getDialog().dismiss();
					}
				});

		((Button) m_view.findViewById(R.id.btnSignIn)).setOnClickListener(
				new OnClickListener() {
					@Override
					public void onClick(View arg0) {
						if (m_listener!=null)
							m_listener.nobodySelected();
						getDialog().dismiss();
					}
				});

		((Button) m_view.findViewById(R.id.btnTaskActionCancel)).setOnClickListener(
				new OnClickListener() {
					@Override
					public void onClick(View arg0) {
						getDialog().dismiss();
					}
				});

		list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parentView, View childView, int position,
					long id) {
				m_listener.userSelected(m_folder.getProject().getTeam().get(position).getId());
				getDialog().dismiss();
			}
		});

        return m_view;
	}
	
}
