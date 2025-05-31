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
import android.widget.ListView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.EditText;

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
import com.example.photobooth.controllers.TrashController;
import com.example.photobooth.controllers.FavoriteController;
import com.example.photobooth.controllers.AlbumController;
import com.example.photobooth.models.PhotoItem;
import com.example.photobooth.models.Album;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class AllPhotoActivity extends AppCompatActivity implements ImagePickerController.ImagePickerCallback, PhotoGroupAdapter.OnPhotoSelectionListener {

    private List<PhotoItem> photos;
    private ImagePickerController imagePickerController;
    private ImageStorageController imageStorageController;
    private TrashController trashController;
    private FavoriteController favoriteController;
    private AlbumController albumController;
    private PhotoGroupAdapter photoAdapter;
    private android.view.ActionMode actionMode;

    private final android.view.ActionMode.Callback actionModeCallback = new android.view.ActionMode.Callback() {
        @Override
        public boolean onCreateActionMode(android.view.ActionMode mode, Menu menu) {
            mode.getMenuInflater().inflate(R.menu.menu_photo_selection, menu);
            return true;
        }

        @Override
        public boolean onPrepareActionMode(android.view.ActionMode mode, Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(android.view.ActionMode mode, MenuItem item) {
            if (item.getItemId() == R.id.action_delete) {
                showDeleteConfirmationDialog();
                return true;
            } else if (item.getItemId() == R.id.action_add_to_album) {
                showCreateAlbumDialog();
                return true;
            }
            return false;
        }

        @Override
        public void onDestroyActionMode(android.view.ActionMode mode) {
            photoAdapter.setMultiSelect(false);
            actionMode = null;
        }
    };

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
        trashController = new TrashController(this);
        favoriteController = new FavoriteController(this);
        albumController = new AlbumController(this);

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
            List<PhotoItem> allPhotos = imageStorageController.loadImages();
            List<String> trashPaths = trashController.getTrashImagePaths();
            
            // Filter out photos that are in trash
            photos = new ArrayList<>();
            for (PhotoItem photo : allPhotos) {
                if (!trashPaths.contains(photo.getPath())) {
                    photos.add(photo);
                }
            }
            
            Log.d("AllPhotoActivity", "Loaded " + photos.size() + " images (filtered from " + allPhotos.size() + " total)");
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
            if (photoAdapter != null) {
                photoAdapter.setMultiSelect(true);
                actionMode = startActionMode(actionModeCallback);
                Toast.makeText(this, "Chọn ảnh để tạo album mới", Toast.LENGTH_SHORT).show();
            }
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
    public void onPhotoSelected(PhotoItem photo, boolean isSelected) {
        if (actionMode != null) {
            // Update action mode title
            int count = photoAdapter.getSelectedItems().size();
            actionMode.setTitle(count + " selected");
        } else if (isSelected) {
            // Start action mode when first photo is selected
            actionMode = startActionMode(actionModeCallback);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d("AllPhotoActivity", "onActivityResult called - requestCode: " + requestCode + ", resultCode: " + resultCode);
        
        if (resultCode == RESULT_OK) {
            if (requestCode == 2) {
                // Handle edited image
                String editedImagePath = data.getStringExtra("editedImagePath");
                Log.d("AllPhotoActivity", "Received edited image path: " + editedImagePath);
                
                if (editedImagePath != null) {
                    // Update UI on the main thread
                    runOnUiThread(this::loadImagesFromStorage);
                }
            } else {
                // Handle deleted image
                String deletedPhotoPath = data.getStringExtra("deleted_photo_path");
                if (deletedPhotoPath != null) {
                    Log.d("AllPhotoActivity", "Photo was deleted: " + deletedPhotoPath);
                    // Remove the deleted photo from the current list immediately
                    photos.removeIf(photo -> photo.getPath().equals(deletedPhotoPath));
                    // Update UI immediately
                    runOnUiThread(() -> {
                        if (photoAdapter != null) {
                            photoAdapter.updatePhotos(photos);
                            photoAdapter.notifyDataSetChanged();
                        }
                        if (listViewAll != null) {
                            listViewAll.invalidateViews();
                            listViewAll.requestLayout();
                        }
                        // Then reload from storage to ensure consistency
                        loadImagesFromStorage();
                    });
                }
            }
        }
    }

    private void showDeleteConfirmationDialog() {
        List<PhotoItem> selectedPhotos = photoAdapter.getSelectedItems();
        if (selectedPhotos.isEmpty()) {
            return;
        }

        new AlertDialog.Builder(this)
                .setTitle("Xóa ảnh")
                .setMessage("Bạn có chắc chắn muốn xóa " + selectedPhotos.size() + " ảnh đã chọn?")
                .setPositiveButton("Xóa", (dialog, which) -> deleteSelectedPhotos(selectedPhotos))
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void deleteSelectedPhotos(List<PhotoItem> selectedPhotos) {
        for (PhotoItem photo : selectedPhotos) {
            // Move to trash
            trashController.addToTrash(photo.getPath());
            // Remove from favorites if it's a favorite
            if (favoriteController.isFavorite(photo.getPath())) {
                favoriteController.removeFavorite(photo.getPath());
            }
            // Remove from albums if it's in any album
            List<Album> albums = albumController.getAllAlbums();
            for (Album album : albums) {
                if (album.getPhotoPaths().contains(photo.getPath())) {
                    albumController.removePhotoFromAlbum(album.getId(), photo.getPath());
                }
            }
        }
        
        // Reload photos
        loadImagesFromStorage();
        
        // Finish action mode
        if (actionMode != null) {
            actionMode.finish();
        }
        
        Toast.makeText(this, "Đã xóa " + selectedPhotos.size() + " ảnh", Toast.LENGTH_SHORT).show();
    }

    private void showCreateAlbumDialog() {
        List<PhotoItem> selectedPhotos = photoAdapter.getSelectedItems();
        if (selectedPhotos.isEmpty()) {
            return;
        }

        View dialogView = getLayoutInflater().inflate(R.layout.dialog_edit_album_name, null);
        EditText editText = dialogView.findViewById(R.id.editAlbumName);

        new AlertDialog.Builder(this)
                .setTitle("Tạo album mới")
                .setView(dialogView)
                .setPositiveButton("Tạo", (dialog, which) -> {
                    String albumName = editText.getText().toString().trim();
                    if (!albumName.isEmpty()) {
                        createNewAlbum(albumName, selectedPhotos);
                    } else {
                        Toast.makeText(this, "Vui lòng nhập tên album", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void createNewAlbum(String albumName, List<PhotoItem> selectedPhotos) {
        // Create new album
        Album album = albumController.createAlbum(albumName, "", null);
        
        // Add selected photos to album
        for (PhotoItem photo : selectedPhotos) {
            Bitmap bitmap = BitmapFactory.decodeFile(photo.getPath());
            if (bitmap != null) {
                albumController.addPhotoToAlbum(album.getId(), bitmap);
            }
        }
        
        // Finish action mode
        if (actionMode != null) {
            actionMode.finish();
        }
        
        Toast.makeText(this, "Đã tạo album " + albumName + " với " + selectedPhotos.size() + " ảnh", Toast.LENGTH_SHORT).show();
    }
}