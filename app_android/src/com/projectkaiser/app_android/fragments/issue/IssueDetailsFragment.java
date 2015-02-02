package com.projectkaiser.app_android.fragments.issue;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.webkit.WebView;

import com.projectkaiser.app_android.R;
import com.projectkaiser.app_android.bl.obj.MRemoteNotSyncedIssue;
import com.projectkaiser.app_android.bl.obj.MRemoteSyncedIssue;
import com.projectkaiser.app_android.consts.Priority;
import com.projectkaiser.app_android.fragments.issue.intf.ITaskDetailsListener;
import com.projectkaiser.app_android.fragments.issue.intf.ITaskDetailsProvider;
import com.projectkaiser.app_android.misc.Time;
import com.projectkaiser.app_android.settings.SessionManager;
import com.projectkaiser.mobile.sync.MDataHelper;
import com.projectkaiser.mobile.sync.MFolder;
import com.projectkaiser.mobile.sync.MIssue;
import com.projectkaiser.mobile.sync.MMyProject;
import com.projectkaiser.mobile.sync.MRemoteIssue;
import com.projectkaiser.mobile.sync.MTeamMember;
import com.triniforce.document.elements.TicketDef;
import com.triniforce.dom.*;
import com.triniforce.dom.dom2html.*;
import com.triniforce.wiki.*;

public class IssueDetailsFragment extends Fragment implements ITaskDetailsListener {
	/**
	 * Returns a new instance of this fragment for the given section number.
	 */
	public static IssueDetailsFragment newInstance() {
		IssueDetailsFragment fragment = new IssueDetailsFragment();
		return fragment;
	}

	public IssueDetailsFragment() {
	}
	
	View m_rootView;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		
		m_rootView = inflater.inflate(R.layout.view_issue, container,
				false);
		
        try {
            ((ITaskDetailsProvider) getActivity()).registerListener(this);
        } catch (ClassCastException e) {
            throw new ClassCastException(getActivity().toString()
                    + " must implement ITaskDetailsProvider");
        }	

        return m_rootView;
	}
	
	@Override
	public void taskLoaded(MIssue details) {

		
		/////////////////////////////////////////////////////
		//  Folder Name
		TextView vFolderName = (TextView)m_rootView.findViewById(R.id.lblFolder);
		if (details instanceof MRemoteIssue) {
			MRemoteIssue ri = (MRemoteIssue)details;
			vFolderName.setText(ri.getPath());
		} else if (details instanceof MRemoteNotSyncedIssue) {
			MRemoteNotSyncedIssue rnsi = (MRemoteNotSyncedIssue)details;
			MDataHelper hlp = new MDataHelper(getActivity().getApplicationContext(), rnsi.getSrvConnId());
			MMyProject pi = hlp.findProjectByFolder(rnsi.getFolderId());
			if (pi != null) {
				MFolder fi = hlp.findFolder(rnsi.getFolderId(), pi);
				if (fi != null)
					vFolderName.setText(pi.getName()+" / "+fi.getName());
				else
					vFolderName.setText(getString(R.string.folder_not_found));
			} else
				vFolderName.setText(getString(R.string.project_not_found));
		} else { // MLocalIssue
			m_rootView.findViewById(R.id.lblFolder).setVisibility(View.GONE);
		}

		/////////////////////////////////////////////////////
		//  Priority
		
		TextView lblPriotiry = (TextView)m_rootView.findViewById(R.id.lblPriority); 
		int priority = details.getPriority()==null?Priority.NORMAL:details.getPriority();
		
		switch (priority) {
		case Priority.LOW:
			lblPriotiry.setText(getString(R.string.issue_priority, getString(R.string.priority_low)));
			break;
		case Priority.HIGH:
			lblPriotiry.setText(getString(R.string.issue_priority, getString(R.string.priority_high)));
			break;
		case Priority.BLOCKER:
			lblPriotiry.setText(getString(R.string.issue_priority, getString(R.string.priority_blocker)));
			break;
		default:
			lblPriotiry.setText(getString(R.string.issue_priority, getString(R.string.priority_normal)));
			m_rootView.findViewById(R.id.lblPriority).setVisibility(View.GONE);
		}

		/////////////////////////////////////////////////////
		//  Due Date
		
		TextView lblDueDate = (TextView)m_rootView.findViewById(R.id.lblDueDate); 
		if (details.getDueDate()!=null && details.getDueDate()>0) {
			SimpleDateFormat df = new SimpleDateFormat(getString(R.string.short_date), Locale.getDefault());
			String due = df.format(new Date(details.getDueDate()));
			lblDueDate.setText(getString(R.string.issue_due_date, due));
		} else 
			m_rootView.findViewById(R.id.lblDueDate).setVisibility(View.GONE);

		/////////////////////////////////////////////////////
		//  Budget
		TextView lblBudget = (TextView)m_rootView.findViewById(R.id.lblBudget); 
		if (details.getBudget() != null && details.getBudget() > 0) {
			String budget = Time.formatMinutes(getActivity().getApplicationContext(), details.getBudget()); 
			lblBudget.setText(getString(R.string.issue_budget, budget));
		} else 
			m_rootView.findViewById(R.id.lblBudget).setVisibility(View.GONE);
		
		/////////////////////////////////////////////////////
		//  Assignee
		TextView lblAssignee = (TextView)m_rootView.findViewById(R.id.lblAssignee); 
		if (details instanceof MRemoteIssue) {
			MRemoteIssue ri = (MRemoteIssue)details;
			if (ri.getAssigneeName()!=null)
				lblAssignee.setText(getString(R.string.issue_assignee, ri.getAssigneeName()));
			else
				m_rootView.findViewById(R.id.lblAssignee).setVisibility(View.GONE); // not specified
		} else if (details instanceof MRemoteNotSyncedIssue) {
			MRemoteNotSyncedIssue rnsi = (MRemoteNotSyncedIssue)details;
			MDataHelper hlp = new MDataHelper(getActivity().getApplicationContext(), rnsi.getSrvConnId());
			if (rnsi.getAssigneeId()!=null && rnsi.getAssigneeId()!=0L) {
				MMyProject pi = hlp.findProjectByFolder(rnsi.getFolderId());
				if (pi != null) {
					MTeamMember m = hlp.findMember(pi, rnsi.getAssigneeId());
					if (m != null)
						lblAssignee.setText(getString(R.string.issue_assignee, m.getName()));
					else
						lblAssignee.setText(R.string.user_not_found);
				} else 
					lblAssignee.setText(R.string.project_not_found); 
			} else
				m_rootView.findViewById(R.id.lblAssignee).setVisibility(View.GONE); // not specified
		} else { // MLocalIssue
			m_rootView.findViewById(R.id.lblAssignee).setVisibility(View.GONE);
		}
		
		/////////////////////////////////////////////////////
		//  Responsible
		TextView lblResponsible = (TextView)m_rootView.findViewById(R.id.lblResponsible); 
		if (details instanceof MRemoteIssue) {
			MRemoteIssue ri = (MRemoteIssue)details;
			if (ri.getResponsibleName()!=null)
				lblResponsible.setText(getString(R.string.issue_responsible, ri.getResponsibleName()));
			else
				m_rootView.findViewById(R.id.lblResponsible).setVisibility(View.GONE); // not specified
		} else if (details instanceof MRemoteNotSyncedIssue) {
			MRemoteNotSyncedIssue rnsi = (MRemoteNotSyncedIssue)details;
			MDataHelper hlp = new MDataHelper(getActivity().getApplicationContext(), rnsi.getSrvConnId());
			if (rnsi.getResponsibleId()!=null && rnsi.getResponsibleId()!=0L) {
				MMyProject pi = hlp.findProjectByFolder(rnsi.getFolderId());
				if (pi != null) {
					MTeamMember m = hlp.findMember(pi, rnsi.getResponsibleId());
					if (m != null)
						lblResponsible.setText(getString(R.string.issue_responsible, m.getName())); 
					else
						lblResponsible.setText(R.string.user_not_found);
				} else 
					lblResponsible.setText(R.string.project_not_found);
			} else
				m_rootView.findViewById(R.id.lblResponsible).setVisibility(View.GONE); // not specified
		} else { // MLocalIssue
			m_rootView.findViewById(R.id.lblResponsible).setVisibility(View.GONE);
		}

		/////////////////////////////////////////////////////
		//  Description
//		TextView lblDescription = (TextView)m_rootView.findViewById(R.id.lblDescription);
		if (details.getDescription() != null) {
			MRemoteSyncedIssue ri = (MRemoteSyncedIssue)details;
			String connId = ri.getSrvConnId();
			long fileId = ri.getId();
			
			TicketDef ticket = getTicket(details.getDescription());

			IssueURLEncoder encoder = new IssueURLEncoder();
			
			final SessionManager sm = getSessionManager();
			String sUrl = sm.getServerUrl(sm.getServerUrl(connId));
			ConvertParameters params = new ConvertParameters();
			params.setAppBaseURL(sUrl); // CUrrent server URL
			params.setFileId(fileId); // Current file ID
			DOM2HtmlConverter m_conv = new DOM2HtmlConverter(params, encoder , 0);
			String html = m_conv.convert(ticket);			

//			String langCode  = "<?xml version=""1.0"" encoding=""UTF-8"" ?>";
			WebView webView1 = (WebView)m_rootView.findViewById(R.id.webView1);
			webView1.loadDataWithBaseURL(null, html,"text/html", "UTF-8", null);		
//			webView1.loadData(langCode + html, "text/html", null);		 
			m_rootView.findViewById(R.id.pnlDescription).setVisibility(View.GONE);		
		}	
	}
	
	private SessionManager getSessionManager() {
		return SessionManager.get(this.getActivity());
	}
	private TicketDef getTicket(String wiki){
	    DOMGenerator gen = new DOMGenerator();
	    WikiParser wparser = new WikiParser();
	    wparser.registerListener(gen);
	    try {
			wparser.parse(wiki);
		} catch (EWikiError e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    return gen.getTicket();
	}
	
}
