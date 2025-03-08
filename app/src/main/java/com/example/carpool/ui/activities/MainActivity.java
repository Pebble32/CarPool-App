package com.example.carpool.ui.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.os.Bundle;
import android.view.View;

import com.example.carpool.R;
import com.example.carpool.ui.fragments.CreateRideFragment;
import com.example.carpool.ui.fragments.LoginFragment;
import com.example.carpool.ui.fragments.MyRidesFragment;
import com.example.carpool.ui.fragments.RideOffersFragment;
import androidx.activity.OnBackPressedCallback;
import androidx.fragment.app.FragmentManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;

/**
 * MainActivity is the entry point of the application.
 * It extends AppCompatActivity and sets up the initial UI and behavior.
 *
 * On creation, it loads the main activity layout and initializes the fragment container.
 * If this is the first creation, it loads the LoginFragment into the container.
 *
 * It also sets up a custom back press handler that:
 * - Clears the back stack if there are any entries.
 * - Replaces the current fragment with the RideOffersFragment.
 * - Sets the bottom navigation to show the Browse tab as selected.
 *
 * Methods:
 * - onCreate(Bundle savedInstanceState): Initializes the activity and sets up the initial fragment.
 * - handleOnBackPressed(): Custom back press handler to manage fragment transactions.
 */
public class MainActivity extends AppCompatActivity {
    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main); // Loads the fragment container

        bottomNavigationView = findViewById(R.id.bottomNavigationView);

        bottomNavigationView.setOnItemSelectedListener(item -> {
            changeFragment(item.getItemId(), false);
            return true;
        });

        // If first creation, load LoginFragment into the container
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new LoginFragment())
                    .commit();
        }


        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {

                if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
                    getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                }

                // Replace with RideOffersFragment (Browse)
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_container, new RideOffersFragment())
                        .commit();

                // Update bottom nav to show Browse tab as selected
                bottomNavigationView.setSelectedItemId(R.id.nav_browse);
            }
        });
    }

    public void changeFragment(int navFragmentId) {
        changeFragment(navFragmentId, true);
    }

    private void changeFragment(int navFragmentId, boolean updateNav) {
        Fragment selectedFragment = null;

        if (navFragmentId == R.id.nav_my_rides) {
            selectedFragment = new MyRidesFragment();
        } else if (navFragmentId == R.id.nav_browse) {
            selectedFragment = new RideOffersFragment();
        } else if (navFragmentId == R.id.nav_create_ride) {
            selectedFragment = new CreateRideFragment();
        } else if (navFragmentId == R.id.nav_profile) {
            selectedFragment = null;
        }

        if (selectedFragment != null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, selectedFragment)
                    .commit();
        }

        if (updateNav) bottomNavigationView.setSelectedItemId(navFragmentId);
    }

    public void showBottomNav(boolean show) {
        if (bottomNavigationView != null) {
            bottomNavigationView.setVisibility(show ? View.VISIBLE : View.GONE);
        }
    }

    /**
     * Public method to navigate to the main screen after login.
     * Sets the Browse tab as the default.
     */
    public void navigateToMainScreen() {
        // Load the RideOffersFragment (Browse)
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, new RideOffersFragment())
                .commit();

        // Show the bottom navigation
        showBottomNav(true);

        // Select the Browse tab
        bottomNavigationView.setSelectedItemId(R.id.nav_browse);
    }
}