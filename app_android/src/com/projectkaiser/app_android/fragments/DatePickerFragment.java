package com.projectkaiser.app_android.fragments;

import java.util.Calendar;

import java.util.Date;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.widget.DatePicker;

public class DatePickerFragment extends DialogFragment implements
		DatePickerDialog.OnDateSetListener {

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		Calendar date;
		date = ((IChangeDateListener) getActivity()).getDueDate();
		int year = 0;
		int month = 0;
		int day = 0;
		if (date == null) {
			Date dt = new Date();
			date = Calendar.getInstance();
			date.setTime(dt);
		}
		year = date.get(Calendar.YEAR);
		month = date.get(Calendar.MONTH);
		day = date.get(Calendar.DAY_OF_MONTH);

		return new DatePickerDialog(getActivity(), this, year, month, day);
	}

	public void onDateSet(DatePicker view, int year, int month, int day) {
		try {
			((IChangeDateListener) getActivity())
					.onNewDateSet(year, month, day);
		} catch (ClassCastException e) {
			throw new ClassCastException(getActivity().toString()
					+ " must implement IChangeDateListener");
		}
	}

}
