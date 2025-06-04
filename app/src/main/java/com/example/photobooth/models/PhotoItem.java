package com.example.photobooth.models;

import java.io.File;

public class PhotoItem {
    private String path;         // Đường dẫn file local hoặc URL từ Firestore
    private long captureDate;    // Thời gian chụp
    private File file;           // Chỉ dùng khi path là file local
    private String id;           // ID Firestore
    private long createdAt;
    private long updatedAt;
    private long size;
    private String type;
    private Object metadata;

    // Constructor mặc định – Bắt buộc cho Firestore
    public PhotoItem() {}

    // Constructor cho ảnh local
    public PhotoItem(String path, long captureDate) {
        this.path = path;
        this.captureDate = captureDate;
        this.file = new File(path);
    }

    // Constructor đầy đủ dùng khi map từ Firestore
    public PhotoItem(String path, long captureDate, String id, long createdAt,
                     long updatedAt, long size, String type, Object metadata) {
        this.path = path;
        this.captureDate = captureDate;
        this.id = id;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.size = size;
        this.type = type;
        this.metadata = metadata;
        this.file = path != null && !path.startsWith("http") ? new File(path) : null;
    }

    // Getter & Setter
    public String getPath() { return path; }

    public void setPath(String path) {
        this.path = path;
        this.file = path != null && !path.startsWith("http") ? new File(path) : null;
    }

    public long getCaptureDate() { return captureDate; }

    public void setCaptureDate(long captureDate) { this.captureDate = captureDate; }

    public File getFile() { return file; }

    public void setFile(File file) { this.file = file; }

    public String getId() { return id; }

    public void setId(String id) { this.id = id; }

    public long getCreatedAt() { return createdAt; }

    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }

    public long getUpdatedAt() { return updatedAt; }

    public void setUpdatedAt(long updatedAt) { this.updatedAt = updatedAt; }

    public long getSize() { return size; }

    public void setSize(long size) { this.size = size; }

    public String getType() { return type; }

    public void setType(String type) { this.type = type; }

    public Object getMetadata() { return metadata; }

    public void setMetadata(Object metadata) { this.metadata = metadata; }
}
