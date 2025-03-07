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
import com.example.carpool.data.api.RetrofitClient;
import com.example.carpool.data.models.RideOfferResponse;
import com.example.carpool.ui.adapters.MyRidesOfferedAdapter;

import java.util.ArrayList;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Fragment for displaying rides offered by the current user.
 */
public class MyRidesOfferedFragment extends Fragment implements MyRidesOfferedAdapter.OnRideOfferActionListener {
    private static final String TAG = "MyRidesOfferedFrag";

    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private TextView emptyView;
    private TextView titleView;
    private MyRidesOfferedAdapter adapter;
    private RideOfferApi rideOfferApi;
    private String currentUserEmail;
    private List<RideOfferResponse> rideOffers = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_my_rides_offered, container, false);

        Log.d(TAG, "onCreateView: Initializing views");

        recyclerView = view.findViewById(R.id.recyclerViewMyJoined);
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);
        emptyView = view.findViewById(R.id.textViewEmpty);
        titleView = view.findViewById(R.id.textViewPageTitle);

        // Update the title
        if (titleView != null) {
            titleView.setText("My Ride Offers");
        }

        // Verify views
        if (recyclerView == null) {
            Log.e(TAG, "onCreateView: recyclerView is null! Check the ID in the layout");
            Toast.makeText(getContext(), "Error: Could not find recycler view", Toast.LENGTH_SHORT).show();
            return view;
        }

        // Set up RecyclerView with proper styling
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
        currentUserEmail = sharedPreferences.getString("email", "");
        Log.d(TAG, "onCreateView: Current user email: " + currentUserEmail);

        adapter = new MyRidesOfferedAdapter(rideOffers, this);
        recyclerView.setAdapter(adapter);

        rideOfferApi = RetrofitClient.getInstance().create(RideOfferApi.class);

        if (swipeRefreshLayout != null) {
            swipeRefreshLayout.setOnRefreshListener(this::loadRideOffers);
        }

        loadRideOffers();

        return view;
    }

    private void loadRideOffers() {
        Log.d(TAG, "loadRideOffers: Loading ride offers");
        if (swipeRefreshLayout != null) {
            swipeRefreshLayout.setRefreshing(true);
        }

        rideOfferApi.getUserRideHistory().enqueue(new Callback<List<RideOfferResponse>>() {
            @Override
            public void onResponse(Call<List<RideOfferResponse>> call, Response<List<RideOfferResponse>> response) {
                if (swipeRefreshLayout != null) {
                    swipeRefreshLayout.setRefreshing(false);
                }

                if (response.isSuccessful() && response.body() != null) {
                    Log.d(TAG, "onResponse: Received " + response.body().size() + " ride offers");

                    rideOffers.clear();
                    rideOffers.addAll(response.body());
                    adapter.notifyDataSetChanged();

                    // Show empty view if there are no offers
                    showEmptyView(rideOffers.isEmpty());
                } else {
                    Log.e(TAG, "onResponse: Failed to load offers: " + response.code());
                    showEmptyView(true);
                    Toast.makeText(getContext(), "Failed to load your ride offers", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<RideOfferResponse>> call, Throwable t) {
                Log.e(TAG, "onFailure: Failed to load offers", t);
                if (swipeRefreshLayout != null) {
                    swipeRefreshLayout.setRefreshing(false);
                }
                showEmptyView(true);
                Toast.makeText(getContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showEmptyView(boolean show) {
        if (recyclerView == null) {
            Log.e(TAG, "showEmptyView: recyclerView is null");
            return;
        }

        if (emptyView == null) {
            Log.e(TAG, "showEmptyView: emptyView is null");
            return;
        }

        Log.d(TAG, "showEmptyView: " + show);
        if (show) {
            emptyView.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            emptyView.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onEditClick(RideOfferResponse rideOffer) {
        Log.d(TAG, "onEditClick: Editing ride offer #" + rideOffer.getId());
        // Navigate to the EditRideFragment with the selected ride offer
        Fragment editFragment = EditRideFragment.newInstance(rideOffer);
        requireActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, editFragment)
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void onDeleteClick(RideOfferResponse rideOffer) {
        Log.d(TAG, "onDeleteClick: Requesting to delete ride offer #" + rideOffer.getId());
        new AlertDialog.Builder(getContext())
                .setTitle("Delete Ride Offer")
                .setMessage("Are you sure you want to delete this ride offer?")
                .setPositiveButton("Yes", (dialog, which) -> deleteRideOffer(rideOffer.getId()))
                .setNegativeButton("No", null)
                .show();
    }

    @Override
    public void onMarkFinishedClick(RideOfferResponse rideOffer) {
        Log.d(TAG, "onMarkFinishedClick: Requesting to mark ride offer #" + rideOffer.getId() + " as finished");
        new AlertDialog.Builder(getContext())
                .setTitle("Mark Ride as Finished")
                .setMessage("Are you sure you want to mark this ride as finished? This action cannot be undone.")
                .setPositiveButton("Yes", (dialog, which) -> markRideAsFinished(rideOffer.getId()))
                .setNegativeButton("No", null)
                .show();
    }

    private void deleteRideOffer(Long rideId) {
        Log.d(TAG, "deleteRideOffer: Deleting ride offer #" + rideId);
        ProgressDialog progressDialog = new ProgressDialog(requireContext());
        progressDialog.setMessage("Deleting ride offer...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        rideOfferApi.deleteRideOffer(rideId).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                progressDialog.dismiss();
                if (response.isSuccessful()) {
                    Log.d(TAG, "onResponse: Successfully deleted ride offer #" + rideId);
                    Toast.makeText(getContext(), "Ride offer deleted successfully", Toast.LENGTH_SHORT).show();
                    loadRideOffers();
                } else {
                    Log.e(TAG, "onResponse: Failed to delete ride offer: " + response.code());
                    Toast.makeText(getContext(), "Failed to delete ride offer: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e(TAG, "onFailure: Failed to delete ride offer", t);
                progressDialog.dismiss();
                Toast.makeText(getContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void markRideAsFinished(Long rideId) {
        Log.d(TAG, "markRideAsFinished: Marking ride offer #" + rideId + " as finished");
        ProgressDialog progressDialog = new ProgressDialog(requireContext());
        progressDialog.setMessage("Marking ride as finished...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        rideOfferApi.markRideAsFinished(rideId).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                progressDialog.dismiss();
                if (response.isSuccessful()) {
                    Log.d(TAG, "onResponse: Successfully marked ride offer #" + rideId + " as finished");
                    Toast.makeText(getContext(), "Ride marked as finished successfully", Toast.LENGTH_SHORT).show();
                    loadRideOffers();
                } else {
                    Log.e(TAG, "onResponse: Failed to mark ride as finished: " + response.code());
                    Toast.makeText(getContext(), "Failed to mark ride as finished: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e(TAG, "onFailure: Failed to mark ride as finished", t);
                progressDialog.dismiss();
                Toast.makeText(getContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}