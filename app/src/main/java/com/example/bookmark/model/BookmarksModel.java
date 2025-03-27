package com.example.bookmark.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class BookmarksModel {
    private String originalUserId, bookmarkedPostId;
    private Date timestamp;

    public BookmarksModel() {
        
    }
    public BookmarksModel(String originalUserId, String bookmarkedPostId, Date timestamp) { 
        this.originalUserId = originalUserId;
        this.bookmarkedPostId = bookmarkedPostId;
        this.timestamp = timestamp;
    }
    public String getOriginalUserId() {
        return originalUserId;
    }
    public String getBookmarkedPostId() {
        return bookmarkedPostId;
    }
    public Date getTimestamp() {
        return timestamp;
    }
    public void setOriginalUserId(String originalUserId) {
        this.originalUserId = originalUserId;
    }
    public void setBookmarkedPostId(String bookmarkedPostId) {  
        this.bookmarkedPostId = bookmarkedPostId;
    }
    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }
}