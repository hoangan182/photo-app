package com.example.photobooth.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
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
import com.example.photobooth.R;
import com.example.photobooth.controllers.FavoriteController;
import com.example.photobooth.controllers.ImageStorageController;
import com.example.photobooth.models.Image;

import java.io.File;
import java.text.DecimalFormat;

public class FullScreenImageActivity extends AppCompatActivity {

    private ImageView fullScreenImageView;
    private ImageView favoriteButton;
    private ImageView deleteButton;
    private TextView resolutionTextView;
    private TextView sizeTextView;
    private String imagePath;
    private boolean isFavorite = false;
    private ImageStorageController imageStorageController;
    private FavoriteController favoriteController;

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

        // Initialize controllers
        imageStorageController = new ImageStorageController(this);
        favoriteController = new FavoriteController(this);

        // Initialize views
        initializeViews();

        // Get image path from intent
        imagePath = getIntent().getStringExtra("imagePath");
        if (imagePath != null) {
            loadImage();
            displayImageDetails();
            updateFavoriteState();
        } else {
            Toast.makeText(this, "Error: No image path provided", Toast.LENGTH_SHORT).show();
            finish();
        }

        // Set click listeners
        setupClickListeners();
    }

    private void initializeViews() {
        fullScreenImageView = findViewById(R.id.fullScreenImageView);
        favoriteButton = findViewById(R.id.favoriteButton);
        deleteButton = findViewById(R.id.deleteButton);
        resolutionTextView = findViewById(R.id.resolutionTextView);
        sizeTextView = findViewById(R.id.sizeTextView);

        findViewById(R.id.txtBackFullScreenImage).setOnClickListener(v -> finish());
    }

    private void loadImage() {
        Glide.with(this)
                .load(imagePath)
                .into(fullScreenImageView);
    }

    private void displayImageDetails() {
        File imageFile = new File(imagePath);
        if (imageFile.exists()) {
            // Get image resolution
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(imagePath, options);
            String resolution = options.outWidth + "x" + options.outHeight;
            resolutionTextView.setText(resolution);

            // Get file size
            long sizeInBytes = imageFile.length();
            String size = formatFileSize(sizeInBytes);
            sizeTextView.setText(size);
        }
    }

    private String formatFileSize(long sizeInBytes) {
        if (sizeInBytes <= 0) return "0 B";
        final String[] units = new String[]{"B", "KB", "MB", "GB", "TB"};
        int digitGroups = (int) (Math.log10(sizeInBytes) / Math.log10(1024));
        return new DecimalFormat("#,##0.#").format(sizeInBytes / Math.pow(1024, digitGroups)) + " " + units[digitGroups];
    }

    private void updateFavoriteState() {
        isFavorite = favoriteController.isFavorite(imagePath);
        favoriteButton.setImageResource(isFavorite ? R.drawable.heart_filled : R.drawable.heart);
    }

    private void setupClickListeners() {
        // Favorite button click listener
        favoriteButton.setOnClickListener(v -> {
            isFavorite = !isFavorite;
            if (isFavorite) {
                favoriteController.addFavorite(imagePath);
                Toast.makeText(this, "Added to favorites", Toast.LENGTH_SHORT).show();
            } else {
                favoriteController.removeFavorite(imagePath);
                Toast.makeText(this, "Removed from favorites", Toast.LENGTH_SHORT).show();
            }
            updateFavoriteState();
        });

        // Delete button click listener
        deleteButton.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Delete Image")
                    .setMessage("Are you sure you want to delete this image?")
                    .setPositiveButton("Delete", (dialog, which) -> {
                        deleteImage();
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });

        // Edit button click listener
        findViewById(R.id.editButton).setOnClickListener(v -> {
            editImage();
        });
    }

    private void deleteImage() {
        File file = new File(imagePath);
        if (file.exists()) {
            if (file.delete()) {
                // Remove from favorites if it was favorited
                if (favoriteController.isFavorite(imagePath)) {
                    favoriteController.removeFavorite(imagePath);
                }
                Toast.makeText(this, "Image deleted successfully", Toast.LENGTH_SHORT).show();
                setResult(RESULT_OK);
                finish();
            } else {
                Toast.makeText(this, "Failed to delete image", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void editImage() {
        Intent intent = new Intent(this, ImageEditActivity.class);
        intent.putExtra("imagePath", imagePath);
        startActivityForResult(intent, 2);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 2 && resultCode == RESULT_OK) {
            // Reload the image after editing
            loadImage();
            setResult(RESULT_OK);
        }
    }
}