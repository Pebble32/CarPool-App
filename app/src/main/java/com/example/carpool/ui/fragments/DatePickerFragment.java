package com.example.carpool.ui.fragments;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.icu.util.Calendar;
import android.os.Bundle;
import android.widget.DatePicker;

import androidx.fragment.app.DialogFragment;

/**
 * A fragment that displays a date picker dialog.
 * This fragment implements the {@link DatePickerDialog.OnDateSetListener} interface
 * to handle the date selection event.
 *
 */
public class DatePickerFragment extends DialogFragment
        implements DatePickerDialog.OnDateSetListener {

    public interface DatePickerListener {
        void onDatePicked(int year, int month, int day);
    }

    private DatePickerListener mListener;

    public void setDatePickerListener(DatePickerListener listener) {
        mListener = listener;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the current date as the default values for the picker
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        // Create a new instance of DatePickerDialog and return it
        return new DatePickerDialog(requireContext(), this, year, month, day);
    }

    public void onDateSet(DatePicker view, int year, int month, int day) {
        // DatePicker returns months as zero based, and thus month + 1.
        mListener.onDatePicked(year, month + 1, day);
    }
}