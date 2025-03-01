package com.example.carpool.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Button;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.carpool.R;
import com.example.carpool.data.models.RideOfferResponse;

import java.util.List;

/**
 * Adapter class for displaying ride offers in a RecyclerView.
 * This adapter binds ride offer data to the views in each item of the RecyclerView.
 */
public class RideOffersAdapter extends RecyclerView.Adapter<RideOffersAdapter.ViewHolder> {
    private final List<RideOfferResponse> rideOffers;
    private final OnRideOfferActionListener listener;
    private final String currentUserEmail;
    
    public interface OnRideOfferActionListener {
        void onEditClick(RideOfferResponse rideOffer);
        void onDeleteClick(RideOfferResponse rideOffer);
    }
    
    public RideOffersAdapter(List<RideOfferResponse> rideOffers, String currentUserEmail, OnRideOfferActionListener listener) {
        this.rideOffers = rideOffers;
        this.currentUserEmail = currentUserEmail;
        this.listener = listener;
    }
    
    public void addOffers(List<RideOfferResponse> offers) {
        int startPosition = rideOffers.size();
        rideOffers.addAll(offers);
        notifyItemRangeInserted(startPosition, offers.size());
    }
    
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_ride_offer, parent, false);
        return new ViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        RideOfferResponse offer = rideOffers.get(position);
        holder.startLocation.setText(offer.getStartLocation());
        holder.endLocation.setText(offer.getEndLocation());
        holder.departureTime.setText(offer.getDepartureTime());
        holder.availableSeats.setText("Available seats: " + offer.getAvailableSeats());
        
        // Check if the current user is the creator of the ride offer
        if(rideOffers.get(position).getCreatorEmail() != null && 
           rideOffers.get(position).getCreatorEmail().equals(currentUserEmail)) {
            // Show edit and delete buttons if the current user is the creator
            holder.buttonLayout.setVisibility(View.VISIBLE);
            holder.editButton.setOnClickListener(v -> listener.onEditClick(rideOffers.get(position)));
            holder.deleteButton.setOnClickListener(v -> listener.onDeleteClick(rideOffers.get(position)));
        } else {
            // Hide edit and delete buttons if the current user is not the creator
            holder.buttonLayout.setVisibility(View.GONE);
        }
    }
    
    @Override
    public int getItemCount() {
        return rideOffers.size();
    }
    
    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView startLocation, endLocation, departureTime, availableSeats;
        Button editButton, deleteButton;
        LinearLayout buttonLayout;
        
        public ViewHolder(View itemView) {
            super(itemView);
            startLocation = itemView.findViewById(R.id.textViewStartLocation);
            endLocation = itemView.findViewById(R.id.textViewEndLocation);
            departureTime = itemView.findViewById(R.id.textViewDepartureTime);
            availableSeats = itemView.findViewById(R.id.textViewAvailableSeats);
            editButton = itemView.findViewById(R.id.editButton);
            deleteButton = itemView.findViewById(R.id.deleteButton);
            buttonLayout = itemView.findViewById(R.id.buttonLayout);
        }
    }
}