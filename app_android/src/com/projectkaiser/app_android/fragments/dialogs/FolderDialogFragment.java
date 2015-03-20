package com.projectkaiser.app_android.fragments.dialogs;

import java.util.ArrayList;

import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.LinearLayout;
import android.widget.Spinner;

import com.projectkaiser.app_android.*;
import com.projectkaiser.app_android.R;
import com.projectkaiser.app_android.adapters.FoldersDlgAdapter;
import com.projectkaiser.app_android.settings.SessionManager;
import com.projectkaiser.app_android.settings.SrvConnectionBaseData;
import com.projectkaiser.mobile.sync.MDataHelper;
import com.projectkaiser.mobile.sync.MDigestedArray;
import com.projectkaiser.mobile.sync.MMyProject;
import com.projectkaiser.mobile.sync.MWorkingSet;
import com.projectkaiser.mobile.sync.MWorkingSets;

public class FolderDialogFragment extends DialogFragment {

	View m_view;

	public interface FolderListener {
		void onLocalSelected();

		void onFolderSelected(String connectionId, Long projectId, Long folderId);
	}

	FolderListener m_listener;

	public FolderListener getListener() {
		return m_listener;
	}

	public void setListener(FolderListener listener) {
		m_listener = listener;
	}

	public FolderDialogFragment() {

	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {

		Dialog dialog = super.onCreateDialog(savedInstanceState);
		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		return dialog;

	}

	private SessionManager getSessionManager() {
		return SessionManager.get(this.getActivity());
	}

	ArrayList<String> m_connectionIds = new ArrayList<String>();

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		m_view = inflater.inflate(R.layout.dialog_folder, container, false);

		final SessionManager sm = getSessionManager();
		final Spinner cmbConnections = (Spinner) m_view
				.findViewById(R.id.cmbConnection);

		ArrayList<CharSequence> arrCons = new ArrayList<CharSequence>();

		int selectedIndex = 0;
		String lastConId = sm.getLatestFolderDlgConnectionId();

		int idx  = 0;
		for (String connId : sm.getConnections()) {
			SrvConnectionBaseData base = sm.getBaseData(connId);
			m_connectionIds.add(connId);
			arrCons.add(base.getServerName());
			EditIssueActivity mainActivity = (EditIssueActivity) getActivity();
			if (connId.equals(mainActivity.getServerName())){
//			if (lastConId != null && lastConId.equals(connId))
				selectedIndex = idx;
			}
			idx = idx + 1;
		}
		
		ArrayAdapter<CharSequence> adptCons = new ArrayAdapter<CharSequence>(
				getActivity().getBaseContext(),
				android.R.layout.simple_spinner_item, arrCons);
		adptCons.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		cmbConnections.setAdapter(adptCons);
		cmbConnections.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> arg0, View view,
					int position, long arg3) {
				showWorkingSets(position);
				sm.updateLastFolderDlgConnectionId(m_connectionIds
						.get(position));
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
			}
		});

		cmbConnections.setSelection(selectedIndex);
		showWorkingSets(selectedIndex);

//		if (adptCons.getCount() == 1)
		cmbConnections.setVisibility(View.GONE);

		// ////////////////////////////////////////////////////
		// / Buttons

		((Button) m_view.findViewById(R.id.btnCancel))
				.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View arg0) {
						getDialog().dismiss();
					}
				});

		return m_view;
	}

	private void showWorkingSets(final int conIndex) {

		final String conId = m_connectionIds.get(conIndex);

		final Spinner cmbWorkingSet = (Spinner) m_view
				.findViewById(R.id.cmbWorkingSet);
		final LinearLayout pnlWorkingSet = (LinearLayout) m_view
				.findViewById(R.id.pnlWorkingSet);
		final ArrayList<CharSequence> arrWS = new ArrayList<CharSequence>();

		arrWS.add(getString(R.string.all_projects));

		SessionManager sm = getSessionManager();
		MWorkingSets workingSets = sm.getWorkingSets(conId);

		for (MWorkingSet ws : workingSets.getItems())
			arrWS.add(ws.getName());

		ArrayAdapter<CharSequence> adptWs = new ArrayAdapter<CharSequence>(
				getActivity().getBaseContext(),
				android.R.layout.simple_spinner_item, arrWS);
		adptWs.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		cmbWorkingSet.setAdapter(adptWs);
		cmbWorkingSet.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> arg0, View view, int pos,
					long arg3) {
				showProjects(conIndex);
				getSessionManager().updateLastWorkingSet(conId,
						arrWS.get(pos).toString());
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
			}
		});

		int selected = -1;
		String lastWorkingSet = sm.getLatestWorkingSet(conId);
		if (lastWorkingSet != null) {
			for (int i = 0; i < arrWS.size(); i++) {
				if (arrWS.get(i).equals(lastWorkingSet)) {
					selected = i;
					break;
				}
			}
		}
		if (selected == -1) {
			String defaultWorkingSet = workingSets.getDefaultWorkingSet();
			if (defaultWorkingSet != null) {
				for (int i = 0; i < arrWS.size(); i++) {
					if (arrWS.get(i).equals(defaultWorkingSet)) {
						selected = i;
						break;
					}
				}
			}
		}

		if (selected == -1)
			selected = 0;

		cmbWorkingSet.setSelection(selected);
		showProjects(conIndex);

		pnlWorkingSet.setVisibility(adptWs.getCount() == 1 ? View.GONE
				: View.VISIBLE);

	}

	private void showProjects(int conIndex) {
		final String conId = m_connectionIds.get(conIndex);
		final Spinner cmbWorkingSet = (Spinner) m_view
				.findViewById(R.id.cmbWorkingSet);
		final ExpandableListView elvFolders = (ExpandableListView) m_view
				.findViewById(R.id.elvFolders);
		int pos = cmbWorkingSet.getSelectedItemPosition();
		if (pos < 0)
			return;

		final ArrayList<MMyProject> wsProjects = new ArrayList<MMyProject>();

		if (pos == 0) { // All projects

			MDigestedArray<MMyProject> projects = getSessionManager()
					.getMyProjects(conId);
			for (MMyProject p : projects.getItems()) {
				wsProjects.add(p);
			}

		} else { // Working Set

			MWorkingSets workingSets = getSessionManager()
					.getWorkingSets(conId);
			final MWorkingSet ws = workingSets.getItems().get(pos - 1);
			MDataHelper hlp = new MDataHelper(getActivity()
					.getApplicationContext(), conId);
			for (Long projectId : ws.getProjects()) {
				MMyProject p = hlp.findProject(projectId);
				if (p != null)
					wsProjects.add(p);
			}

		}

		FoldersDlgAdapter adp = new FoldersDlgAdapter(getActivity()
				.getBaseContext(), wsProjects);
		elvFolders.setAdapter(adp);
		if (adp.getGroupCount() > 0)
			elvFolders.expandGroup(0);
		elvFolders.setOnChildClickListener(new OnChildClickListener() {
			@Override
			public boolean onChildClick(ExpandableListView parent, View v,
					int groupPosition, int childPosition, long id) {
				if (m_listener != null)
					m_listener.onFolderSelected(conId,
							wsProjects.get(groupPosition).getId(), id);
				getDialog().dismiss();
				return true;
			}
		});
	}

}
