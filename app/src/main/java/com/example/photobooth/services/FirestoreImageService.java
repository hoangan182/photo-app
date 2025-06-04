package com.example.photobooth.services;

import android.content.Context;
import android.util.Log;

import com.example.photobooth.models.Image;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class FirestoreImageService {
    private static final String TAG = "FirestoreImageService";
    private static final String COLLECTION_IMAGES = "images";
    private final FirebaseFirestore db;

    public FirestoreImageService() {
        this.db = FirebaseFirestore.getInstance();
    }

    public interface ImageCallback {
        void onSuccess(List<Image> images);
        void onError(String error);
    }

    public void getImages(ImageCallback callback) {
        Log.d(TAG, "Fetching images from Firestore...");
        db.collection(COLLECTION_IMAGES)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                List<Image> images = new ArrayList<>();
                Log.d(TAG, "Found " + queryDocumentSnapshots.size() + " images");
                
                for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                    Image image = document.toObject(Image.class);
                    image.setId(document.getId());
                    
                    // Log image details for debugging
                    Log.d(TAG, "Image ID: " + image.getId());
                    Log.d(TAG, "Image URL: " + image.getUrl());
                    Log.d(TAG, "Image Type: " + image.getType());
                    Log.d(TAG, "Image Size: " + image.getSize());
                    Log.d(TAG, "Created At: " + image.getCreatedAt());
                    
                    // Validate image data
                    if (image.getUrl() != null && !image.getUrl().isEmpty()) {
                        images.add(image);
                    } else {
                        Log.w(TAG, "Skipping image with null or empty URL: " + image.getId());
                    }
                }
                
                Log.d(TAG, "Returning " + images.size() + " valid images");
                callback.onSuccess(images);
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Error getting images", e);
                callback.onError("Failed to load images: " + e.getMessage());
            });
    }

    public Task<Void> deleteImage(String imageId) {
        Log.d(TAG, "Deleting image: " + imageId);
        return db.collection(COLLECTION_IMAGES)
            .document(imageId)
            .delete();
    }

    public Task<Void> updateImage(Image image) {
        Log.d(TAG, "Updating image: " + image.getId());
        return db.collection(COLLECTION_IMAGES)
            .document(image.getId())
            .set(image);
    }
} 