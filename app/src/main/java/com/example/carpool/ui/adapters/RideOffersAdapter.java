package com.example.carpool.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.carpool.R;
import com.example.carpool.data.models.RideOfferResponse;
import java.util.List;

public class RideOffersAdapter extends RecyclerView.Adapter<RideOffersAdapter.ViewHolder> {

    private final List<RideOfferResponse> rideOffers;

    public RideOffersAdapter(List<RideOfferResponse> rideOffers) {
        this.rideOffers = rideOffers;
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
        holder.textViewStartLocation.setText(offer.getStartLocation());
        holder.textViewEndLocation.setText(offer.getEndLocation());
        holder.textViewDepartureTime.setText(offer.getDepartureTime());
    }

    @Override
    public int getItemCount() {
        return rideOffers.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textViewStartLocation, textViewEndLocation, textViewDepartureTime;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewStartLocation = itemView.findViewById(R.id.textViewStartLocation);
            textViewEndLocation = itemView.findViewById(R.id.textViewEndLocation);
            textViewDepartureTime = itemView.findViewById(R.id.textViewDepartureTime);
        }
    }
}
