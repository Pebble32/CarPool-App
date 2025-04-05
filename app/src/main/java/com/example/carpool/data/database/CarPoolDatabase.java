package com.example.carpool.data.database;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import android.content.Context;

/**
 * CarPoolDatabase is the Room database that stores ride offer entities.
 * It uses a destructive migration strategy for schema changes.
 */
@Database(entities = {RideOfferEntity.class}, version = 2, exportSchema = false)
@TypeConverters({DateConverters.class})
public abstract class CarPoolDatabase extends RoomDatabase {
    public abstract RideOfferDao rideOfferDao();

    private static volatile CarPoolDatabase INSTANCE;

    /**
     * Returns the singleton instance of CarPoolDatabase.
     *
     * @param context The application context.
     * @return The CarPoolDatabase instance.
     */
    public static CarPoolDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (CarPoolDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    CarPoolDatabase.class, "carpool_database")
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
