package com.example.carpool.ui.adapters;

import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.carpool.R;
import com.example.carpool.data.models.RideOfferResponse;
import com.example.carpool.data.models.RideRequestResponse;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;

/**
 * Adapter for displaying ride requests made by the current user in the MyRidesJoinedFragment.
 */
public class MyRidesJoinedAdapter extends RecyclerView.Adapter<MyRidesJoinedAdapter.ViewHolder> {
    private static final String TAG = "MyRidesJoinedAdapter";
    private final List<RideRequestResponse> rideRequests;
    private final Map<Long, RideOfferResponse> rideOffersMap;
    private final OnRideRequestActionListener listener;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm");

    public interface OnRideRequestActionListener {
        void onCancelRequestClick(RideRequestResponse rideRequest);
    }

    public MyRidesJoinedAdapter(List<RideRequestResponse> rideRequests, Map<Long, RideOfferResponse> rideOffersMap, OnRideRequestActionListener listener) {
        this.rideRequests = rideRequests;
        this.rideOffersMap = rideOffersMap;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Note: Using item_my_ride_joined.xml instead of item_ride_offer.xml
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_my_ride_joined, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        RideRequestResponse request = rideRequests.get(position);
        RideOfferResponse rideOffer = rideOffersMap.get(request.getRideOfferId());

        try {
            // Set request status
            String requestStatus = request.getRequestStatus();
            holder.textViewRequestStatus.setText("Status: " + requestStatus);

            // Set status color
            int statusColor;
            switch (requestStatus) {
                case "ACCEPTED":
                    statusColor = Color.parseColor("#4CAF50"); // Green
                    break;
                case "REJECTED":
                    statusColor = Color.parseColor("#F44336"); // Red
                    break;
                case "CANCELED":
                    statusColor = Color.parseColor("#9E9E9E"); // Gray
                    break;
                case "PENDING":
                default:
                    statusColor = Color.parseColor("#FF9800"); // Orange
                    break;
            }
            holder.textViewRequestStatus.setTextColor(statusColor);

            // Set ride offer details if available
            if (rideOffer != null) {
                Log.d(TAG, "Binding ride offer: " + rideOffer.getId() + ", Start: " + rideOffer.getStartLocation());

                holder.textViewStartLocation.setText("From: " + rideOffer.getStartLocation());
                holder.textViewEndLocation.setText("To: " + rideOffer.getEndLocation());

                if (rideOffer.getCreatorEmail() != null) {
                    holder.textViewDriverEmail.setText("Driver: " + rideOffer.getCreatorEmail());
                } else {
                    holder.textViewDriverEmail.setVisibility(View.GONE);
                }

                holder.textViewRideStatus.setText("Ride Status: " + rideOffer.getStatus());

                if (rideOffer.getDepartureTime() != null) {
                    try {
                        String formattedDate;
                        String departureTimeStr = rideOffer.getDepartureTime();

                        // Check if we need to parse it as a string or it's already formatted
                        if (departureTimeStr.contains("T")) {
                            // ISO format string like "2023-05-20T14:30:00"
                            LocalDateTime dateTime = LocalDateTime.parse(departureTimeStr);
                            formattedDate = dateTime.format(formatter);
                        } else {
                            // Already formatted or unknown format
                            formattedDate = departureTimeStr;
                        }

                        holder.textViewDepartureTime.setText("Departure: " + formattedDate);
                    } catch (DateTimeParseException e) {
                        Log.e(TAG, "Date parsing error: " + e.getMessage());
                        holder.textViewDepartureTime.setText("Departure: " + rideOffer.getDepartureTime());
                    }
                } else {
                    holder.textViewDepartureTime.setText("Departure: Not specified");
                }
            } else {
                // No ride offer details available, display placeholder
                Log.d(TAG, "No ride offer details for request ID: " + request.getId());
                holder.textViewStartLocation.setText("Request #" + request.getId());
                holder.textViewEndLocation.setText("Ride offer details unavailable");
                holder.textViewDriverEmail.setVisibility(View.GONE);
                holder.textViewRideStatus.setVisibility(View.GONE);
                holder.textViewDepartureTime.setVisibility(View.GONE);
            }

            // Set request date
            if (request.getRequestDate() != null) {
                holder.textViewRequestDate.setText("Requested on: " + request.getRequestDate().format(formatter));
            } else {
                holder.textViewRequestDate.setVisibility(View.GONE);
            }

            // Configure cancel button visibility based on status
            if ("PENDING".equals(requestStatus) || "ACCEPTED".equals(requestStatus)) {
                holder.buttonCancelRequest.setVisibility(View.VISIBLE);
                holder.buttonCancelRequest.setOnClickListener(v -> listener.onCancelRequestClick(request));
            } else {
                holder.buttonCancelRequest.setVisibility(View.GONE);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error binding view holder at position " + position, e);
        }
    }

    @Override
    public int getItemCount() {
        return rideRequests.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textViewRequestStatus, textViewStartLocation, textViewEndLocation,
                textViewDepartureTime, textViewRideStatus, textViewDriverEmail,
                textViewRequestDate;
        Button buttonCancelRequest;

        public ViewHolder(View itemView) {
            super(itemView);
            textViewRequestStatus = itemView.findViewById(R.id.textViewRequestStatus);
            textViewStartLocation = itemView.findViewById(R.id.textViewStartLocation);
            textViewEndLocation = itemView.findViewById(R.id.textViewEndLocation);
            textViewDepartureTime = itemView.findViewById(R.id.textViewDepartureTime);
            textViewRideStatus = itemView.findViewById(R.id.textViewRideStatus);
            textViewDriverEmail = itemView.findViewById(R.id.textViewDriverEmail);
            textViewRequestDate = itemView.findViewById(R.id.textViewRequestDate);
            buttonCancelRequest = itemView.findViewById(R.id.buttonCancelRequest);

            // Add null checks for views
            if (textViewRequestStatus == null) Log.e(TAG, "textViewRequestStatus view not found");
            if (textViewStartLocation == null) Log.e(TAG, "textViewStartLocation view not found");
            if (textViewEndLocation == null) Log.e(TAG, "textViewEndLocation view not found");
            if (textViewDepartureTime == null) Log.e(TAG, "textViewDepartureTime view not found");
            if (buttonCancelRequest == null) Log.e(TAG, "buttonCancelRequest view not found");
        }
    }
}