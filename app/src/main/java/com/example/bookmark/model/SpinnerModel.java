package com.example.bookmark.model;
public class SpinnerModel {
    private String activityType;

    public SpinnerModel(String activityType) {
        this.activityType = activityType;
    }
    public String getActivityType() {
        return activityType;
    }
    public void setActivityType(String activityType) {
        this.activityType = activityType;
    }
}