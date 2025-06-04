package com.example.photobooth.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.EditText;
import android.widget.ProgressBar;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.ActionMode;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.photobooth.R;
import com.example.photobooth.adapter.FirestoreImageAdapter;
import com.example.photobooth.controllers.ImagePickerController;
import com.example.photobooth.controllers.ImageStorageController;
import com.example.photobooth.controllers.TrashController;
import com.example.photobooth.controllers.FavoriteController;
import com.example.photobooth.controllers.AlbumController;
import com.example.photobooth.models.Image;
import com.example.photobooth.models.Album;
import com.example.photobooth.services.FirestoreImageService;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.example.photobooth.services.ImageUploadService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class AllPhotoActivity extends AppCompatActivity implements ImagePickerController.ImagePickerCallback {

    private static final String TAG = "AllPhotoActivity";
    private List<Image> images;
    private ImagePickerController imagePickerController;
    private ImageStorageController imageStorageController;
    private TrashController trashController;
    private FavoriteController favoriteController;
    private AlbumController albumController;
    private FirestoreImageAdapter imageAdapter;
    private FirestoreImageService imageService;
    private android.view.ActionMode actionMode;
    private View topBarLayout;
    private ProgressBar progressBar;
    private AlertDialog loadingDialog;

    private GridView gridView;
    private ImageView imgShowOption;
    private ConstraintLayout noAllImageLayout;
    private TextView txtBackAllPhoto;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_all_photo);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize collections
        images = new ArrayList<>();

        // Initialize views
        initializeViews();

        // Initialize controllers and services
        imagePickerController = new ImagePickerController(this, this);
        imageStorageController = new ImageStorageController(this);
        trashController = new TrashController(this);
        favoriteController = new FavoriteController(this);
        albumController = new AlbumController(this);
        imageService = new FirestoreImageService();

        // Set click listeners
        setupClickListeners();

        // Load images from Firestore
        loadImagesFromFirestore();
    }

    private void initializeViews() {
        gridView = findViewById(R.id.gridView);
        noAllImageLayout = findViewById(R.id.noAllImageLayout);
        imgShowOption = findViewById(R.id.imgShowOptionAllPhoto);
        txtBackAllPhoto = findViewById(R.id.txtBackAllPhoto);
        topBarLayout = findViewById(R.id.topBarLayout);
        progressBar = findViewById(R.id.progressBar);

        // Set initial visibility
        if (gridView != null) {
            gridView.setVisibility(View.VISIBLE);
        }
        if (noAllImageLayout != null) {
            noAllImageLayout.setVisibility(View.GONE);
        }
    }

    private void setupClickListeners() {
        if (imgShowOption != null) {
            imgShowOption.setOnClickListener(view -> showAddOptions());
        }

        if (txtBackAllPhoto != null) {
            txtBackAllPhoto.setOnClickListener(v -> finish());
        }
    }

    private void loadImagesFromFirestore() {
        Log.d(TAG, "Loading images from Firestore");
        showLoading(true);
        
        imageService.getImages(new FirestoreImageService.ImageCallback() {
            @Override
            public void onSuccess(List<Image> loadedImages) {
                Log.d(TAG, "Successfully loaded " + loadedImages.size() + " images");
                images.clear();
                images.addAll(loadedImages);
                runOnUiThread(() -> {
                    updateGridView();
                    showLoading(false);
                });
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "Error loading images: " + error);
                runOnUiThread(() -> {
                    showError(error);
                    showLoading(false);
                });
            }
        });
    }

    private void updateGridView() {
        if (gridView == null || noAllImageLayout == null) {
            Log.e(TAG, "Views not initialized");
            return;
        }

        try {
            Log.d(TAG, "Updating GridView with " + images.size() + " images");
            if (images.isEmpty()) {
                Log.d(TAG, "No images to display");
                noAllImageLayout.setVisibility(View.VISIBLE);
                gridView.setVisibility(View.GONE);
            } else {
                Log.d(TAG, "Displaying images");
                noAllImageLayout.setVisibility(View.GONE);
                gridView.setVisibility(View.VISIBLE);
                
                // Create new adapter
                imageAdapter = new FirestoreImageAdapter(this, images);
                gridView.setAdapter(imageAdapter);
                
                // Force refresh
                gridView.invalidateViews();
                gridView.requestLayout();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error updating grid view", e);
            Toast.makeText(this, "Error updating display", Toast.LENGTH_SHORT).show();
        }
    }

    private void showLoading(boolean isLoading) {
        if (progressBar != null) {
            progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        }
        if (gridView != null) {
            gridView.setVisibility(isLoading ? View.GONE : View.VISIBLE);
        }
        if (noAllImageLayout != null) {
            noAllImageLayout.setVisibility(View.GONE);
        }
    }

    private void showError(String error) {
        if (noAllImageLayout != null) {
            noAllImageLayout.setVisibility(View.VISIBLE);
        }
        if (gridView != null) {
            gridView.setVisibility(View.GONE);
        }
        Toast.makeText(this, error, Toast.LENGTH_LONG).show();
    }

    private void showAddOptions() {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);
        View view = getLayoutInflater().inflate(R.layout.bottom_sheet_add_options, null);
        bottomSheetDialog.setContentView(view);

        TextView createSubject = view.findViewById(R.id.optionAddImage);
        TextView createFolder = view.findViewById(R.id.optionAddAlbum);

        createSubject.setOnClickListener(v -> {
            bottomSheetDialog.dismiss();
            if (imagePickerController.hasPermission()) {
                imagePickerController.openGallery();
            } else {
                imagePickerController.requestPermission();
            }
        });

        createFolder.setOnClickListener(v -> {
            bottomSheetDialog.dismiss();
            // TODO: Implement album creation with Firestore images
            Toast.makeText(this, "Album creation not implemented yet", Toast.LENGTH_SHORT).show();
        });

        bottomSheetDialog.show();
    }

    @Override
    public void onImagePicked(Uri imageUri) {
        handleImageSelection(imageUri);
    }

    @Override
    public void onPermissionDenied() {
        Toast.makeText(this, "Permission denied to access gallery", Toast.LENGTH_SHORT).show();
    }

    private void handleImageSelection(Uri imageUri) {
        // Show loading dialog
        loadingDialog = new AlertDialog.Builder(this)
            .setMessage("Đang tải ảnh lên...")
            .setCancelable(false)
            .create();
        loadingDialog.show();

        imageStorageController.saveImage(imageUri, new ImageUploadService.UploadCallback() {
            @Override
            public void onSuccess(String imageUrl) {
                runOnUiThread(() -> {
                    hideLoading();
                    showToast("Upload successful");
                    refreshImages();
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    hideLoading();
                    showToast("Upload failed: " + error);
                });
            }

            @Override
            public void onProgress(int progress) {
                runOnUiThread(() -> {
                    if (progressBar != null) {
                        progressBar.setProgress(progress);
                    }
                });
            }
        });
    }

    private void hideLoading() {
        if (loadingDialog != null && loadingDialog.isShowing()) {
            loadingDialog.dismiss();
        }
        if (progressBar != null) {
            progressBar.setVisibility(View.GONE);
        }
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void refreshImages() {
        loadImagesFromFirestore();
    }
}