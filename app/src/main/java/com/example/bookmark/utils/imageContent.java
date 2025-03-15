package com.example.bookmark.utils;

import android.net.Uri;

import com.example.bookmark.model.Galleryimages;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class imageContent {
    static final List<Galleryimages> list = new ArrayList<>();

    public static void loadImages(File file)
    {
        Galleryimages images = new Galleryimages();
        images.picUri = Uri.fromFile(file);
        addImages(images);
    }
    private static void addImages (Galleryimages images)
    {
        list.add(0, images);
    }
    public static void loadSavedImages(File directory)
    {
        list.clear();
        if (directory.exists()){
            File[] files = directory.listFiles();
            for(File file:files)
            {
                String absolutePath = file.getAbsolutePath();
                String extension = absolutePath.substring(absolutePath.lastIndexOf("."));
                if (extension.equals("jpg") || extension.equals(".png"))
                {
                    loadImages(file);
                }
            }
        }
    }
}
