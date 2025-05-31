package com.example.photobooth.activity;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.example.photobooth.R;
import com.example.photobooth.controllers.UserController;
import com.example.photobooth.models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.UUID;

public class ChangeInformationActivity extends AppCompatActivity {
    private Button btnSaveInfo;
    private EditText editChangeUsername;
    private ImageView imgAvatar;
    private TextView txtChangeAvatar;
    private UserController userController;
    private User currentUser;
    private Uri selectedImageUri;
    private FirebaseStorage storage;
    private StorageReference storageRef;

    private final ActivityResultLauncher<Intent> pickImage = registerForActivityResult(
        new ActivityResultContracts.StartActivityForResult(),
        result -> {
            if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                selectedImageUri = result.getData().getData();
                // Hiển thị ảnh đã chọn
                Glide.with(this)
                    .load(selectedImageUri)
                    .circleCrop()
                    .into(imgAvatar);
            }
        }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_change_information);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Khởi tạo Firebase Storage
        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();

        // Khởi tạo UserController
        userController = new UserController();

        // Ánh xạ view
        btnSaveInfo = findViewById(R.id.btnSaveInfo);
        editChangeUsername = findViewById(R.id.editChangeUsername);
        imgAvatar = findViewById(R.id.imgAvatar);
        txtChangeAvatar = findViewById(R.id.txtChangeAvartar);
        TextView txtBack = findViewById(R.id.txtBackChangeInfo);

        // Load thông tin người dùng hiện tại
        loadCurrentUserInfo();

        // Xử lý nút back
        txtBack.setOnClickListener(v -> finish());

        // Xử lý thay đổi avatar
        txtChangeAvatar.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            pickImage.launch(intent);
        });

        // Xử lý lưu thông tin
        btnSaveInfo.setOnClickListener(v -> saveUserInfo());
    }

    private void loadCurrentUserInfo() {
        // Hiển thị dialog loading
        AlertDialog loadingDialog = new AlertDialog.Builder(this)
                .setMessage("Đang tải thông tin...")
                .setCancelable(false)
                .create();
        loadingDialog.show();

        userController.getCurrentUserFromFirestore(
            user -> {
                if (user != null) {
                    currentUser = user;
                    editChangeUsername.setText(user.getUsername());
                    
                    // Hiển thị avatar hiện tại
                    if (user.getAvatar() != null && !user.getAvatar().isEmpty()) {
                        Glide.with(this)
                            .load(user.getAvatar())
                            .placeholder(R.drawable.baseline_account_circle_24)
                            .error(R.drawable.baseline_account_circle_24)
                            .circleCrop()
                            .into(imgAvatar);
                    } else {
                        imgAvatar.setImageResource(R.drawable.baseline_account_circle_24);
                    }
                }
                loadingDialog.dismiss();
            },
            e -> {
                loadingDialog.dismiss();
                Toast.makeText(this, "Không thể tải thông tin người dùng: " + e.getMessage(), 
                    Toast.LENGTH_SHORT).show();
            }
        );
    }

    private void saveUserInfo() {
        String newUsername = editChangeUsername.getText().toString().trim();

        if (newUsername.isEmpty()) {
            editChangeUsername.setError("Vui lòng nhập tên người dùng");
            editChangeUsername.requestFocus();
            return;
        }

        // Hiển thị dialog loading
        AlertDialog loadingDialog = new AlertDialog.Builder(this)
                .setMessage("Đang lưu thông tin...")
                .setCancelable(false)
                .create();
        loadingDialog.show();

        // Nếu có ảnh mới được chọn, upload lên Firebase Storage
        if (selectedImageUri != null) {
            String imageId = UUID.randomUUID().toString();
            StorageReference imageRef = storageRef.child("avatars/" + imageId);

            UploadTask uploadTask = imageRef.putFile(selectedImageUri);
            uploadTask.continueWithTask(task -> {
                if (!task.isSuccessful()) {
                    throw task.getException();
                }
                return imageRef.getDownloadUrl();
            }).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Uri downloadUri = task.getResult();
                    updateUserInfo(newUsername, downloadUri.toString(), loadingDialog);
                } else {
                    loadingDialog.dismiss();
                    Toast.makeText(this, "Không thể tải lên ảnh: " + task.getException().getMessage(),
                        Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            // Nếu không có ảnh mới, chỉ cập nhật username
            updateUserInfo(newUsername, currentUser.getAvatar(), loadingDialog);
        }
    }

    private void updateUserInfo(String newUsername, String avatarUrl, AlertDialog loadingDialog) {
        currentUser.setUsername(newUsername);
        currentUser.setAvatar(avatarUrl);
        currentUser.setUpdated_at(System.currentTimeMillis());

        userController.updateUser(currentUser,
            unused -> {
                loadingDialog.dismiss();
                Toast.makeText(this, "Cập nhật thông tin thành công", Toast.LENGTH_SHORT).show();
                finish();
            },
            e -> {
                loadingDialog.dismiss();
                Toast.makeText(this, "Không thể cập nhật thông tin: " + e.getMessage(),
                    Toast.LENGTH_SHORT).show();
            }
        );
    }
}