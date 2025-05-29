package com.example.photobooth.controllers;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class TrashController {
    private static final String PREF_NAME = "trash_prefs";
    private static final String KEY_TRASH_IMAGES = "trash_images";
    private final SharedPreferences preferences;
    private final Gson gson;

    public TrashController(Context context) {
        preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        gson = new Gson();
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

    public void restoreAllImages() {
        List<String> trashImages = getTrashImagePaths();
        // Here you would implement the logic to restore all images
        // For now, we'll just clear the trash
        clearTrash();
    }

    public void deleteAllImages() {
        List<String> trashImages = getTrashImagePaths();
        // Here you would implement the logic to permanently delete all images
        // For now, we'll just clear the trash
        clearTrash();
    }
} 