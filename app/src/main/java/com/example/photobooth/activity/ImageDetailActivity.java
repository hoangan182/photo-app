package com.example.photobooth.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.bumptech.glide.request.target.Target;
import com.example.photobooth.R;
import com.example.photobooth.controllers.AlbumController;
import com.example.photobooth.controllers.TrashController;
import com.example.photobooth.models.Album;
import com.example.photobooth.models.Image;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.graphics.drawable.Drawable;

public class ImageDetailActivity extends AppCompatActivity {
    private static final String TAG = "ImageDetailActivity";
    public static final String EXTRA_IMAGE = "extra_image";

    private ImageView photoView;
    private TextView txtBack;
    private TextView editButton;
    private ImageView favoriteButton;
    private ImageView addToAlbumButton;
    private ImageView deleteButton;
    private ImageView restoreButton;
    private ImageView permanentDeleteButton;
    private TextView resolutionTextView;
    private TextView sizeTextView;
    private ProgressBar progressBar;
    private FirebaseFirestore db;
    private Image currentImage;
    private boolean isFavorite = false;
    private AlbumController albumController;
    private TrashController trashController;
    private boolean isInTrash = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_detail);

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();
        albumController = new AlbumController(this);
        trashController = new TrashController(this);

        // Initialize views
        initializeViews();

        // Get image data from intent
        currentImage = getIntent().getParcelableExtra(EXTRA_IMAGE);
        if (currentImage != null) {
            displayImageDetails(currentImage);
            checkFavoriteStatus();
            checkTrashStatus();
        } else {
            Log.e(TAG, "No image data received");
            finish();
        }
    }

    private void initializeViews() {
        photoView = findViewById(R.id.photoView);
        txtBack = findViewById(R.id.txtBack);
        editButton = findViewById(R.id.editButton);
        favoriteButton = findViewById(R.id.favoriteButton);
        addToAlbumButton = findViewById(R.id.addToAlbumButton);
        deleteButton = findViewById(R.id.deleteButton);
        restoreButton = findViewById(R.id.restoreButton);
        permanentDeleteButton = findViewById(R.id.permanentDeleteButton);
        resolutionTextView = findViewById(R.id.resolutionTextView);
        sizeTextView = findViewById(R.id.sizeTextView);
        progressBar = findViewById(R.id.progressBar);

        // Set click listeners
        txtBack.setOnClickListener(v -> finish());
        editButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, ImageEditActivity.class);
            intent.putExtra("imagePath", currentImage.getUrl());
            startActivityForResult(intent, 1);
        });
        favoriteButton.setOnClickListener(v -> toggleFavorite());
        addToAlbumButton.setOnClickListener(v -> showAlbumDialog());
        deleteButton.setOnClickListener(v -> moveToTrash());
        restoreButton.setOnClickListener(v -> restoreFromTrash());
        permanentDeleteButton.setOnClickListener(v -> permanentlyDelete());
    }

    private void checkFavoriteStatus() {
        if (currentImage != null) {
            db.collection("favorites")
                .document(currentImage.getId())
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    isFavorite = documentSnapshot.exists();
                    updateFavoriteButton();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error checking favorite status: " + e.getMessage());
                    Toast.makeText(this, "Không thể kiểm tra trạng thái yêu thích", Toast.LENGTH_SHORT).show();
                });
        }
    }

    private void toggleFavorite() {
        if (currentImage == null) return;

        if (isFavorite) {
            // Remove from favorites
            db.collection("favorites")
                .document(currentImage.getId())
                .delete()
                .addOnSuccessListener(aVoid -> {
                    isFavorite = false;
                    updateFavoriteButton();
                    Toast.makeText(this, "Đã xóa khỏi yêu thích", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error removing from favorites: " + e.getMessage());
                    Toast.makeText(this, "Không thể xóa khỏi yêu thích", Toast.LENGTH_SHORT).show();
                });
        } else {
            // Add to favorites
            db.collection("favorites")
                .document(currentImage.getId())
                .set(currentImage)
                .addOnSuccessListener(aVoid -> {
                    isFavorite = true;
                    updateFavoriteButton();
                    Toast.makeText(this, "Đã thêm vào yêu thích", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error adding to favorites: " + e.getMessage());
                    Toast.makeText(this, "Không thể thêm vào yêu thích", Toast.LENGTH_SHORT).show();
                });
        }
    }

    private void updateFavoriteButton() {
        favoriteButton.setImageResource(isFavorite ? R.drawable.heart_filled : R.drawable.heart);
    }

    private void showAlbumDialog() {
        List<Album> albums = albumController.getAllAlbums();
        if (albums.isEmpty()) {
            Toast.makeText(this, "Chưa có album nào. Vui lòng tạo album trước.", Toast.LENGTH_SHORT).show();
            return;
        }

        String[] albumNames = new String[albums.size()];
        for (int i = 0; i < albums.size(); i++) {
            albumNames[i] = albums.get(i).getTitle();
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Thêm vào album");
        builder.setItems(albumNames, (dialog, which) -> {
            Album selectedAlbum = albums.get(which);
            addImageToAlbum(selectedAlbum);
        });
        builder.show();
    }

    private void addImageToAlbum(Album album) {
        if (currentImage == null || currentImage.getUrl() == null) {
            Toast.makeText(this, "Không thể thêm ảnh vào album", Toast.LENGTH_SHORT).show();
            return;
        }

        Glide.with(this)
            .asBitmap()
            .load(currentImage.getUrl())
            .into(new CustomTarget<Bitmap>() {
                @Override
                public void onResourceReady(@NonNull Bitmap bitmap, @Nullable Transition<? super Bitmap> transition) {
                    albumController.addPhotoToAlbum(album.getId(), bitmap, new AlbumController.OnPhotoAddedListener() {
                        @Override
                        public void onSuccess() {
                            runOnUiThread(() -> {
                                Toast.makeText(ImageDetailActivity.this, "Đã thêm ảnh vào album", Toast.LENGTH_SHORT).show();
                            });
                        }

                        @Override
                        public void onFailure(String error) {
                            runOnUiThread(() -> {
                                Toast.makeText(ImageDetailActivity.this, "Không thể thêm ảnh: " + error, Toast.LENGTH_SHORT).show();
                            });
                        }
                    });
                }

                @Override
                public void onLoadCleared(@Nullable Drawable placeholder) {
                    Toast.makeText(ImageDetailActivity.this, "Không thể tải ảnh", Toast.LENGTH_SHORT).show();
                }
            });
    }

    private void displayImageDetails(Image image) {
        Log.d(TAG, "Displaying image details: " + image.getUrl());
        showLoading(true);

        // Load image using Glide with specific options
        Glide.with(this)
            .load(image.getUrl())
            .override(Target.SIZE_ORIGINAL)
            .fitCenter()
            .listener(new RequestListener<android.graphics.drawable.Drawable>() {
                @Override
                public boolean onLoadFailed(GlideException e, Object model, Target<android.graphics.drawable.Drawable> target, boolean isFirstResource) {
                    Log.e(TAG, "Error loading image: " + e.getMessage());
                    showLoading(false);
                    return false;
                }

                @Override
                public boolean onResourceReady(android.graphics.drawable.Drawable resource, Object model, Target<android.graphics.drawable.Drawable> target, DataSource dataSource, boolean isFirstResource) {
                    Log.d(TAG, "Image loaded successfully");
                    showLoading(false);
                    return false;
                }
            })
            .into(photoView);

        // Display image details
        sizeTextView.setText(formatFileSize(image.getSize()));
        
        // TODO: Get and display image resolution
        // For now, using a placeholder
        resolutionTextView.setText("1920x1920");
    }

    private String formatFileSize(long size) {
        if (size <= 0) return "0 B";
        final String[] units = new String[]{"B", "KB", "MB", "GB", "TB"};
        int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
        return String.format(Locale.getDefault(), "%.1f %s", size / Math.pow(1024, digitGroups), units[digitGroups]);
    }

    private void showLoading(boolean isLoading) {
        if (progressBar != null) {
            progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK && data != null) {
            String editedImageUrl = data.getStringExtra("editedImageUrl");
            if (editedImageUrl != null) {
                // Update the current image with the new URL
                currentImage.setUrl(editedImageUrl);
                currentImage.setUpdatedAt(System.currentTimeMillis());
                // Update in Firestore
                db.collection("images")
                    .document(currentImage.getId())
                    .set(currentImage)
                    .addOnSuccessListener(aVoid -> {
                        // Reload image details
                        displayImageDetails(currentImage);
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Failed to update image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
            }
        }
    }

    private void checkTrashStatus() {
        if (currentImage != null) {
            List<String> trashImages = trashController.getTrashImagePaths();
            isInTrash = trashImages.contains(currentImage.getUrl());
            updateTrashButtons();
        }
    }

    private void updateTrashButtons() {
        if (isInTrash) {
            deleteButton.setVisibility(View.GONE);
            restoreButton.setVisibility(View.VISIBLE);
            permanentDeleteButton.setVisibility(View.VISIBLE);
        } else {
            deleteButton.setVisibility(View.VISIBLE);
            restoreButton.setVisibility(View.GONE);
            permanentDeleteButton.setVisibility(View.GONE);
        }
    }

    private void moveToTrash() {
        if (currentImage == null) return;

        // Show confirmation dialog
        new AlertDialog.Builder(this)
            .setTitle("Xóa ảnh")
            .setMessage("Bạn có chắc chắn muốn xóa ảnh này? Ảnh sẽ được chuyển vào thùng rác.")
            .setPositiveButton("Xóa", (dialog, which) -> {
                // Add to trash first
                if (trashController.addToTrash(currentImage.getUrl())) {
                    // Update image status in Firestore
                    currentImage.setDeleted(true);
                    currentImage.setUpdatedAt(System.currentTimeMillis());
                    db.collection("images")
                        .document(currentImage.getId())
                        .set(currentImage)
                        .addOnSuccessListener(aVoid -> {
                            isInTrash = true;
                            updateTrashButtons();
                            Toast.makeText(this, "Đã chuyển ảnh vào thùng rác", Toast.LENGTH_SHORT).show();
                            
                            // Navigate back to main activity
                            Intent mainIntent = new Intent(this, MainActivity.class);
                            mainIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(mainIntent);
                            finish();
                        })
                        .addOnFailureListener(e -> {
                            // If Firestore update fails, remove from trash
                            trashController.removeFromTrash(currentImage.getUrl());
                            Toast.makeText(this, "Không thể xóa ảnh: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        });
                } else {
                    Toast.makeText(this, "Không thể chuyển ảnh vào thùng rác", Toast.LENGTH_SHORT).show();
                }
            })
            .setNegativeButton("Hủy", null)
            .show();
    }

    private void restoreFromTrash() {
        if (currentImage == null) return;

        // Check if image is in trash
        if (!trashController.isInTrash(currentImage.getUrl())) {
            Toast.makeText(this, "Ảnh không tồn tại trong thùng rác", Toast.LENGTH_SHORT).show();
            return;
        }

        // Update image status in Firestore first
        currentImage.setDeleted(false);
        currentImage.setUpdatedAt(System.currentTimeMillis());
        db.collection("images")
            .document(currentImage.getId())
            .set(currentImage)
            .addOnSuccessListener(aVoid -> {
                // Remove from trash after successful Firestore update
                if (trashController.removeFromTrash(currentImage.getUrl())) {
                    isInTrash = false;
                    updateTrashButtons();
                    Toast.makeText(this, "Đã khôi phục ảnh", Toast.LENGTH_SHORT).show();
                    
                    // Navigate back to main activity
                    Intent mainIntent = new Intent(this, MainActivity.class);
                    mainIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(mainIntent);
                    finish();
                } else {
                    // If failed to remove from trash, revert the Firestore update
                    currentImage.setDeleted(true);
                    db.collection("images")
                        .document(currentImage.getId())
                        .set(currentImage)
                        .addOnSuccessListener(aVoid2 -> {
                            Toast.makeText(this, "Không thể khôi phục ảnh", Toast.LENGTH_SHORT).show();
                        });
                }
            })
            .addOnFailureListener(e -> {
                Toast.makeText(this, "Không thể khôi phục ảnh: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
    }

    private void permanentlyDelete() {
        if (currentImage == null) return;

        // Show confirmation dialog
        new AlertDialog.Builder(this)
            .setTitle("Xóa vĩnh viễn")
            .setMessage("Bạn có chắc chắn muốn xóa vĩnh viễn ảnh này? Hành động này không thể hoàn tác.")
            .setPositiveButton("Xóa", (dialog, which) -> {
                // Delete from Firestore first
                db.collection("images")
                    .document(currentImage.getId())
                    .delete()
                    .addOnSuccessListener(aVoid -> {
                        // Delete from trash
                        if (trashController.permanentlyDeleteImage(currentImage.getUrl())) {
                            Toast.makeText(this, "Đã xóa ảnh vĩnh viễn", Toast.LENGTH_SHORT).show();
                            
                            // Navigate back to main activity
                            Intent mainIntent = new Intent(this, MainActivity.class);
                            mainIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(mainIntent);
                            finish();
                        } else {
                            Toast.makeText(this, "Không thể xóa ảnh khỏi thùng rác", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Không thể xóa ảnh: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
            })
            .setNegativeButton("Hủy", null)
            .show();
    }
} 