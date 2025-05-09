// src/main/java/com/example/jobsearch/service/JobApplicationService.java
package com.example.jobsearch.service;

import com.example.jobsearch.model.Job;
import com.example.jobsearch.model.JobApplication;
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
import java.util.Arrays;

@Service
public class JobApplicationService {

    @Autowired
    private ApiUtils apiUtils;

    // Store files in user's home directory for better persistence between app launches
    private static final String APP_DATA_DIR = System.getProperty("user.home") + File.separator + ".jobsearchapp";
    private static final String APPLICATIONS_FILE = APP_DATA_DIR + File.separator + "applications.csv";
    private List<JobApplication> applications = new ArrayList<>();
    
    @PostConstruct
    public void init() {
        // Create app data directory if it doesn't exist
        createAppDataDirectory();
        
        // First try to load from the user's home directory
        if (loadApplicationsFromHomeDir()) {
            System.out.println("Successfully loaded applications from home directory");
        } else {
            // If that fails, try to load from the project directory (for development)
            System.out.println("No applications found in home directory, trying project directory...");
            loadApplicationsFromProjectDir();
        }
        
        // If no applications are loaded, try to debug the issue
        if (applications.isEmpty()) {
            System.out.println("No applications were loaded. Trying to debug the CSV files...");
            debugCSVFiles();
        }
    }
    
    private void debugCSVFiles() {
        // Try to read the home directory file
        File homeFile = new File(APPLICATIONS_FILE);
        if (homeFile.exists()) {
            System.out.println("Found applications file at: " + APPLICATIONS_FILE);
            System.out.println("File size: " + homeFile.length() + " bytes");
            
            try (BufferedReader reader = new BufferedReader(new FileReader(homeFile))) {
                String header = reader.readLine();
                System.out.println("Header: " + header);
                
                // Count fields in header
                String[] headerFields = header.split(",");
                System.out.println("Header field count: " + headerFields.length);
                
                // Read first data line
                String firstLine = reader.readLine();
                if (firstLine != null) {
                    System.out.println("First data line (raw): " + firstLine);
                    
                    // Count fields without regex (simple splitting)
                    String[] simpleFields = firstLine.split(",");
                    System.out.println("Simple field count: " + simpleFields.length);
                    
                    // Print each field from the simple split for debugging
                    for (int i = 0; i < simpleFields.length; i++) {
                        System.out.println("Field " + i + ": " + simpleFields[i]);
                    }
                    
                    // Try manual parsing
                    parseAndAddApplicationManually(firstLine);
                } else {
                    System.out.println("No data lines found in the file");
                }
            } catch (IOException e) {
                System.err.println("Error reading file for debugging: " + e.getMessage());
            }
        }
    }
    
    private void parseAndAddApplicationManually(String line) {
        try {
            System.out.println("Attempting manual parsing of line...");
            
            // Split the line and preserve the values in quotes
            List<String> fields = new ArrayList<>();
            boolean inQuotes = false;
            StringBuilder currentField = new StringBuilder();
            
            for (char c : line.toCharArray()) {
                if (c == '\"') {
                    inQuotes = !inQuotes;
                    currentField.append(c);
                } else if (c == ',' && !inQuotes) {
                    fields.add(currentField.toString());
                    currentField = new StringBuilder();
                } else {
                    currentField.append(c);
                }
            }
            fields.add(currentField.toString()); // Add the last field
            
            System.out.println("Manually parsed fields: " + fields.size());
            for (int i = 0; i < fields.size(); i++) {
                System.out.println("Field " + i + ": " + fields.get(i));
            }
            
            // Check if we have at least the minimum fields needed
            if (fields.size() >= 9) {
                String title = unescapeCSV(fields.get(0));
                String company = unescapeCSV(fields.get(1));
                String location = unescapeCSV(fields.get(2));
                String description = unescapeCSV(fields.get(3));
                String url = unescapeCSV(fields.get(4));
                String salary = unescapeCSV(fields.get(5));
                
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                LocalDate datePosted = LocalDate.parse(unescapeCSV(fields.get(6)), formatter);
                LocalDate applicationDate = LocalDate.parse(unescapeCSV(fields.get(7)), formatter);
                
                String status = unescapeCSV(fields.get(8));
                
                // If there's no notes field, use empty string
                String notes = fields.size() > 9 ? unescapeCSV(fields.get(9)) : "";
                
                // Create job object
                Job job = new Job(title, company, location, description, url, salary, datePosted);
                
                // Create application object
                JobApplication app = new JobApplication(job, status, notes);
                app.setApplicationDate(applicationDate);
                
                applications.add(app);
                System.out.println("Successfully added application manually: " + title + " at " + company);
            }
        } catch (Exception e) {
            System.err.println("Error in manual parsing: " + e.getMessage());
            e.printStackTrace();
        }
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
    
    private boolean loadApplicationsFromHomeDir() {
        File file = new File(APPLICATIONS_FILE);
        if (!file.exists()) {
            System.out.println("No applications file found at: " + APPLICATIONS_FILE);
            return false; // No file to load from
        }
        
        return loadApplicationsFromFile(file);
    }
    
    private boolean loadApplicationsFromProjectDir() {
        // Try to load from project directory (for development)
        File projectFile = new File("applications.csv");
        if (!projectFile.exists()) {
            System.out.println("No applications file found in project directory");
            return false;
        }
        
        boolean success = loadApplicationsFromFile(projectFile);
        if (success) {
            // Copy the file to the home directory for future use
            try {
                Files.createDirectories(Paths.get(APP_DATA_DIR));
                Files.copy(projectFile.toPath(), Paths.get(APPLICATIONS_FILE));
                System.out.println("Copied applications.csv from project directory to: " + APPLICATIONS_FILE);
            } catch (IOException e) {
                System.err.println("Failed to copy applications.csv to home directory: " + e.getMessage());
            }
        }
        return success;
    }
    
    private boolean loadApplicationsFromFile(File file) {
        applications.clear();
        
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            // Skip header line
            String line = reader.readLine();
            if (line == null) {
                System.out.println("Applications file is empty or only contains header");
                return false;
            }
            
            // For debugging
            System.out.println("Applications file header: " + line);
            
            // Read each application
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            
            int linesRead = 0;
            boolean atLeastOneSuccess = false;
            
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) {
                    continue;
                }
                
                linesRead++;
                System.out.println("Reading line " + linesRead + ": " + line);
                
                try {
                    // Try different parsing methods
                    boolean parsed = parseUsingRegexSplit(line, formatter);
                    
                    if (!parsed) {
                        // Try manual parsing as a fallback
                        parseAndAddApplicationManually(line);
                    }
                    
                    atLeastOneSuccess = atLeastOneSuccess || parsed;
                } catch (Exception e) {
                    System.err.println("Error parsing line " + linesRead + ": " + e.getMessage());
                }
            }
            
            System.out.println("Loaded " + applications.size() + " job applications from " + file.getAbsolutePath());
            return atLeastOneSuccess || !applications.isEmpty();
        } catch (IOException e) {
            System.err.println("Error loading applications: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    private boolean parseUsingRegexSplit(String line, DateTimeFormatter formatter) {
        try {
            // Split by commas that are not inside quotes
            String[] parts = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");
            
            if (parts.length >= 9) { // Changed from 10 to 9 to handle missing notes
                // Extract job data
                String title = unescapeCSV(parts[0]);
                String company = unescapeCSV(parts[1]);
                String location = unescapeCSV(parts[2]);
                String description = unescapeCSV(parts[3]);
                String url = unescapeCSV(parts[4]);
                String salary = unescapeCSV(parts[5]);
                
                // Parse dates with error handling
                LocalDate datePosted;
                try {
                    datePosted = LocalDate.parse(unescapeCSV(parts[6]), formatter);
                } catch (Exception e) {
                    System.err.println("Error parsing datePosted: " + parts[6] + " - using current date");
                    datePosted = LocalDate.now();
                }
                
                // Create job object
                Job job = new Job(title, company, location, description, url, salary, datePosted);
                
                // Extract application data
                LocalDate applicationDate;
                try {
                    applicationDate = LocalDate.parse(unescapeCSV(parts[7]), formatter);
                } catch (Exception e) {
                    System.err.println("Error parsing applicationDate: " + parts[7] + " - using current date");
                    applicationDate = LocalDate.now();
                }
                
                String status = unescapeCSV(parts[8]);
                
                // Handle missing notes field
                String notes = parts.length > 9 ? unescapeCSV(parts[9]) : "";
                
                // Create application object
                JobApplication app = new JobApplication(job, status, notes);
                // Set correct application date (not today's date from constructor)
                app.setApplicationDate(applicationDate);
                
                applications.add(app);
                System.out.println("Added application: " + title + " at " + company);
                return true;
            } else {
                System.err.println("Line has fewer than expected fields: " + parts.length + " (expected at least 9)");
                // Print out the parts for debugging
                for (int i = 0; i < parts.length; i++) {
                    System.out.println("Field " + i + ": " + parts[i]);
                }
                return false;
            }
        } catch (Exception e) {
            System.err.println("Error in regex parsing: " + e.getMessage());
            return false;
        }
    }
    
    private void saveApplications() {
        try {
            // Ensure directory exists
            File dir = new File(APP_DATA_DIR);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            
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
                System.out.println("Saved " + applications.size() + " job applications to " + APPLICATIONS_FILE);
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