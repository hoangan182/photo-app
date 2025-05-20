package com.example.photobooth.controllers;

import android.util.Log;

import androidx.annotation.NonNull;

import com.example.photobooth.models.User;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class UserController {
    final String tag = "Hương hấp";
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final String COLLECTION_NAME = "users";

    // Tạo người dùng mới
    public void createUser(User user, OnSuccessListener<Void> onSuccess, OnFailureListener onFailure) {
        if (user.getId() == null) {
            Log.e("UserController", "User ID is null.");
            return;
        }

        db.collection(COLLECTION_NAME)
                .document(user.getId())
                .set(user)
                .addOnSuccessListener(onSuccess)
                .addOnFailureListener(onFailure);
    }

    // Lấy thông tin người dùng theo ID
    public void getUserById(String userId, OnSuccessListener<User> onSuccess, OnFailureListener onFailure) {
        Log.e(tag, "abc");
        Log.d(tag, userId);

        db.collection(COLLECTION_NAME)
                .document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    Log.e(tag, "abcd");
                    if (documentSnapshot.exists()) {
                        User user = documentSnapshot.toObject(User.class);

                        onSuccess.onSuccess(user);
                    } else {
                        onFailure.onFailure(new Exception("User not found"));
                    }
                })
                .addOnFailureListener(onFailure);
    }

    // Cập nhật thông tin người dùng
    public void updateUser(User user, OnSuccessListener<Void> onSuccess, OnFailureListener onFailure) {
        user.setUpdated_at(System.currentTimeMillis());

        db.collection(COLLECTION_NAME)
                .document(user.getId())
                .set(user)
                .addOnSuccessListener(onSuccess)
                .addOnFailureListener(onFailure);
    }

    // (Tùy chọn) Xoá người dùng
    public void deleteUser(String userId, OnSuccessListener<Void> onSuccess, OnFailureListener onFailure) {
        db.collection(COLLECTION_NAME)
                .document(userId)
                .delete()
                .addOnSuccessListener(onSuccess)
                .addOnFailureListener(onFailure);
    }

    // Lấy ID người dùng hiện tại (nếu đã đăng nhập)
    public String getCurrentUserId() {
        return FirebaseAuth.getInstance().getCurrentUser() != null
                ? FirebaseAuth.getInstance().getCurrentUser().getUid()
                : null;
    }
}
