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


/**
 * RideOfferApi interface provides methods for creating, updating, and deleting ride offers.
 * It also provides methods for retrieving a paginated list of ride offers and
 * for retrieving the details of a specific ride offer.
 */
public interface RideOfferApi {


/**
 * Retrieves a paginated list of ride offers.
 *
 * @param page the page number to retrieve
 * @param size the number of items per page
 * @return a Call object to send the request
 */
    @GET("offers/all/paginated")
    Call<PageResponse<RideOfferResponse>> getPaginatedOffers(
            @Query("page") int page,
            @Query("size") int size
    );

/**
 * Creates a new ride offer.
 *
 * @param rideOfferRequest the request body containing the details of the ride offer to be created
 * @return a Call object with a ResponseBody that contains the server's response
 */
    @POST("offers/create")
    Call<ResponseBody> createRideOffer(
            @Body RideOfferRequest rideOfferRequest
    );

/**
 * Updates the details of an existing ride offer.
 *
 * @param editRideOfferRequest the request body containing the updated ride offer details
 * @return a Call object with a RideOfferResponse indicating the result of the update operation
 */
    @PUT("offers/details")
        Call<RideOfferResponse> updateRideOffer(
                @Body EditRideOfferRequest editRideOfferRequest
        );

/**
 * Deletes a ride offer with the specified ID.
 *
 * @param id The ID of the ride offer to be deleted.
 * @return A Call object to execute the HTTP request.
 */
    @DELETE("offers/details/{id}")
        Call<ResponseBody> deleteRideOffer(
                @Path("id") Long id
        );

/**
 * Retrieves the details of a ride offer based on the provided ID.
 *
 * @param id The unique identifier of the ride offer.
 * @return A Call object that can be used to request the ride offer details.
 */
     @GET("offers/details/{id}")
        Call<RideOfferResponse> getRideOfferDetails(
                @Query("id") Long id
        );
     
}
