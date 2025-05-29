package com.example.photobooth.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.text.Html;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
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
    private ImageView imgShowPassword;
    private Button btnLogin;
    private TextView txtSignIn;
    private TextView signUpTextView;
    private FirebaseAuth mAuth;
    private UserController userController;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_activity); // Đảm bảo bạn đã tạo file layout tương ứng

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0);
            return insets;
        });

        // Khởi tạo Firebase Auth và UserController
        mAuth = FirebaseAuth.getInstance();

        // Ánh xạ view
        editUsername = findViewById(R.id.editUsername);
        editPassword = findViewById(R.id.editPassword);
        imgShowPassword = findViewById(R.id.imgShowPassword);
        btnLogin = findViewById(R.id.btnLogin);
        signUpTextView = findViewById(R.id.txtSignUp);
        signUpTextView.setText(Html.fromHtml(getString(R.string.sign_up_link)));
        signUpTextView.setMovementMethod(LinkMovementMethod.getInstance());

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
