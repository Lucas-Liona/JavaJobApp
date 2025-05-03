// src/main/java/com/example/jobsearch/ui/ApplicationsController.java
package com.example.jobsearch.ui;

import com.example.jobsearch.model.JobApplication;
import com.example.jobsearch.service.JobApplicationService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import org.springframework.stereotype.Component;

import java.net.URL;
import java.util.ResourceBundle;

@Component
public class ApplicationsController implements Initializable {

    private final JobApplicationService applicationService;
    
    @FXML private ComboBox<String> statusFilterCombo;
    @FXML private TableView<JobApplication> applicationsTable;
    @FXML private TableColumn<JobApplication, String> jobTitleColumn;
    @FXML private TableColumn<JobApplication, String> companyColumn;
    @FXML private TableColumn<JobApplication, String> applicationDateColumn;
    @FXML private TableColumn<JobApplication, String> statusColumn;
    @FXML private TableColumn<JobApplication, String> notesColumn;
    
    private ObservableList<JobApplication> applications = FXCollections.observableArrayList();
    
    public ApplicationsController(JobApplicationService applicationService) {
        this.applicationService = applicationService;
    }
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Initialize status filter combo
        statusFilterCombo.setItems(FXCollections.observableArrayList(
            "All", "Applied", "Interview Scheduled", "Offer Received", "Rejected"
        ));
        statusFilterCombo.getSelectionModel().selectFirst();
        statusFilterCombo.setOnAction(this::handleStatusFilter);
        
        // Configure table columns
        // TODO: Configure table columns with proper property values
        
        // Set items to table
        applicationsTable.setItems(applications);
        
        // Load applications
        loadApplications();
    }
    
    private void loadApplications() {
        applications.clear();
        applications.addAll(applicationService.getApplications());
    }
    
    private void handleStatusFilter(ActionEvent event) {
        String selectedStatus = statusFilterCombo.getValue();
        
        if ("All".equals(selectedStatus)) {
            loadApplications();
        } else {
            applications.clear();
            // Filter by selected status
            applicationService.getApplications().stream()
                .filter(app -> selectedStatus.equals(app.getStatus()))
                .forEach(applications::add);
        }
    }
    
    // TODO: Implement handler methods for buttons
    
    @FXML
    private void handleUpdateStatus(ActionEvent event) {
        // TODO: Implement update status functionality
    }
    
    @FXML
    private void handleUpdateNotes(ActionEvent event) {
        // TODO: Implement update notes functionality
    }
    
    @FXML
    private void handleRemoveApplication(ActionEvent event) {
        // TODO: Implement remove application functionality
    }
    
    @FXML
    private void handleExportResume(ActionEvent event) {
        // TODO: Implement export resume functionality
    }
    
    // Show alert helper method (same as in other controllers)
}