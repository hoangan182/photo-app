package com.example.photobooth.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

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
    boolean isPasswordVisible = false;

    private EditText editUsername, editPassword;
    private Button btnLogin;
    private TextView signInTextView;

    ImageView imgShowPassword;

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
        imgShowPassword = findViewById(R.id.imgShowPassword);

        imgShowPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isPasswordVisible) {
                    // Ẩn mật khẩu
                    editPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    imgShowPassword.setImageResource(R.drawable.show_password);
                } else {
                    // Hiện mật khẩu
                    editPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                    imgShowPassword.setImageResource(R.drawable.hide_password);
                }
                isPasswordVisible = !isPasswordVisible;
                editPassword.setSelection(editPassword.getText().length());
            }
        });




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
                     // Chuyển sang màn chính (HomeActivity chẳng hạn)
//                            Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
//                            startActivity(intent);
//                            finish();

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

