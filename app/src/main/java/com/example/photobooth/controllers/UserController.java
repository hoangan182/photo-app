package com.example.photobooth.controllers;

import android.util.Log;

import androidx.annotation.NonNull;

import com.example.photobooth.models.User;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class UserController {

    private static final String TAG = "UserController";
    private static final String COLLECTION_NAME = "users";
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    /**
     * Tạo người dùng mới
     */
    public void createUser(User user, OnSuccessListener<Void> onSuccess, OnFailureListener onFailure) {
        if (user.getId() == null) {
            Log.e(TAG, "createUser: User ID is null.");
            onFailure.onFailure(new IllegalArgumentException("User ID cannot be null"));
            return;
        }

        db.collection(COLLECTION_NAME)
                .document(user.getId())
                .set(user)
                .addOnSuccessListener(onSuccess)
                .addOnFailureListener(onFailure);
    }

    /**
     * Lấy người dùng theo ID
     */
    public void getUserById(String userId, OnSuccessListener<User> onSuccess, OnFailureListener onFailure) {
        if (userId == null || userId.isEmpty()) {
            Log.e(TAG, "getUserById: User ID is null or empty.");
            onFailure.onFailure(new IllegalArgumentException("User ID cannot be null or empty"));
            return;
        }

        db.collection(COLLECTION_NAME)
                .document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        User user = documentSnapshot.toObject(User.class);
                        onSuccess.onSuccess(user);
                    } else {
                        onFailure.onFailure(new Exception("Không tìm thấy người dùng với ID: " + userId));
                    }
                })
                .addOnFailureListener(onFailure);
    }

    /**
     * Cập nhật thông tin người dùng
     */
    public void updateUser(User user, OnSuccessListener<Void> onSuccess, OnFailureListener onFailure) {
        if (user.getId() == null) {
            Log.e(TAG, "updateUser: User ID is null.");
            onFailure.onFailure(new IllegalArgumentException("User ID cannot be null"));
            return;
        }

        user.setUpdated_at(System.currentTimeMillis());

        db.collection(COLLECTION_NAME)
                .document(user.getId())
                .set(user)
                .addOnSuccessListener(onSuccess)
                .addOnFailureListener(onFailure);
    }

    /**
     * Xoá người dùng theo ID
     */
    public void deleteUser(String userId, OnSuccessListener<Void> onSuccess, OnFailureListener onFailure) {
        if (userId == null || userId.isEmpty()) {
            Log.e(TAG, "deleteUser: User ID is null or empty.");
            onFailure.onFailure(new IllegalArgumentException("User ID cannot be null or empty"));
            return;
        }

        db.collection(COLLECTION_NAME)
                .document(userId)
                .delete()
                .addOnSuccessListener(onSuccess)
                .addOnFailureListener(onFailure);
    }

    /**
     * Lấy ID người dùng hiện tại từ FirebaseAuth
     */
    public String getCurrentUserId() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        return user != null ? user.getUid() : null;
    }

    /**
     * Lấy thông tin người dùng hiện tại từ Firestore dựa vào email
     */
    public void getCurrentUserFromFirestore(OnSuccessListener<User> onSuccess, OnFailureListener onFailure) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser != null) {
            String email = currentUser.getEmail();
            if (email != null && !email.isEmpty()) {
                getUserByEmail(email, onSuccess, onFailure);
            } else {
                onFailure.onFailure(new Exception("Email người dùng hiện tại là null hoặc rỗng"));
            }
        } else {
            onFailure.onFailure(new Exception("Người dùng chưa đăng nhập"));
        }
    }

    /**
     * Lấy người dùng theo email
     */
    public void getUserByEmail(String email, OnSuccessListener<User> onSuccess, OnFailureListener onFailure) {
        if (email == null || email.isEmpty()) {
            Log.e(TAG, "getUserByEmail: Email is null or empty.");
            onFailure.onFailure(new IllegalArgumentException("Email không được để trống"));
            return;
        }

        db.collection(COLLECTION_NAME)
                .whereEqualTo("email", email)
                .limit(1)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        User user = querySnapshot.getDocuments().get(0).toObject(User.class);
                        onSuccess.onSuccess(user);
                    } else {
                        onFailure.onFailure(new Exception("Không tìm thấy người dùng với email: " + email));
                    }
                })
                .addOnFailureListener(onFailure);
    }
}
