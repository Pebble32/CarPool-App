package com.example.carpool.ui.fragments;

import android.os.Bundle;
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

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.ResponseBody;
import retrofit2.Call;

public class ProfileFragment extends Fragment {

    private ImageView imageViewProfilePicture;
    private TextView textViewUserName;
    private TextView textViewUserEmail;
    private Button buttonPersonalInformation;
    private Button buttonNotifications;
    private AuthApi authApi;

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

        // Set the user's profile picture, name, and email
        getUserInformation();

        buttonPersonalInformation.setOnClickListener(v -> {
            // Switch to AccountSettingsFragment

        });

        buttonNotifications.setOnClickListener(v -> {
            // Switch to NotificationsFragment
        });

        return view;
    }

    private void getUserInformation() {
        // Fetch user information from the server and set the profile picture, name, and email
        authApi.getUser().enqueue(new retrofit2.Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull retrofit2.Response<ResponseBody> response) {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        String responseBody = response.body().string();
                        // Assuming the response body is a JSON string
                        JSONObject jsonObject = new JSONObject(responseBody);
                        String email = jsonObject.getString("email");
                        String firstName = jsonObject.getString("firstName");

                        // Update the UI with the retrieved user information
                        textViewUserEmail.setText(email);
                        textViewUserName.setText(firstName);
                    } catch (IOException | JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(getContext(), "Failed to parse user information", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getContext(), "Failed to get user information", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                Toast.makeText(getContext(), "Failed to get user information", Toast.LENGTH_SHORT).show();
            }
        });

    }
}