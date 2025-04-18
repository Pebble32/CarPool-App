package com.example.carpool.ui.fragments;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.app.ProgressDialog;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.carpool.R;
import com.example.carpool.data.api.RetrofitClient;
import com.example.carpool.data.api.RideOfferApi;
import com.example.carpool.data.models.EditRideOfferRequest;
import com.example.carpool.data.models.RideOfferResponse;
import com.example.carpool.ui.activities.MainActivity;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


/**
 * EditRideFragment is a Fragment that allows users to edit an existing ride offer.
 * It implements DatePickerFragment.DatePickerListener and TimePickerFragment.TimePickerListener
 * to handle date and time picking events.
 * 
 * This fragment provides UI elements for editing the start location, end location, departure date,
 * departure time, and available seats of a ride offer. It also includes buttons for updating the ride offer
 * and for editing the departure date and time.
 * 
 * Methods:
 * - newInstance(RideOfferResponse rideOffer): Creates a new instance of EditRideFragment with the provided ride offer details.
 * - onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState): Inflates the fragment's layout and initializes UI elements.
 * - populateFields(): Populates the UI fields with the ride offer details passed in the fragment's arguments.
 * - onClickEditDepartureDate(): Opens a DatePickerFragment to allow the user to select a new departure date.
 * - onDatePicked(int year, int month, int day): Handles the date picked event from the DatePickerFragment.
 * - onClickEditDepartureTime(): Opens a TimePickerFragment to allow the user to select a new departure time.
 * - onTimePicked(int hourOfDay, int minute): Handles the time picked event from the TimePickerFragment.
 * - onClickUpdate(): Validates the input fields and sends a request to update the ride offer.
 * - updateRideOffer(EditRideOfferRequest request): Sends a network request to update the ride offer and handles the response.
 */
public class EditRideFragment extends Fragment implements DatePickerFragment.DatePickerListener, TimePickerFragment.TimePickerListener{

    private EditText editStartLocation, editEndLocation, editDepartureDate, editDepartureTime, editAvailableSeats;
    private Button buttonUpdate, buttonEditDepartureDate, buttonEditDepartureTime;
    private RideOfferApi rideOfferApi;
    private RideOfferResponse currentRideOffer;

    private int year, month, day, hourOfDay, minute;

    public static EditRideFragment newInstance(RideOfferResponse rideOffer){
        EditRideFragment fragment = new EditRideFragment();
        Bundle args = new Bundle();
        args.putLong("ride_id", rideOffer.getId());
        args.putString("start_location", rideOffer.getStartLocation());
        args.putString("end_location", rideOffer.getEndLocation());
        args.putString("departure_time", rideOffer.getDepartureTime());
        args.putInt("available_seats", rideOffer.getAvailableSeats());
        args.putString("status", rideOffer.getRideStatus().toString());
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_edit_ride, container, false);

        // Initialize UI elements
        editStartLocation = view.findViewById(R.id.editStartLocation);
        editEndLocation = view.findViewById(R.id.editEndLocation);
        editDepartureDate = view.findViewById(R.id.editDepartureDate);
        editDepartureTime = view.findViewById(R.id.editDepartureTime);
        editAvailableSeats = view.findViewById(R.id.editAvailableSeats);
        buttonUpdate = view.findViewById(R.id.buttonUpdate);
        buttonEditDepartureDate = view.findViewById(R.id.buttonEditDepartureDate);
        buttonEditDepartureTime = view.findViewById(R.id.buttonEditDepartureTime);

        // Initialize API client
        rideOfferApi = RetrofitClient.getInstance().create(RideOfferApi.class);

        populateFields();

        // Set click listeners for buttons
        buttonUpdate.setOnClickListener(v -> onClickUpdate());
        buttonEditDepartureDate.setOnClickListener(v -> onClickEditDepartureDate());
        buttonEditDepartureTime.setOnClickListener(v -> onClickEditDepartureTime());

        return view;
    }

    @Override
    public void onResume(){
        super.onResume();
        if (getActivity() instanceof MainActivity){
            MainActivity mainActivity = (MainActivity) getActivity();
            mainActivity.showBottomNav(true);
            mainActivity.uncheckBottomNav();
        }
    }

    private void populateFields(){
        if (getArguments() != null){
            // Populate fields with existing ride offer details
            editStartLocation.setText(getArguments().getString("start_location"));
            editEndLocation.setText(getArguments().getString("end_location"));

            String departureTimeString = getArguments().getString("departure_time");

            try {
                // Parse and set departure date and time
                LocalDateTime dateTime = LocalDateTime.parse(departureTimeString, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"));
                editDepartureDate.setText(dateTime.toLocalDate().toString());
                editDepartureTime.setText(dateTime.toLocalTime().toString());

                year = dateTime.getYear();
                month = dateTime.getMonthValue() - 1;
                day = dateTime.getDayOfMonth();
                hourOfDay = dateTime.getHour();
                minute = dateTime.getMinute();
            } catch (Exception e){
                Toast.makeText(getContext(), "Error parsing date/time", Toast.LENGTH_SHORT).show();
            }

            editAvailableSeats.setText(String.valueOf(getArguments().getInt("available_seats")));
        }
    }

    private void onClickEditDepartureDate(){
        // Open date picker dialog with current date values
        DatePickerFragment datePickerFragment = new DatePickerFragment();
        Bundle args = new Bundle();
        args.putInt("year", year);
        args.putInt("month", month);
        args.putInt("day", day);
        datePickerFragment.setArguments(args);
        datePickerFragment.setDatePickerListener(this);
        datePickerFragment.show(getChildFragmentManager(), "datePicker");
    }

    @Override
    public void onDatePicked(int year, int month, int day) {
        // Update date fields with selected date
        this.year = year;
        this.month = month;
        this.day = day;

        LocalDate selectedDate = LocalDate.of(year, month + 1, day);
        editDepartureDate.setText(selectedDate.toString());
    }

    private void onClickEditDepartureTime(){
        // Open time picker dialog with current time values
        TimePickerFragment timePickerFragment = new TimePickerFragment();
        Bundle args = new Bundle();
        args.putInt("hour", hourOfDay);
        args.putInt("minute", minute);
        timePickerFragment.setArguments(args);
        timePickerFragment.setTimePickerListener(this);
        timePickerFragment.show(getChildFragmentManager(), "timePicker");
    }

    @Override
    public void onTimePicked(int hourOfDay, int minute) {
        // Update time fields with selected time
        this.hourOfDay = hourOfDay;
        this.minute = minute;

        LocalTime selectedTime = LocalTime.of(hourOfDay, minute);
        editDepartureTime.setText(selectedTime.toString());
    }

    private void onClickUpdate(){
        // Validate input fields
        String startLocation = editStartLocation.getText().toString().trim();
        String endLocation = editEndLocation.getText().toString().trim();
        String departureDate = editDepartureDate.getText().toString().trim();
        String departureTime = editDepartureTime.getText().toString().trim();
        String availableSeatsStr = editAvailableSeats.getText().toString().trim();

        if (TextUtils.isEmpty(startLocation) || TextUtils.isEmpty(endLocation) || TextUtils.isEmpty(departureDate) || TextUtils.isEmpty(departureTime) || TextUtils.isEmpty(availableSeatsStr)){
            Toast.makeText(getContext(), "All fields are required", Toast.LENGTH_SHORT).show();
            return;
        }

        int availableSeats;
        try {
            availableSeats = Integer.parseInt(availableSeatsStr);
            if (availableSeats < 1){
                Toast.makeText(getContext(), "Available seats must be at least 1", Toast.LENGTH_SHORT).show();
                return;
            }
        } catch(NumberFormatException e){
            Toast.makeText(getContext(), "Please enter a valid number of seats", Toast.LENGTH_SHORT).show();
            return;
        }

        String departureDateTime;

        try {
            // Parse and format departure date and time
            LocalDate date = LocalDate.parse(departureDate);
            LocalTime time = LocalTime.parse(departureTime);
            LocalDateTime dateTime = LocalDateTime.of(date, time);
            departureDateTime = dateTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        } catch(DateTimeException e){
            Toast.makeText(getContext(), "Invalid date/time format", Toast.LENGTH_SHORT).show();
            return;
        }

        Long rideId = getArguments().getLong("ride_id");
        String status = getArguments().getString("status", "AVAILABLE");

        // Create request object
        EditRideOfferRequest request = new EditRideOfferRequest(
            rideId, startLocation, endLocation, departureDateTime, availableSeats, status);
        
        updateRideOffer(request);
    }

    private void updateRideOffer(EditRideOfferRequest request){
        // Show progress dialog
        ProgressDialog progressDialog = new ProgressDialog(requireContext());
        progressDialog.setMessage("Updating ride offer...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        // Send network request to update ride offer
        rideOfferApi.updateRideOffer(request).enqueue(new Callback<RideOfferResponse>(){
            @Override
            public void onResponse(Call<RideOfferResponse> call, Response<RideOfferResponse> response){
                progressDialog.dismiss();
                if(response.isSuccessful() && response.body() != null){
                    Toast.makeText(getContext(), "Ride offer updated successfully", Toast.LENGTH_SHORT).show();
                    (Objects.requireNonNull((MainActivity) getActivity())).changeFragment(R.id.nav_my_rides);
                    /*getActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new RideOffersFragment())
                        .commit();*/
                } else {
                    Toast.makeText(getContext(), "Failed to update ride offer", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<RideOfferResponse> call, Throwable t){
                progressDialog.dismiss();
                Toast.makeText(getContext(), "Error :" + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
