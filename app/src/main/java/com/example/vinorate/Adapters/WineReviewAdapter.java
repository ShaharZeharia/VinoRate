package com.example.vinorate.Adapters;

import android.animation.ObjectAnimator;
import android.content.Context;
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

public class WineReviewAdapter extends RecyclerView.Adapter<WineReviewAdapter.ReviewViewHolder> {

    private final ArrayList<Review> reviews;

    public WineReviewAdapter(Context context, ArrayList<Review> reviews) {
        this.reviews = reviews;
    }

    @NonNull
    @Override
    public ReviewViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.horizontal_review_item, parent, false);
        return new ReviewViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReviewViewHolder holder, int position) {
        Review review = getItem(position);

        holder.review_LBL_username.setText(review.getUsername());
        holder.review_LBL_comment.setText(review.getComment());
        holder.review_RTNG_rating.setRating(review.getRating());

        holder.review_LBL_comment.post(() -> {
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
        });
    }

    @Override
    public int getItemCount() {
        return reviews == null ? 0 : reviews.size();
    }

    private Review getItem(int position) {
        return reviews.get(position);
    }

    public static class ReviewViewHolder extends RecyclerView.ViewHolder {
        private final CardView review_CARD_data;
        private final MaterialTextView review_LBL_username;
        private final MaterialTextView review_LBL_comment;
        private final AppCompatRatingBar review_RTNG_rating;

        public ReviewViewHolder(@NonNull View itemView) {
            super(itemView);
            review_CARD_data = itemView.findViewById(R.id.review_CARD_data);
            review_LBL_username = itemView.findViewById(R.id.review_LBL_username);
            review_LBL_comment = itemView.findViewById(R.id.review_LBL_comment);
            review_RTNG_rating = itemView.findViewById(R.id.review_RTNG_rating);
        }
    }
}

