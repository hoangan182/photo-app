package com.example.photobooth.models;

public class Image {
    private String id;
    private String url;
    private String type;
    private long size;
    private String caption;
    private long created_at;
    private long updated_at;

    // Constructor rỗng (cần thiết cho Firebase)
    public Image() {
    }

    // Constructor đầy đủ
    public Image(String id, String url, String type, long size, String caption, long created_at, long updated_at) {
        this.id = id;
        this.url = url;
        this.type = type;
        this.size = size;
        this.caption = caption;
        this.created_at = created_at;
        this.updated_at = updated_at;
    }

    // Getters & Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public String getCaption() {
        return caption;
    }

    public void setCaption(String caption) {
        this.caption = caption;
    }

    public long getCreated_at() {
        return created_at;
    }

    public void setCreated_at(long created_at) {
        this.created_at = created_at;
    }

    public long getUpdated_at() {
        return updated_at;
    }

    public void setUpdated_at(long updated_at) {
        this.updated_at = updated_at;
    }

    @Override
    public String toString() {
        return "Image{" +
                "id='" + id + '\'' +
                ", url='" + url + '\'' +
                ", type='" + type + '\'' +
                ", size=" + size +
                ", caption='" + caption + '\'' +
                ", created_at=" + created_at +
                ", updated_at=" + updated_at +
                '}';
    }
}
