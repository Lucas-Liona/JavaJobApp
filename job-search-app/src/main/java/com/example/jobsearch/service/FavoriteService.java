// src/main/java/com/example/jobsearch/service/FavoriteService.java
package com.example.jobsearch.service;

import com.example.jobsearch.model.Job;
import com.example.jobsearch.util.ApiUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
public class FavoriteService {

    @Autowired
    private ApiUtils apiUtils;

    // Store files in user's home directory for better persistence between app launches
    private static final String APP_DATA_DIR = System.getProperty("user.home") + File.separator + ".jobsearchapp";
    private static final String FAVORITES_FILE = APP_DATA_DIR + File.separator + "favorite_jobs.csv";
    private List<Job> favoriteJobs = new ArrayList<>();
    
    @PostConstruct
    public void init() {
        // Create app data directory if it doesn't exist
        createAppDataDirectory();
        loadFavorites();
    }
    
    private void createAppDataDirectory() {
        try {
            Path dirPath = Paths.get(APP_DATA_DIR);
            if (!Files.exists(dirPath)) {
                Files.createDirectories(dirPath);
                System.out.println("Created application data directory: " + APP_DATA_DIR);
            }
        } catch (IOException e) {
            System.err.println("Error creating application data directory: " + e.getMessage());
        }
    }
    
    public List<Job> getFavorites() {
        return favoriteJobs;
    }
    
    public void addFavorite(Job job) {
        // Check if job already exists in favorites
        boolean exists = favoriteJobs.stream()
            .anyMatch(favJob -> favJob.equals(job));
        
        if (!exists) {
            favoriteJobs.add(job);
            saveFavorites();
        }
    }
    
    public void removeFavorite(int index) {
        if (index >= 0 && index < favoriteJobs.size()) {
            favoriteJobs.remove(index);
            saveFavorites();
        }
    }
    
    private void loadFavorites() {
        favoriteJobs.clear();
        
        File file = new File(FAVORITES_FILE);
        if (!file.exists()) {
            System.out.println("No favorites file found at: " + FAVORITES_FILE);
            return; // No file to load from
        }
        
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            // Skip header line
            String line = reader.readLine();
            
            // Read each favorite job
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) {
                    continue;
                }
                
                // Parse CSV line
                // Expected format: title,company,location,description,url,salary,datePosted
                
                // Split by commas that are not inside quotes
                String[] parts = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");
                
                if (parts.length >= 7) {
                    String title = unescapeCSV(parts[0]);
                    String company = unescapeCSV(parts[1]);
                    String location = unescapeCSV(parts[2]);
                    String description = unescapeCSV(parts[3]);
                    String url = unescapeCSV(parts[4]);
                    String salary = unescapeCSV(parts[5]);
                    LocalDate datePosted = LocalDate.parse(unescapeCSV(parts[6]), formatter);
                    
                    Job job = new Job(title, company, location, description, url, salary, datePosted);
                    favoriteJobs.add(job);
                }
            }
            System.out.println("Loaded " + favoriteJobs.size() + " favorite jobs from " + FAVORITES_FILE);
        } catch (IOException e) {
            System.err.println("Error loading favorites: " + e.getMessage());
        }
    }
    
    private void saveFavorites() {
        try {
            // Ensure directory exists
            File dir = new File(APP_DATA_DIR);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            
            try (FileWriter writer = new FileWriter(FAVORITES_FILE)) {
                // Write header
                writer.write("title,company,location,description,url,salary,datePosted\n");
                
                // Format for dates
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                
                // Write each favorite job
                for (Job job : favoriteJobs) {
                    writer.write(
                        apiUtils.escapeCSV(job.getTitle()) + "," +
                        apiUtils.escapeCSV(job.getCompany()) + "," +
                        apiUtils.escapeCSV(job.getLocation()) + "," +
                        apiUtils.escapeCSV(job.getDescription()) + "," +
                        apiUtils.escapeCSV(job.getUrl()) + "," +
                        apiUtils.escapeCSV(job.getSalary()) + "," +
                        apiUtils.escapeCSV(job.getDatePosted().format(formatter)) + "\n"
                    );
                }
                System.out.println("Saved " + favoriteJobs.size() + " favorite jobs to " + FAVORITES_FILE);
            }
        } catch (IOException e) {
            System.err.println("Error saving favorites: " + e.getMessage());
        }
    }
    
    private String unescapeCSV(String input) {
        if (input == null) {
            return "";
        }
        
        // If the input starts and ends with quotes, remove them and unescape any doubled quotes
        if (input.startsWith("\"") && input.endsWith("\"")) {
            String content = input.substring(1, input.length() - 1);
            return content.replace("\"\"", "\"");
        }
        return input;
    }
}