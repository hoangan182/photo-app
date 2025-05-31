package com.example.photobooth.controllers;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.example.photobooth.models.Album;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class AlbumController {
    private static final String TAG = "AlbumController";
    private static final String PREF_NAME = "albums";
    private static final String KEY_ALBUMS = "albums_list";
    private static final String ALBUM_COVERS_DIR = "album_covers";
    private static final String ALBUM_PHOTOS_DIR = "album_photos";

    private final Context context;
    private final SharedPreferences preferences;
    private final Gson gson;
    private final File albumCoversDir;
    private final File albumPhotosDir;

    public AlbumController(Context context) {
        this.context = context;
        this.preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        this.gson = new Gson();
        
        // Create directories for album covers and photos if they don't exist
        File filesDir = context.getFilesDir();
        albumCoversDir = new File(filesDir, ALBUM_COVERS_DIR);
        albumPhotosDir = new File(filesDir, ALBUM_PHOTOS_DIR);
        if (!albumCoversDir.exists()) {
            albumCoversDir.mkdirs();
        }
        if (!albumPhotosDir.exists()) {
            albumPhotosDir.mkdirs();
        }
    }

    public Album createAlbum(String title, String description, Bitmap coverImage) {
        String id = UUID.randomUUID().toString();
        long currentTime = System.currentTimeMillis();
        
        Album album = new Album(id, title, description, currentTime, currentTime);
        
        // Save album cover image if provided
        if (coverImage != null) {
            String coverPath = saveAlbumCover(coverImage, id);
            album.setCoverPath(coverPath);
        }
        
        // Save album to preferences
        List<Album> albums = getAllAlbums();
        albums.add(album);
        saveAlbums(albums);
        
        return album;
    }

    public List<Album> getAllAlbums() {
        String json = preferences.getString(KEY_ALBUMS, null);
        if (json == null) {
            return new ArrayList<>();
        }
        
        Type type = new TypeToken<List<Album>>(){}.getType();
        return gson.fromJson(json, type);
    }

    public void deleteAlbum(String albumId) {
        List<Album> albums = getAllAlbums();
        for (int i = 0; i < albums.size(); i++) {
            if (albums.get(i).getId().equals(albumId)) {
                // Delete album cover if exists
                String coverPath = albums.get(i).getCoverPath();
                if (coverPath != null) {
                    new File(coverPath).delete();
                }
                albums.remove(i);
                break;
            }
        }
        saveAlbums(albums);
    }

    private void saveAlbums(List<Album> albums) {
        String json = gson.toJson(albums);
        preferences.edit().putString(KEY_ALBUMS, json).apply();
    }

    public void updateAlbum(Album updatedAlbum) {
        List<Album> albums = getAllAlbums();
        for (int i = 0; i < albums.size(); i++) {
            if (albums.get(i).getId().equals(updatedAlbum.getId())) {
                albums.set(i, updatedAlbum);
                break;
            }
        }
        saveAlbums(albums);
    }

    private String saveAlbumCover(Bitmap coverImage, String albumId) {
        File coverFile = new File(albumCoversDir, albumId + ".jpg");
        try (FileOutputStream out = new FileOutputStream(coverFile)) {
            coverImage.compress(Bitmap.CompressFormat.JPEG, 90, out);
            return coverFile.getAbsolutePath();
        } catch (IOException e) {
            Log.e(TAG, "Error saving album cover", e);
            return null;
        }
    }

    public Bitmap getAlbumCover(String coverPath) {
        if (coverPath == null) return null;
        return BitmapFactory.decodeFile(coverPath);
    }

    public Album getAlbumById(String albumId) {
        List<Album> albums = getAllAlbums();
        for (Album album : albums) {
            if (album.getId().equals(albumId)) {
                return album;
            }
        }
        return null;
    }

    public void addPhotoToAlbum(String albumId, Bitmap photo) {
        Album album = getAlbumById(albumId);
        if (album == null) return;

        // Save photo to file
        String photoPath = saveAlbumPhoto(photo, albumId);
        if (photoPath != null) {
            album.addPhoto(photoPath);
            album.setUpdated_at(System.currentTimeMillis());
            updateAlbum(album);
        }
    }

    public void removePhotoFromAlbum(String albumId, String photoPath) {
        Album album = getAlbumById(albumId);
        if (album == null) return;

        List<String> photoPaths = album.getPhotoPaths();
        if (photoPaths != null && photoPaths.remove(photoPath)) {
            // Delete photo file
            new File(photoPath).delete();
            
            album.setPhotoPaths(photoPaths);
            album.setUpdated_at(System.currentTimeMillis());
            updateAlbum(album);
        }
    }

    private String saveAlbumPhoto(Bitmap photo, String albumId) {
        String fileName = albumId + "_" + System.currentTimeMillis() + ".jpg";
        File photoFile = new File(albumPhotosDir, fileName);
        try (FileOutputStream out = new FileOutputStream(photoFile)) {
            photo.compress(Bitmap.CompressFormat.JPEG, 90, out);
            return photoFile.getAbsolutePath();
        } catch (IOException e) {
            Log.e(TAG, "Error saving album photo", e);
            return null;
        }
    }

    public Bitmap getAlbumPhoto(String photoPath) {
        if (photoPath == null) return null;
        return BitmapFactory.decodeFile(photoPath);
    }
} 