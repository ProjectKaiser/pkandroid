package com.projectkaiser.app_android.services;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;

import android.app.ActivityManager;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.PowerManager;
import android.support.v4.app.NotificationCompat;

import com.projectkaiser.app_android.MainActivity;
import com.projectkaiser.app_android.R;
import com.projectkaiser.app_android.bl.BL;
import com.projectkaiser.app_android.bl.local.ILocalBL;
import com.projectkaiser.app_android.bl.local.TasksFilter;
import com.projectkaiser.app_android.bl.obj.MNewComment;
import com.projectkaiser.app_android.bl.obj.MRemoteNotSyncedIssue;
import com.projectkaiser.app_android.bl.obj.MRemoteSyncedIssue;
import com.projectkaiser.app_android.jsonapi.parser.ResponseParser;
import com.projectkaiser.app_android.jsonrpc.JsonRPC;
import com.projectkaiser.app_android.jsonrpc.auth.SessionAuthScheme;
import com.projectkaiser.app_android.settings.CreateFilesResultParser;
import com.projectkaiser.app_android.settings.SessionManager;
import com.projectkaiser.app_android.settings.SrvConnectionBaseData;
import com.projectkaiser.mobile.sync.MCreateRequestEx;
import com.projectkaiser.mobile.sync.MDigestedArray;
import com.projectkaiser.mobile.sync.MIssue;
import com.projectkaiser.mobile.sync.MLocalIssue;
import com.projectkaiser.mobile.sync.MMyProject;
import com.projectkaiser.mobile.sync.MSynchronizeRequestEx;
import com.projectkaiser.mobile.sync.MWorkingSets;

public class SyncService extends IntentService {

	public static final int NOTIFICATION_ID = 1;
	public static final String TAG = "PK Sync Service";

	private NotificationManager mNotificationManager;
	private final Logger log = Logger.getLogger(SyncService.class);

	public SyncService() {
		super("ProjectKaiserSyncService");
	}

	private void sendNotification(String title, String msg, MIssue issue) {

		mNotificationManager = (NotificationManager) this
				.getSystemService(Context.NOTIFICATION_SERVICE);
		Intent activityIntent = new Intent(this, MainActivity.class);
		activityIntent.putExtra(MainActivity.ALARM_TASK, issue);

		PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
				activityIntent, PendingIntent.FLAG_UPDATE_CURRENT);

		Uri alarmSound = RingtoneManager
				.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

		Bitmap bm = BitmapFactory.decodeResource(getResources(), R.drawable.ic_newtasks_notification);
		NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(
				this).setSmallIcon(R.drawable.ic_newtasks_notification)
				.setContentTitle(title)
				.setStyle(new NotificationCompat.BigTextStyle().bigText(msg))
				.setContentText(msg).setSound(alarmSound).setAutoCancel(true);

		mBuilder.setContentIntent(contentIntent);
		Notification notif = mBuilder.build();
		notif.contentView.setImageViewBitmap(android.R.id.icon, bm);
		mNotificationManager.notify(NOTIFICATION_ID, notif);
	}

	private void sendDueNotification(String title, String msg, MIssue issue) {

		mNotificationManager = (NotificationManager) this
				.getSystemService(Context.NOTIFICATION_SERVICE);
		Intent activityIntent = new Intent(this, MainActivity.class);
		activityIntent.putExtra(MainActivity.DUE_TASK, issue);

		PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
				activityIntent, PendingIntent.FLAG_UPDATE_CURRENT);

		Uri alarmSound = RingtoneManager
				.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

		Bitmap bm = BitmapFactory.decodeResource(getResources(), R.drawable.id_calendar);
		NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(
				this).setSmallIcon(R.drawable.ic_newtasks_notification)
				.setContentTitle(title)
				.setStyle(new NotificationCompat.BigTextStyle().bigText(msg))
				.setContentText(msg).setSound(alarmSound).setAutoCancel(true);

		mBuilder.setContentIntent(contentIntent);
		Notification notif = mBuilder.build();
		notif.contentView.setImageViewBitmap(android.R.id.icon, bm);
		mNotificationManager.notify(NOTIFICATION_ID, notif);
	}

	private Date getNewestTaskDate(MDigestedArray<MRemoteSyncedIssue> issues) {

		Date d = new Date(0);

		for (MRemoteSyncedIssue issue : issues.getItems()) {
			Date mod = new Date(issue.getModified());
			if (mod.after(d))
				d = mod;
		}

		return d;

	}

	private void sendNotifications(String serverName,
			MDigestedArray<MRemoteSyncedIssue> oldData,
			MDigestedArray<MRemoteSyncedIssue> newData, Long currentUserId) {
		Date oldDate = getNewestTaskDate(oldData);
		ArrayList<MRemoteSyncedIssue> newTasks = new ArrayList<MRemoteSyncedIssue>();

		// Searching new arrived tasks
		for (MRemoteSyncedIssue issue : newData.getItems()) {
			Date mod = new Date(issue.getModified());

			// if (mod.after(oldDate)) {
			if (mod.after(oldDate)
					&& !issue.getModifier().equals(currentUserId)) {
				newTasks.add(issue);
			}
		}

		if (newTasks.size() > 0) {
			StringBuilder text = new StringBuilder();
			text.append(newTasks.get(0).getName());
			if (newTasks.size() > 1)
				text.append(getString(R.string.notification_new_tasks) + "("
						+ String.valueOf(newTasks.size() - 1) + ")");
			sendNotification(serverName, text.toString(), newTasks.get(0));
		}
	}

	public boolean isForeground(String myPackage) {
		ActivityManager manager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
		List<ActivityManager.RunningTaskInfo> runningTaskInfo = manager
				.getRunningTasks(1);

		ComponentName componentInfo = runningTaskInfo.get(0).topActivity;
		if (componentInfo.getPackageName().equals(myPackage))
			return true;
		return false;
	}

	private void createFiles(String connectionId, SessionManager sm,
			SrvConnectionBaseData base) {
		ILocalBL bl = BL.getLocal(getApplicationContext());
		final List<MNewComment> newComments = bl.getNewComments();
		final List<MRemoteNotSyncedIssue> newTasks = bl.getNotSyncedTasks();

		if (newComments.size() == 0 && newTasks.size() == 0)
			return;

		MCreateRequestEx request = new MCreateRequestEx();

		SessionAuthScheme scheme = new SessionAuthScheme();
		scheme.setSessionId(base.getSessionId());
		request.setAuthScheme(scheme);

		request.setServerUrl(sm.getServerUrl(connectionId));
		request.setLocale(getResources().getConfiguration().locale);

		request.setNewComments(newComments);
		request.setNewIssues(newTasks);

		JsonRPC rpc = new JsonRPC();
		List<Object> commentsRes = new ArrayList<Object>();
		List<Object> tasksRes = new ArrayList<Object>();

		CreateFilesResultParser.parseResponse(rpc.rpc_create(request),
				commentsRes, tasksRes);

		bl.handleCreateResult(request, commentsRes, tasksRes);
	}

	private void loadData(String connectionId, SessionManager sm,
			SrvConnectionBaseData base) {
		MSynchronizeRequestEx request = new MSynchronizeRequestEx();

		SessionAuthScheme scheme = new SessionAuthScheme();
		scheme.setSessionId(base.getSessionId());
		request.setAuthScheme(scheme);

		request.setServerUrl(sm.getServerUrl(connectionId));
		request.setLocale(getResources().getConfiguration().locale);

		MDigestedArray<MRemoteSyncedIssue> issues = sm.getIssues(connectionId);
		MDigestedArray<MMyProject> projects = sm.getMyProjects(connectionId);
		MWorkingSets workingSets = sm.getWorkingSets(connectionId);

		request.setIssuesDigest(issues.getDigest());
		request.setProjectsDigest(projects.getDigest());
		request.setWorkingSetsDigest(workingSets.getDigest());

		JsonRPC rpc = new JsonRPC();
		String result = rpc.rpc_syncronize(request);

		if (result != null && !result.equalsIgnoreCase("null")) {
			// null = no changes since last sync
			MDigestedArray<MRemoteSyncedIssue> oldIssues = sm
					.getIssues(connectionId);
			ResponseParser.parseSyncResponse(sm, connectionId, result);
			MDigestedArray<MRemoteSyncedIssue> newIssues = sm
					.getIssues(connectionId);
			sendNotifications(base.getServerName(), oldIssues, newIssues, base.getUserId());
			sm.updateLastSyncDate(connectionId);
		}
	}

	private void CheckLocalDueDates() {
		SessionManager sm = SessionManager.get(getApplicationContext());
		Integer hNotif = sm.getTimeNotif();
		if (hNotif == 0) {
			hNotif = 9;
		}
		if (hNotif == -1)
			return;

		Calendar calendar = Calendar.getInstance();
		Date curDT = new Date();
		calendar.setTime(curDT);
		if (calendar.get(Calendar.HOUR_OF_DAY) < hNotif)
			return;

		Date lastCheckDate = sm.getLastLocalDueDate();
		if (lastCheckDate != null) {
			Calendar calLast = Calendar.getInstance();
			calLast.setTime(lastCheckDate);
			if (calLast.get(Calendar.DATE) == calendar.get(Calendar.DATE) 
					&& calLast.get(Calendar.YEAR) == calendar.get(Calendar.YEAR)
					&& calLast.get(Calendar.MONTH) == calendar.get(Calendar.MONTH)) {
				return;
			}
		}
		
		sm.updateLastLocalDueDate();
		ILocalBL bl = BL.getLocal(getApplicationContext());
		ArrayList<MIssue> m_localtasks = new ArrayList<MIssue>();
		for (MLocalIssue li : bl.getLocalTasks(TasksFilter.ACTIVE)) {
			Date duedate = new Date(li.getDueDate());
			if (duedate.before(new Date()) && !sm.isLocalIssueNotified(li)) {
				m_localtasks.add(li);
				sm.setLocalIssueNotified(li);
			}
		}

		if (m_localtasks.size() > 0) {
			StringBuilder text = new StringBuilder();
			text.append(m_localtasks.get(0).getName());
			if (m_localtasks.size() > 1)
				text.append(getString(R.string.notification_new_tasks) + " ("
						+ String.valueOf(m_localtasks.size() - 1) + ")");
			sendDueNotification(getString(R.string.tab_local), text.toString(),m_localtasks.get(0));
		}
	}

	@Override
	protected void onHandleIntent(Intent intent) {

		try {

			PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);

			boolean isInteractive = pm.isScreenOn();
			boolean isAppRunning = isForeground("com.projectkaiser.app_android");

			if (isInteractive && isAppRunning) {
				log.debug("App is active, skipping sync");
				return;
			}

			SessionManager sm = SessionManager.get(getApplicationContext());
			Date now = new Date();
			Date lastSync = sm.getCommonLastSyncDate();
			boolean bNeedSync = (lastSync == null || now.getTime()
					- lastSync.getTime() >= SyncAlarmReceiver.SYNC_INTERVAL_MILLIS);

			if (bNeedSync) {
				CheckLocalDueDates();
				for (String connectionId : sm.getConnections()) {

					SrvConnectionBaseData base = sm.getBaseData(connectionId);

					createFiles(connectionId, sm, base);

					loadData(connectionId, sm, base);

				}
			}
		} catch (Throwable e) {
			log.error(e);
		} finally {
			SyncAlarmReceiver.completeWakefulIntent(intent);
		}
	}

}
