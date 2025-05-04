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
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.util.Optional;
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
        jobTitleColumn.setCellValueFactory(new PropertyValueFactory<>("jobTitle"));
        companyColumn.setCellValueFactory(new PropertyValueFactory<>("company"));
        applicationDateColumn.setCellValueFactory(new PropertyValueFactory<>("applicationDateFormatted"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        notesColumn.setCellValueFactory(new PropertyValueFactory<>("notes"));
        
        // Set items to table
        applicationsTable.setItems(applications);
        
        // Add double-click event handler for view details
        applicationsTable.setRowFactory(tv -> {
            TableRow<JobApplication> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    handleUpdateStatus(new ActionEvent());
                }
            });
            return row;
        });
        
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
    
    @FXML
    private void handleUpdateStatus(ActionEvent event) {
        JobApplication selectedApp = applicationsTable.getSelectionModel().getSelectedItem();
        if (selectedApp != null) {
            int selectedIndex = applicationsTable.getSelectionModel().getSelectedIndex();
            
            // Create a dialog for status selection
            Dialog<String> dialog = new Dialog<>();
            dialog.setTitle("Update Application Status");
            dialog.setHeaderText("Update status for:\n" + 
                                 selectedApp.getJobTitle() + " at " + selectedApp.getCompany());
            
            // Set the button types
            ButtonType updateButtonType = new ButtonType("Update", ButtonBar.ButtonData.OK_DONE);
            dialog.getDialogPane().getButtonTypes().addAll(updateButtonType, ButtonType.CANCEL);
            
            // Create the status combo box
            ComboBox<String> statusCombo = new ComboBox<>();
            statusCombo.setItems(FXCollections.observableArrayList(
                "Applied", "Interview Scheduled", "Offer Received", "Rejected"
            ));
            statusCombo.setValue(selectedApp.getStatus());
            
            dialog.getDialogPane().setContent(statusCombo);
            
            // Convert the result to a status string when the update button is clicked
            dialog.setResultConverter(dialogButton -> {
                if (dialogButton == updateButtonType) {
                    return statusCombo.getValue();
                }
                return null;
            });
            
            // Show the dialog and process the result
            Optional<String> result = dialog.showAndWait();
            result.ifPresent(status -> {
                applicationService.updateApplicationStatus(selectedIndex, status);
                loadApplications();
                statusFilterCombo.getSelectionModel().selectFirst(); // Reset filter to "All"
            });
        } else {
            showAlert(Alert.AlertType.WARNING, "Selection Required", "Please select an application first.");
        }
    }
    
    @FXML
    private void handleUpdateNotes(ActionEvent event) {
        JobApplication selectedApp = applicationsTable.getSelectionModel().getSelectedItem();
        if (selectedApp != null) {
            int selectedIndex = applicationsTable.getSelectionModel().getSelectedIndex();
            
            // Create a dialog for notes update
            Dialog<String> dialog = new Dialog<>();
            dialog.setTitle("Update Application Notes");
            dialog.setHeaderText("Update notes for:\n" + 
                                selectedApp.getJobTitle() + " at " + selectedApp.getCompany());
            
            // Set the button types
            ButtonType updateButtonType = new ButtonType("Update", ButtonBar.ButtonData.OK_DONE);
            dialog.getDialogPane().getButtonTypes().addAll(updateButtonType, ButtonType.CANCEL);
            
            // Create the notes text area
            TextArea notesArea = new TextArea();
            notesArea.setText(selectedApp.getNotes());
            notesArea.setPrefRowCount(10);
            notesArea.setPrefColumnCount(40);
            
            dialog.getDialogPane().setContent(notesArea);
            
            // Convert the result to a notes string when the update button is clicked
            dialog.setResultConverter(dialogButton -> {
                if (dialogButton == updateButtonType) {
                    return notesArea.getText();
                }
                return null;
            });
            
            // Show the dialog and process the result
            Optional<String> result = dialog.showAndWait();
            result.ifPresent(notes -> {
                applicationService.updateApplicationNotes(selectedIndex, notes);
                loadApplications();
            });
        } else {
            showAlert(Alert.AlertType.WARNING, "Selection Required", "Please select an application first.");
        }
    }
    
    @FXML
    private void handleRemoveApplication(ActionEvent event) {
        JobApplication selectedApp = applicationsTable.getSelectionModel().getSelectedItem();
        if (selectedApp != null) {
            // Ask for confirmation
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirm Removal");
            alert.setHeaderText("Remove Application");
            alert.setContentText("Are you sure you want to remove the application for:\n" +
                                selectedApp.getJobTitle() + " at " + selectedApp.getCompany() + "?");
            
            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                int selectedIndex = applicationsTable.getSelectionModel().getSelectedIndex();
                applicationService.removeApplication(selectedIndex);
                loadApplications();
            }
        } else {
            showAlert(Alert.AlertType.WARNING, "Selection Required", "Please select an application first.");
        }
    }
    
    @FXML
    private void handleExportResume(ActionEvent event) {
        JobApplication selectedApp = applicationsTable.getSelectionModel().getSelectedItem();
        if (selectedApp != null) {
            // Show file chooser dialog
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Export Application to CSV");
            fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("CSV Files", "*.csv")
            );
            fileChooser.setInitialFileName(
                selectedApp.getCompany().replaceAll("[^a-zA-Z0-9]", "_") + "_application.csv"
            );
            
            File file = fileChooser.showSaveDialog(applicationsTable.getScene().getWindow());
            if (file != null) {
                try (FileWriter writer = new FileWriter(file)) {
                    // Write header
                    writer.write("Job Title,Company,Location,Application Date,Status,Notes,URL\n");
                    
                    // Write application data
                    writer.write(
                        escapeCSV(selectedApp.getJobTitle()) + "," +
                        escapeCSV(selectedApp.getCompany()) + "," +
                        escapeCSV(selectedApp.getJob().getLocation()) + "," +
                        escapeCSV(selectedApp.getApplicationDateFormatted()) + "," +
                        escapeCSV(selectedApp.getStatus()) + "," +
                        escapeCSV(selectedApp.getNotes()) + "," +
                        escapeCSV(selectedApp.getJob().getUrl()) + "\n"
                    );
                    
                    showAlert(Alert.AlertType.INFORMATION, "Success", 
                             "Application exported to " + file.getName());
                } catch (IOException e) {
                    showAlert(Alert.AlertType.ERROR, "Export Error", 
                             "An error occurred while exporting: " + e.getMessage());
                }
            }
        } else {
            showAlert(Alert.AlertType.WARNING, "Selection Required", "Please select an application first.");
        }
    }
    
    private String escapeCSV(String input) {
        if (input == null) {
            return "";
        }
        
        // If the input contains commas, quotes, or newlines, wrap it in quotes
        if (input.contains(",") || input.contains("\"") || input.contains("\n")) {
            // Double up any quotes already in the string
            String escaped = input.replace("\"", "\"\"");
            return "\"" + escaped + "\"";
        }
        return input;
    }
    
    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}