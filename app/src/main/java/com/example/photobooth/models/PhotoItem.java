package com.example.photobooth.models;

import java.io.File;

public class PhotoItem {
    private String path;
    private long captureDate;
    private File file;

    public PhotoItem(String path, long captureDate) {
        this.path = path;
        this.captureDate = captureDate;
        this.file = new File(path);
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public long getCaptureDate() {
        return captureDate;
    }

    public void setCaptureDate(long captureDate) {
        this.captureDate = captureDate;
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }
} 