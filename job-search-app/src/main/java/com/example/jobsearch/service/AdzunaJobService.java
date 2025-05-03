// src/main/java/com/example/jobsearch/service/AdzunaJobService.java
package com.example.jobsearch.service;

import com.example.jobsearch.config.AppConfig;
import com.example.jobsearch.model.Job;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class AdzunaJobService {

    @Autowired
    private RestTemplate restTemplate;
    
    @Autowired
    private AppConfig appConfig;
    
    public List<Job> searchJobs(String keywords, String location) {
        List<Job> jobList = new ArrayList<>();
        
        // Build URL with query parameters
        String url = UriComponentsBuilder.fromHttpUrl("https://api.adzuna.com/v1/api/jobs/us/search/1")
            .queryParam("app_id", appConfig.getAdzunaAppId())
            .queryParam("app_key", appConfig.getAdzunaApiKey())
            .queryParam("results_per_page", 50)
            .queryParam("what", keywords)
            .queryParam("where", location)
            .build()
            .toUriString();
        
        // Make API call
        String response = restTemplate.getForObject(url, String.class);
        
        // Parse response
        if (response != null) {
            JSONObject jsonResponse = new JSONObject(response);
            JSONArray results = jsonResponse.getJSONArray("results");
            
            // Process each job listing
            for (int i = 0; i < results.length(); i++) {
                // TODO: Extract job details and add to jobList
                
                // Example:
                JSONObject jobData = results.getJSONObject(i);
                
                // Extract job details...
                
                // Add to list
                // jobList.add(new Job(...));
            }
        }
        
        return jobList;
    }
}