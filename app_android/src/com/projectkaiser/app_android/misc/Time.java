package com.projectkaiser.app_android.misc;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import com.projectkaiser.app_android.R;

import android.content.Context;

public class Time {

	public static int getMinutes(String txtHours, String txtMinutes) {
		
		int minutes = 0;
				
		try {
			minutes = Integer.parseInt(txtMinutes.trim());
		} catch (NumberFormatException e) {
			minutes = 0;
		}

		try {
			minutes += Integer.parseInt(txtHours.trim())*60;
		} catch (NumberFormatException e) {
			// couldn't parse hours, do nothing
		}
		
		return minutes;
	}

	public static String formatMinutes(Context ctx, int minutes) {
		int hours = minutes / 60;
		int mins = minutes % 60;
		if (minutes == 0)
			return ctx.getString(R.string.hrs_pattern, 0);
		
		if (hours == 0)
			return ctx.getString(R.string.mins_pattern, mins);			
		else if (mins == 0)
			return ctx.getString(R.string.hrs_pattern, hours);
		else
			return ctx.getString(R.string.hrs_pattern, hours) + " " + ctx.getString(R.string.mins_pattern, mins);			
	}
	
	public static String formatDate(Context ctx, long date) {
		return formatDate(ctx, new Date(date));
	}

	public static String formatDate(Context ctx, Date date) {
		SimpleDateFormat df = new SimpleDateFormat(ctx.getString(R.string.short_date), Locale.getDefault());
		return df.format(date);
	}
}
