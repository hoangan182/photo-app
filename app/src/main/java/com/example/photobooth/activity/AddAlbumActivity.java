package com.example.photobooth.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.photobooth.R;
import com.example.photobooth.adapter.SelectedImagesAdapter;
import com.example.photobooth.controllers.AlbumController;
import com.example.photobooth.controllers.ImagePickerController;
import com.example.photobooth.models.Album;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class AddAlbumActivity extends AppCompatActivity implements ImagePickerController.ImagePickerCallback {

    private EditText albumNameEditText;
    private Button selectPhotoButton;
    private Button createAlbumButton;
    private ImageView albumCoverPreview;
    private GridView selectedImagesGrid;
    private AlbumController albumController;
    private ImagePickerController imagePickerController;
    private Bitmap selectedCoverImage;
    private List<Bitmap> selectedImages;
    private SelectedImagesAdapter selectedImagesAdapter;

    private final ActivityResultLauncher<Intent> pickImagesLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    if (result.getData().getClipData() != null) {
                        // Multiple images selected
                        int count = result.getData().getClipData().getItemCount();
                        for (int i = 0; i < count; i++) {
                            Uri imageUri = result.getData().getClipData().getItemAt(i).getUri();
                            try {
                                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                                selectedImages.add(bitmap);
                                // Use first image as cover
                                if (i == 0) {
                                    selectedCoverImage = bitmap;
                                    albumCoverPreview.setImageBitmap(bitmap);
                                    albumCoverPreview.setVisibility(View.VISIBLE);
                                }
                            } catch (IOException e) {
                                Toast.makeText(this, "Failed to load image " + (i + 1), Toast.LENGTH_SHORT).show();
                            }
                        }
                    } else if (result.getData().getData() != null) {
                        // Single image selected
                        Uri imageUri = result.getData().getData();
                        try {
                            Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                            selectedImages.add(bitmap);
                            selectedCoverImage = bitmap;
                            albumCoverPreview.setImageBitmap(bitmap);
                            albumCoverPreview.setVisibility(View.VISIBLE);
                        } catch (IOException e) {
                            Toast.makeText(this, "Failed to load image", Toast.LENGTH_SHORT).show();
                        }
                    }
                    selectedImagesAdapter.notifyDataSetChanged();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_abum);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.addalbum), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize collections
        selectedImages = new ArrayList<>();

        // Initialize controllers
        albumController = new AlbumController(this);
        imagePickerController = new ImagePickerController(this, this);

        // Initialize views
        initializeViews();

        // Set click listeners
        setupClickListeners();

        // Setup keyboard handling
        setupKeyboardHandling();
    }

    private void initializeViews() {
        albumNameEditText = findViewById(R.id.textView8);
        selectPhotoButton = findViewById(R.id.selectPhotoButton);
        createAlbumButton = findViewById(R.id.button6);
        albumCoverPreview = findViewById(R.id.albumCoverPreview);
        selectedImagesGrid = findViewById(R.id.selectedImagesGrid);

        // Setup grid view for selected images
        selectedImagesAdapter = new SelectedImagesAdapter(this, selectedImages);
        selectedImagesGrid.setAdapter(selectedImagesAdapter);

        // Setup EditText
        albumNameEditText.setImeOptions(android.view.inputmethod.EditorInfo.IME_ACTION_DONE);
        albumNameEditText.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_DONE) {
                hideKeyboard();
                return true;
            }
            return false;
        });

        findViewById(R.id.txtBackAddAlbum).setOnClickListener(v -> finish());
    }

    private void setupKeyboardHandling() {
        // Hide keyboard when touching outside EditText
        View rootView = findViewById(R.id.addalbum);
        rootView.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                hideKeyboard();
            }
            return false;
        });

        // Hide keyboard when scrolling
        selectedImagesGrid.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                hideKeyboard();
            }
            return false;
        });
    }

    private void hideKeyboard() {
        View view = getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            view.clearFocus();
        }
    }

    private void setupClickListeners() {
        selectPhotoButton.setOnClickListener(v -> {
            if (imagePickerController.hasPermission()) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                pickImagesLauncher.launch(intent);
            } else {
                imagePickerController.requestPermission();
            }
        });

        createAlbumButton.setOnClickListener(v -> createAlbum());
    }

    private void createAlbum() {
        String albumName = albumNameEditText.getText().toString().trim();
        
        if (albumName.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập tên album", Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedImages.isEmpty()) {
            Toast.makeText(this, "Vui lòng chọn ít nhất một ảnh", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create album with first image as cover
        albumController.createAlbum(albumName, "", selectedCoverImage, new AlbumController.OnAlbumCreatedListener() {
            @Override
            public void onSuccess(Album album) {
                // Save all selected images to album
                int[] successCount = {0};
                int totalImages = selectedImages.size();
                
                for (Bitmap image : selectedImages) {
                    albumController.addPhotoToAlbum(album.getId(), image, new AlbumController.OnPhotoAddedListener() {
                        @Override
                        public void onSuccess() {
                            successCount[0]++;
                            if (successCount[0] == totalImages) {
                                runOnUiThread(() -> {
                                    Toast.makeText(AddAlbumActivity.this, "Đã tạo album thành công", Toast.LENGTH_SHORT).show();
                                    finish();
                                });
                            }
                        }

                        @Override
                        public void onFailure(String error) {
                            Log.e("AddAlbumActivity", "Failed to add photo: " + error);
                            successCount[0]++;
                            if (successCount[0] == totalImages) {
                                runOnUiThread(() -> {
                                    Toast.makeText(AddAlbumActivity.this, "Đã tạo album nhưng một số ảnh không thể thêm vào", Toast.LENGTH_SHORT).show();
                                    finish();
                                });
                            }
                        }
                    });
                }
            }

            @Override
            public void onFailure(String error) {
                runOnUiThread(() -> {
                    Toast.makeText(AddAlbumActivity.this, "Không thể tạo album: " + error, Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    @Override
    public void onImagePicked(Uri imageUri) {
        // Not used in this activity
    }

    @Override
    public void onPermissionDenied() {
        Toast.makeText(this, "Permission denied to access gallery", Toast.LENGTH_SHORT).show();
    }
}