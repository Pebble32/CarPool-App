package com.example.carpool.ui.fragments;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.carpool.R;
import com.example.carpool.data.database.AppDatabase;
import com.example.carpool.data.database.LocationDao;
import com.example.carpool.data.database.LocationEntity;
import com.google.android.material.appbar.MaterialToolbar;

import org.osmdroid.api.IMapController;
import org.osmdroid.bonuspack.routing.OSRMRoadManager;
import org.osmdroid.bonuspack.routing.Road;
import org.osmdroid.bonuspack.routing.RoadManager;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.BoundingBox;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polyline;

import java.util.ArrayList;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class MapViewFragment extends Fragment {

    private static final String TAG = "MapViewFragment";
    private static final String ARG_START_LOCATION = "start_location";
    private static final String ARG_END_LOCATION = "end_location";

    private MapView mapView;
    private ProgressBar progressBar;
    private TextView textViewRoute, textViewDistance, textViewDuration;
    private String startLocation, endLocation;
    private AppDatabase database;
    private LocationDao locationDao;
    private final Executor executor = Executors.newSingleThreadExecutor();

    public static MapViewFragment newInstance(String startLocation, String endLocation) {
        MapViewFragment fragment = new MapViewFragment();
        Bundle args = new Bundle();
        args.putString(ARG_START_LOCATION, startLocation);
        args.putString(ARG_END_LOCATION, endLocation);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            startLocation = getArguments().getString(ARG_START_LOCATION);
            endLocation = getArguments().getString(ARG_END_LOCATION);
        }

        // Initialize the database
        database = AppDatabase.getInstance(requireContext());
        locationDao = database.locationDao();

        // Load OpenStreetMap configuration
        Context ctx = requireContext();
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx));
        Configuration.getInstance().setUserAgentValue(requireActivity().getPackageName());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_map_view, container, false);

        // Set up the toolbar with back button
        MaterialToolbar toolbar = view.findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> requireActivity().getSupportFragmentManager().popBackStack());
        toolbar.setTitle("Route: " + startLocation + " to " + endLocation);

        // Initialize views
        mapView = view.findViewById(R.id.mapView);
        progressBar = view.findViewById(R.id.progressBar);
        textViewRoute = view.findViewById(R.id.textViewRoute);
        textViewDistance = view.findViewById(R.id.textViewDistance);
        textViewDuration = view.findViewById(R.id.textViewDuration);

        // Set route info
        textViewRoute.setText(startLocation + " to " + endLocation);

        // Configure the map
        mapView.setTileSource(TileSourceFactory.MAPNIK);
        mapView.setMultiTouchControls(true);
        mapView.setBuiltInZoomControls(false);

        // Show loading indicator
        progressBar.setVisibility(View.VISIBLE);

        // Load route
        loadRouteFromDatabase();

        return view;
    }

    private void loadRouteFromDatabase() {
        // This should be done on a background thread to avoid blocking the UI
        executor.execute(() -> {
            try {
                // Get locations from database
                LocationEntity startLocationEntity = locationDao.getLocationByName(startLocation);
                LocationEntity endLocationEntity = locationDao.getLocationByName(endLocation);

                // Update access timestamp
                locationDao.updateLastAccessed(startLocation, System.currentTimeMillis());
                locationDao.updateLastAccessed(endLocation, System.currentTimeMillis());

                // If locations are found in the database, draw the route
                if (startLocationEntity != null && endLocationEntity != null) {
                    GeoPoint startPoint = new GeoPoint(startLocationEntity.getLatitude(), startLocationEntity.getLongitude());
                    GeoPoint endPoint = new GeoPoint(endLocationEntity.getLatitude(), endLocationEntity.getLongitude());

                    // Draw route on UI thread
                    requireActivity().runOnUiThread(() -> drawRoute(startPoint, endPoint));
                } else {
                    // If one or both locations are not found, use default coordinates
                    requireActivity().runOnUiThread(() -> {
                        Toast.makeText(requireContext(), "Using approximate coordinates for missing locations", Toast.LENGTH_SHORT).show();

                        GeoPoint startPoint, endPoint;

                        if (startLocationEntity != null) {
                            startPoint = new GeoPoint(startLocationEntity.getLatitude(), startLocationEntity.getLongitude());
                        } else {
                            // Fallback to default point (Reykjavik)
                            startPoint = getDefaultCoordinates(startLocation);

                            // Save new location to database
                            LocationEntity newLocation = new LocationEntity(
                                    startLocation,
                                    startPoint.getLatitude(),
                                    startPoint.getLongitude()
                            );
                            locationDao.insert(newLocation);
                        }

                        if (endLocationEntity != null) {
                            endPoint = new GeoPoint(endLocationEntity.getLatitude(), endLocationEntity.getLongitude());
                        } else {
                            // Fallback to default point
                            endPoint = getDefaultCoordinates(endLocation);

                            // Save new location to database
                            LocationEntity newLocation = new LocationEntity(
                                    endLocation,
                                    endPoint.getLatitude(),
                                    endPoint.getLongitude()
                            );
                            locationDao.insert(newLocation);
                        }

                        drawRoute(startPoint, endPoint);
                    });
                }
            } catch (Exception e) {
                Log.e(TAG, "Error loading route from database", e);
                requireActivity().runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(requireContext(), "Error loading route: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private void drawRoute(GeoPoint startPoint, GeoPoint endPoint) {
        // Clear existing overlays
        mapView.getOverlays().clear();

        // Add start marker
        Marker startMarker = new Marker(mapView);
        startMarker.setPosition(startPoint);
        startMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        startMarker.setTitle("Start: " + startLocation);
        startMarker.setSnippet("Starting point of your journey");
        startMarker.setIcon(getResources().getDrawable(org.osmdroid.library.R.drawable.osm_ic_follow_me_on));
        mapView.getOverlays().add(startMarker);

        // Add end marker
        Marker endMarker = new Marker(mapView);
        endMarker.setPosition(endPoint);
        endMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        endMarker.setTitle("End: " + endLocation);
        endMarker.setSnippet("Destination of your journey");
        endMarker.setIcon(getResources().getDrawable(R.drawable.ic_destination));
        mapView.getOverlays().add(endMarker);

        // Calculate path between points
        executor.execute(() -> {
            try {
                // For simplicity, we'll draw a straight line
                // In a real app, you would use OSRM or similar for proper routing
                ArrayList<GeoPoint> waypoints = new ArrayList<>();
                waypoints.add(startPoint);
                waypoints.add(endPoint);

                // Use OSRM for routing if available
                try {
                    RoadManager roadManager = new OSRMRoadManager(requireContext(), requireActivity().getPackageName());
                    ((OSRMRoadManager)roadManager).setMean(OSRMRoadManager.MEAN_BY_CAR);

                    Road road = roadManager.getRoad(waypoints);

                    requireActivity().runOnUiThread(() -> {
                        try {
                            if (road != null && road.mStatus == Road.STATUS_OK) {
                                // Draw the route with a proper path
                                Polyline roadOverlay = RoadManager.buildRoadOverlay(road);
                                roadOverlay.setWidth(10);
                                roadOverlay.setColor(Color.BLUE);
                                mapView.getOverlays().add(roadOverlay);

                                // Update distance and duration
                                double distanceInKm = road.mLength;
                                double durationInMin = road.mDuration / 60.0;

                                textViewDistance.setText(String.format("Distance: %.1f km", distanceInKm));
                                textViewDuration.setText(String.format("Estimated time: %.0f minutes", durationInMin));

                                mapView.invalidate();

                                // Zoom to road bounds
                                mapView.zoomToBoundingBox(road.mBoundingBox, true);
                            } else {
                                fallbackToStraightLine(startPoint, endPoint);
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "Error showing route", e);
                            fallbackToStraightLine(startPoint, endPoint);
                        }

                        progressBar.setVisibility(View.GONE);
                    });
                } catch (Exception e) {
                    Log.e(TAG, "Error getting road", e);
                    requireActivity().runOnUiThread(() -> fallbackToStraightLine(startPoint, endPoint));
                }

            } catch (Exception e) {
                Log.e(TAG, "Error drawing route", e);
                requireActivity().runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(requireContext(), "Error drawing route: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void fallbackToStraightLine(GeoPoint startPoint, GeoPoint endPoint) {
        // Draw a straight line if road manager fails
        Polyline line = new Polyline(mapView);
        line.setWidth(10);
        line.setColor(Color.BLUE);

        Paint textPaint = new Paint();
        textPaint.setStyle(Paint.Style.FILL);
        textPaint.setColor(Color.BLACK);
        textPaint.setTextSize(32);
        line.getOutlinePaint().set(textPaint);

        ArrayList<GeoPoint> points = new ArrayList<>();
        points.add(startPoint);
        points.add(endPoint);
        line.setPoints(points);

        mapView.getOverlays().add(line);

        // Calculate distance as crow flies
        float[] results = new float[1];
        android.location.Location.distanceBetween(
                startPoint.getLatitude(), startPoint.getLongitude(),
                endPoint.getLatitude(), endPoint.getLongitude(),
                results);

        float distanceInKm = results[0] / 1000;
        textViewDistance.setText(String.format("Distance: %.1f km (as crow flies)", distanceInKm));
        textViewDuration.setText("Estimated time not available");

        // Zoom to show both points - use direct BoundingBox constructor instead of Builder
        double north = Math.max(startPoint.getLatitude(), endPoint.getLatitude());
        double south = Math.min(startPoint.getLatitude(), endPoint.getLatitude());
        double east = Math.max(startPoint.getLongitude(), endPoint.getLongitude());
        double west = Math.min(startPoint.getLongitude(), endPoint.getLongitude());

        // Add some padding
        double latPadding = (north - south) * 0.1;
        double lonPadding = (east - west) * 0.1;

        BoundingBox boundingBox = new BoundingBox(
                north + latPadding,  // North
                east + lonPadding,   // East
                south - latPadding,  // South
                west - lonPadding    // West
        );

        mapView.invalidate();
        mapView.zoomToBoundingBox(boundingBox, true, 100);

        progressBar.setVisibility(View.GONE);
    }

    private GeoPoint getDefaultCoordinates(String locationName) {
        // Hardcoded coordinates for some Icelandic cities
        // This is a fallback method when the database doesn't have the location
        switch (locationName.toLowerCase()) {
            case "reykjavik":
                return new GeoPoint(64.1466, -21.9426);
            case "akureyri":
                return new GeoPoint(65.6835, -18.1002);
            case "keflavik":
                return new GeoPoint(64.0049, -22.5657);
            case "selfoss":
                return new GeoPoint(63.9330, -21.0040);
            case "egilsstadir":
                return new GeoPoint(65.2634, -14.3948);
            case "isafjordur":
                return new GeoPoint(66.0748, -23.1355);
            case "husavik":
                return new GeoPoint(66.0449, -17.3389);
            case "hofn":
                return new GeoPoint(64.2538, -15.2101);
            case "vestmannaeyjar":
                return new GeoPoint(63.4427, -20.2734);
            default:
                // Default to Reykjavik with a small random offset to create some variety
                double lat = 64.1466 + (Math.random() - 0.5) * 0.1;
                double lng = -21.9426 + (Math.random() - 0.5) * 0.1;
                return new GeoPoint(lat, lng);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }
}