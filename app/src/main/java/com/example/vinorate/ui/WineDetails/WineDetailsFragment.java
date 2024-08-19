package com.example.vinorate.ui.WineDetails;

import static java.lang.String.format;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.vinorate.Adapters.WineReviewAdapter;
import com.example.vinorate.Models.Review;
import com.example.vinorate.Models.Wine;
import com.example.vinorate.R;
import com.example.vinorate.Utilities.FirebaseManager;
import com.example.vinorate.Utilities.ImageLoader;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textview.MaterialTextView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatRatingBar;

import java.util.ArrayList;
import java.util.Objects;

public class WineDetailsFragment extends Fragment {

    private Wine wine;
    private WineReviewAdapter reviewAdapter;
    private ArrayList<Review> reviews;
    private ShapeableImageView wishlistIcon;
    private FirebaseManager firebaseManager;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            wine = (Wine) getArguments().getSerializable("wine");
        }
        reviews = wine != null ? new ArrayList<>(wine.getReviews().values()) : new ArrayList<>();
        firebaseManager = new FirebaseManager();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_wine_details, container, false);

        if (getActivity() != null) {
            Objects.requireNonNull(((AppCompatActivity) getActivity()).getSupportActionBar()).setDisplayHomeAsUpEnabled(false);
        }

        ShapeableImageView wineImgPoster = view.findViewById(R.id.wine_IMG_poster);
        MaterialTextView wineLblName = view.findViewById(R.id.wine_LBL_name);
        MaterialTextView wineLblYear = view.findViewById(R.id.wine_LBL_year);
        MaterialTextView wineLblCost = view.findViewById(R.id.wine_LBL_cost);
        MaterialTextView wineLblOrigin = view.findViewById(R.id.wine_LBL_origin);
        MaterialTextView wineLblGrapes = view.findViewById(R.id.wine_LBL_grapes);
        AppCompatRatingBar wineRatingBar = view.findViewById(R.id.wine_RTNG_rating);
        wishlistIcon = view.findViewById(R.id.wine_IMG_wishlist);

        TextInputEditText reviewEtText = view.findViewById(R.id.review_ET_text);
        AppCompatRatingBar reviewRatingBar = view.findViewById(R.id.review_rating_bar);
        Button addReviewButton = view.findViewById(R.id.add_review_button);
        RecyclerView reviewsRecyclerView = view.findViewById(R.id.reviews_recycler_view);
        MaterialTextView wineLblOverview = view.findViewById(R.id.wine_LBL_overview);

        ImageLoader.getInstance().load(wine.getPoster(), wineImgPoster);
        wineLblName.setText(wine.getName());
        wineLblYear.setText(String.valueOf(wine.getYear()));
        wineLblCost.setText(format("$%s", wine.getCost()));
        wineLblOrigin.setText(wine.getOrigin());
        wineLblGrapes.setText(wine.getGrapes());
        wineRatingBar.setRating(wine.getRating());
        wineLblOverview.setText(wine.getOverview());

        reviewAdapter = new WineReviewAdapter(getContext(), reviews);
        reviewsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        reviewsRecyclerView.setAdapter(reviewAdapter);

        updateWishlistIcon();

        wishlistIcon.setOnClickListener(v -> {
            wine.setWhitelisted(!wine.isWhitelisted());
            firebaseManager.toggleWishlistItem(wine.getId());
            updateWishlistIcon();
        });

        addReviewButton.setOnClickListener(v -> {
            String reviewText = Objects.requireNonNull(reviewEtText.getText()).toString();
            float rating = reviewRatingBar.getRating();

            if (!reviewText.isEmpty() && rating > 0) {
                FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                if (currentUser != null) {
                    String username = currentUser.getDisplayName() != null ? currentUser.getDisplayName() : "Anonymous";

                    Review newReview = new Review(username, wine.getName(), wine.getId(), reviewText, rating);
                    String reviewId = firebaseManager.addReview(wine.getId(), newReview);

                    if (reviewId != null) {
                        wine.addReview(reviewId, newReview);
                        reviews.add(newReview);
                        reviewAdapter.notifyItemInserted(reviews.size() - 1);
                        firebaseManager.updateWineRating(wine.getId(), wineRatingBar::setRating);

                        reviewEtText.setText("");
                        reviewRatingBar.setRating(0);
                    }
                }
            }
        });


        return view;
    }

    private void updateWishlistIcon() {
        if (wine.isWhitelisted()) {
            wishlistIcon.setImageResource(R.drawable.ic_wishlist_full);
        } else {
            wishlistIcon.setImageResource(R.drawable.ic_wishlist_empty);
        }
    }
}

