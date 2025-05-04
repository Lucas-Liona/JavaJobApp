// src/main/java/com/example/jobsearch/service/GoogleJobService.java
package com.example.jobsearch.service;

import com.example.jobsearch.config.AppConfig;
import com.example.jobsearch.model.Job;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class GoogleJobService {

    @Autowired
    private RestTemplate restTemplate;
    
    @Autowired
    private AppConfig appConfig;

    public boolean testApiConnection() {
    try {
        // Create API endpoint for a simple call (list companies)
        String endpoint = "https://jobs.googleapis.com/v3/projects/" + 
                          appConfig.getGoogleProjectId() + 
                          "/tenants/default";
        
        // Create request headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("X-Goog-Api-Key", appConfig.getGoogleApiKey());
        
        // Create HTTP entity
        HttpEntity<String> entity = new HttpEntity<>(headers);
        
        // Make a simple GET request
        restTemplate.exchange(endpoint, HttpMethod.GET, entity, String.class);
        
        System.out.println("Google Cloud Talent API connection successful!");
        return true;
    } catch (Exception e) {
        System.err.println("Google Cloud Talent API connection failed: " + e.getMessage());
        e.printStackTrace();
        return false;
        }
    }
    
    public List<Job> searchJobs(String keywords, String location) {
        List<Job> jobList = new ArrayList<>();
        
        // Create API endpoint
        String endpoint = "https://jobs.googleapis.com/v3/projects/" + 
                          appConfig.getGoogleProjectId() + 
                          "/jobs:search";
        
        // Create request headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("X-Goog-Api-Key", appConfig.getGoogleApiKey());
        
        // Create request body
        JSONObject requestBody = new JSONObject();
        requestBody.put("query", keywords);
        
        // Add location filter if provided
        if (location != null && !location.isEmpty()) {
            JSONObject locationFilter = new JSONObject();
            JSONObject address = new JSONObject();
            address.put("regionCode", "US");
            address.put("locality", location);
            locationFilter.put("address", address);
            locationFilter.put("distanceInMiles", 30);
            
            JSONObject jobQuery = new JSONObject();
            jobQuery.put("locationFilters", new JSONArray().put(locationFilter));
            requestBody.put("jobQuery", jobQuery);
        }
        
        // Set result count limit
        requestBody.put("pageSize", 50);
        
        // Create HTTP entity
        HttpEntity<String> entity = new HttpEntity<>(requestBody.toString(), headers);
        
        // Make API call
        String response = restTemplate.postForObject(endpoint, entity, String.class);
        
        // Parse response
        if (response != null) {
            JSONObject jsonResponse = new JSONObject(response);
            JSONArray matchingJobs = jsonResponse.optJSONArray("matchingJobs");
            
            if (matchingJobs != null) {
                // Process each job listing
                for (int i = 0; i < matchingJobs.length(); i++) {
                    // TODO: Extract job details and add to jobList
                    // Similar to the previous code but adapted for Spring Boot
                    
                    // Example:
                    JSONObject matchingJob = matchingJobs.getJSONObject(i);
                    JSONObject jobData = matchingJob.getJSONObject("job");
                    
                    // Extract job details...
                    
                    // Add to list
                    // jobList.add(new Job(...));
                }
            }
        }
        
        return jobList;
    }
}