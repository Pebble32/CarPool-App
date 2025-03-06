package com.example.carpool.ui.adapters;

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
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_my_ride_joined, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        RideRequestResponse request = rideRequests.get(position);
        RideOfferResponse rideOffer = rideOffersMap.get(request.getRideOfferId());
        
        // Set request details
        holder.requestStatus.setText(String.format("Status: %s", request.getRequestStatus()));
        
        // Set status color based on request status
        int statusColor;
        switch (request.getRequestStatus()) {
            case "ACCEPTED":
                statusColor = android.graphics.Color.parseColor("#4CAF50"); // Green
                break;
            case "REJECTED":
                statusColor = android.graphics.Color.parseColor("#F44336"); // Red
                break;
            case "CANCELED":
                statusColor = android.graphics.Color.parseColor("#9E9E9E"); // Gray
                break;
            case "PENDING":
            default:
                statusColor = android.graphics.Color.parseColor("#FF9800"); // Orange
                break;
        }
        holder.requestStatus.setTextColor(statusColor);
        
        holder.requestDate.setText(String.format("Requested on: %s", 
                request.getRequestDate() != null ? request.getRequestDate().format(formatter) : "Unknown"));
        
        // Set ride offer details if available
        if (rideOffer != null) {
            holder.startLocation.setText(String.format("From: %s", rideOffer.getStartLocation()));
            holder.endLocation.setText(String.format("To: %s", rideOffer.getEndLocation()));
            holder.departureTime.setText(String.format("Departure: %s", 
                    rideOffer.getDepartureTime() != null ? rideOffer.getDepartureTime().format(formatter) : "Unknown"));
            holder.rideStatus.setText(String.format("Ride Status: %s", rideOffer.getStatus()));
            holder.driverEmail.setText(String.format("Driver: %s", rideOffer.getCreatorEmail()));
        } else {
            holder.startLocation.setText("From: Loading...");
            holder.endLocation.setText("To: Loading...");
            holder.departureTime.setText("Departure: Loading...");
            holder.rideStatus.setText("Ride Status: Loading...");
            holder.driverEmail.setText("Driver: Loading...");
        }
        
        // Configure cancel button based on request status
        if ("PENDING".equals(request.getRequestStatus())) {
            holder.cancelButton.setVisibility(View.VISIBLE);
            holder.cancelButton.setOnClickListener(v -> listener.onCancelRequestClick(request));
        } else if ("ACCEPTED".equals(request.getRequestStatus())) {
            // For accepted requests, allow cancellation if the ride is not finished
            if (rideOffer != null && !("FINISHED".equals(rideOffer.getStatus()) || "CANCELLED".equals(rideOffer.getStatus()))) {
                holder.cancelButton.setVisibility(View.VISIBLE);
                holder.cancelButton.setText("Cancel Participation");
                holder.cancelButton.setOnClickListener(v -> listener.onCancelRequestClick(request));
            } else {
                holder.cancelButton.setVisibility(View.GONE);
            }
        } else {
            // For rejected, canceled, or other statuses, hide the cancel button
            holder.cancelButton.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return rideRequests.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView startLocation, endLocation, departureTime, rideStatus, requestStatus, requestDate, driverEmail;
        Button cancelButton;

        public ViewHolder(View itemView) {
            super(itemView);
            startLocation = itemView.findViewById(R.id.textViewStartLocation);
            endLocation = itemView.findViewById(R.id.textViewEndLocation);
            departureTime = itemView.findViewById(R.id.textViewDepartureTime);
            rideStatus = itemView.findViewById(R.id.textViewRideStatus);
            requestStatus = itemView.findViewById(R.id.textViewRequestStatus);
            requestDate = itemView.findViewById(R.id.textViewRequestDate);
            driverEmail = itemView.findViewById(R.id.textViewDriverEmail);
            cancelButton = itemView.findViewById(R.id.buttonCancelRequest);
        }
    }
}