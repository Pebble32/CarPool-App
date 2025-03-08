package com.example.carpool.data.models;

public class RideOfferResponse {
    private Long id;
    private String startLocation;
    private String endLocation;
    private String departureTime; // formatted string
    public String creatorEmail;
    private RideStatus status;
    private Integer availableSeats;

    //Getters
    public Long getId() { return id; }
    public String getStartLocation() { return startLocation; }
    public String getEndLocation() { return endLocation; }
    public String getDepartureTime() { return departureTime; }
    public String getCreatorEmail() { return creatorEmail; }
    public RideStatus getRideStatus() { return status; }
    public Integer getAvailableSeats() { return availableSeats; }

    //Setters
    public void setId(Long id) { this.id = id; }
    public void setStartLocation(String startLocation) { this.startLocation = startLocation; }
    public void setEndLocation(String endLocation) { this.endLocation = endLocation; }
    public void setDepartureTime(String departureTime) { this.departureTime = departureTime; }
    public void setCreatorEmail(String creatorEmail) { this.creatorEmail = creatorEmail; }
    public void setRideStatus(RideStatus status) { this.status = status; }
    public void setAvailableSeats(Integer availableSeats) { this.availableSeats = availableSeats; }

    public String getStatus() {
        return status.toString();
    }
}
