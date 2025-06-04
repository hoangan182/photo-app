package com.example.photobooth.models;

import android.os.Parcel;
import android.os.Parcelable;

public class Image implements Parcelable {
    private String id;
    private String url;
    private long capturedAt;
    private long createdAt;
    private long updatedAt;
    private long size;
    private String type;
    private String metadata;
    private boolean deleted;

    // Constructor rỗng (cần thiết cho Firebase)
    public Image() {
    }

    // Constructor đầy đủ
    public Image(String id, String url, long capturedAt, long createdAt, long updatedAt, long size, String type, String metadata) {
        this.id = id;
        this.url = url;
        this.capturedAt = capturedAt;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.size = size;
        this.type = type;
        this.metadata = metadata;
        this.deleted = false;
    }

    protected Image(Parcel in) {
        id = in.readString();
        url = in.readString();
        capturedAt = in.readLong();
        createdAt = in.readLong();
        updatedAt = in.readLong();
        size = in.readLong();
        type = in.readString();
        metadata = in.readString();
        deleted = in.readByte() != 0;
    }

    public static final Creator<Image> CREATOR = new Creator<Image>() {
        @Override
        public Image createFromParcel(Parcel in) {
            return new Image(in);
        }

        @Override
        public Image[] newArray(int size) {
            return new Image[size];
        }
    };

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

    public long getCapturedAt() {
        return capturedAt;
    }

    public void setCapturedAt(long capturedAt) {
        this.capturedAt = capturedAt;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public long getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(long updatedAt) {
        this.updatedAt = updatedAt;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getMetadata() {
        return metadata;
    }

    public void setMetadata(String metadata) {
        this.metadata = metadata;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(url);
        dest.writeLong(capturedAt);
        dest.writeLong(createdAt);
        dest.writeLong(updatedAt);
        dest.writeLong(size);
        dest.writeString(type);
        dest.writeString(metadata);
        dest.writeByte((byte) (deleted ? 1 : 0));
    }

    @Override
    public String toString() {
        return "Image{" +
                "id='" + id + '\'' +
                ", url='" + url + '\'' +
                ", type='" + type + '\'' +
                ", size=" + size +
                ", capturedAt=" + capturedAt +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                ", metadata='" + metadata + '\'' +
                ", deleted=" + deleted +
                '}';
    }
}
