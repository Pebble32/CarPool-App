package com.example.carpool.data.models;

import com.google.gson.annotations.SerializedName;

public class RideRequestRequest {
    @SerializedName("rideOfferId")
    private Long rideOfferId;

    public RideRequestRequest(Long rideOfferId) {
        this.rideOfferId = rideOfferId;
    }

    public Long getRideOfferId() {
        return rideOfferId;
    }

    public void setRideOfferId(Long rideOfferId) {
        this.rideOfferId = rideOfferId;
    }
}
