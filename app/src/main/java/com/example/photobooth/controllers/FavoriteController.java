package com.example.photobooth.controllers;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.example.photobooth.models.FavoriteImage;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class FavoriteController {
    private static final String TAG = "FavoriteController";
    private static final String PREF_NAME = "favorites";
    private static final String KEY_FAVORITES = "favorite_images";
    private final SharedPreferences preferences;
    private final Gson gson;
    private final Context context;

    public FavoriteController(Context context) {
        this.context = context;
        preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        gson = new Gson();
    }

    public void addFavorite(String imagePath) {
        Log.d(TAG, "Adding favorite: " + imagePath);
        if (imagePath == null || imagePath.isEmpty()) {
            Log.w(TAG, "Attempted to add null or empty image path");
            return;
        }

        // Verify file exists
        File imageFile = new File(imagePath);
        if (!imageFile.exists()) {
            Log.w(TAG, "Image file does not exist: " + imagePath);
            return;
        }

        List<FavoriteImage> favorites = getFavorites();
        // Check if image is already in favorites
        for (FavoriteImage favorite : favorites) {
            if (favorite.getImagePath().equals(imagePath)) {
                Log.d(TAG, "Image already in favorites: " + imagePath);
                return;
            }
        }
        
        // Add new favorite
        FavoriteImage newFavorite = new FavoriteImage(String.valueOf(System.currentTimeMillis()), imagePath);
        favorites.add(newFavorite);
        saveFavorites(favorites);
        Log.d(TAG, "Successfully added new favorite: " + imagePath);
    }

    public void removeFavorite(String imagePath) {
        Log.d(TAG, "Removing favorite: " + imagePath);
        List<FavoriteImage> favorites = getFavorites();
        int initialSize = favorites.size();
        favorites.removeIf(favorite -> favorite.getImagePath().equals(imagePath));
        if (favorites.size() < initialSize) {
            saveFavorites(favorites);
            Log.d(TAG, "Successfully removed favorite: " + imagePath);
        } else {
            Log.w(TAG, "Image not found in favorites: " + imagePath);
        }
    }

    public boolean isFavorite(String imagePath) {
        if (imagePath == null || imagePath.isEmpty()) {
            Log.w(TAG, "Checking favorite status for null or empty path");
            return false;
        }

        List<FavoriteImage> favorites = getFavorites();
        for (FavoriteImage favorite : favorites) {
            if (favorite.getImagePath().equals(imagePath)) {
                Log.d(TAG, "Image is favorite: " + imagePath);
                return true;
            }
        }
        Log.d(TAG, "Image is not favorite: " + imagePath);
        return false;
    }

    public List<String> getFavoriteImagePaths() {
        Log.d(TAG, "Getting favorite image paths");
        List<FavoriteImage> favorites = getFavorites();
        List<String> paths = new ArrayList<>();
        
        for (FavoriteImage favorite : favorites) {
            String path = favorite.getImagePath();
            if (path != null && !path.isEmpty()) {
                // Verify file exists
                File imageFile = new File(path);
                if (imageFile.exists()) {
                    paths.add(path);
                    Log.d(TAG, "Added valid favorite path: " + path);
                } else {
                    Log.w(TAG, "Favorite image file does not exist: " + path);
                }
            } else {
                Log.w(TAG, "Found null or empty path in favorites");
            }
        }
        
        Log.d(TAG, "Returning " + paths.size() + " valid favorite paths");
        return paths;
    }

    private List<FavoriteImage> getFavorites() {
        String json = preferences.getString(KEY_FAVORITES, null);
        if (json == null) {
            Log.d(TAG, "No favorites found in preferences");
            return new ArrayList<>();
        }
        
        try {
            Type type = new TypeToken<List<FavoriteImage>>() {}.getType();
            List<FavoriteImage> favorites = gson.fromJson(json, type);
            Log.d(TAG, "Retrieved " + (favorites != null ? favorites.size() : 0) + " favorites from preferences");
            return favorites != null ? favorites : new ArrayList<>();
        } catch (Exception e) {
            Log.e(TAG, "Error parsing favorites from preferences", e);
            return new ArrayList<>();
        }
    }

    private void saveFavorites(List<FavoriteImage> favorites) {
        try {
            String json = gson.toJson(favorites);
            preferences.edit().putString(KEY_FAVORITES, json).apply();
            Log.d(TAG, "Successfully saved " + favorites.size() + " favorites to preferences");
        } catch (Exception e) {
            Log.e(TAG, "Error saving favorites to preferences", e);
        }
    }
} 