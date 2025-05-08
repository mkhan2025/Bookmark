package com.example.bookmark.model;

import java.util.ArrayList;
import java.util.List;
import java.io.Serializable;
import java.util.Date;

public class HomeModel implements Serializable {
    private static final long serialVersionUID = 1L;  // Add this line
    private String uid, profileImage, imageUrl, name, comment, description, id, locationName, activityType;
    private float trendingScore; 
    private long timestamp; 
    //    private String timeStamp;
    private double latitude, longitude;
    private int likeCount;
    private int localPostImage;
    private List<String> likedBy = new ArrayList<>();
    private List<CommentModel> comments = new ArrayList<>();
    private List<String> imageUrls = new ArrayList<>();  // Add this field for multiple images

    public HomeModel() {
    }

    public HomeModel(String uid, String profileImage, String imageUrl, String name, String comment, String description, String id, String locationName, String activityType, int localPostImage, int likeCount, List<String>likedBy, double latitude, double longitude, long timestamp, float trendingScore) {
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
        this.timestamp = System.currentTimeMillis();
        this.trendingScore = trendingScore;
        // Initialize imageUrls with the single imageUrl for backward compatibility
        if (imageUrl != null) {
            this.imageUrls.add(imageUrl);
        }
    }

    // Add getter and setter for imageUrls
    public List<String> getImageUrls() {
        return imageUrls;
    }

    public void setImageUrls(List<String> imageUrls) {
        this.imageUrls = imageUrls;
        // Update single imageUrl for backward compatibility
        if (imageUrls != null && !imageUrls.isEmpty()) {
            this.imageUrl = imageUrls.get(0);
        }
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }      
    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }
    public void setLongitude(double longitude) {
        this.longitude = longitude;
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
    public float getTrendingScore(){
        return trendingScore;
    }
    public void setTrendingScore(float trendingScore){
        this.trendingScore = trendingScore;
    }   
    public long getTimestamp(){
        return timestamp;
    }
    public void setTimestamp(long timestamp){
        this.timestamp = timestamp;
    }
    //im not getting the initializing likedBy in my constructor
}