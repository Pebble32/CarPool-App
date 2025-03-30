package com.example.carpool.data.api;

import com.example.carpool.data.models.PasswordChangeRequest;
import com.example.carpool.data.models.UpdateUserRequest;
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
    @PUT("users/profile-picture")
    Call<ResponseBody> uploadProfilePicture(@Part MultipartBody.Part file);

    @PUT("users/update")
    Call<ResponseBody> updateUser(@Body UpdateUserRequest request);

    @GET("auth/get-user")
    Call<UserResponse> getUserInfo();

    @PUT("users/update/password")
    Call<ResponseBody> updatePassword(@Body PasswordChangeRequest request);
}
