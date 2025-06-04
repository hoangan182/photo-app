package com.example.photobooth.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.GridView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.photobooth.R;
import com.example.photobooth.adapter.PhotoGroupAdapter;
import com.example.photobooth.models.PhotoItem;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class FavoritesActivity extends AppCompatActivity {
    private static final String TAG = "FavoritesActivity";

    private GridView gridViewFavorites;
    private ProgressBar progressBar;
    private TextView emptyView;
    private TextView txtBack;
    private FirebaseFirestore db;
    private PhotoGroupAdapter photoAdapter;
    private List<PhotoItem> favoritePhotos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorites);

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // Initialize views
        initializeViews();

        // Load favorite images
        loadFavoriteImages();
    }

    private void initializeViews() {
        gridViewFavorites = findViewById(R.id.gridViewFavorites);
        progressBar = findViewById(R.id.progressBar);
        emptyView = findViewById(R.id.emptyView);
        txtBack = findViewById(R.id.txtBack);
        favoritePhotos = new ArrayList<>();

        // Set click listener for back button
        txtBack.setOnClickListener(v -> finish());

        // Set up adapter
        photoAdapter = new PhotoGroupAdapter(this, favoritePhotos);
        gridViewFavorites.setAdapter(photoAdapter);

        // Set item click listener
        gridViewFavorites.setOnItemClickListener((parent, view, position, id) -> {
            PhotoItem selectedPhoto = favoritePhotos.get(position);
            Intent intent = new Intent(this, FullScreenImageActivity.class);
            intent.putExtra("imagePath", selectedPhoto.getPath());
            startActivity(intent);
        });
    }

    private void loadFavoriteImages() {
        showLoading(true);
        emptyView.setVisibility(View.GONE);

        Log.d(TAG, "Starting to load favorite images from Firestore");
        db.collection("favorites")
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                Log.d(TAG, "Successfully got " + queryDocumentSnapshots.size() + " documents from favorites collection");
                favoritePhotos.clear();
                for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                    try {
                        // Log toàn bộ dữ liệu của document để debug
                        Log.d(TAG, "Document data: " + document.getData());
                        
                        String url = document.getString("url");
                        Long capturedAt = document.getLong("capturedAt");
                        
                        if (url != null && !url.isEmpty()) {
                            // Tạo PhotoItem với URL và thời gian chụp
                            PhotoItem photoItem = new PhotoItem(url, capturedAt != null ? capturedAt : System.currentTimeMillis());
                            favoritePhotos.add(photoItem);
                            Log.d(TAG, "Added photo to list. URL: " + url);
                        } else {
                            Log.w(TAG, "Skipping photo with null or empty URL");
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error converting document to PhotoItem: " + e.getMessage(), e);
                    }
                }
                
                // Log số lượng ảnh đã tải
                Log.d(TAG, "Total photos loaded: " + favoritePhotos.size());
                
                // Cập nhật UI trên main thread
                runOnUiThread(this::updateUI);
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Error loading favorite images: " + e.getMessage(), e);
                showLoading(false);
                emptyView.setVisibility(View.VISIBLE);
                emptyView.setText("Không thể tải ảnh yêu thích");
            });
    }

    private void updateUI() {
        showLoading(false);
        if (favoritePhotos.isEmpty()) {
            emptyView.setVisibility(View.VISIBLE);
            gridViewFavorites.setVisibility(View.GONE);
            Log.d(TAG, "No favorite images found, showing empty view");
        } else {
            emptyView.setVisibility(View.GONE);
            gridViewFavorites.setVisibility(View.VISIBLE);
            photoAdapter.notifyDataSetChanged();
            Log.d(TAG, "Found " + favoritePhotos.size() + " favorite images, updating grid view");
        }
    }

    private void showLoading(boolean isLoading) {
        progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Reload images when returning to this activity
        loadFavoriteImages();
    }
} 