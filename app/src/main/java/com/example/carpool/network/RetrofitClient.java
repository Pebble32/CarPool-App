package com.example.carpool.network;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * RetrofitClient is a singleton class that provides a Retrofit instance
 * configured with a base URL and a Gson converter factory.
 * 
 * The base URL is set to "http://10.0.2.2:8088/", which is typically used
 * for accessing localhost from an Android emulator.
 * 
 * This class provides a method to get an implementation of the ApiService
 * interface, which can be used to make network requests.
 * 
 * Usage:
 * ApiService apiService = RetrofitClient.getApi();
 */
public class RetrofitClient {
    private static final String BASE_URL = "http://10.0.2.2:8088/"; // localhost for emulator
    private static Retrofit retrofit;

    public static ApiService getApi() {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        }
        return retrofit.create(ApiService.class);
    }
}