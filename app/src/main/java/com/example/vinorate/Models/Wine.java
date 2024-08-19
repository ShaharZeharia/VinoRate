package com.example.vinorate.Models;

import androidx.annotation.NonNull;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Wine implements Serializable {

    private final String id;
    private String name = "Unnamed Wine";
    private String grapes = "Unknown Grapes";
    private int year = 0;
    private String origin = "Unknown Origin";
    private double cost = 0.0;
    private String overview = "No overview available.";
    private String poster = "";
    private float rating = 0.0f;
    private boolean isWhitelisted = false;
    private Map<String, Review> reviews = new HashMap<>();

    public Wine() {
        this.id = UUID.randomUUID().toString();
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Wine setName(String name) {
        this.name = name;
        return this;
    }

    public String getGrapes() {
        return grapes;
    }

    public Wine setGrapes(String grapes) {
        this.grapes = grapes;
        return this;
    }

    public int getYear() {
        return year;
    }

    public Wine setYear(int year) {
        this.year = year;
        return this;
    }

    public String getOrigin() {
        return origin;
    }

    public Wine setOrigin(String origin) {
        this.origin = origin;
        return this;
    }

    public double getCost() {
        return cost;
    }

    public Wine setCost(double cost) {
        this.cost = cost;
        return this;
    }

    public String getOverview() {
        return overview;
    }

    public Wine setOverview(String overview) {
        this.overview = overview;
        return this;
    }

    public String getPoster() {
        return poster;
    }

    public Wine setPoster(String poster) {
        this.poster = poster;
        return this;
    }

    public float getRating() {
        return rating;
    }

    public void setRating() {
        this.rating = calculateAverageRating();
    }

    public float calculateAverageRating() {
        if (reviews.isEmpty()) {
            return 0.0f;
        }
        float sum = 0.0f;
        for (Review review : reviews.values()) {
            sum += review.getRating();
        }
        return sum / reviews.size();
    }

    public boolean isWhitelisted() {
        return isWhitelisted;
    }

    public void setWhitelisted(boolean whitelisted) {
        isWhitelisted = whitelisted;
    }

    public Map<String, Review> getReviews() {
        return reviews;
    }

    public void addReview(String reviewId, Review review) {
        this.reviews.put(reviewId, review);
        setRating();
    }

    public Wine setReviews(Map<String, Review> reviews) {
        this.reviews = reviews;
        return this;
    }

    @NonNull
    @Override
    public String toString() {
        return "Wine{" +
                "id='" + id + '\'' +
                "name='" + name + '\'' +
                ", grapes='" + grapes + '\'' +
                ", year=" + year +
                ", origin='" + origin + '\'' +
                ", cost=" + cost +
                ", overview='" + overview + '\'' +
                ", poster='" + poster + '\'' +
                ", rating=" + rating +
                ", isWhitelisted=" + isWhitelisted +
                ", reviews=" + reviews +
                '}';
    }
}


