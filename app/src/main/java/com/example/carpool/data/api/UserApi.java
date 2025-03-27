package com.example.carpool.data.api;

import com.example.carpool.data.models.UserResponse;

import okhttp3.MultipartBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;

/**
 * UserApi interface provides methods for user information and profile picture.
 * It defines endpoints for updating user information.
 */
public interface UserApi {

    @GET("users/profile-picture")
    Call<String> getProfilePicture();

    @Multipart
    @POST("users/profile-picture")
    Call<ResponseBody> updateProfilePicture(@Part MultipartBody.Part file);

    @Multipart
    @PUT("users/profile-picture")
    Call<ResponseBody> changeProfilePicture(@Part MultipartBody.Part file);

    @POST("users/update")
    Call<ResponseBody> updateUser(@Body com.example.carpool.data.models.UpdateUserRequest request);

    @GET("auth/get-user")
    Call<UserResponse> getUserInfo();

    @POST("users/update/password")
    Call<ResponseBody> updatePassword(@Body com.example.carpool.data.models.UpdateUserRequest request);
}
