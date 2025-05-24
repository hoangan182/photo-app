package com.example.photobooth.controllers;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class ImagePickerController {

    public static final int REQUEST_CODE_PICK_IMAGE = 1001;
    public static final int REQUEST_CODE_PERMISSION = 1002;

    private final Activity activity;
    private final ImagePickerCallback callback;

    public ImagePickerController(Activity activity, ImagePickerCallback callback) {
        this.activity = activity;
        this.callback = callback;
    }

    // Kiểm tra quyền
    public boolean hasPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return ContextCompat.checkSelfPermission(activity, Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED;
        } else {
            return ContextCompat.checkSelfPermission(activity, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
        }
    }

    // Yêu cầu quyền
    public void requestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.READ_MEDIA_IMAGES}, REQUEST_CODE_PERMISSION);
        } else {
            ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_CODE_PERMISSION);
        }
    }

    // Mở gallery chọn ảnh
    public void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        activity.startActivityForResult(intent, REQUEST_CODE_PICK_IMAGE);
    }

    // Xử lý kết quả chọn ảnh
    public void handleActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == REQUEST_CODE_PICK_IMAGE && resultCode == Activity.RESULT_OK && data != null) {
            Uri selectedImageUri = data.getData();
            if (selectedImageUri != null) {
                callback.onImagePicked(selectedImageUri);
            }
        }
    }

    // Xử lý kết quả cấp quyền
    public void handleRequestPermissionsResult(int requestCode, int[] grantResults) {
        if (requestCode == REQUEST_CODE_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openGallery();
            } else {
                callback.onPermissionDenied();
            }
        }
    }

    // Interface callback để trả về kết quả cho Activity
    public interface ImagePickerCallback {
        void onImagePicked(Uri imageUri);

        void onPermissionDenied();
    }
}
