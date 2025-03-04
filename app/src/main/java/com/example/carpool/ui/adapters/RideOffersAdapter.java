package com.example.carpool.ui.adapters;

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
import java.util.List;
import java.util.Set;

/**
 * Adapter class for displaying ride offers in a RecyclerView.
 * This adapter now supports a join ride feature:
 * - If the current user is the creator, only edit/delete buttons are shown.
 * - Otherwise, a join button is shown, and it is disabled if a join request is already sent.
 */
public class RideOffersAdapter extends RecyclerView.Adapter<RideOffersAdapter.ViewHolder> {
    private final List<RideOfferResponse> rideOffers;
    private final OnRideOfferActionListener listener;
    private final String currentUserEmail;
    // Set of ride offer IDs for which the join request has already been sent
    private final Set<Long> joinRequestedRideOfferIds;
    
    public interface OnRideOfferActionListener {
        void onEditClick(RideOfferResponse rideOffer);
        void onDeleteClick(RideOfferResponse rideOffer);
        void onJoinClick(RideOfferResponse rideOffer, int position);
    }
    
    public RideOffersAdapter(List<RideOfferResponse> rideOffers, String currentUserEmail, Set<Long> joinRequestedRideOfferIds, OnRideOfferActionListener listener) {
        this.rideOffers = rideOffers;
        this.currentUserEmail = currentUserEmail;
        this.joinRequestedRideOfferIds = joinRequestedRideOfferIds;
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
        holder.departureTime.setText(offer.getDepartureTime().toString());
        holder.availableSeats.setText("Available seats: " + offer.getAvailableSeats());
        
        // Check if the current user is the creator of the ride offer
        if (offer.getCreatorEmail() != null && offer.getCreatorEmail().equals(currentUserEmail)) {
            // Show edit and delete buttons if the current user is the creator; hide join button.
            holder.buttonLayout.setVisibility(View.VISIBLE);
            holder.editButton.setVisibility(View.VISIBLE);
            holder.deleteButton.setVisibility(View.VISIBLE);
            holder.joinButton.setVisibility(View.GONE);
            
            holder.editButton.setOnClickListener(v -> listener.onEditClick(offer));
            holder.deleteButton.setOnClickListener(v -> listener.onDeleteClick(offer));
        } else {
            // For non-creators, hide edit/delete and show join button.
            holder.buttonLayout.setVisibility(View.VISIBLE);
            holder.editButton.setVisibility(View.GONE);
            holder.deleteButton.setVisibility(View.GONE);
            holder.joinButton.setVisibility(View.VISIBLE);
            
            if (joinRequestedRideOfferIds.contains(offer.getId())) {
                holder.joinButton.setText("Request Sent");
                holder.joinButton.setEnabled(false);
            } else {
                holder.joinButton.setText("Join Ride");
                holder.joinButton.setEnabled(true);
                holder.joinButton.setOnClickListener(v -> listener.onJoinClick(offer, position));
            }
        }
    }
    
    @Override
    public int getItemCount() {
        return rideOffers.size();
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
