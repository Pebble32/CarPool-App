package com.example.carpool.ui.fragments;

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
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RideOffersFragment extends Fragment {

    private RecyclerView recyclerView;
    private Button buttonLoadMore, buttonGoToCreate;
    private RideOffersAdapter adapter;
    private int currentPage = 0;
    private int totalPages = 1; // initial assumption
    private final int PAGE_SIZE = 10;
    private RideOfferApi rideOfferApi;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_ride_offers, container, false);
        recyclerView = view.findViewById(R.id.recyclerViewRideOffers);
        buttonLoadMore = view.findViewById(R.id.buttonLoadMore);
        buttonGoToCreate = view.findViewById(R.id.buttonGoToCreate);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new RideOffersAdapter(new ArrayList<>());
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
}
