// src/main/java/com/example/jobsearch/model/JobApplication.java
package com.example.jobsearch.model;

import java.time.LocalDate;

public class JobApplication {
    private Job job;
    private LocalDate applicationDate;
    private String status; // e.g., "Applied", "Interview Scheduled", "Rejected", "Offer Received"
    private String notes;
    
    public JobApplication(Job job, String status, String notes) {
        this.job = job;
        this.applicationDate = LocalDate.now();
        this.status = status;
        this.notes = notes;
    }
    
    // Getters
    public Job getJob() { return job; }
    public LocalDate getApplicationDate() { return applicationDate; }
    public String getStatus() { return status; } // Added this missing getter
    public String getNotes() { return notes; }
    
    // Setters
    public void setStatus(String status) { this.status = status; }
    public void setNotes(String notes) { this.notes = notes; }
    
    // Other methods...
}