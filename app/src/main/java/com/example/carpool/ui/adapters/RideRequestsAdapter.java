package com.example.carpool.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.carpool.R;
import com.example.carpool.data.models.RideRequestResponse;

import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Adapter for displaying ride requests in a RecyclerView.
 */
public class RideRequestsAdapter extends RecyclerView.Adapter<RideRequestsAdapter.ViewHolder> {

    private List<RideRequestResponse> rideRequests;
    private OnRideRequestActionListener listener;

    /**
     * Interface for handling ride request actions.
     */
    public interface OnRideRequestActionListener {
        void onAcceptRequest(RideRequestResponse request);
        void onDeclineRequest(RideRequestResponse request);
    }


    public RideRequestsAdapter(List<RideRequestResponse> rideRequests, OnRideRequestActionListener listener) {
        this.rideRequests = rideRequests;
        this.listener = listener;
    }

    public void setRideRequests(List<RideRequestResponse> rideRequests) {
        this.rideRequests = rideRequests;
        notifyDataSetChanged();
    }
    

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_ride_request, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position){
        RideRequestResponse request = rideRequests.get(position);
        holder.requesterEmail.setText(request.getRequesterEmail());

        if(request.getRequestDate() != null) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM d, yyyy HH:mm");
            holder.requestDate.setText(request.getRequestDate().format(formatter));
        } else {
            holder.requestDate.setText("Date not available");
        }

        holder.requestStatus.setText("Status: " + request.getRequestStatus());

        boolean isPending = "PENDING".equalsIgnoreCase(request.getRequestStatus());
        holder.buttonLayout.setVisibility(isPending ? View.VISIBLE : View.GONE);
        
        holder.acceptButton.setOnClickListener (v -> listener.onAcceptClick(request));
        holder.declineButton.setOnClickListener(v -> listener.onDeclineClick(request));
    }

    @Override
    public int getItemCount() {
        return rideRequests.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView requesterEmail, requestDate, requestStatus;
        Button acceptButton, declineButton;
        View buttonLayout;

        public ViewHolder(View itemView) {
            super(itemView);
            requesterEmail = itemView.findViewById(R.id.textViewRequesterEmail);
            requestDate = itemView.findViewById(R.id.textViewRequestDate);
            requestStatus = itemView.findViewById(R.id.textViewRequestStatus);
            acceptButton = itemView.findViewById(R.id.acceptButton);
            declineButton = itemView.findViewById(R.id.declineButton);
            buttonLayout = itemView.findViewById(R.id.buttonLayout);
        }
    }
}