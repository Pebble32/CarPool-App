package com.example.carpool.ui.fragments;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.example.carpool.R;
import com.example.carpool.data.api.RideOfferApi;
import com.example.carpool.data.api.RideRequestApi;
import com.example.carpool.data.api.RetrofitClient;
import com.example.carpool.ui.activities.MainActivity;
import com.example.carpool.ui.adapters.MyRidesPagerAdapter;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

/**
 * MyRidesFragment displays the user's ride offers and ride requests.
 * It uses a TabLayout and ViewPager to switch between "Rides Offered" and "Rides Joined".
 */
public class MyRidesFragment extends Fragment {

    private TabLayout tabLayout;
    private ViewPager2 viewPager;
    private RideOfferApi rideOfferApi;
    private RideRequestApi rideRequestApi;
    private String currentUserEmail;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_my_rides, container, false);
        
        tabLayout = view.findViewById(R.id.tabLayout);
        viewPager = view.findViewById(R.id.viewPager);
        
        // Set up the toolbar
        com.google.android.material.appbar.MaterialToolbar toolbar = view.findViewById(R.id.topAppBar);
        toolbar.setTitle("My Rides");
        
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
        currentUserEmail = sharedPreferences.getString("email", "");
        
        rideOfferApi = RetrofitClient.getInstance().create(RideOfferApi.class);
        rideRequestApi = RetrofitClient.getInstance().create(RideRequestApi.class);
        
        setupViewPager();
        
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        ((MainActivity) getActivity()).showBottomNav(true);  // Show navigation bar
    }
    
    private void setupViewPager() {
        MyRidesPagerAdapter pagerAdapter = new MyRidesPagerAdapter(this);
        viewPager.setAdapter(pagerAdapter);
        
        // Connect TabLayout with ViewPager2
        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            switch (position) {
                case 0:
                    tab.setText("My Offerings");
                    break;
                case 1:
                    tab.setText("Rides Joined");
                    break;
            }
        }).attach();
    }
}