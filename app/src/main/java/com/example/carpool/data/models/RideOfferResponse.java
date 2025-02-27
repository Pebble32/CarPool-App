package com.example.carpool.data.models;

public class RideOfferResponse {
    private Long id;
    private String startLocation;
    private String endLocation;
    private String departureTime; // formatted string

    public Long getId() { return id; }
    public String getStartLocation() { return startLocation; }
    public String getEndLocation() { return endLocation; }
    public String getDepartureTime() { return departureTime; }
}
