package com.example.bookmark.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.io.Serializable;


//CommentModel is the data class that represents the comment object
public class CommentModel implements Serializable {
    private static final long serialVersionUID = 1L; 
    private String comment, uid, username, profileImage;
    private Date timestamp;

    public CommentModel() {
        
    }
    public CommentModel(String comment, String uid, Date timestamp, String username, String profileImage) {
        this.comment = comment;
        this.uid = uid;
        this.timestamp = timestamp;
        this.username = username;
        this.profileImage = profileImage;
    }   
    public String getComment() {
        return comment;
    }
    public String getUid() {
        return uid;
    }                                       
    public String getUsername() {
        return username;
    }
    public void setUsername(String username) {
        this.username = username;
    }
    public String getProfileImage() {
        return profileImage;
    }
    public void setProfileImage(String profileImage) {
        this.profileImage = profileImage;
    }
    public Date getTimestamp() {
        return timestamp;
    }
    public void setComment(String comment) {
        this.comment = comment;
    }   
    public void setUid(String uid) {
        this.uid = uid;
    }   
    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }      
    
} 