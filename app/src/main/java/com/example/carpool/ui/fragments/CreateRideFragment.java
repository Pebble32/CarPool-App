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
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.carpool.R;
import com.example.carpool.data.api.RetrofitClient;
import com.example.carpool.data.api.RideOfferApi;
import com.example.carpool.data.models.RideOfferRequest;
import com.example.carpool.ui.adapters.RideOffersAdapter;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CreateRideFragment extends Fragment {

    private EditText editStartLocation, editEndLocation, editDepartureTime, editAvailableSeats;

    private Button buttonCreate;

    private RideOfferApi rideOfferApi;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_create_ride, container, false);
        editStartLocation = view.findViewById(R.id.editStartLocation);
        editEndLocation = view.findViewById(R.id.editEndLocation);
        editDepartureTime = view.findViewById(R.id.editDepartureTime);
        editAvailableSeats = view.findViewById(R.id.editAvailableSeats);
        buttonCreate = view.findViewById(R.id.buttonCreate);
        rideOfferApi = RetrofitClient.getInstance().create(RideOfferApi.class);

        buttonCreate.setOnClickListener(v -> onClickCreate());

        return view;
    }

    private void onClickCreate() {
        String startLocation = editStartLocation.getText().toString();
        String endLocation = editEndLocation.getText().toString();
        String departureTimeString = editDepartureTime.getText().toString();
        String availableSeatsString = editAvailableSeats.getText().toString();

        if (TextUtils.isEmpty(startLocation) || TextUtils.isEmpty(endLocation) || TextUtils.isEmpty(departureTimeString)
                || TextUtils.isEmpty(availableSeatsString)) {
            Toast.makeText(getContext(), "Please fill in all required fields", Toast.LENGTH_SHORT).show();
            return;
        }

        LocalDateTime departureTime;
        try {
            departureTime = LocalDateTime.parse(departureTimeString);
        } catch (DateTimeParseException e) {
            Toast.makeText(getContext(), "Date and time invalid", Toast.LENGTH_SHORT).show();
            return;
        }

        int availableSeats;
        try {
            availableSeats = Integer.parseInt(availableSeatsString);
        } catch (DateTimeParseException e) {
            Toast.makeText(getContext(), "Date and time invalid", Toast.LENGTH_SHORT).show();
            return;
        }

        RideOfferRequest rideOfferRequest = new RideOfferRequest(
                startLocation,
                endLocation,
                departureTime,
                availableSeats
        );

        rideOfferApi.createRideOffer(rideOfferRequest).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(getContext(), "Ride created successfully!", Toast.LENGTH_SHORT).show();
                    // Navigate back to the LoginFragment by popping the back stack
                    if (getActivity() != null) {
                        getActivity().getSupportFragmentManager().popBackStack();
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
