package com.example.carpool.data.models;

public class EditRideRequestRequest {
    private Long rideRequestId;
    private String status;

    public EditRideRequestRequest(Long rideRequestId, String status) {
        this.rideRequestId = rideRequestId;
        this.status = status;
    }

    public Long getRideRequestId() {
        return rideRequestId;
    }

    public void setRideRequestId(Long rideRequestId) {
        this.rideRequestId = rideRequestId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}