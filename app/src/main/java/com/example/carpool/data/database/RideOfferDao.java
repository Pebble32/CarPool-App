package com.example.carpool.data.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import java.util.List;

@Dao
public interface RideOfferDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertRideOffers(List<RideOfferEntity> offers);

    @Query("SELECT * FROM ride_offers")
    List<RideOfferEntity> getAllRideOffers();

    @Query("DELETE FROM ride_offers")
    void deleteAllRideOffers();
}
