package com.example.photobooth.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.example.photobooth.R;
import com.example.photobooth.controllers.ImageStorageController;
import com.example.photobooth.services.ImageUploadService;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class ImageEditActivity extends AppCompatActivity {

    private ImageView imageView;
    private ImageView backButton;
    private ImageView saveButton;
    private SeekBar brightnessSeekBar;
    private SeekBar contrastSeekBar;
    private SeekBar saturationSeekBar;
    private String imagePath;
    private Bitmap originalBitmap;
    private Bitmap editedBitmap;
    private ImageStorageController imageStorageController;
    private float brightness = 0f;
    private float contrast = 1f;
    private float saturation = 1f;
    private ImageUploadService imageUploadService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_image_edit);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize controllers
        imageStorageController = new ImageStorageController(this);
        imageUploadService = new ImageUploadService(this);

        // Initialize views
        initializeViews();

        // Get image path from intent
        imagePath = getIntent().getStringExtra("imagePath");
        if (imagePath != null) {
            loadImage();
        } else {
            Toast.makeText(this, "Error: No image path provided", Toast.LENGTH_SHORT).show();
            finish();
        }

        // Set click listeners
        setupClickListeners();

        // Set up seekbar listeners
        setupSeekBarListeners();
    }

    private void initializeViews() {
        imageView = findViewById(R.id.editImageView);
        backButton = findViewById(R.id.backButton);
        saveButton = findViewById(R.id.saveButton);
        brightnessSeekBar = findViewById(R.id.brightnessSeekBar);
        contrastSeekBar = findViewById(R.id.contrastSeekBar);
        saturationSeekBar = findViewById(R.id.saturationSeekBar);
    }

    private void loadImage() {
        try {
            // Load image from URL using Glide
            Glide.with(this)
                .asBitmap()
                .load(imagePath)
                .into(new CustomTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(Bitmap bitmap, Transition<? super Bitmap> transition) {
                        originalBitmap = bitmap;
                        editedBitmap = originalBitmap.copy(originalBitmap.getConfig(), true);
                        imageView.setImageBitmap(editedBitmap);
                    }

                    @Override
                    public void onLoadCleared(@Nullable Drawable placeholder) {
                        Toast.makeText(ImageEditActivity.this, "Error loading image", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                });
        } catch (Exception e) {
            Toast.makeText(this, "Error loading image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void setupClickListeners() {
        backButton.setOnClickListener(v -> {
            finish();
        });

        saveButton.setOnClickListener(v -> {
            saveEditedImage();
        });
    }

    private void setupSeekBarListeners() {
        brightnessSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                brightness = (progress - 100) / 100f;
                applyFilters();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        contrastSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                contrast = progress / 100f;
                applyFilters();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        saturationSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                saturation = progress / 100f;
                applyFilters();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }

    private void applyFilters() {
        if (originalBitmap == null) return;

        // Create a new bitmap for the edited image
        editedBitmap = originalBitmap.copy(originalBitmap.getConfig(), true);
        Canvas canvas = new Canvas(editedBitmap);
        Paint paint = new Paint();

        // Create color matrix for brightness, contrast and saturation
        ColorMatrix colorMatrix = new ColorMatrix();
        
        // Apply brightness
        ColorMatrix brightnessMatrix = new ColorMatrix();
        brightnessMatrix.set(new float[] {
            1, 0, 0, 0, brightness,
            0, 1, 0, 0, brightness,
            0, 0, 1, 0, brightness,
            0, 0, 0, 1, 0
        });
        colorMatrix.postConcat(brightnessMatrix);

        // Apply contrast
        ColorMatrix contrastMatrix = new ColorMatrix();
        contrastMatrix.set(new float[] {
            contrast, 0, 0, 0, 0,
            0, contrast, 0, 0, 0,
            0, 0, contrast, 0, 0,
            0, 0, 0, 1, 0
        });
        colorMatrix.postConcat(contrastMatrix);

        // Apply saturation
        ColorMatrix saturationMatrix = new ColorMatrix();
        saturationMatrix.setSaturation(saturation);
        colorMatrix.postConcat(saturationMatrix);

        paint.setColorFilter(new ColorMatrixColorFilter(colorMatrix));
        canvas.drawBitmap(originalBitmap, 0, 0, paint);

        // Update the image view
        imageView.setImageBitmap(editedBitmap);
    }

    private void saveEditedImage() {
        try {
            // Create a temporary file to store the edited bitmap
            File tempFile = File.createTempFile("edited_", ".jpg", getCacheDir());
            FileOutputStream out = new FileOutputStream(tempFile);
            editedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
            out.flush();
            out.close();

            // Upload the edited image
            Uri imageUri = Uri.fromFile(tempFile);
            imageUploadService.uploadImage(imageUri, "image/jpeg", new ImageUploadService.UploadCallback() {
                @Override
                public void onSuccess(String imageUrl) {
                    runOnUiThread(() -> {
                        // Pass the edited image URL back to ImageDetailActivity
                        Intent resultIntent = new Intent();
                        resultIntent.putExtra("editedImageUrl", imageUrl);
                        setResult(RESULT_OK, resultIntent);
                        Toast.makeText(ImageEditActivity.this, "Image saved successfully", Toast.LENGTH_SHORT).show();
                        
                        // Navigate to main activity and clear all previous activities
                        Intent mainIntent = new Intent(ImageEditActivity.this, MainActivity.class);
                        mainIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(mainIntent);
                        finish();
                    });
                }

                @Override
                public void onError(String error) {
                    runOnUiThread(() -> {
                        Toast.makeText(ImageEditActivity.this, "Failed to save image: " + error, Toast.LENGTH_SHORT).show();
                    });
                }

                @Override
                public void onProgress(int progress) {
                    // Show progress if needed
                }
            });
        } catch (IOException e) {
            Toast.makeText(this, "Failed to save image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (originalBitmap != null && !originalBitmap.isRecycled()) {
            originalBitmap.recycle();
        }
        if (editedBitmap != null && !editedBitmap.isRecycled()) {
            editedBitmap.recycle();
        }
    }
} 