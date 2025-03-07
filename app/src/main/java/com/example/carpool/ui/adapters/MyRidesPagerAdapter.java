package com.example.carpool.ui.adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.carpool.ui.fragments.MyRidesOfferedFragment;
import com.example.carpool.ui.fragments.MyRidesJoinedFragment;

/**
 * Adapter for the ViewPager2 in MyRidesFragment.
 * It manages the two tabs: "Rides Offered" and "Rides Joined".
 */
public class MyRidesPagerAdapter extends FragmentStateAdapter {

    /**
     * Constructor that takes a FragmentManager and Lifecycle
     * @param fragmentManager The fragment manager
     * @param lifecycle The lifecycle
     */
    public MyRidesPagerAdapter(@NonNull FragmentManager fragmentManager, @NonNull Lifecycle lifecycle) {
        super(fragmentManager, lifecycle);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        // Return the appropriate fragment for each tab
        switch (position) {
            case 0:
                return new MyRidesOfferedFragment();
            case 1:
                return new MyRidesJoinedFragment();
            default:
                return new MyRidesOfferedFragment();
        }
    }

    @Override
    public int getItemCount() {
        return 2; // Two tabs: Rides Offered and Rides Joined
    }
}