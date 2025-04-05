package com.example.carpool.data.database;

import androidx.room.TypeConverter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DateConverters {
    private static final DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    @TypeConverter
    public static LocalDateTime fromTimestamp(String value) {
        return value == null ? null : LocalDateTime.parse(value, formatter);
    }

    @TypeConverter
    public static String dateToTimestamp(LocalDateTime date) {
        return date == null ? null : date.format(formatter);
    }
}
