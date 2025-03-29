package com.example.carpool.utils;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;

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
     * Converts a Base64 string to a Bitmap
     * @param base64String Base64 encoded string
     * @return Bitmap representation of the image
     */
    public static Bitmap base64ToBitmap(String base64String) {
        if (base64String == null || base64String.isEmpty()) return null;
        
        byte[] decodedBytes = Base64.decode(base64String, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
    }

    /**
     * Resize bitmap while maintaining aspect ratio
     * @param bitmap Original bitmap
     * @param maxWidth Maximum width
     * @param maxHeight Maximum height
     * @return Resized bitmap
     */
    public static Bitmap resizeBitmap(Bitmap bitmap, int maxWidth, int maxHeight) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        float ratioBitmap = (float) width / (float) height;
        float ratioMax = (float) maxWidth / (float) maxHeight;

        int finalWidth = maxWidth;
        int finalHeight = maxHeight;
        if (ratioMax > ratioBitmap) {
            finalWidth = (int) (maxHeight * ratioBitmap);
        } else {
            finalHeight = (int) (maxWidth / ratioBitmap);
        }

        Bitmap resizedBitmap = Bitmap.createScaledBitmap(bitmap, finalWidth, finalHeight, true);
        return resizedBitmap;
    }

    /**
     * Load bitmap from URI
     * @param context Context
     * @param uri Image URI
     * @return Bitmap from URI
     * @throws IOException
     */
    public static Bitmap loadBitmapFromUri(Context context, Uri uri) throws IOException {
        InputStream inputStream = context.getContentResolver().openInputStream(uri);
        Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
        
        return bitmap;
    }
}