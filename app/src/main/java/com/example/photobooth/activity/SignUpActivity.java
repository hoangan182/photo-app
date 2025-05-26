package com.example.photobooth.activity;

import android.app.Activity;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.text.Html;
import android.text.method.LinkMovementMethod;

import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.photobooth.R;

public class SignUpActivity extends Activity {

    boolean isSignUpPasswordVisible = false;
    boolean isRetypePasswordVisible = false;
    TextView Login1TextView;
    Button btnSignUp;

    EditText editSignUpPassword, editRetypePassword;

    ImageView imgShowSignUpPassword, imgShowRetypePassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0);
            return insets;
        });

        Login1TextView= findViewById(R.id.txtLogin1);
        Login1TextView.setText(Html.fromHtml(getString(R.string.login_link)));
        Login1TextView.setMovementMethod(LinkMovementMethod.getInstance());

        btnSignUp = findViewById(R.id.btnSignUp);
        editSignUpPassword = findViewById(R.id.editSignUpPassword);
        editRetypePassword = findViewById(R.id.editRetypePassword);
        imgShowSignUpPassword = findViewById(R.id.imgShowSignUpPassword);
        imgShowRetypePassword = findViewById(R.id.imgShowRetypePassword);

        Login1TextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        imgShowSignUpPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isSignUpPasswordVisible) {
                    // Ẩn mật khẩu
                    editSignUpPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    imgShowSignUpPassword.setImageResource(R.drawable.show_password);
                } else {
                    // Hiện mật khẩu
                    editSignUpPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                    imgShowSignUpPassword.setImageResource(R.drawable.hide_password);
                }
                isSignUpPasswordVisible = !isSignUpPasswordVisible;
                editSignUpPassword.setSelection(editSignUpPassword.getText().length());
            }
        });

        imgShowRetypePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isRetypePasswordVisible) {
                    // Ẩn mật khẩu
                    editRetypePassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    imgShowRetypePassword.setImageResource(R.drawable.show_password);
                } else {
                    // Hiện mật khẩu
                    editRetypePassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                    imgShowRetypePassword.setImageResource(R.drawable.hide_password);
                }
                isRetypePasswordVisible = !isRetypePasswordVisible;
                editRetypePassword.setSelection(editRetypePassword.getText().length());
            }
        });

    }
}
