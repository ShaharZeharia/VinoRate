package com.example.vinorate.ui.search;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.vinorate.Adapters.WineAdapter;
import com.example.vinorate.Models.Wine;
import com.example.vinorate.Models.WineCollection;
import com.example.vinorate.Utilities.FirebaseManager;
import com.example.vinorate.databinding.FragmentSearchBinding;

import java.util.ArrayList;
import java.util.List;


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

        firebaseManager.loadWineCollectionWithWishlist(wineCollection -> {
            this.wineCollection = wineCollection;
            wineAdapter.updateWines(new ArrayList<>(wineCollection.getAllWines().values()));
        });

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


