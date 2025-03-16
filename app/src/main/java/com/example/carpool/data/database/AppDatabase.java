package com.example.carpool.data.database;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Database(entities = {LocationEntity.class}, version = 1, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    private static final String DATABASE_NAME = "carpool_database";

    private static volatile AppDatabase INSTANCE;
    private static final int NUMBER_OF_THREADS = 4;

    public abstract LocationDao locationDao();

    static final ExecutorService databaseWriteExecutor =
            Executors.newFixedThreadPool(NUMBER_OF_THREADS);

    public static AppDatabase getInstance(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    AppDatabase.class, DATABASE_NAME)
                            .addCallback(roomCallback)
                            .build();
                }
            }
        }
        return INSTANCE;
    }

    private static final RoomDatabase.Callback roomCallback = new RoomDatabase.Callback() {
        @Override
        public void onCreate(@NonNull SupportSQLiteDatabase db) {
            super.onCreate(db);

            // Pre-populate the database with Icelandic cities when created
            databaseWriteExecutor.execute(() -> {
                LocationDao dao = INSTANCE.locationDao();

                if (dao.getLocationCount() == 0) {
                    List<LocationEntity> locations = new ArrayList<>();

                    // Add Icelandic cities with their coordinates
                    locations.add(new LocationEntity("Reykjavik", 64.1466, -21.9426));
                    locations.add(new LocationEntity("Akureyri", 65.6835, -18.1002));
                    locations.add(new LocationEntity("Keflavik", 64.0049, -22.5657));
                    locations.add(new LocationEntity("Selfoss", 63.9330, -21.0040));
                    locations.add(new LocationEntity("Egilsstadir", 65.2634, -14.3948));
                    locations.add(new LocationEntity("Isafjordur", 66.0748, -23.1355));
                    locations.add(new LocationEntity("Husavik", 66.0449, -17.3389));
                    locations.add(new LocationEntity("Hofn", 64.2538, -15.2101));
                    locations.add(new LocationEntity("Vestmannaeyjar", 63.4427, -20.2734));

                    dao.insertAll(locations);
                }
            });
        }
    };
}