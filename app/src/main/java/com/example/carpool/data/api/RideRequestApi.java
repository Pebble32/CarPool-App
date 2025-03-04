package com.example.carpool.data.api;

import com.example.carpool.data.models.RideRequestRequest;
import com.example.carpool.data.models.RideRequestResponse;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import java.util.List;

public interface RideRequestApi {

    /**
     * Creates a new ride request (join request) for a given ride offer.
     */
    @POST("ride-requests/create")
    Call<ResponseBody> createRideRequest(@Body RideRequestRequest rideRequestRequest);

    /**
     * Retrieves all ride requests made by the current user.
     */
    @GET("ride-requests/user-requests")
    Call<List<RideRequestResponse>> getUserRideRequests();
}
