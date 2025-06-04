package com.example.photobooth.activity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.GridView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.photobooth.R;
import com.example.photobooth.adapter.FirestoreImageAdapter;
import com.example.photobooth.models.Image;
import com.example.photobooth.services.FirestoreImageService;

import java.util.ArrayList;
import java.util.List;

public class FirestoreImagesActivity extends AppCompatActivity {
    private static final String TAG = "FirestoreImagesActivity";
    private GridView gridView;
    private ProgressBar progressBar;
    private TextView errorText;
    private FirestoreImageAdapter adapter;
    private FirestoreImageService imageService;
    private List<Image> images;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_firestore_images);

        Log.d(TAG, "Initializing FirestoreImagesActivity");

        // Initialize views
        gridView = findViewById(R.id.gridView);
        progressBar = findViewById(R.id.progressBar);
        errorText = findViewById(R.id.errorText);

        // Initialize service and data
        imageService = new FirestoreImageService();
        images = new ArrayList<>();
        adapter = new FirestoreImageAdapter(this, images);
        gridView.setAdapter(adapter);

        // Load images
        loadImages();
    }

    private void loadImages() {
        Log.d(TAG, "Loading images from Firestore");
        showLoading(true);
        imageService.getImages(new FirestoreImageService.ImageCallback() {
            @Override
            public void onSuccess(List<Image> loadedImages) {
                Log.d(TAG, "Successfully loaded " + loadedImages.size() + " images");
                images.clear();
                images.addAll(loadedImages);
                adapter.notifyDataSetChanged();
                showLoading(false);

                if (loadedImages.isEmpty()) {
                    showError("No images found");
                }
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "Error loading images: " + error);
                showError(error);
                showLoading(false);
                Toast.makeText(FirestoreImagesActivity.this, error, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void showLoading(boolean isLoading) {
        Log.d(TAG, "Setting loading state: " + isLoading);
        progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        gridView.setVisibility(isLoading ? View.GONE : View.VISIBLE);
        errorText.setVisibility(View.GONE);
    }

    private void showError(String error) {
        Log.e(TAG, "Showing error: " + error);
        errorText.setText(error);
        errorText.setVisibility(View.VISIBLE);
        gridView.setVisibility(View.GONE);
    }
} 