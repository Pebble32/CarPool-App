
package com.example.carpool.ui.fragments;

import android.app.AlertDialog;
import android.app.ProgressDialog;
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

import com.example.carpool.R;
import com.example.carpool.data.api.RetrofitClient;
import com.example.carpool.data.api.RideRequestApi;
import com.example.carpool.data.models.AnswerRideRequestRequest;
import com.example.carpool.data.models.RideRequestResponse;
import com.example.carpool.ui.activities.MainActivity;
import com.example.carpool.ui.adapters.RideRequestsAdapter;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * A fragment that displays all ride requests for a specific ride offer and allows
 * the ride offer creator to accept or decline those requests.
 */
public class RideRequestsFragment extends Fragment implements RideRequestsAdapter.OnRideRequestActionListener {

    private RecyclerView recyclerView;
    private RideRequestAdapter adapter;
    private RideRequestApi rideRequestApi;
    private Long rideOfferId;
    private TextView emptyStateTextView;



    /**
     * Creates a new instance of RideRequestsFragment with the given ride offer ID.
     *
     * @param rideOfferId The ID of the ride offer to display requests for.
     * @return A new instance of RideRequestsFragment.
     */
    public static RideRequestsFragment newInstance(Long rideOfferId) {
        RideRequestsFragment fragment = new RideRequestsFragment();
        Bundle args = new Bundle();
        args.putLong("ride_offer_id", rideOfferId);
        fragment.setArguments(args);
        return fragment;
    }


    @Nullable
    @Override
    public view onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle saveInstanceState){
        View view = inflater.inflate(R.layout.fragment_ride_requests, container, false);

        recyclerView = view.findViewById(R.id.recycler_view);
        emptyStateTextView = view.findViewById(R.id.empty_state_text_view);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new RideRequestsAdapter(new ArrayList<>(), this);
        recyclerView.setAdapter(adapter);

        rideRequestApi = RetrofitClient.getInstance().create(RideRequestApi.class);

        if (getArguments() != null) {
            rideOfferId = getArguments().getLong("ride_offer_id");
            loadRideRequests();
        } else {
            Toast.makeText(getContext(), "Ride offer id not provided", Toast.LENGTH_SHORT).show();
            if(getActivity() != null) {
                getActivity().getSupportFragmentManager().popBackStack();
            }
        }
        return view;
    }

    @Override
    public void onResume(){
        super.onResume();
        if (getActivity() instanceof MainActivity){
            ((MainActivity) getActivity()).showBottomNav(true);
        }
    }

    /**
     * Loads all ride requests for the specified ride offer.
     */
    private void loadRideRequests(){
        ProgressDialog progressDialog = new ProgressDialog(requireContext());
        progressDialog.setMessage("Loading ride requests...");
        progressDialog.setCancelable(false); 
        progressDialog.show();

        rideRequestApi.getRideRequestsForRideOffer(rideOfferId).enqueue(new Callback<List<RideRequestResponse>>(){
            @Override
            public void onResponse(Call<List<RideRequestResponse>> call, Response<List<RideRequestResponse>> response){
                progressDialog.dismiss();
                if (response.isSuccessful() && response.body() != null){
                    List<RideRequestResponse> rideRequests = response.body();
                    if (rideRequests.isEmpty()){
                        recyclerView.setVisibility(View.GONE);
                        emptyStateTextView.setVisibility(View.VISIBLE);
                    } else {
                        recyclerView.setVisibility(View.VISIBLE);
                        emptyStateTextView.setVisibility(View.GONE);
                        adapter.setRideRequests(rideRequests);
                    }
                } else {
                    Toast.makeText(getContext(), "Failed to load ride requests", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<RideRequestResponse>> call, Throwable t){
                progressDialog.dismiss();
                Toast.makeText(getContext(),"Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
                
        });
    }

    @Override
    public void onAcceptClick(RideRequestResponse rideRequest){
        showConfirmationDialog(
            rideRequest,
            "Accept Request",
            "Are you sure you want to accept this ride request?",
            AnswerRideRequestRequest.AnswerStatus.ACCEPTED
        );
    }

    @Override
    public void onDeclineClick(RideRequestResponse rideRequest){
        showConfirmationDialog(
            rideRequest,
            "Decline Request",
            "Are you sure you want to decline this ride request?",
            AnswerRideRequestRequest.AnswerStatus.REJECTED
        );
    }


    /**
     * Shows a confirmation dialog before accepting or declining a ride request.
     */
    private void showConfirmationDialog(RideRequestResponse rideRequest, String title, String message, String answerStatus){
        new AlrertDialog.Builder(getContext())
        .setTitle(title)
        .setMessage(message)
        .setPositiveButton("Yes", (dialog, which) -> answerRideRequest(rideRequest, answerStatus))
        .setNegativeButton("No", null)
        .show();
    }


    /**
     * Sends a request to the server to accept or decline a ride request.
     */
    private void answerRideRequest(Long rideRequestId, String answerStatus) {
        ProgressDialog progressDialog = new ProgressDialog(requireContext());
        progressDialog.setMessage("Processing request...");
        progressDialog.setCancelable(false);
        progressDialog.show();
        
        AnswerRideRequestRequest request = new AnswerRideRequestRequest(rideRequestId, answerStatus);
        
        rideRequestApi.answerRideRequest(request).enqueue(new Callback<RideRequestResponse>() {
            @Override
            public void onResponse(Call<RideRequestResponse> call, Response<RideRequestResponse> response) {
                progressDialog.dismiss();
                if (response.isSuccessful() && response.body() != null) {
                    String statusMessage = answerStatus.equals(AnswerRideRequestRequest.AnswerStatus.ACCEPTED) 
                            ? "Request accepted" : "Request declined";
                    Toast.makeText(getContext(), statusMessage, Toast.LENGTH_SHORT).show();
                    loadRideRequests(); 
                } else {
                    Toast.makeText(getContext(), "Failed to process the request", Toast.LENGTH_SHORT).show();
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