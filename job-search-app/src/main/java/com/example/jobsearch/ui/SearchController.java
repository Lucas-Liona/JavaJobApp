// src/main/java/com/example/jobsearch/ui/SearchController.java
package com.example.jobsearch.ui;

import com.example.jobsearch.model.Job;
import com.example.jobsearch.model.JobApplication;
import com.example.jobsearch.service.AdzunaJobService;
import com.example.jobsearch.service.FavoriteService;
import com.example.jobsearch.service.GoogleJobService;
import com.example.jobsearch.service.JobApplicationService;
import com.example.jobsearch.util.ApiUtils;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.scene.Scene;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

@Component
public class SearchController implements Initializable {

    private final GoogleJobService googleJobService;
    private final AdzunaJobService adzunaJobService;
    private final FavoriteService favoriteService;
    private final JobApplicationService applicationService;
    private final ApiUtils apiUtils;
    
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
            JobApplicationService applicationService,
            ApiUtils apiUtils) {
        this.googleJobService = googleJobService;
        this.adzunaJobService = adzunaJobService;
        this.favoriteService = favoriteService;
        this.applicationService = applicationService;
        this.apiUtils = apiUtils;
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
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("datePostedFormatted"));
        
        // Set items to table
        resultsTable.setItems(jobResults);
        
        // Add double-click event handler for view details
        resultsTable.setRowFactory(tv -> {
            TableRow<Job> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    handleViewDetails(new ActionEvent());
                }
            });
            return row;
        });
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
        
        // Start search
        try {
            // Show loading indicator
            searchButton.setDisable(true);
            searchButton.setText("Searching...");
            
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
            applySalaryFilter();
            
            // Apply job type filter if selected
            String jobType = jobTypeCombo.getValue();
            if (!"All job types".equals(jobType)) {
                jobResults.removeIf(job -> 
                    !job.getTitle().toLowerCase().contains(jobType.toLowerCase()) &&
                    !job.getDescription().toLowerCase().contains(jobType.toLowerCase())
                );
            }
            
            // Apply experience level filter if selected
            String expLevel = experienceLevelCombo.getValue();
            if (!"All experience levels".equals(expLevel)) {
                jobResults.removeIf(job -> 
                    !job.getTitle().toLowerCase().contains(expLevel.toLowerCase().replace(" level", "")) &&
                    !job.getDescription().toLowerCase().contains(expLevel.toLowerCase().replace(" level", ""))
                );
            }
            
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Search Error", 
                     "An error occurred while searching for jobs: " + e.getMessage());
        } finally {
            // Reset button state
            searchButton.setDisable(false);
            searchButton.setText("Search Jobs");
        }
    }
    
    private void applySalaryFilter() {
        String salaryFilter = salaryFilterCombo.getValue();
        if ("No salary filter".equals(salaryFilter)) {
            return; // No filtering needed
        }
        
        // Extract minimum salary from filter selection
        int minSalary = 0;
        switch (salaryFilter) {
            case "$30,000+":
                minSalary = 30000;
                break;
            case "$50,000+":
                minSalary = 50000;
                break;
            case "$75,000+":
                minSalary = 75000;
                break;
            case "$100,000+":
                minSalary = 100000;
                break;
            case "$150,000+":
                minSalary = 150000;
                break;
        }
        
        // Apply filter
        if (minSalary > 0) {
            final int finalMinSalary = minSalary;
            jobResults.removeIf(job -> {
                String salary = job.getSalary();
                if ("Not specified".equals(salary)) {
                    return true; // Remove if no salary info
                }
                
                try {
                    // Try to extract numbers from salary string
                    String numericPart = salary.replaceAll("[^0-9]", " ")
                                              .trim().replaceAll("\\s+", " ");
                    String[] numbers = numericPart.split(" ");
                    
                    // Use the first number found
                    if (numbers.length > 0 && !numbers[0].isEmpty()) {
                        int jobSalary = Integer.parseInt(numbers[0]);
                        return jobSalary < finalMinSalary;
                    }
                    
                    return true; // Remove if can't parse
                } catch (Exception e) {
                    return true; // Remove if error
                }
            });
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
            Dialog<JobApplication> dialog = new Dialog<>();
            dialog.setTitle("Record Job Application");
            dialog.setHeaderText("Enter application details for:\n" + 
                                 selectedJob.getTitle() + " at " + selectedJob.getCompany());
            
            // Set the button types
            ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
            dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);
            
            // Create the status and notes fields
            ComboBox<String> statusCombo = new ComboBox<>();
            statusCombo.setItems(FXCollections.observableArrayList(
                "Applied", "Interview Scheduled", "Offer Received", "Rejected"
            ));
            statusCombo.getSelectionModel().selectFirst();
            
            TextArea notesArea = new TextArea();
            notesArea.setPromptText("Enter any notes (optional)");
            notesArea.setPrefRowCount(5);
            
            // Create layout
            VBox content = new VBox(10);
            content.getChildren().addAll(
                new Label("Status:"), statusCombo,
                new Label("Notes:"), notesArea
            );
            dialog.getDialogPane().setContent(content);
            
            // Request focus on the status field by default
            statusCombo.requestFocus();
            
            // Convert the result to a JobApplication object when the save button is clicked
            dialog.setResultConverter(dialogButton -> {
                if (dialogButton == saveButtonType) {
                    return new JobApplication(
                        selectedJob, 
                        statusCombo.getValue(), 
                        notesArea.getText()
                    );
                }
                return null;
            });
            
            // Show the dialog and process the result
            Optional<JobApplication> result = dialog.showAndWait();
            result.ifPresent(application -> {
                applicationService.addApplication(application);
                showAlert(Alert.AlertType.INFORMATION, "Success", "Application recorded.");
            });
        } else {
            showAlert(Alert.AlertType.WARNING, "Selection Required", "Please select a job first.");
        }
    }
    
    @FXML
    private void handleViewDetails(ActionEvent event) {
        Job selectedJob = resultsTable.getSelectionModel().getSelectedItem();
        if (selectedJob != null) {
            // Create a details dialog
            Dialog<Void> dialog = new Dialog<>();
            dialog.setTitle("Job Details");
            dialog.setHeaderText(selectedJob.getTitle() + " at " + selectedJob.getCompany());
            dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
            
            // Format description as HTML for better readability
            String htmlContent = String.format(
                "<html><body style='font-family: Arial; margin: 10px;'>" +
                "<h2>%s</h2>" +
                "<h3>%s</h3>" +
                "<p><strong>Location:</strong> %s</p>" +
                "<p><strong>Salary:</strong> %s</p>" +
                "<p><strong>Date Posted:</strong> %s</p>" +
                "<p><strong>URL:</strong> <a href='%s'>%s</a></p>" +
                "<hr><h3>Description:</h3>" +
                "<div>%s</div>" +
                "</body></html>",
                selectedJob.getTitle(),
                selectedJob.getCompany(),
                selectedJob.getLocation(),
                selectedJob.getSalary(),
                selectedJob.getDatePostedFormatted(),
                selectedJob.getUrl(),
                "Apply Online",
                selectedJob.getDescription().replace("\n", "<br>")
            );
            
            // Create WebView to display HTML content
            WebView webView = new WebView();
            webView.getEngine().loadContent(htmlContent);
            webView.setPrefSize(600, 400);
            
            dialog.getDialogPane().setContent(webView);
            dialog.getDialogPane().setPrefSize(650, 500);
            
            // Show the dialog
            dialog.showAndWait();
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
        
        // Show file chooser dialog
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Job Results");
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("CSV Files", "*.csv")
        );
        fileChooser.setInitialFileName("job_results.csv");
        
        File file = fileChooser.showSaveDialog(resultsTable.getScene().getWindow());
        if (file != null) {
            try (FileWriter writer = new FileWriter(file)) {
                // Write header
                writer.write("Title,Company,Location,Salary,Date Posted,URL\n");
                
                // Write jobs
                for (Job job : jobResults) {
                    writer.write(
                        apiUtils.escapeCSV(job.getTitle()) + "," +
                        apiUtils.escapeCSV(job.getCompany()) + "," +
                        apiUtils.escapeCSV(job.getLocation()) + "," +
                        apiUtils.escapeCSV(job.getSalary()) + "," +
                        apiUtils.escapeCSV(job.getDatePostedFormatted()) + "," +
                        apiUtils.escapeCSV(job.getUrl()) + "\n"
                    );
                }
                
                showAlert(Alert.AlertType.INFORMATION, "Success", 
                         "Results saved to " + file.getName());
            } catch (IOException e) {
                showAlert(Alert.AlertType.ERROR, "Save Error", 
                         "An error occurred while saving results: " + e.getMessage());
            }
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