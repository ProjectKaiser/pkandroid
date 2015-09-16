package com.projectkaiser.app_android.fragments;

import java.util.Calendar;
import java.util.Date;

import android.app.TimePickerDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.widget.TimePicker;

public class TimePickerFragment extends DialogFragment implements
		TimePickerDialog.OnTimeSetListener {

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		// Use the current time as the default values for the picker
		Calendar date = ((IChangeDateListener) getActivity()).getDueDate();
		int hour = 0;
		int minute = 0;
		Date dt  = new Date();
		if (date == null) {
			date = Calendar.getInstance();
			date.setTime(dt);
		} else if (date.get(Calendar.HOUR_OF_DAY)==0) {
			date.setTime(dt);
		}
		hour = date.get(Calendar.HOUR_OF_DAY);
		minute = date.get(Calendar.MINUTE);

		// Create a new instance of TimePickerDialog and return it
		return new TimePickerDialog(getActivity(), this, hour, minute,true);
	}

	public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
		try {
			((IChangeDateListener) getActivity()).onNewTimeSet(hourOfDay,
					minute, 0);
		} catch (ClassCastException e) {
			throw new ClassCastException(getActivity().toString()
					+ " must implement IChangeDateListener");
		}
	}
}