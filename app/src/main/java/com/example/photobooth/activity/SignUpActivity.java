package com.example.photobooth.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.photobooth.R;

public class SignUpActivity extends Activity {
    TextView Login1TextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());

            int originalLeft = v.getPaddingLeft();
            int originalTop = v.getPaddingTop();
            int originalRight = v.getPaddingRight();
            int originalBottom = v.getPaddingBottom();

            v.setPadding(
                    originalLeft + systemBars.left,
                    originalTop,
                    originalRight + systemBars.right,
                    originalBottom
            );
            return insets;
        });
        Login1TextView= findViewById(R.id.txtLogin1);
        Login1TextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SignUpActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });

    }
}
