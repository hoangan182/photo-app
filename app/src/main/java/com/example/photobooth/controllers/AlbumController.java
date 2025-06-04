package com.example.photobooth.controllers;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.example.photobooth.models.Album;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.ByteArrayOutputStream;
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
    private final FirebaseFirestore db;
    private final FirebaseStorage storage;
    private final StorageReference storageRef;

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

        this.db = FirebaseFirestore.getInstance();
        this.storage = FirebaseStorage.getInstance();
        this.storageRef = storage.getReference();
    }

    public void createAlbum(String title, String description, Bitmap coverImage, OnAlbumCreatedListener listener) {
        String albumId = UUID.randomUUID().toString();
        long currentTime = System.currentTimeMillis();
        
        Album album = new Album(albumId, title, description, currentTime, currentTime);
        
        // Upload cover image if provided
        if (coverImage != null) {
            uploadImage(coverImage, "album_covers/" + albumId + ".jpg", new OnImageUploadedListener() {
                @Override
                public void onSuccess(String imageUrl) {
                    album.setCoverPath(imageUrl);
                    saveAlbumToFirestore(album, listener);
                }

                @Override
                public void onFailure(String error) {
                    Log.e(TAG, "Failed to upload cover image: " + error);
                    listener.onFailure("Failed to upload cover image");
                }
            });
        } else {
            saveAlbumToFirestore(album, listener);
        }
    }

    private void saveAlbumToFirestore(Album album, OnAlbumCreatedListener listener) {
        db.collection("albums")
            .document(album.getId())
            .set(album)
            .addOnSuccessListener(aVoid -> {
                Log.d(TAG, "Album created successfully: " + album.getId());
                listener.onSuccess(album);
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Error creating album", e);
                listener.onFailure("Failed to create album: " + e.getMessage());
            });
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
        try {
        File coverFile = new File(albumCoversDir, albumId + ".jpg");
        try (FileOutputStream out = new FileOutputStream(coverFile)) {
            coverImage.compress(Bitmap.CompressFormat.JPEG, 90, out);
                Log.d(TAG, "Saved cover: " + coverFile.getAbsolutePath());
            return coverFile.getAbsolutePath();
            }
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

    public void addPhotoToAlbum(String albumId, Bitmap photo, OnPhotoAddedListener listener) {
        uploadImage(photo, "album_photos/" + albumId + "/" + System.currentTimeMillis() + ".jpg", new OnImageUploadedListener() {
            @Override
            public void onSuccess(String imageUrl) {
                db.collection("albums")
                    .document(albumId)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        Album album = documentSnapshot.toObject(Album.class);
                        if (album != null) {
                            List<String> photoPaths = album.getPhotoPaths();
                            if (photoPaths == null) {
                                photoPaths = new ArrayList<>();
                            }
                            photoPaths.add(imageUrl);
                            album.setPhotoPaths(photoPaths);
                            album.setUpdated_at(System.currentTimeMillis());

                            db.collection("albums")
                                .document(albumId)
                                .set(album)
                                .addOnSuccessListener(aVoid -> {
                                    Log.d(TAG, "Photo added to album: " + albumId);
                                    listener.onSuccess();
                                })
                                .addOnFailureListener(e -> {
                                    Log.e(TAG, "Error adding photo to album", e);
                                    listener.onFailure("Failed to add photo to album");
                                });
                        } else {
                            listener.onFailure("Album not found");
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error getting album", e);
                        listener.onFailure("Failed to get album");
                    });
            }

            @Override
            public void onFailure(String error) {
                Log.e(TAG, "Failed to upload photo: " + error);
                listener.onFailure("Failed to upload photo");
            }
        });
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

    private void uploadImage(Bitmap bitmap, String path, OnImageUploadedListener listener) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, baos);
        byte[] data = baos.toByteArray();

        StorageReference imageRef = storageRef.child(path);
        UploadTask uploadTask = imageRef.putBytes(data);

        uploadTask.addOnSuccessListener(taskSnapshot -> {
            imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                Log.d(TAG, "Image uploaded successfully: " + uri.toString());
                listener.onSuccess(uri.toString());
            });
        }).addOnFailureListener(e -> {
            Log.e(TAG, "Error uploading image", e);
            listener.onFailure(e.getMessage());
        });
    }

    public interface OnAlbumCreatedListener {
        void onSuccess(Album album);
        void onFailure(String error);
    }

    public interface OnPhotoAddedListener {
        void onSuccess();
        void onFailure(String error);
    }

    private interface OnImageUploadedListener {
        void onSuccess(String imageUrl);
        void onFailure(String error);
    }
} 