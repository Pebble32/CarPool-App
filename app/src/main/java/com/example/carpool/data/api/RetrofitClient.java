package com.example.carpool.data.api;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Singleton class to manage Retrofit instance for network operations.
 * 
 * This class provides a single instance of Retrofit configured with a base URL,
 * an OkHttpClient with an in-memory cookie jar, and a Gson converter factory.
 * 
 */
public class RetrofitClient {

    private static final String BASE_URL = "http://10.0.2.2:8088/api/v1/"; // Adjust as needed
    private static Retrofit retrofit = null;
    private static final InMemoryCookieJar cookieJar = new InMemoryCookieJar();

    public static Retrofit getInstance() {
        if (retrofit == null) {
            OkHttpClient client = new OkHttpClient.Builder()
                    .cookieJar(cookieJar)
                    .build();

            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }
}
