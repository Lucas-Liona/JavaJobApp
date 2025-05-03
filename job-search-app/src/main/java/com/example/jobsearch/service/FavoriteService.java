// src/main/java/com/example/jobsearch/service/FavoriteService.java
package com.example.jobsearch.service;

import com.example.jobsearch.model.Job;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

@Service
public class FavoriteService {

    private static final String FAVORITES_FILE = "favorite_jobs.csv";
    private List<Job> favoriteJobs = new ArrayList<>();
    
    @PostConstruct
    public void init() {
        loadFavorites();
    }
    
    public List<Job> getFavorites() {
        return favoriteJobs;
    }
    
    public void addFavorite(Job job) {
        favoriteJobs.add(job);
        saveFavorites();
    }
    
    public void removeFavorite(int index) {
        if (index >= 0 && index < favoriteJobs.size()) {
            favoriteJobs.remove(index);
            saveFavorites();
        }
    }
    
    // TODO: Implement loading favorites from CSV
    private void loadFavorites() {
        // Implementation to load favorites from file
    }
    
    // TODO: Implement saving favorites to CSV
    private void saveFavorites() {
        // Implementation to save favorites to file
    }
}