package com.example.carpool.ui.fragments;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.carpool.R;
import com.example.carpool.data.api.AuthApi;
import com.example.carpool.data.api.RetrofitClient;
import com.example.carpool.data.api.UserApi;
import com.example.carpool.data.models.PasswordChangeRequest;
import com.example.carpool.data.models.UpdateUserRequest;
import com.example.carpool.data.models.UserResponse;
import com.example.carpool.ui.activities.MainActivity;
import com.example.carpool.ui.utils.ProfilePictureUtils;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class AccountSettingsFragment extends Fragment {
    private static final String TAG = "ProfileManagementFragment";
    private static final String PREFS_NAME = "ProfilePrefs";
    private static final String PREF_PROFILE_PICTURE = "ProfilePicture";
    private static final int REQUEST_CAMERA_PERMISSION = 100;
    private static final int REQUEST_STORAGE_PERMISSION = 101;

    private ImageView imageViewProfilePicture;
    private EditText  editTextFirstName, editTextLastName, editTextPhone, editTextOldPassword, editTextNewPassword;
    private TextView textView, textView2, textView3, textView4, textView5, textView6, textView7;
    private Button buttonSaveChanges;
    private Button buttonUploadProfilePicture;

    private Bitmap currentProfileBitmap;
    private Retrofit retrofit;
    private Uri photoUri;

    private ActivityResultLauncher<Intent> takePictureLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    try {
                        if (photoUri != null) {
                            // Load the full image from the file
                            currentProfileBitmap = ProfilePictureUtils.loadBitmapFromUri(requireContext(), photoUri);
                            // Process the bitmap (resize if needed)
                            currentProfileBitmap = ProfilePictureUtils.processBitmap(currentProfileBitmap);
                            // Set the image
                            imageViewProfilePicture.setImageBitmap(currentProfileBitmap);
                            // Upload the processed image
                            uploadProfilePicture(currentProfileBitmap);
                        } else {
                            // Fallback to thumbnail if URI is null
                            Bundle extras = result.getData().getExtras();
                            if (extras != null && extras.containsKey("data")) {
                                currentProfileBitmap = (Bitmap) extras.get("data");
                                imageViewProfilePicture.setImageBitmap(currentProfileBitmap);
                                uploadProfilePicture(currentProfileBitmap);
                            }
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error processing camera image", e);
                        Toast.makeText(getContext(), "Failed to process image", Toast.LENGTH_SHORT).show();
                    }
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
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_account_settings, container, false);
        imageViewProfilePicture = view.findViewById(R.id.imageViewProfilePicture);
        editTextOldPassword = view.findViewById(R.id.editTextOldPassword);
        editTextNewPassword = view.findViewById(R.id.editTextNewPassword);
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
        // Load saved profile picture
        loadSavedProfilePicture();

        buttonSaveChanges.setOnClickListener(v -> saveChanges());
        buttonUploadProfilePicture.setOnClickListener(v -> showPictureOptions());

        return view;
    }


    @Override
    public void onResume(){
        super.onResume();
        if (getActivity() instanceof MainActivity){
            MainActivity mainActivity = (MainActivity) getActivity();
            mainActivity.showBottomNav(true);
            mainActivity.uncheckBottomNav();
        }
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
        String firstName = editTextFirstName.getText().toString().trim();
        String lastName = editTextLastName.getText().toString().trim();
        String phoneNumber = editTextPhone.getText().toString().trim();

        UpdateUserRequest request = new UpdateUserRequest(firstName, lastName, phoneNumber);

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

        String oldPassword = editTextOldPassword.getText().toString().trim();
        String newPassword = editTextNewPassword.getText().toString().trim();

        // If the user has entered a new password, update the password
        if (!newPassword.isEmpty() && !oldPassword.isEmpty()) {
            PasswordChangeRequest passwordRequest = new PasswordChangeRequest(oldPassword, newPassword);
            retrofit.create(com.example.carpool.data.api.UserApi.class).updatePassword(passwordRequest)
                    .enqueue(new Callback<ResponseBody>() {
                        @Override
                        public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                            if (response.isSuccessful()) {
                                Toast.makeText(requireContext(), "Password updated successfully", Toast.LENGTH_SHORT).show();
                            } else {
                                try {
                                    JSONObject error = new JSONObject(response.errorBody().string());
                                    Toast.makeText(requireContext(), error.getString("message"), Toast.LENGTH_SHORT).show();
                                } catch (IOException | JSONException e) {
                                    Toast.makeText(requireContext(), "Failed to update password", Toast.LENGTH_SHORT).show();
                                }
                            }
                        }

                        @Override
                        public void onFailure(Call<ResponseBody> call, Throwable t) {
                            Toast.makeText(requireContext(), "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        }

    }

    /**
     * Method to load the user's saved profile picture
     */
    private void loadSavedProfilePicture() {
        /*SharedPreferences prefs = requireActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
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
        }*/

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

    private void uploadProfilePicture(Bitmap bitmap) {
        if (bitmap == null) return;

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
                            if (response.isSuccessful()) {
                                Toast.makeText(getContext(), "Profile picture updated", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(getContext(), "Failed to upload picture", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<ResponseBody> call, Throwable t) {
                            Toast.makeText(getContext(), "Error uploading picture", Toast.LENGTH_SHORT).show();
                        }
                    });

        } catch (IOException e) {
            Log.e(TAG, "Error preparing image for upload", e);
            Toast.makeText(getContext(), "Failed to prepare image", Toast.LENGTH_SHORT).show();
        }
    }

    private void showPictureOptions() {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(requireContext());
        View sheetView = getLayoutInflater().inflate(R.layout.bottom_sheet_picture_options, null);

        Button btnCamera = sheetView.findViewById(R.id.btnCamera);
        Button btnGallery = sheetView.findViewById(R.id.btnGallery);

        btnCamera.setOnClickListener(v -> {
            if (ProfilePictureUtils.hasCameraPermission(requireContext())) {
                openCamera();
            } else {
                ProfilePictureUtils.requestCameraPermission(requireActivity(), REQUEST_CAMERA_PERMISSION);
            }
            bottomSheetDialog.dismiss();
        });

        btnGallery.setOnClickListener(v -> {
            if (ProfilePictureUtils.hasStoragePermission(requireContext())) {
                Intent pickPhotoIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                galleryLauncher.launch(pickPhotoIntent);
            } else {
                ProfilePictureUtils.requestStoragePermission(requireActivity(), REQUEST_STORAGE_PERMISSION);
            }
            bottomSheetDialog.dismiss();
        });

        bottomSheetDialog.setContentView(sheetView);
        bottomSheetDialog.show();
    }

    private void openCamera() {
        try {
            // Create a file to save the image
            File photoFile = ProfilePictureUtils.createTempImageFile(requireContext());
            
            // Get a content URI for the file using FileProvider
            photoUri = ProfilePictureUtils.getUriForFile(requireContext(), photoFile);
            
            // Create camera intent
            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
            
            // Grant write permission to the camera app
            takePictureIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            
            // Launch the camera
            takePictureLauncher.launch(takePictureIntent);
        } catch (IOException e) {
            Log.e(TAG, "Error creating image file", e);
            Toast.makeText(requireContext(), "Failed to create image file", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openCamera();
            } else {
                Toast.makeText(requireContext(), "Camera permission required", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == REQUEST_STORAGE_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Intent pickPhotoIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                galleryLauncher.launch(pickPhotoIntent);
            } else {
                Toast.makeText(requireContext(), "Storage permission required", Toast.LENGTH_SHORT).show();
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

}
