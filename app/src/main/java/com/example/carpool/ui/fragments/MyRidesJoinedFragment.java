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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

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
    private TextView titleView;
    private MyRidesJoinedAdapter adapter;
    private RideRequestApi rideRequestApi;
    private RideOfferApi rideOfferApi;
    private String currentUserEmail;
    private List<RideRequestResponse> rideRequests = new ArrayList<>();
    private Map<Long, RideOfferResponse> rideOffersMap = new HashMap<>();
    private boolean isLoading = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView: Starting to create view");
        View view = inflater.inflate(R.layout.fragment_my_rides_joined, container, false);

        try {
            // Find views by ID
            recyclerView = view.findViewById(R.id.recyclerViewMyJoined);
            swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);
            emptyView = view.findViewById(R.id.textViewEmpty);
            titleView = view.findViewById(R.id.textViewPageTitle);

            if (titleView != null) {
                titleView.setText("Rides I've Joined");
            }

            // Make sure recyclerView exists
            if (recyclerView == null) {
                Log.e(TAG, "onCreateView: recyclerView is null! ID not found");
                Toast.makeText(getContext(), "Layout error: Could not find recycler view", Toast.LENGTH_SHORT).show();
                return view;
            }

            // Get the current user email
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

            // Set up swipe to refresh
            if (swipeRefreshLayout != null) {
                swipeRefreshLayout.setOnRefreshListener(() -> {
                    if (!isLoading) {
                        loadRideRequests();
                    } else {
                        swipeRefreshLayout.setRefreshing(false);
                    }
                });
            }

            // Show initial loading state
            showEmptyView(true, "Loading your ride requests...");

        } catch (Exception e) {
            Log.e(TAG, "onCreateView: Exception during initialization", e);
            Toast.makeText(getContext(), "Error initializing view: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.d(TAG, "onViewCreated: Loading ride requests");
        loadRideRequests();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (adapter != null && rideRequests.isEmpty() && !isLoading) {
            Log.d(TAG, "onResume: No requests loaded yet, loading now");
            loadRideRequests();
        }
    }

    private void loadRideRequests() {
        if (isLoading) {
            Log.d(TAG, "loadRideRequests: Already loading, ignoring request");
            return;
        }

        if (!isAdded()) {
            Log.d(TAG, "loadRideRequests: Fragment not attached, ignoring request");
            return;
        }

        isLoading = true;
        Log.d(TAG, "loadRideRequests: Starting to load ride requests");

        if (swipeRefreshLayout != null) {
            swipeRefreshLayout.setRefreshing(true);
        }

        // Clear any existing data
        rideRequests.clear();
        rideOffersMap.clear();

        try {
            Call<List<RideRequestResponse>> call = rideRequestApi.getUserRideRequests();
            Log.d(TAG, "loadRideRequests: Making API call to: " + call.request().url());

            call.enqueue(new Callback<List<RideRequestResponse>>() {
                @Override
                public void onResponse(Call<List<RideRequestResponse>> call, Response<List<RideRequestResponse>> response) {
                    if (!isAdded()) {
                        Log.d(TAG, "onResponse: Fragment not attached, ignoring response");
                        isLoading = false;
                        return;
                    }

                    try {
                        if (swipeRefreshLayout != null) {
                            swipeRefreshLayout.setRefreshing(false);
                        }

                        if (response.isSuccessful() && response.body() != null) {
                            List<RideRequestResponse> requests = response.body();
                            Log.d(TAG, "onResponse: Received " + requests.size() + " ride requests");

                            if (!requests.isEmpty()) {
                                // Log the first request details for debugging
                                RideRequestResponse firstRequest = requests.get(0);
                                Log.d(TAG, "First request - ID: " + firstRequest.getId()
                                        + ", Status: " + firstRequest.getRequestStatus()
                                        + ", RideOfferId: " + firstRequest.getRideOfferId());

                                rideRequests.addAll(requests);
                                adapter.notifyDataSetChanged();

                                // Show content instead of empty view
                                showEmptyView(false, null);

                                // Load ride offer details
                                fetchRideOfferDetails();
                            } else {
                                Log.d(TAG, "onResponse: No ride requests found");
                                showEmptyView(true, "You haven't joined any rides yet.");
                            }
                        } else {
                            Log.e(TAG, "onResponse: Request failed with code " + response.code());
                            String errorMsg = "Failed to load ride requests (Code: " + response.code() + ")";

                            try {
                                if (response.errorBody() != null) {
                                    errorMsg += "\n" + response.errorBody().string();
                                }
                            } catch (Exception e) {
                                Log.e(TAG, "Failed to read error body", e);
                            }

                            showEmptyView(true, "Error loading requests");
                            Toast.makeText(getContext(), errorMsg, Toast.LENGTH_LONG).show();
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "onResponse: Exception while processing response", e);
                        showEmptyView(true, "Error processing data");
                        Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    } finally {
                        isLoading = false;
                    }
                }

                @Override
                public void onFailure(Call<List<RideRequestResponse>> call, Throwable t) {
                    isLoading = false;

                    if (!isAdded()) {
                        Log.d(TAG, "onFailure: Fragment not attached, ignoring failure");
                        return;
                    }

                    try {
                        Log.e(TAG, "onFailure: Network request failed", t);

                        if (swipeRefreshLayout != null) {
                            swipeRefreshLayout.setRefreshing(false);
                        }

                        showEmptyView(true, "Network error");
                        Toast.makeText(getContext(), "Could not load ride requests: " + t.getMessage(), Toast.LENGTH_LONG).show();
                    } catch (Exception e) {
                        Log.e(TAG, "onFailure: Exception handling network failure", e);
                    }
                }
            });
        } catch (Exception e) {
            isLoading = false;
            Log.e(TAG, "loadRideRequests: Exception making API call", e);

            if (swipeRefreshLayout != null) {
                swipeRefreshLayout.setRefreshing(false);
            }

            showEmptyView(true, "Error loading requests");
            Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void showEmptyView(boolean show, String message) {
        if (!isAdded()) {
            return;
        }

        Log.d(TAG, "showEmptyView: " + show + (message != null ? ", message: " + message : ""));

        try {
            if (recyclerView != null && emptyView != null) {
                if (show) {
                    if (message != null) {
                        emptyView.setText(message);
                    }
                    emptyView.setVisibility(View.VISIBLE);
                    recyclerView.setVisibility(View.GONE);
                } else {
                    emptyView.setVisibility(View.GONE);
                    recyclerView.setVisibility(View.VISIBLE);
                }
            } else {
                Log.e(TAG, "showEmptyView: recyclerView or emptyView is null");
            }
        } catch (Exception e) {
            Log.e(TAG, "showEmptyView: Exception", e);
        }
    }

    private void fetchRideOfferDetails() {
        if (!isAdded() || rideRequests.isEmpty()) {
            return;
        }

        Log.d(TAG, "fetchRideOfferDetails: Starting to fetch details for " + rideRequests.size() + " ride offers");

        // Use AtomicInteger to track completion in async callbacks
        final AtomicInteger requestsProcessed = new AtomicInteger(0);
        final int totalRequests = rideRequests.size();

        // Prepare map for new data
        rideOffersMap.clear();

        // Process each ride request
        for (RideRequestResponse request : rideRequests) {
            Long rideOfferId = request.getRideOfferId();
            if (rideOfferId == null) {
                Log.e(TAG, "fetchRideOfferDetails: Ride offer ID is null for request ID: " + request.getId());
                requestsProcessed.incrementAndGet();
                continue;
            }

            try {
                Log.d(TAG, "fetchRideOfferDetails: Fetching details for ride offer ID: " + rideOfferId);

                Call<RideOfferResponse> call = rideOfferApi.getRideOfferDetails(rideOfferId);
                call.enqueue(new Callback<RideOfferResponse>() {
                    @Override
                    public void onResponse(Call<RideOfferResponse> call, Response<RideOfferResponse> response) {
                        int processed = requestsProcessed.incrementAndGet();

                        try {
                            if (!isAdded()) {
                                Log.d(TAG, "onResponse: Fragment not attached, ignoring response for ride offer " + rideOfferId);
                                return;
                            }

                            if (response.isSuccessful() && response.body() != null) {
                                Log.d(TAG, "onResponse: Got details for ride offer ID: " + rideOfferId);
                                rideOffersMap.put(rideOfferId, response.body());
                            } else {
                                Log.e(TAG, "onResponse: Failed to get details for ride offer ID: " + rideOfferId);
                            }

                            // Update UI if all requests have been processed
                            if (processed >= totalRequests) {
                                Log.d(TAG, "onResponse: All " + processed + " ride offer details fetched. Updating adapter.");
                                requireActivity().runOnUiThread(() -> {
                                    if (adapter != null) {
                                        adapter.notifyDataSetChanged();
                                    }
                                });
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "onResponse: Exception processing ride offer details", e);
                        }
                    }

                    @Override
                    public void onFailure(Call<RideOfferResponse> call, Throwable t) {
                        int processed = requestsProcessed.incrementAndGet();

                        if (!isAdded()) {
                            Log.d(TAG, "onFailure: Fragment not attached, ignoring failure for ride offer " + rideOfferId);
                            return;
                        }

                        Log.e(TAG, "onFailure: Failed to get details for ride offer ID: " + rideOfferId, t);

                        // Update UI if all requests have been processed
                        if (processed >= totalRequests) {
                            Log.d(TAG, "onFailure: All " + processed + " ride offer detail requests complete (some failed). Updating adapter.");
                            requireActivity().runOnUiThread(() -> {
                                if (adapter != null) {
                                    adapter.notifyDataSetChanged();
                                }
                            });
                        }
                    }
                });
            } catch (Exception e) {
                Log.e(TAG, "fetchRideOfferDetails: Exception for ride offer ID: " + rideOfferId, e);
                requestsProcessed.incrementAndGet();
            }
        }
    }

    @Override
    public void onCancelRequestClick(RideRequestResponse rideRequest) {
        if (!isAdded()) {
            return;
        }

        Log.d(TAG, "onCancelRequestClick: Canceling request ID: " + rideRequest.getId());

        try {
            new AlertDialog.Builder(getContext())
                    .setTitle("Cancel Request")
                    .setMessage("Are you sure you want to cancel this ride request?")
                    .setPositiveButton("Yes", (dialog, which) -> cancelRideRequest(rideRequest))
                    .setNegativeButton("No", null)
                    .show();
        } catch (Exception e) {
            Log.e(TAG, "onCancelRequestClick: Exception showing dialog", e);
            Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onViewRouteClick(RideOfferResponse rideOffer) {
        if (!isAdded() || rideOffer == null) {
            return;
        }

        Log.d(TAG, "onViewRouteClick: Viewing route for ride offer ID: " + rideOffer.getId());

        try {
            // Navigate to the map fragment
            Fragment mapRouteFragment = MapRouteFragment.newInstance(
                    rideOffer.getStartLocation(),
                    rideOffer.getEndLocation());

            requireActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, mapRouteFragment)
                    .addToBackStack(null)
                    .commit();
        } catch (Exception e) {
            Log.e(TAG, "onViewRouteClick: Exception navigating to map", e);
            Toast.makeText(getContext(), "Error showing route: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void cancelRideRequest(RideRequestResponse rideRequest) {
        if (!isAdded()) {
            return;
        }

        Log.d(TAG, "cancelRideRequest: Sending cancel request for ID: " + rideRequest.getId());

        ProgressDialog progressDialog = new ProgressDialog(requireContext());
        progressDialog.setMessage("Canceling ride request...");
        progressDialog.setCancelable(false);

        try {
            progressDialog.show();

            // Create a request to change the ride request status to CANCELED
            EditRideRequestRequest editRequest = new EditRideRequestRequest(rideRequest.getId(), "CANCELED");

            // Make the API call to cancel the request
            rideRequestApi.editRideRequestStatus(editRequest).enqueue(new Callback<RideRequestResponse>() {
                @Override
                public void onResponse(Call<RideRequestResponse> call, Response<RideRequestResponse> response) {
                    try {
                        progressDialog.dismiss();
                    } catch (Exception e) {
                        Log.e(TAG, "Error dismissing dialog", e);
                    }

                    if (!isAdded()) {
                        return;
                    }

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
                    try {
                        progressDialog.dismiss();
                    } catch (Exception e) {
                        Log.e(TAG, "Error dismissing dialog", e);
                    }

                    if (!isAdded()) {
                        return;
                    }

                    Log.e(TAG, "onFailure: Cancel request failed", t);
                    Toast.makeText(getContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } catch (Exception e) {
            try {
                progressDialog.dismiss();
            } catch (Exception dialogError) {
                Log.e(TAG, "Error dismissing dialog", dialogError);
            }

            Log.e(TAG, "cancelRideRequest: Exception", e);
            Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}