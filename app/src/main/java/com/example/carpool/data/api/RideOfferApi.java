package com.example.carpool.data.api;

import com.example.carpool.data.models.PageResponse;
import com.example.carpool.data.models.RideOfferResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface RideOfferApi {
    @GET("offers/all/paginated")
    Call<PageResponse<RideOfferResponse>> getPaginatedOffers(
            @Query("page") int page,
            @Query("size") int size
    );
}
