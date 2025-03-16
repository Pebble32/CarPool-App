package com.example.carpool.ui.fragments;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.carpool.R;
import com.example.carpool.data.api.RideRequestApi;
import com.example.carpool.data.api.RetrofitClient;
import com.example.carpool.data.models.EditRideRequestRequest;
import com.example.carpool.data.models.RideOfferResponse;
import com.example.carpool.data.models.RideRequestResponse;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.progressindicator.LinearProgressIndicator;

import java.io.Serializable;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RideDetailsFragment extends Fragment {

    private static final String TAG = "RideDetailsFragment";
    private static final String ARG_RIDE_REQUEST = "ride_request";
    private static final String ARG_RIDE_OFFER = "ride_offer";

    private RideRequestResponse rideRequest;
    private RideOfferResponse rideOffer;
    private TextView textViewStartLocation, textViewEndLocation, textViewDepartureTime,
            textViewDriver, textViewRideStatus, textViewRequestStatus;
    private Button buttonCancel, buttonContactDriver, buttonShowRoute;
    private LinearProgressIndicator progressIndicator;
    private RideRequestApi rideRequestApi;

    // Static factory method (unchanged)
    public static RideDetailsFragment newInstance(RideRequestResponse rideRequest, RideOfferResponse rideOffer) {
        RideDetailsFragment fragment = new RideDetailsFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_RIDE_REQUEST, (Serializable) rideRequest);
        args.putSerializable(ARG_RIDE_OFFER, (Serializable) rideOffer);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            rideRequest = (RideRequestResponse) getArguments().getSerializable(ARG_RIDE_REQUEST);
            rideOffer = (RideOfferResponse) getArguments().getSerializable(ARG_RIDE_OFFER);
        }

        rideRequestApi = RetrofitClient.getInstance().create(RideRequestApi.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_ride_details, container, false);

        MaterialToolbar toolbar = view.findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> requireActivity().getSupportFragmentManager().popBackStack());

        textViewStartLocation = view.findViewById(R.id.textViewStartLocation);
        textViewEndLocation = view.findViewById(R.id.textViewEndLocation);
        textViewDepartureTime = view.findViewById(R.id.textViewDepartureTime);
        textViewDriver = view.findViewById(R.id.textViewDriver);
        textViewRideStatus = view.findViewById(R.id.textViewRideStatus);
        textViewRequestStatus = view.findViewById(R.id.textViewRequestStatus);
        buttonCancel = view.findViewById(R.id.buttonCancel);
        buttonContactDriver = view.findViewById(R.id.buttonContactDriver);
        buttonShowRoute = view.findViewById(R.id.buttonShowRoute);
        progressIndicator = view.findViewById(R.id.progressIndicator);

        // Add debug logging
        Log.d(TAG, "onCreateView: buttonShowRoute is " + (buttonShowRoute != null ? "found" : "NULL"));

        populateRideDetails();
        setupButtons();

        return view;
    }

    private void populateRideDetails() {
        if (rideOffer != null) {
            textViewStartLocation.setText(rideOffer.getStartLocation());
            textViewEndLocation.setText(rideOffer.getEndLocation());
            textViewDepartureTime.setText(rideOffer.getDepartureTime());
            textViewDriver.setText(rideOffer.getCreatorEmail());
            textViewRideStatus.setText(rideOffer.getStatus());
        }

        if (rideRequest != null) {
            String requestStatus = rideRequest.getRequestStatus();
            textViewRequestStatus.setText(requestStatus);

            int statusColor;
            switch (requestStatus) {
                case "ACCEPTED":
                    statusColor = Color.parseColor("#4CAF50"); // Green
                    break;
                case "REJECTED":
                    statusColor = Color.parseColor("#F44336"); // Red
                    break;
                case "CANCELED":
                    statusColor = Color.parseColor("#9E9E9E"); // Gray
                    break;
                case "PENDING":
                default:
                    statusColor = Color.parseColor("#FF9800"); // Orange
                    break;
            }
            textViewRequestStatus.setTextColor(statusColor);

            if ("PENDING".equals(requestStatus) || "ACCEPTED".equals(requestStatus)) {
                buttonCancel.setVisibility(View.VISIBLE);
            } else {
                buttonCancel.setVisibility(View.GONE);
            }
        }
    }

    private void setupButtons() {
        // Debug logging for button setup
        Log.d(TAG, "setupButtons: Setting up buttons");
        Log.d(TAG, "setupButtons: buttonShowRoute is " +
                (buttonShowRoute != null ? "found" : "NULL") +
                ", visibility is " +
                (buttonShowRoute != null ?
                        (buttonShowRoute.getVisibility() == View.VISIBLE ? "VISIBLE" :
                                buttonShowRoute.getVisibility() == View.GONE ? "GONE" : "INVISIBLE") :
                        "unknown"));
        Log.d(TAG, "setupButtons: rideOffer is " + (rideOffer != null ? "not null" : "NULL"));

        buttonCancel.setOnClickListener(v -> {
            new AlertDialog.Builder(requireContext())
                    .setTitle("Cancel Request")
                    .setMessage("Are you sure you want to cancel this ride request?")
                    .setPositiveButton("Yes", (dialog, which) -> cancelRideRequest())
                    .setNegativeButton("No", null)
                    .show();
        });

        buttonContactDriver.setOnClickListener(v -> {
            if (rideOffer != null && rideOffer.getCreatorEmail() != null) {
                Intent intent = new Intent(Intent.ACTION_SENDTO);
                intent.setData(Uri.parse("mailto:" + rideOffer.getCreatorEmail()));
                intent.putExtra(Intent.EXTRA_SUBJECT, "About Ride from " + rideOffer.getStartLocation() + " to " + rideOffer.getEndLocation());

                try {
                    startActivity(Intent.createChooser(intent, "Send Email"));
                } catch (Exception e) {
                    Toast.makeText(requireContext(), "No email app found", Toast.LENGTH_SHORT).show();
                }
            }
        });

        buttonShowRoute.setOnClickListener(v -> {
            Log.d(TAG, "Show Route button clicked");
            if (rideOffer != null) {
                Log.d(TAG, "Navigating to MapViewFragment with start=" + rideOffer.getStartLocation() + ", end=" + rideOffer.getEndLocation());

                // Navigate to the MapViewFragment
                MapViewFragment mapFragment = MapViewFragment.newInstance(
                        rideOffer.getStartLocation(),
                        rideOffer.getEndLocation()
                );

                requireActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, mapFragment)
                        .addToBackStack(null)
                        .commit();
            } else {
                Log.e(TAG, "Cannot show route: rideOffer is null");
                Toast.makeText(requireContext(), "Ride details not available", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void cancelRideRequest() {
        // Existing code (unchanged)
        if (rideRequest == null || !isAdded()) {
            return;
        }

        progressIndicator.setVisibility(View.VISIBLE);

        EditRideRequestRequest editRequest = new EditRideRequestRequest(rideRequest.getId(), "CANCELED");

        rideRequestApi.editRideRequestStatus(editRequest).enqueue(new Callback<RideRequestResponse>() {
            @Override
            public void onResponse(Call<RideRequestResponse> call, Response<RideRequestResponse> response) {
                if (!isAdded()) return;

                progressIndicator.setVisibility(View.GONE);

                if (response.isSuccessful()) {
                    Toast.makeText(requireContext(), "Ride request canceled successfully", Toast.LENGTH_SHORT).show();
                    textViewRequestStatus.setText("CANCELED");
                    textViewRequestStatus.setTextColor(Color.parseColor("#9E9E9E"));
                    buttonCancel.setVisibility(View.GONE);

                    buttonCancel.postDelayed(() -> {
                        if (isAdded()) {
                            requireActivity().getSupportFragmentManager().popBackStack();
                        }
                    }, 1500);
                } else {
                    Toast.makeText(requireContext(), "Failed to cancel request", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<RideRequestResponse> call, Throwable t) {
                if (!isAdded()) return;

                progressIndicator.setVisibility(View.GONE);
                Toast.makeText(requireContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}