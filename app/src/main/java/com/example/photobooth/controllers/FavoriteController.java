package com.example.photobooth.controllers;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.example.photobooth.models.FavoriteImage;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class FavoriteController {
    private static final String TAG = "FavoriteController";
    private static final String PREF_NAME = "favorites";
    private static final String KEY_FAVORITES = "favorite_images";
    private final SharedPreferences preferences;
    private final Gson gson;

    public FavoriteController(Context context) {
        preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        gson = new Gson();
    }

    public void addFavorite(String imagePath) {
        List<FavoriteImage> favorites = getFavorites();
        // Check if image is already in favorites
        for (FavoriteImage favorite : favorites) {
            if (favorite.getImagePath().equals(imagePath)) {
                return;
            }
        }
        // Add new favorite
        FavoriteImage newFavorite = new FavoriteImage(String.valueOf(System.currentTimeMillis()), imagePath);
        favorites.add(newFavorite);
        saveFavorites(favorites);
    }

    public void removeFavorite(String imagePath) {
        List<FavoriteImage> favorites = getFavorites();
        favorites.removeIf(favorite -> favorite.getImagePath().equals(imagePath));
        saveFavorites(favorites);
    }

    public boolean isFavorite(String imagePath) {
        List<FavoriteImage> favorites = getFavorites();
        for (FavoriteImage favorite : favorites) {
            if (favorite.getImagePath().equals(imagePath)) {
                return true;
            }
        }
        return false;
    }

    public List<String> getFavoriteImagePaths() {
        List<FavoriteImage> favorites = getFavorites();
        List<String> paths = new ArrayList<>();
        for (FavoriteImage favorite : favorites) {
            paths.add(favorite.getImagePath());
        }
        return paths;
    }

    private List<FavoriteImage> getFavorites() {
        String json = preferences.getString(KEY_FAVORITES, null);
        if (json == null) {
            return new ArrayList<>();
        }
        Type type = new TypeToken<List<FavoriteImage>>() {}.getType();
        return gson.fromJson(json, type);
    }

    private void saveFavorites(List<FavoriteImage> favorites) {
        String json = gson.toJson(favorites);
        preferences.edit().putString(KEY_FAVORITES, json).apply();
    }
} 