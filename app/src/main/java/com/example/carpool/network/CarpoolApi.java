package com.example.carpool.network;

import com.example.carpool.models.RideOffer;
import com.example.carpool.models.RideOfferRequest;
import com.example.carpool.models.RideRequestRequest;


import java.util.List;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Body;


public interface CarpoolApi {

    @GET("offers/all")
    Call<List<RideOffer>> getRideOffers();

    @POST("offers/create")
    Call<Long> createRideOffer(@Body RideOfferRequest request);

    @POST("ride-requests/create")
    Call<Long> createRideRequest(@Body RideRequestRequest request);
}
