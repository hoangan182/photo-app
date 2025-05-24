package com.example.photobooth.controllers;

import android.util.Log;

import androidx.annotation.NonNull;

import com.example.photobooth.models.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class UserController {

    private static final String TAG = "UserController";
    private static final String COLLECTION_NAME = "users";

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final FirebaseAuth auth = FirebaseAuth.getInstance();

    // Các hàm cũ ở đây (createUser, getUserById, ...)

    /**
     * Đăng ký user: tạo tài khoản Auth + lưu profile trong Firestore users collection.
     * Xử lý rollback nếu Firestore thất bại, đồng thời kiểm tra nếu email đã tồn tại trong Auth nhưng chưa có trong Firestore.
     */
    public void signUpUserDocument(String email, String password, final User userProfileData,
                                   OnCompleteListener<AuthResult> listener) {
        // First check if email already exists in Firestore
        getUserByEmail(email,
            existingUser -> {
                // Email already exists in Firestore
                Task<AuthResult> failedTask = Tasks.forException(
                    new Exception("Email đã được đăng ký trong hệ thống."));
                listener.onComplete(failedTask);
            },
            error -> {
                // Email not found in Firestore, proceed with registration
                auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(authTask -> {
                        if (authTask.isSuccessful()) {
                            FirebaseUser firebaseUser = auth.getCurrentUser();
                            if (firebaseUser != null) {
                                // Set user data
                                userProfileData.setEmail(firebaseUser.getEmail());
                                userProfileData.setId(firebaseUser.getUid());
                                userProfileData.setCreated_at(System.currentTimeMillis());
                                userProfileData.setUpdated_at(System.currentTimeMillis());

                                // Save to Firestore
                                db.collection(COLLECTION_NAME)
                                    .document(firebaseUser.getUid())
                                    .set(userProfileData)
                                    .addOnCompleteListener(firestoreTask -> {
                                        if (firestoreTask.isSuccessful()) {
                                            listener.onComplete(authTask); // Both auth and Firestore success
                                        } else {
                                            // Firestore write failed, rollback auth user
                                            firebaseUser.delete()
                                                .addOnCompleteListener(deleteTask -> {
                                                    String errorMessage = "Không thể lưu thông tin người dùng.";
                                                    if (deleteTask.isSuccessful()) {
                                                        errorMessage += " Đã huỷ đăng ký tài khoản.";
                                                    } else {
                                                        errorMessage += " Không thể huỷ đăng ký tài khoản. Vui lòng liên hệ hỗ trợ.";
                                                    }
                                                    Task<AuthResult> failedAuthTask = Tasks.forException(
                                                        new Exception(errorMessage, firestoreTask.getException()));
                                                    listener.onComplete(failedAuthTask);
                                                });
                                        }
                                    });
                            } else {
                                Task<AuthResult> failedAuthTask = Tasks.forException(
                                    new Exception("Không thể lấy thông tin người dùng sau khi đăng ký."));
                                listener.onComplete(failedAuthTask);
                            }
                        } else {
                            // Auth creation failed
                            listener.onComplete(authTask);
                        }
                    });
            });
    }

    /**
     * Tạo người dùng mới và lưu uid vào field `id`
     */
    public void createUser(User user, OnSuccessListener<Void> onSuccess, OnFailureListener onFailure) {
        db.collection("users")
                .document(user.getId()) // ID = uid từ FirebaseAuth
                .set(user)
                .addOnSuccessListener(unused -> onSuccess.onSuccess(null))
                .addOnFailureListener(onFailure);
    }

    /**
     * Lấy người dùng theo ID
     */
    public void getUserById(String uid, OnSuccessListener<User> onSuccess, OnFailureListener onFailure) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users").document(uid)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        User user = documentSnapshot.toObject(User.class);
                        onSuccess.onSuccess(user);
                    } else {
                        onFailure.onFailure(new Exception("User not found"));
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
            onFailure.onFailure(new IllegalArgumentException("User ID không được để trống"));
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
            onFailure.onFailure(new IllegalArgumentException("User ID không được để trống"));
            return;
        }

        db.collection(COLLECTION_NAME)
                .document(userId)
                .delete()
                .addOnSuccessListener(onSuccess)
                .addOnFailureListener(onFailure);
    }

    /**
     * Lấy ID người dùng hiện tại
     */
    public String getCurrentUserId() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        return user != null ? user.getUid() : null;
    }

    /**
     * Lấy thông tin người dùng hiện tại bằng ID
     */
    public void getCurrentUserFromFirestore(OnSuccessListener<User> onSuccess, OnFailureListener onFailure) {
        String uid = getCurrentUserId();
        if (uid != null) {
            getUserById(uid, onSuccess, onFailure);
        } else {
            onFailure.onFailure(new Exception("Người dùng chưa đăng nhập"));
        }
    }

    /**
     * (Tuỳ chọn) Lấy người dùng theo email — KHÔNG KHUYẾN KHÍCH dùng làm chính
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
