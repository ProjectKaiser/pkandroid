package com.projectkaiser.app_android.fragments;

import java.util.Calendar;
public interface IChangeDateListener {
	
	void onNewDateSet(int year, int month, int day);
	void onNewTimeSet(int hour, int minute, int second);
	Calendar getDueDate();

}
