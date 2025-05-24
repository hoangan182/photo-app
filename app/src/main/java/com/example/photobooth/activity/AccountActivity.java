//package com.example.photobooth.activity;
//
//import android.content.Intent;
//import android.os.Bundle;
//import android.view.View;
//import android.widget.Button;
//import android.widget.TextView;
//
//import androidx.activity.EdgeToEdge;
//import androidx.appcompat.app.AlertDialog;
//import androidx.appcompat.app.AppCompatActivity;
//import androidx.core.graphics.Insets;
//import androidx.core.view.ViewCompat;
//import androidx.core.view.WindowInsetsCompat;
//
//import com.example.photobooth.R;
//import com.google.firebase.auth.FirebaseAuth;
//
//public class AccountActivity extends AppCompatActivity {
//
//    TextView txtBackAccount;
//    Button btnChangePassword, btnChangeInfo, btnLogout;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        EdgeToEdge.enable(this);
//        setContentView(R.layout.activity_account);
//
//        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
//            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
//            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
//            return insets;
//        });
//
//        txtBackAccount = findViewById(R.id.txtBackAccount);
//        btnChangePassword = findViewById(R.id.btnChangePassword);
//        btnChangeInfo = findViewById(R.id.btnChangeInfo);
//        btnLogout = findViewById(R.id.btnLogout);
//
//        txtBackAccount.setOnClickListener(view -> {
//            Intent intent = new Intent(AccountActivity.this, HomeActivity.class);
//            startActivity(intent);
//        });
//
//        btnChangePassword.setOnClickListener(view -> {
//            Intent intent = new Intent(AccountActivity.this, ChangePasswordActivity.class);
//            startActivity(intent);
//        });
//
//        btnChangeInfo.setOnClickListener(view -> {
//            Intent intent = new Intent(AccountActivity.this, ChangeInformationActivity.class);
//            startActivity(intent);
//        });
//
//        btnLogout.setOnClickListener(view -> showLogoutDialog());
//    }
//
//    // Hàm hiển thị dialog xác nhận đăng xuất
//    private void showLogoutDialog() {
//        new AlertDialog.Builder(this)
//                .setTitle("Đăng xuất")
//                .setMessage("Bạn có chắc chắn muốn đăng xuất không?")
//                .setPositiveButton("Đăng xuất", (dialog, which) -> {
//                    // Nếu dùng Firebase Auth, có thể gọi signOut()
//                    FirebaseAuth.getInstance().signOut();
//
//                    Intent intent = new Intent(AccountActivity.this, LoginActivity.class);
//                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
//                    startActivity(intent);
//                    finish();
//                })
//                .setNegativeButton("Hủy", (dialog, which) -> dialog.dismiss())
//                .show();
//    }
//}
