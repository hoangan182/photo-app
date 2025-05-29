package com.example.photobooth.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.util.Log;
import com.google.gson.Gson;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.EditText;
import android.widget.TextView;
import android.text.Html;
import android.text.method.LinkMovementMethod;

import com.example.photobooth.R;
import com.example.photobooth.controllers.UserController;
import com.example.photobooth.models.User;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;



public class SignUpActivity extends Activity {

    boolean isSignUpPasswordVisible = false;
    boolean isRetypePasswordVisible = false;
    TextView Login1TextView;
    private static final String TAG = "SignUpActivity";

    TextView txtLogin1;
    Button btnSignUp;
    EditText edtEmail, edtPassword, edtUsername, edtRetypePassword;
    ImageView imgShowSignUpPassword, imgShowRetypePassword;
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

        Login1TextView= findViewById(R.id.txtLogin1);
        Login1TextView.setText(Html.fromHtml(getString(R.string.login_link)));
        Login1TextView.setMovementMethod(LinkMovementMethod.getInstance());


        userController = new UserController();

        txtLogin1 = findViewById(R.id.txtLogin1);
        btnSignUp = findViewById(R.id.btnSignUp);
        edtEmail = findViewById(R.id.edtEmail);
        edtPassword = findViewById(R.id.edtPassword);
        edtRetypePassword = findViewById(R.id.edtRetypePassword);
        edtUsername = findViewById(R.id.edtUsername);
        imgShowSignUpPassword = findViewById(R.id.imgShowSignUpPassword);
        imgShowRetypePassword = findViewById(R.id.imgShowRetypePassword);

        txtLogin1.setOnClickListener(v -> finish());

        btnSignUp.setOnClickListener(v -> performSignUp());
    }

    private void performSignUp() {
        String email = edtEmail.getText().toString().trim();
        String password = edtPassword.getText().toString().trim();
        String retypePassword = edtRetypePassword.getText().toString().trim();
        String username = edtUsername.getText().toString().trim();

        // Validate input
        if (email.isEmpty() || password.isEmpty() || retypePassword.isEmpty() || username.isEmpty()) {
            showDialog("Lỗi", "Vui lòng điền đầy đủ thông tin.");
            return;
        }

        // Validate email format
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            showDialog("Lỗi", "Email không hợp lệ.");
            return;
        }

        // Validate password length
        if (password.length() < 6) {
            showDialog("Lỗi", "Mật khẩu phải có ít nhất 6 ký tự.");
            return;
        }

        // Validate password match
        if (!password.equals(retypePassword)) {
            showDialog("Lỗi", "Mật khẩu và xác nhận mật khẩu không khớp.");
            return;
        }

        // Create user object
        User newUser = new User();
        newUser.setEmail(email);
        newUser.setUsername(username);
        newUser.setCreated_at(System.currentTimeMillis());
        newUser.setUpdated_at(System.currentTimeMillis());

        // Show loading dialog
        AlertDialog loadingDialog = new AlertDialog.Builder(this)
                .setMessage("Đang đăng ký...")
                .setCancelable(false)
                .create();
        loadingDialog.show();

        // Register user
        userController.signUpUserDocument(email, password, newUser, task -> {
            loadingDialog.dismiss();

            if (task.isSuccessful()) {
                Toast.makeText(SignUpActivity.this, "Đăng ký thành công!", Toast.LENGTH_SHORT).show();
                // Navigate to login screen
                Intent intent = new Intent(SignUpActivity.this, LoginActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            } else {
                String errorMessage = task.getException() != null ?
                        task.getException().getMessage() : "Đăng ký thất bại";
                showDialog("Lỗi", errorMessage);
            }
        });


        imgShowSignUpPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isSignUpPasswordVisible) {
                    // Ẩn mật khẩu
                    edtPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    imgShowSignUpPassword.setImageResource(R.drawable.show_password);
                } else {
                    // Hiện mật khẩu
                    edtPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                    imgShowSignUpPassword.setImageResource(R.drawable.hide_password);
                }
                isSignUpPasswordVisible = !isSignUpPasswordVisible;
                edtPassword.setSelection(edtPassword.getText().length());
            }
        });

        imgShowRetypePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isRetypePasswordVisible) {
                    // Ẩn mật khẩu
                    edtRetypePassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    imgShowRetypePassword.setImageResource(R.drawable.show_password);
                } else {
                    // Hiện mật khẩu
                    edtRetypePassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                    imgShowRetypePassword.setImageResource(R.drawable.hide_password);
                }
                isRetypePasswordVisible = !isRetypePasswordVisible;
                edtRetypePassword.setSelection(edtRetypePassword.getText().length());
            }
        });
        }

    private void showDialog(String title, String message) {
        new AlertDialog.Builder(SignUpActivity.this)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("OK", null)
                .show();
    }
}
