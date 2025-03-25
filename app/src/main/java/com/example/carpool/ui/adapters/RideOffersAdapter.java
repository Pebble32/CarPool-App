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
 * This adapter now supports:
 * - If the current user is the creator: edit/delete/route/viewRequests buttons are shown.
 * - Otherwise: join/route buttons are shown, and join is disabled if a join request is already sent.
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
        void onViewRequestsClick(RideOfferResponse rideOffer);
        void onRouteClick(RideOfferResponse rideOffer);
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
        holder.departureTime.setText(offer.getDepartureTime() != null ? offer.getDepartureTime().toString() : "Not specified");
        holder.availableSeats.setText("Available seats: " + offer.getAvailableSeats());

        // Always show the button layout
        holder.buttonLayout.setVisibility(View.VISIBLE);

        // Check if the current user is the creator of the ride offer
        boolean isCreator = offer.getCreatorEmail() != null && offer.getCreatorEmail().equals(currentUserEmail);

        // Show Route button for everyone
        holder.routeButton.setVisibility(View.VISIBLE);
        holder.routeButton.setOnClickListener(v -> listener.onRouteClick(offer));

        if (isCreator) {
            // Show edit, delete, and viewRequests buttons for the creator
            holder.editButton.setVisibility(View.VISIBLE);
            holder.deleteButton.setVisibility(View.VISIBLE);
            holder.viewRequestsButton.setVisibility(View.VISIBLE);
            holder.joinButton.setVisibility(View.GONE);

            holder.editButton.setOnClickListener(v -> listener.onEditClick(offer));
            holder.deleteButton.setOnClickListener(v -> listener.onDeleteClick(offer));
            holder.viewRequestsButton.setOnClickListener(v -> listener.onViewRequestsClick(offer));
        } else {
            // For non-creators, hide edit/delete/viewRequests and show join
            holder.editButton.setVisibility(View.GONE);
            holder.deleteButton.setVisibility(View.GONE);
            holder.viewRequestsButton.setVisibility(View.GONE);
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
        Button editButton, deleteButton, joinButton, viewRequestsButton, routeButton;
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
            viewRequestsButton = itemView.findViewById(R.id.viewRequestsButton);
            routeButton = itemView.findViewById(R.id.routeButton);
            buttonLayout = itemView.findViewById(R.id.buttonLayout);
        }
    }
}