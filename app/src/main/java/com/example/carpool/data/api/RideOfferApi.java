package com.example.carpool.data.api;

import com.example.carpool.data.models.PageResponse;
import com.example.carpool.data.models.RideOfferRequest;
import com.example.carpool.data.models.RideOfferResponse;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface RideOfferApi {
    @GET("offers/all/paginated")
    Call<PageResponse<RideOfferResponse>> getPaginatedOffers(
            @Query("page") int page,
            @Query("size") int size
    );

    @POST("offers/create")
    Call<ResponseBody> createRideOffer(
            @Body RideOfferRequest rideOfferRequest
    );
}
