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
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.carpool.R;
import com.example.carpool.data.api.AuthApi;
import com.example.carpool.data.api.RetrofitClient;
import com.example.carpool.data.api.UserApi;
import com.example.carpool.data.models.GetUserRequest;
import com.example.carpool.data.models.UpdateUserRequest;
import com.example.carpool.ui.activities.MainActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Callback;
import okhttp3.Response;
import okhttp3.ResponseBody;
import retrofit2.Call;

public class AccountSettingsFragment extends Fragment {

    private ImageView imageViewProfilePicture;
    private EditText editTextEmail, editTextPassword, editTextFirstName, editTextLastName, editTextPhone;
    private Button buttonSaveChanges;
    private Button buttonUploadProfilePicture;
    private AuthApi authApi;

    private UserApi userApi;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_account_settings, container, false);
        imageViewProfilePicture = view.findViewById(R.id.imageViewProfilePicture);
        editTextEmail = view.findViewById(R.id.editTextTextEmailAddress);
        editTextPassword = view.findViewById(R.id.editTextPassword);
        editTextFirstName = view.findViewById(R.id.editTextFirstname);
        editTextLastName = view.findViewById(R.id.editTextLastname);
        editTextPhone = view.findViewById(R.id.editTextPhone);
        buttonSaveChanges = view.findViewById(R.id.buttonSaveChanges);
        buttonUploadProfilePicture = view.findViewById(R.id.buttonChangeProfilePicture);
        authApi = RetrofitClient.getInstance().create(AuthApi.class);
        userApi = RetrofitClient.getInstance().create(UserApi.class);

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
        ((MainActivity) requireActivity()).showBottomNav(false);
    }

    /**
     * Method to get the current user's information
     */
    private void getCurrentInformation() {
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
                        String lastName = jsonObject.getString("lastName");
                        String phone = jsonObject.getString("phoneNumber");
                        String Password = jsonObject.getString("password");

                        // Update the UI with the retrieved user information
                        editTextEmail.setText(email);
                        editTextFirstName.setText(firstName);
                        editTextLastName.setText(lastName);
                        editTextPhone.setText(phone);
                        editTextPassword.setText(Password);
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

    /**
     * Method to save changes to the user's information
     */
    private void saveChanges() {
        // collect user input from the EditText fields (they can be empty)
        String email = editTextEmail.getText().toString();
        String password = editTextPassword.getText().toString();
        String firstName = editTextFirstName.getText().toString();
        String lastName = editTextLastName.getText().toString();
        String phone = editTextPhone.getText().toString();

        // Create a UpdateUserRequest object with the collected data
        UpdateUserRequest request = new UpdateUserRequest(email, password, firstName, lastName, phone);

        // Make an API call to update the user's information
        userApi.updateUser(request).enqueue(new retrofit2.Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull retrofit2.Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(getContext(), "Changes saved successfully!", Toast.LENGTH_SHORT).show();
                    // Navigate back to the ProfileFragment by popping the back stack
                    if (getActivity() != null) {
                        getActivity().getSupportFragmentManager().popBackStack();
                    }
                } else {
                    Toast.makeText(getContext(), "Failed to save changes", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                Toast.makeText(getContext(), "Failed to save changes", Toast.LENGTH_SHORT).show();
            }
        });

    }

    /**
     * Method to upload a new profile picture
     */
    private void uploadProfilePicture() {

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
