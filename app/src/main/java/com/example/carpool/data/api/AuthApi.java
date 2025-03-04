package com.example.carpool.data.api;

import com.example.carpool.data.models.AuthenticationRequest;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

/**
 * AuthApi interface provides methods for user authentication and registration.
 * It defines endpoints for authenticating users and registering new users.
 */
public interface AuthApi {
    @POST("auth/authenticate")
    Call<ResponseBody> authenticate(@Body AuthenticationRequest request);

    @POST("auth/register")
    Call<ResponseBody> register(@Body com.example.carpool.data.models.RegisterRequest request);
}
