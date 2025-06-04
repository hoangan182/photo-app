package com.example.photobooth.services;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.example.photobooth.models.Image;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentReference;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ImageUploadService {
    private static final String TAG = "ImageUploadService";
    private static final String API_URL = "https://85c7-2405-4803-fc24-1080-91b8-35f4-8429-94cb.ngrok-free.app/api/upload";
    private final Context context;
    private final FirebaseFirestore db;
    private final Gson gson;
    private final OkHttpClient client;
    private boolean isUploading = false;

    public ImageUploadService(Context context) {
        this.context = context.getApplicationContext();
        this.db = FirebaseFirestore.getInstance();
        this.gson = new Gson();
        this.client = new OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build();
        Log.d(TAG, "Initialized ImageUploadService with API URL: " + API_URL);
    }

    public interface UploadCallback {
        void onSuccess(String imageUrl);
        void onError(String error);
        void onProgress(int progress);
    }

    public void uploadImage(Uri imageUri, String type, UploadCallback callback) {
        if (isUploading) {
            callback.onError("Another upload is in progress");
            return;
        }

        isUploading = true;
        new Thread(() -> {
            File tempFile = null;
            try {
                tempFile = createTempFileFromUri(imageUri);
                if (tempFile == null) {
                    callback.onError("Could not create temporary file from image");
                    return;
                }

                // Log file information
                Log.d(TAG, "Temp file created: " + tempFile.getAbsolutePath());
                Log.d(TAG, "File size: " + tempFile.length() + " bytes");

                // Create request body
                RequestBody requestBody = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("file", tempFile.getName(),
                        RequestBody.create(MediaType.parse("image/jpeg"), tempFile))
                    .addFormDataPart("type", type)
                    .build();

                // Create request
                Request request = new Request.Builder()
                    .url(API_URL)
                    .post(requestBody)
                    .build();

                Log.d(TAG, "Sending request...");
                Response response = client.newCall(request).execute();
                String responseBody = response.body().string();
                Log.d(TAG, "Response code: " + response.code());
                Log.d(TAG, "Response body: " + responseBody);

                if (response.isSuccessful()) {
                    try {
                        JsonObject jsonResponse = gson.fromJson(responseBody, JsonObject.class);
                        String imageUrl = jsonResponse.get("fileUrl").getAsString();
                        saveImageToFirestore(imageUrl, type, tempFile.length(), callback);
                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing response: " + e.getMessage());
                        callback.onError("Error parsing server response");
                    }
                } else {
                    Log.e(TAG, "Upload failed. Response: " + responseBody);
                    callback.onError("Upload failed: " + responseBody);
                }

            } catch (Exception e) {
                Log.e(TAG, "Error uploading image", e);
                callback.onError("Upload failed: " + e.getMessage());
            } finally {
                isUploading = false;
                if (tempFile != null && tempFile.exists()) {
                    tempFile.delete();
                }
            }
        }).start();
    }

    private File createTempFileFromUri(Uri uri) {
        try {
            InputStream inputStream = context.getContentResolver().openInputStream(uri);
            if (inputStream == null) {
                Log.e(TAG, "Could not open input stream for URI: " + uri);
                return null;
            }

            File tempFile = File.createTempFile("upload_", ".jpg", context.getCacheDir());
            FileOutputStream outputStream = new FileOutputStream(tempFile);

            byte[] buffer = new byte[4096];
            int bytesRead;
            long totalBytesRead = 0;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
                totalBytesRead += bytesRead;
                Log.d(TAG, "Bytes read from URI: " + totalBytesRead);
            }

            outputStream.close();
            inputStream.close();

            Log.d(TAG, "Created temporary file: " + tempFile.getAbsolutePath());
            Log.d(TAG, "Temp file size: " + tempFile.length() + " bytes");
            return tempFile;
        } catch (IOException e) {
            Log.e(TAG, "Error creating temporary file", e);
            return null;
        }
    }

    private void saveImageToFirestore(String imageUrl, String type, long size, UploadCallback callback) {
        String imageId = UUID.randomUUID().toString();
        long currentTime = System.currentTimeMillis();

        Image image = new Image(
            imageId,
            imageUrl,
            System.currentTimeMillis(), // capturedAt
            System.currentTimeMillis(), // createdAt
            System.currentTimeMillis(), // updatedAt
            size,
            type,
            null // metadata
        );

        db.collection("images")
            .document(imageId)
            .set(image)
            .addOnSuccessListener(aVoid -> callback.onSuccess(imageUrl))
            .addOnFailureListener(e -> callback.onError("Failed to save image data: " + e.getMessage()));
    }
} 