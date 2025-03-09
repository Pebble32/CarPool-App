package com.example.carpool.data.api;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class DateTypeAdapter implements JsonDeserializer<LocalDateTime> {

    private static final DateTimeFormatter ISO_FORMATTER = DateTimeFormatter.ISO_DATE_TIME;

    @Override
    public LocalDateTime deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        try {
            String dateString = json.getAsString();
            return LocalDateTime.parse(dateString, ISO_FORMATTER);
        } catch (DateTimeParseException e) {
            try {
                return LocalDateTime.parse(json.getAsString());
            } catch (Exception ex) {
                throw new JsonParseException("Cannot parse date: " + json.getAsString(), ex);
            }
        }
    }
}