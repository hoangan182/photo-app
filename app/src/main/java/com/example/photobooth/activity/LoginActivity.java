package com.example.photobooth.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.photobooth.R;
import com.example.photobooth.controllers.UserController;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {

    private EditText editUsername, editPassword;
    private Button btnLogin;
    private TextView signInTextView;

    private FirebaseAuth mAuth;
    private UserController userController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.login_activity);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(
                    v.getPaddingLeft() + systemBars.left,
                    v.getPaddingTop(),
                    v.getPaddingRight() + systemBars.right,
                    v.getPaddingBottom()
            );
            return insets;
        });

        // Khởi tạo Firebase Auth và UserController
        mAuth = FirebaseAuth.getInstance();
        userController = new UserController();

        // Ánh xạ view
        editUsername = findViewById(R.id.editUsername);
        editPassword = findViewById(R.id.editPassword);
        btnLogin = findViewById(R.id.btnLogin);
        signInTextView = findViewById(R.id.txtSignIn);

        // Mở màn đăng ký khi chưa có tài khoản
        signInTextView.setOnClickListener(view -> {
            Intent intent = new Intent(LoginActivity.this, SignUpActivity.class);
            startActivity(intent);
        });

        // Xử lý đăng nhập
        btnLogin.setOnClickListener(view -> {
            String email = editUsername.getText().toString().trim();
            String password = editPassword.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                showDialog("Thiếu thông tin", "Vui lòng nhập đầy đủ thông tin");
                return;
            }

            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnSuccessListener(authResult -> {
                        FirebaseUser firebaseUser = authResult.getUser();
                        if (firebaseUser == null) {
                            showDialog("Lỗi", "Lỗi không xác định");
                            return;
                        }

                        userController.getUserByEmail(email,
                                user -> {
                                    showDialog("Đăng nhập thành công", "Chào " + user.getUsername());
                                    Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    startActivity(intent);
                                    finish();
                                },
                                e -> {
                                    Log.e("LoginActivity", "Không lấy được thông tin người dùng", e);
                                    showDialog("Lỗi", "Đăng nhập thành công nhưng không lấy được thông tin người dùng");
                                });

                    })
                    .addOnFailureListener(e -> {
                        String translatedMessage = translateFirebaseErrorMessage(e.getMessage());
                        showDialog("Đăng nhập thất bại", "Thông tin đăng nhập bị sai");
                    });
        });
    }

    // Hàm hiển thị dialog thông báo
    private void showDialog(String title, String message) {
        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private String translateFirebaseErrorMessage(String errorMessage) {
        if (errorMessage == null) return "Đã xảy ra lỗi không xác định";

        if (errorMessage.contains("The email address is badly formatted")) {
            return "Địa chỉ email không đúng định dạng";
        } else if (errorMessage.contains("There is no user record")) {
            return "Email không tồn tại";
        } else if (errorMessage.contains("The password is invalid")) {
            return "Mật khẩu không đúng";
        } else if (errorMessage.contains("A network error")) {
            return "Không thể kết nối mạng. Vui lòng thử lại sau";
        } else if (errorMessage.contains("We have blocked all requests")) {
            return "Tạm thời bị chặn do quá nhiều lần đăng nhập sai";
        }

        return "Lỗi: " + errorMessage;
    }
}

