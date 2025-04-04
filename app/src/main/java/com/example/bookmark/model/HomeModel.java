package com.example.bookmark.model;

import java.util.ArrayList;
import java.util.List;

public class HomeModel {
    private String uid, profileImage, imageUrl, name, comment, description, id, locationName, activityType;
    //    private String timeStamp;
    private int likeCount;
    private int localPostImage;
    private List<String> likedBy = new ArrayList<>();
    private List<CommentModel> comments = new ArrayList<>();

    public HomeModel() {
    }

    public HomeModel(String uid, String profileImage, String imageUrl, String name, String comment, String description, String id, String locationName, String activityType, int localPostImage, int likeCount, List<String>likedBy) {
        this.uid = uid;
        this.profileImage = profileImage;
        this.imageUrl = imageUrl;
        this.name = name;
        this.comment = comment;
        this.description = description;
        this.id = id;
        this.locationName = locationName;
        this.activityType = activityType;
        this.localPostImage = localPostImage;
        this.likeCount = likeCount;
    }
      public List<String> getLikedBy(){
        return likedBy; 
    }
    public void setLikedBy(){
        this.likedBy = likedBy; 
    }

    public int getLikeCount() {
        return likeCount;
    }

    public void setLikeCount(int likeCount) {
        this.likeCount = likeCount;
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

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public String getLocationName() {
        return locationName;
    }

    public void setLocationName(String locationName) {
        this.locationName = locationName;
    }

    public String getActivityType() {
        return activityType;
    }

    public void setActivityType(String activityType) {
        this.activityType = activityType;
    }

    public int getLocalPostImage() {
        return localPostImage;
    }

    public void setLocalPostImage(int localPostImage) {
        this.localPostImage = localPostImage;
    }
}
