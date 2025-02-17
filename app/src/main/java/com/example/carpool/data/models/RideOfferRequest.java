package com.example.carpool.data.models;

import java.time.LocalDateTime;

public class RideOfferRequest {
    private String startLocation;
    private String endLocation;
    private String departureTime;
    private Integer availableSeats;

    public RideOfferRequest(String startLocation, String endLocation, String departureTime, Integer availableSeats) {
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

    public String getDepartureTime() {
        return departureTime;
    }

    public Integer getAvailableSeats() {
        return availableSeats;
    }
}
