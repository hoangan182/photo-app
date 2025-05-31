package com.example.photobooth.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.photobooth.R;
import com.example.photobooth.adapter.PhotoAdapter;
import com.example.photobooth.controllers.AlbumController;
import com.example.photobooth.models.Album;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AlbumDetailActivity extends AppCompatActivity {

    private TextView txtBack;
    private TextView txtAlbumTitle;
    private ImageView menuButton;
    private GridView gridView;
    private View noPhotosLayout;
    private AlbumController albumController;
    private String albumId;
    private PhotoAdapter adapter;
    private BottomSheetDialog bottomSheetDialog;

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
                                albumController.addPhotoToAlbum(albumId, bitmap);
                            } catch (IOException e) {
                                Toast.makeText(this, "Không thể thêm ảnh " + (i + 1), Toast.LENGTH_SHORT).show();
                            }
                        }
                        loadAlbumDetails(); // Reload album after adding photos
                        Toast.makeText(this, "Đã thêm " + count + " ảnh vào album", Toast.LENGTH_SHORT).show();
                    } else if (result.getData().getData() != null) {
                        // Single image selected
                        Uri imageUri = result.getData().getData();
                        try {
                            Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                            albumController.addPhotoToAlbum(albumId, bitmap);
                            loadAlbumDetails(); // Reload album after adding photo
                            Toast.makeText(this, "Đã thêm ảnh vào album", Toast.LENGTH_SHORT).show();
                        } catch (IOException e) {
                            Toast.makeText(this, "Không thể thêm ảnh", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_album_detail);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Get album ID from intent
        albumId = getIntent().getStringExtra("album_id");
        if (albumId == null) {
            Toast.makeText(this, "Album not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Initialize controller
        albumController = new AlbumController(this);

        // Initialize views
        initializeViews();

        // Set click listeners
        setupClickListeners();

        // Load album details and photos
        loadAlbumDetails();
    }

    private void initializeViews() {
        txtBack = findViewById(R.id.txtBackAlbumDetail);
        txtAlbumTitle = findViewById(R.id.txtAlbumTitle);
        menuButton = findViewById(R.id.menuButton);
        gridView = findViewById(R.id.gridViewPhotos);
        noPhotosLayout = findViewById(R.id.noPhotosLayout);

        // Initialize bottom sheet
        View bottomSheetView = getLayoutInflater().inflate(R.layout.bottom_sheet_album_options, null);
        bottomSheetDialog = new BottomSheetDialog(this);
        bottomSheetDialog.setContentView(bottomSheetView);

        // Set up bottom sheet click listeners
        TextView editOption = bottomSheetView.findViewById(R.id.editAlbumOption);
        TextView addPhotoOption = bottomSheetView.findViewById(R.id.addPhotoOption);

        editOption.setOnClickListener(v -> {
            bottomSheetDialog.dismiss();
            showEditAlbumNameDialog();
        });

        addPhotoOption.setOnClickListener(v -> {
            bottomSheetDialog.dismiss();
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
            pickImagesLauncher.launch(intent);
        });
    }

    private void setupClickListeners() {
        txtBack.setOnClickListener(v -> finish());

        menuButton.setOnClickListener(v -> bottomSheetDialog.show());

        gridView.setOnItemClickListener((parent, view, position, id) -> {
            Map<String, String> item = (Map<String, String>) parent.getItemAtPosition(position);
            String photoPath = item.get("path");
            if (photoPath != null) {
                Intent intent = new Intent(this, FullScreenImageActivity.class);
                intent.putExtra("photo_path", photoPath);
                intent.putExtra("album_id", albumId);
                startActivity(intent);
            }
        });
    }

    private void loadAlbumDetails() {
        Album album = albumController.getAlbumById(albumId);
        if (album == null) {
            Toast.makeText(this, "Album not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Set album title
        txtAlbumTitle.setText(album.getTitle());

        // Load photos
        List<String> photoPaths = album.getPhotoPaths();
        List<Map<String, String>> items = new ArrayList<>();

        if (photoPaths != null && !photoPaths.isEmpty()) {
            for (String photoPath : photoPaths) {
                Map<String, String> item = new HashMap<>();
                item.put("path", photoPath);
                items.add(item);
            }
            noPhotosLayout.setVisibility(View.GONE);
            gridView.setVisibility(View.VISIBLE);
        } else {
            noPhotosLayout.setVisibility(View.VISIBLE);
            gridView.setVisibility(View.GONE);
        }

        adapter = new PhotoAdapter(this, items);
        gridView.setAdapter(adapter);
    }

    private void showEditAlbumNameDialog() {
        Album album = albumController.getAlbumById(albumId);
        if (album == null) return;

        View dialogView = getLayoutInflater().inflate(R.layout.dialog_edit_album_name, null);
        EditText editText = dialogView.findViewById(R.id.editAlbumName);
        editText.setText(album.getTitle());

        new AlertDialog.Builder(this)
                .setTitle("Sửa tên album")
                .setView(dialogView)
                .setPositiveButton("Lưu", (dialog, which) -> {
                    String newTitle = editText.getText().toString().trim();
                    if (!newTitle.isEmpty()) {
                        album.setTitle(newTitle);
                        album.setUpdated_at(System.currentTimeMillis());
                        albumController.updateAlbum(album);
                        txtAlbumTitle.setText(newTitle);
                        Toast.makeText(this, "Đã cập nhật tên album", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Tên album không được để trống", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadAlbumDetails(); // Reload when returning from other activities
    }
} 