package com.example.carpool.data.database;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "locations")
public class LocationEntity {

    @PrimaryKey
    @NonNull
    private String name;

    private double latitude;
    private double longitude;

    // Used to indicate if the location is frequently accessed
    private boolean favorite;

    // Timestamp when the location was last accessed
    private long lastAccessed;

    public LocationEntity(@NonNull String name, double latitude, double longitude) {
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
        this.favorite = false;
        this.lastAccessed = System.currentTimeMillis();
    }

    @NonNull
    public String getName() {
        return name;
    }

    public void setName(@NonNull String name) {
        this.name = name;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public boolean isFavorite() {
        return favorite;
    }

    public void setFavorite(boolean favorite) {
        this.favorite = favorite;
    }

    public long getLastAccessed() {
        return lastAccessed;
    }

    public void setLastAccessed(long lastAccessed) {
        this.lastAccessed = lastAccessed;
    }

    public void updateLastAccessed() {
        this.lastAccessed = System.currentTimeMillis();
    }
}