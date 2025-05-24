package com.example.photobooth.fragment;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.example.photobooth.R;
import com.example.photobooth.activity.ChangeInformationActivity;
import com.example.photobooth.activity.ChangePasswordActivity;
import com.example.photobooth.activity.LoginActivity;
import com.google.firebase.auth.FirebaseAuth;

public class ProfileFragment extends Fragment {

    private Button btnChangePassword, btnChangeInfo, btnLogout;

    public ProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        btnChangePassword = view.findViewById(R.id.btnChangePassword);
        btnChangeInfo = view.findViewById(R.id.btnChangeInfo);
        btnLogout = view.findViewById(R.id.btnLogout);

        btnChangePassword.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), ChangePasswordActivity.class);
            startActivity(intent);
        });

        btnChangeInfo.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), ChangeInformationActivity.class);
            startActivity(intent);
        });

        btnLogout.setOnClickListener(v -> {
            // Đăng xuất Firebase
            FirebaseAuth.getInstance().signOut();

            // Chuyển về màn hình đăng nhập và kết thúc activity hiện tại
            Intent intent = new Intent(getContext(), LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // Clear all previous activities
            startActivity(intent);
        });

        return view;
    }
}
