// src/main/java/com/example/jobsearch/ui/FavoritesController.java
package com.example.jobsearch.ui;

import com.example.jobsearch.model.Job;
import com.example.jobsearch.model.JobApplication;
import com.example.jobsearch.service.FavoriteService;
import com.example.jobsearch.service.JobApplicationService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebView;
import org.springframework.stereotype.Component;

import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

@Component
public class FavoritesController implements Initializable {

    private final FavoriteService favoriteService;
    private final JobApplicationService applicationService;
    
    @FXML private TableView<Job> favoritesTable;
    @FXML private TableColumn<Job, String> titleColumn;
    @FXML private TableColumn<Job, String> companyColumn;
    @FXML private TableColumn<Job, String> locationColumn;
    @FXML private TableColumn<Job, String> salaryColumn;
    
    private ObservableList<Job> favoriteJobs = FXCollections.observableArrayList();
    
    public FavoritesController(FavoriteService favoriteService, JobApplicationService applicationService) {
        this.favoriteService = favoriteService;
        this.applicationService = applicationService;
    }
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Configure table columns
        titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        companyColumn.setCellValueFactory(new PropertyValueFactory<>("company"));
        locationColumn.setCellValueFactory(new PropertyValueFactory<>("location"));
        salaryColumn.setCellValueFactory(new PropertyValueFactory<>("salary"));
        
        // Set items to table
        favoritesTable.setItems(favoriteJobs);
        
        // Add double-click event handler for view details
        favoritesTable.setRowFactory(tv -> {
            TableRow<Job> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    handleViewDetails(new ActionEvent());
                }
            });
            return row;
        });
        
        // Load favorites
        loadFavorites();
    }
    
    private void loadFavorites() {
        favoriteJobs.clear();
        favoriteJobs.addAll(favoriteService.getFavorites());
    }
    
    @FXML
    private void handleRemoveFavorite(ActionEvent event) {
        Job selectedJob = favoritesTable.getSelectionModel().getSelectedItem();
        if (selectedJob != null) {
            // Ask for confirmation
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirm Removal");
            alert.setHeaderText("Remove Favorite");
            alert.setContentText("Are you sure you want to remove the favorite job:\n" +
                                selectedJob.getTitle() + " at " + selectedJob.getCompany() + "?");
            
            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                int index = favoritesTable.getSelectionModel().getSelectedIndex();
                favoriteService.removeFavorite(index);
                loadFavorites(); // Refresh the list
            }
        } else {
            showAlert(Alert.AlertType.WARNING, "Selection Required", "Please select a job first.");
        }
    }
    
    @FXML
    private void handleViewDetails(ActionEvent event) {
        Job selectedJob = favoritesTable.getSelectionModel().getSelectedItem();
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
    private void handleRecordApplication(ActionEvent event) {
        Job selectedJob = favoritesTable.getSelectionModel().getSelectedItem();
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
    
    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}