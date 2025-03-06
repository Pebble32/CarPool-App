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
import com.example.carpool.data.models.RideStatus;

import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Adapter for displaying ride offers created by the current user in the MyRidesOfferedFragment.
 */
public class MyRidesOfferedAdapter extends RecyclerView.Adapter<MyRidesOfferedAdapter.ViewHolder> {

    private final List<RideOfferResponse> rideOffers;
    private final OnRideOfferActionListener listener;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm");

    public interface OnRideOfferActionListener {
        void onEditClick(RideOfferResponse rideOffer);
        void onDeleteClick(RideOfferResponse rideOffer);
        void onMarkFinishedClick(RideOfferResponse rideOffer);
    }

    public MyRidesOfferedAdapter(List<RideOfferResponse> rideOffers, OnRideOfferActionListener listener) {
        this.rideOffers = rideOffers;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_my_ride_offer, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        RideOfferResponse offer = rideOffers.get(position);
        
        holder.startLocation.setText(String.format("From: %s", offer.getStartLocation()));
        holder.endLocation.setText(String.format("To: %s", offer.getEndLocation()));
        holder.departureTime.setText(String.format("Departure: %s", offer.getDepartureTime().format(formatter)));
        holder.availableSeats.setText(String.format("Available seats: %d", offer.getAvailableSeats()));
        holder.status.setText(String.format("Status: %s", offer.getStatus()));
        
        // Set status color based on ride status
        int statusColor;
        switch (offer.getStatus()) {
            case "AVAILABLE":
                statusColor = android.graphics.Color.parseColor("#4CAF50"); // Green
                break;
            case "UNAVAILABLE":
                statusColor = android.graphics.Color.parseColor("#FF9800"); // Orange
                break;
            case "FINISHED":
                statusColor = android.graphics.Color.parseColor("#2196F3"); // Blue
                break;
            case "CANCELLED":
                statusColor = android.graphics.Color.parseColor("#F44336"); // Red
                break;
            default:
                statusColor = android.graphics.Color.parseColor("#9E9E9E"); // Gray
                break;
        }
        holder.status.setTextColor(statusColor);
        
        // Configure buttons based on ride status
        if (offer.getStatus().equals("AVAILABLE") || offer.getStatus().equals("UNAVAILABLE")) {
            holder.editButton.setVisibility(View.VISIBLE);
            holder.deleteButton.setVisibility(View.VISIBLE);
            holder.markFinishedButton.setVisibility(View.VISIBLE);
            
            holder.editButton.setOnClickListener(v -> listener.onEditClick(offer));
            holder.deleteButton.setOnClickListener(v -> listener.onDeleteClick(offer));
            holder.markFinishedButton.setOnClickListener(v -> listener.onMarkFinishedClick(offer));
        } else {
            // For finished or cancelled rides, hide action buttons
            holder.editButton.setVisibility(View.GONE);
            holder.deleteButton.setVisibility(View.VISIBLE); // Still allow deleting
            holder.markFinishedButton.setVisibility(View.GONE);
            
            holder.deleteButton.setOnClickListener(v -> listener.onDeleteClick(offer));
        }
    }

    @Override
    public int getItemCount() {
        return rideOffers.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView startLocation, endLocation, departureTime, availableSeats, status;
        Button editButton, deleteButton, markFinishedButton;

        public ViewHolder(View itemView) {
            super(itemView);
            startLocation = itemView.findViewById(R.id.textViewStartLocation);
            endLocation = itemView.findViewById(R.id.textViewEndLocation);
            departureTime = itemView.findViewById(R.id.textViewDepartureTime);
            availableSeats = itemView.findViewById(R.id.textViewAvailableSeats);
            status = itemView.findViewById(R.id.textViewStatus);
            editButton = itemView.findViewById(R.id.buttonEdit);
            deleteButton = itemView.findViewById(R.id.buttonDelete);
            markFinishedButton = itemView.findViewById(R.id.buttonMarkFinished);
        }
    }
}