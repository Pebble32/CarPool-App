package com.example.carpool.ui.fragments;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.carpool.R;
import com.example.carpool.ui.activities.MainActivity;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.AutocompletePrediction;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest;
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsResponse;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.material.appbar.MaterialToolbar;

import java.util.Arrays;
import java.util.List;

/**
 * MapRouteFragment displays a Google Map with the route between the ride's origin and destination.
 * It geocodes the locations using Google Places API and draws a route on the map.
 */
public class MapRouteFragment extends Fragment implements OnMapReadyCallback {

    private static final String TAG = "MapRouteFragment";
    private GoogleMap mMap;
    private String startLocation;
    private String endLocation;
    private TextView textViewStartLocation;
    private TextView textViewEndLocation;
    private TextView textViewEstimatedTime;
    private PlacesClient placesClient;
    private LatLng startLatLng;
    private LatLng endLatLng;

    /**
     * Creates a new instance of MapRouteFragment with the provided start and end locations.
     *
     * @param startLocation The origin of the ride
     * @param endLocation The destination of the ride
     * @return A new instance of MapRouteFragment
     */
    public static MapRouteFragment newInstance(String startLocation, String endLocation) {
        MapRouteFragment fragment = new MapRouteFragment();
        Bundle args = new Bundle();
        args.putString("start_location", startLocation);
        args.putString("end_location", endLocation);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            startLocation = getArguments().getString("start_location");
            endLocation = getArguments().getString("end_location");
        }

        // Initialize the Places API
        if (!Places.isInitialized()) {
            Places.initialize(requireContext(), getString(R.string.google_maps_key));
        }
        placesClient = Places.createClient(requireContext());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_map_route, container, false);

        // Set up the toolbar
        MaterialToolbar toolbar = view.findViewById(R.id.topAppBar);
        toolbar.setNavigationOnClickListener(v -> requireActivity().getOnBackPressedDispatcher().onBackPressed());

        // Hide bottom navigation if present
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).showBottomNav(false);
        }

        // Initialize TextViews
        textViewStartLocation = view.findViewById(R.id.textViewStartLocation);
        textViewEndLocation = view.findViewById(R.id.textViewEndLocation);
        textViewEstimatedTime = view.findViewById(R.id.textViewEstimatedTime);

        // Set location names
        textViewStartLocation.setText("From: " + startLocation);
        textViewEndLocation.setText("To: " + endLocation);

        // Get the SupportMapFragment and request notification when the map is ready
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        } else {
            Log.e(TAG, "Error: Map fragment is null");
            Toast.makeText(getContext(), "Error loading map. Please try again.", Toast.LENGTH_SHORT).show();
        }

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        // Hide bottom navigation
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).showBottomNav(false);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        try {
            // Enable my location (permission should be checked in the parent activity)
            mMap.setMyLocationEnabled(true);
        } catch (SecurityException e) {
            Log.e(TAG, "Error enabling my location: " + e.getMessage());
        }

        // Enable zoom controls and compass
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setCompassEnabled(true);
        mMap.getUiSettings().setMapToolbarEnabled(true);

        // Geocode the locations and display markers
        geocodeLocations();
    }

    private void geocodeLocations() {
        geocodePlace(startLocation, true);
        geocodePlace(endLocation, false);
    }

    private void geocodePlace(String locationName, boolean isStart) {
        Log.d(TAG, "Geocoding location: " + locationName);

        // Search for the location using the Places API
        FindAutocompletePredictionsRequest request = FindAutocompletePredictionsRequest.builder()
                .setQuery(locationName)
                .build();

        placesClient.findAutocompletePredictions(request).addOnSuccessListener((response) -> {
            if (response.getAutocompletePredictions().isEmpty()) {
                Log.w(TAG, "No predictions found for: " + locationName);
                // Use fallback coordinates for demonstration
                useFallbackCoordinates(locationName, isStart);
                return;
            }

            // Get the place ID from the first prediction
            AutocompletePrediction prediction = response.getAutocompletePredictions().get(0);
            String placeId = prediction.getPlaceId();

            // Fetch the place details
            List<Place.Field> placeFields = Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG);
            FetchPlaceRequest fetchRequest = FetchPlaceRequest.newInstance(placeId, placeFields);

            placesClient.fetchPlace(fetchRequest).addOnSuccessListener((placeResponse) -> {
                Place place = placeResponse.getPlace();
                LatLng latLng = place.getLatLng();

                if (latLng != null) {
                    if (isStart) {
                        startLatLng = latLng;
                    } else {
                        endLatLng = latLng;
                    }

                    addMarkerAndMoveCamera(isStart);

                    // If both locations are geocoded, draw the route
                    if (startLatLng != null && endLatLng != null) {
                        drawRoute();
                    }
                } else {
                    Log.w(TAG, "LatLng is null for place: " + place.getName());
                    // Use fallback coordinates
                    useFallbackCoordinates(locationName, isStart);
                }
            }).addOnFailureListener((exception) -> {
                Log.e(TAG, "Error fetching place details: " + exception.getMessage());
                // Use fallback coordinates
                useFallbackCoordinates(locationName, isStart);
            });
        }).addOnFailureListener((exception) -> {
            Log.e(TAG, "Error finding autocomplete predictions: " + exception.getMessage());
            // Use fallback coordinates
            useFallbackCoordinates(locationName, isStart);
        });
    }

    private void useFallbackCoordinates(String locationName, boolean isStart) {
        Log.d(TAG, "Using fallback coordinates for: " + locationName);

        // This is a simplified example. In a real app, you would use proper geocoding
        // For demonstration, we're using dummy coordinates based on the location name
        if (isStart) {
            // Generate deterministic coordinates based on the string hash
            int hash = Math.abs(locationName.hashCode());
            double lat = 40.0 + (hash % 10) * 0.1;
            double lng = -74.0 + ((hash / 10) % 10) * 0.1;
            startLatLng = new LatLng(lat, lng);
            addMarkerAndMoveCamera(true);
        } else {
            // Generate deterministic coordinates based on the string hash
            int hash = Math.abs(locationName.hashCode());
            double lat = 40.0 + (hash % 10) * 0.1;
            double lng = -73.0 + ((hash / 10) % 10) * 0.1;
            endLatLng = new LatLng(lat, lng);
            addMarkerAndMoveCamera(false);
        }

        // If both coordinates are now available, draw the route
        if (startLatLng != null && endLatLng != null) {
            drawRoute();
        }
    }

    private void addMarkerAndMoveCamera(boolean isStart) {
        if (mMap == null) return;

        if (isStart && startLatLng != null) {
            mMap.addMarker(new MarkerOptions()
                    .position(startLatLng)
                    .title("Start: " + startLocation));

            // If endLatLng is not yet available, move camera to start
            if (endLatLng == null) {
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(startLatLng, 12));
            }
        } else if (!isStart && endLatLng != null) {
            mMap.addMarker(new MarkerOptions()
                    .position(endLatLng)
                    .title("End: " + endLocation));

            // If both points are available, fit the map to show both
            if (startLatLng != null) {
                fitMapToShowMarkers();
            }
        }
    }

    private void fitMapToShowMarkers() {
        if (startLatLng != null && endLatLng != null && mMap != null) {
            try {
                LatLngBounds.Builder builder = new LatLngBounds.Builder();
                builder.include(startLatLng);
                builder.include(endLatLng);

                // Add padding around the bounds
                int padding = 100; // In pixels
                LatLngBounds bounds = builder.build();

                // Animate camera with padding to show the entire route
                mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, padding));

                // Calculate estimated distance and time
                calculateEstimatedTime();
            } catch (Exception e) {
                Log.e(TAG, "Error fitting map to markers: " + e.getMessage());
            }
        }
    }

    private void calculateEstimatedTime() {
        if (startLatLng != null && endLatLng != null) {
            // Calculate Euclidean distance (as the crow flies)
            double lat1 = startLatLng.latitude;
            double lon1 = startLatLng.longitude;
            double lat2 = endLatLng.latitude;
            double lon2 = endLatLng.longitude;

            // Calculate distance in kilometers
            double earthRadius = 6371.0; // in kilometers
            double dLat = Math.toRadians(lat2 - lat1);
            double dLon = Math.toRadians(lon2 - lon1);
            double a = Math.sin(dLat/2) * Math.sin(dLat/2) +
                    Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                            Math.sin(dLon/2) * Math.sin(dLon/2);
            double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
            double distance = earthRadius * c;

            // Estimate time based on average speed of 60 km/h
            double timeInHours = distance / 60.0;
            int hours = (int) timeInHours;
            int minutes = (int) ((timeInHours - hours) * 60);

            String estimatedTime = String.format("%.1f km ≈ %d h %d min",
                    distance, hours, minutes);

            textViewEstimatedTime.setText(estimatedTime);
        }
    }

    private void drawRoute() {
        if (mMap == null || startLatLng == null || endLatLng == null) return;

        // In a real app, you would use the Directions API to get the actual route
        // For this example, we draw a straight line with some intermediate points

        // Calculate a few intermediate points along the straight line
        // This creates a slightly curved path for better visualization
        PolylineOptions polylineOptions = new PolylineOptions()
                .add(startLatLng)
                .width(8)
                .color(Color.parseColor("#4285F4")); // Google Maps blue

        // Add some intermediate points
        double latDiff = endLatLng.latitude - startLatLng.latitude;
        double lngDiff = endLatLng.longitude - startLatLng.longitude;

        for (int i = 1; i <= 8; i++) {
            double fraction = i / 10.0;
            double lat = startLatLng.latitude + (latDiff * fraction);
            double lng = startLatLng.longitude + (lngDiff * fraction);

            // Add a slight curve
            if (i > 1 && i < 8) {
                double offset = Math.sin(fraction * Math.PI) * 0.005;
                lat += offset;
            }

            polylineOptions.add(new LatLng(lat, lng));
        }

        // Add the end point
        polylineOptions.add(endLatLng);

        mMap.addPolyline(polylineOptions);

        // Fit the map to show the entire route
        fitMapToShowMarkers();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Show bottom navigation when leaving this fragment
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).showBottomNav(true);
        }
    }
}