package com.example.carpool.data.models;

import java.time.LocalDateTime;

public class RideRequestResponse {
    private Long id;
    private String requestStatus;
    private Long rideOfferId;
    private String requesterEmail;
    private LocalDateTime requestDate;

    public Long getId() {
        return id;
    }

    public String getRequestStatus() {
        return requestStatus;
    }

    public Long getRideOfferId() {
        return rideOfferId;
    }

    public String getRequesterEmail() {
        return requesterEmail;
    }

    public LocalDateTime getRequestDate() {
        return requestDate;
    }
}
