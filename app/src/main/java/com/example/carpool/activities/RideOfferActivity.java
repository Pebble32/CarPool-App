package com.example.carpool.activities;

import android.os.Bundle;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.google.android.material.snackbar.Snackbar;
import com.example.carpool.adapters.RideOfferAdapter;
import com.example.carpool.databinding.ActivityRideOfferBinding;
import com.example.carpool.models.RideOffer;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * RideOfferActivity is an activity that displays a list of ride offers and allows users to add new ride offers.
 * It uses a RecyclerView to display the ride offers and a FloatingActionButton (FAB) to trigger the add ride offer functionality.
 *
 * This activity performs the following functions:
 * 
 * Initializes the view binding and sets the content view.
 * Sets up the RecyclerView with a LinearLayoutManager and a RideOfferAdapter.
 * Populates the RecyclerView with dummy ride offers for demonstration purposes.
 * Sets up a click listener on the FAB to handle adding new ride offers.
 * 
 *
 * Note: The add ride offer functionality is currently not implemented and shows a Snackbar message when the FAB is clicked.
 *
 * @see AppCompatActivity
 * @see ActivityRideOfferBinding
 * @see RideOfferAdapter
 * @see RideOffer
 */
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