package com.example.carpool.models;

public class RideOfferRequest {
    private String startLocation;
    private String endLocation;
    private int availableSeats;
    private String departureTime;

    public RideOfferRequest(String startLocation, String endLocation, int availableSeats, String departureTime) {
        this.startLocation = startLocation;
        this.endLocation = endLocation;
        this.availableSeats = availableSeats;
        this.departureTime = departureTime;
    }

    // Getters and setters
    public String getStartLocation() { return startLocation; }
    public String getEndLocation() { return endLocation; }
    public int getAvailableSeats() { return availableSeats; }
    public String getDepartureTime() { return departureTime; }

    public void setStartLocation(String startLocation) { this.startLocation = startLocation; }
    public void setEndLocation(String endLocation) { this.endLocation = endLocation; }
    public void setAvailableSeats(int availableSeats) { this.availableSeats = availableSeats; }
    public void setDepartureTime(String departureTime) { this.departureTime = departureTime; }
}