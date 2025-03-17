package com.example.carpool.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.carpool.R;
import com.example.carpool.ui.activities.MainActivity;

public class RoutesFragment extends Fragment {

    private EditText editStartLocation, editEndLocation;
    private Button buttonShowRoute;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_routes, container, false);

        editStartLocation = view.findViewById(R.id.editStartLocation);
        editEndLocation = view.findViewById(R.id.editEndLocation);
        buttonShowRoute = view.findViewById(R.id.buttonShowRoute);

        buttonShowRoute.setOnClickListener(v -> {
            String startLocation = editStartLocation.getText().toString().trim();
            String endLocation = editEndLocation.getText().toString().trim();

            if (startLocation.isEmpty() || endLocation.isEmpty()) {
                Toast.makeText(getContext(), "Please enter both start and end locations", Toast.LENGTH_SHORT).show();
                return;
            }

            // Navigate to the MapViewFragment with the selected locations
            MapViewFragment mapFragment = MapViewFragment.newInstance(startLocation, endLocation);
            requireActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, mapFragment)
                    .addToBackStack(null)
                    .commit();
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        ((MainActivity) requireActivity()).showBottomNav(true);
    }
}