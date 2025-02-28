package com.example.carpool.ui.fragments;

import android.app.AlertDialog;
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
import com.example.carpool.data.api.RetrofitClient;
import com.example.carpool.data.models.PageResponse;
import com.example.carpool.data.models.RideOfferResponse;
import com.example.carpool.ui.adapters.RideOffersAdapter;
import java.util.ArrayList;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import android.app.ProgressDialog;



/**
 * RideOffersFragment is responsible for displaying a list of ride offers in a RecyclerView.
 * It allows users to load more offers, create a new ride offer, edit existing offers, and delete offers.
 * This fragment implements the RideOffersAdapter.OnRideOfferActionListener interface to handle user actions on ride offers.
 */
public class RideOffersFragment extends Fragment implements RideOffersAdapter.OnRideOfferActionListener {

    private RecyclerView recyclerView;
    private Button buttonLoadMore, buttonGoToCreate;
    private RideOffersAdapter adapter;
    private int currentPage = 0;
    private int totalPages = 1; // initial assumption
    private final int PAGE_SIZE = 10;
    private RideOfferApi rideOfferApi;
    private String currentUserEmail;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_ride_offers, container, false);
        recyclerView = view.findViewById(R.id.ride_offers_recycler_view);
        buttonLoadMore = view.findViewById(R.id.buttonLoadMore);
        buttonGoToCreate = view.findViewById(R.id.buttonGoToCreate);

        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
        currentUserEmail = sharedPreferences.getString("email", "");

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new RideOffersAdapter(new ArrayList<>(), currentUserEmail, this);
        recyclerView.setAdapter(adapter);

        rideOfferApi = RetrofitClient.getInstance().create(RideOfferApi.class);

        buttonLoadMore.setOnClickListener(v -> loadRideOffers());

        buttonGoToCreate.setOnClickListener(v -> {
            getActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new CreateRideFragment())
                    .addToBackStack(null)
                    .commit();
        });

        loadRideOffers();

        return view;
    }

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

    private void deleteRideOffer(Long rideId){

        ProgressDialog progressDialog = new ProgressDialog(requireContext());
        progressDialog.setMessage("Deleting ride offer...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        rideOfferApi.deleteRideOffer(rideId).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                progressDialog.dismiss();
                if(response.isSuccessful()){
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

    private void resetAndReloadOffers(){
        adapter = new RideOffersAdapter(new ArrayList<>(), currentUserEmail, this);
        recyclerView.setAdapter(adapter);
        currentPage = 0;
        loadRideOffers();
    }
}
