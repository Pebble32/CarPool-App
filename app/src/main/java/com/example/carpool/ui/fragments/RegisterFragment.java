package com.example.carpool.ui.fragments;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.carpool.R;
import com.example.carpool.data.api.AuthApi;
import com.example.carpool.data.api.RetrofitClient;
import com.example.carpool.data.models.RegisterRequest;
import com.example.carpool.ui.activities.MainActivity;
import com.example.carpool.utils.ProfilePictureUtils;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.io.File;
import java.io.IOException;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * RegisterFragment handles user registration, including input fields for user details,
 * profile picture selection from camera or gallery, and registration via API call.
 * 
 * Features:
 * - Input fields for email, password, first name, last name, phone number, and profile picture.
 * - Buttons to register and select a profile picture.
 * - Displays selected profile picture.
 * - Handles camera and storage permissions.
 * - Converts images to Base64 format.
 * 
 * Methods:
 * - onCreateView: Initializes UI components.
 * - onResume: Hides bottom navigation bar.
 * - showPictureOptions: Displays options to select a picture.
 * - openCamera: Launches camera intent.
 * - openGallery: Launches gallery intent.
 * - processCameraImage: Processes and displays camera image.
 * - processGalleryImage: Processes and displays gallery image.
 * - onRequestPermissionsResult: Handles permission results.
 * - registerUser: Validates input and registers user via API.
 */
public class RegisterFragment extends Fragment {

    private static final int REQUEST_CAMERA_PERMISSION = 100;
    private static final int REQUEST_STORAGE_PERMISSION = 101;

    private EditText editTextEmail, editTextPassword, editTextFirstName, editTextLastName, editTextPhone, editTextProfilePicture;
    private Button buttonRegister, buttonSelectPicture;
    private ImageView imageViewProfilePicture;
    private AuthApi authApi;
    
    private File photoFile;
    private Uri photoUri;
    private String base64ProfilePicture;

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
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_register, container, false);
        editTextEmail = view.findViewById(R.id.editTextEmail);
        editTextPassword = view.findViewById(R.id.editTextPassword);
        editTextFirstName = view.findViewById(R.id.editTextFirstName);
        editTextLastName = view.findViewById(R.id.editTextLastName);
        editTextPhone = view.findViewById(R.id.editTextPhone);
        editTextProfilePicture = view.findViewById(R.id.editTextProfilePicture);
        buttonRegister = view.findViewById(R.id.buttonRegister);
        
        // New components for profile picture
        buttonSelectPicture = view.findViewById(R.id.buttonSelectPicture);
        imageViewProfilePicture = view.findViewById(R.id.imageViewProfilePicture);
        
        authApi = RetrofitClient.getInstance().create(AuthApi.class);

        buttonRegister.setOnClickListener(v -> registerUser());
        
        // Set up profile picture button
        buttonSelectPicture.setOnClickListener(v -> showPictureOptions());
        
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        ((MainActivity) requireActivity()).showBottomNav(false);
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
                base64ProfilePicture = ProfilePictureUtils.bitmapToBase64(processedBitmap);
                // Make the image view visible
                imageViewProfilePicture.setVisibility(View.VISIBLE);
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
                base64ProfilePicture = ProfilePictureUtils.bitmapToBase64(processedBitmap);
                // Make the image view visible
                imageViewProfilePicture.setVisibility(View.VISIBLE);
            }
        } catch (Exception e) {
            Toast.makeText(requireContext(), "Failed to process image", Toast.LENGTH_SHORT).show();
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

    private void registerUser() {
        // Collect user input from EditText fields
        String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();
        String firstname = editTextFirstName.getText().toString().trim();
        String lastname = editTextLastName.getText().toString().trim();
        String phoneNumber = editTextPhone.getText().toString().trim();

        // Validate that all required fields are filled
        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password) || TextUtils.isEmpty(firstname)
                || TextUtils.isEmpty(lastname) || TextUtils.isEmpty(phoneNumber)) {
            Toast.makeText(getContext(), "Please fill in all required fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create a RegisterRequest object with the collected data
        RegisterRequest request = new RegisterRequest(email, password, firstname, lastname, phoneNumber, base64ProfilePicture);
        
        // Make an API call to register the user
        authApi.register(request).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(getContext(), "Registration successful!", Toast.LENGTH_SHORT).show();
                    // Navigate back to the LoginFragment by popping the back stack
                    if (getActivity() != null) {
                        getActivity().getSupportFragmentManager().popBackStack();
                    }
                } else {
                    Toast.makeText(getContext(), "Registration failed: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Toast.makeText(getContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}