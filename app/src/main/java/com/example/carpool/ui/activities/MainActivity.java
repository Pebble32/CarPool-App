package com.example.carpool.ui.activities;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import com.example.carpool.R;
import com.example.carpool.ui.fragments.LoginFragment;
import com.example.carpool.ui.fragments.RideOffersFragment;
import androidx.activity.OnBackPressedCallback;
import androidx.fragment.app.FragmentManager;

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
 * 
 * Methods:
 * - onCreate(Bundle savedInstanceState): Initializes the activity and sets up the initial fragment.
 * - handleOnBackPressed(): Custom back press handler to manage fragment transactions.
 */
public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main); // Loads the fragment container

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

                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_container, new RideOffersFragment())
                        .commit();
            }
        });
    }
}
