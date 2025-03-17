package com.example.carpool.data.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface LocationDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(LocationEntity location);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<LocationEntity> locations);

    @Update
    void update(LocationEntity location);

    @Query("SELECT * FROM locations WHERE name = :name LIMIT 1")
    LocationEntity getLocationByName(String name);

    @Query("SELECT * FROM locations WHERE name LIKE '%' || :keyword || '%' ORDER BY lastAccessed DESC LIMIT 10")
    List<LocationEntity> searchLocations(String keyword);

    @Query("SELECT * FROM locations WHERE favorite = 1 ORDER BY name ASC")
    List<LocationEntity> getFavoriteLocations();

    @Query("SELECT * FROM locations ORDER BY lastAccessed DESC LIMIT 10")
    List<LocationEntity> getRecentLocations();

    @Query("SELECT COUNT(*) FROM locations")
    int getLocationCount();

    @Query("DELETE FROM locations WHERE favorite = 0 AND lastAccessed < :timestamp")
    void deleteOldLocations(long timestamp);

    @Query("UPDATE locations SET lastAccessed = :timestamp WHERE name = :name")
    void updateLastAccessed(String name, long timestamp);
}