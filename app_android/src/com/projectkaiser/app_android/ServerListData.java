package com.projectkaiser.app_android;

import android.content.Context;
import android.graphics.Color;

import com.projectkaiser.app_android.DrawerItemData;
import com.projectkaiser.app_android.settings.SessionManager;
import java.lang.String;
import java.util.Calendar;

public class ServerListData {
	private String mName = "";
	private String mConnId = "";
	private SessionManager sm = null;

	public ServerListData(SessionManager _sm, String _Name, String connId) {
		mName = _Name;
		sm = _sm;
		mConnId = connId;
	}

	public String getName() {
		return mName;
	}

	public void setName(String value) {
		mName = value;
	}

	public String getStatus() {
		return sm.getLastSyncStatus(mConnId);
	}


	public long getSyncDate() {
		return sm.getLastSyncDate(mConnId).getTime();
	}

	public DrawerItemData getSyncDateCaption(Context ctx) {
		DrawerItemData drID = new DrawerItemData();
		if (ctx == null)
			return drID;
		long ms = getSyncDate();
		if (ms == 0)
			return drID;

		drID.setColor(Color.GRAY);
		Calendar eCalendar = Calendar.getInstance();
		Calendar sCalendar = Calendar.getInstance();
		sCalendar.setTimeInMillis(ms);

		int diffYear = eCalendar.get(Calendar.YEAR)
				- sCalendar.get(Calendar.YEAR);
		if (diffYear > 0) {
			drID.setText(diffYear + ctx.getString(R.string.short_year));
			return drID;
		}
		;
		int diffMonths = eCalendar.get(Calendar.MONTH)
				- sCalendar.get(Calendar.MONTH);
		if (diffMonths > 0) {
			drID.setText(diffMonths + ctx.getString(R.string.short_month));
			return drID;
		}
		int diffWeeks = eCalendar.get(Calendar.WEEK_OF_MONTH)
				- sCalendar.get(Calendar.WEEK_OF_MONTH);
		if (diffWeeks > 0) {
			drID.setText(diffWeeks + ctx.getString(R.string.short_week));
			return drID;
		}
		int diffdays = eCalendar.get(Calendar.DAY_OF_MONTH)
				- sCalendar.get(Calendar.DAY_OF_MONTH);
		if (diffdays > 0) {
			drID.setText(diffdays + ctx.getString(R.string.short_day));
			return drID;
		}
		int diffhours = eCalendar.get(Calendar.HOUR_OF_DAY)
				- sCalendar.get(Calendar.HOUR_OF_DAY);
		/*
		 * if (sm != null) { if (sm.getSyncIntervalMin() >= 60) { if (diffhours
		 * >= 0) { if (sm.getSyncIntervalMin() * 60 >= diffhours) {
		 * drID.setColor(Color.GREEN); } } }
		 * 
		 * }
		 */
		if (diffhours > 0) {
			drID.setText(diffhours + ctx.getString(R.string.short_hour));
			return drID;
		}
		int diffmins = eCalendar.get(Calendar.MINUTE)
				- sCalendar.get(Calendar.MINUTE);
		if (sm != null) {
			if (sm.getSyncIntervalMin() < 60) {
				if (diffmins >= 0 && diffmins < 5) {
					drID.setColor(Color.GREEN);
				}
			}
		}
		if (diffmins > 0) {
			drID.setText(diffmins + ctx.getString(R.string.short_min));
			return drID;
		}
		drID.setText(ctx.getString(R.string.ok));
		return drID;

	}

}
