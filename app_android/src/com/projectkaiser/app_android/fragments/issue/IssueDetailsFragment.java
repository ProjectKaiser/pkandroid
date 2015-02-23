package com.projectkaiser.app_android.fragments.issue;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import android.webkit.WebView;
import android.webkit.CookieSyncManager;
import android.webkit.WebViewClient;

import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.UserRecoverableAuthException;
import com.google.android.gms.common.Scopes;
import com.projectkaiser.app_android.R;
import com.projectkaiser.app_android.SigninActivity;
import com.projectkaiser.app_android.bl.obj.MRemoteNotSyncedIssue;
import com.projectkaiser.app_android.bl.obj.MRemoteSyncedIssue;
import com.projectkaiser.app_android.consts.Priority;
import com.projectkaiser.app_android.fragments.issue.intf.ITaskDetailsListener;
import com.projectkaiser.app_android.fragments.issue.intf.ITaskDetailsProvider;
import com.projectkaiser.app_android.misc.Time;
import com.projectkaiser.app_android.settings.SessionManager;
import com.projectkaiser.app_android.settings.SrvConnectionId;
import com.projectkaiser.mobile.sync.MDataHelper;
import com.projectkaiser.mobile.sync.MFolder;
import com.projectkaiser.mobile.sync.MIssue;
import com.projectkaiser.mobile.sync.MMyProject;
import com.projectkaiser.mobile.sync.MRemoteIssue;
import com.projectkaiser.mobile.sync.MTeamMember;
import com.projectkaiser.mobile.sync.MAttachment;
import com.projectkaiser.app_android.jsonapi.parser.ResponseParser;
import com.projectkaiser.app_android.misc.*;

import java.io.File;
import java.io.IOException;
import android.webkit.CookieManager;
import java.net.URISyntaxException;

import com.triniforce.document.elements.TicketDef;
import java.util.*;
import java.net.HttpCookie;
import java.net.URI;
import com.triniforce.dom.*;
import com.triniforce.dom.dom2html.*;
import com.triniforce.wiki.*;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.CookieStore;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.cookie.Cookie;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.HttpResponse;
import org.apache.http.protocol.HttpRequestHandler;

public class IssueDetailsFragment extends Fragment implements ITaskDetailsListener {
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

		myDetails = details;
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
			String sUrl = sm.getServerUrl(connId);
			ConvertParameters params = new ConvertParameters();
			params.setAppBaseURL(sUrl); // CUrrent server URL
			params.setFileId(fileId); // Current file ID
			DOM2HtmlConverter m_conv = new DOM2HtmlConverter(params, encoder , 0);
			String html = m_conv.convert(ticket);			

			WebView webView1 = (WebView)m_rootView.findViewById(R.id.webView1);
			CookieSyncManager.createInstance(this.getActivity());
			webCookieManager = CookieManager.getInstance();
			webCookieManager.removeAllCookie();
			webCookieManager.setAcceptCookie(true);
			sessionID = sm.getBaseData(connId).getSessionId();
		    webCookieManager.setCookie(sUrl, "sid = " + sessionID );
		    CookieSyncManager.getInstance().sync();

		    webView1.getSettings().setJavaScriptEnabled(true);
		    webView1.getSettings().setDomStorageEnabled(true);
		    webView1.getSettings().setAppCacheEnabled(true);
		    webView1.getSettings().setBlockNetworkImage(false);
		    webView1.getSettings().setBlockNetworkLoads(false);
		    webView1.getSettings().setLoadsImagesAutomatically(true);
		    
			WebViewClient myWebClient = new WebViewClient()
			{
	            String imgUrl = "";
			    // I tell the webclient you want to catch when a url is about to load
			    @Override
			    public boolean shouldOverrideUrlLoading(WebView  view, String  url){
			        return true;
			    }
			    // here you execute an action when the URL you want is about to load

			    File imgdir = null;
	        	@Override
			    public void onLoadResource(WebView  view, String  url){
		    		imgUrl = url;
			        if( url.contains("/att?name") ){
			           // save into local file
			        	imgdir = GetImageDir();
			            if (!imgdir.canWrite()){
			        		// TODO: write to log 
			            	return;		
			            } 
			            
			            AsyncTask<Void, Void, Void> myTask = new AsyncTask<Void, Void, Void >() {
			    			@Override
			    			protected Void  doInBackground(Void... params) {
					        	ImageDownloader imDownloader = new ImageDownloader(imgdir.getAbsolutePath());
					    		String imFileName = myDetails.getId().toString() + "_" + GetImageNameFromURL(imgUrl);
					        	imDownloader.DownloadFromUrl(imgUrl, imFileName, sessionID);
					        	return null;
			    			}
			    		};
                       	myTask.execute();
			        }
			    }
			};
			webView1.setWebViewClient(myWebClient);
		    html = ParsePictures(html);
			html = GetImageShowScript() + html;
			
           	webView1.loadDataWithBaseURL(sUrl, html,"text/html", "UTF-8", null);
			m_rootView.findViewById(R.id.pnlDescription).setVisibility(View.GONE);
		}
	
	}
	    
	private File GetImageDir(){
		File imdir = null;
        if (getActivity().getApplicationContext().getExternalFilesDir(null)==null){
        	imdir = new File(getActivity().getApplicationContext().getFilesDir().getAbsolutePath());
        } else {
        	imdir = new File(getActivity().getApplicationContext().getExternalFilesDir(null).getAbsolutePath());
        }
        if (!imdir.exists()){ 
        	imdir.mkdirs();
        }	
        return imdir;
	}
	private String GetImageShowScript(){
		StringBuilder sb  = new StringBuilder();
		sb.append("<script type=\"text/javascript\">");
		sb.append("function toggle(divElem, imgElem, sUrl) {");
		sb.append("if(imgElem) {");
		sb.append("imgElem.src = sUrl;");
		sb.append("imgElem.style.visibility='visible';");
		sb.append("}");
		sb.append("if(divElem) {");
		sb.append("divElem.style.visibility='hidden';");
		sb.append("}");
		sb.append("}");
		sb.append("</script>");
		return sb.toString();
	}
	
	private String GetImageNameFromURL(String url){
		int idxStart = 0; 
		int idxEnd = 0; 
		if (url.contains("/att?name")) {
			idxStart = url.indexOf("/att?name") + 10; 
			idxEnd   = url.indexOf("&", idxStart);
			if (idxEnd>idxStart + 3){
				return url.substring(idxStart, idxEnd);
			}
		}
		return url;
	}
	
	private String GetEmptyImageFrom(int idx, String strURL){
		
		String imFileName = myDetails.getId().toString() + "_" + GetImageNameFromURL(strURL);
		boolean bneedscript = false;
		File imgdir = GetImageDir();
		if (imgdir==null){bneedscript = true;};
		File imgfile = null;
		if (!bneedscript){
			imgfile = new File(imgdir.getAbsolutePath() + "/" + imFileName);
	        if (!imgfile.exists()) {bneedscript = true;}
		}
		if (!bneedscript){
        	return "<IMG src=\"file://" + imgfile.getAbsolutePath() +  "\""; 
        } else {
        	
        	StringBuilder sb = new StringBuilder();
        	sb.append("<img id=\"img"+ idx + "\" src=\"\" style='visibility:hidden'>");
        	sb.append("<div onclick=\"toggle(this,document.getElementById('img" + idx + "'), '" + strURL +"'"
        			+ ")\" style=\"height:30px; text-align: center; vertical-align: bottom-text; border: 1px solid; border-radius: 5px; border-color:Grey; cursor: pointer; cursor: hand\">"
        			+ getString(R.string.issue_load_image) + "</div>");
    		return sb.toString();
        }
	}

	private String ParsePictures(String strHTML){
		int idx = 0;
		while(strHTML.indexOf("<IMG src=\"{/-")>0){
			String strURL = strHTML.substring(strHTML.indexOf("<IMG src=\"{/-") + 13, strHTML.indexOf("-/}")); 
	        strHTML = strHTML.replace("<IMG src=\"{/-" + strURL + "-/}\" _pk_ue=\"UTF-8\" style=\"border:none;\" />", 
					GetEmptyImageFrom(idx, strURL) );
			idx =+1;
		}
		return strHTML;
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
