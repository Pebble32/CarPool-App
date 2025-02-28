package com.example.carpool.data.models;

import java.time.LocalDateTime;


public class EditRideOfferRequest {
    private Long rideId;
    private String startLocation;
    private String endLocation;
    private String departureTime; 
    private Integer availableSeats;
    private String rideStatus;  

    public EditRideOfferRequest(Long rideId, String startLocation, String endLocation,
                                String departureTime, Integer availableSeats, String rideStatus) {
        this.rideId = rideId;
        this.startLocation = startLocation;
        this.endLocation = endLocation;
        this.departureTime = departureTime;
        this.availableSeats = availableSeats;
        this.rideStatus = rideStatus;
    }

    //Getters
    public Long getRideId() { return rideId; }
    public String getStartLocation() { return startLocation; }
    public String getEndLocation() { return endLocation; }
    public String getDepartureTime() { return departureTime; }
    public Integer getAvailableSeats() { return availableSeats; }
    public String getRideStatus() { return rideStatus; }
    

}
