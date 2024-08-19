package com.example.vinorate.Utilities;

import androidx.annotation.NonNull;

import com.example.vinorate.Models.Review;
import com.example.vinorate.Models.User;
import com.example.vinorate.Models.Wine;
import com.example.vinorate.Models.WineCollection;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class FirebaseManager {

    private static final FirebaseDatabase database = FirebaseDatabase.getInstance();
    private static final DatabaseReference usersRef = database.getReference("Users");
    private static final DatabaseReference winesRef = database.getReference("WineCollection").child("allWines");
    private static final FirebaseAuth mAuth = FirebaseAuth.getInstance();

    public static FirebaseUser getCurrentUser() {
        return mAuth.getCurrentUser();
    }

    public static DatabaseReference getUsersRef() {
        return usersRef;
    }

    public static DatabaseReference getWinesRef() {
        return winesRef;
    }

    public void saveUserToFirebase(User user) {
        usersRef.child(user.getId()).setValue(user);
    }

    public void handleUserLogin() {
        FirebaseUser currentUser = getCurrentUser();
        if (currentUser != null) {
            String uid = currentUser.getUid();
            String name = currentUser.getDisplayName();
            createUserIfNotExists(uid, name);
        }
    }

    private void createUserIfNotExists(String uid, String name) {
        User newUser = new User(uid, name);
        usersRef.child(uid).setValue(newUser);
    }

    public void toggleWishlistItem(String wineId) {
        FirebaseUser currentUser = getCurrentUser();
        if (currentUser == null) return;

        DatabaseReference wishlistRef = usersRef.child(currentUser.getUid()).child("wishlist");
        wishlistRef.child(wineId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    wishlistRef.child(wineId).removeValue();
                } else {
                    wishlistRef.child(wineId).setValue(true);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    public String addReview(String wineId, Review review) {
        FirebaseUser currentUser = getCurrentUser();
        if (currentUser == null) return null;

        DatabaseReference wineRef = winesRef.child(wineId);
        DatabaseReference userReviewsRef = usersRef.child(currentUser.getUid()).child("reviews");

        String reviewId = userReviewsRef.push().getKey();
        if (reviewId == null) return null;

        userReviewsRef.child(reviewId).setValue(review);

        wineRef.child("reviews").child(reviewId).setValue(review);

        wineRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Wine wine = snapshot.getValue(Wine.class);
                if (wine != null) {
                    wine.setRating();
                    wineRef.child("rating").setValue(wine.getRating());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });

        return reviewId;
    }

    public void getUserWishlist(ValueEventListener listener) {
        FirebaseUser currentUser = getCurrentUser();
        if (currentUser == null) return;

        DatabaseReference wishlistRef = usersRef.child(currentUser.getUid()).child("wishlist");
        wishlistRef.addListenerForSingleValueEvent(listener);
    }

    public void getWineById(String wineId, ValueEventListener listener) {
        DatabaseReference wineRef = winesRef.child(wineId);
        wineRef.addListenerForSingleValueEvent(listener);
    }

    public void updateWineRating(String wineId, OnRatingUpdateListener listener) {
        DatabaseReference wineRef = FirebaseDatabase.getInstance().getReference("WineCollection").child("allWines").child(wineId);

        wineRef.child("reviews").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                float totalRating = 0;
                int reviewCount = 0;

                for (DataSnapshot reviewSnapshot : dataSnapshot.getChildren()) {
                    Review review = reviewSnapshot.getValue(Review.class);
                    if (review != null) {
                        totalRating += review.getRating();
                        reviewCount++;
                    }
                }

                if (reviewCount > 0) {
                    float newAverageRating = totalRating / reviewCount;
                    wineRef.child("rating").setValue(newAverageRating);

                    listener.onRatingUpdate(newAverageRating);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    public interface OnRatingUpdateListener {
        void onRatingUpdate(float newRating);
    }

    public void saveWineCollectionToFirebase(WineCollection wineCollection) {
        winesRef.setValue(wineCollection.getAllWines());
    }

    public void addWineToFirebase(Wine wine) {
        winesRef.child(wine.getId()).setValue(wine);
    }

    public void loadWineCollectionFromFirebase(ValueEventListener listener) {
        winesRef.addValueEventListener(listener);
    }

    public void updateWineInFirebase(Wine wine) {
        winesRef.child(wine.getId()).setValue(wine);
    }

}

