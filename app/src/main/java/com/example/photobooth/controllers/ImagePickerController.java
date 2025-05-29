package com.example.photobooth.controllers;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;

import androidx.activity.ComponentActivity;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class ImagePickerController {
    private static final int PERMISSION_REQUEST_CODE = 100;
    private final ComponentActivity activity;
    private final ImagePickerCallback callback;
    private ActivityResultLauncher<Intent> imagePickerLauncher;
    private ActivityResultLauncher<String[]> permissionLauncher;

    public interface ImagePickerCallback {
        void onImagePicked(Uri imageUri);
        void onPermissionDenied();
    }

    public ImagePickerController(ComponentActivity activity, ImagePickerCallback callback) {
        this.activity = activity;
        this.callback = callback;
        setupImagePickerLauncher();
        setupPermissionLauncher();
    }

    private void setupImagePickerLauncher() {
        imagePickerLauncher = activity.registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    Uri selectedImageUri = result.getData().getData();
                    if (selectedImageUri != null) {
                        callback.onImagePicked(selectedImageUri);
                    }
                }
            }
        );
    }

    private void setupPermissionLauncher() {
        permissionLauncher = activity.registerForActivityResult(
            new ActivityResultContracts.RequestMultiplePermissions(),
            permissions -> {
                boolean allGranted = true;
                for (Boolean isGranted : permissions.values()) {
                    if (!isGranted) {
                        allGranted = false;
                        break;
                    }
                }
                if (allGranted) {
                    openGallery();
                } else {
                    callback.onPermissionDenied();
                }
            }
        );
    }

    public boolean hasPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return ContextCompat.checkSelfPermission(activity, Manifest.permission.READ_MEDIA_IMAGES)
                    == PackageManager.PERMISSION_GRANTED;
        } else {
            return ContextCompat.checkSelfPermission(activity, Manifest.permission.READ_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED;
        }
    }

    public void requestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissionLauncher.launch(new String[]{Manifest.permission.READ_MEDIA_IMAGES});
        } else {
            permissionLauncher.launch(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE});
        }
    }

    public void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        imagePickerLauncher.launch(intent);
    }
}
