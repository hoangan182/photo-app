package com.example.photobooth.models;


public class Album {
    private String id;
    private String title;
    private String description;
    private long created_at;
    private long updated_at;

    // Constructor rỗng cần thiết cho Firebase
    public Album() {
    }

    // Constructor đầy đủ
    public Album(String id, String title, String description, long created_at, long updated_at) {
        this.id = id;
        this.title = title;
        this.description = description;
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

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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
        return "Album{" +
                "id='" + id + '\'' +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", created_at=" + created_at +
                ", updated_at=" + updated_at +
                '}';
    }
}

