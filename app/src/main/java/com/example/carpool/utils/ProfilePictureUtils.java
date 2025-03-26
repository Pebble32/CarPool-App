package com.example.carpool.utils;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;

import androidx.activity.result.ActivityResultLauncher;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ProfilePictureUtils {
    private static final String TAG = "ProfilePictureUtils";
    private static final int MAX_IMAGE_SIZE = 500; 
    private static final int COMPRESSION_QUALITY = 85; 

    /**
     * Check camera permission
     */
    public static boolean hasCameraPermission(Context context) {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) 
                == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * Check storage permission based on Android version
     */
    public static boolean hasStoragePermission(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return ContextCompat.checkSelfPermission(context, Manifest.permission.READ_MEDIA_IMAGES) 
                    == PackageManager.PERMISSION_GRANTED;
        } else {
            return ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) 
                    == PackageManager.PERMISSION_GRANTED;
        }
    }

    /**
     * Request camera permission
     */
    public static void requestCameraPermission(Activity activity, int requestCode) {
        ActivityCompat.requestPermissions(activity, 
                new String[]{Manifest.permission.CAMERA}, 
                requestCode);
    }

    /**
     * Request storage permission based on Android version
     */
    public static void requestStoragePermission(Activity activity, int requestCode) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(activity, 
                    new String[]{Manifest.permission.READ_MEDIA_IMAGES}, 
                    requestCode);
        } else {
            ActivityCompat.requestPermissions(activity, 
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 
                    requestCode);
        }
    }

    /**
     * Create a temporary file for storing the camera image
     */
    public static File createTempImageFile(Context context) throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        return File.createTempFile(imageFileName, ".jpg", storageDir);
    }

    /**
     * Get URI for a file using FileProvider
     */
    public static Uri getUriForFile(Context context, File file) {
        return FileProvider.getUriForFile(context, 
                "com.example.carpool.fileprovider", 
                file);
    }

    /**
     * Process bitmap (resize and compress)
     */
    public static Bitmap processBitmap(Bitmap originalBitmap) {
        if (originalBitmap == null) return null;

        int width = originalBitmap.getWidth();
        int height = originalBitmap.getHeight();
        float ratio = (float) width / height;

        if (width > MAX_IMAGE_SIZE || height > MAX_IMAGE_SIZE) {
            if (width > height) {
                width = MAX_IMAGE_SIZE;
                height = (int) (width / ratio);
            } else {
                height = MAX_IMAGE_SIZE;
                width = (int) (height * ratio);
            }
            return Bitmap.createScaledBitmap(originalBitmap, width, height, true);
        }
        return originalBitmap;
    }

    /**
     * Convert bitmap to Base64 string
     */
    public static String bitmapToBase64(Bitmap bitmap) {
        if (bitmap == null) return null;
        
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, COMPRESSION_QUALITY, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(byteArray, Base64.DEFAULT);
    }

    /**
     * Convert URI to Bitmap
     */
    public static Bitmap uriToBitmap(Context context, Uri uri) {
        try {
            InputStream inputStream = context.getContentResolver().openInputStream(uri);
            return BitmapFactory.decodeStream(inputStream);
        } catch (Exception e) {
            Log.e(TAG, "Error converting URI to bitmap: " + e.getMessage());
            return null;
        }
    }
}