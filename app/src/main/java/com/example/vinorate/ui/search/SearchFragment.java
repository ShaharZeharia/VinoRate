package com.example.vinorate.ui.search;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.vinorate.Adapters.WineAdapter;
import com.example.vinorate.Models.Review;
import com.example.vinorate.Models.Wine;
import com.example.vinorate.Models.WineCollection;
import com.example.vinorate.Utilities.FirebaseManager;
import com.example.vinorate.databinding.FragmentSearchBinding;
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

public class SearchFragment extends Fragment {

    private FragmentSearchBinding binding;
    private WineAdapter wineAdapter;
    private WineCollection wineCollection;
    private FirebaseManager firebaseManager;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentSearchBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        firebaseManager = new FirebaseManager();
        wineCollection = new WineCollection();

        RecyclerView searchRecyclerView = binding.searchLSTWines;
        searchRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        wineAdapter = new WineAdapter(new ArrayList<>(wineCollection.getAllWines().values()));
        searchRecyclerView.setAdapter(wineAdapter);

        loadWineCollection();

        SearchView searchView = binding.searchText;
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                filterWines(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterWines(newText);
                return true;
            }
        });

        // Callback for wishlist icon click
        wineAdapter.setWineCallback((wine, position) -> {
            wine.setWhitelisted(!wine.isWhitelisted());
            firebaseManager.toggleWishlistItem(wine.getId());
            wineAdapter.notifyItemChanged(position);
        });

        return root;
    }

    private void loadWineCollection() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference winesRef = database.getReference("WineCollection").child("allWines");
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        winesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                wineCollection = new WineCollection();

                for (DataSnapshot wineSnapshot : snapshot.getChildren()) {
                    Wine wine = wineSnapshot.getValue(Wine.class);
                    if (wine != null) {
                        DataSnapshot reviewsSnapshot = wineSnapshot.child("reviews");
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
                        wineCollection.addWine(wine);
                    }
                }
                if (currentUser != null) {
                    firebaseManager.getUserWishlist(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            for (DataSnapshot wineSnapshot : snapshot.getChildren()) {
                                String wineId = wineSnapshot.getKey();
                                if (wineId != null && wineCollection.getWineById(wineId) != null) {
                                    wineCollection.getWineById(wineId).setWhitelisted(true);
                                }
                            }
                            wineAdapter.updateWines(new ArrayList<>(wineCollection.getAllWines().values()));
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Log.e("Firebase", "Error loading wishlist: " + error.getMessage());
                        }
                    });
                } else {
                    wineAdapter.updateWines(new ArrayList<>(wineCollection.getAllWines().values()));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("Firebase", "Error loading wines: " + error.getMessage());
            }
        });
    }

    private void filterWines(String query) {
        List<Wine> filteredWines = new ArrayList<>();
        for (Wine wine : wineCollection.getAllWines().values()) {
            if (wine.getName().toLowerCase().contains(query.toLowerCase())) {
                filteredWines.add(wine);
            }
        }
        wineAdapter.updateWines(new ArrayList<>(filteredWines));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}


