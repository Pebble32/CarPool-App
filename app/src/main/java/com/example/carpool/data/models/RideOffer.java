package com.example.carpool.data.models;

import java.time.LocalDateTime;

public class RideOffer {
    private Long id;
    private String startLocation;
    private String endLocation;
    private Integer availableSeats;
    private LocalDateTime departureTime;
    private String status;
    private String creatorEmail;
    private LocalDateTime lastModified;
    private LocalDateTime createdAt;

    // Constructor
    public RideOffer(Long id, String startLocation, String endLocation, Integer availableSeats,
                     LocalDateTime departureTime, String status, String creatorEmail,
                     LocalDateTime lastModified, LocalDateTime createdAt) {
        this.id = id;
        this.startLocation = startLocation;
        this.endLocation = endLocation;
        this.availableSeats = availableSeats;
        this.departureTime = departureTime;
        this.status = status;
        this.creatorEmail = creatorEmail;
        this.lastModified = lastModified;
        this.createdAt = createdAt;
    }

    // Getters
    public Long getId() { return id; }
    public String getStartLocation() { return startLocation; }
    public String getEndLocation() { return endLocation; }
    public Integer getAvailableSeats() { return availableSeats; }
    public LocalDateTime getDepartureTime() { return departureTime; }
    public String getStatus() { return status; }
    public String getCreatorEmail() { return creatorEmail; }
    public LocalDateTime getLastModified() { return lastModified; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}