// src/main/java/com/example/jobsearch/service/JobApplicationService.java
package com.example.jobsearch.service;

import com.example.jobsearch.model.Job;
import com.example.jobsearch.model.JobApplication;
import com.example.jobsearch.util.ApiUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.io.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
public class JobApplicationService {

    @Autowired
    private ApiUtils apiUtils;

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
        // Check if application already exists
        boolean exists = applications.stream()
            .anyMatch(app -> app.getJob().equals(application.getJob()));
        
        if (!exists) {
            applications.add(application);
            saveApplications();
        }
    }
    
    public void updateApplicationStatus(int index, String status) {
        if (index >= 0 && index < applications.size()) {
            applications.get(index).setStatus(status);
            saveApplications();
        }
    }
    
    public void updateApplicationNotes(int index, String notes) {
        if (index >= 0 && index < applications.size()) {
            applications.get(index).setNotes(notes);
            saveApplications();
        }
    }
    
    public void removeApplication(int index) {
        if (index >= 0 && index < applications.size()) {
            applications.remove(index);
            saveApplications();
        }
    }
    
    private void loadApplications() {
        applications.clear();
        
        File file = new File(APPLICATIONS_FILE);
        if (!file.exists()) {
            return; // No file to load from
        }
        
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            // Skip header line
            String line = reader.readLine();
            
            // Read each application
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) {
                    continue;
                }
                
                // Parse CSV line
                // Expected format: jobTitle,company,location,description,url,salary,datePosted,applicationDate,status,notes
                
                // Split by commas that are not inside quotes
                String[] parts = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");
                
                if (parts.length >= 10) {
                    // Extract job data
                    String title = unescapeCSV(parts[0]);
                    String company = unescapeCSV(parts[1]);
                    String location = unescapeCSV(parts[2]);
                    String description = unescapeCSV(parts[3]);
                    String url = unescapeCSV(parts[4]);
                    String salary = unescapeCSV(parts[5]);
                    LocalDate datePosted = LocalDate.parse(unescapeCSV(parts[6]), formatter);
                    
                    // Create job object
                    Job job = new Job(title, company, location, description, url, salary, datePosted);
                    
                    // Extract application data
                    LocalDate applicationDate = LocalDate.parse(unescapeCSV(parts[7]), formatter);
                    String status = unescapeCSV(parts[8]);
                    String notes = unescapeCSV(parts[9]);
                    
                    // Create application object
                    JobApplication app = new JobApplication(job, status, notes);
                    // Set correct application date (not today's date from constructor)
                    app.setApplicationDate(applicationDate);
                    
                    applications.add(app);
                }
            }
        } catch (IOException e) {
            System.err.println("Error loading applications: " + e.getMessage());
        }
    }
    
    private void saveApplications() {
        try (FileWriter writer = new FileWriter(APPLICATIONS_FILE)) {
            // Write header
            writer.write("jobTitle,company,location,description,url,salary,datePosted,applicationDate,status,notes\n");
            
            // Format for dates
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            
            // Write each application
            for (JobApplication app : applications) {
                Job job = app.getJob();
                
                writer.write(
                    apiUtils.escapeCSV(job.getTitle()) + "," +
                    apiUtils.escapeCSV(job.getCompany()) + "," +
                    apiUtils.escapeCSV(job.getLocation()) + "," +
                    apiUtils.escapeCSV(job.getDescription()) + "," +
                    apiUtils.escapeCSV(job.getUrl()) + "," +
                    apiUtils.escapeCSV(job.getSalary()) + "," +
                    apiUtils.escapeCSV(job.getDatePosted().format(formatter)) + "," +
                    apiUtils.escapeCSV(app.getApplicationDate().format(formatter)) + "," +
                    apiUtils.escapeCSV(app.getStatus()) + "," +
                    apiUtils.escapeCSV(app.getNotes()) + "\n"
                );
            }
        } catch (IOException e) {
            System.err.println("Error saving applications: " + e.getMessage());
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