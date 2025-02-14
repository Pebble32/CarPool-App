package com.example.carpool.ui.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.carpool.databinding.ItemRideOfferBinding;
import com.example.carpool.data.models.RideOffer;

import java.time.format.DateTimeFormatter;
import java.util.List;

public class RideOfferAdapter extends RecyclerView.Adapter<RideOfferAdapter.RideOfferViewHolder> {
    private List<RideOffer> rideOffers;
    private static final DateTimeFormatter formatter =
            DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm");

    public RideOfferAdapter(List<RideOffer> rideOffers) {
        this.rideOffers = rideOffers;
    }

    @NonNull
    @Override
    public RideOfferViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemRideOfferBinding binding = ItemRideOfferBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new RideOfferViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull RideOfferViewHolder holder, int position) {
        holder.bind(rideOffers.get(position));
    }

    @Override
    public int getItemCount() {
        return rideOffers.size();
    }

    static class RideOfferViewHolder extends RecyclerView.ViewHolder {
        private final ItemRideOfferBinding binding;

        RideOfferViewHolder(ItemRideOfferBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(RideOffer rideOffer) {
            binding.textLocations.setText(
                    String.format("%s → %s",
                            rideOffer.getStartLocation(),
                            rideOffer.getEndLocation()));

            binding.textDepartureTime.setText(
                    rideOffer.getDepartureTime().format(formatter));

            binding.textStatus.setText(rideOffer.getStatus());

            binding.textAvailableSeats.setText(
                    String.format("Available seats: %d",
                            rideOffer.getAvailableSeats()));

            binding.textCreator.setText(
                    String.format("Posted by: %s",
                            rideOffer.getCreatorEmail()));
        }
    }

    public void updateRideOffers(List<RideOffer> newRideOffers) {
        this.rideOffers = newRideOffers;
        notifyDataSetChanged();
    }
}