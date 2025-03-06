package com.example.carpool.ui.fragments;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
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

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Fragment for displaying rides the user has requested to join.
 */
public class MyRidesJoinedFragment extends Fragment implements MyRidesJoinedAdapter.OnRideRequestActionListener {

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
        
        recyclerView = view.findViewById(R.id.recyclerViewMyJoined);
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);
        emptyView = view.findViewById(R.id.textViewEmpty);
        
        // Set up RecyclerView with proper styling
        recyclerView.setHasFixedSize(true);
        
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
        currentUserEmail = sharedPreferences.getString("email", "");
        
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new MyRidesJoinedAdapter(rideRequests, rideOffersMap, this);
        recyclerView.setAdapter(adapter);
        
        rideRequestApi = RetrofitClient.getInstance().create(RideRequestApi.class);
        rideOfferApi = RetrofitClient.getInstance().create(RideOfferApi.class);
        
        swipeRefreshLayout.setOnRefreshListener(this::loadRideRequests);
        
        loadRideRequests();
        
        return view;
    }

    private void loadRideRequests() {
        swipeRefreshLayout.setRefreshing(true);
        
        rideRequestApi.getUserRideRequests().enqueue(new Callback<List<RideRequestResponse>>() {
            @Override
            public void onResponse(Call<List<RideRequestResponse>> call, Response<List<RideRequestResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    rideRequests.clear();
                    rideRequests.addAll(response.body());
                    
                    // Clear ride offers map and fetch details for each ride offer
                    rideOffersMap.clear();
                    fetchRideOfferDetails();
                    
                    // Show empty view if there are no requests
                    if (rideRequests.isEmpty()) {
                        emptyView.setVisibility(View.VISIBLE);
                        recyclerView.setVisibility(View.GONE);
                        swipeRefreshLayout.setRefreshing(false);
                    } else {
                        emptyView.setVisibility(View.GONE);
                        recyclerView.setVisibility(View.VISIBLE);
                    }
                } else {
                    swipeRefreshLayout.setRefreshing(false);
                    Toast.makeText(getContext(), "Failed to load your ride requests", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<RideRequestResponse>> call, Throwable t) {
                swipeRefreshLayout.setRefreshing(false);
                Toast.makeText(getContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchRideOfferDetails() {
        final int[] requestsProcessed = {0};
        final int totalRequests = rideRequests.size();
        
        if (totalRequests == 0) {
            swipeRefreshLayout.setRefreshing(false);
            return;
        }
        
        for (RideRequestResponse request : rideRequests) {
            rideOfferApi.getRideOfferDetails(request.getRideOfferId()).enqueue(new Callback<RideOfferResponse>() {
                @Override
                public void onResponse(Call<RideOfferResponse> call, Response<RideOfferResponse> response) {
                    requestsProcessed[0]++;
                    
                    if (response.isSuccessful() && response.body() != null) {
                        rideOffersMap.put(request.getRideOfferId(), response.body());
                    }
                    
                    if (requestsProcessed[0] >= totalRequests) {
                        swipeRefreshLayout.setRefreshing(false);
                        adapter.notifyDataSetChanged();
                    }
                }

                @Override
                public void onFailure(Call<RideOfferResponse> call, Throwable t) {
                    requestsProcessed[0]++;
                    
                    if (requestsProcessed[0] >= totalRequests) {
                        swipeRefreshLayout.setRefreshing(false);
                        adapter.notifyDataSetChanged();
                    }
                }
            });
        }
    }

    @Override
    public void onCancelRequestClick(RideRequestResponse rideRequest) {
        new AlertDialog.Builder(getContext())
                .setTitle("Cancel Request")
                .setMessage("Are you sure you want to cancel this ride request?")
                .setPositiveButton("Yes", (dialog, which) -> cancelRideRequest(rideRequest))
                .setNegativeButton("No", null)
                .show();
    }

    private void cancelRideRequest(RideRequestResponse rideRequest) {
        ProgressDialog progressDialog = new ProgressDialog(requireContext());
        progressDialog.setMessage("Canceling ride request...");
        progressDialog.setCancelable(false);
        progressDialog.show();
        
        // Create a request to change the ride request status to CANCELED
        EditRideRequestRequest editRequest = new EditRideRequestRequest(rideRequest.getId(), "CANCELED");
        
        // Make the API call to cancel the request
        // Note: You'll need to implement this method in your RideRequestApi interface
        rideRequestApi.editRideRequestStatus(editRequest).enqueue(new Callback<RideRequestResponse>() {
            @Override
            public void onResponse(Call<RideRequestResponse> call, Response<RideRequestResponse> response) {
                progressDialog.dismiss();
                if (response.isSuccessful()) {
                    Toast.makeText(getContext(), "Ride request canceled successfully", Toast.LENGTH_SHORT).show();
                    loadRideRequests();
                } else {
                    Toast.makeText(getContext(), "Failed to cancel ride request: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<RideRequestResponse> call, Throwable t) {
                progressDialog.dismiss();
                Toast.makeText(getContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}