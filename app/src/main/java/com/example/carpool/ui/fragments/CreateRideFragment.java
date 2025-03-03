package com.example.carpool.ui.fragments;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.carpool.R;
import com.example.carpool.data.api.RetrofitClient;
import com.example.carpool.data.api.RideOfferApi;
import com.example.carpool.data.models.RideOfferRequest;
import com.example.carpool.ui.activities.MainActivity;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Locale;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * CreateRideFragment is a Fragment that allows users to create a new ride offer.
 * It implements DatePickerFragment.DatePickerListener and TimePickerFragment.TimePickerListener
 * to handle date and time picking for the ride departure.
 * 
 * Fields:
 * - editStartLocation: EditText for entering the start location of the ride.
 * - editEndLocation: EditText for entering the end location of the ride.
 * - editDepartureDate: EditText for displaying the selected departure date.
 * - editDepartureTime: EditText for displaying the selected departure time.
 * - editAvailableSeats: EditText for entering the number of available seats.
 * - buttonCreate: Button to create the ride offer.
 * - buttonEditDepartureDate: Button to open the date picker dialog.
 * - buttonEditDepartureTime: Button to open the time picker dialog.
 * - rideOfferApi: API interface for creating ride offers.
 * - year, month, day, hourOfDay, minute: Variables to store the selected date and time.
 * 
 * Methods:
 * - onCreateView: Initializes the view and sets up click listeners for buttons.
 * - onClickEditDepartureDate: Opens the date picker dialog.
 * - onDatePicked: Callback method for when a date is picked from the date picker.
 * - onClickEditDepartureTime: Opens the time picker dialog.
 * - onTimePicked: Callback method for when a time is picked from the time picker.
 * - onClickCreate: Validates input fields and sends a request to create a new ride offer.
 */
public class CreateRideFragment extends Fragment implements DatePickerFragment.DatePickerListener, TimePickerFragment.TimePickerListener {

    private EditText editStartLocation, editEndLocation, editDepartureDate, editDepartureTime, editAvailableSeats;

    private Button buttonCreate, buttonEditDepartureDate, buttonEditDepartureTime;

    private RideOfferApi rideOfferApi;

    private int year, month, day, hourOfDay, minute;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_create_ride, container, false);
        // Initialize UI components
        editStartLocation = view.findViewById(R.id.editStartLocation);
        editEndLocation = view.findViewById(R.id.editEndLocation);
        editDepartureDate = view.findViewById(R.id.editDepartureDate);
        editDepartureTime = view.findViewById(R.id.editDepartureTime);
        editAvailableSeats = view.findViewById(R.id.editAvailableSeats);
        buttonCreate = view.findViewById(R.id.buttonCreate);
        buttonEditDepartureDate = view.findViewById(R.id.buttonEditDepartureDate);
        buttonEditDepartureTime = view.findViewById(R.id.buttonEditDepartureTime);
        rideOfferApi = RetrofitClient.getInstance().create(RideOfferApi.class);

        // Set click listeners for buttons
        buttonCreate.setOnClickListener(v -> onClickCreate());
        buttonEditDepartureDate.setOnClickListener(v -> onClickEditDepartureDate());
        buttonEditDepartureTime.setOnClickListener(v -> onClickEditDepartureTime());

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        ((MainActivity) requireActivity()).showBottomNav(true);
    }

    private void onClickEditDepartureDate() {
        // Open the date picker dialog
        DatePickerFragment datePickerFragment = new DatePickerFragment();
        datePickerFragment.setDatePickerListener(this);
        datePickerFragment.show(getChildFragmentManager(), "datePicker");
    }

    @Override
    public void onDatePicked(int year, int month, int day) {
        this.year = year;
        this.month = month;
        this.day = day;
        LocalDate date = LocalDate.of(year, month, day);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM d, yyyy", Locale.US);
        editDepartureDate.setText(date.format(formatter));
    }

    private void onClickEditDepartureTime() {
        // Open the time picker dialog
        TimePickerFragment timePickerFragment = new TimePickerFragment();
        timePickerFragment.setTimePickerListener(this);
        timePickerFragment.show(getChildFragmentManager(), "timePicker");
    }

    @Override
    public void onTimePicked(int hourOfDay, int minute) {
        this.hourOfDay = hourOfDay;
        this.minute = minute;
        LocalTime time = LocalTime.of(hourOfDay, minute);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm", Locale.US);
        editDepartureTime.setText(time.format(formatter));
    }

    private void onClickCreate() {
        // Retrieve input values from UI components
        String startLocation = editStartLocation.getText().toString();
        String endLocation = editEndLocation.getText().toString();
        String availableSeatsString = editAvailableSeats.getText().toString();
        LocalDateTime departureTime;

        // Validate and parse the selected date and time
        try {
            departureTime = LocalDateTime.of(year, month, day, hourOfDay, minute);
        } catch (DateTimeException e) {
            Toast.makeText(getContext(), "Please fill in a valid date and time.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Check if any required fields are empty
        if (TextUtils.isEmpty(startLocation) || TextUtils.isEmpty(endLocation)
                || TextUtils.isEmpty(availableSeatsString)) {
            Toast.makeText(getContext(), "Please fill in all required fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // Parse the number of available seats
        int availableSeats;
        try {
            availableSeats = Integer.parseInt(availableSeatsString);
        } catch (DateTimeParseException e) {
            Toast.makeText(getContext(), "Date and time invalid", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create a new RideOfferRequest object
        RideOfferRequest rideOfferRequest = new RideOfferRequest(
                startLocation,
                endLocation,
                departureTime,
                availableSeats
        );

        // Send the request to create a new ride offer
        rideOfferApi.createRideOffer(rideOfferRequest).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(getContext(), "Ride created successfully!", Toast.LENGTH_SHORT).show();
                    // Navigate back to the RideOffersFragment by replacing the current fragment
                    if (getActivity() != null) {
                        ((MainActivity)getActivity()).changeFragment(R.id.nav_my_rides);
                    }
                } else {
                    Toast.makeText(getContext(), "Ride creation failed: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Toast.makeText(getContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });


    }
}
