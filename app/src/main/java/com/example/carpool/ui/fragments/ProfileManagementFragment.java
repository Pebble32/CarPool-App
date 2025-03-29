package com.example.carpool.ui.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;
import android.content.pm.PackageManager;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.carpool.R;
import com.example.carpool.data.api.RetrofitClient;
import com.example.carpool.data.models.UserInfoChangeRequest;
import com.example.carpool.ui.activities.MainActivity;
import com.example.carpool.utils.ProfilePictureUtils;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class ProfileManagementFragment extends Fragment {

    private static final String TAG = "ProfileManagementFragment";
    private static final String PREFS_NAME = "ProfilePrefs";
    private static final String PREF_PROFILE_PICTURE = "ProfilePicture";

    private EditText editTextFirstName, editTextLastName, editTextPhone;
    private Button buttonUpdateProfile, buttonChangePicture;
    private ImageView imageViewProfilePicture;
    private ProgressBar progressBar;

    private Bitmap currentProfileBitmap;
    private Retrofit retrofit;

    private ActivityResultLauncher<Intent> takePictureLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Bundle extras = result.getData().getExtras();
                    currentProfileBitmap = (Bitmap) extras.get("data");
                    imageViewProfilePicture.setImageBitmap(currentProfileBitmap);
                    uploadProfilePicture(currentProfileBitmap);
                }
            });

    private ActivityResultLauncher<Intent> galleryLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    Uri selectedImageUri = result.getData().getData();
                    try {
                        currentProfileBitmap = ProfilePictureUtils.loadBitmapFromUri(requireContext(), selectedImageUri);
                        imageViewProfilePicture.setImageBitmap(currentProfileBitmap);
                        uploadProfilePicture(currentProfileBitmap);
                    } catch (IOException e) {
                        Log.e(TAG, "Error loading image from gallery", e);
                        Toast.makeText(getContext(), "Failed to load image", Toast.LENGTH_SHORT).show();
                    }
                }
            });

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile_management, container, false);

        editTextFirstName = view.findViewById(R.id.editTextFirstName);
        editTextLastName = view.findViewById(R.id.editTextLastName);
        editTextPhone = view.findViewById(R.id.editTextPhone);
        buttonUpdateProfile = view.findViewById(R.id.buttonUpdateProfile);
        buttonChangePicture = view.findViewById(R.id.buttonChangePicture);
        imageViewProfilePicture = view.findViewById(R.id.imageViewProfilePicture);
        progressBar = view.findViewById(R.id.progressBar);

        retrofit = RetrofitClient.getInstance();

        // Load saved profile picture
        loadSavedProfilePicture();

        // Set click listeners
        buttonUpdateProfile.setOnClickListener(v -> updateUserProfile());
        buttonChangePicture.setOnClickListener(v -> showPictureOptions());

        // Load user profile data
        loadUserProfile();

        return view;
    }

    private void loadSavedProfilePicture() {
        SharedPreferences prefs = requireActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String savedProfilePicture = prefs.getString(PREF_PROFILE_PICTURE, null);
        
        if (savedProfilePicture != null) {
            try {
                Bitmap savedBitmap = ProfilePictureUtils.base64ToBitmap(savedProfilePicture);
                if (savedBitmap != null) {
                    imageViewProfilePicture.setImageBitmap(savedBitmap);
                    currentProfileBitmap = savedBitmap;
                }
            } catch (Exception e) {
                Log.e(TAG, "Error loading saved profile picture", e);
            }
        }
    }

    private void saveProfilePicture(Bitmap bitmap) {
        if (bitmap != null) {
            String base64Picture = ProfilePictureUtils.bitmapToBase64(bitmap);
            SharedPreferences prefs = requireActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            prefs.edit().putString(PREF_PROFILE_PICTURE, base64Picture).apply();
        }
    }

    private void uploadProfilePicture(Bitmap bitmap) {
        if (bitmap == null) return;

        progressBar.setVisibility(View.VISIBLE);

        // Resize the bitmap to reduce file size
        Bitmap resizedBitmap = ProfilePictureUtils.resizeBitmap(bitmap, 800, 800);

        // Temporarily save the bitmap
        saveProfilePicture(resizedBitmap);

        // Convert bitmap to file
        try {
            // Save bitmap to a temporary file
            java.io.File tempFile = java.io.File.createTempFile("profile", ".jpg", requireContext().getCacheDir());
            java.io.FileOutputStream fos = new java.io.FileOutputStream(tempFile);
            resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 90, fos);
            fos.close();

            // Create RequestBody
            RequestBody requestFile = RequestBody.create(MediaType.parse("image/jpeg"), tempFile);
            MultipartBody.Part body = MultipartBody.Part.createFormData("file", tempFile.getName(), requestFile);

            // Make API call
            retrofit.create(com.example.carpool.data.api.UserApi.class)
                    .uploadProfilePicture(body)
                    .enqueue(new Callback<ResponseBody>() {
                        @Override
                        public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                            progressBar.setVisibility(View.GONE);
                            if (response.isSuccessful()) {
                                Toast.makeText(getContext(), "Profile picture updated", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(getContext(), "Failed to upload picture", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<ResponseBody> call, Throwable t) {
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(getContext(), "Error uploading picture", Toast.LENGTH_SHORT).show();
                        }
                    });

        } catch (IOException e) {
            Log.e(TAG, "Error preparing image for upload", e);
            Toast.makeText(getContext(), "Failed to prepare image", Toast.LENGTH_SHORT).show();
            progressBar.setVisibility(View.GONE);
        }
    }

    private void showPictureOptions() {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(requireContext());
        View sheetView = getLayoutInflater().inflate(R.layout.bottom_sheet_picture_options, null);
        
        Button btnCamera = sheetView.findViewById(R.id.btnCamera);
        Button btnGallery = sheetView.findViewById(R.id.btnGallery);
        
        btnCamera.setOnClickListener(v -> {
            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            takePictureLauncher.launch(takePictureIntent);
            bottomSheetDialog.dismiss();
        });
        
        btnGallery.setOnClickListener(v -> {
            Intent pickPhotoIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            galleryLauncher.launch(pickPhotoIntent);
            bottomSheetDialog.dismiss();
        });
        
        bottomSheetDialog.setContentView(sheetView);
        bottomSheetDialog.show();
    }

    // Rest of the methods remain the same as in your original implementation
    private void loadUserProfile() {
        progressBar.setVisibility(View.VISIBLE);

        retrofit.create(com.example.carpool.data.api.UserApi.class).getUserInfo()
                .enqueue(new Callback<com.example.carpool.data.models.UserResponse>() {
                    @Override
                    public void onResponse(Call<com.example.carpool.data.models.UserResponse> call,
                                         Response<com.example.carpool.data.models.UserResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            editTextFirstName.setText(response.body().getFirstName());
                            editTextLastName.setText(response.body().getLastName());
                            editTextPhone.setText(response.body().getPhoneNumber());
                            
                            // Load profile picture from the server (Optional - depends on your backend implementation)
                            loadProfilePictureFromServer();
                        } else {
                            Toast.makeText(requireContext(), "Failed to load profile", Toast.LENGTH_SHORT).show();
                            progressBar.setVisibility(View.GONE);
                        }
                    }

                    @Override
                    public void onFailure(Call<com.example.carpool.data.models.UserResponse> call, Throwable t) {
                        Toast.makeText(requireContext(), "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                        progressBar.setVisibility(View.GONE);
                    }
                });
    }

    private void loadProfilePictureFromServer() {
        retrofit.create(com.example.carpool.data.api.UserApi.class).getProfilePicture()
                .enqueue(new Callback<String>() {
                    @Override
                    public void onResponse(Call<String> call, Response<String> response) {
                        progressBar.setVisibility(View.GONE);
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
                Log.e(TAG, "Error decoding profile picture", e);
            }
        }
    }

    @Override
    public void onFailure(Call<String> call, Throwable t) {
        progressBar.setVisibility(View.GONE);
        Log.e(TAG, "Error loading profile picture", t);
    }
});
}

private void updateUserProfile() {
    String firstName = editTextFirstName.getText().toString().trim();
    String lastName = editTextLastName.getText().toString().trim();
    String phoneNumber = editTextPhone.getText().toString().trim();

    UserInfoChangeRequest request = new UserInfoChangeRequest(firstName, lastName, phoneNumber);

    progressBar.setVisibility(View.VISIBLE);
    retrofit.create(com.example.carpool.data.api.UserApi.class).updateUserInfo(request)
            .enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    progressBar.setVisibility(View.GONE);
                    if (response.isSuccessful()) {
                        Toast.makeText(requireContext(), "Profile updated successfully", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(requireContext(), "Failed to update profile", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(requireContext(), "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
}

@Override
public void onResume() {
    super.onResume();
    ((MainActivity) requireActivity()).showBottomNav(true);
}
}