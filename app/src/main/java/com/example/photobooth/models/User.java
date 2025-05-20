package com.example.photobooth.models;

public class User {
    private String id;
    private String username;
    private String email;
    //    private String password;
    private long created_at;
    private long updated_at;

    // Bắt buộc phải có constructor không đối số cho Firebase
    public User() {
    }

    // Constructor đầy đủ
    public User(String id, String username, String email, long created_at, long updated_at) {
        this.id = id;
        this.username = username;
        this.email = email;
//        this.password = password;
        this.created_at = created_at;
        this.updated_at = updated_at;
    }

    // Getter và Setter
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

//    public String getPassword() {
//        return password;
//    }

//    public void setPassword(String password) {
//        this.password = password;
//    }

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
}
