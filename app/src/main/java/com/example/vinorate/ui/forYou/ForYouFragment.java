package com.example.vinorate.ui.forYou;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatRatingBar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.vinorate.Adapters.WineReviewAdapter;
import com.example.vinorate.Models.Review;
import com.example.vinorate.Models.Wine;
import com.example.vinorate.Models.WineCollection;
import com.example.vinorate.R;
import com.example.vinorate.Utilities.ImageLoader;
import com.example.vinorate.databinding.FragmentForYouBinding;
import com.google.android.material.textview.MaterialTextView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseException;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class ForYouFragment extends Fragment {

    private static final String PREFS_NAME = "VinoRatePrefs";
    private static final String PREF_DAILY_WINE_ID = "dailyWineId";
    private static final String PREF_LAST_UPDATE = "lastUpdate";

    private FragmentForYouBinding binding;
    private WineCollection wineCollection;
    private Wine dailyRecommendation;
    private WineReviewAdapter reviewAdapter;
    private final ArrayList<Review> reviewList = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentForYouBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // Set up the RecyclerView for reviews
        setupRecyclerView();

        // Load or generate the daily recommendation
        loadOrGenerateDailyRecommendation();

        return root;
    }

    private void setupRecyclerView() {
        reviewAdapter = new WineReviewAdapter(getContext(), reviewList);
        binding.reviewsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.reviewsRecyclerView.setAdapter(reviewAdapter);
    }

    private void loadOrGenerateDailyRecommendation() {
        SharedPreferences preferences = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        long lastUpdate = preferences.getLong(PREF_LAST_UPDATE, 0);
        long currentTime = Calendar.getInstance().getTimeInMillis();

        if (currentTime - lastUpdate >= 24 * 60 * 60 * 1000) {
            loadWineCollection();
        } else {
            String dailyWineId = preferences.getString(PREF_DAILY_WINE_ID, null);
            if (dailyWineId != null) {
                loadDailyRecommendationFromId(dailyWineId);
            }
        }
    }

    private void loadWineCollection() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference winesRef = database.getReference("WineCollection").child("allWines");
        winesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                wineCollection = new WineCollection();

                for (DataSnapshot wineSnapshot : snapshot.getChildren()) {
                    try {
                        Wine wine = wineSnapshot.getValue(Wine.class);
                        if (wine != null) {
                            DataSnapshot reviewsSnapshot = wineSnapshot.child("reviews");
                            if (reviewsSnapshot.exists()) {
                                Map<String, Review> reviewsMap = (Map<String, Review>) reviewsSnapshot.getValue();
                                if (reviewsMap != null) {
                                    wine.setReviews(reviewsMap);
                                }
                            }
                            wineCollection.addWine(wine);
                        } else {
                            Log.e("ForYouFragment", "Wine is null for key: " + wineSnapshot.getKey());
                        }
                    } catch (DatabaseException e) {
                        Log.e("ForYouFragment", "Error parsing wine: " + e.getMessage());
                    }
                }

                if (!wineCollection.getAllWines().isEmpty()) {
                    dailyRecommendation = getRandomWine();
                    saveDailyRecommendation(dailyRecommendation);
                    displayDailyRecommendation();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("ForYouFragment", "Database error: " + error.getMessage());
            }
        });
    }

    private Wine getRandomWine() {
        List<Wine> wines = new ArrayList<>(wineCollection.getAllWines().values());
        Random random = new Random();
        return wines.get(random.nextInt(wines.size()));
    }

    private void saveDailyRecommendation(Wine wine) {
        SharedPreferences preferences = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(PREF_DAILY_WINE_ID, wine.getId());
        editor.putLong(PREF_LAST_UPDATE, Calendar.getInstance().getTimeInMillis());
        editor.apply();
    }

    private void loadDailyRecommendationFromId(String wineId) {
        DatabaseReference wineRef = FirebaseDatabase.getInstance().getReference("WineCollection").child("allWines").child(wineId);
        wineRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                dailyRecommendation = snapshot.getValue(Wine.class);
                if (dailyRecommendation != null) {
                    displayDailyRecommendation();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("ForYouFragment", "Error loading daily recommendation: " + error.getMessage());
            }
        });
    }

    @SuppressLint("NotifyDataSetChanged")
    private void displayDailyRecommendation() {
        if (dailyRecommendation != null) {
            ImageLoader.getInstance().load(dailyRecommendation.getPoster(), binding.getRoot().findViewById(R.id.wine_IMG_poster));

            MaterialTextView wineLblName = binding.getRoot().findViewById(R.id.wine_LBL_name);
            MaterialTextView wineLblYear = binding.getRoot().findViewById(R.id.wine_LBL_year);
            MaterialTextView wineLblCost = binding.getRoot().findViewById(R.id.wine_LBL_cost);
            MaterialTextView wineLblOrigin = binding.getRoot().findViewById(R.id.wine_LBL_origin);
            MaterialTextView wineLblGrapes = binding.getRoot().findViewById(R.id.wine_LBL_grapes);
            AppCompatRatingBar wineRtngRating = binding.getRoot().findViewById(R.id.wine_RTNG_rating);
            ImageView wineWishlistIcon = binding.getRoot().findViewById(R.id.wine_IMG_wishlist);

            wineLblName.setText(dailyRecommendation.getName());
            wineLblYear.setText(String.valueOf(dailyRecommendation.getYear()));
            wineLblCost.setText(String.format("$%s", dailyRecommendation.getCost()));
            wineLblOrigin.setText(dailyRecommendation.getOrigin());
            wineLblGrapes.setText(dailyRecommendation.getGrapes());
            wineRtngRating.setRating(dailyRecommendation.getRating());
            binding.wineLBLOverview.setText(dailyRecommendation.getOverview());

            updateWishlistIcon(wineWishlistIcon, dailyRecommendation);

            wineWishlistIcon.setOnClickListener(v -> {
                dailyRecommendation.setWhitelisted(!dailyRecommendation.isWhitelisted());
                updateWishlistIcon(wineWishlistIcon, dailyRecommendation);
            });

            reviewList.clear();
            reviewList.addAll(dailyRecommendation.getReviews().values());
            reviewAdapter.notifyDataSetChanged();
        }
    }

    private void updateWishlistIcon(ImageView wineWishlistIcon, Wine wine) {
        if (wine.isWhitelisted()) {
            wineWishlistIcon.setImageResource(R.drawable.ic_wishlist_full);
        } else {
            wineWishlistIcon.setImageResource(R.drawable.ic_wishlist_empty);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}


