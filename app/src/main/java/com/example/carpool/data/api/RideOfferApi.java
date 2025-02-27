package com.example.carpool.data.api;

import com.example.carpool.data.models.PageResponse;
import com.example.carpool.data.models.RideOfferRequest;
import com.example.carpool.data.models.RideOfferResponse;
import com.example.carpool.data.models.EditRideOfferRequest;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;
import retrofit2.http.PUT;
import retrofit2.http.DELETE;
import retrofit2.http.Path;

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

    @PUT("offers/update")
        Call<RideOfferResponse> updateRideOffer(
                @Body EditRideOfferRequest editRideOfferRequest
        );

    @DELETE("offers/details/{id}")
        Call<ResponseBody> deleteRideOffer(
                @Path("id") Long id
        );

     @GET("offers/details/{id}")
        Call<RideOfferResponse> getRideOfferDetails(
                @Query("id") Long id
        );
     
}
