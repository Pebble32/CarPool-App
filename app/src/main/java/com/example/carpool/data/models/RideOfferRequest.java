package com.example.carpool.data.models;

import java.time.LocalDateTime;

public class RideOfferRequest {
    private String startLocation;
    private String endLocation;
    private LocalDateTime departureTime;
    private Integer availableSeats;

    public RideOfferRequest(String startLocation, String endLocation, LocalDateTime departureTime, Integer availableSeats) {
        this.startLocation = startLocation;
        this.endLocation = endLocation;
        this.departureTime = departureTime;
        this.availableSeats = availableSeats;
    }

    public String getStartLocation() {
        return startLocation;
    }

    public String getEndLocation() {
        return endLocation;
    }

    public LocalDateTime getDepartureTime() {
        return departureTime;
    }

    public Integer getAvailableSeats() {
        return availableSeats;
    }
}
