package com.example.bookmark.model;

public class HomeModel {
    private String uid, profileImage, postImage, username, comment, description, id;
    //    private String timeStamp;
    private int likeCount;
    private int localPostImage;

    public HomeModel() {
    }

    public HomeModel(String uid, String profileImage, String postImage, String username, String comment, String description, String id, int likeCount, int localPostImage) {
        this.uid = uid;
        this.profileImage = profileImage;
        this.postImage = postImage;
        this.username = username;
        this.comment = comment;
        this.description = description;
        this.id = id;
        this.likeCount = likeCount;
        this.localPostImage = localPostImage;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getProfileImage() {
        return profileImage;
    }

    public void setProfileImage(String profileImage) {
        this.profileImage = profileImage;
    }

    public String getPostImage() {
        return postImage;
    }

    public void setPostImage(String postImage) {
        this.postImage = postImage;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getLikeCount() {
        return likeCount;
    }

    public void setLikeCount(int likeCount) {
        this.likeCount = likeCount;
    }

    public int getLocalPostImage() {
        return localPostImage;
    }

    public void setLocalPostImage(int localPostImage) {
        this.localPostImage = localPostImage;
    }
}
