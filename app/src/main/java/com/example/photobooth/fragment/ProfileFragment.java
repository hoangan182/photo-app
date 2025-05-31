package com.example.photobooth.fragment;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.example.photobooth.R;
import com.example.photobooth.activity.ChangeInformationActivity;
import com.example.photobooth.activity.ChangePasswordActivity;
import com.example.photobooth.activity.LoginActivity;
import com.example.photobooth.controllers.UserController;
import com.example.photobooth.models.User;
import com.google.firebase.auth.FirebaseAuth;

public class ProfileFragment extends Fragment {
    private Button btnChangePassword, btnChangeInfo, btnLogout;
    private TextView txtUsername, txtEmail;
    private ImageView imgAvatar;
    private UserController userController;

    public ProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        try {
            // Khởi tạo UserController
            userController = new UserController();

            // Ánh xạ view
            btnChangePassword = view.findViewById(R.id.btnChangePassword);
            btnChangeInfo = view.findViewById(R.id.btnChangeInfo);
            btnLogout = view.findViewById(R.id.btnLogout);
            txtUsername = view.findViewById(R.id.textView11);
            txtEmail = view.findViewById(R.id.textView16);
            imgAvatar = view.findViewById(R.id.imgAvatar);

            // Lấy và hiển thị thông tin người dùng
            loadUserInfo();

            // Xử lý các nút
            btnChangePassword.setOnClickListener(v -> {
                Intent intent = new Intent(getContext(), ChangePasswordActivity.class);
                startActivity(intent);
            });

            btnChangeInfo.setOnClickListener(v -> {
                Intent intent = new Intent(getContext(), ChangeInformationActivity.class);
                startActivity(intent);
            });

            btnLogout.setOnClickListener(v -> {
                // Hiển thị dialog xác nhận đăng xuất
                new AlertDialog.Builder(requireContext())
                    .setTitle("Đăng xuất")
                    .setMessage("Bạn có chắc chắn muốn đăng xuất không?")
                    .setPositiveButton("Đăng xuất", (dialog, which) -> {
                        // Đăng xuất Firebase
                        FirebaseAuth.getInstance().signOut();

                        // Chuyển về màn hình đăng nhập và kết thúc activity hiện tại
                        Intent intent = new Intent(getContext(), LoginActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        if (getActivity() != null) {
                            getActivity().finish();
                        }
                    })
                    .setNegativeButton("Hủy", null)
                    .show();
            });

            return view;
        } catch (Exception e) {
            Toast.makeText(getContext(), "Có lỗi xảy ra: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
            return view;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        // Cập nhật lại thông tin khi quay lại fragment
        if (userController != null) {
            loadUserInfo();
        }
    }

    private void loadUserInfo() {
        if (getContext() == null) return;

        // Hiển thị dialog loading
        AlertDialog loadingDialog = new AlertDialog.Builder(requireContext())
                .setMessage("Đang tải thông tin...")
                .setCancelable(false)
                .create();
        loadingDialog.show();

        userController.getCurrentUserFromFirestore(
            user -> {
                if (user != null && getContext() != null) {
                    txtUsername.setText(user.getUsername());
                    txtEmail.setText(user.getEmail());
                    
                    // Hiển thị avatar
                    if (user.getAvatar() != null && !user.getAvatar().isEmpty()) {
                        try {
                            // Load avatar từ URL với xử lý lỗi tốt hơn
                            Glide.with(requireContext())
                                .load(user.getAvatar())
                                .placeholder(R.drawable.baseline_account_circle_24)
                                .error(R.drawable.baseline_account_circle_24)
                                .circleCrop()
                                .timeout(15000) // 15 seconds timeout
                                .listener(new RequestListener<Drawable>() {
                                    @Override
                                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                                        if (getContext() != null) {
                                            Log.e("ProfileFragment", "Error loading avatar: " + (e != null ? e.getMessage() : "Unknown error"));
                                            // Hiển thị avatar mặc định khi có lỗi
                                            imgAvatar.setImageResource(R.drawable.baseline_account_circle_24);
                                        }
                                        return false;
                                    }

                                    @Override
                                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                                        return false;
                                    }
                                })
                                .into(imgAvatar);
                        } catch (Exception e) {
                            Log.e("ProfileFragment", "Exception loading avatar: " + e.getMessage());
                            imgAvatar.setImageResource(R.drawable.baseline_account_circle_24);
                        }
                    } else {
                        // Hiển thị avatar mặc định
                        imgAvatar.setImageResource(R.drawable.baseline_account_circle_24);
                    }
                }
                if (loadingDialog.isShowing()) {
                    loadingDialog.dismiss();
                }
            },
            e -> {
                if (getContext() != null) {
                    Log.e("ProfileFragment", "Error loading user info: " + e.getMessage());
                    Toast.makeText(getContext(), "Không thể tải thông tin người dùng: " + e.getMessage(), 
                        Toast.LENGTH_SHORT).show();
                }
                if (loadingDialog.isShowing()) {
                    loadingDialog.dismiss();
                }
            }
        );
    }
}
