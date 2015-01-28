package com.projectkaiser.app_android;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.apache.log4j.Logger;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBar.Tab;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;
import android.net.Uri;

import com.projectkaiser.app_android.async.AsyncCallback;
import com.projectkaiser.app_android.bl.BL;
import com.projectkaiser.app_android.bl.events.AppEvent;
import com.projectkaiser.app_android.bl.events.DataSyncStarted;
import com.projectkaiser.app_android.bl.events.IGlobalAppEventsListener;
import com.projectkaiser.app_android.bl.events.IGlobalAppEventsProvider;
import com.projectkaiser.app_android.bl.events.LocalTaskAdded;
import com.projectkaiser.app_android.bl.events.RefreshLists;
import com.projectkaiser.app_android.bl.local.ILocalBL;
import com.projectkaiser.app_android.bl.obj.MNewComment;
import com.projectkaiser.app_android.bl.obj.MRemoteNotSyncedIssue;
import com.projectkaiser.app_android.bl.obj.MRemoteSyncedIssue;
import com.projectkaiser.app_android.consts.ActivityReq;
import com.projectkaiser.app_android.fragments.main.InboxFragment;
import com.projectkaiser.app_android.fragments.main.LocalTasksFragment;
import com.projectkaiser.app_android.fragments.main.NoConnectionFragment;
import com.projectkaiser.app_android.jsonapi.parser.ResponseParser;
import com.projectkaiser.app_android.jsonrpc.auth.SessionAuthScheme;
import com.projectkaiser.app_android.jsonrpc.errors.EAuthError;
import com.projectkaiser.app_android.services.PkAlarmManager;
import com.projectkaiser.app_android.services.SyncAlarmReceiver;
import com.projectkaiser.app_android.settings.SessionManager;
import com.projectkaiser.app_android.settings.SrvConnectionBaseData;
import com.projectkaiser.app_android.settings.SrvConnectionId;
import com.projectkaiser.mobile.sync.BatchRequest;
import com.projectkaiser.mobile.sync.MCreateRequestEx;
import com.projectkaiser.mobile.sync.MIssue;
import com.projectkaiser.mobile.sync.MSynchronizeRequestEx;
import com.projectkaiser.mobile.sync.MSynchronizeResponseEx;

public class MainActivity extends ActionBarActivity implements
		IGlobalAppEventsProvider {
	
	private final Logger log = Logger.getLogger(MainActivity.class);

	public static final String ALARM_TASK = "com.projectkaiser.app_android.ALARM_TASK";

	private static final String ID_LOCAL = "local";

	private static final String ID_NOT_CONFIGURED = "not_configured";

	private static final String triniforce_email = "ivvist@gmail.com";
	
	SectionsPagerAdapter mSectionsPagerAdapter;
	ViewPager mViewPager;
	ArrayList<IGlobalAppEventsListener> m_eventListeners = new ArrayList<IGlobalAppEventsListener>();
	
	List<String> m_connectionIds = new ArrayList<String>();

	List<String> m_tabNames = new ArrayList<String>();
	
	SessionManager m_sessionManager;
	
	private void addTab(String conId, String tabName) {
		Locale l = Locale.getDefault();
		m_connectionIds.add(conId); 
		m_tabNames.add(tabName.toUpperCase(l));
//		m_fragments.add(fragment);		
		final ActionBar actionBar = getSupportActionBar();
		Tab tab = actionBar.newTab().setText(tabName).setTabListener(m_tabListener);
		mSectionsPagerAdapter.notifyDataSetChanged();
		actionBar.addTab(tab);
	}

	private void createConnections() {
		m_connectionIds.clear();
		m_tabNames.clear();

		final ActionBar actionBar = getSupportActionBar();
		mSectionsPagerAdapter.notifyDataSetChanged();
		actionBar.removeAllTabs();

		addTab(ID_LOCAL, getString(R.string.tab_local));
		
		List<String> connections = m_sessionManager.getConnections();
		
		for (String conId: connections) {
			SrvConnectionBaseData bd = m_sessionManager.getBaseData(conId);
			addTab(conId, bd.getServerName());
		}
		
		if (connections.size() == 0) 
			addTab(ID_NOT_CONFIGURED, getString(R.string.tab_inbox_not_configured));
	}
	
	ActionBar.TabListener m_tabListener = new ActionBar.TabListener() {
		@Override
		public void onTabReselected(Tab arg0, FragmentTransaction arg1) {
			// Ignore this event
		}

		@Override
		public void onTabSelected(Tab tab, FragmentTransaction arg1) {
			mViewPager.setCurrentItem(tab.getPosition());
		}

		@Override
		public void onTabUnselected(Tab arg0, FragmentTransaction arg1) {
			// Ignore this event
		}
	};  
	
	protected void onResume() {
		super.onResume();
		raise(new RefreshLists());		
	};
	
	protected void onNewIntent(Intent intent) {
		if (intent!=null) {
			MRemoteSyncedIssue alarmIssue = (MRemoteSyncedIssue)intent.getSerializableExtra(ALARM_TASK);
			if (alarmIssue != null) {
				final ActionBar actionBar = getSupportActionBar();
				for (int i=1; i<m_connectionIds.size(); i++) {
					if (m_connectionIds.get(i).equals(alarmIssue.getSrvConnId())) {

						actionBar.setSelectedNavigationItem(i);					
						Intent openIssueIntent = new Intent(getApplicationContext(), ViewIssueActivity.class);
						openIssueIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
						openIssueIntent.putExtra(MIssue.class.getName(),  alarmIssue);
				        getApplicationContext().startActivity(openIssueIntent);
					}
				}
				// open remote tab
				// open issue
			}
		}
	};

	private void createUi() {
		setContentView(R.layout.activity_main);

		final ActionBar actionBar = getSupportActionBar();
		actionBar.setHomeButtonEnabled(false);
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
		actionBar.setDisplayShowTitleEnabled(true);

		// Create the adapter that will return a fragment for each of the three
		// primary sections of the activity.
		mSectionsPagerAdapter = new SectionsPagerAdapter(
				getSupportFragmentManager());

		// Set up the ViewPager with the sections adapter.
		mViewPager = (ViewPager) findViewById(R.id.pager);
		mViewPager.setAdapter(mSectionsPagerAdapter);
		mViewPager
				.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
					@Override
					public void onPageSelected(int position) {
						// When swiping between pages, select the
						// corresponding tab.
						actionBar.setSelectedNavigationItem(position);
					}
				});

		createConnections();
		
	}
	
	@Override
	protected void onStart() {		
		PkAlarmManager.activityStarted(getApplicationContext());
		super.onStart();
	}
	
	@Override
	protected void onStop() {
		PkAlarmManager.activityStopped(getApplicationContext());
		super.onStop();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		m_sessionManager = SessionManager.get(this);
	
		SyncAlarmReceiver receiver = new SyncAlarmReceiver();
		if (m_sessionManager.getSyncIntervalMin() > -1) { 
			if (!receiver.isAlarmEnabled(getApplicationContext()))
				receiver.setAlarm(getApplicationContext());
		} else
			receiver.cancelAlarm(getApplicationContext());

		// Thread.setDefaultUncaughtExceptionHandler(new
		// ErrorHandler(getApplicationContext(), getLocalClassName()));
		createUi();
		
		if (getIntent()!=null)
			onNewIntent(getIntent()); // we need to call this for the case when Activity is started for the first time 
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	private ArrayList<String> m_syncingIds = new ArrayList<String>();
	
	private void syncronize(final String connectionId, final AsyncCallback<String> finishedCallback) {
		log.debug("syncronize");
		SrvConnectionBaseData base = m_sessionManager.getBaseData(connectionId);
		
		final MSynchronizeRequestEx request = new MSynchronizeRequestEx();
		ILocalBL bl = BL.getLocal(getApplicationContext());
		
		SessionAuthScheme scheme = new SessionAuthScheme();
		scheme.setSessionId(base.getSessionId());		
		request.setAuthScheme(scheme);
		
		request.setServerUrl(m_sessionManager.getServerUrl(connectionId));
		request.setLocale(getResources().getConfiguration().locale);
		
		final List<MNewComment> newComments = bl.getNewComments(); 
		final List<MRemoteNotSyncedIssue> newTasks = bl.getNotSyncedTasks(); 
		
		
		request.setWorkingSetsDigest(m_sessionManager.getWorkingSets(connectionId).getDigest());
		request.setProjectsDigest(m_sessionManager.getMyProjects(connectionId).getDigest());
		request.setIssuesDigest(m_sessionManager.getIssues(connectionId).getDigest());

		final MCreateRequestEx cr = new MCreateRequestEx();
		
		cr.setAuthScheme(scheme);

		cr.setLocale(getResources().getConfiguration().locale);
		cr.setServerUrl(m_sessionManager.getServerUrl(connectionId));
		cr.setNewComments(newComments);
		cr.setNewIssues(newTasks);
		
		BatchRequest batch = new BatchRequest();
		batch.setCreateRequest(cr);
		batch.setSyncRequest(request);
				
		BL.getServer(getApplicationContext()).synchronize(batch, new AsyncCallback<MSynchronizeResponseEx>() {
			@Override
			public void onSuccess(MSynchronizeResponseEx response) {

				ILocalBL bl = BL.getLocal(getApplicationContext());

				bl.handleCreateResult(cr, response.getCommentRes(), response.getTaskRes());
				
				String data = response.getData();
				
				ResponseParser.parseSyncResponse(m_sessionManager, connectionId, data);
				
				finishedCallback.onSuccess(data);
			}
			
			@Override
			public void onFailure(Throwable e) {
				finishedCallback.onFailure(e);
			}
		});
	}
	
	private void syncFinishedForCon(String connId) {
		m_syncingIds.remove(connId);
		if (m_syncingIds.size() == 0) {
			raise(new RefreshLists());
			m_sessionManager.updateLastSyncDate();
		}
	}
	
	protected void syncronize() {
		
		if (m_syncingIds.size()>0)
			return; // Sync in progress 
		
		for (final String connId: m_sessionManager.getConnections()) {
			
			syncronize(connId, new AsyncCallback<String>() {
				
				@Override
				public void onSuccess(String data) {
					syncFinishedForCon(connId);
				}
				
				@Override
				public void onFailure(Throwable e) {
					log.error(e);
					syncFinishedForCon(connId);
					if (e instanceof EAuthError) 
						Toast.makeText(getApplicationContext(), getString(R.string.authentication_failed) , Toast.LENGTH_LONG).show();
					else  
						Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
				}
			});
			m_syncingIds.add(connId);
		}
		
		if (m_sessionManager.getConnections().size() > 0)
			raise(new DataSyncStarted());
		else {
			raise(new RefreshLists());
		}
		
	}
	
	@Override
	public void newConnectionRequested() {
		Intent i = new Intent(getApplicationContext(),
				SigninActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		finish();
		getApplicationContext().startActivity(i);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_new_task:
			Intent i = new Intent(getApplicationContext(),
					EditIssueActivity.class);
			startActivityForResult(i, ActivityReq.NEW_ISSUE);
			break;
		case R.id.action_settings:
			i = new Intent(getApplicationContext(),
					SettingsActivity.class);
	        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
	        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			finish();
			getApplicationContext().startActivity(i);
			return true;
		case R.id.action_about:
			i = new Intent(getApplicationContext(),
					InfoActivity.class);
	        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
	        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			getApplicationContext().startActivity(i);
			return true;
		case R.id.action_send_log:
			SendErrorLog();
			return true;
		case R.id.action_sync:
			syncronize();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	public void SendErrorLog() { 
		File Attdir = null;
		if (getApplicationContext().getExternalFilesDir(null)==null){
			Attdir = new File(getApplicationContext().getFilesDir().getAbsolutePath());
		} else {
			Attdir = new File(getApplicationContext().getExternalFilesDir(null).getAbsolutePath());
		}
        File logfile = new File(Attdir.getPath() + "/pklog.txt");
        if (logfile.exists()){
            Intent i = new Intent(Intent.ACTION_SEND);
    		i.setType("vnd.android.cursor.dir/email");
    		i.putExtra(Intent.EXTRA_EMAIL  , new String[]{"ivvist@gmail.com"});
    		i.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.action_errorlog_caption));
    		i.putExtra(Intent.EXTRA_TEXT   , getString(R.string.action_errorlog_body));
    		Uri uri = Uri.parse("file://" + logfile.getAbsolutePath());
    		i.putExtra(Intent.EXTRA_STREAM,  uri);
    		try {
    		    startActivity(Intent.createChooser(i, getString(R.string.action_errorlog_sent)));
    		} catch (android.content.ActivityNotFoundException ex) {
    		    Toast.makeText(MainActivity.this, R.string.action_no_email_client, Toast.LENGTH_SHORT).show();
    		}	
        }

	}
	
	public class SectionsPagerAdapter extends FragmentPagerAdapter {

		public SectionsPagerAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public Fragment getItem(int position) {
			String connectionId = m_connectionIds.get(position);
			if (ID_LOCAL.equals(connectionId))
				return LocalTasksFragment.newInstance();
			else if (ID_NOT_CONFIGURED.equals(connectionId)) {
				return new NoConnectionFragment();
			} else {
				InboxFragment f = new InboxFragment();
				Bundle args = new Bundle();
				args.putString(SrvConnectionId.ARG, connectionId);
				f.setArguments(args);
				return f;
			}
		}

		@Override
		public int getCount() {
			return m_connectionIds.size();
		}

		@Override
		public CharSequence getPageTitle(int position) {
			return m_tabNames.get(position);
		}
	}

	@Override
	public void register(IGlobalAppEventsListener listener) {
		m_eventListeners.add(listener);
	}
	
	private void raise(AppEvent event) {
		for (IGlobalAppEventsListener lst : m_eventListeners)
			lst.onEvent(event);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == RESULT_OK) {
			switch (requestCode) {
			case ActivityReq.NEW_ISSUE:
				raise(new LocalTaskAdded());
				break;
			case ActivityReq.EDIT_LOCAL_ISSUE:
				raise(new LocalTaskAdded());
				break;
			}
		}
		super.onActivityResult(requestCode, resultCode, data);
	}
	
	@Override
	public void syncRequested() {
		syncronize();		
	}
	
}
