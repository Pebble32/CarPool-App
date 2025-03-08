package com.example.carpool.ui.fragments;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.carpool.R;
import com.example.carpool.data.api.RideOfferApi;
import com.example.carpool.data.api.RideRequestApi;
import com.example.carpool.data.api.RetrofitClient;
import com.example.carpool.data.models.PageResponse;
import com.example.carpool.data.models.RideOfferResponse;
import com.example.carpool.data.models.RideRequestRequest;
import com.example.carpool.data.models.RideRequestResponse;
import com.example.carpool.ui.activities.MainActivity;
import com.example.carpool.ui.adapters.RideOffersAdapter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * RideOffersFragment is responsible for displaying a list of ride offers in a RecyclerView.
 * In addition to editing and deleting ride offers, it now lets a user send a join request
 * for rides they do not own. The fragment maintains a set of ride offer IDs for which a join
 * request has already been sent.
 */
public class RideOffersFragment extends Fragment implements RideOffersAdapter.OnRideOfferActionListener {

    private RecyclerView recyclerView;
    private Button buttonLoadMore, buttonGoToCreate;
    private RideOffersAdapter adapter;
    private int currentPage = 0;
    private int totalPages = 1; // initial assumption
    private final int PAGE_SIZE = 10;
    private RideOfferApi rideOfferApi;
    private RideRequestApi rideRequestApi;
    private String currentUserEmail;
    // Set to store ride offer IDs for which the user already sent a join request
    private Set<Long> joinRequestedRideOfferIds = new HashSet<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_ride_offers, container, false);
        recyclerView = view.findViewById(R.id.ride_offers_recycler_view);
        buttonLoadMore = view.findViewById(R.id.buttonLoadMore);

        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
        currentUserEmail = sharedPreferences.getString("email", "");

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        // Pass the joinRequestedRideOfferIds set into the adapter
        adapter = new RideOffersAdapter(new ArrayList<>(), currentUserEmail, joinRequestedRideOfferIds, this);
        recyclerView.setAdapter(adapter);

        rideOfferApi = RetrofitClient.getInstance().create(RideOfferApi.class);
        rideRequestApi = RetrofitClient.getInstance().create(RideRequestApi.class);

        buttonLoadMore.setOnClickListener(v -> loadRideOffers());

        // First, fetch existing join requests for the current user
        fetchUserJoinRequests();
        loadRideOffers();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        ((MainActivity) getActivity()).showBottomNav(true);  // Show navigation bar
    }

    /**
     * Fetches the list of ride requests already sent by the current user
     * and populates the joinRequestedRideOfferIds set.
     */
    private void fetchUserJoinRequests() {
        // Assumes RideRequestApi.getUserRideRequests() returns List<RideRequestResponse>
        rideRequestApi.getUserRideRequests().enqueue(new Callback<List<RideRequestResponse>>() {
            @Override
            public void onResponse(Call<List<RideRequestResponse>> call, Response<List<RideRequestResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    for (RideRequestResponse req : response.body()) {
                        joinRequestedRideOfferIds.add(req.getRideOfferId());
                    }
                    adapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(getContext(), "Failed to fetch join requests", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<RideRequestResponse>> call, Throwable t) {
                Toast.makeText(getContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Loads ride offers using a paginated API call.
     */
    private void loadRideOffers() {
        if (currentPage >= totalPages) {
            buttonLoadMore.setVisibility(View.GONE);
            return;
        }
        rideOfferApi.getPaginatedOffers(currentPage, PAGE_SIZE).enqueue(new Callback<PageResponse<RideOfferResponse>>() {
            @Override
            public void onResponse(Call<PageResponse<RideOfferResponse>> call, Response<PageResponse<RideOfferResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    PageResponse<RideOfferResponse> pageResponse = response.body();
                    totalPages = pageResponse.getTotalPages();
                    adapter.addOffers(pageResponse.getContent());
                    currentPage++;
                    buttonLoadMore.setVisibility(currentPage < totalPages ? View.VISIBLE : View.GONE);
                } else {
                    Toast.makeText(getContext(), "Failed to load offers", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<PageResponse<RideOfferResponse>> call, Throwable t) {
                Toast.makeText(getContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onEditClick(RideOfferResponse rideOffer) {
        // Navigate to the EditRideFragment with the selected ride offer
        Fragment editFragment = EditRideFragment.newInstance(rideOffer);
        getActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, editFragment)
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void onDeleteClick(RideOfferResponse rideOffer) {
        new AlertDialog.Builder(getContext())
                .setTitle("Delete Ride Offer")
                .setMessage("Are you sure you want to delete this ride offer?")
                .setPositiveButton("Yes", (dialog, which) -> deleteRideOffer(rideOffer.getId()))
                .setNegativeButton("No", null)
                .show();
    }

    @Override
    public void onJoinClick(RideOfferResponse rideOffer, int position) {
        RideRequestRequest request = new RideRequestRequest(rideOffer.getId());
        ProgressDialog progressDialog = new ProgressDialog(requireContext());
        progressDialog.setMessage("Sending join request...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        rideRequestApi.createRideRequest(request).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                progressDialog.dismiss();
                if (response.isSuccessful()) {
                    Toast.makeText(getContext(), "Join request sent", Toast.LENGTH_SHORT).show();
                    // Add the ride offer ID to the set and update the adapter to disable the join button.
                    joinRequestedRideOfferIds.add(rideOffer.getId());
                    adapter.notifyItemChanged(position);
                } else {
                    Toast.makeText(getContext(), "Failed to send join request", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                progressDialog.dismiss();
                Toast.makeText(getContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Deletes a ride offer and reloads the list.
     */
    private void deleteRideOffer(Long rideId) {
        ProgressDialog progressDialog = new ProgressDialog(requireContext());
        progressDialog.setMessage("Deleting ride offer...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        rideOfferApi.deleteRideOffer(rideId).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                progressDialog.dismiss();
                if (response.isSuccessful()) {
                    Toast.makeText(getContext(), "Ride offer deleted successfully", Toast.LENGTH_SHORT).show();
                    resetAndReloadOffers();
                } else {
                    Toast.makeText(getContext(), "Failed to delete ride offer: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                progressDialog.dismiss();
                Toast.makeText(getContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Resets the adapter and reloads the ride offers.
     */
    private void resetAndReloadOffers() {
        adapter = new RideOffersAdapter(new ArrayList<>(), currentUserEmail, joinRequestedRideOfferIds, this);
        recyclerView.setAdapter(adapter);
        currentPage = 0;
        loadRideOffers();
    }


    /**
     * Handles the click event for viewing ride requests.
     */
    @Override
    public void onViewRequestsClick(RideOfferResponse rideOffer) {
    Fragment rideRequestsFragment = RideRequestsFragment.newInstance(rideOffer.getId());
    getActivity().getSupportFragmentManager().beginTransaction()
            .replace(R.id.fragment_container, rideRequestsFragment)
            .addToBackStack(null)
            .commit();
    }
}
