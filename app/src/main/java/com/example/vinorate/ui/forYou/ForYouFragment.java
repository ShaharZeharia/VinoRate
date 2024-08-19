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
import com.example.vinorate.Utilities.FirebaseManager;
import com.example.vinorate.Utilities.ImageLoader;
import com.example.vinorate.databinding.FragmentForYouBinding;
import com.google.android.material.textview.MaterialTextView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Random;

public class ForYouFragment extends Fragment {

    private static final String PREFS_NAME = "VinoRatePrefs";
    private static final String PREF_DAILY_WINE_ID = "dailyWineId";
    private static final String PREF_LAST_UPDATE = "lastUpdate";

    private FragmentForYouBinding binding;
    private WineCollection wineCollection;
    private Wine dailyRecommendation;
    private WineReviewAdapter reviewAdapter;
    private FirebaseManager firebaseManager;
    private final ArrayList<Review> reviewList = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentForYouBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        setupRecyclerView();

        firebaseManager = new FirebaseManager();

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
            // Time to generate a new daily recommendation
            firebaseManager.loadWineCollectionWithWishlist(wineCollection -> {
                this.wineCollection = wineCollection;
                dailyRecommendation = getRandomWine();
                assert dailyRecommendation != null;
                saveDailyRecommendation(dailyRecommendation);
                displayDailyRecommendation();
            });
        } else {
            String dailyWineId = preferences.getString(PREF_DAILY_WINE_ID, null);
            if (dailyWineId != null) {
                loadDailyRecommendationFromId(dailyWineId);
            }
        }
    }

    private void saveDailyRecommendation(Wine wine) {
        SharedPreferences preferences = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(PREF_DAILY_WINE_ID, wine.getId());
        editor.putLong(PREF_LAST_UPDATE, Calendar.getInstance().getTimeInMillis());
        editor.apply();
    }

    private void loadDailyRecommendationFromId(String wineId) {
        firebaseManager.getWineById(wineId, new ValueEventListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                dailyRecommendation = snapshot.getValue(Wine.class);
                if (dailyRecommendation != null) {
                    firebaseManager.getUserWishlist(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot wishlistSnapshot) {
                            dailyRecommendation.setWhitelisted(wishlistSnapshot.hasChild(dailyRecommendation.getId()));
                            displayDailyRecommendation();
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Log.e("ForYouFragment", "Error loading wishlist: " + error.getMessage());
                        }
                    });
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
                firebaseManager.toggleWishlistItem(dailyRecommendation.getId());
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

    private Wine getRandomWine() {
        if (wineCollection != null && !wineCollection.getAllWines().isEmpty()) {
            List<Wine> wines = new ArrayList<>(wineCollection.getAllWines().values());
            Random random = new Random();
            return wines.get(random.nextInt(wines.size()));
        }
        return null;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}

