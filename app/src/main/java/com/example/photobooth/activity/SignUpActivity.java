package com.example.photobooth.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.photobooth.R;
import com.example.photobooth.controllers.UserController;
import com.example.photobooth.models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SignUpActivity extends Activity {
    TextView txtLogin1;
    Button btnSignUp;
    EditText edtEmail, edtPassword, edtUsername, edtRetypePassword;

    FirebaseAuth mAuth;
    UserController userController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

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

        mAuth = FirebaseAuth.getInstance();
        userController = new UserController();

        // Ánh xạ View
        txtLogin1 = findViewById(R.id.txtLogin1);
        btnSignUp = findViewById(R.id.btnSignUp);
        edtEmail = findViewById(R.id.edtEmail);
        edtPassword = findViewById(R.id.edtPassword);
        edtRetypePassword = findViewById(R.id.edtRetypePassword);
        edtUsername = findViewById(R.id.edtUsername);

        txtLogin1.setOnClickListener(v -> finish());

        btnSignUp.setOnClickListener(v -> performSignUp());
    }

    private void performSignUp() {
        String email = edtEmail.getText().toString().trim();
        String password = edtPassword.getText().toString().trim();
        String retypePassword = edtRetypePassword.getText().toString().trim();
        String username = edtUsername.getText().toString().trim();

        // Kiểm tra rỗng
        if (email.isEmpty() || password.isEmpty() || retypePassword.isEmpty() || username.isEmpty()) {
            showDialog("Lỗi", "Vui lòng điền đầy đủ thông tin.");
            return;
        }

        // Kiểm tra mật khẩu khớp nhau
        if (!password.equals(retypePassword)) {
            showDialog("Lỗi", "Mật khẩu và xác nhận mật khẩu không khớp.");
            return;
        }

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    FirebaseUser firebaseUser = mAuth.getCurrentUser();
                    if (firebaseUser != null) {
                        User user = new User();
                        user.setId(firebaseUser.getUid());
                        user.setEmail(email);
                        user.setUsername(username);
                        user.setCreated_at(System.currentTimeMillis());
                        user.setUpdated_at(System.currentTimeMillis());

                        userController.createUser(user,
                                unused -> {
                                    showDialog("Thành công", "Đăng ký thành công.");
                                    finish();
                                },
                                e -> showDialog("Lỗi", "Đăng ký thất bại: " + e.getMessage()));
                    }
                })
                .addOnFailureListener(e -> showDialog("Lỗi", "Tạo tài khoản thất bại: " + e.getMessage()));
    }

    private void showDialog(String title, String message) {
        new AlertDialog.Builder(SignUpActivity.this)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("OK", null)
                .show();
    }
}
