package com.example.vinorate.ui.wishlist;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.vinorate.Adapters.WineAdapter;
import com.example.vinorate.Models.Wine;
import com.example.vinorate.Utilities.FirebaseManager;
import com.example.vinorate.databinding.FragmentWishlistBinding;

import java.util.ArrayList;
import java.util.List;

public class WishlistFragment extends Fragment {

    private FragmentWishlistBinding binding;
    private WineAdapter wineAdapter;
    private List<Wine> wishlistWines;
    private FirebaseManager firebaseManager;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentWishlistBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        wishlistWines = new ArrayList<>();
        wineAdapter = new WineAdapter((ArrayList<Wine>) wishlistWines);

        RecyclerView wishlistRecyclerView = binding.wishlistRecyclerView;
        wishlistRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        wishlistRecyclerView.setAdapter(wineAdapter);

        firebaseManager = new FirebaseManager();
        loadWishlistWines();

        return root;
    }

    @SuppressLint("NotifyDataSetChanged")
    private void loadWishlistWines() {
        firebaseManager.loadWishlist(wishlist -> {
            wishlistWines.clear();
            wishlistWines.addAll(wishlist);
            wineAdapter.notifyDataSetChanged();
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


