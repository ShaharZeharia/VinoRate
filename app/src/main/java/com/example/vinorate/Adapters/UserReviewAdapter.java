package com.example.vinorate.Adapters;

import android.animation.ObjectAnimator;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatRatingBar;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.vinorate.Models.Review;
import com.example.vinorate.R;
import com.google.android.material.textview.MaterialTextView;

import java.util.ArrayList;

public class UserReviewAdapter extends RecyclerView.Adapter<UserReviewAdapter.UserReviewViewHolder> {

    private final ArrayList<Review> reviews;

    public UserReviewAdapter(ArrayList<Review> reviews) {
        this.reviews = reviews;
    }

    @NonNull
    @Override
    public UserReviewViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.horizontal_review_item_with_name_wine, parent, false);
        return new UserReviewViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserReviewViewHolder holder, int position) {
        Review review = reviews.get(position);

        holder.review_LBL_wine_name.setText(review.getWineName());
        holder.review_LBL_username.setText(review.getUsername());
        holder.review_LBL_comment.setText(review.getComment());
        holder.review_RTNG_rating.setRating(review.getRating());


        int maxLines = review.isCollapsed() ? Review.MAX_LINES_COLLAPSED : holder.review_LBL_comment.getLineCount();
        holder.review_LBL_comment.setMaxLines(maxLines);

        holder.review_CARD_data.setOnClickListener(v -> {
            boolean isCollapsed = review.isCollapsed();
            int startMaxLines = isCollapsed ? Review.MAX_LINES_COLLAPSED : holder.review_LBL_comment.getLineCount();
            int endMaxLines = isCollapsed ? holder.review_LBL_comment.getLineCount() : Review.MAX_LINES_COLLAPSED;

            ObjectAnimator animation = ObjectAnimator.ofInt(holder.review_LBL_comment, "maxLines", startMaxLines, endMaxLines);
            animation.setDuration(Math.abs(endMaxLines - startMaxLines) * 50L);
            animation.start();

            review.setCollapsed(!isCollapsed);
        });
    }

    @Override
    public int getItemCount() {
        return reviews.size();
    }

    public static class UserReviewViewHolder extends RecyclerView.ViewHolder {
        MaterialTextView review_LBL_wine_name;
        MaterialTextView review_LBL_username;
        MaterialTextView review_LBL_comment;
        AppCompatRatingBar review_RTNG_rating;
        CardView review_CARD_data;

        public UserReviewViewHolder(@NonNull View itemView) {
            super(itemView);
            review_LBL_wine_name = itemView.findViewById(R.id.review_LBL_wine_name);
            review_LBL_username = itemView.findViewById(R.id.review_LBL_username);
            review_LBL_comment = itemView.findViewById(R.id.review_LBL_comment);
            review_RTNG_rating = itemView.findViewById(R.id.review_RTNG_rating);
            review_CARD_data = itemView.findViewById(R.id.review_CARD_data);
        }
    }
}



