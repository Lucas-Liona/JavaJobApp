// src/main/java/com/example/jobsearch/service/AdzunaJobService.java
package com.example.jobsearch.service;

import com.example.jobsearch.config.AppConfig;
import com.example.jobsearch.model.Job;
import com.example.jobsearch.util.ApiUtils;
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
    
    @Autowired
    private ApiUtils apiUtils;
    
    public boolean testApiConnection() {
        try {
            // Build URL with query parameters for a simple test
            String url = UriComponentsBuilder.fromHttpUrl("https://api.adzuna.com/v1/api/jobs/us/search/1")
                .queryParam("app_id", appConfig.getAdzunaAppId())
                .queryParam("app_key", appConfig.getAdzunaApiKey())
                .queryParam("results_per_page", 1)
                .build()
                .toUriString();
            
            System.out.println("Testing Adzuna API connection with URL: " + url);
            
            // Make API call
            String response = restTemplate.getForObject(url, String.class);
            
            // If we got a response without exception, connection is working
            System.out.println("Adzuna API connection successful!");
            return (response != null);
        } catch (Exception e) {
            System.err.println("Adzuna API connection failed: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    public List<Job> searchJobs(String keywords, String location) {
        List<Job> jobList = new ArrayList<>();
        
        try {
            // Build URL with query parameters
            UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl("https://api.adzuna.com/v1/api/jobs/us/search/1")
                .queryParam("app_id", appConfig.getAdzunaAppId())
                .queryParam("app_key", appConfig.getAdzunaApiKey())
                .queryParam("results_per_page", 50)
                .queryParam("what", keywords);
            
            // Add location if provided
            if (location != null && !location.isEmpty() && !location.equalsIgnoreCase("remote")) {
                builder.queryParam("where", location);
            }
            
            // Add content flag to get full job description
            builder.queryParam("content-type", "application/json");
            
            String url = builder.build().toUriString();
            System.out.println("Searching Adzuna with URL: " + url);
            
            // Make API call
            String response = restTemplate.getForObject(url, String.class);
            
            // Parse response
            if (response != null) {
                JSONObject jsonResponse = new JSONObject(response);
                JSONArray results = jsonResponse.getJSONArray("results");
                
                System.out.println("Found " + results.length() + " jobs from Adzuna");
                
                // Process each job listing
                for (int i = 0; i < results.length(); i++) {
                    JSONObject jobData = results.getJSONObject(i);
                    
                    // Extract job details
                    String title = jobData.optString("title", "No Title");
                    String company = "Unknown Company";
                    if (jobData.has("company")) {
                        if (jobData.get("company") instanceof JSONObject) {
                            // If company is a JSON object, try to get the display_name property
                            JSONObject companyObj = jobData.getJSONObject("company");
                            if (companyObj.has("display_name")) {
                                company = companyObj.getString("display_name");
                            }
                        } else {
                            // If it's just a string, use it directly
                            company = jobData.getString("company");
                        }
                    }
                    
                    // Extract location
                    String jobLocation = "Not specified";
                    if (jobData.has("location") && jobData.getJSONObject("location").has("display_name")) {
                        jobLocation = jobData.getJSONObject("location").getString("display_name");
                    }
                    
                    // Extract description
                    String description = jobData.optString("description", "No description available");
                    
                    // Extract URL
                    String url_redirect = jobData.optString("redirect_url", "");
                    
                    // Extract salary
                    String salary = "Not specified";
                    if (jobData.has("salary_min") && jobData.has("salary_max")) {
                        double minSalary = jobData.getDouble("salary_min");
                        double maxSalary = jobData.getDouble("salary_max");
                        
                        if (minSalary > 0 && maxSalary > 0) {
                            salary = String.format("$%.0f - $%.0f", minSalary, maxSalary);
                        } else if (minSalary > 0) {
                            salary = String.format("$%.0f+", minSalary);
                        }
                    }
                    
                    // Extract date posted
                    LocalDate datePosted = LocalDate.now();
                    if (jobData.has("created")) {
                        datePosted = apiUtils.parseDate(jobData.getString("created"));
                    }
                    
                    // Create job object and add to list
                    Job job = new Job(title, company, jobLocation, description, url_redirect, salary, datePosted);
                    jobList.add(job);
                }
            }
        } catch (Exception e) {
            System.err.println("Error searching Adzuna jobs: " + e.getMessage());
            e.printStackTrace();
        }
        
        return jobList;
    }
}