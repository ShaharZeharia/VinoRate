package com.example.vinorate.Models;

import static com.google.android.gms.common.internal.safeparcel.SafeParcelable.NULL;

import androidx.annotation.NonNull;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Objects;

public class User implements Serializable {

    private String id;
    private String name = "Unnamed user";  // User's name (optional, especially if logged in with Gmail)
    private final ArrayList<Wine> wishlist;
    private final ArrayList<Review> reviews;

    public User() {
        this.wishlist = new ArrayList<>();
        this.reviews = new ArrayList<>();
    }


    public User(String id, String name) {
        this.id = id;
        if (!Objects.equals(name, NULL))
            this.name = name;
        this.wishlist = new ArrayList<>();
        this.reviews = new ArrayList<>();
    }

    public String getId() {
        return id;
    }

    public User setId(String id) {
        this.id = id;
        return this;
    }
    public String getName() {
        return name;
    }

    public User setName(String name) {
        this.name = name;
        return this;
    }

    public ArrayList<Wine> getWishlist() {
        return wishlist;
    }

    public User addWineToWishlist(Wine wine) {
        this.wishlist.add(wine);
        return this;
    }

    public User removeWineFromWishlist(Wine wine) {
        this.wishlist.remove(wine);
        return this;
    }

    public ArrayList<Review> getReviews() {
        return reviews;
    }

    public User addReview(Review review) {
        this.reviews.add(review);
        return this;
    }

    @NonNull
    @Override
    public String toString() {
        return "User{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", wishlist=" + wishlist +
                ", reviews=" + reviews +
                '}';
    }
}
