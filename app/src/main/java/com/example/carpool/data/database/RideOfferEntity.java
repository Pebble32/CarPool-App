package com.example.carpool.data.database;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;
import java.time.LocalDateTime;

@Entity(tableName = "ride_offers")
@TypeConverters(DateConverters.class)
public class RideOfferEntity {
    @PrimaryKey
    private Long id;
    private String startLocation;
    private String endLocation;
    private Integer availableSeats;
    private LocalDateTime departureTime;
    private String status;
    private String creatorEmail;
    private LocalDateTime lastModified;
    private LocalDateTime createdAt;

    public RideOfferEntity(Long id, String startLocation, String endLocation, Integer availableSeats,
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

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getStartLocation() { return startLocation; }
    public void setStartLocation(String startLocation) { this.startLocation = startLocation; }
    public String getEndLocation() { return endLocation; }
    public void setEndLocation(String endLocation) { this.endLocation = endLocation; }
    public Integer getAvailableSeats() { return availableSeats; }
    public void setAvailableSeats(Integer availableSeats) { this.availableSeats = availableSeats; }
    public LocalDateTime getDepartureTime() { return departureTime; }
    public void setDepartureTime(LocalDateTime departureTime) { this.departureTime = departureTime; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getCreatorEmail() { return creatorEmail; }
    public void setCreatorEmail(String creatorEmail) { this.creatorEmail = creatorEmail; }
    public LocalDateTime getLastModified() { return lastModified; }
    public void setLastModified(LocalDateTime lastModified) { this.lastModified = lastModified; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
