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
        List<String> paths = gson.fromJson(json, type);
        // Filter out any null or empty paths
        paths.removeIf(path -> path == null || path.isEmpty());
        return paths;
    }

    public boolean addToTrash(String imagePath) {
        try {
            if (imagePath == null || imagePath.isEmpty()) {
                Log.e(TAG, "Invalid image path");
                return false;
            }

            List<String> trashImages = getTrashImagePaths();
            if (!trashImages.contains(imagePath)) {
                trashImages.add(imagePath);
                saveTrashImages(trashImages);
                Log.d(TAG, "Added to trash: " + imagePath);
                return true;
            }
            return true; // Already in trash
        } catch (Exception e) {
            Log.e(TAG, "Error adding to trash: " + e.getMessage());
            return false;
        }
    }

    public boolean removeFromTrash(String imagePath) {
        try {
            if (imagePath == null || imagePath.isEmpty()) {
                Log.e(TAG, "Invalid image path");
                return false;
            }

            List<String> trashImages = getTrashImagePaths();
            boolean removed = trashImages.remove(imagePath);
            if (removed) {
                saveTrashImages(trashImages);
                Log.d(TAG, "Removed from trash: " + imagePath);
            }
            return removed;
        } catch (Exception e) {
            Log.e(TAG, "Error removing from trash: " + e.getMessage());
            return false;
        }
    }

    public void clearTrash() {
        try {
            saveTrashImages(new ArrayList<>());
            Log.d(TAG, "Trash cleared");
        } catch (Exception e) {
            Log.e(TAG, "Error clearing trash: " + e.getMessage());
        }
    }

    private void saveTrashImages(List<String> trashImages) {
        try {
            String json = gson.toJson(trashImages);
            preferences.edit()
                .putString(KEY_TRASH_IMAGES, json)
                .apply();
            Log.d(TAG, "Saved trash images: " + trashImages.size());
        } catch (Exception e) {
            Log.e(TAG, "Error saving trash images: " + e.getMessage());
        }
    }

    public boolean isInTrash(String imagePath) {
        if (imagePath == null || imagePath.isEmpty()) {
            return false;
        }
        return getTrashImagePaths().contains(imagePath);
    }

    public boolean permanentlyDeleteImage(String imagePath) {
        try {
            if (imagePath == null || imagePath.isEmpty()) {
                Log.e(TAG, "Invalid image path");
                return false;
            }

            // Remove from trash list first
            boolean removed = removeFromTrash(imagePath);
            if (!removed) {
                Log.e(TAG, "Failed to remove from trash list: " + imagePath);
                return false;
            }

            // Try to delete the file if it exists
            File imageFile = new File(imagePath);
            if (imageFile.exists()) {
                boolean deleted = imageFile.delete();
                if (!deleted) {
                    Log.e(TAG, "Failed to delete file: " + imagePath);
                    return false;
                }
            }

            Log.d(TAG, "Permanently deleted: " + imagePath);
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Error permanently deleting image: " + e.getMessage());
            return false;
        }
    }

    public Bitmap getTrashImage(String imagePath) {
        try {
            if (imagePath == null || imagePath.isEmpty()) {
                return null;
            }

            File imageFile = new File(imagePath);
            if (!imageFile.exists()) {
                Log.e(TAG, "Image file does not exist: " + imagePath);
                return null;
            }
            return BitmapFactory.decodeFile(imagePath);
        } catch (Exception e) {
            Log.e(TAG, "Error getting trash image: " + e.getMessage());
            return null;
        }
    }

    public boolean restoreImage(String imagePath) {
        try {
            if (imagePath == null || imagePath.isEmpty()) {
                Log.e(TAG, "Invalid image path");
                return false;
            }

            // Remove from trash list
            boolean removed = removeFromTrash(imagePath);
            if (removed) {
                Log.d(TAG, "Restored image: " + imagePath);
            }
            return removed;
        } catch (Exception e) {
            Log.e(TAG, "Error restoring image: " + e.getMessage());
            return false;
        }
    }

    public void restoreAllImages() {
        try {
            List<String> trashImages = getTrashImagePaths();
            for (String imagePath : trashImages) {
                restoreImage(imagePath);
            }
            Log.d(TAG, "Restored all images from trash");
        } catch (Exception e) {
            Log.e(TAG, "Error restoring all images: " + e.getMessage());
        }
    }

    public void deleteAllImages() {
        try {
            List<String> trashImages = getTrashImagePaths();
            for (String imagePath : trashImages) {
                permanentlyDeleteImage(imagePath);
            }
            Log.d(TAG, "Deleted all images from trash");
        } catch (Exception e) {
            Log.e(TAG, "Error deleting all images: " + e.getMessage());
        }
    }
} 