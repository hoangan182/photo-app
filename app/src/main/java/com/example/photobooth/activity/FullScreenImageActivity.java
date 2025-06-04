package com.example.photobooth.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ExifInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.example.photobooth.R;
import com.example.photobooth.controllers.AlbumController;
import com.example.photobooth.controllers.FavoriteController;
import com.example.photobooth.controllers.ImageStorageController;
import com.example.photobooth.controllers.TrashController;
import com.example.photobooth.models.Album;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import android.graphics.drawable.Drawable;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class FullScreenImageActivity extends AppCompatActivity {

    private ImageView fullScreenImage;
    private TextView txtBack;
    private TextView editButton;
    private ImageView favoriteButton;
    private ImageView addToAlbumButton;
    private ImageView deleteButton;
    private TextView resolutionTextView;
    private TextView sizeTextView;
    private ImageView restoreButton;
    private ImageView permanentDeleteButton;

    private AlbumController albumController;
    private FavoriteController favoriteController;
    private ImageStorageController imageStorageController;
    private TrashController trashController;
    private String photoPath;
    private String albumId;
    private boolean isFavorite;
    private boolean isTrash;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_full_screen_image);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Get photo path and album ID from intent
        photoPath = getIntent().getStringExtra("photo_path");
        albumId = getIntent().getStringExtra("album_id");
        isTrash = getIntent().getBooleanExtra("isTrash", false);
        if (photoPath == null) {
            Toast.makeText(this, "Error loading image", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Initialize controllers
        albumController = new AlbumController(this);
        favoriteController = new FavoriteController(this);
        imageStorageController = new ImageStorageController(this);
        trashController = new TrashController(this);

        // Initialize views
        initializeViews();

        // Set click listeners
        setupClickListeners();

        // Load image and details
        loadImage();
        loadImageDetails();
        updateFavoriteState();
        updateTrashButtons();
    }

    private void initializeViews() {
        fullScreenImage = findViewById(R.id.fullScreenImage);
        txtBack = findViewById(R.id.txtBack);
        editButton = findViewById(R.id.editButton);
        favoriteButton = findViewById(R.id.favoriteButton);
        addToAlbumButton = findViewById(R.id.addToAlbumButton);
        deleteButton = findViewById(R.id.deleteButton);
        resolutionTextView = findViewById(R.id.resolutionTextView);
        sizeTextView = findViewById(R.id.sizeTextView);
        restoreButton = findViewById(R.id.restoreButton);
        permanentDeleteButton = findViewById(R.id.permanentDeleteButton);
    }

    private void setupClickListeners() {
        txtBack.setOnClickListener(v -> finish());

        if (editButton != null) {
            editButton.setOnClickListener(v -> {
                Intent intent = new Intent(this, ImageEditActivity.class);
                intent.putExtra("imagePath", photoPath);
                startActivityForResult(intent, 1);
            });
        }

        if (favoriteButton != null) {
            favoriteButton.setOnClickListener(v -> toggleFavorite());
        }

        if (addToAlbumButton != null) {
            addToAlbumButton.setOnClickListener(v -> showAlbumSelectionDialog());
        }

        if (deleteButton != null) {
            deleteButton.setOnClickListener(v -> showDeleteConfirmationDialog());
        }

        if (restoreButton != null) {
            restoreButton.setOnClickListener(v -> showRestoreConfirmationDialog());
        }

        if (permanentDeleteButton != null) {
            permanentDeleteButton.setOnClickListener(v -> showPermanentDeleteConfirmationDialog());
        }

        // Add click listener for the image to toggle system UI visibility
        fullScreenImage.setOnClickListener(v -> {
            View decorView = getWindow().getDecorView();
            int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
            decorView.setSystemUiVisibility(uiOptions);
        });
    }

    private void updateTrashButtons() {
        if (isTrash) {
            // Hide normal buttons
            if (editButton != null) editButton.setVisibility(View.GONE);
            if (favoriteButton != null) favoriteButton.setVisibility(View.GONE);
            if (addToAlbumButton != null) addToAlbumButton.setVisibility(View.GONE);
            if (deleteButton != null) deleteButton.setVisibility(View.GONE);

            // Show trash buttons
            if (restoreButton != null) restoreButton.setVisibility(View.VISIBLE);
            if (permanentDeleteButton != null) permanentDeleteButton.setVisibility(View.VISIBLE);
        } else {
            // Show normal buttons
            if (editButton != null) editButton.setVisibility(View.VISIBLE);
            if (favoriteButton != null) favoriteButton.setVisibility(View.VISIBLE);
            if (addToAlbumButton != null) addToAlbumButton.setVisibility(View.VISIBLE);
            if (deleteButton != null) deleteButton.setVisibility(View.VISIBLE);

            // Hide trash buttons
            if (restoreButton != null) restoreButton.setVisibility(View.GONE);
            if (permanentDeleteButton != null) permanentDeleteButton.setVisibility(View.GONE);
        }
    }

    private void loadImage() {
        Bitmap bitmap = BitmapFactory.decodeFile(photoPath);
        if (bitmap != null) {
            fullScreenImage.setImageBitmap(bitmap);
        } else {
            Toast.makeText(this, "Error loading image", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void loadImageDetails() {
        try {
            // Get image dimensions
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(photoPath, options);
            String resolution = options.outWidth + "x" + options.outHeight;
            resolutionTextView.setText(resolution);

            // Get file size
            File file = new File(photoPath);
            long sizeInBytes = file.length();
            String sizeText = formatFileSize(sizeInBytes);
            sizeTextView.setText(sizeText);

        } catch (Exception e) {
            Toast.makeText(this, "Error loading image details", Toast.LENGTH_SHORT).show();
        }
    }

    private String formatFileSize(long sizeInBytes) {
        if (sizeInBytes < 1024) {
            return sizeInBytes + " B";
        } else if (sizeInBytes < 1024 * 1024) {
            return String.format("%.1f KB", sizeInBytes / 1024.0);
        } else {
            return String.format("%.1f MB", sizeInBytes / (1024.0 * 1024.0));
        }
    }

    private void updateFavoriteState() {
        isFavorite = favoriteController.isFavorite(photoPath);
        favoriteButton.setImageResource(isFavorite ? R.drawable.heart_filled : R.drawable.heart);
    }

    private void toggleFavorite() {
        if (isFavorite) {
            favoriteController.removeFavorite(photoPath);
        } else {
            favoriteController.addFavorite(photoPath);
        }
        isFavorite = !isFavorite;
        favoriteButton.setImageResource(isFavorite ? R.drawable.heart_filled : R.drawable.heart);
    }

    private void showDeleteConfirmationDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Xóa ảnh")
                .setMessage("Bạn có chắc chắn muốn xóa ảnh này?")
                .setPositiveButton("Xóa", (dialog, which) -> deleteImage())
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void deleteImage() {
        if (albumId != null) {
            // Delete from album
            albumController.removePhotoFromAlbum(albumId, photoPath);
        } else {
            // Move to trash instead of permanent delete
            trashController.addToTrash(photoPath);
            // Remove from favorites if it's a favorite
            if (favoriteController.isFavorite(photoPath)) {
                favoriteController.removeFavorite(photoPath);
            }
            // Remove from albums if it's in any album
            List<Album> albums = albumController.getAllAlbums();
            for (Album album : albums) {
                if (album.getPhotoPaths().contains(photoPath)) {
                    albumController.removePhotoFromAlbum(album.getId(), photoPath);
                }
            }
        }
        
        // Set result to notify AllPhotoActivity to refresh
        Intent resultIntent = new Intent();
        resultIntent.putExtra("deleted_photo_path", photoPath);
        setResult(RESULT_OK, resultIntent);
        finish();
    }

    private void showRestoreConfirmationDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Khôi phục ảnh")
                .setMessage("Bạn có chắc chắn muốn khôi phục ảnh này?")
                .setPositiveButton("Khôi phục", (dialog, which) -> restoreImage())
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void showPermanentDeleteConfirmationDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Xóa vĩnh viễn")
                .setMessage("Bạn có chắc chắn muốn xóa vĩnh viễn ảnh này?")
                .setPositiveButton("Xóa", (dialog, which) -> permanentlyDeleteImage())
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void restoreImage() {
        if (trashController.restoreImage(photoPath)) {
            Toast.makeText(this, "Đã khôi phục ảnh", Toast.LENGTH_SHORT).show();
            setResult(RESULT_OK);
            finish();
        } else {
            Toast.makeText(this, "Không thể khôi phục ảnh", Toast.LENGTH_SHORT).show();
        }
    }

    private void permanentlyDeleteImage() {
        if (trashController.permanentlyDeleteImage(photoPath)) {
            Toast.makeText(this, "Đã xóa vĩnh viễn ảnh", Toast.LENGTH_SHORT).show();
            setResult(RESULT_OK);
            finish();
        } else {
            Toast.makeText(this, "Không thể xóa ảnh", Toast.LENGTH_SHORT).show();
        }
    }

    private void showAlbumSelectionDialog() {
        List<Album> albums = albumController.getAllAlbums();
        if (albums.isEmpty()) {
            Toast.makeText(this, "Không có album nào", Toast.LENGTH_SHORT).show();
            return;
        }

        String[] albumNames = new String[albums.size()];
        for (int i = 0; i < albums.size(); i++) {
            albumNames[i] = albums.get(i).getTitle();
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Chọn album")
                .setItems(albumNames, (dialog, which) -> {
                    Album selectedAlbum = albums.get(which);
                    addToAlbum(selectedAlbum);
                })
                .setNegativeButton("Hủy", null);

        AlertDialog dialog = builder.create();
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.black);
        dialog.show();
    }

    private void addToAlbum(Album selectedAlbum) {
        if (photoPath == null) {
            Toast.makeText(this, "Không thể thêm ảnh vào album", Toast.LENGTH_SHORT).show();
            return;
        }

        Glide.with(this)
            .asBitmap()
            .load(photoPath)
            .into(new CustomTarget<Bitmap>() {
                @Override
                public void onResourceReady(@NonNull Bitmap bitmap, @Nullable Transition<? super Bitmap> transition) {
                    albumController.addPhotoToAlbum(selectedAlbum.getId(), bitmap, new AlbumController.OnPhotoAddedListener() {
                        @Override
                        public void onSuccess() {
                            runOnUiThread(() -> {
                                Toast.makeText(FullScreenImageActivity.this, "Đã thêm ảnh vào album", Toast.LENGTH_SHORT).show();
                            });
                        }

                        @Override
                        public void onFailure(String error) {
                            runOnUiThread(() -> {
                                Toast.makeText(FullScreenImageActivity.this, "Không thể thêm ảnh: " + error, Toast.LENGTH_SHORT).show();
                            });
                        }
                    });
                }

                @Override
                public void onLoadCleared(@Nullable Drawable placeholder) {
                    Toast.makeText(FullScreenImageActivity.this, "Không thể tải ảnh", Toast.LENGTH_SHORT).show();
                }
            });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK) {
            // Reload image after editing
            loadImage();
            loadImageDetails();
        }
    }
}