package com.example.carpool.ui.fragments;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.carpool.R;
import com.example.carpool.data.api.RideOfferApi;
import com.example.carpool.data.api.RideRequestApi;
import com.example.carpool.data.api.RetrofitClient;
import com.example.carpool.data.models.EditRideRequestRequest;
import com.example.carpool.data.models.RideOfferResponse;
import com.example.carpool.data.models.RideRequestResponse;
import com.example.carpool.ui.adapters.MyRidesJoinedAdapter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Fragment for displaying rides the user has requested to join.
 */
public class MyRidesJoinedFragment extends Fragment implements MyRidesJoinedAdapter.OnRideRequestActionListener {

    private static final String TAG = "MyRidesJoinedFragment";

    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private TextView emptyView;
    private MyRidesJoinedAdapter adapter;
    private RideRequestApi rideRequestApi;
    private RideOfferApi rideOfferApi;
    private String currentUserEmail;
    private List<RideRequestResponse> rideRequests = new ArrayList<>();
    private Map<Long, RideOfferResponse> rideOffersMap = new HashMap<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_my_rides_joined, container, false);

        Log.d(TAG, "onCreateView: Initializing views");
        recyclerView = view.findViewById(R.id.recyclerViewMyJoined);
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);
        emptyView = view.findViewById(R.id.textViewEmpty);

        // Verify the views were found
        if (recyclerView == null) {
            Log.e(TAG, "onCreateView: recyclerView is null! Check the ID in the layout");
            Toast.makeText(getContext(), "Error: Could not find recycler view", Toast.LENGTH_SHORT).show();
            return view;
        }

        if (swipeRefreshLayout == null) {
            Log.e(TAG, "onCreateView: swipeRefreshLayout is null! Check the ID in the layout");
        }

        if (emptyView == null) {
            Log.e(TAG, "onCreateView: emptyView is null! Check the ID in the layout");
        }

        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
        currentUserEmail = sharedPreferences.getString("email", "");
        Log.d(TAG, "onCreateView: Current user email: " + currentUserEmail);

        // Set up RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new MyRidesJoinedAdapter(rideRequests, rideOffersMap, this);
        recyclerView.setAdapter(adapter);

        // Initialize API clients
        rideRequestApi = RetrofitClient.getInstance().create(RideRequestApi.class);
        rideOfferApi = RetrofitClient.getInstance().create(RideOfferApi.class);

        if (swipeRefreshLayout != null) {
            swipeRefreshLayout.setOnRefreshListener(this::loadRideRequests);
        }

        // Load data
        loadRideRequests();

        return view;
    }

    private void loadRideRequests() {
        Log.d(TAG, "loadRideRequests: Loading ride requests");
        if (swipeRefreshLayout != null) {
            swipeRefreshLayout.setRefreshing(true);
        }

        // Use the correct method from the interface
        Call<List<RideRequestResponse>> call = rideRequestApi.getUserRideRequests();
        Log.d(TAG, "loadRideRequests: Making API call to: " + call.request().url());

        call.enqueue(new Callback<List<RideRequestResponse>>() {
            @Override
            public void onResponse(Call<List<RideRequestResponse>> call, Response<List<RideRequestResponse>> response) {
                if (swipeRefreshLayout != null) {
                    swipeRefreshLayout.setRefreshing(false);
                }

                if (response.isSuccessful()) {
                    List<RideRequestResponse> responseData = response.body();
                    if (responseData != null) {
                        Log.d(TAG, "onResponse: Received " + responseData.size() + " ride requests");

                        // Clear existing data
                        rideRequests.clear();

                        if (!responseData.isEmpty()) {
                            // Log the first request to verify data structure
                            RideRequestResponse sampleRequest = responseData.get(0);
                            Log.d(TAG, "Sample request - ID: " + sampleRequest.getId()
                                    + ", Status: " + sampleRequest.getRequestStatus()
                                    + ", RideOfferId: " + sampleRequest.getRideOfferId());

                            // Sort by request date (newest first)
                            Collections.sort(responseData, (r1, r2) -> {
                                if (r1.getRequestDate() == null) return 1;
                                if (r2.getRequestDate() == null) return -1;
                                return r2.getRequestDate().compareTo(r1.getRequestDate());
                            });

                            // Add sorted data to our list
                            rideRequests.addAll(responseData);
                            Log.d(TAG, "Added " + rideRequests.size() + " requests to the adapter");

                            // Update UI
                            adapter.notifyDataSetChanged();
                            showEmptyView(false);

                            // Fetch ride offer details to enhance display
                            fetchRideOfferDetails();
                        } else {
                            Log.d(TAG, "onResponse: No ride requests found");
                            showEmptyView(true);
                        }
                    } else {
                        Log.e(TAG, "onResponse: Response body is null");
                        showEmptyView(true);
                    }
                } else {
                    Log.e(TAG, "onResponse: Request failed with code " + response.code());
                    try {
                        Log.e(TAG, "onResponse: Error body: " + response.errorBody().string());
                    } catch (Exception e) {
                        Log.e(TAG, "onResponse: Could not read error body", e);
                    }
                    Toast.makeText(getContext(), "Failed to load ride requests: " + response.code(), Toast.LENGTH_SHORT).show();
                    showEmptyView(true);
                }
            }

            @Override
            public void onFailure(Call<List<RideRequestResponse>> call, Throwable t) {
                Log.e(TAG, "onFailure: Request failed", t);
                if (swipeRefreshLayout != null) {
                    swipeRefreshLayout.setRefreshing(false);
                }
                Toast.makeText(getContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                showEmptyView(true);
            }
        });
    }

    private void showEmptyView(boolean show) {
        if (recyclerView == null || emptyView == null) return;

        Log.d(TAG, "showEmptyView: " + show);
        if (show) {
            emptyView.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            emptyView.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
    }

    private void fetchRideOfferDetails() {
        Log.d(TAG, "fetchRideOfferDetails: Fetching details for " + rideRequests.size() + " ride offers");

        if (rideRequests.isEmpty()) {
            return;
        }

        rideOffersMap.clear();
        final int[] requestsProcessed = {0};
        final int totalRequests = rideRequests.size();

        for (RideRequestResponse request : rideRequests) {
            Long rideOfferId = request.getRideOfferId();
            if (rideOfferId == null) {
                Log.e(TAG, "fetchRideOfferDetails: Ride offer ID is null for request ID: " + request.getId());
                requestsProcessed[0]++;
                continue;
            }

            Log.d(TAG, "fetchRideOfferDetails: Fetching details for ride offer ID: " + rideOfferId);
            Call<RideOfferResponse> call = rideOfferApi.getRideOfferDetails(rideOfferId);
            call.enqueue(new Callback<RideOfferResponse>() {
                @Override
                public void onResponse(Call<RideOfferResponse> call, Response<RideOfferResponse> response) {
                    requestsProcessed[0]++;

                    if (response.isSuccessful() && response.body() != null) {
                        Log.d(TAG, "onResponse: Got details for ride offer ID: " + rideOfferId);
                        rideOffersMap.put(rideOfferId, response.body());
                    } else {
                        Log.e(TAG, "onResponse: Failed to get details for ride offer ID: " + rideOfferId);
                    }

                    if (requestsProcessed[0] >= totalRequests) {
                        Log.d(TAG, "onResponse: All ride offer details fetched. Updating adapter.");
                        adapter.notifyDataSetChanged();
                    }
                }

                @Override
                public void onFailure(Call<RideOfferResponse> call, Throwable t) {
                    Log.e(TAG, "onFailure: Failed to get details for ride offer ID: " + rideOfferId, t);
                    requestsProcessed[0]++;

                    if (requestsProcessed[0] >= totalRequests) {
                        Log.d(TAG, "onFailure: All ride offer requests completed (some failed). Updating adapter.");
                        adapter.notifyDataSetChanged();
                    }
                }
            });
        }
    }

    @Override
    public void onCancelRequestClick(RideRequestResponse rideRequest) {
        Log.d(TAG, "onCancelRequestClick: Canceling request ID: " + rideRequest.getId());

        new AlertDialog.Builder(getContext())
                .setTitle("Cancel Request")
                .setMessage("Are you sure you want to cancel this ride request?")
                .setPositiveButton("Yes", (dialog, which) -> cancelRideRequest(rideRequest))
                .setNegativeButton("No", null)
                .show();
    }

    private void cancelRideRequest(RideRequestResponse rideRequest) {
        Log.d(TAG, "cancelRideRequest: Sending cancel request for ID: " + rideRequest.getId());

        ProgressDialog progressDialog = new ProgressDialog(requireContext());
        progressDialog.setMessage("Canceling ride request...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        // Create a request to change the ride request status to CANCELED
        EditRideRequestRequest editRequest = new EditRideRequestRequest(rideRequest.getId(), "CANCELED");

        // Make the API call to cancel the request
        rideRequestApi.editRideRequestStatus(editRequest).enqueue(new Callback<RideRequestResponse>() {
            @Override
            public void onResponse(Call<RideRequestResponse> call, Response<RideRequestResponse> response) {
                progressDialog.dismiss();
                if (response.isSuccessful()) {
                    Log.d(TAG, "onResponse: Successfully canceled ride request");
                    Toast.makeText(getContext(), "Ride request canceled successfully", Toast.LENGTH_SHORT).show();
                    loadRideRequests();
                } else {
                    Log.e(TAG, "onResponse: Failed to cancel ride request: " + response.code());
                    Toast.makeText(getContext(), "Failed to cancel ride request: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<RideRequestResponse> call, Throwable t) {
                Log.e(TAG, "onFailure: Cancel request failed", t);
                progressDialog.dismiss();
                Toast.makeText(getContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}