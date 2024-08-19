package com.example.vinorate.Models;

import androidx.annotation.NonNull;

import java.io.Serializable;
import java.util.UUID;

public class Review implements Serializable {
    public static final int MAX_LINES_COLLAPSED = 3;
    public static final int MIN_LINES_COLLAPSED = 1;

    private final String id;
    private String username = "";
    private String comment = "";
    private String wineName = "";
    private String wineId = "";
    private float rating = 0.0f;
    private boolean isCollapsed = true;

    public Review() {
        this.id = UUID.randomUUID().toString();
    }

    public Review(String username, String wineName, String wineId, String comment, float rating) {
        this.id = UUID.randomUUID().toString();
        this.username = username;
        this.wineName = wineName;
        this.wineId = wineId;
        this.comment = comment;
        this.rating = rating;
    }


    public String getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public Review setUsername(String username) {
        this.username = username;
        return this;
    }

    public String getComment() {
        return comment;
    }

    public Review setComment(String comment) {
        this.comment = comment;
        return this;
    }

    public String getWineName() {
        return wineName;
    }

    public Review setWineName(String wineName) {
        this.wineName = wineName;
        return this;
    }

    public String getWineId() {
        return wineId;
    }

    public Review setWineId(String wineId) {
        this.wineId = wineId;
        return this;
    }


    public float getRating() {
        return rating;
    }

    public Review setRating(float rating) {
        this.rating = rating;
        return this;
    }

    public boolean isCollapsed() {
        return isCollapsed;
    }

    public void setCollapsed(boolean collapsed) {
        isCollapsed = collapsed;
    }

    @NonNull
    @Override
    public String toString() {
        return "Review{" +
                "id='" + id + '\'' +
                ", username='" + username + '\'' +
                ", wineName='" + wineName + '\'' +
                ", wineId='" + wineId + '\'' +
                ", comment='" + comment + '\'' +
                ", rating=" + rating +
                '}';
    }
}
