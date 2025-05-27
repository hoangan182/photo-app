package com.example.photobooth.activity;

import android.content.Intent;
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

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.ActionMode;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.photobooth.R;
import com.example.photobooth.adapter.ImageAdapter;
import com.example.photobooth.controllers.ImagePickerController;
import com.example.photobooth.controllers.ImageStorageController;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AllPhotoActivity extends AppCompatActivity implements ImagePickerController.ImagePickerCallback {

    private List<String> imageUrls;
    private ImagePickerController imagePickerController;
    private ImageStorageController imageStorageController;
    private ImageAdapter imageAdapter;
    private ActionMode actionMode;
    private Set<Integer> selectedItems;

    private GridView gridViewAll;
    private ImageView imgShowOption;
    private ConstraintLayout noAllImageLayout;
    private TextView txtBackAllPhoto;

    private boolean isMultiSelect = false;

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
        imageUrls = new ArrayList<>();

        // Initialize views
        initializeViews();

        // Initialize controllers
        imagePickerController = new ImagePickerController(this, this);
        imageStorageController = new ImageStorageController(this);

        // Initialize selection set
        selectedItems = new HashSet<>();

        // Set click listeners
        setupClickListeners();

        // Load existing images
        loadImagesFromStorage();
    }

    private void initializeViews() {
        gridViewAll = findViewById(R.id.gridViewAll);
        noAllImageLayout = findViewById(R.id.noAllImageLayout);
        txtBackAllPhoto = findViewById(R.id.txtBackChangePassword);
        imgShowOption = findViewById(R.id.imgShowOptionAllPhoto);

        // Set initial visibility
        if (gridViewAll != null) {
            gridViewAll.setVisibility(View.VISIBLE);
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

        if (gridViewAll != null) {
            // Click listener for viewing image
            gridViewAll.setOnItemClickListener((parent, view, position, id) -> {
                if (isMultiSelect) {
                    toggleSelection(position);
                } else {
                    Intent intent = new Intent(this, FullScreenImageActivity.class);
                    intent.putExtra("imagePath", imageUrls.get(position));
                    startActivityForResult(intent, 1);
                }
            });

            // Long click listener for selection mode
            gridViewAll.setOnItemLongClickListener((parent, view, position, id) -> {
                if (!isMultiSelect) {
                    startMultiSelect();
                }
                toggleSelection(position);
                return true;
            });
        }
    }

    private void loadImagesFromStorage() {
        try {
            File storageDir = imageStorageController.getStorageDir();
            File[] files = storageDir.listFiles();
            if (files != null) {
                imageUrls.clear();
                for (File file : files) {
                    if (file.getName().endsWith(".jpg")) {
                        imageUrls.add(file.getAbsolutePath());
                    }
                }
            }
            updateGridView();
        } catch (Exception e) {
            Log.e("AllPhotoActivity", "Error loading images", e);
            Toast.makeText(this, "Error loading images", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateGridView() {
        if (gridViewAll == null || noAllImageLayout == null) {
            Log.e("AllPhotoActivity", "Views not initialized");
            return;
        }

        try {
            if (imageUrls.isEmpty()) {
                noAllImageLayout.setVisibility(View.VISIBLE);
                gridViewAll.setVisibility(View.GONE);
            } else {
                noAllImageLayout.setVisibility(View.GONE);
                gridViewAll.setVisibility(View.VISIBLE);
                imageAdapter = new ImageAdapter(this, imageUrls, selectedItems, isMultiSelect);
                gridViewAll.setAdapter(imageAdapter);
            }
        } catch (Exception e) {
            Log.e("AllPhotoActivity", "Error updating grid view", e);
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

    private void startMultiSelect() {
        isMultiSelect = true;
        actionMode = startSupportActionMode(new ActionMode.Callback() {
            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                mode.getMenuInflater().inflate(R.menu.menu_multi_select, menu);
                return true;
            }

            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                return false;
            }

            @Override
            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                if (item.getItemId() == R.id.action_delete) {
                    showDeleteConfirmationDialog();
                    return true;
                }
                return false;
            }

            @Override
            public void onDestroyActionMode(ActionMode mode) {
                exitMultiSelect();
            }
        });
        imgShowOption.setVisibility(View.GONE);
    }

    private void exitMultiSelect() {
        isMultiSelect = false;
        selectedItems.clear();
        if (actionMode != null) {
            actionMode.finish();
        }
        imgShowOption.setVisibility(View.VISIBLE);
        if (imageAdapter != null) {
            imageAdapter.notifyDataSetChanged();
        }
    }

    private void toggleSelection(int position) {
        if (selectedItems.contains(position)) {
            selectedItems.remove(position);
        } else {
            selectedItems.add(position);
        }
        
        if (selectedItems.isEmpty()) {
            actionMode.finish();
        } else {
            actionMode.setTitle(selectedItems.size() + " selected");
            imageAdapter.notifyDataSetChanged();
        }
    }

    private void deleteSelectedItems() {
        // Convert to list and sort in descending order to avoid index shifting
        List<Integer> selectedPositions = new ArrayList<>(selectedItems);
        Collections.sort(selectedPositions, Collections.reverseOrder());

        int deletedCount = 0;
        for (int position : selectedPositions) {
            String imagePath = imageUrls.get(position);
            if (imageStorageController.deleteImage(imagePath)) {
                imageUrls.remove(position);
                deletedCount++;
            }
        }

        if (deletedCount > 0) {
            Toast.makeText(this, deletedCount + " images deleted", Toast.LENGTH_SHORT).show();
            updateGridView();
        }

        exitMultiSelect();
    }

    private void showDeleteConfirmationDialog() {
        new AlertDialog.Builder(this)
            .setTitle("Delete Images")
            .setMessage("Are you sure you want to delete " + selectedItems.size() + " images?")
            .setPositiveButton("Delete", (dialog, which) -> deleteSelectedItems())
            .setNegativeButton("Cancel", null)
            .show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK) {
            // Reload images after returning from FullScreenImageActivity
            loadImagesFromStorage();
        }
        imagePickerController.handleActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        imagePickerController.handleRequestPermissionsResult(requestCode, grantResults);
    }

    @Override
    public void onImagePicked(Uri imageUri) {
        try {
            String savedImagePath = imageStorageController.saveImage(imageUri);
            imageUrls.add(savedImagePath);
            updateGridView();
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
}