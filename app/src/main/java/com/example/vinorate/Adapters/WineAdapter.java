package com.example.vinorate.Adapters;

import static java.lang.String.format;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatRatingBar;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.FragmentActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import com.example.vinorate.Interfaces.WineCallback;
import com.example.vinorate.Models.Wine;
import com.example.vinorate.R;
import com.example.vinorate.Utilities.ImageLoader;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.textview.MaterialTextView;

import java.util.ArrayList;


public class WineAdapter extends RecyclerView.Adapter<WineAdapter.WineViewHolder> {

    private final ArrayList<Wine> wines;
    private WineCallback wineCallback;

    public WineAdapter(ArrayList<Wine> wines) {
        this.wines = wines;
    }

    public void setWineCallback(WineCallback wineCallback) {
        this.wineCallback = wineCallback;
    }

    @SuppressLint("NotifyDataSetChanged")
    public void updateWines(ArrayList<Wine> newWines) {
        this.wines.clear();
        this.wines.addAll(newWines);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public WineViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.horizontal_wine_item, parent, false);
        return new WineViewHolder(view);
    }

    @SuppressLint("DefaultLocale")
    @Override
    public void onBindViewHolder(@NonNull WineViewHolder holder, int position) {
        Wine wine = getItem(position);

        ImageLoader.getInstance().load(wine.getPoster(), holder.wine_IMG_poster);
        holder.wine_LBL_name.setText(wine.getName());
        holder.wine_LBL_year.setText(String.valueOf(wine.getYear()));
        holder.wine_LBL_cost.setText(format("$%.2f", wine.getCost()));
        holder.wine_LBL_grapes.setText(wine.getGrapes());
        holder.wine_LBL_origin.setText(wine.getOrigin());
        holder.wine_LBL_overview.setText(wine.getOverview());
        holder.wine_RTNG_rating.setRating(wine.getRating());
        if (wine.isWhitelisted())
            holder.wine_IMG_wishlist.setImageResource(R.drawable.ic_wishlist_full);
        else
            holder.wine_IMG_wishlist.setImageResource(R.drawable.ic_wishlist_empty);

        holder.wine_CARD_data.setOnClickListener(v -> {
            FragmentActivity activity = (FragmentActivity) v.getContext();
            NavController navController = Navigation.findNavController(activity, R.id.nav_host_fragment_activity_main);
            Bundle args = new Bundle();
            args.putSerializable("wine", wine);

            navController.navigate(R.id.navigation_wine_details, args);
        });

    }


    @Override
    public int getItemCount() {
        return wines == null ? 0 : wines.size();
    }

    private Wine getItem(int position) {
        return wines.get(position);
    }

    public class WineViewHolder extends RecyclerView.ViewHolder {
        private final CardView wine_CARD_data;
        private final ShapeableImageView wine_IMG_poster;
        private final ShapeableImageView wine_IMG_wishlist;
        private final MaterialTextView wine_LBL_name;
        private final MaterialTextView wine_LBL_year;
        private final MaterialTextView wine_LBL_cost;
        private final MaterialTextView wine_LBL_grapes;
        private final MaterialTextView wine_LBL_origin;
        private final MaterialTextView wine_LBL_overview;
        private final AppCompatRatingBar wine_RTNG_rating;

        public WineViewHolder(@NonNull View itemView) {
            super(itemView);
            wine_CARD_data = itemView.findViewById(R.id.wine_CARD_data);
            wine_IMG_poster = itemView.findViewById(R.id.wine_IMG_poster);
            wine_IMG_wishlist = itemView.findViewById(R.id.wine_IMG_wishlist);
            wine_LBL_name = itemView.findViewById(R.id.wine_LBL_name);
            wine_LBL_year = itemView.findViewById(R.id.wine_LBL_year);
            wine_LBL_cost = itemView.findViewById(R.id.wine_LBL_cost);
            wine_LBL_grapes = itemView.findViewById(R.id.wine_LBL_grapes);
            wine_LBL_origin = itemView.findViewById(R.id.wine_LBL_origin);
            wine_LBL_overview = itemView.findViewById(R.id.wine_LBL_overview);
            wine_RTNG_rating = itemView.findViewById(R.id.wine_RTNG_rating);
            wine_IMG_wishlist.setOnClickListener(v -> {
                if (wineCallback != null)
                    wineCallback.wishlistButtonClicked(getItem(getAdapterPosition()), getAdapterPosition());
            });
        }
    }
}

