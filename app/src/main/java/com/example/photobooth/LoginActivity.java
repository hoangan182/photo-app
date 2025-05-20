package com.example.photobooth;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.photobooth.controllers.UserController;
import com.example.photobooth.models.User;
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

//            if (email.isEmpty() || password.isEmpty()) {
//                Toast.makeText(LoginActivity.this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
//                return;
//            }

            mAuth.signInWithEmailAndPassword("huogthu5521@gmail.com", "123456")
                    .addOnSuccessListener(authResult -> {
                     // Chuyển sang màn chính (HomeActivity chẳng hạn)
//                            Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
//                            startActivity(intent);
//                            finish();

                        FirebaseUser firebaseUser = authResult.getUser();
                        if (firebaseUser == null) {
                            Toast.makeText(LoginActivity.this, "Lỗi không xác định", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        String userId = firebaseUser.getUid();
                        Log.e("heheheheheheheheheh", userId);
                        // Lấy thông tin user từ Firestore
                        userController.getUserById(userId,
                                user -> {
                                    Toast.makeText(LoginActivity.this, "Đăng nhập thành công! Chào " + user.getUsername(), Toast.LENGTH_SHORT).show();

                                    // Chuyển sang màn chính (HomeActivity chẳng hạn)
                                    Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
                                    startActivity(intent);
                                    finish();
                                },
                                e -> {
                                    Log.e("LoginActivity", "Không lấy được thông tin người dùng", e);
                                    Toast.makeText(LoginActivity.this, "Đăng nhập thành công nhưng không lấy được thông tin người dùng", Toast.LENGTH_LONG).show();
                                });

                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(LoginActivity.this, "Đăng nhập thất bại: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    });
        });
    }
}
