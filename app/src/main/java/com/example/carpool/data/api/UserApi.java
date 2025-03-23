package com.example.carpool.data.api;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;

/**
 * UserApi interface provides methods for user information and profile picture.
 * It defines endpoints for updating user information.
 */
public interface UserApi {

    @GET("users/profile-picture")
    Call<ResponseBody> getProfilePicture(); // vantar kannski eitthvað inntak, t.d. userinn

    @POST("users/profile-picture")
    Call<ResponseBody> updateProfilePicture(@Body com.example.carpool.data.models.UpdateUserRequest request);

    @POST("users/update")
    Call<ResponseBody> updateUser(@Body com.example.carpool.data.models.UpdateUserRequest request);

    @POST("users/update/password")
    Call<ResponseBody> updatePassword(@Body com.example.carpool.data.models.UpdateUserRequest request);
}
