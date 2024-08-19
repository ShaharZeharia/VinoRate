package com.example.vinorate.Utilities;

import android.util.Log;

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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    public interface OnWineCollectionLoadedListener {
        void onWineCollectionLoaded(WineCollection wineCollection);
    }

    public void loadWineCollectionWithWishlist(OnWineCollectionLoadedListener listener) {
        DatabaseReference winesRef = getWinesRef();
        loadWines(winesRef, wineCollection -> {
            FirebaseUser currentUser = getCurrentUser();
            if (currentUser != null) {
                addWishlistToWineCollection(wineCollection, listener);
            } else {
                listener.onWineCollectionLoaded(wineCollection);
            }
        });
    }

    private void loadWines(DatabaseReference winesRef, OnWineCollectionLoadedListener listener) {
        winesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                WineCollection wineCollection = parseWineCollection(snapshot);
                listener.onWineCollectionLoaded(wineCollection);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("Firebase", "Error loading wines: " + error.getMessage());
                listener.onWineCollectionLoaded(new WineCollection());
            }
        });
    }

    private WineCollection parseWineCollection(DataSnapshot snapshot) {
        WineCollection wineCollection = new WineCollection();
        for (DataSnapshot wineSnapshot : snapshot.getChildren()) {
            Wine wine = parseWine(wineSnapshot);
            if (wine != null) {
                wineCollection.addWine(wine);
            }
        }
        return wineCollection;
    }

    private Wine parseWine(DataSnapshot wineSnapshot) {
        Wine wine = wineSnapshot.getValue(Wine.class);
        if (wine != null) {
            DataSnapshot reviewsSnapshot = wineSnapshot.child("reviews");
            if (reviewsSnapshot.exists()) {
                wine.setReviews(parseReviews(reviewsSnapshot));
            }
        }
        return wine;
    }

    private Map<String, Review> parseReviews(DataSnapshot reviewsSnapshot) {
        Map<String, Review> reviewsMap = new HashMap<>();
        for (DataSnapshot reviewSnapshot : reviewsSnapshot.getChildren()) {
            Review review = reviewSnapshot.getValue(Review.class);
            if (review != null) {
                reviewsMap.put(reviewSnapshot.getKey(), review);
            }
        }
        return reviewsMap;
    }

    private void addWishlistToWineCollection(WineCollection wineCollection, OnWineCollectionLoadedListener listener) {
        getUserWishlist(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                updateWineCollectionWithWishlist(wineCollection, snapshot);
                listener.onWineCollectionLoaded(wineCollection);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("Firebase", "Error loading wishlist: " + error.getMessage());
                listener.onWineCollectionLoaded(wineCollection);
            }
        });
    }

    private void updateWineCollectionWithWishlist(WineCollection wineCollection, DataSnapshot snapshot) {
        for (DataSnapshot wineSnapshot : snapshot.getChildren()) {
            String wineId = wineSnapshot.getKey();
            if (wineId != null && wineCollection.getWineById(wineId) != null) {
                wineCollection.getWineById(wineId).setWhitelisted(true);
            }
        }
    }

    public void loadWishlist(OnWishlistLoadedListener listener) {
        FirebaseUser currentUser = getCurrentUser();
        if (currentUser == null) return;

        DatabaseReference wishlistRef = usersRef.child(currentUser.getUid()).child("wishlist");
        loadWishlistItems(wishlistRef, listener);
    }

    private void loadWishlistItems(DatabaseReference wishlistRef, OnWishlistLoadedListener listener) {
        wishlistRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Wine> wishlistWines = new ArrayList<>();
                processWishlistItems(snapshot, wishlistWines, listener);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("Firebase", "Error loading wishlist: " + error.getMessage());
            }
        });
    }

    private void processWishlistItems(DataSnapshot snapshot, List<Wine> wishlistWines, OnWishlistLoadedListener listener) {
        for (DataSnapshot wineSnapshot : snapshot.getChildren()) {
            String wineId = wineSnapshot.getKey();
            if (wineId != null) {
                loadWineForWishlist(wineId, wishlistWines, listener);
            }
        }
    }

    private void loadWineForWishlist(String wineId, List<Wine> wishlistWines, OnWishlistLoadedListener listener) {
        getWineById(wineId, new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot wineDataSnapshot) {
                Wine wine = wineDataSnapshot.getValue(Wine.class);
                if (wine != null) {
                    wine.setWhitelisted(true);
                    wishlistWines.add(wine);
                }
                if (listener != null) {
                    listener.onWishlistLoaded(wishlistWines);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("Firebase", "Error loading wine: " + error.getMessage());
            }
        });
    }


    public interface OnWishlistLoadedListener {
        void onWishlistLoaded(List<Wine> wishlist);
    }


}

