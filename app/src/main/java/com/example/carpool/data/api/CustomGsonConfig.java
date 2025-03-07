package com.example.carpool.data.api;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Configuration class for customizing Gson to handle specific data types.
 */
public class CustomGsonConfig {

    /**
     * Creates a customized Gson instance with type adapters for Java 8 date/time classes.
     *
     * @return Configured Gson instance
     */
    public static Gson createGson() {
        return new GsonBuilder()
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeDeserializer())
                .create();
    }

    /**
     * Custom deserializer for LocalDateTime objects.
     * Converts ISO-8601 formatted strings to LocalDateTime objects.
     */
    private static class LocalDateTimeDeserializer implements JsonDeserializer<LocalDateTime> {
        @Override
        public LocalDateTime deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
                throws JsonParseException {
            try {
                return LocalDateTime.parse(json.getAsString(), DateTimeFormatter.ISO_DATE_TIME);
            } catch (Exception e) {
                throw new JsonParseException("Could not parse LocalDateTime: " + json.getAsString(), e);
            }
        }
    }
}