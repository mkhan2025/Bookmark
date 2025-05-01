package com.example.bookmark.model;

import java.io.Serializable;

public class UserModel implements Serializable {
    private String uid;
    private String name;
    private String profileImage;

    public UserModel() {
        // Required empty constructor for Firestore
    }

    public UserModel(String uid, String name, String profileImage) {
        this.uid = uid;
        this.name = name;
        this.profileImage = profileImage;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getProfileImage() {
        return profileImage;
    }

    public void setProfileImage(String profileImage) {
        this.profileImage = profileImage;
    }
} 