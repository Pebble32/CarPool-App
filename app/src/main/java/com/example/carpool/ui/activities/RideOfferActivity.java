package com.example.carpool.ui.activities;

import android.os.Bundle;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.google.android.material.snackbar.Snackbar;
import com.example.carpool.ui.adapters.RideOfferAdapter;
import com.example.carpool.databinding.ActivityRideOfferBinding;
import com.example.carpool.data.models.RideOffer;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class RideOfferActivity extends AppCompatActivity {
    private ActivityRideOfferBinding binding;
    private RideOfferAdapter rideOfferAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRideOfferBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Set up RecyclerView
        binding.rideOffersRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        rideOfferAdapter = new RideOfferAdapter(getDummyRideOffers());
        binding.rideOffersRecyclerView.setAdapter(rideOfferAdapter);

        // Set up FAB
        binding.fabAddRideOffer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: Implement add ride offer functionality
                Snackbar.make(binding.getRoot(),
                        "Add ride offer clicked",
                        Snackbar.LENGTH_SHORT).show();
            }
        });
    }

    private List<RideOffer> getDummyRideOffers() {
        List<RideOffer> rideOffers = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();

        rideOffers.add(new RideOffer(
                1L,
                "Reykjavik",
                "Akureyri",
                3,
                now.plusDays(1),
                "AVAILABLE",
                "john@example.com",
                now,
                now
        ));

        rideOffers.add(new RideOffer(
                2L,
                "Akureyri",
                "Reykjavik",
                2,
                now.plusDays(2),
                "AVAILABLE",
                "jane@example.com",
                now,
                now
        ));

        return rideOffers;
    }
}