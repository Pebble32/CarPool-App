package com.example.carpool.ui.fragments;

import android.app.AlertDialog;
import android.graphics.Color;
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
import com.example.carpool.data.api.RideOfferApi;
import com.example.carpool.data.api.RetrofitClient;
import com.example.carpool.data.models.RideOfferResponse;
import com.example.carpool.data.models.RideStatus;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.progressindicator.LinearProgressIndicator;

import java.io.Serializable;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RideOfferedDetailsFragment extends Fragment {
    private static final String TAG = "RideOfferedDetailsFrag";
    private static final String ARG_RIDE_OFFER = "ride_offer";

    private RideOfferResponse rideOffer;
    private TextView textViewStartLocation, textViewEndLocation, textViewDepartureTime,
            textViewRideStatus, textViewAvailableSeats, textViewCreatedBy;
    private Button buttonEdit, buttonDelete, buttonMarkFinished, buttonShowRoute;
    private LinearProgressIndicator progressIndicator;
    private RideOfferApi rideOfferApi;

    public static RideOfferedDetailsFragment newInstance(RideOfferResponse rideOffer) {
        RideOfferedDetailsFragment fragment = new RideOfferedDetailsFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_RIDE_OFFER, (Serializable) rideOffer);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate called");
        if (getArguments() != null) {
            rideOffer = (RideOfferResponse) getArguments().getSerializable(ARG_RIDE_OFFER);
            Log.d(TAG, "Loaded ride offer: " + (rideOffer != null ? rideOffer.getId() : "null"));
        }

        rideOfferApi = RetrofitClient.getInstance().create(RideOfferApi.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView called");
        View view = inflater.inflate(R.layout.fragment_ride_offered_details, container, false);

        MaterialToolbar toolbar = view.findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> requireActivity().getSupportFragmentManager().popBackStack());

        textViewStartLocation = view.findViewById(R.id.textViewStartLocation);
        textViewEndLocation = view.findViewById(R.id.textViewEndLocation);
        textViewDepartureTime = view.findViewById(R.id.textViewDepartureTime);
        textViewRideStatus = view.findViewById(R.id.textViewRideStatus);
        textViewAvailableSeats = view.findViewById(R.id.textViewAvailableSeats);
        textViewCreatedBy = view.findViewById(R.id.textViewCreatedBy);
        buttonEdit = view.findViewById(R.id.buttonEdit);
        buttonDelete = view.findViewById(R.id.buttonDelete);
        buttonMarkFinished = view.findViewById(R.id.buttonMarkFinished);
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
            textViewRideStatus.setText(rideOffer.getStatus());
            textViewAvailableSeats.setText(String.valueOf(rideOffer.getAvailableSeats()));
            textViewCreatedBy.setText(rideOffer.getCreatorEmail());

            int statusColor;
            switch (rideOffer.getStatus()) {
                case "AVAILABLE":
                    statusColor = Color.parseColor("#4CAF50"); // Green
                    break;
                case "UNAVAILABLE":
                    statusColor = Color.parseColor("#FF9800"); // Orange
                    break;
                case "FINISHED":
                    statusColor = Color.parseColor("#2196F3"); // Blue
                    break;
                case "CANCELLED":
                    statusColor = Color.parseColor("#F44336"); // Red
                    break;
                default:
                    statusColor = Color.parseColor("#9E9E9E"); // Gray
                    break;
            }
            textViewRideStatus.setTextColor(statusColor);

            boolean isActiveRide = "AVAILABLE".equals(rideOffer.getStatus()) || "UNAVAILABLE".equals(rideOffer.getStatus());
            buttonEdit.setVisibility(isActiveRide ? View.VISIBLE : View.GONE);
            buttonMarkFinished.setVisibility(isActiveRide ? View.VISIBLE : View.GONE);
        } else {
            Log.w(TAG, "rideOffer is null, cannot populate ride details");
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

        buttonEdit.setOnClickListener(v -> {
            if (rideOffer != null) {
                Fragment editFragment = EditRideFragment.newInstance(rideOffer);
                requireActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, editFragment)
                        .addToBackStack(null)
                        .commit();
            }
        });

        buttonDelete.setOnClickListener(v -> {
            if (rideOffer != null) {
                new AlertDialog.Builder(requireContext())
                        .setTitle("Delete Ride Offer")
                        .setMessage("Are you sure you want to delete this ride offer?")
                        .setPositiveButton("Yes", (dialog, which) -> deleteRideOffer())
                        .setNegativeButton("No", null)
                        .show();
            }
        });

        buttonMarkFinished.setOnClickListener(v -> {
            if (rideOffer != null) {
                new AlertDialog.Builder(requireContext())
                        .setTitle("Mark Ride as Finished")
                        .setMessage("Are you sure you want to mark this ride as finished? This action cannot be undone.")
                        .setPositiveButton("Yes", (dialog, which) -> markRideAsFinished())
                        .setNegativeButton("No", null)
                        .show();
            }
        });
    }

    private void deleteRideOffer() {
        if (rideOffer == null || !isAdded()) {
            return;
        }

        progressIndicator.setVisibility(View.VISIBLE);

        rideOfferApi.deleteRideOffer(rideOffer.getId()).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (!isAdded()) return;

                progressIndicator.setVisibility(View.GONE);

                if (response.isSuccessful()) {
                    Toast.makeText(requireContext(), "Ride offer deleted successfully", Toast.LENGTH_SHORT).show();

                    requireActivity().getSupportFragmentManager().popBackStack();
                } else {
                    Toast.makeText(requireContext(), "Failed to delete ride offer", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                if (!isAdded()) return;

                progressIndicator.setVisibility(View.GONE);
                Toast.makeText(requireContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void markRideAsFinished() {
        if (rideOffer == null || !isAdded()) {
            return;
        }

        progressIndicator.setVisibility(View.VISIBLE);

        rideOfferApi.markRideAsFinished(rideOffer.getId()).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (!isAdded()) return;

                progressIndicator.setVisibility(View.GONE);

                if (response.isSuccessful()) {
                    Toast.makeText(requireContext(), "Ride marked as finished successfully", Toast.LENGTH_SHORT).show();

                    // Update UI to reflect finished status
                    rideOffer.setRideStatus(RideStatus.FINISHED);
                    textViewRideStatus.setText("FINISHED");
                    textViewRideStatus.setTextColor(Color.parseColor("#2196F3"));

                    // Hide edit and mark finished buttons
                    buttonEdit.setVisibility(View.GONE);
                    buttonMarkFinished.setVisibility(View.GONE);
                } else {
                    Toast.makeText(requireContext(), "Failed to mark ride as finished", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                if (!isAdded()) return;

                progressIndicator.setVisibility(View.GONE);
                Toast.makeText(requireContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}