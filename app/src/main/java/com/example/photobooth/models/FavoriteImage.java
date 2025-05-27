package com.example.photobooth.models;

public class FavoriteImage {
    private String id;
    private String imagePath;
    private long timestamp;

    public FavoriteImage() {
    }

    public FavoriteImage(String id, String imagePath) {
        this.id = id;
        this.imagePath = imagePath;
        this.timestamp = System.currentTimeMillis();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
} 