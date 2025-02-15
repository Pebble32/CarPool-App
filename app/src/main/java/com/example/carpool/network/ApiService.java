package com.example.carpool.network;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;

public interface ApiService {

    @GET("offers")
    Call<List<Object>> getAllOffers(); 
}