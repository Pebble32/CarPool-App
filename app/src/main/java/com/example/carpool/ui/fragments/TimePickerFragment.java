package com.example.carpool.ui.fragments;

import android.app.Dialog;
import android.app.TimePickerDialog;
import android.icu.util.Calendar;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.widget.TimePicker;

import androidx.fragment.app.DialogFragment;

/**
 * TimePickerFragment is a DialogFragment that displays a time picker dialog.
 * It allows the user to select a time (hour and minute) and notifies a listener
 * when the time is set.
 *
 * Implements TimePickerDialog.OnTimeSetListener to handle the time set event.
 */
public class TimePickerFragment extends DialogFragment
        implements TimePickerDialog.OnTimeSetListener {

    public interface TimePickerListener {
        void onTimePicked(int hourOfDay, int minute);
    }

    private TimePickerFragment.TimePickerListener mListener;

    public void setTimePickerListener(TimePickerFragment.TimePickerListener listener) {
        mListener = listener;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the current time as the default values for the picker
        final Calendar c = Calendar.getInstance();
        int hour = c.get(Calendar.HOUR_OF_DAY);
        int minute = c.get(Calendar.MINUTE);

        // Create a new instance of TimePickerDialog and return it
        return new TimePickerDialog(getActivity(), this, hour, minute,
                DateFormat.is24HourFormat(getActivity()));
    }

    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        // Notify the listener with the selected time
        mListener.onTimePicked(hourOfDay, minute);
    }
}
