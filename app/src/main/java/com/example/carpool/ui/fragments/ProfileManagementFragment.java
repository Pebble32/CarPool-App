package com.example.carpool.ui.fragments;

import android.app.Activity;
import android.content.Intent;
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

import java.io.File;
import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileManagementFragment extends Fragment {

    private static final String TAG = "ProfileManagementFrag";
    private static final int REQUEST_CAMERA_PERMISSION = 100;
    private static final int REQUEST_STORAGE_PERMISSION = 101;

    private EditText editTextFirstName, editTextLastName, editTextPhone;
    private Button buttonUpdateProfile, buttonChangePicture;
    private ImageView imageViewProfilePicture;
    private ProgressBar progressBar;

    private File photoFile;
    private Uri photoUri;
    private retrofit2.Retrofit retrofit;

    // Activity result launcher for camera
    private ActivityResultLauncher<Intent> takePictureLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    processCameraImage();
                }
            });

    // Activity result launcher for gallery
    private ActivityResultLauncher<Intent> galleryLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    Uri selectedImageUri = result.getData().getData();
                    processGalleryImage(selectedImageUri);
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

        // Load user profile data
        loadUserProfile();

        // Set click listeners
        buttonUpdateProfile.setOnClickListener(v -> updateUserProfile());
        buttonChangePicture.setOnClickListener(v -> showPictureOptions());

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        ((MainActivity) requireActivity()).showBottomNav(true);
    }

    private void loadUserProfile() {
        progressBar.setVisibility(View.VISIBLE);

        // Load user info
        retrofit.create(com.example.carpool.data.api.UserApi.class).getUserInfo()
                .enqueue(new Callback<com.example.carpool.data.models.UserResponse>() {
                    @Override
                    public void onResponse(Call<com.example.carpool.data.models.UserResponse> call,
                                         Response<com.example.carpool.data.models.UserResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            editTextFirstName.setText(response.body().getFirstName());
                            editTextLastName.setText(response.body().getLastName());
                            editTextPhone.setText(response.body().getPhoneNumber());
                            
                            // Now load profile picture
                            loadProfilePicture();
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

    private void loadProfilePicture() {
        retrofit.create(com.example.carpool.data.api.UserApi.class).getProfilePicture()
                .enqueue(new Callback<String>() {
                    @Override
                    public void onResponse(Call<String> call, Response<String> response) {
                        progressBar.setVisibility(View.GONE);
                        if (response.isSuccessful() && response.body() != null) {
                            try {
                                String base64Image = response.body();
                                byte[] decodedString = Base64.decode(base64Image, Base64.DEFAULT);
                                Bitmap bitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                                imageViewProfilePicture.setImageBitmap(bitmap);
                            } catch (Exception e) {
                                Log.e(TAG, "Error decoding image: " + e.getMessage());
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<String> call, Throwable t) {
                        progressBar.setVisibility(View.GONE);
                        Log.e(TAG, "Error loading profile picture: " + t.getMessage());
                    }
                });
    }

    private void updateUserProfile() {
        String firstName = editTextFirstName.getText().toString().trim();
        String lastName = editTextLastName.getText().toString().trim();
        String phoneNumber = editTextPhone.getText().toString().trim();

        UserInfoChangeRequest request = new UserInfoChangeRequest();
        request.setFirstName(firstName);
        request.setLastName(lastName);
        request.setPhoneNumber(phoneNumber);

        progressBar.setVisibility(View.VISIBLE);
        retrofit.create(com.example.carpool.data.api.UserApi.class).updateUserInfo(request)
                .enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        progressBar.setVisibility(View.GONE);
                        if (response.isSuccessful()) {
                            Toast.makeText(requireContext(), "Profile updated successfully", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(requireContext(), "Failed to update profile: " + response.code(), Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(requireContext(), "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
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
                openGallery();
            } else {
                ProfilePictureUtils.requestStoragePermission(requireActivity(), REQUEST_STORAGE_PERMISSION);
            }
            bottomSheetDialog.dismiss();
        });
        
        bottomSheetDialog.setContentView(sheetView);
        bottomSheetDialog.show();
    }
    
    private void openCamera() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(requireActivity().getPackageManager()) != null) {
            try {
                photoFile = ProfilePictureUtils.createTempImageFile(requireContext());
                photoUri = ProfilePictureUtils.getUriForFile(requireContext(), photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
                takePictureLauncher.launch(takePictureIntent);
            } catch (IOException ex) {
                Toast.makeText(requireContext(), "Error creating image file", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(requireContext(), "No camera app found", Toast.LENGTH_SHORT).show();
        }
    }
    
    private void openGallery() {
        Intent pickPhotoIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        galleryLauncher.launch(pickPhotoIntent);
    }
    
    private void processCameraImage() {
        try {
            Bitmap bitmap = ProfilePictureUtils.uriToBitmap(requireContext(), photoUri);
            if (bitmap != null) {
                Bitmap processedBitmap = ProfilePictureUtils.processBitmap(bitmap);
                imageViewProfilePicture.setImageBitmap(processedBitmap);
                uploadProfilePicture(photoUri);
            }
        } catch (Exception e) {
            Toast.makeText(requireContext(), "Failed to process image", Toast.LENGTH_SHORT).show();
        }
    }
    
    private void processGalleryImage(Uri imageUri) {
        try {
            Bitmap bitmap = ProfilePictureUtils.uriToBitmap(requireContext(), imageUri);
            if (bitmap != null) {
                Bitmap processedBitmap = ProfilePictureUtils.processBitmap(bitmap);
                imageViewProfilePicture.setImageBitmap(processedBitmap);
                uploadProfilePicture(imageUri);
            }
        } catch (Exception e) {
            Toast.makeText(requireContext(), "Failed to process image", Toast.LENGTH_SHORT).show();
        }
    }
    
    private void uploadProfilePicture(Uri imageUri) {
        try {
            // Create a temporary file from the URI
            File file = ProfilePictureUtils.createTempImageFile(requireContext());
            Bitmap bitmap = ProfilePictureUtils.uriToBitmap(requireContext(), imageUri);
            FileOutputStream fos = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, fos);
            fos.close();
            
            // Create request body for file
            RequestBody requestFile = RequestBody.create(MediaType.parse("image/jpeg"), file);
            MultipartBody.Part body = MultipartBody.Part.createFormData("file", file.getName(), requestFile);
            
            progressBar.setVisibility(View.VISIBLE);
            retrofit.create(com.example.carpool.data.api.UserApi.class).uploadProfilePicture(body)
                    .enqueue(new Callback<ResponseBody>() {
                        @Override
                        public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                            progressBar.setVisibility(View.GONE);
                            if (response.isSuccessful()) {
                                Toast.makeText(requireContext(), "Profile picture updated", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(requireContext(), "Failed to upload picture: " + response.code(), Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<ResponseBody> call, Throwable t) {
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(requireContext(), "Error uploading picture: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
            
        } catch (Exception e) {
            Toast.makeText(requireContext(), "Error preparing image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
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
                openGallery();
            } else {
                Toast.makeText(requireContext(), "Storage permission required", Toast.LENGTH_SHORT).show();
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }
}