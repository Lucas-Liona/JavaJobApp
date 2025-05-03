// src/main/java/com/example/jobsearch/config/AppConfig.java
package com.example.jobsearch.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class AppConfig {

    @Value("${google.api.key}")
    private String googleApiKey;
    
    @Value("${google.project.id}")
    private String googleProjectId;
    
    @Value("${adzuna.api.key}")
    private String adzunaApiKey;
    
    @Value("${adzuna.app.id}")
    private String adzunaAppId;

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
    
    // Getters for API credentials
    public String getGoogleApiKey() {
        return googleApiKey;
    }
    
    public String getGoogleProjectId() {
        return googleProjectId;
    }
    
    public String getAdzunaApiKey() {
        return adzunaApiKey;
    }
    
    public String getAdzunaAppId() {
        return adzunaAppId;
    }
}