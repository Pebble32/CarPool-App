package com.example.carpool.data.api;

import com.example.carpool.data.models.EditRideRequestRequest;
import com.example.carpool.data.models.RideRequestRequest;
import com.example.carpool.data.models.RideRequestResponse;
import com.example.carpool.data.models.AnswerRideRequestRequest;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;
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

    /**
     * Retrieves all ride requests for a specific ride offer.
     * This is used by the driver to view requests for their ride offer.
     */
    @GET("ride-requests/requests")
    Call<List<RideRequestResponse>> getRideRequestsForRideOffer(@Query("rideOfferId") Long rideOfferId);
    
    /**
     * Answers a ride request by accepting or rejecting it.
     */
    @PUT("ride-requests/answer")
    Call<RideRequestResponse> answerRideRequest(@Body AnswerRideRequestRequest request);
    
    /**
     * Edits the status of a ride request.
     * This can be used to cancel a ride request by setting status to CANCELED.
     */
    @PUT("ride-requests/edit-request")
    Call<RideRequestResponse> editRideRequestStatus(@Body EditRideRequestRequest editRequest);
    
    /**
     * Deletes a ride request.
     */
    @DELETE("ride-requests/delete-request/{id}")
    Call<ResponseBody> deleteRideRequest(@Path("id") Long id);
}