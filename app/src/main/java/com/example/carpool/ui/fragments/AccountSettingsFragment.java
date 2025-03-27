package com.example.carpool.ui.fragments;


import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
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
import com.example.carpool.data.models.UpdateUserRequest;
import com.example.carpool.data.models.UserResponse;
import com.example.carpool.ui.activities.MainActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class AccountSettingsFragment extends Fragment {

    private ImageView imageViewProfilePicture;
    private EditText  editTextPassword, editTextFirstName, editTextLastName, editTextPhone;
    private TextView textView, textView2, textView3, textView4, textView5, textView6, textView7;
    private Button buttonSaveChanges;
    private Button buttonUploadProfilePicture;
    private Retrofit retrofit;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_account_settings, container, false);
        imageViewProfilePicture = view.findViewById(R.id.imageViewProfilePicture);
        editTextPassword = view.findViewById(R.id.editTextPassword);
        editTextFirstName = view.findViewById(R.id.editTextFirstname);
        editTextLastName = view.findViewById(R.id.editTextLastname);
        editTextPhone = view.findViewById(R.id.editTextPhone);
        textView = view.findViewById(R.id.textView);
        textView2 = view.findViewById(R.id.textView2);
        textView3 = view.findViewById(R.id.textView3);
        textView4 = view.findViewById(R.id.textView4);
        textView6 = view.findViewById(R.id.textView6);
        textView7 = view.findViewById(R.id.textView7);
        buttonSaveChanges = view.findViewById(R.id.buttonSaveChanges);
        buttonUploadProfilePicture = view.findViewById(R.id.buttonChangeProfilePicture);

        retrofit = RetrofitClient.getInstance();

        // set text in the EditText fields to the user's current information
        getCurrentInformation();
        // set the user's profile picture
        getProfilePicture();

        buttonSaveChanges.setOnClickListener(v -> saveChanges());
        buttonUploadProfilePicture.setOnClickListener(v -> uploadProfilePicture());

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        ((MainActivity) requireActivity()).showBottomNav(true);
    }

    /**
     * Method to get the current user's information
     */
    private void getCurrentInformation() {
        retrofit.create(com.example.carpool.data.api.UserApi.class).getUserInfo()
                .enqueue(new Callback<UserResponse>() {
                    @Override
                    public void onResponse(Call<com.example.carpool.data.models.UserResponse> call,
                                           Response<UserResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            editTextFirstName.setText(response.body().getFirstName());
                            editTextLastName.setText(response.body().getLastName());
                            editTextPhone.setText(response.body().getPhoneNumber());

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

    /**
     * Method to save changes to the user's information
     */
    private void saveChanges() {
        // collect user input from the EditText fields (they can be empty)
        String firstName = editTextFirstName.getText().toString().trim();
        String lastName = editTextLastName.getText().toString().trim();
        String phone = editTextPhone.getText().toString().trim();

        // Create a UpdateUserRequest object with the collected data
        UpdateUserRequest request = new UpdateUserRequest(firstName, lastName, phone);

        // Make an API call to update the user's information
        retrofit.create(com.example.carpool.data.api.UserApi.class).updateUser(request)
                .enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        if (response.isSuccessful()) {
                            Toast.makeText(requireContext(), "Profile updated successfully", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(requireContext(), "Failed to update profile", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                        Toast.makeText(requireContext(), "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

    }

    /**
     * Method to upload a new profile picture
     */
    private void uploadProfilePicture() {
        // implemented by dorian
    }

    private void getProfilePicture() {
        // method implemented by Dorian
    }

}
