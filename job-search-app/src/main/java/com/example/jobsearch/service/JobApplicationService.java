// src/main/java/com/example/jobsearch/service/JobApplicationService.java
package com.example.jobsearch.service;

import com.example.jobsearch.model.Job;
import com.example.jobsearch.model.JobApplication;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

@Service
public class JobApplicationService {

    private static final String APPLICATIONS_FILE = "applications.csv";
    private List<JobApplication> applications = new ArrayList<>();
    
    @PostConstruct
    public void init() {
        loadApplications();
    }
    
    public List<JobApplication> getApplications() {
        return applications;
    }
    
    public void addApplication(JobApplication application) {
        applications.add(application);
        saveApplications();
    }
    
    public void updateApplicationStatus(int index, String status) {
        if (index >= 0 && index < applications.size()) {
            // Update status
            // applications.get(index).setStatus(status);
            saveApplications();
        }
    }
    
    public void updateApplicationNotes(int index, String notes) {
        if (index >= 0 && index < applications.size()) {
            // Update notes
            // applications.get(index).setNotes(notes);
            saveApplications();
        }
    }
    
    // TODO: Implement loading applications from CSV
    private void loadApplications() {
        // Implementation to load applications from file
    }
    
    // TODO: Implement saving applications to CSV
    private void saveApplications() {
        // Implementation to save applications to file
    }
}