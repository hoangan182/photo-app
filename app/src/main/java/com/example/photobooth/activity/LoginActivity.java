package com.example.photobooth.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.photobooth.R;
import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity {

    private EditText editUsername, editPassword;
    private ImageView imgShowPassword;
    private Button btnLogin;
    private TextView txtSignIn;

    private boolean isPasswordVisible = false;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_activity); // Đảm bảo bạn đã tạo file layout tương ứng

        // Khởi tạo Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Ánh xạ view
        editUsername = findViewById(R.id.editUsername);
        editPassword = findViewById(R.id.editPassword);
        imgShowPassword = findViewById(R.id.imgShowPassword);
        btnLogin = findViewById(R.id.btnLogin);
        txtSignIn = findViewById(R.id.txtSignIn);

        // Xử lý hiện/ẩn mật khẩu
        imgShowPassword.setOnClickListener(v -> {
            if (isPasswordVisible) {
                editPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
                imgShowPassword.setImageResource(R.drawable.show_password);
                isPasswordVisible = false;
            } else {
                editPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                imgShowPassword.setImageResource(R.drawable.hide_password);
                isPasswordVisible = true;
            }
            editPassword.setSelection(editPassword.getText().length());
        });

        // Xử lý đăng nhập
        btnLogin.setOnClickListener(v -> {
            String email = editUsername.getText().toString().trim();
            String password = editPassword.getText().toString();

            if (email.isEmpty()) {
                editUsername.setError("Vui lòng nhập email");
                editUsername.requestFocus();
                return;
            }

            if (password.isEmpty()) {
                editPassword.setError("Vui lòng nhập mật khẩu");
                editPassword.requestFocus();
                return;
            }

            // Đăng nhập với Firebase Authentication
            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(LoginActivity.this, "Đăng nhập thành công", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(LoginActivity.this, MainActivity.class));
                            finish();
                        } else {
                            Toast.makeText(LoginActivity.this, "Đăng nhập thất bại: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
        });

        // Chuyển sang màn hình đăng ký
        txtSignIn.setOnClickListener(v -> startActivity(new Intent(LoginActivity.this, SignUpActivity.class)));
    }
}
