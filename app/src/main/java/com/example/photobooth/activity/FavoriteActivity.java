package com.example.photobooth.activity;

import android.content.Intent;
import android.media.ExifInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.photobooth.R;
import com.example.photobooth.adapter.PhotoGroupAdapter;
import com.example.photobooth.controllers.FavoriteController;
import com.example.photobooth.models.PhotoItem;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class FavoriteActivity extends AppCompatActivity {
    private static final String TAG = "FavoriteActivity";
    private static final String[] EXIF_DATE_TAGS = {
            ExifInterface.TAG_DATETIME_ORIGINAL,
            ExifInterface.TAG_DATETIME,
            ExifInterface.TAG_DATETIME_DIGITIZED
    };

    private List<PhotoItem> photos = new ArrayList<>();
    private ListView listViewFavorite;
    private ConstraintLayout noFavoriteImageLayout;
    private TextView txtBackFavorite;
    private FavoriteController favoriteController;
    private PhotoGroupAdapter photoAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_favorite);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize controllers
        favoriteController = new FavoriteController(this);

        // Initialize views
        initializeViews();

        // Set click listeners
        setupClickListeners();

        // Load favorite images
        loadFavoriteImages();
    }

    private void initializeViews() {
        listViewFavorite = findViewById(R.id.listViewFavorite);
        noFavoriteImageLayout = findViewById(R.id.noFavoriteImageLayout);
        txtBackFavorite = findViewById(R.id.txtBackAddAlbum);
    }

    private void setupClickListeners() {
        txtBackFavorite.setOnClickListener(v -> finish());

        listViewFavorite.setOnItemClickListener((parent, view, position, id) -> {
            Intent intent = new Intent(this, FullScreenImageActivity.class);
            intent.putExtra("imagePath", photos.get(position).getPath());
            startActivityForResult(intent, 1);
        });
    }

    private long getPhotoCaptureTime(String imagePath) {
        try {
            ExifInterface exif = new ExifInterface(imagePath);
            
            // Try to get date from EXIF data
            for (String tag : EXIF_DATE_TAGS) {
                String dateStr = exif.getAttribute(tag);
                if (dateStr != null) {
                    try {
                        // Parse EXIF date format (usually "yyyy:MM:dd HH:mm:ss")
                        SimpleDateFormat exifDateFormat = new SimpleDateFormat("yyyy:MM:dd HH:mm:ss", Locale.US);
                        Date date = exifDateFormat.parse(dateStr);
                        if (date != null) {
                            Log.d(TAG, "Found EXIF date for " + imagePath + ": " + dateStr);
                            return date.getTime();
                        }
                    } catch (ParseException e) {
                        Log.w(TAG, "Failed to parse EXIF date: " + dateStr, e);
                    }
                }
            }

            // If no EXIF date found, try to get file creation time
            File imageFile = new File(imagePath);
            if (imageFile.exists()) {
                long lastModified = imageFile.lastModified();
                Log.d(TAG, "Using file last modified time for " + imagePath + ": " + lastModified);
                return lastModified;
            }
        } catch (IOException e) {
            Log.e(TAG, "Error reading EXIF data for " + imagePath, e);
        }

        // If all else fails, use current time
        Log.w(TAG, "Using current time for " + imagePath);
        return System.currentTimeMillis();
    }

    private void loadFavoriteImages() {
        try {
            Log.d(TAG, "Starting to load favorite images");
            List<String> imagePaths = favoriteController.getFavoriteImagePaths();
            Log.d(TAG, "Retrieved " + imagePaths.size() + " favorite image paths");
            
            photos.clear();
            for (String path : imagePaths) {
                Log.d(TAG, "Processing image path: " + path);
                if (path != null && !path.isEmpty()) {
                    File imageFile = new File(path);
                    if (imageFile.exists()) {
                        // Get the actual photo capture time from EXIF data
                        long captureTime = getPhotoCaptureTime(path);
                        Log.d(TAG, "Capture time for " + path + ": " + captureTime);
                        PhotoItem photoItem = new PhotoItem(path, captureTime);
                        photos.add(photoItem);
                        Log.d(TAG, "Added photo to list: " + path);
                    } else {
                        Log.w(TAG, "Image file does not exist: " + path);
                    }
                } else {
                    Log.w(TAG, "Skipping null or empty image path");
                }
            }
            
            Log.d(TAG, "Total photos loaded: " + photos.size());
            runOnUiThread(this::updateListView);
        } catch (Exception e) {
            Log.e(TAG, "Error loading favorite images", e);
        }
    }

    private void updateListView() {
        if (listViewFavorite == null || noFavoriteImageLayout == null) {
            Log.e(TAG, "Views not initialized");
            return;
        }

        try {
            Log.d(TAG, "Starting UI update with " + photos.size() + " photos");
            
            if (photos.isEmpty()) {
                Log.d(TAG, "No photos to display, showing empty state");
                noFavoriteImageLayout.setVisibility(View.VISIBLE);
                listViewFavorite.setVisibility(View.GONE);
            } else {
                Log.d(TAG, "Displaying photos in ListView");
                noFavoriteImageLayout.setVisibility(View.GONE);
                listViewFavorite.setVisibility(View.VISIBLE);
                
                if (photoAdapter == null) {
                    Log.d(TAG, "Creating new PhotoGroupAdapter");
                    photoAdapter = new PhotoGroupAdapter(this, photos);
                    listViewFavorite.setAdapter(photoAdapter);
                } else {
                    Log.d(TAG, "Updating existing PhotoGroupAdapter");
                    photoAdapter.updatePhotos(photos);
                }
                
                // Force refresh with detailed logging
                Log.d(TAG, "Forcing adapter refresh");
                photoAdapter.notifyDataSetChanged();
                
                Log.d(TAG, "Invalidating ListView");
                listViewFavorite.invalidateViews();
                
                Log.d(TAG, "Requesting layout update");
                listViewFavorite.requestLayout();
                
                // Post a delayed check to verify the update
                listViewFavorite.post(() -> {
                    Log.d(TAG, "Post-update check - ListView child count: " + listViewFavorite.getChildCount());
                    if (listViewFavorite.getChildCount() == 0) {
                        Log.w(TAG, "ListView has no children after update");
                    }
                });
            }
        } catch (Exception e) {
            Log.e(TAG, "Error updating list view", e);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActivityResult - requestCode: " + requestCode + ", resultCode: " + resultCode);
        
        if (requestCode == 1 && resultCode == RESULT_OK) {
            Log.d(TAG, "Reloading favorite images after returning from FullScreenImageActivity");
            loadFavoriteImages();
        }
    }
}