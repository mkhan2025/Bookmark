package com.example.bookmark.model;

import android.net.Uri;

public class Galleryimages {
    public Uri picUri;

    public Galleryimages() {
    }

    public Galleryimages(Uri picUri) {
        this.picUri = picUri;
    }

    public Uri getPicUri() {
        return picUri;
    }

    public void setPicUri(Uri picUri) {
        this.picUri = picUri;
    }

}
