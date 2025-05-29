package com.example.photobooth.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.ActionMode;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.photobooth.R;
import com.example.photobooth.adapter.PhotoGroupAdapter;
import com.example.photobooth.controllers.ImagePickerController;
import com.example.photobooth.controllers.ImageStorageController;
import com.example.photobooth.models.PhotoItem;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class AllPhotoActivity extends AppCompatActivity implements ImagePickerController.ImagePickerCallback, PhotoGroupAdapter.OnPhotoSelectionListener {

    private List<PhotoItem> photos;
    private ImagePickerController imagePickerController;
    private ImageStorageController imageStorageController;
    private PhotoGroupAdapter photoAdapter;
    private ActionMode actionMode;

    private ListView listViewAll;
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
        photos = new ArrayList<>();

        // Initialize views
        initializeViews();

        // Initialize controllers
        imagePickerController = new ImagePickerController(this, this);
        imageStorageController = new ImageStorageController(this);

        // Set click listeners
        setupClickListeners();

        // Load existing images
        loadImagesFromStorage();
    }

    private void initializeViews() {
        listViewAll = findViewById(R.id.listViewAll);
        noAllImageLayout = findViewById(R.id.noAllImageLayout);
        imgShowOption = findViewById(R.id.imgShowOptionAllPhoto);
        txtBackAllPhoto = findViewById(R.id.txtBackAllPhoto);

        // Set initial visibility
        if (listViewAll != null) {
            listViewAll.setVisibility(View.VISIBLE);
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

    private void loadImagesFromStorage() {
        try {
            Log.d("AllPhotoActivity", "Loading images from storage");
            photos = imageStorageController.loadImages();
            Log.d("AllPhotoActivity", "Loaded " + photos.size() + " images");
            // Update UI on the main thread
            runOnUiThread(this::updateListView);
        } catch (Exception e) {
            Log.e("AllPhotoActivity", "Error loading images", e);
            Toast.makeText(this, "Error loading images", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateListView() {
        if (listViewAll == null || noAllImageLayout == null) {
            Log.e("AllPhotoActivity", "Views not initialized");
            return;
        }

        try {
            Log.d("AllPhotoActivity", "Updating ListView with " + photos.size() + " photos");
            if (photos.isEmpty()) {
                Log.d("AllPhotoActivity", "No photos to display");
                noAllImageLayout.setVisibility(View.VISIBLE);
                listViewAll.setVisibility(View.GONE);
            } else {
                Log.d("AllPhotoActivity", "Displaying photos");
                noAllImageLayout.setVisibility(View.GONE);
                listViewAll.setVisibility(View.VISIBLE);
                if (photoAdapter == null) {
                    Log.d("AllPhotoActivity", "Creating new PhotoGroupAdapter");
                    photoAdapter = new PhotoGroupAdapter(this, photos);
                    photoAdapter.setOnPhotoSelectionListener(this);
                    listViewAll.setAdapter(photoAdapter);
                } else {
                    Log.d("AllPhotoActivity", "Updating existing PhotoGroupAdapter");
                    photoAdapter.updatePhotos(photos);
                }
                // Force refresh
                photoAdapter.notifyDataSetChanged();
                listViewAll.invalidateViews();
                listViewAll.requestLayout();
            }
        } catch (Exception e) {
            Log.e("AllPhotoActivity", "Error updating list view", e);
            Toast.makeText(this, "Error updating display", Toast.LENGTH_SHORT).show();
        }
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
            // TODO: Implement add album functionality
        });

        bottomSheetDialog.show();
    }

    @Override
    public void onImagePicked(Uri imageUri) {
        try {
            String savedImagePath = imageStorageController.saveImage(imageUri);
            loadImagesFromStorage(); // Reload all photos to update the view
            Toast.makeText(this, "Image saved successfully", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            Toast.makeText(this, "Failed to save image", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    @Override
    public void onPermissionDenied() {
        Toast.makeText(this, "Permission denied to access gallery", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onSelectionChanged(int selectedCount) {
        if (selectedCount > 0) {
            if (actionMode == null) {
                actionMode = startSupportActionMode(new ActionMode.Callback() {
                    @Override
                    public boolean onCreateActionMode(ActionMode mode, android.view.Menu menu) {
                        mode.getMenuInflater().inflate(R.menu.menu_photo_selection, menu);
                        return true;
                    }

                    @Override
                    public boolean onPrepareActionMode(ActionMode mode, android.view.Menu menu) {
                        return false;
                    }

                    @Override
                    public boolean onActionItemClicked(ActionMode mode, android.view.MenuItem item) {
                        if (item.getItemId() == R.id.action_delete) {
                            showDeleteConfirmationDialog();
                            return true;
                        }
                        return false;
                    }

                    @Override
                    public void onDestroyActionMode(ActionMode mode) {
                        actionMode = null;
                        photoAdapter.setMultiSelect(false);
                    }
                });
            }
            actionMode.setTitle(selectedCount + " được chọn");
        } else {
            if (actionMode != null) {
                actionMode.finish();
            }
        }
    }

    private void showDeleteConfirmationDialog() {
        new AlertDialog.Builder(this)
            .setTitle("Xóa ảnh này")
            .setMessage("Bạn có muốn xóa ảnh đã chọn không?")
            .setPositiveButton("Xóa", (dialog, which) -> deleteSelectedPhotos())
            .setNegativeButton("Hủy", null)
            .show();
    }

    private void deleteSelectedPhotos() {
        for (PhotoItem photo : photoAdapter.getSelectedItems()) {
            imageStorageController.deleteImage(photo.getPath());
        }
        photoAdapter.clearSelection();
        loadImagesFromStorage();
        if (actionMode != null) {
            actionMode.finish();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d("AllPhotoActivity", "onActivityResult called - requestCode: " + requestCode + ", resultCode: " + resultCode);
        
        if (requestCode == 2 && resultCode == RESULT_OK) {
            // Check if we received an edited image path
            String editedImagePath = data.getStringExtra("editedImagePath");
            Log.d("AllPhotoActivity", "Received edited image path: " + editedImagePath);
            
            if (editedImagePath != null) {
                // Update UI on the main thread
                runOnUiThread(() -> {
                    Log.d("AllPhotoActivity", "Starting UI update on main thread");
                    // Reload all photos
                    loadImagesFromStorage();
                    
                    // Force adapter to refresh
                    if (photoAdapter != null) {
                        Log.d("AllPhotoActivity", "Notifying adapter of data change");
                        photoAdapter.notifyDataSetChanged();
                    } else {
                        Log.e("AllPhotoActivity", "photoAdapter is null");
                    }
                    
                    // Force list view to refresh
                    if (listViewAll != null) {
                        Log.d("AllPhotoActivity", "Refreshing ListView");
                        listViewAll.invalidateViews();
                        listViewAll.requestLayout();
                        listViewAll.post(() -> {
                            Log.d("AllPhotoActivity", "Post refresh of ListView");
                            listViewAll.invalidateViews();
                            listViewAll.requestLayout();
                        });
                    } else {
                        Log.e("AllPhotoActivity", "listViewAll is null");
                    }
                });
            }
        }
    }
}