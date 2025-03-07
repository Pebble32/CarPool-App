package com.example.carpool.data.models;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class RideRequestResponse {
    private Long id;
    private String requestStatus;
    private Long rideOfferId;
    private String requesterEmail;
    private LocalDateTime requestDate;

    // Static formatter for consistent date formatting
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm");

    // Getters
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

    /**
     * Format the request date as a user-friendly string
     *
     * @return Formatted date string or empty string if date is null
     */
    public String getFormattedRequestDate() {
        if (requestDate == null) {
            return "";
        }
        return requestDate.format(formatter);
    }

    // Setters
    public void setId(Long id) {
        this.id = id;
    }

    public void setRequestStatus(String requestStatus) {
        this.requestStatus = requestStatus;
    }

    public void setRideOfferId(Long rideOfferId) {
        this.rideOfferId = rideOfferId;
    }

    public void setRequesterEmail(String requesterEmail) {
        this.requesterEmail = requesterEmail;
    }

    public void setRequestDate(LocalDateTime requestDate) {
        this.requestDate = requestDate;
    }
}