// src/main/java/com/example/jobsearch/ui/SearchController.java
package com.example.jobsearch.ui;

import com.example.jobsearch.model.Job;
import com.example.jobsearch.model.JobApplication;
import com.example.jobsearch.service.AdzunaJobService;
import com.example.jobsearch.service.FavoriteService;
import com.example.jobsearch.service.GoogleJobService;
import com.example.jobsearch.service.JobApplicationService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import org.springframework.stereotype.Component;

import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

@Component
public class SearchController implements Initializable {

    private final GoogleJobService googleJobService;
    private final AdzunaJobService adzunaJobService;
    private final FavoriteService favoriteService;
    private final JobApplicationService applicationService;
    
    @FXML private TextField keywordsField;
    @FXML private TextField locationField;
    @FXML private ComboBox<String> jobTypeCombo;
    @FXML private ComboBox<String> experienceLevelCombo;
    @FXML private ComboBox<String> salaryFilterCombo;
    @FXML private ComboBox<String> apiSourceCombo;
    @FXML private Button searchButton;
    @FXML private TableView<Job> resultsTable;
    @FXML private TableColumn<Job, String> titleColumn;
    @FXML private TableColumn<Job, String> companyColumn;
    @FXML private TableColumn<Job, String> locationColumn;
    @FXML private TableColumn<Job, String> salaryColumn;
    @FXML private TableColumn<Job, String> dateColumn;
    
    private ObservableList<Job> jobResults = FXCollections.observableArrayList();
    
    public SearchController(
            GoogleJobService googleJobService,
            AdzunaJobService adzunaJobService,
            FavoriteService favoriteService,
            JobApplicationService applicationService) {
        this.googleJobService = googleJobService;
        this.adzunaJobService = adzunaJobService;
        this.favoriteService = favoriteService;
        this.applicationService = applicationService;
    }
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Initialize combo boxes
        jobTypeCombo.setItems(FXCollections.observableArrayList(
            "All job types", "Full-time", "Part-time", "Contract", "Internship"
        ));
        jobTypeCombo.getSelectionModel().selectFirst();
        
        experienceLevelCombo.setItems(FXCollections.observableArrayList(
            "All experience levels", "Entry level", "Mid level", "Senior level"
        ));
        experienceLevelCombo.getSelectionModel().selectFirst();
        
        salaryFilterCombo.setItems(FXCollections.observableArrayList(
            "No salary filter", "$30,000+", "$50,000+", "$75,000+", "$100,000+", "$150,000+"
        ));
        salaryFilterCombo.getSelectionModel().selectFirst();
        
        apiSourceCombo.setItems(FXCollections.observableArrayList(
            "Google Cloud Talent", "Adzuna", "Both (combined results)"
        ));
        apiSourceCombo.getSelectionModel().selectFirst();
        
        // Configure table columns
        titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        companyColumn.setCellValueFactory(new PropertyValueFactory<>("company"));
        locationColumn.setCellValueFactory(new PropertyValueFactory<>("location"));
        salaryColumn.setCellValueFactory(new PropertyValueFactory<>("salary"));
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("datePosted"));
        
        // Set items to table
        resultsTable.setItems(jobResults);
    }
    
    @FXML
    private void handleSearch(ActionEvent event) {
        // Clear previous results
        jobResults.clear();
        
        String keywords = keywordsField.getText();
        String location = locationField.getText();
        
        if (keywords.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Input Required", "Please enter job keywords.");
            return;
        }
        
        // TODO: Get selected values from combos and apply filters
        
        try {
            // Select API source and perform search
            String apiSource = apiSourceCombo.getValue();
            
            if ("Google Cloud Talent".equals(apiSource) || "Both (combined results)".equals(apiSource)) {
                List<Job> googleJobs = googleJobService.searchJobs(keywords, location);
                jobResults.addAll(googleJobs);
            }
            
            if ("Adzuna".equals(apiSource) || "Both (combined results)".equals(apiSource)) {
                List<Job> adzunaJobs = adzunaJobService.searchJobs(keywords, location);
                jobResults.addAll(adzunaJobs);
            }
            
            // Apply salary filter if selected
            // TODO: Implement salary filtering
            
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Search Error", 
                     "An error occurred while searching for jobs: " + e.getMessage());
        }
    }
    
    @FXML
    private void handleAddToFavorites(ActionEvent event) {
        Job selectedJob = resultsTable.getSelectionModel().getSelectedItem();
        if (selectedJob != null) {
            favoriteService.addFavorite(selectedJob);
            showAlert(Alert.AlertType.INFORMATION, "Success", "Job added to favorites.");
        } else {
            showAlert(Alert.AlertType.WARNING, "Selection Required", "Please select a job first.");
        }
    }
    
    @FXML
    private void handleRecordApplication(ActionEvent event) {
        Job selectedJob = resultsTable.getSelectionModel().getSelectedItem();
        if (selectedJob != null) {
            // Show dialog to enter application details
            // TODO: Implement dialog to collect status and notes
            
            String status = "Applied"; // Default status
            String notes = ""; // Default notes
            
            JobApplication application = new JobApplication(selectedJob, status, notes);
            applicationService.addApplication(application);
            showAlert(Alert.AlertType.INFORMATION, "Success", "Application recorded.");
        } else {
            showAlert(Alert.AlertType.WARNING, "Selection Required", "Please select a job first.");
        }
    }
    
    @FXML
    private void handleViewDetails(ActionEvent event) {
        Job selectedJob = resultsTable.getSelectionModel().getSelectedItem();
        if (selectedJob != null) {
            // Show job details dialog
            // TODO: Implement dialog to show full job details
        } else {
            showAlert(Alert.AlertType.WARNING, "Selection Required", "Please select a job first.");
        }
    }
    
    @FXML
    private void handleSaveResults(ActionEvent event) {
        if (jobResults.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "No Results", "There are no job results to save.");
            return;
        }
        
        // TODO: Implement saving to CSV
        // Example:
        try (FileWriter writer = new FileWriter("job_results.csv")) {
            // Write header
            writer.write("Title,Company,Location,Salary,Date Posted,URL\n");
            
            // Write jobs
            for (Job job : jobResults) {
                // TODO: Implement CSV formatting and writing
            }
            
            showAlert(Alert.AlertType.INFORMATION, "Success", "Results saved to job_results.csv");
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Save Error", 
                     "An error occurred while saving results: " + e.getMessage());
        }
    }
    
    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}