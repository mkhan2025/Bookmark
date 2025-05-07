package com.example.bookmark.utils;

public class ActivityTypeMapper {
    public static String mapOldToNewActivityType(String oldType) {
        if (oldType == null) return "All";
        
        switch (oldType.toLowerCase()) {
            case "eating":
                return "Food & Drink";
            case "adventure":
                return "Nature & Adventure";
            case "tourist":
                return "Cultural & Historical";
            case "indoor":
                return "Indoor";
            case "outdoor":
                return "Outdoor";
            default:
                return oldType;
        }
    }

    public static String mapNewToOldActivityType(String newType) {
        if (newType == null) return "All";
        
        switch (newType.toLowerCase()) {
            case "food & drink":
                return "eating";
            case "nature & adventure":
                return "adventure";
            case "cultural & historical":
                return "tourist";
            case "indoor":
                return "indoor";
            case "outdoor":
                return "outdoor";
            default:
                return newType;
        }
    }
} 