package com.example.photobooth.activity;

import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.photobooth.R;

public class ChangePasswordActivity extends AppCompatActivity {

    boolean isPasswordVisible = false;

    Button btnSavePassword;

    ImageView imgShowOldPassword, imgShowNewPassword, imgShowRetypeNewPassword;

    EditText editNewPassword, editOldPassword, editRetypeNewPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_change_password);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        btnSavePassword = findViewById(R.id.btnSavePassword);
        imgShowOldPassword = findViewById(R.id.imgShowOldPassword);
        imgShowNewPassword = findViewById(R.id.imgShowNewPassword);
        imgShowRetypeNewPassword = findViewById(R.id.imgShowRetypeNewPassword);
        editNewPassword = findViewById(R.id.editNewPassword);
        editOldPassword = findViewById(R.id.editOldPassword);
        editRetypeNewPassword = findViewById(R.id.editRetypeNewPassword);

        findViewById(R.id.txtBackChangePassword).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        btnSavePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        imgShowOldPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isPasswordVisible) {
                    // Ẩn mật khẩu
                    editOldPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    imgShowOldPassword.setImageResource(R.drawable.show_password);
                } else {
                    // Hiện mật khẩu
                    editOldPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                    imgShowOldPassword.setImageResource(R.drawable.hide_password);
                }
                isPasswordVisible = !isPasswordVisible;
                editOldPassword.setSelection(editOldPassword.getText().length());
            }
        });

        imgShowNewPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isPasswordVisible) {
                    // Ẩn mật khẩu
                    editNewPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    imgShowNewPassword.setImageResource(R.drawable.show_password);
                } else {
                    // Hiện mật khẩu
                    editNewPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                    imgShowNewPassword.setImageResource(R.drawable.hide_password);
                }
                isPasswordVisible = !isPasswordVisible;
                editNewPassword.setSelection(editNewPassword.getText().length());
            }
        });

        imgShowRetypeNewPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isPasswordVisible) {
                    // Ẩn mật khẩu
                    editRetypeNewPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    imgShowRetypeNewPassword.setImageResource(R.drawable.show_password);
                } else {
                    // Hiện mật khẩu
                    editRetypeNewPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                    imgShowRetypeNewPassword.setImageResource(R.drawable.hide_password);
                }
                isPasswordVisible = !isPasswordVisible;
                editRetypeNewPassword.setSelection(editRetypeNewPassword.getText().length());
            }
        });
    }
}