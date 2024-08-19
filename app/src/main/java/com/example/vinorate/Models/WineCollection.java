package com.example.vinorate.Models;

import androidx.annotation.NonNull;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class WineCollection implements Serializable {

    private final Map<String, Wine> wines;

    public WineCollection() {
        this.wines = new HashMap<>();
    }

    public void addWine(Wine wine) {
        wines.put(wine.getId(), wine);
    }

    public void removeWine(String wineId) {
        wines.remove(wineId);
    }

    public Wine getWineById(String wineId) {
        return wines.get(wineId);
    }

    public Map<String, Wine> getAllWines() {
        return wines;
    }

    @NonNull
    @Override
    public String toString() {
        return "WineCollection{" +
                "wines=" + wines +
                '}';
    }
}
