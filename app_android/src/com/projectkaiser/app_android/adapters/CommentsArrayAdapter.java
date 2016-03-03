package com.projectkaiser.app_android.adapters;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.projectkaiser.app_android.R;
import com.projectkaiser.app_android.bl.obj.MRemoteNonSyncedComment;
import com.projectkaiser.app_android.bl.obj.MRemoteSyncedIssue;
import com.projectkaiser.app_android.fragments.issue.IssueURLEncoder;
import com.projectkaiser.app_android.misc.ImageDownloader;
import com.projectkaiser.app_android.settings.SessionManager;
import com.projectkaiser.mobile.sync.MComment;
import com.projectkaiser.mobile.sync.MIssue;
import com.projectkaiser.app_android.bl.obj.*;
import com.projectkaiser.app_android.fragments.issue.ImgLoadHelper;

import com.triniforce.document.elements.TicketDef;
import com.triniforce.dom.ConvertParameters;
import com.triniforce.dom.dom2html.DOM2HtmlConverter;

public class CommentsArrayAdapter extends ArrayAdapter<MComment> {

	String sUrl;
	Context m_ctx;
	MIssue m_details = null; 
	List<MComment> m_comments;
	private String sessionID = null;

	public CommentsArrayAdapter(Context ctx, List<MComment> comments, MIssue details) {
		super(ctx, R.layout.comments_row, comments);
		m_comments =  comments;
		m_ctx = ctx;
		m_details = details;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		LayoutInflater inflater = (LayoutInflater) m_ctx
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		SimpleDateFormat df = new SimpleDateFormat(m_ctx.getString(R.string.short_time), Locale.getDefault());
		
		View rowView = inflater.inflate(R.layout.comments_row, parent, false);
		TextView lblCommentCreator = (TextView) rowView.findViewById(R.id.lblCommentCreator);
		TextView lblCommentCreated = (TextView) rowView.findViewById(R.id.lblCommentCreated);
		TextView lblCommentBody = (TextView) rowView.findViewById(R.id.lblCommentBody);
		MComment comment = m_comments.get(position);
		lblCommentCreated.setText(df.format(new Date(comment.getCreated())));

		
		
		TicketDef ticket = ImgLoadHelper.getTicket(comment.getDescription());
		IssueURLEncoder encoder = new IssueURLEncoder();

		ConvertParameters conv_params = new ConvertParameters();
		DOM2HtmlConverter m_conv = new DOM2HtmlConverter(conv_params, encoder, 0);

		WebView webView1 = (WebView) rowView.findViewById(R.id.webView1);
			
		final SessionManager sm = SessionManager.get( m_ctx );
		String connId = "";
		if (m_details instanceof MRemoteSyncedIssue){
			connId = ((MRemoteSyncedIssue)m_details).getSrvConnId();
		}
		else if (m_details instanceof MRemoteNotSyncedIssue){
			connId = ((MRemoteNotSyncedIssue)m_details).getSrvConnId();
		}

		sUrl = sm.getServerUrl(connId);
		String html = m_conv.convert(ticket);
		if (html.trim() == "" || connId=="") {
			lblCommentBody.setVisibility(View.GONE);
			lblCommentBody.setText(comment.getDescription());
			webView1.setVisibility(0);
		} else {
			lblCommentBody.setVisibility(View.GONE);
			webView1.setVisibility(0);
			
			CookieManager webCookieManager = CookieManager.getInstance();
			CookieSyncManager.createInstance(m_ctx);
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
			
			webView1.setInitialScale(200);

			WebViewClient myWebClient = new WebViewClient() {
				String imgUrl = "";
				
				@Override
				public boolean shouldOverrideUrlLoading(WebView view,
						String url) {
					if (url.contains("http://") && !url.contains("@")) {
						Uri uriUrl = Uri.parse(url);
						Intent launchBrowser = new Intent(
								Intent.ACTION_VIEW, uriUrl);
						m_ctx.startActivity(launchBrowser);
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
							m_ctx.startActivity(testIntent); 
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
						imgdir = ImgLoadHelper.GetImageDir(m_ctx);
						if (imgdir == null)
							return;
						if (!imgdir.canWrite()) {
							// TODO: write to log
							return;
						}

						AsyncTask<Void, Void, Void> myTask = new AsyncTask<Void, Void, Void>() {
							@Override
							protected Void doInBackground(Void... conv_params) {
								ImageDownloader imDownloader = new ImageDownloader(
										imgdir.getAbsolutePath());
								String imFileName =m_details.getId().toString() + "_"	+ ImgLoadHelper.GetImageNameFromURL(imgUrl);
								imDownloader.DownloadFromUrl(imgUrl, imFileName, sessionID);
								return null;
							}
						};
						myTask.execute();
					}
				}
			};

			webView1.setWebViewClient(myWebClient);
			html = ImgLoadHelper.ParsePictures(m_ctx,html, sUrl, m_details.getId().toString());
			webView1.loadDataWithBaseURL(sUrl, html, "text/html", "UTF-8",null);
		}	
			
			
		
		if (comment instanceof MRemoteNonSyncedComment) { // Local non synced
			lblCommentCreator.setVisibility(View.GONE);
			RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams)lblCommentCreated.getLayoutParams();
			params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);			
		} else if  (comment.getCreatorName() == null || "".equals(comment.getCreatorName())) { // Local
			lblCommentCreator.setVisibility(View.GONE);			
			RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams)lblCommentCreated.getLayoutParams();
			params.addRule(RelativeLayout.ALIGN_PARENT_LEFT);						
		} else { // Remote synced
			RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams)lblCommentCreated.getLayoutParams();
			params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
			lblCommentCreator.setText(comment.getCreatorName());
		}
		
		ImageView imgNonSynced = (ImageView) rowView.findViewById(R.id.imgNonSynced);	
		ImageView imgSyncFailure = (ImageView) rowView.findViewById(R.id.imgSyncFailure); 
		TextView lblSyncError = (TextView) rowView.findViewById(R.id.lblSyncError);
		imgNonSynced.setVisibility(View.GONE);
		imgSyncFailure.setVisibility(View.GONE);
		
		if (comment instanceof MRemoteNonSyncedComment) {
			MRemoteNonSyncedComment rnsc = (MRemoteNonSyncedComment)comment;
			if (rnsc.getFailure()!=null) {
				imgSyncFailure.setVisibility(View.VISIBLE);
				lblSyncError.setVisibility(View.VISIBLE);
				lblSyncError.setText(m_ctx.getString(R.string.sync_error, rnsc.getFailure()));
			} else
				imgNonSynced.setVisibility(View.VISIBLE);
		}
		
		return rowView;
	}

}
