package com.example.bookmark.model;

public class HomeModel {
    private String uid, profileImage, imageUrl, name, comment, description, id;
    //    private String timeStamp;
    private int likeCount;
    private int localPostImage;

    public HomeModel() {
    }

    public HomeModel(String uid, String profileImage, String imageUrl, String name, String comment, String description, String id, int likeCount, int localPostImage) {
        this.uid = uid;
        this.profileImage = profileImage;
        this.imageUrl = imageUrl;
        this.name = name;
        this.comment = comment;
        this.description = description;
        this.id = id;
        this.likeCount = likeCount;
        this.localPostImage = localPostImage;
    }

    public int getLocalPostImage() {
        return localPostImage;
    }

    public void setLocalPostImage(int localPostImage) {
        this.localPostImage = localPostImage;
    }

    public int getLikeCount() {
        return likeCount;
    }

    public void setLikeCount(int likeCount) {
        this.likeCount = likeCount;
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

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getUsername() {
        return name;
    }

    public void setUsername(String username) {
        this.name = username;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getProfileImage() {
        return profileImage;
    }

    public void setProfileImage(String profileImage) {
        this.profileImage = profileImage;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }
}
