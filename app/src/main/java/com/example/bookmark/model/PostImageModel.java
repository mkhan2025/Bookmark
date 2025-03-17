package com.example.bookmark.model;

public class PostImageModel {
    private String imageUrl;
    private String id;
    private String description;
    private String uid;

    public PostImageModel() {
    }

    public PostImageModel(String imageUrl, String id, String description, String uid) {
        this.imageUrl = imageUrl;
        this.id = id;
        this.description = description;
        this.uid = uid;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }
}
