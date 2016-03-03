package com.projectkaiser.app_android.fragments.issue;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.webkit.WebView;
import android.webkit.CookieSyncManager;
import android.webkit.WebViewClient;
import android.webkit.URLUtil;
import android.webkit.MimeTypeMap;
import android.content.Intent;
import android.net.Uri;
import android.net.MailTo;

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
import com.projectkaiser.app_android.services.PkAlarmManager;
import com.projectkaiser.app_android.misc.*;

import java.io.File;
import android.webkit.CookieManager;

import com.triniforce.document.elements.TicketDef;
import com.triniforce.document.elements.macros.def.*;
import com.triniforce.document.elements.macros.def.intf.*;
import com.triniforce.document.elements.macros.def.container.*;
import com.triniforce.dom.*;
import com.triniforce.dom.dom2html.*;
import com.triniforce.wiki.*;

public class IssueDetailsFragment extends Fragment implements
		ITaskDetailsListener {
	/**
	 * Returns a new instance of this fragment for the given section number.
	 */
	private MIssue myDetails = null;
	private String sessionID = null;
	private CookieManager webCookieManager = CookieManager.getInstance();

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

		m_rootView = inflater.inflate(R.layout.view_issue, container, false);

		try {
			((ITaskDetailsProvider) getActivity()).registerListener(this);
		} catch (ClassCastException e) {
			throw new ClassCastException(getActivity().toString()
					+ " must implement ITaskDetailsProvider");
		}

		return m_rootView;
	}

	public class IssueTicketCloakMacroDef extends TicketCloakMacroDef {
		@Override
		public int getWikiOptions() {
			return WikiOptions.BREAKS_PARAGRAPH
					| WikiOptions.BR_BEFORE_STARTTAG
					| WikiOptions.BR_AFTER_ENDTAG;
		}

		@Override
		public DOMOptions getDOMOptions(ITicketMacro instance) {
			DOMOptions options = new DOMOptions();
			options.setCustomHtmlTag("details");
			options.setPreContentCode("<summary>%name%</summary>");
			return options;
		}

	}

	@Override
	public void taskLoaded(MIssue details) {

		myDetails = details;
		// ///////////////////////////////////////////////////
		// Folder Name
		TextView vFolderName = (TextView) m_rootView
				.findViewById(R.id.lblFolder);
		if (details instanceof MRemoteIssue) {
			MRemoteIssue ri = (MRemoteIssue) details;
			vFolderName.setText(PkAlarmManager.GetFolderName(ri.getPath()));
		} else if (details instanceof MRemoteNotSyncedIssue) {
			MRemoteNotSyncedIssue rnsi = (MRemoteNotSyncedIssue) details;
			MDataHelper hlp = new MDataHelper(getActivity()
					.getApplicationContext(), rnsi.getSrvConnId());
			MMyProject pi = hlp.findProjectByFolder(rnsi.getFolderId());
			if (pi != null) {
				MFolder fi = hlp.findFolder(rnsi.getFolderId(), pi);
				String pName = PkAlarmManager.GetFolderName(pi.getName());
				if (fi != null)
					vFolderName.setText(pName + " / " + fi.getName());
				else {
					vFolderName.setText(pName);
				}
			} else
				vFolderName.setText(getString(R.string.project_not_found));
		} else { // MLocalIssue
			m_rootView.findViewById(R.id.lblFolder).setVisibility(View.GONE);
		}

		// ///////////////////////////////////////////////////
		// Priority

		TextView lblPriotiry = (TextView) m_rootView
				.findViewById(R.id.lblPriority);
		int priority = details.getPriority() == null ? Priority.NORMAL
				: details.getPriority();

		switch (priority) {
		case Priority.LOW:
			lblPriotiry.setText(getString(R.string.issue_priority,
					getString(R.string.priority_low)));
			break;
		case Priority.HIGH:
			lblPriotiry.setText(getString(R.string.issue_priority,
					getString(R.string.priority_high)));
			break;
		case Priority.BLOCKER:
			lblPriotiry.setText(getString(R.string.issue_priority,
					getString(R.string.priority_blocker)));
			break;
		default:
			lblPriotiry.setText(getString(R.string.issue_priority,
					getString(R.string.priority_normal)));
			m_rootView.findViewById(R.id.lblPriority).setVisibility(View.GONE);
		}

		// ///////////////////////////////////////////////////
		// Due Date

		TextView lblDueDate = (TextView) m_rootView
				.findViewById(R.id.lblDueDate);
		if (details.getDueDate() != null && details.getDueDate() > 0) {

			Calendar cdt = Calendar.getInstance();
			cdt.setTime(new Date(details.getDueDate()));
			boolean nullDueTime = (cdt.get(Calendar.HOUR_OF_DAY)) == 0;

			SimpleDateFormat df;
			if (!nullDueTime)
				df = new SimpleDateFormat(getString(R.string.short_date_time),
						Locale.getDefault());
			else
				df = new SimpleDateFormat(getString(R.string.short_date),
						Locale.getDefault());

			String due = df.format(new Date(details.getDueDate()));
			lblDueDate.setText(getString(R.string.issue_due_date, due));
		} else
			m_rootView.findViewById(R.id.lblDueDate).setVisibility(View.GONE);

		// ///////////////////////////////////////////////////
		// Budget
		TextView lblBudget = (TextView) m_rootView.findViewById(R.id.lblBudget);
		if (details.getBudget() != null && details.getBudget() > 0) {
			String budget = Time.formatMinutes(getActivity()
					.getApplicationContext(), details.getBudget());
			lblBudget.setText(getString(R.string.issue_budget, budget));
		} else
			m_rootView.findViewById(R.id.lblBudget).setVisibility(View.GONE);

		// ///////////////////////////////////////////////////
		// Assignee
		TextView lblAssignee = (TextView) m_rootView
				.findViewById(R.id.lblAssignee);
		if (details instanceof MRemoteIssue) {
			MRemoteIssue ri = (MRemoteIssue) details;
			if (ri.getAssigneeName() != null)
				lblAssignee.setText(getString(R.string.issue_assignee,
						ri.getAssigneeName()));
			else
				m_rootView.findViewById(R.id.lblAssignee).setVisibility(
						View.GONE); // not specified
		} else if (details instanceof MRemoteNotSyncedIssue) {
			MRemoteNotSyncedIssue rnsi = (MRemoteNotSyncedIssue) details;
			MDataHelper hlp = new MDataHelper(getActivity()
					.getApplicationContext(), rnsi.getSrvConnId());
			if (rnsi.getAssigneeId() != null && rnsi.getAssigneeId() != 0L) {
				MMyProject pi = hlp.findProjectByFolder(rnsi.getFolderId());
				if (pi != null) {
					MTeamMember m = hlp.findMember(pi, rnsi.getAssigneeId());
					if (m != null)
						lblAssignee.setText(getString(R.string.issue_assignee,
								m.getName()));
					else
						lblAssignee.setText(R.string.user_not_found);
				} else
					lblAssignee.setText(R.string.project_not_found);
			} else
				m_rootView.findViewById(R.id.lblAssignee).setVisibility(
						View.GONE); // not specified
		} else { // MLocalIssue
			m_rootView.findViewById(R.id.lblAssignee).setVisibility(View.GONE);
		}

		// ///////////////////////////////////////////////////
		// Responsible
		TextView lblResponsible = (TextView) m_rootView
				.findViewById(R.id.lblResponsible);
		if (details instanceof MRemoteIssue) {
			MRemoteIssue ri = (MRemoteIssue) details;
			if (ri.getResponsibleName() != null)
				lblResponsible.setText(getString(R.string.issue_responsible,
						ri.getResponsibleName()));
			else
				m_rootView.findViewById(R.id.lblResponsible).setVisibility(
						View.GONE); // not specified
		} else if (details instanceof MRemoteNotSyncedIssue) {
			MRemoteNotSyncedIssue rnsi = (MRemoteNotSyncedIssue) details;
			MDataHelper hlp = new MDataHelper(getActivity()
					.getApplicationContext(), rnsi.getSrvConnId());
			if (rnsi.getResponsibleId() != null
					&& rnsi.getResponsibleId() != 0L) {
				MMyProject pi = hlp.findProjectByFolder(rnsi.getFolderId());
				if (pi != null) {
					MTeamMember m = hlp.findMember(pi, rnsi.getResponsibleId());
					if (m != null)
						lblResponsible.setText(getString(
								R.string.issue_responsible, m.getName()));
					else
						lblResponsible.setText(R.string.user_not_found);
				} else
					lblResponsible.setText(R.string.project_not_found);
			} else
				m_rootView.findViewById(R.id.lblResponsible).setVisibility(
						View.GONE); // not specified
		} else { // MLocalIssue
			m_rootView.findViewById(R.id.lblResponsible).setVisibility(
					View.GONE);
		}

		// ///////////////////////////////////////////////////
		// Description

		if (details.getDescription() != null) {
			MRemoteSyncedIssue ri = (MRemoteSyncedIssue) details;
			String connId = ri.getSrvConnId();
			long fileId = ri.getId();

			TicketDef ticket = ImgLoadHelper.getTicket(details.getDescription());
			IssueURLEncoder encoder = new IssueURLEncoder();

			final SessionManager sm = getSessionManager();
			String sUrl = sm.getServerUrl(connId);
			ConvertParameters params = new ConvertParameters();
			params.setAppBaseURL(sUrl); // CUrrent server URL
			params.setFileId(fileId); // Current file ID
			RegisteredMacros.setInstaller(new SystemMacrosInstaller()); // Installs
																		// default
																		// macros
			RegisteredMacros.put(new IssueTicketCloakMacroDef());

			DOM2HtmlConverter m_conv = new DOM2HtmlConverter(params, encoder, 0);

			String html = m_conv.convert(ticket);

			WebView webView1 = (WebView) m_rootView.findViewById(R.id.webView1);
			if (html.trim() == "") {
				webView1.setVisibility(0);
			} else {
				CookieSyncManager.createInstance(this.getActivity());
				webCookieManager = CookieManager.getInstance();
				webCookieManager.removeAllCookie();
				webCookieManager.setAcceptCookie(true);
				sessionID = sm.getBaseData(connId).getSessionId();
				webCookieManager.setCookie(sUrl, "sid = " + sessionID);
				CookieSyncManager.getInstance().sync();

				webView1.getSettings().setJavaScriptEnabled(true);
				webView1.getSettings().setDomStorageEnabled(true);
				webView1.getSettings().setAppCacheEnabled(true);
				webView1.getSettings().setBlockNetworkImage(false);
				webView1.getSettings().setBlockNetworkLoads(false);
				webView1.getSettings().setLoadsImagesAutomatically(true);

				webView1.getSettings().setLoadWithOverviewMode(true);
				webView1.getSettings().setUseWideViewPort(true);
				webView1.getSettings().setSupportZoom(true);
				webView1.getSettings().setBuiltInZoomControls(true);
				webView1.setInitialScale(300);

				WebViewClient myWebClient = new WebViewClient() {
					String imgUrl = "";
					
					// I tell the webclient you want to catch when a url is
					// about to load
					@Override
					public boolean shouldOverrideUrlLoading(WebView view,
							String url) {
						if (url.contains("http://") && !url.contains("@")) {
							Uri uriUrl = Uri.parse(url);
							Intent launchBrowser = new Intent(
									Intent.ACTION_VIEW, uriUrl);
							startActivity(launchBrowser);
							return true;
						} else {
							if (url.contains("@")) {
								url = ImgLoadHelper.ParseEmail(url);
								String[] recipients = new String[]{url, ""};  
								Intent testIntent = new Intent(android.content.Intent.ACTION_SEND);  
								testIntent.setType("text/email");  
								testIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "");  
								testIntent.putExtra(android.content.Intent.EXTRA_TEXT, "");  
								testIntent.putExtra(android.content.Intent.EXTRA_EMAIL, recipients);  
								startActivity(testIntent); 
								return true;
							}
						}

						return false;
					}

					// here you execute an action when the URL you want is about
					// to load

					File imgdir = null;

					@Override
					public void onLoadResource(WebView view, String url) {
						imgUrl = url;
						if (!url.contains("file:///")) {
							// save into local file
							imgdir = ImgLoadHelper.GetImageDir(getActivity().getApplicationContext());
							if (imgdir == null)
								return;
							if (!imgdir.canWrite()) {
								// TODO: write to log
								return;
							}

							AsyncTask<Void, Void, Void> myTask = new AsyncTask<Void, Void, Void>() {
								@Override
								protected Void doInBackground(Void... params) {
									ImageDownloader imDownloader = new ImageDownloader(
											imgdir.getAbsolutePath());
									String imFileName = myDetails.getId()
											.toString()
											+ "_"
											+ ImgLoadHelper.GetImageNameFromURL(imgUrl);
									imDownloader.DownloadFromUrl(imgUrl,
											imFileName, sessionID);
									return null;
								}
							};
							myTask.execute();
						}
					}
				};

				webView1.setWebViewClient(myWebClient);
				html = ImgLoadHelper.ParsePictures(getActivity().getApplicationContext(),
							html, sUrl, myDetails.getId().toString());
				webView1.loadDataWithBaseURL(sUrl, html, "text/html", "UTF-8",	null);

			}
			m_rootView.findViewById(R.id.pnlDescription).setVisibility(
					View.GONE);
		}

	}

	private SessionManager getSessionManager() {
		return SessionManager.get(this.getActivity());
	}


}
