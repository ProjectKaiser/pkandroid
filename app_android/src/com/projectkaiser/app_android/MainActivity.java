package com.projectkaiser.app_android;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.NotificationCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.*;
import android.view.View;
import android.view.ViewGroup;
import android.view.LayoutInflater;
import android.net.Uri;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.view.Gravity;
import java.net.UnknownHostException;
import com.projectkaiser.mobile.sync.MLocalIssue;

import com.projectkaiser.app_android.jsonrpc.errors.*;
import com.projectkaiser.app_android.misc.*;
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
import com.projectkaiser.app_android.fragments.main.IRetainedFragment;
import com.projectkaiser.app_android.fragments.main.IssueListData;
import com.projectkaiser.app_android.ServerListData;
import com.projectkaiser.app_android.bl.local.*;

public class MainActivity extends ActionBarActivity implements
		IGlobalAppEventsProvider {

	private final Logger log = Logger.getLogger(MainActivity.class);

	public static final String ALARM_TASK = "com.projectkaiser.app_android.ALARM_TASK";
	public static final String DUE_TASK = "com.projectkaiser.app_android.DUE_TASK";
	private static final String ID_LOCAL = "local";
	private static final String ID_NOT_CONFIGURED = "not_configured";
	private static final String triniforce_email = "ivvist@gmail.com";
	private FloatingActionButton fabButton = null;
	private Menu _menu = null;
	private String curServerName = null;
	private int oldItemPosition = 1;
	private ListView mDrawerServerListView;
	ArrayAdapter<ServerListData> ldp = null;
	Fragment curfragment = null;
	private boolean bNeedShowOverDue = false;

	private ArrayList<ServerListData> mDrawerServers = new ArrayList<ServerListData>();

	ArrayList<IGlobalAppEventsListener> m_eventListeners = new ArrayList<IGlobalAppEventsListener>();

	List<String> m_connectionIds = new ArrayList<String>();
	SessionManager m_sessionManager;

	private void UpdateServerListSuccess(String connId) {
		m_sessionManager.updateLastSyncDate(connId);
		if (mDrawerServers == null)
			return;
		for (int i = 1; i < m_connectionIds.size(); i++) {
			if (connId.equals(m_connectionIds.get(i))) {
				m_sessionManager.updateLastSyncStatus(connId, "");
				m_sessionManager.updateLastSyncWarning(connId, "");
				if (ldp != null) {
					ldp.notifyDataSetChanged();
				}
				return;
			}
		}
	}

	private void UpdateServerListError(String connId, String eStr,
			boolean bWarning) {
		if (mDrawerServers == null)
			return;
		if (eStr.isEmpty())
			return;
		for (int i = 1; i < m_connectionIds.size(); i++) {
			if (connId.equals(m_connectionIds.get(i))) {
				if (!bWarning) {
					m_sessionManager.updateLastSyncStatus(connId, eStr);
					m_sessionManager.updateLastSyncWarning(connId, "");
				} else {
					m_sessionManager.updateLastSyncStatus(connId, "");
					m_sessionManager.updateLastSyncWarning(connId, eStr);
				}
				if (ldp != null) {
					ldp.notifyDataSetChanged();
				}
				return;
			}
		}
	}

	private void createConnections() {
		mDrawerServers.clear();
		m_connectionIds.clear();

		mDrawerServers.add(new ServerListData(m_sessionManager, "-", "", ""));
		m_connectionIds.add("");
		m_connectionIds.add(ID_LOCAL);
		mDrawerServers.add(new ServerListData(m_sessionManager,
				getString(R.string.tab_local), "", ""));
		List<String> connections = m_sessionManager.getConnections();

		for (String conId : connections) {
			SrvConnectionBaseData bd = m_sessionManager.getBaseData(conId);
			m_connectionIds.add(conId);
			mDrawerServers.add(new ServerListData(m_sessionManager, bd
					.getServerName(), conId, bd.getUserName()));
		}

		if (connections.size() == 0)
			mDrawerServers.add(new ServerListData(m_sessionManager,
					getString(R.string.tab_inbox_not_configured), "", ""));
		m_connectionIds.add(ID_NOT_CONFIGURED);
		mDrawerServers.add(new ServerListData(m_sessionManager, "-", "", ""));
		mDrawerServers.add(new ServerListData(m_sessionManager,
				getString(R.string.title_activity_settings), "", ""));
		mDrawerServers.add(new ServerListData(m_sessionManager,
				getString(R.string.action_send_log), "", ""));
		mDrawerServers.add(new ServerListData(m_sessionManager,
				getString(R.string.action_about), "", ""));

	}

	protected void onResume() {
		super.onResume();
		createConnections();
		raise(new RefreshLists());
		if (ldp != null) {
			ldp.notifyDataSetChanged();
		}
	};

	protected void onNewIntent(Intent intent) {
		if (intent != null) {
			MIssue issue = (MIssue) intent.getSerializableExtra(ALARM_TASK);
			if (issue != null) {
				MIssue alarmIssue = null;
				String srvName;
				if (issue instanceof MRemoteSyncedIssue) {
					alarmIssue = (MRemoteSyncedIssue) issue;
					srvName = ((MRemoteSyncedIssue) alarmIssue).getSrvConnId();
					Intent openIssueIntent = new Intent(
							getApplicationContext(), ViewIssueActivity.class);
					openIssueIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					openIssueIntent
							.putExtra(MIssue.class.getName(), alarmIssue);
					startActivity(openIssueIntent);
				} else {
					alarmIssue = (MLocalIssue) issue;
					srvName = ID_LOCAL;
					this.selectServer(1);
					Intent i = new Intent(getApplicationContext(),
							EditIssueActivity.class);
					i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					i.putExtra(MIssue.class.getName(), alarmIssue);
					i.putExtra("SRVNAME", "");
					startActivity(i);
				}
			} else {
				issue = (MIssue) intent.getSerializableExtra(DUE_TASK);
				if (issue != null) {
					bNeedShowOverDue = true;
				}
			}
		}
	};

	private class DrawerItemClickListener implements
			ListView.OnItemClickListener {
		@Override
		public void onItemClick(AdapterView parent, View view, int position,
				long id) {
			selectServer(position);
		}
	}

	private void initLocalTasks(int iActive) {
		if (_menu != null) {
			_menu.getItem(1).setChecked(iActive == 0);
			_menu.getItem(2).setChecked(iActive == 1);
			_menu.getItem(3).setChecked(iActive == 2);
			String act_title = getString(R.string.tab_local) + "(";
			if (iActive == 0) {
				act_title = act_title + getString(R.string.filter_active) + ")";
			} else if (iActive == 1) {
				act_title = act_title + getString(R.string.filter_closed) + ")";
			} else {
				act_title = act_title + getString(R.string.filter_overdue)
						+ ")";
			}
			this.setTitle(act_title);
		}
	}

	private void actLocalMenu(boolean bOn) {
		if (_menu == null)
			return;
		_menu.getItem(1).setVisible(bOn);
		_menu.getItem(2).setVisible(bOn);
		_menu.getItem(3).setVisible(bOn);
	}

	/** Swaps fragments in the main content view */
	private void selectServer(int position) {
		Intent i = null;

		if (position < mDrawerServers.size() - 4) {

			curServerName = "";
			oldItemPosition = position;

			FragmentManager fragmentManager = getSupportFragmentManager();
			FragmentTransaction transaction = fragmentManager
					.beginTransaction();

			if (curfragment != null) {
				transaction.detach(curfragment);
				curfragment = null;
			}
			String connectionId = m_connectionIds.get(position);
			mDrawerServerListView.setItemChecked(position, true);
			boolean bShowPlusButton = false;
			{
				if (ID_LOCAL.equals(connectionId)) {
					curfragment = LocalTasksFragment.newInstance();
					((IRetainedFragment) curfragment).getData().setActiveIssue(
							0);
					initLocalTasks(0);
					bShowPlusButton = true;
				} else if (ID_NOT_CONFIGURED.equals(connectionId)) {
					actLocalMenu(false);
					curfragment = NoConnectionFragment.newInstance();
					this.setTitle(mDrawerServers.get(position).getName());
				} else {
					actLocalMenu(false);
					InboxFragment f = new InboxFragment();
					Bundle args = new Bundle();
					args.putString(SrvConnectionId.ARG, connectionId);
					f.setArguments(args);
					curfragment = f;
					bShowPlusButton = true;
					curServerName = m_connectionIds.get(position);
					this.setTitle(mDrawerServers.get(position).getName());
				}
			}
			if (curfragment == null) {
				return;
			}

			((IRetainedFragment) curfragment).getData().setItemNumber(position);

			transaction.replace(R.id.content_frame, curfragment, "data");
			// transaction.addToBackStack(null);
			transaction.commit();

			if (fabButton != null) {
				if (ID_LOCAL.equals(connectionId)) {
					if (_menu != null) {
						actLocalMenu(true);
					}
				}
				if (bShowPlusButton) {
					fabButton.showFloatingActionButton();
				} else {
					fabButton.hideFloatingActionButton();
				}
			}

		} else if (position == mDrawerServers.size() - 3) { // Settings
			i = new Intent(this, SettingsActivity.class);
			i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			this.startActivity(i);
		} else if (position == mDrawerServers.size() - 2) { // Send log by email
			SendErrorLog();
		} else if (position == mDrawerServers.size() - 1) {
			i = new Intent(getApplicationContext(), InfoActivity.class); // About
			i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			getApplicationContext().startActivity(i);
		}
		mDrawerLayout.closeDrawer(mDrawerServerListView);
	}

	private void createUi() {
		setContentView(R.layout.activity_main);

		createConnections();
		Drawable newTaskIcon = getResources()
				.getDrawable(R.drawable.ic_addtask);
		fabButton = new FloatingActionButton.Builder(this)
				.withDrawable(newTaskIcon).withButtonColor(Color.RED)
				.withGravity(Gravity.BOTTOM | Gravity.RIGHT)
				.withMargins(0, 0, 16, 16).create();

		fabButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				createNewIssue();
				return;
			}
		});
		if (m_sessionManager.getShowNewTask()) {
			showNewTaskNotification();
		}
	}

	public void showNewTaskNotification() {

		Intent i = new Intent(getApplicationContext(), EditIssueActivity.class);
		PendingIntent contentIntent = PendingIntent.getActivity(this, 0, i,
				PendingIntent.FLAG_UPDATE_CURRENT);

		Bitmap bm = BitmapFactory.decodeResource(getResources(),
				R.drawable.ic_addtask_red_xxx);
		NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(
				this).setSmallIcon(R.drawable.ic_newtasks_notification)
				.setContentTitle(getString(R.string.app_name))
				.setContentText(getString(R.string.new_task_notif))
				.setAutoCancel(false).setOngoing(true);

		mBuilder.setContentIntent(contentIntent);
		Notification notif = mBuilder.build();
		notif.contentView.setImageViewBitmap(android.R.id.icon, bm);
		// setImageViewResource(android.R.id.icon, R.drawable.ic_addtask_red);
		NotificationManager mNotificationManager = (NotificationManager) this
				.getSystemService(Context.NOTIFICATION_SERVICE);
		mNotificationManager.notify(SessionManager.NEW_TASK_ACTIVITY_ID, notif);
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

	private DrawerLayout mDrawerLayout;
	private ActionBarDrawerToggle mDrawerToggle;
	private LayoutInflater mInflater;

	public class ViewHolder {
		public TextView textViewTitle;
		public TextView textView;
		public TextView textViewEmail;
		public TextView textStatusView;
	}

	private View drawTitleItem(ViewHolder holder, String strTitle) {
		View convertView = mInflater
				.inflate(R.layout.drawer_list_sep_srv, null);
		holder.textView = (TextView) convertView
				.findViewById(R.id.textDrawerView);
		holder.textView.setText("");
		holder.textView.setHeight(1);
		holder.textViewEmail = (TextView) convertView
				.findViewById(R.id.txtDrawerItememail);
		if (holder.textViewEmail != null) {
			holder.textViewEmail.setText("");
			holder.textViewEmail.setHeight(1);
		}
		convertView.setBackgroundColor(Color.DKGRAY);
		holder.textViewTitle = (TextView) convertView
				.findViewById(R.id.textDrawerViewTitle);
		holder.textViewTitle.setText(strTitle);
		return convertView;
	}

	private DrawerItemData getServerStatusCaption(ServerListData sld) {
		if (sld.getSyncDate() > 0) {
			return sld.getSyncDateCaption(this);
		} else {
			return new DrawerItemData();
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		m_sessionManager = SessionManager.get(this);

		// set a custom shadow that overlays the main content when the drawer
		// opens
		SyncAlarmReceiver receiver = new SyncAlarmReceiver();
		if (m_sessionManager.getSyncIntervalMin() > -1) {
			if (!receiver.isAlarmEnabled(getApplicationContext()))
				receiver.setAlarm(getApplicationContext());
		} else
			receiver.cancelAlarm(getApplicationContext());

		createUi();

		mDrawerLayout = (DrawerLayout) findViewById(R.id.activity_main);
		mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow,
				GravityCompat.START);

		mInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mDrawerServerListView = (ListView) findViewById(R.id.left_drawer_servers);
		ldp = new ArrayAdapter<ServerListData>(this, R.layout.drawer_list_item,
				mDrawerServers) {
			@Override
			public boolean isEnabled(int position) {

				return (position > 0)
						&& (position != mDrawerServers.size() - 4);
			}

			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				ViewHolder holder = null;
				if (convertView == null) {
					holder = new ViewHolder();
					if (position == mDrawerServers.size() - 4) {
						convertView = drawTitleItem(holder,
								getString(R.string.group_mngr));
					} else if (position == 0) {
						convertView = drawTitleItem(holder,
								getString(R.string.group_srv));
					} else if (position == 1
							|| position > mDrawerServers.size() - 4) {
						convertView = mInflater.inflate(
								R.layout.drawer_list_item_set, null);
						holder.textView = (TextView) convertView
								.findViewById(R.id.textDrawerView);
						holder.textViewEmail = (TextView) convertView
								.findViewById(R.id.txtDrawerItememail);
					} else {
						convertView = mInflater.inflate(
								R.layout.drawer_list_item, null);
						holder.textView = (TextView) convertView
								.findViewById(R.id.textDrawerView);
						holder.textViewEmail = (TextView) convertView
								.findViewById(R.id.txtDrawerItememail);
						holder.textStatusView = (TextView) convertView
								.findViewById(R.id.textStatusDrawerView);
						UpdateItemStatusText(holder,
								mDrawerServers.get(position));
						if (holder.textViewEmail != null) {
							holder.textViewEmail.setText(mDrawerServers.get(
									position).getEmail());
						}
					}
					convertView.setTag(holder);
				} else {
					holder = (ViewHolder) convertView.getTag();
					if (position > 1 && position < mDrawerServers.size() - 4) {
						UpdateItemStatusText(holder,
								mDrawerServers.get(position));
					}
				}
				holder.textView.setText(mDrawerServers.get(position).getName());
				return convertView;
			}
		};

		mDrawerServerListView.setAdapter(ldp);

		Intent cIntent = getIntent();
		if (cIntent != null)
			onNewIntent(cIntent); // we need to call this for the case when
									// Activity is started for the first
									// time

		mDrawerServerListView
				.setOnItemClickListener(new DrawerItemClickListener());

		// enable ActionBar app icon to behave as action to toggle nav drawer
		getActionBar().setDisplayHomeAsUpEnabled(true);
		getActionBar().setHomeButtonEnabled(true);

		mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
				R.drawable.ic_drawer, 0, 0) {
			/** Called when a drawer has settled in a completely open state. */
			@Override
			public void onDrawerOpened(View drawerView) {
				super.onDrawerOpened(drawerView);
				if (ldp != null) {
					ldp.notifyDataSetChanged();
				}

			}
		};

		mDrawerLayout.setDrawerListener(mDrawerToggle);

		if (savedInstanceState == null && cIntent == null) {
			selectServer(1);
		} else {
			FragmentManager fragmentManager = MainActivity.this
					.getSupportFragmentManager();
			curfragment = fragmentManager.findFragmentByTag("data");
			if (curfragment != null) {
				InitFragmentData();
			} else {
				oldItemPosition = 1;
				boolean bSrvFound = false;
				if (cIntent != null) {
					MIssue issue = (MIssue) cIntent
							.getSerializableExtra(ALARM_TASK);
					if (issue != null) {
						if (issue instanceof MRemoteSyncedIssue) {
							MRemoteSyncedIssue alarmIssue = (MRemoteSyncedIssue) issue;
							String srvName = ((MRemoteSyncedIssue) alarmIssue)
									.getSrvConnId();
							for (int i = 1; i < m_connectionIds.size(); i++) {
								if (srvName.equals(m_connectionIds.get(i))) {
									this.selectServer(i);
									bSrvFound = true;
									break;
								}
							}
						}
					}
				}
				if (!bSrvFound) {
					selectServer(oldItemPosition);
				}
			}
		}

	}

	private void UpdateItemStatusText(ViewHolder holder, ServerListData sld) {
		if (holder == null)
			return;
		if (sld == null)
			return;
		DrawerItemData dw = getServerStatusCaption(sld);
		if (dw != null) {
			holder.textStatusView.setText(dw.getText() + " ");
			holder.textStatusView.setTextColor(dw.getColor());
		} else {
			holder.textStatusView.setText("");
		}
		if (sld.getStatus() != "") {
			holder.textStatusView.setText("X");
			holder.textStatusView.setTextColor(Color.RED);
		}
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		// Sync the toggle state after onRestoreInstanceState has occurred.
		mDrawerToggle.syncState();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		// Pass any configuration change to the drawer toggles
		mDrawerToggle.onConfigurationChanged(newConfig);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		_menu = menu;

		if (curfragment != null) {
			IssueListData sData = ((IRetainedFragment) curfragment).getData();
			InitFragmentData();
		}

		return super.onCreateOptionsMenu(menu);
	}

	private void InitFragmentData() {
		IssueListData sData = ((IRetainedFragment) curfragment).getData();
		if (sData != null) {
			oldItemPosition = sData.getItemNumber();
			if (oldItemPosition == 1) {
				actLocalMenu(true);
				if (bNeedShowOverDue) {
					onOptionsItemSelected(this.getMenu().getItem(3));
					bNeedShowOverDue = false;
				} else {
					initLocalTasks(sData.getActiveIssue());
				}
			} else {
				actLocalMenu(false);
				this.setTitle(mDrawerServers.get(oldItemPosition).getName());
			}
		}
	}

	public Menu getMenu() {
		// use it like this
		return _menu;
	}

	private ArrayList<String> m_syncingIds = new ArrayList<String>();

	private void syncronize(final String connectionId,
			final AsyncCallback<String> finishedCallback) {
		log.debug("syncronize");
		SrvConnectionBaseData base = m_sessionManager.getBaseData(connectionId);

		final MSynchronizeRequestEx request = new MSynchronizeRequestEx();
		ILocalBL localBL = BL.getLocal(getApplicationContext());

		SessionAuthScheme scheme = new SessionAuthScheme();
		scheme.setSessionId(base.getSessionId());
		request.setAuthScheme(scheme);

		request.setServerUrl(m_sessionManager.getServerUrl(connectionId));
		request.setLocale(getResources().getConfiguration().locale);

		final List<MNewComment> newComments = localBL.getNewComments();
		final List<MRemoteNotSyncedIssue> newTasks = localBL
				.getNotSyncedTasks();

		request.setWorkingSetsDigest(m_sessionManager.getWorkingSets(
				connectionId).getDigest());
		request.setProjectsDigest(m_sessionManager.getMyProjects(connectionId)
				.getDigest());
		request.setIssuesDigest(m_sessionManager.getIssues(connectionId)
				.getDigest());

		final MCreateRequestEx cr = new MCreateRequestEx();

		cr.setAuthScheme(scheme);

		cr.setLocale(getResources().getConfiguration().locale);
		cr.setServerUrl(m_sessionManager.getServerUrl(connectionId));
		cr.setNewComments(newComments);
		cr.setNewIssues(newTasks);

		BatchRequest batch = new BatchRequest();
		batch.setCreateRequest(cr);
		batch.setSyncRequest(request);

		BL.getServer(getApplicationContext()).synchronize(batch,
				new AsyncCallback<MSynchronizeResponseEx>() {
					@Override
					public void onSuccess(MSynchronizeResponseEx response) {

						ILocalBL bl = BL.getLocal(getApplicationContext());

						bl.handleCreateResult(cr, response.getCommentRes(),
								response.getTaskRes());

						String data = response.getData();

						ResponseParser.parseSyncResponse(m_sessionManager,
								connectionId, data);

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
		}
	}

	protected void syncronize() {

		if (m_syncingIds.size() > 0)
			return; // Sync in progress

		for (final String connId : m_sessionManager.getConnections()) {

			syncronize(connId, new AsyncCallback<String>() {

				@Override
				public void onSuccess(String data) {
					syncFinishedForCon(connId);
					UpdateServerListSuccess(connId);
				}

				@Override
				public void onFailure(Throwable e) {
					log.error(e);
					syncFinishedForCon(connId);
					if (e instanceof EAuthError) {
						Toast.makeText(getApplicationContext(),
								getString(R.string.authentication_failed),
								Toast.LENGTH_LONG).show();
						UpdateServerListError(connId,
								getString(R.string.authentication_failed),
								false);
					} else if (e instanceof EServerOutDate) {
						String newmsg = ((EServerOutDate) e)
								.GetErrorText(getApplicationContext());
						Toast.makeText(getApplicationContext(), newmsg,
								Toast.LENGTH_LONG).show();
						UpdateServerListError(connId, newmsg, false);
					} else if (e instanceof EAppSyncWarning) {
						String newmsg = ((EAppSyncWarning) e)
								.GetErrorText(getApplicationContext());
						Toast.makeText(getApplicationContext(), newmsg,
								Toast.LENGTH_LONG).show();
						UpdateServerListError(connId, newmsg, true);
					} else if (e.getCause() instanceof UnknownHostException) {
						Toast.makeText(getApplicationContext(),
								getString(R.string.network_error),
								Toast.LENGTH_LONG).show();
						UpdateServerListError(connId,
								getString(R.string.network_error), false);
					} else {
						Toast.makeText(getApplicationContext(),
								getString(R.string.network_error),
								Toast.LENGTH_LONG).show();
						UpdateServerListError(connId, e.getMessage(), false);
					}
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
		Intent i = new Intent(getApplicationContext(), SigninActivity.class);
		i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		finish();
		getApplicationContext().startActivity(i);
	}

	private Fragment getVisibleFragment() {
		FragmentManager fragmentManager = MainActivity.this
				.getSupportFragmentManager();
		List<Fragment> fragments = fragmentManager.getFragments();
		for (Fragment fragment : fragments) {
			if (fragment != null && fragment.isVisible())
				return fragment;
		}
		return null;
	}

	@Override
	// Menu selector
	public boolean onOptionsItemSelected(MenuItem item) {
		if (mDrawerToggle.onOptionsItemSelected(item)) {
			return true;
		}

		switch (item.getItemId()) {
		case R.id.action_active_tasks:
			LocalTasksFragment curFragmentAc = (LocalTasksFragment) getVisibleFragment();
			if (curFragmentAc != null) {
				curFragmentAc.setTaskListType(TasksFilter.ACTIVE);
				((IRetainedFragment) curFragmentAc).getData().setActiveIssue(0);
				initLocalTasks(0);
			}
			break;
		case R.id.action_overdue_tasks:
			LocalTasksFragment curFragmentDue = (LocalTasksFragment) getVisibleFragment();
			if (curFragmentDue != null) {
				curFragmentDue.setTaskListType(TasksFilter.OVERDUE);
				((IRetainedFragment) curFragmentDue).getData()
						.setActiveIssue(2);
				initLocalTasks(2);
			}
			break;
		case R.id.action_closed_tasks:
			LocalTasksFragment curFragmentCl = (LocalTasksFragment) getVisibleFragment();
			if (curFragmentCl != null) {
				curFragmentCl.setTaskListType(TasksFilter.CLOSED);
				((IRetainedFragment) curFragmentCl).getData().setActiveIssue(1);
				initLocalTasks(1);
			}
			break;
		case R.id.action_send_log:
			SendErrorLog();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	public void createNewIssue() {
		Intent i = new Intent(getApplicationContext(), EditIssueActivity.class);
		i.putExtra("SRVNAME", curServerName);
		startActivityForResult(i, ActivityReq.NEW_ISSUE);
	}

	public void SendErrorLog() {
		File Attdir = null;
		if (getApplicationContext().getExternalFilesDir(null) == null) {
			Attdir = new File(getApplicationContext().getFilesDir()
					.getAbsolutePath());
		} else {
			Attdir = new File(getApplicationContext().getExternalFilesDir(null)
					.getAbsolutePath());
		}
		File logfile = new File(Attdir.getPath() + "/pklog.txt");
		if (logfile.exists()) {
			Intent i = new Intent(Intent.ACTION_SEND);
			i.setType("vnd.android.cursor.dir/email");
			i.putExtra(Intent.EXTRA_EMAIL, new String[] { triniforce_email });
			i.putExtra(Intent.EXTRA_SUBJECT, "");
			StringBuilder sb = new StringBuilder();
			sb.append("Brand: " + android.os.Build.BRAND);
			sb.append("\n");
			sb.append("Device: " + android.os.Build.DEVICE);
			sb.append("\n");
			sb.append("Manufactirer: " + android.os.Build.MANUFACTURER);
			sb.append("\n");
			sb.append("Model: " + android.os.Build.MODEL);
			sb.append("\n");
			i.putExtra(Intent.EXTRA_TEXT,
					getString(R.string.action_errorlog_caption));
			i.putExtra(Intent.EXTRA_TEXT, sb.toString());
			Uri uri = Uri.parse("file://" + logfile.getAbsolutePath());
			i.putExtra(Intent.EXTRA_STREAM, uri);
			try {
				startActivity(Intent.createChooser(i,
						getString(R.string.action_errorlog_sent)));
			} catch (android.content.ActivityNotFoundException ex) {
				Toast.makeText(MainActivity.this,
						R.string.action_no_email_client, Toast.LENGTH_SHORT)
						.show();
			}
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

	public void RefreshItemList() {
		raise(new LocalTaskAdded());
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
