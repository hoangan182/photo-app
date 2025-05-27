package com.example.photobooth.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.photobooth.R;
import com.example.photobooth.adapter.ImageAdapter;
import com.example.photobooth.controllers.FavoriteController;

import java.util.ArrayList;
import java.util.List;

public class FavoriteActivity extends AppCompatActivity {

    private List<String> imageUrls = new ArrayList<>();
    private GridView gridViewFavorite;
    private ConstraintLayout noFavoriteImageLayout;
    private ImageView imgShowOption;
    private TextView txtBackFavorite;
    private FavoriteController favoriteController;

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
        gridViewFavorite = findViewById(R.id.gridViewFavorite);
        noFavoriteImageLayout = findViewById(R.id.noFavoriteImageLayout);
        txtBackFavorite = findViewById(R.id.txtBackAddAlbum);
        imgShowOption = findViewById(R.id.imgShowOptionFavorite);
    }

    private void setupClickListeners() {
        imgShowOption.setOnClickListener(view -> showAddOptions());

        txtBackFavorite.setOnClickListener(v -> finish());

        gridViewFavorite.setOnItemClickListener((parent, view, position, id) -> {
            Intent intent = new Intent(this, FullScreenImageActivity.class);
            intent.putExtra("imagePath", imageUrls.get(position));
            startActivityForResult(intent, 1);
        });
    }

    private void loadFavoriteImages() {
        imageUrls = favoriteController.getFavoriteImagePaths();
        updateGridView();
    }

    private void updateGridView() {
        if (imageUrls.isEmpty()) {
            noFavoriteImageLayout.setVisibility(View.VISIBLE);
            gridViewFavorite.setVisibility(View.GONE);
        } else {
            noFavoriteImageLayout.setVisibility(View.GONE);
            gridViewFavorite.setVisibility(View.VISIBLE);
            ImageAdapter adapter = new ImageAdapter(this, imageUrls);
            gridViewFavorite.setAdapter(adapter);
        }
    }

    private void showAddOptions() {
        // This method is kept for future implementation
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK) {
            // Reload favorite images after returning from FullScreenImageActivity
            loadFavoriteImages();
        }
    }
}