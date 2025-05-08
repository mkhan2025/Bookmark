package com.example.bookmark.model;

import java.util.ArrayList;
import java.util.List;

public class PostImageModel {
    private List<String> imageUrls;
    private String id;
    private String description;
    private String uid;

    public PostImageModel() {
        this.imageUrls = new ArrayList<>();
    }

    public PostImageModel(List<String> imageUrls, String id, String description, String uid) {
        this.imageUrls = imageUrls;
        this.id = id;
        this.description = description;
        this.uid = uid;
    }

    public List<String> getImageUrls() {
        return imageUrls;
    }

    public void setImageUrls(List<String> imageUrls) {
        this.imageUrls = imageUrls;
    }

    public String getImageUrl() {
        return imageUrls != null && !imageUrls.isEmpty() ? imageUrls.get(0) : null;
    }

    public void setImageUrl(String imageUrl) {
        if (imageUrls == null) {
            imageUrls = new ArrayList<>();
        }
        if (!imageUrls.isEmpty()) {
            imageUrls.set(0, imageUrl);
        } else {
            imageUrls.add(imageUrl);
        }
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
