package com.example.carpool.network;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;

/**
 * ApiService interface defines the network API endpoints for the carpool application.
 * It includes methods to interact with the backend services.
 */
public interface ApiService {

    @GET("offers")
    Call<List<Object>> getAllOffers(); 
}