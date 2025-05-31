package com.example.photobooth.controllers;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import java.io.File;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class TrashController {
    private static final String TAG = "TrashController";
    private static final String PREF_NAME = "trash_prefs";
    private static final String KEY_TRASH_IMAGES = "trash_images";
    private final SharedPreferences preferences;
    private final Gson gson;
    private final ImageStorageController imageStorageController;

    public TrashController(Context context) {
        preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        gson = new Gson();
        imageStorageController = new ImageStorageController(context);
    }

    public List<String> getTrashImagePaths() {
        String json = preferences.getString(KEY_TRASH_IMAGES, null);
        if (json == null) {
            return new ArrayList<>();
        }
        Type type = new TypeToken<List<String>>() {}.getType();
        return gson.fromJson(json, type);
    }

    public void addToTrash(String imagePath) {
        List<String> trashImages = getTrashImagePaths();
        if (!trashImages.contains(imagePath)) {
            trashImages.add(imagePath);
            saveTrashImages(trashImages);
        }
    }

    public void removeFromTrash(String imagePath) {
        List<String> trashImages = getTrashImagePaths();
        trashImages.remove(imagePath);
        saveTrashImages(trashImages);
    }

    public void clearTrash() {
        saveTrashImages(new ArrayList<>());
    }

    private void saveTrashImages(List<String> trashImages) {
        String json = gson.toJson(trashImages);
        preferences.edit().putString(KEY_TRASH_IMAGES, json).apply();
    }

    public boolean restoreImage(String imagePath) {
        File imageFile = new File(imagePath);
        if (!imageFile.exists()) {
            Log.e(TAG, "Image file does not exist: " + imagePath);
            return false;
        }

        // Remove from trash list
        removeFromTrash(imagePath);
        return true;
    }

    public boolean permanentlyDeleteImage(String imagePath) {
        File imageFile = new File(imagePath);
        if (!imageFile.exists()) {
            Log.e(TAG, "Image file does not exist: " + imagePath);
            return false;
        }

        // Delete the file
        boolean deleted = imageFile.delete();
        if (deleted) {
            // Remove from trash list
            removeFromTrash(imagePath);
        }
        return deleted;
    }

    public void restoreAllImages() {
        List<String> trashImages = getTrashImagePaths();
        for (String imagePath : trashImages) {
            restoreImage(imagePath);
        }
    }

    public void deleteAllImages() {
        List<String> trashImages = getTrashImagePaths();
        for (String imagePath : trashImages) {
            permanentlyDeleteImage(imagePath);
        }
    }

    public Bitmap getTrashImage(String imagePath) {
        File imageFile = new File(imagePath);
        if (!imageFile.exists()) {
            return null;
        }
        return BitmapFactory.decodeFile(imagePath);
    }
} 