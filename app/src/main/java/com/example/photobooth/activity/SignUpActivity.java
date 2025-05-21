package com.example.photobooth.activity;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.photobooth.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SignUpActivity extends AppCompatActivity {

    private EditText edtUsername, edtEmail, edtPassword, edtRetypePassword;
    private Button btnSignUp;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        // Khởi tạo Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Ánh xạ các view
        edtUsername = findViewById(R.id.editUsername);
        edtEmail = findViewById(R.id.editEmail);
        edtPassword = findViewById(R.id.editPassword);
        edtRetypePassword = findViewById(R.id.editRetypePassword);
        btnSignUp = findViewById(R.id.btnSignUp);

        // Xử lý sự kiện click nút Đăng ký
        btnSignUp.setOnClickListener(view -> {
            String username = edtUsername.getText().toString().trim();
            String email = edtEmail.getText().toString().trim();
            String password = edtPassword.getText().toString().trim();
            String retypePassword = edtRetypePassword.getText().toString().trim();

            // Kiểm tra các trường nhập liệu
            if (username.isEmpty() || email.isEmpty() || password.isEmpty() || retypePassword.isEmpty()) {
                Toast.makeText(SignUpActivity.this, "Vui lòng điền đầy đủ thông tin!", Toast.LENGTH_SHORT).show();
                return;
            }

            // Kiểm tra mật khẩu và xác nhận mật khẩu có trùng nhau không
            if (!password.equals(retypePassword)) {
                Toast.makeText(SignUpActivity.this, "Mật khẩu và xác nhận mật khẩu không trùng khớp!", Toast.LENGTH_SHORT).show();
                return;
            }

            // Tiến hành đăng ký người dùng với Firebase Authentication
            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnSuccessListener(authResult -> {
                        FirebaseUser firebaseUser = authResult.getUser();
                        if (firebaseUser != null) {
                            // Nếu đăng ký thành công, lưu thông tin người dùng
                            String userId = firebaseUser.getUid();
                            Toast.makeText(SignUpActivity.this, "Đăng ký thành công", Toast.LENGTH_SHORT).show();

                            // Bạn có thể thêm mã để lưu thông tin người dùng vào Firestore ở đây

                            // Sau khi đăng ký thành công, chuyển về màn hình đăng nhập
                            finish();
                        }
                    })
                    .addOnFailureListener(e -> {
                        // Nếu có lỗi khi đăng ký
                        Log.e("SignUpActivity", "Đăng ký thất bại", e);
                        Toast.makeText(SignUpActivity.this, "Đăng ký thất bại: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    });
        });
    }
}
