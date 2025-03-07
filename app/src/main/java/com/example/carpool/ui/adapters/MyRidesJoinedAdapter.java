package com.example.carpool.ui.adapters;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.carpool.R;
import com.example.carpool.data.models.RideOfferResponse;
import com.example.carpool.data.models.RideRequestResponse;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * Adapter for displaying ride requests made by the current user in the MyRidesJoinedFragment.
 */
public class MyRidesJoinedAdapter extends RecyclerView.Adapter<MyRidesJoinedAdapter.ViewHolder> {

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
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_ride_offer, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        RideRequestResponse request = rideRequests.get(position);
        RideOfferResponse rideOffer = rideOffersMap.get(request.getRideOfferId());

        // Display request ID and status at the top
        String requestStatus = request.getRequestStatus();
        StringBuilder statusText = new StringBuilder("Request: ");
        statusText.append(requestStatus);

        // Set the status color
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

        // Set ride offer details if available
        if (rideOffer != null) {
            holder.startLocation.setText(rideOffer.getStartLocation());
            holder.endLocation.setText(rideOffer.getEndLocation());

            if (rideOffer.getDepartureTime() != null) {
                holder.departureTime.setText("Departure: " + rideOffer.getDepartureTime().format(String.valueOf(formatter)));
            } else {
                holder.departureTime.setText("Departure: Not specified");
            }

            // Add ride status to available seats field
            holder.availableSeats.setText(statusText.toString() + " • Ride: " + rideOffer.getStatus());
        } else {
            holder.startLocation.setText("Request #" + request.getId());
            holder.endLocation.setText(statusText.toString());

            if (request.getRequestDate() != null) {
                holder.departureTime.setText("Requested on: " + request.getRequestDate().format(formatter));
            } else {
                holder.departureTime.setText("Request date unknown");
            }

            holder.availableSeats.setText("Loading ride details...");
        }

        // Show buttons layout
        holder.buttonLayout.setVisibility(View.VISIBLE);

        // Hide edit and delete buttons for ride requests
        holder.editButton.setVisibility(View.GONE);
        holder.deleteButton.setVisibility(View.GONE);

        // Configure join button as "Cancel Request"
        if (holder.joinButton != null) {
            if ("PENDING".equals(requestStatus) || "ACCEPTED".equals(requestStatus)) {
                holder.joinButton.setText("Cancel Request");
                holder.joinButton.setVisibility(View.VISIBLE);
                holder.joinButton.setOnClickListener(v -> listener.onCancelRequestClick(request));
            } else {
                holder.joinButton.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public int getItemCount() {
        return rideRequests.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView startLocation, endLocation, departureTime, availableSeats;
        Button editButton, deleteButton, joinButton;
        LinearLayout buttonLayout;

        public ViewHolder(View itemView) {
            super(itemView);
            startLocation = itemView.findViewById(R.id.textViewStartLocation);
            endLocation = itemView.findViewById(R.id.textViewEndLocation);
            departureTime = itemView.findViewById(R.id.textViewDepartureTime);
            availableSeats = itemView.findViewById(R.id.textViewAvailableSeats);
            editButton = itemView.findViewById(R.id.editButton);
            deleteButton = itemView.findViewById(R.id.deleteButton);
            joinButton = itemView.findViewById(R.id.joinButton);
            buttonLayout = itemView.findViewById(R.id.buttonLayout);
        }
    }
}