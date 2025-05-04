// src/main/java/com/example/jobsearch/model/JobApplication.java
package com.example.jobsearch.model;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

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
    public String getStatus() { return status; }
    public String getNotes() { return notes; }
    
    // Formatted date getter for TableView display
    public String getApplicationDateFormatted() {
        if (applicationDate == null) return "";
        return applicationDate.format(DateTimeFormatter.ofPattern("MM/dd/yyyy"));
    }
    
    // Getters for job properties (for TableView)
    public String getJobTitle() { return job.getTitle(); }
    public String getCompany() { return job.getCompany(); }
    
    // Setters
    public void setStatus(String status) { this.status = status; }
    public void setNotes(String notes) { this.notes = notes; }
    public void setApplicationDate(LocalDate applicationDate) { this.applicationDate = applicationDate; }
    
    @Override
    public String toString() {
        return "JobApplication{" +
               "job=" + job.getTitle() + " at " + job.getCompany() +
               ", applicationDate=" + applicationDate +
               ", status='" + status + '\'' +
               '}';
    }
}