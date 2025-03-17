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

    public static RideDetailsFragment newInstance(RideRequestResponse rideRequest, RideOfferResponse rideOffer) {
        RideDetailsFragment fragment = new RideDetailsFragment();
        Bundle args = new Bundle();
        if (rideRequest != null) {
            args.putSerializable(ARG_RIDE_REQUEST, (Serializable) rideRequest);
        }
        if (rideOffer != null) {
            args.putSerializable(ARG_RIDE_OFFER, (Serializable) rideOffer);
        }
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate called");
        if (getArguments() != null) {
            if (getArguments().containsKey(ARG_RIDE_REQUEST)) {
                rideRequest = (RideRequestResponse) getArguments().getSerializable(ARG_RIDE_REQUEST);
                Log.d(TAG, "Loaded ride request: " + (rideRequest != null ? rideRequest.getId() : "null"));
            }
            if (getArguments().containsKey(ARG_RIDE_OFFER)) {
                rideOffer = (RideOfferResponse) getArguments().getSerializable(ARG_RIDE_OFFER);
                Log.d(TAG, "Loaded ride offer: " + (rideOffer != null ? rideOffer.getId() : "null"));
            }
        }

        rideRequestApi = RetrofitClient.getInstance().create(RideRequestApi.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView called");
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

        Log.d(TAG, "buttonShowRoute found: " + (buttonShowRoute != null));

        if (buttonShowRoute != null) {
            buttonShowRoute.setVisibility(View.VISIBLE);
        } else {
            Log.e(TAG, "buttonShowRoute is NULL! Check your layout XML ID");
        }

        populateRideDetails();
        setupButtons();

        return view;
    }

    private void populateRideDetails() {
        Log.d(TAG, "populateRideDetails called");

        if (rideOffer != null) {
            Log.d(TAG, "Populating ride offer details: " + rideOffer.getId());
            textViewStartLocation.setText(rideOffer.getStartLocation());
            textViewEndLocation.setText(rideOffer.getEndLocation());
            textViewDepartureTime.setText(rideOffer.getDepartureTime());
            textViewDriver.setText(rideOffer.getCreatorEmail());
            textViewRideStatus.setText(rideOffer.getStatus());
        } else {
            Log.w(TAG, "rideOffer is null, cannot populate ride details");
        }

        if (rideRequest != null) {
            Log.d(TAG, "Populating ride request details: " + rideRequest.getId());
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
        } else {
            Log.d(TAG, "rideRequest is null, hiding request-specific UI elements");
            if (textViewRequestStatus != null) {
                textViewRequestStatus.setVisibility(View.GONE);
            }
        }
    }

    private void setupButtons() {
        Log.d(TAG, "setupButtons called");

        // Show Route button - ALWAYS visible
        if (buttonShowRoute != null) {
            Log.d(TAG, "Setting up Show Route button");
            buttonShowRoute.setVisibility(View.VISIBLE);
            buttonShowRoute.setOnClickListener(v -> {
                Log.d(TAG, "Show Route button clicked");

                if (rideOffer != null &&
                        rideOffer.getStartLocation() != null &&
                        rideOffer.getEndLocation() != null) {

                    Log.d(TAG, "Navigating to map view with route from " +
                            rideOffer.getStartLocation() + " to " + rideOffer.getEndLocation());

                    MapViewFragment mapFragment = MapViewFragment.newInstance(
                            rideOffer.getStartLocation(),
                            rideOffer.getEndLocation()
                    );

                    requireActivity().getSupportFragmentManager().beginTransaction()
                            .replace(R.id.fragment_container, mapFragment)
                            .addToBackStack(null)
                            .commit();
                } else {
                    Log.e(TAG, "Cannot show route - rideOffer is null or missing locations");
                    Toast.makeText(requireContext(),
                            "Cannot show route - location information is missing",
                            Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Log.e(TAG, "buttonShowRoute is null in setupButtons()");
        }

        // Cancel button
        if (buttonCancel != null) {
            buttonCancel.setOnClickListener(v -> {
                new AlertDialog.Builder(requireContext())
                        .setTitle("Cancel Request")
                        .setMessage("Are you sure you want to cancel this ride request?")
                        .setPositiveButton("Yes", (dialog, which) -> cancelRideRequest())
                        .setNegativeButton("No", null)
                        .show();
            });
        }

        // Contact Driver button
        if (buttonContactDriver != null) {
            buttonContactDriver.setOnClickListener(v -> {
                if (rideOffer != null && rideOffer.getCreatorEmail() != null) {
                    Intent intent = new Intent(Intent.ACTION_SENDTO);
                    intent.setData(Uri.parse("mailto:" + rideOffer.getCreatorEmail()));
                    intent.putExtra(Intent.EXTRA_SUBJECT, "About Ride from " +
                            rideOffer.getStartLocation() + " to " + rideOffer.getEndLocation());

                    try {
                        startActivity(Intent.createChooser(intent, "Send Email"));
                    } catch (Exception e) {
                        Toast.makeText(requireContext(), "No email app found", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    private void cancelRideRequest() {
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