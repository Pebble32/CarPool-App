package com.example.carpool.data.models;

import java.time.LocalDateTime;


public class EditRideOfferRequest {
    private Long rideId;
    private String startLocation;
    private String endLocation;
    private String departureTime;
    private Integer availableSeats;
    private String rideStatus;

    public EditRideOfferRequest(Long rideId, String startLocation, String endLocation, String departureTime, Integer availableSeats, String rideStatus) {
        this.rideId = rideId;
        this.startLocation = startLocation;
        this.endLocation = endLocation;
        this.departureTime = departureTime;
        this.availableSeats = availableSeats;
        this.rideStatus = rideStatus;
    }

    //Getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long rideId){
        this.rideId = rideId;
    }

    public String getStartLocation() {
        return startLocation;
    }

    public void setStartLocation(String startLocation) {
        this.startLocation = startLocation;
    }

    public String getEndLocation() {
        return endLocation;
    }

    public void setEndLocation(String endLocation) {
        this.endLocation = endLocation;
    }

    public String getDepartureTime() {
        return departureTime;
    }

    public void setDepartureTime(String departureTime) {
        this.departureTime = departureTime;
    }

    public Integer getAvailableSeats() {
        return availableSeats;
    }

    public void setAvailableSeats(Integer availableSeats) {
        this.availableSeats = availableSeats;
    }

    public String getRideStatus() {
        return rideStatus;
    }

    public void setRideStatus(String rideStatus) {
        this.rideStatus = rideStatus;
    }
    
}
