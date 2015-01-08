package com.projectkaiser.app_android.fragments.issue;

import java.util.List;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import com.projectkaiser.app_android.R;
import com.projectkaiser.app_android.adapters.CommentsArrayAdapter;
import com.projectkaiser.app_android.bl.BL;
import com.projectkaiser.app_android.bl.local.ILocalBL;
import com.projectkaiser.app_android.bl.obj.MRemoteNotSyncedIssue;
import com.projectkaiser.app_android.bl.obj.MRemoteSyncedIssue;
import com.projectkaiser.app_android.fragments.issue.intf.ITaskDetailsListener;
import com.projectkaiser.app_android.fragments.issue.intf.ITaskDetailsProvider;
import com.projectkaiser.app_android.settings.SessionManager;
import com.projectkaiser.app_android.settings.SrvConnectionBaseData;
import com.projectkaiser.mobile.sync.MComment;
import com.projectkaiser.mobile.sync.MIssue;
import com.projectkaiser.mobile.sync.MLocalIssue;
import com.projectkaiser.mobile.sync.MRemoteIssue;


public class CommentsFragment extends Fragment implements ITaskDetailsListener, OnClickListener {
	
	View m_rootView;
	
	MIssue m_details;

	public static CommentsFragment newInstance() {
		CommentsFragment fragment = new CommentsFragment();
		return fragment;
	}

	public CommentsFragment() {
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		m_rootView = inflater.inflate(R.layout.fragment_comments, container,
				false);
		
		Button btn = (Button)m_rootView.findViewById(R.id.btnNewComment);
		EditText newComment = (EditText)m_rootView.findViewById(R.id.edtComment);
		btn.setEnabled(false);
		btn.setOnClickListener(this);
		newComment.setEnabled(false);
		
        try {
            ((ITaskDetailsProvider) getActivity()).registerListener(this);
        } catch (ClassCastException e) {
            throw new ClassCastException(getActivity().toString()
                    + " must implement ITaskDetailsProvider");
        }	
		return m_rootView;
	}
	
	private void refresh() {
		final ListView listView = (ListView)m_rootView.findViewById(R.id.lvComments);
		
		List<MComment> comments;
		ILocalBL bl = BL.getLocal(getActivity().getApplicationContext());
		
		if (m_details instanceof MLocalIssue)
			comments = bl.getComments((MLocalIssue)m_details);
		else if (m_details instanceof MRemoteNotSyncedIssue)
			comments = bl.getComments((MRemoteNotSyncedIssue)m_details);
		else if (m_details instanceof MRemoteIssue)
			comments = bl.getComments((MRemoteIssue)m_details);
		else
			throw new RuntimeException("Issue type not supported");
		
		CommentsArrayAdapter adapter = new CommentsArrayAdapter(m_rootView.getContext(), comments);
		listView.setAdapter(adapter);
	}
	
	@Override
	public void taskLoaded(MIssue details) {
		Button btn = (Button)m_rootView.findViewById(R.id.btnNewComment);
		EditText newComment = (EditText)m_rootView.findViewById(R.id.edtComment);
		m_details = details;
		refresh();

		btn.setEnabled(true);
		newComment.setEnabled(true);
	}
	
	public void newCommentClick(View view) {
		EditText newComment = (EditText)m_rootView.findViewById(R.id.edtComment);
		if (newComment.getText().length() > 0) {
			SessionManager sm = SessionManager.get(getActivity());
			MComment comment = new MComment();
			comment.setCreated(System.currentTimeMillis());
			comment.setDescription(newComment.getText().toString());
			
			if (m_details instanceof MRemoteSyncedIssue) {
				MRemoteSyncedIssue ri = (MRemoteSyncedIssue)m_details;
				String connId = ri.getSrvConnId();
				SrvConnectionBaseData base = sm.getBaseData(connId);
				comment.setCreator(base.getUserId());
				comment.setCreatorName(base.getUserName());
			} else if (m_details instanceof MRemoteNotSyncedIssue) {
				MRemoteNotSyncedIssue ri = (MRemoteNotSyncedIssue)m_details;
				String connId = ri.getSrvConnId();
				SrvConnectionBaseData base = sm.getBaseData(connId);
				comment.setCreator(base.getUserId());
				comment.setCreatorName(base.getUserName());
			}
			
			
			ILocalBL bl = BL.getLocal(getActivity().getApplicationContext());
			if (m_details instanceof MLocalIssue)
				bl.addComment((MLocalIssue)m_details, comment);
			else if (m_details instanceof MRemoteNotSyncedIssue)
				bl.addComment((MRemoteNotSyncedIssue)m_details, comment);
			else if (m_details instanceof MRemoteIssue)
				bl.addComment((MRemoteIssue)m_details, comment);
			
			newComment.setText("");
			refresh();
		}
	}
	
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
        case R.id.btnNewComment:
        	newCommentClick(v);
            break;
        }
		
	}
	
	
	
}
