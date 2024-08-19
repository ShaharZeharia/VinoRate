package com.example.vinorate.ui.wishlist;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.vinorate.Adapters.WineAdapter;
import com.example.vinorate.Models.Review;
import com.example.vinorate.Models.Wine;
import com.example.vinorate.Utilities.FirebaseManager;
import com.example.vinorate.databinding.FragmentWishlistBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WishlistFragment extends Fragment {

    private FragmentWishlistBinding binding;
    private WineAdapter wineAdapter;
    private List<Wine> wishlistWines;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentWishlistBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        wishlistWines = new ArrayList<>();
        wineAdapter = new WineAdapter((ArrayList<Wine>) wishlistWines);

        RecyclerView wishlistRecyclerView = binding.wishlistRecyclerView;
        wishlistRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        wishlistRecyclerView.setAdapter(wineAdapter);

        loadWishlistWines();

        return root;
    }

    private void loadWishlistWines() {
        FirebaseManager firebaseManager = new FirebaseManager();
        firebaseManager.getUserWishlist(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                wishlistWines.clear();
                for (DataSnapshot wineSnapshot : snapshot.getChildren()) {
                    String wineId = wineSnapshot.getKey();
                    if (wineId != null) {
                        firebaseManager.getWineById(wineId, new ValueEventListener() {
                            @SuppressLint("NotifyDataSetChanged")
                            @Override
                            public void onDataChange(@NonNull DataSnapshot wineDataSnapshot) {
                                Wine wine = wineDataSnapshot.getValue(Wine.class);
                                if (wine != null) {
                                    DataSnapshot reviewsSnapshot = wineDataSnapshot.child("reviews");
                                    if (reviewsSnapshot.exists()) {
                                        try {
                                            Map<String, Review> reviewsMap = new HashMap<>();
                                            for (DataSnapshot reviewSnapshot : reviewsSnapshot.getChildren()) {
                                                Review review = reviewSnapshot.getValue(Review.class);
                                                if (review != null) {
                                                    reviewsMap.put(reviewSnapshot.getKey(), review);
                                                }
                                            }
                                            wine.setReviews(reviewsMap);
                                        } catch (ClassCastException e) {
                                            Log.e("Firebase", "Failed to cast reviews to Map: " + e.getMessage());
                                        }
                                    }
                                    wine.setWhitelisted(true);
                                    wishlistWines.add(wine);
                                    wineAdapter.notifyDataSetChanged();
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                Log.e("Firebase", "Error loading wine: " + error.getMessage());
                            }
                        });
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("Firebase", "Error loading wishlist: " + error.getMessage());
            }
        });

        wineAdapter.setWineCallback((wine, position) -> {
            firebaseManager.toggleWishlistItem(wine.getId());
            wishlistWines.remove(position);
            wineAdapter.notifyItemRemoved(position);
        });
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}


