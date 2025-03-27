package com.example.carpool.ui.fragments;

import android.graphics.BitmapFactory;
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
import com.example.carpool.data.api.UserApi;
import com.example.carpool.ui.activities.MainActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.ResponseBody;
import retrofit2.Call;
import android.graphics.Bitmap;
import android.util.Base64;

public class ProfileFragment extends Fragment {

    private ImageView imageViewProfilePicture;
    private TextView textViewUserName, textViewUserEmail;
    private Button buttonPersonalInformation;
    private Button buttonNotifications;
    private AuthApi authApi;
    private UserApi userApi;

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

        // Set the user's profile picture, name, and email
        //getUserInformation();
        //getProfilePicture();

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
        // Fetch user information from the server and set the profile picture, name, and email
        authApi.getUser().enqueue(new retrofit2.Callback<>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull retrofit2.Response<ResponseBody> response) {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        String responseBody = response.body().string();
                        // Assuming the response body is a JSON string
                        JSONObject jsonObject = new JSONObject(responseBody);
                        String email = jsonObject.getString("email");
                        String firstName = jsonObject.getString("firstName");
                        String lastName = jsonObject.getString("lastName");
                        String fullName = firstName + " " + lastName;

                        // Update the UI with the retrieved user information
                        textViewUserEmail.setText(email);
                        textViewUserName.setText(fullName);
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

    private void getProfilePicture(){
        // Fetch user profile picture from the server and set the profile picture
        userApi.getProfilePicture().enqueue(new retrofit2.Callback<>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull retrofit2.Response<ResponseBody> response) {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        String profilePictureString = response.body().string();
                        byte[] decodedString = Base64.decode(profilePictureString, Base64.DEFAULT);
                        Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                        imageViewProfilePicture.setImageBitmap(decodedByte); // veit ekki hvort þetta virki
                    } catch (IOException e) {
                        e.printStackTrace();
                        Toast.makeText(getContext(), "Failed to decode profile picture", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getContext(), "Failed to get profile picture", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                Toast.makeText(getContext(), "Failed to get profile picture", Toast.LENGTH_SHORT).show();
            }
        });
    }
}