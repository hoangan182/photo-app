package com.example.photobooth.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.photobooth.R;
import com.example.photobooth.adapter.ImageAdapter;
import com.example.photobooth.controllers.TrashController;
import com.example.photobooth.models.PhotoItem;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.util.ArrayList;
import java.util.List;

public class TrashActivity extends AppCompatActivity {
    private List<PhotoItem> photos;
    private TrashController trashController;
    private ImageAdapter adapter;

    private GridView gridViewTrash;
    private ImageView imgShowOption;
    private ConstraintLayout noTrashImageLayout;
    private TextView txtBackTrash;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_trash);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize collections
        photos = new ArrayList<>();

        // Initialize views
        initializeViews();

        // Initialize controllers
        trashController = new TrashController(this);

        // Set click listeners
        setupClickListeners();

        // Load trash images
        loadTrashImages();
    }

    private void initializeViews() {
        gridViewTrash = findViewById(R.id.gridViewTrash);
        noTrashImageLayout = findViewById(R.id.noTrashImageLayout);
        imgShowOption = findViewById(R.id.imgTrashPageOption);
        txtBackTrash = findViewById(R.id.txtBackTrash);

        // Set initial visibility
        if (gridViewTrash != null) {
            gridViewTrash.setVisibility(View.VISIBLE);
        }
        if (noTrashImageLayout != null) {
            noTrashImageLayout.setVisibility(View.GONE);
        }
    }

    private void setupClickListeners() {
        if (txtBackTrash != null) {
            txtBackTrash.setOnClickListener(v -> finish());
        }

        if (imgShowOption != null) {
            imgShowOption.setOnClickListener(v -> showTrashOption());
        }

        if (gridViewTrash != null) {
            // Click to view full screen
            gridViewTrash.setOnItemClickListener((parent, view, position, id) -> {
                PhotoItem photo = photos.get(position);
                Intent intent = new Intent(this, FullScreenImageActivity.class);
                intent.putExtra("imagePath", photo.getPath());
                intent.putExtra("isTrash", true);
                startActivityForResult(intent, 1);
            });

            // Long press to show delete/restore options
            gridViewTrash.setOnItemLongClickListener((parent, view, position, id) -> {
                PhotoItem photo = photos.get(position);
                showPhotoOptionsDialog(photo);
                return true;
            });
        }
    }

    private void loadTrashImages() {
        List<String> imagePaths = trashController.getTrashImagePaths();
        photos.clear();
        for (String path : imagePaths) {
            photos.add(new PhotoItem(path, System.currentTimeMillis())); // Using current time as capture date
        }
        updateGridView();
    }

    private void updateGridView() {
        if (photos.isEmpty()) {
            noTrashImageLayout.setVisibility(View.VISIBLE);
            gridViewTrash.setVisibility(View.GONE);
        } else {
            noTrashImageLayout.setVisibility(View.GONE);
            gridViewTrash.setVisibility(View.VISIBLE);
            adapter = new ImageAdapter(this, photos);
            gridViewTrash.setAdapter(adapter);
        }
    }

    private void showTrashOption() {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);
        View view = getLayoutInflater().inflate(R.layout.trash_page_option, null);
        bottomSheetDialog.setContentView(view);

        TextView deleteAll = view.findViewById(R.id.optionDeleteAllPhoto);
        TextView restoreAll = view.findViewById(R.id.optionRestoreAllPhoto);

        deleteAll.setOnClickListener(v -> {
            bottomSheetDialog.dismiss();
            showDeleteAllConfirmationDialog();
        });

        restoreAll.setOnClickListener(v -> {
            bottomSheetDialog.dismiss();
            showRestoreAllConfirmationDialog();
        });

        bottomSheetDialog.show();
    }

    private void showDeleteAllConfirmationDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Xóa tất cả ảnh")
                .setMessage("Bạn có chắc chắn muốn xóa vĩnh viễn tất cả ảnh trong thùng rác?")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    trashController.deleteAllImages();
                    loadTrashImages();
                    Toast.makeText(this, "Đã xóa tất cả ảnh", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void showRestoreAllConfirmationDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Khôi phục tất cả ảnh")
                .setMessage("Bạn có chắc chắn muốn khôi phục tất cả ảnh từ thùng rác?")
                .setPositiveButton("Khôi phục", (dialog, which) -> {
                    trashController.restoreAllImages();
                    loadTrashImages();
                    Toast.makeText(this, "Đã khôi phục tất cả ảnh", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void showPhotoOptionsDialog(PhotoItem photo) {
        String[] options = {"Khôi phục", "Xóa vĩnh viễn"};
        new AlertDialog.Builder(this)
                .setTitle("Tùy chọn")
                .setItems(options, (dialog, which) -> {
                    switch (which) {
                        case 0: // Restore
                            if (trashController.restoreImage(photo.getPath())) {
                                Toast.makeText(this, "Đã khôi phục ảnh", Toast.LENGTH_SHORT).show();
                                loadTrashImages();
                            } else {
                                Toast.makeText(this, "Không thể khôi phục ảnh", Toast.LENGTH_SHORT).show();
                            }
                            break;
                        case 1: // Delete permanently
                            new AlertDialog.Builder(this)
                                    .setTitle("Xóa vĩnh viễn")
                                    .setMessage("Bạn có chắc chắn muốn xóa vĩnh viễn ảnh này?")
                                    .setPositiveButton("Xóa", (dialogInterface, i) -> {
                                        if (trashController.permanentlyDeleteImage(photo.getPath())) {
                                            Toast.makeText(this, "Đã xóa vĩnh viễn ảnh", Toast.LENGTH_SHORT).show();
                                            loadTrashImages();
                                        } else {
                                            Toast.makeText(this, "Không thể xóa ảnh", Toast.LENGTH_SHORT).show();
                                        }
                                    })
                                    .setNegativeButton("Hủy", null)
                                    .show();
                            break;
                    }
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK) {
            // Reload trash images after returning from FullScreenImageActivity
            loadTrashImages();
        }
    }
}
