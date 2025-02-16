package com.example.carpool.models;

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
    /**
     * Represents a ride offer in the carpool application.
     *
     * @param id The unique identifier for the ride offer.
     * @param startLocation The starting location of the ride.
     * @param endLocation The destination location of the ride.
     * @param availableSeats The number of available seats in the ride.
     * @param departureTime The departure time of the ride.
     * @param status The current status of the ride offer.
     * @param creatorEmail The email of the user who created the ride offer.
     * @param lastModified The timestamp of the last modification to the ride offer.
     * @param createdAt The timestamp when the ride offer was created.
     */
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