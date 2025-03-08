package com.example.carpool.data.api;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import java.time.LocalDateTime;

/**
 * Singleton class to manage Retrofit instance for network operations.
 */
public class RetrofitClient {

    private static final String BASE_URL = "http://10.0.2.2:8088/api/v1/"; // Adjust as needed
    private static Retrofit retrofit = null;
    private static final InMemoryCookieJar cookieJar = new InMemoryCookieJar();

    public static Retrofit getInstance() {
        if (retrofit == null) {
            // Create Gson instance with custom date type adapter
            Gson gson = new GsonBuilder()
                    .registerTypeAdapter(LocalDateTime.class, new DateTypeAdapter())
                    .create();
                    
            OkHttpClient client = new OkHttpClient.Builder()
                    .cookieJar(cookieJar)
                    .build();

            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .build();
        }
        return retrofit;
    }
}