package com.example.carpool.data.api;

import com.example.carpool.data.models.AuthenticationRequest;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface AuthApi {
    // This should /api/v1/auth/X
    @POST("auth/authenticate")
    Call<ResponseBody> authenticate(@Body AuthenticationRequest request);

    @POST("auth/register")
    Call<ResponseBody> register(@Body com.example.carpool.data.model.RegisterRequest request);
}
