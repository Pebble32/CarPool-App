package com.example.carpool.data.api;

import android.util.Log;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.concurrent.TimeUnit;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import retrofit2.Converter;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import java.time.LocalDateTime;

/**
 * Singleton class to manage Retrofit instance for network operations.
 * Enhanced with timeouts, logging, and custom Gson configuration.
 */
public class RetrofitClient {
    private static final String TAG = "RetrofitClient";
    private static final String BASE_URL = "http://10.0.2.2:8088/api/v1/"; 
    private static Retrofit retrofit = null;
    private static final InMemoryCookieJar cookieJar = new InMemoryCookieJar();

    public static Retrofit getInstance() {
        if (retrofit == null) {
            try {
                // Create a custom interceptor for logging
                Interceptor loggingInterceptor = new Interceptor() {
                    @Override
                    public Response intercept(Chain chain) throws IOException {
                        Request request = chain.request();
                        Log.d(TAG, "--> " + request.method() + " " + request.url());
                        Log.d(TAG, "--> END " + request.method());
                        Log.d(TAG, "Sending request to: " + request.url());

                        long startTime = System.currentTimeMillis();
                        Response response;

                        try {
                            response = chain.proceed(request);
                            long duration = System.currentTimeMillis() - startTime;

                            // Log response code
                            Log.d(TAG, "Response code: " + response.code() + " for " + request.url());
                            Log.d(TAG, "<-- " + response.code() + " " + request.url() + " (" + duration + "ms)");

                            // Log headers
                            Log.d(TAG, response.headers().toString());

                            // Log body
                            String responseBody = "";
                            if (response.body() != null) {
                                responseBody = response.peekBody(Long.MAX_VALUE).string();
                                Log.d(TAG, responseBody);
                            }
                            Log.d(TAG, "<-- END HTTP " + (responseBody.length() > 0 ? "(" + responseBody.length() + "-byte body)" : ""));

                            return response;
                        } catch (Exception e) {
                            Log.e(TAG, "Request failed for " + request.url(), e);
                            throw e;
                        }
                    }
                };

                // Build OkHttpClient with timeouts, interceptor and your existing cookieJar
                OkHttpClient client = new OkHttpClient.Builder()
                        .connectTimeout(15, TimeUnit.SECONDS)
                        .readTimeout(30, TimeUnit.SECONDS)
                        .writeTimeout(15, TimeUnit.SECONDS)
                        .addInterceptor(loggingInterceptor)
                        .cookieJar(cookieJar)
                        .build();

                // Create Gson with our DateTypeAdapter
                Gson gson = new GsonBuilder()
                        .registerTypeAdapter(LocalDateTime.class, new DateTypeAdapter())
                        .setLenient()
                        .create();

                // Build Retrofit instance
                retrofit = new Retrofit.Builder()
                        .baseUrl(BASE_URL)
                        .client(client)
                        .addConverterFactory(new StringConverterFactory())
                        .addConverterFactory(GsonConverterFactory.create(gson))
                        .build();

                Log.d(TAG, "Retrofit client initialized with BASE_URL: " + BASE_URL);
            } catch (Exception e) {
                Log.e(TAG, "Failed to initialize Retrofit", e);
                throw new RuntimeException("Failed to initialize RetrofitClient", e);
            }
        }
        return retrofit;
    }
    
    /**
     * Custom converter factory to handle String responses properly
     */
    static class StringConverterFactory extends Converter.Factory {
        @Override
        public Converter<ResponseBody, ?> responseBodyConverter(
                Type type, Annotation[] annotations, Retrofit retrofit) {
            if (type == String.class) {
                return new Converter<ResponseBody, String>() {
                    @Override
                    public String convert(ResponseBody value) throws IOException {
                        return value.string();
                    }
                };
            }
            return null; // Let Gson handle other types
        }
    }
}