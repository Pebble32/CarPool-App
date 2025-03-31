package com.example.carpool.ui.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.carpool.R;
import com.example.carpool.data.api.AuthApi;
import com.example.carpool.data.api.RetrofitClient;
import com.example.carpool.data.api.UserApi;
import com.example.carpool.data.models.UserResponse;
import com.example.carpool.ui.activities.MainActivity;
import com.example.carpool.ui.utils.ProfilePictureUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

import android.graphics.Bitmap;
import android.util.Base64;

public class ProfileFragment extends Fragment {

    private ImageView imageViewProfilePicture;
    private TextView textViewUserName, textViewUserEmail;
    private Button buttonPersonalInformation;
    private Button buttonNotifications;
    private AuthApi authApi;
    private UserApi userApi;
    private Retrofit retrofit;
    private Bitmap currentProfileBitmap;

    private static final String TAG = "ProfileManagementFragment";
    private static final String PREFS_NAME = "ProfilePrefs";
    private static final String PREF_PROFILE_PICTURE = "ProfilePicture";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        imageViewProfilePicture = view.findViewById(R.id.imageViewProfilePicture);
        textViewUserName = view.findViewById(R.id.textViewUserName);
        textViewUserEmail = view.findViewById(R.id.textViewUserEmail);
        buttonPersonalInformation = view.findViewById(R.id.buttonPersonalInformation);
        buttonNotifications = view.findViewById(R.id.buttonNotifications);

        authApi = RetrofitClient.getInstance().create(AuthApi.class);
        userApi = RetrofitClient.getInstance().create(UserApi.class);
        
        retrofit = RetrofitClient.getInstance();

        // Set the user's profile picture, name, and email
        getUserInformation();
        getProfilePicture();

        buttonPersonalInformation.setOnClickListener(v -> {
            // Switch to AccountSettingsFragment
            AccountSettingsFragment accountSettingsFragment = new AccountSettingsFragment();
            getActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, accountSettingsFragment)
                    .addToBackStack(null)
                    .commit();

        });

        buttonNotifications.setOnClickListener(v -> {
            // Switch to NotificationsFragment
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        ((MainActivity) requireActivity()).showBottomNav(true);
    }

    private void getUserInformation() {
        retrofit.create(com.example.carpool.data.api.UserApi.class).getUserInfo()
                .enqueue(new Callback<UserResponse>() {
                    @Override
                    public void onResponse(Call<com.example.carpool.data.models.UserResponse> call,
                                           Response<UserResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            String firstName = response.body().getFirstName();
                            String lastName = response.body().getLastName();
                            String fullName = firstName + " " + lastName;
                            textViewUserName.setText(fullName);
                            textViewUserEmail.setText(response.body().getEmail());

                        } else {
                            Toast.makeText(requireContext(), "Failed to load profile", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<com.example.carpool.data.models.UserResponse> call, Throwable t) {
                        Toast.makeText(requireContext(), "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

    }

    private void getProfilePicture(){
        // Fetch user profile picture from the server and set the profile picture
        retrofit.create(UserApi.class).getProfilePicture().enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        // If server returns Base64, decode and set
                        Bitmap bitmap = ProfilePictureUtils.base64ToBitmap(response.body());
                        if (bitmap != null) {
                            imageViewProfilePicture.setImageBitmap(bitmap);
                            currentProfileBitmap = bitmap;
                            saveProfilePicture(bitmap);
                        }
                    } catch (Exception e) {
                        // Log error and show toast
                        Log.e(TAG, "Error processing profile picture: " + e.getMessage(), e);
                        Toast.makeText(requireContext(), "Error processing profile picture: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    // Handle unsuccessful response
                    Toast.makeText(requireContext(), "Failed to load profile picture. Server response: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                Log.e(TAG, "Failed to load profile picture", t);
                Toast.makeText(requireContext(), "Error loading profile picture: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveProfilePicture(Bitmap bitmap) {
        if (bitmap != null) {
            String base64Picture = ProfilePictureUtils.bitmapToBase64(bitmap);
            SharedPreferences prefs = requireActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            prefs.edit().putString(PREF_PROFILE_PICTURE, base64Picture).apply();
        }

    }
}