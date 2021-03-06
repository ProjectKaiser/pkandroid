package com.projectkaiser.app_android;

import java.util.List;

import android.annotation.TargetApi;
import android.app.NotificationManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.os.Build;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.CheckBoxPreference;
import android.support.v4.app.NotificationCompat;
import android.view.MenuItem;

import com.projectkaiser.app_android.services.SyncAlarmReceiver;
import com.projectkaiser.app_android.settings.SessionManager;

/**
 * A {@link PreferenceActivity} that presents a set of application settings. On
 * handset devices, settings are presented as a single list. On tablets,
 * settings are split by category, with category headers shown to the left of
 * the list of settings.
 * <p>
 * See <a href="http://developer.android.com/design/patterns/settings.html">
 * Android Design: Settings</a> for design guidelines and the <a
 * href="http://developer.android.com/guide/topics/ui/settings.html">Settings
 * API Guide</a> for more information on developing a Settings UI.
 */
public class SettingsActivity extends PreferenceActivity {
	/**
	 * Determines whether to always show the simplified settings UI, where
	 * settings are presented in a single list. When false, settings are shown
	 * as a master/detail two-pane view on tablets. When true, a single pane is
	 * shown on tablets.
	 */
	private static final boolean ALWAYS_SIMPLE_PREFS = false;

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		setupSimplePreferencesScreen();

		getActionBar().setDisplayHomeAsUpEnabled(true);
		getActionBar().setHomeButtonEnabled(true);
	}

	/**
	 * Shows the simplified settings UI if the device configuration if the
	 * device configuration dictates that a simplified, single-pane UI should be
	 * shown.
	 */
	private void setupSimplePreferencesScreen() {
		if (!isSimplePreferences(this)) {
			return;
		}

		// In the simplified UI, fragments are not used at all and we instead
		// use the older PreferenceActivity APIs.

		// Add 'general' preferences.
		addPreferencesFromResource(R.xml.pref_general);

		// Bind the summaries of EditText/List/Dialog/Ringtone preferences to
		// their values. When their values change, their summaries are updated
		// to reflect the new value, per the Android Design guidelines.

		SessionManager sm = SessionManager.get(this);
		Preference pInterval = findPreference(SessionManager.KEY_SYNC_INTERVAL);
		String defValue = String.valueOf(sm.getSyncIntervalMin());
		((ListPreference) pInterval).setValue(defValue);
		pInterval.setDefaultValue(defValue);
		bindPreferenceSummaryToValue(pInterval, defValue);

		Preference pTimeNotif = findPreference(SessionManager.KEY_TIME_NOTIF);
		String defHourNotif = String.valueOf(sm.getTimeNotif());
		((ListPreference) pTimeNotif).setValue(defHourNotif);
		pTimeNotif.setDefaultValue(defHourNotif);
		bindPreferenceSummaryToValue(pTimeNotif, defHourNotif);

		Preference pNewTask = findPreference(SessionManager.KEY_SHOW_NEW_TASK);
		Boolean defBoolValue = Boolean.valueOf(sm.getShowNewTask());
		((CheckBoxPreference) pNewTask).setChecked(false);
		pInterval.setDefaultValue(false);
		bindPreferenceNewTaskToValue(pNewTask, defBoolValue);
		pInterval.setPersistent(false);
	}

	/** {@inheritDoc} */
	@Override
	public boolean onIsMultiPane() {
		return isXLargeTablet(this) && !isSimplePreferences(this);
	}

	/**
	 * Helper method to determine if the device has an extra-large screen. For
	 * example, 10" tablets are extra-large.
	 */
	private static boolean isXLargeTablet(Context context) {
		return (context.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_XLARGE;
	}

	/**
	 * Determines whether the simplified settings UI should be shown. This is
	 * true if this is forced via {@link #ALWAYS_SIMPLE_PREFS}, or the device
	 * doesn't have newer APIs like {@link PreferenceFragment}, or the device
	 * doesn't have an extra-large screen. In these cases, a single-pane
	 * "simplified" settings UI should be shown.
	 */
	private static boolean isSimplePreferences(Context context) {
		return ALWAYS_SIMPLE_PREFS
				|| Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB
				|| !isXLargeTablet(context);
	}

	/** {@inheritDoc} */
	@Override
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public void onBuildHeaders(List<Header> target) {
		if (!isSimplePreferences(this)) {
			loadHeadersFromResource(R.xml.pref_headers, target);
		}
	}

	private static void updateSummary(Preference preference, String stringValue) {

		if (preference instanceof ListPreference) {

			ListPreference listPreference = (ListPreference) preference;
			int index = listPreference.findIndexOfValue(stringValue);
			preference
					.setSummary(index >= 0 ? listPreference.getEntries()[index]
							: null);

		} else {
			preference.setSummary(stringValue);
		}
	}

	private void hideNewTaskNotification() {
		NotificationManager mNotificationManager = (NotificationManager) this
				.getSystemService(Context.NOTIFICATION_SERVICE);
		mNotificationManager.cancel(SessionManager.NEW_TASK_ACTIVITY_ID);
	}

	public static int calculateInSampleSize(BitmapFactory.Options options,
			int reqWidth, int reqHeight) {
		// Raw height and width of image
		final int height = options.outHeight;
		final int width = options.outWidth;
		int inSampleSize = 1;

		if (height > reqHeight || width > reqWidth) {

			final int halfHeight = height / 2;
			final int halfWidth = width / 2;

			// Calculate the largest inSampleSize value that is a power of 2 and
			// keeps both
			// height and width larger than the requested height and width.
			while ((halfHeight / inSampleSize) > reqHeight
					&& (halfWidth / inSampleSize) > reqWidth) {
				inSampleSize *= 2;
			}
		}

		return inSampleSize;
	}

	private void showNewTaskNotification() {

		Intent i = new Intent(getApplicationContext(), EditIssueActivity.class);
		PendingIntent contentIntent = PendingIntent.getActivity(this, 0, i,
				PendingIntent.FLAG_UPDATE_CURRENT);

		Bitmap bm = BitmapFactory.decodeResource(getResources(),
				R.drawable.ic_addtask_white_xxx);

		NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(
				this).setSmallIcon(R.drawable.ic_newtasks_notification)
				.setContentTitle(getString(R.string.app_name))
				.setContentText(getString(R.string.new_task_notif))
				.setAutoCancel(false).setOngoing(true);

		mBuilder.setContentIntent(contentIntent);
		Notification notif = mBuilder.build();
		notif.contentView.setImageViewBitmap(android.R.id.icon, bm);
		NotificationManager mNotificationManager = (NotificationManager) this
				.getSystemService(Context.NOTIFICATION_SERVICE);
		mNotificationManager.notify(SessionManager.NEW_TASK_ACTIVITY_ID, notif);
	}

	private static void savePreference(Preference preference, String stringValue) {
		SessionManager sm = SessionManager.get(preference.getContext());
		if (SessionManager.KEY_SYNC_INTERVAL.equalsIgnoreCase(preference
				.getKey())) {
			int intervalMinutes = Integer.valueOf(stringValue);
			sm.setSyncIntervalMin(intervalMinutes);
			SyncAlarmReceiver receiver = new SyncAlarmReceiver();
			receiver.cancelAlarm(preference.getContext());
			if (intervalMinutes > 0)
				receiver.setAlarm(preference.getContext());
		} else	if (SessionManager.KEY_TIME_NOTIF.equalsIgnoreCase(preference
					.getKey())) {
				int hourNotif = Integer.valueOf(stringValue);
				sm.setTimeNotif(hourNotif);
				SyncAlarmReceiver receiver = new SyncAlarmReceiver();
				receiver.cancelAlarm(preference.getContext());
				if (hourNotif > 0)
					receiver.setAlarm(preference.getContext());
				
		} else if (SessionManager.KEY_SHOW_NEW_TASK.equalsIgnoreCase(preference
				.getKey())) {
			boolean showNewTask = Boolean.valueOf(stringValue);
			sm.setShowNewTask(showNewTask);
			if (showNewTask) {
				((SettingsActivity) preference.getContext())
						.showNewTaskNotification();
			} else {
				((SettingsActivity) preference.getContext())
						.hideNewTaskNotification();
			}
		}

	}

	/**
	 * A preference value change listener that updates the preference's summary
	 * to reflect its new value.
	 */
	private static Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener = new Preference.OnPreferenceChangeListener() {
		@Override
		public boolean onPreferenceChange(Preference preference, Object value) {
			String stringValue = value.toString();
			if (!preference.getKey().equals("new_task_block")) {
				updateSummary(preference, stringValue);
			}
			savePreference(preference, stringValue);
			return true;
		}
	};

	/**
	 * Binds a preference's summary to its value. More specifically, when the
	 * preference's value is changed, its summary (line of text below the
	 * preference title) is updated to reflect the value. The summary is also
	 * immediately updated upon calling this method. The exact display format is
	 * dependent on the type of preference.
	 * 
	 * @see #sBindPreferenceSummaryToValueListener
	 */

	private static void bindPreferenceNewTaskToValue(Preference preference,
			Boolean initialValue) {
		preference
				.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);

		((CheckBoxPreference) preference).setChecked(initialValue);
	}

	private static void bindPreferenceSummaryToValue(Preference preference,
			String initialValue) {
		// Set the listener to watch for value changes.
		preference
				.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);

		updateSummary(preference, initialValue);
	}

	/**
	 * This fragment shows general preferences only. It is used when the
	 * activity is showing a two-pane settings UI.
	 */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public static class GeneralPreferenceFragment extends PreferenceFragment {
		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			addPreferencesFromResource(R.xml.pref_general);

			// Bind the summaries of EditText/List/Dialog/Ringtone preferences
			// to their values. When their values change, their summaries are
			// updated to reflect the new value, per the Android Design
			// guidelines.
			Preference pInterval = findPreference(SessionManager.KEY_SYNC_INTERVAL);
			String defValue = String.valueOf(SessionManager.get(getActivity())
					.getSyncIntervalMin());
			pInterval.setDefaultValue(defValue);
			((ListPreference) pInterval).setValue(defValue);
			bindPreferenceSummaryToValue(pInterval, defValue);
			pInterval.setPersistent(false);
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			onBackPressed();
		}
		return true;
	}

	/*
	 * @Override public void onBackPressed() { Intent i = new
	 * Intent(getApplicationContext(), MainActivity.class);
	 * i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
	 * i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
	 * getApplicationContext().startActivity(i); finish(); }
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

}
