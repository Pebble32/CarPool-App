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
import com.example.carpool.data.database.CarPoolDatabase;
import com.example.carpool.data.database.RideOfferEntity;
import com.example.carpool.data.models.PageResponse;
import com.example.carpool.data.models.RideOfferResponse;
import com.example.carpool.data.models.RideRequestRequest;
import com.example.carpool.data.models.RideRequestResponse;
import com.example.carpool.data.models.RideStatus;
import com.example.carpool.ui.activities.MainActivity;
import com.example.carpool.ui.adapters.RideOffersAdapter;
import java.time.LocalDateTime;
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
 * It integrates persistent caching using Room. When a network call is successful,
 * ride offers are cached locally. If the network call fails, cached ride offers are loaded.
 */
public class RideOffersFragment extends Fragment implements RideOffersAdapter.OnRideOfferActionListener {
    private static final String TAG = "RideOffersFragment";
    private RecyclerView recyclerView;
    private Button buttonLoadMore, buttonGoToCreate;
    private RideOffersAdapter adapter;
    private int currentPage = 0;
    private int totalPages = 1;
    private final int PAGE_SIZE = 10;
    private RideOfferApi rideOfferApi;
    private RideRequestApi rideRequestApi;
    private String currentUserEmail;
    private Set<Long> joinRequestedRideOfferIds = new HashSet<>();
    private CarPoolDatabase db;

    /**
     * Inflates the layout, initializes UI components, API interfaces, and the Room database.
     *
     * @param inflater LayoutInflater for inflating views.
     * @param container The parent view that the fragment's UI should attach to.
     * @param savedInstanceState Saved instance state bundle.
     * @return The root view of the fragment.
     */
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
        adapter = new RideOffersAdapter(new ArrayList<>(), currentUserEmail, joinRequestedRideOfferIds, this);
        recyclerView.setAdapter(adapter);
        rideOfferApi = RetrofitClient.getInstance().create(RideOfferApi.class);
        rideRequestApi = RetrofitClient.getInstance().create(RideRequestApi.class);
        db = CarPoolDatabase.getInstance(getContext());
        buttonLoadMore.setOnClickListener(v -> loadRideOffers());
        fetchUserJoinRequests();
        loadRideOffers();
        return view;
    }

    /**
     * Ensures the bottom navigation is visible when the fragment is resumed.
     */
    @Override
    public void onResume() {
        super.onResume();
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).showBottomNav(true);
        }
    }

    /**
     * Fetches ride requests already sent by the current user and populates the join request set.
     */
    private void fetchUserJoinRequests() {
        Log.d(TAG, "Fetching user join requests");
        rideRequestApi.getUserRideRequests().enqueue(new Callback<List<RideRequestResponse>>() {
            @Override
            public void onResponse(Call<List<RideRequestResponse>> call, Response<List<RideRequestResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Log.d(TAG, "Received " + response.body().size() + " user ride requests");
                    joinRequestedRideOfferIds.clear();
                    for (RideRequestResponse req : response.body()) {
                        if (req.getRideOfferId() != null) {
                            joinRequestedRideOfferIds.add(req.getRideOfferId());
                        }
                    }
                    adapter.notifyDataSetChanged();
                } else {
                    Log.e(TAG, "Failed to fetch join requests: " + response.code());
                    Toast.makeText(getContext(), "Failed to fetch join requests", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<List<RideRequestResponse>> call, Throwable t) {
                Log.e(TAG, "Error fetching join requests: " + t.getMessage(), t);
                Toast.makeText(getContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Loads ride offers using a paginated API call. On success, ride offers are displayed and cached.
     * On failure, cached ride offers are loaded.
     */
    private void loadRideOffers() {
        Log.d(TAG, "Loading ride offers, page " + currentPage);
        if (currentPage >= totalPages) {
            buttonLoadMore.setVisibility(View.GONE);
            return;
        }
        rideOfferApi.getPaginatedOffers(currentPage, PAGE_SIZE).enqueue(new Callback<PageResponse<RideOfferResponse>>() {
            @Override
            public void onResponse(Call<PageResponse<RideOfferResponse>> call, Response<PageResponse<RideOfferResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    PageResponse<RideOfferResponse> pageResponse = response.body();
                    Log.d(TAG, "Received " + pageResponse.getContent().size() + " ride offers");
                    totalPages = pageResponse.getTotalPages();
                    adapter.addOffers(pageResponse.getContent());
                    currentPage++;
                    buttonLoadMore.setVisibility(currentPage < totalPages ? View.VISIBLE : View.GONE);
                    cacheRideOffers(pageResponse.getContent());
                } else {
                    Log.e(TAG, "Failed to load offers: " + response.code());
                    Toast.makeText(getContext(), "Failed to load offers, loading cached data", Toast.LENGTH_SHORT).show();
                    loadCachedOffers();
                }
            }
            @Override
            public void onFailure(Call<PageResponse<RideOfferResponse>> call, Throwable t) {
                Log.e(TAG, "Error loading offers: " + t.getMessage(), t);
                Toast.makeText(getContext(), "Error: " + t.getMessage() + ". Loading cached data", Toast.LENGTH_SHORT).show();
                loadCachedOffers();
            }
        });
    }

    /**
     * Caches the list of ride offers from the server into the local Room database.
     *
     * @param offers List of RideOfferResponse objects.
     */
    private void cacheRideOffers(List<RideOfferResponse> offers) {
        new Thread(() -> {
            List<RideOfferEntity> entities = new ArrayList<>();
            for (RideOfferResponse response : offers) {
                entities.add(convertResponseToEntity(response));
            }
            db.rideOfferDao().insertRideOffers(entities);
        }).start();
    }

    /**
     * Loads cached ride offers from the local Room database and updates the adapter.
     */
    private void loadCachedOffers() {
        new Thread(() -> {
            List<RideOfferEntity> cachedOffers = db.rideOfferDao().getAllRideOffers();
            List<RideOfferResponse> responses = new ArrayList<>();
            for (RideOfferEntity entity : cachedOffers) {
                responses.add(convertEntityToResponse(entity));
            }
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> adapter.addOffers(responses));
            }
        }).start();
    }

    /**
     * Converts a RideOfferResponse object to a RideOfferEntity for local storage.
     *
     * @param response The RideOfferResponse object.
     * @return A corresponding RideOfferEntity object.
     */
    private RideOfferEntity convertResponseToEntity(RideOfferResponse response) {
        LocalDateTime departureTime = null;
        try {
            if (response.getDepartureTime() != null && !response.getDepartureTime().isEmpty()) {
                departureTime = LocalDateTime.parse(response.getDepartureTime());
            }
        } catch (Exception e) {
            Log.e(TAG, "Error parsing departure time: " + e.getMessage(), e);
        }
        return new RideOfferEntity(
                response.getId(),
                response.getStartLocation(),
                response.getEndLocation(),
                response.getAvailableSeats(),
                departureTime,
                response.getStatus(),
                response.getCreatorEmail(),
                LocalDateTime.now(),
                LocalDateTime.now()
        );
    }

    /**
     * Converts a RideOfferEntity from the local database to a RideOfferResponse for display.
     *
     * @param entity The RideOfferEntity object.
     * @return A corresponding RideOfferResponse object.
     */
    private RideOfferResponse convertEntityToResponse(RideOfferEntity entity) {
        RideOfferResponse response = new RideOfferResponse();
        response.setId(entity.getId());
        response.setStartLocation(entity.getStartLocation());
        response.setEndLocation(entity.getEndLocation());
        if (entity.getDepartureTime() != null) {
            response.setDepartureTime(entity.getDepartureTime().toString());
        } else {
            response.setDepartureTime("Not specified");
        }
        response.setCreatorEmail(entity.getCreatorEmail());
        try {
            response.setRideStatus(RideStatus.valueOf(entity.getStatus()));
        } catch (Exception e) {
            response.setRideStatus(RideStatus.AVAILABLE);
        }
        response.setAvailableSeats(entity.getAvailableSeats());
        return response;
    }

    /**
     * Handles the edit action for a ride offer.
     *
     * @param rideOffer The RideOfferResponse object to be edited.
     */
    @Override
    public void onEditClick(RideOfferResponse rideOffer) {
        Log.d(TAG, "Editing ride offer #" + rideOffer.getId());
        Fragment editFragment = EditRideFragment.newInstance(rideOffer);
        getActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, editFragment)
                .addToBackStack(null)
                .commit();
    }

    /**
     * Handles the delete action for a ride offer.
     *
     * @param rideOffer The RideOfferResponse object to be deleted.
     */
    @Override
    public void onDeleteClick(RideOfferResponse rideOffer) {
        Log.d(TAG, "Delete clicked for ride offer #" + rideOffer.getId());
        new AlertDialog.Builder(getContext())
                .setTitle("Delete Ride Offer")
                .setMessage("Are you sure you want to delete this ride offer?")
                .setPositiveButton("Yes", (dialog, which) -> deleteRideOffer(rideOffer.getId()))
                .setNegativeButton("No", null)
                .show();
    }

    /**
     * Handles the join action for a ride offer.
     *
     * @param rideOffer The RideOfferResponse object to join.
     * @param position The adapter position.
     */
    @Override
    public void onJoinClick(RideOfferResponse rideOffer, int position) {
        Log.d(TAG, "Join clicked for ride offer #" + rideOffer.getId());
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
                    Log.d(TAG, "Successfully sent join request for ride #" + rideOffer.getId());
                    Toast.makeText(getContext(), "Join request sent", Toast.LENGTH_SHORT).show();
                    joinRequestedRideOfferIds.add(rideOffer.getId());
                    adapter.notifyItemChanged(position);
                } else {
                    Log.e(TAG, "Failed to send join request: " + response.code());
                    Toast.makeText(getContext(), "Failed to send join request", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                progressDialog.dismiss();
                Log.e(TAG, "Error sending join request: " + t.getMessage(), t);
                Toast.makeText(getContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Handles the route action for a ride offer.
     *
     * @param rideOffer The RideOfferResponse object whose route is to be displayed.
     */
    @Override
    public void onRouteClick(RideOfferResponse rideOffer) {
        Log.d(TAG, "Route clicked for ride offer #" + rideOffer.getId());
        Fragment mapRouteFragment = MapRouteFragment.newInstance(
                rideOffer.getStartLocation(),
                rideOffer.getEndLocation());
        getActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, mapRouteFragment)
                .addToBackStack(null)
                .commit();
    }

    /**
     * Deletes a ride offer via the API and reloads the list upon success.
     *
     * @param rideId The ID of the ride offer to delete.
     */
    private void deleteRideOffer(Long rideId) {
        Log.d(TAG, "Deleting ride offer #" + rideId);
        ProgressDialog progressDialog = new ProgressDialog(requireContext());
        progressDialog.setMessage("Deleting ride offer...");
        progressDialog.setCancelable(false);
        progressDialog.show();
        rideOfferApi.deleteRideOffer(rideId).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                progressDialog.dismiss();
                if (response.isSuccessful()) {
                    Log.d(TAG, "Successfully deleted ride offer #" + rideId);
                    Toast.makeText(getContext(), "Ride offer deleted successfully", Toast.LENGTH_SHORT).show();
                    resetAndReloadOffers();
                } else {
                    Log.e(TAG, "Failed to delete ride offer: " + response.code());
                    Toast.makeText(getContext(), "Failed to delete ride offer: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                progressDialog.dismiss();
                Log.e(TAG, "Error deleting ride offer: " + t.getMessage(), t);
                Toast.makeText(getContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Resets the adapter and reloads ride offers.
     */
    private void resetAndReloadOffers() {
        Log.d(TAG, "Resetting and reloading offers");
        adapter = new RideOffersAdapter(new ArrayList<>(), currentUserEmail, joinRequestedRideOfferIds, this);
        recyclerView.setAdapter(adapter);
        currentPage = 0;
        loadRideOffers();
    }

    /**
     * Handles the view requests action for a ride offer.
     *
     * @param rideOffer The RideOfferResponse object for which to view requests.
     */
    @Override
    public void onViewRequestsClick(RideOfferResponse rideOffer) {
        Log.d(TAG, "View requests clicked for ride offer #" + rideOffer.getId());
        Fragment rideRequestsFragment = RideRequestsFragment.newInstance(rideOffer.getId());
        getActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, rideRequestsFragment)
                .addToBackStack(null)
                .commit();
    }
}
