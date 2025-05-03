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
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import org.springframework.stereotype.Component;

import java.net.URL;
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
        
        // Load favorites
        loadFavorites();
    }
    
    private void loadFavorites() {
        favoriteJobs.clear();
        favoriteJobs.addAll(favoriteService.getFavorites());
    }
    
    // TODO: Implement handler methods for buttons
    
    @FXML
    private void handleRemoveFavorite(ActionEvent event) {
        Job selectedJob = favoritesTable.getSelectionModel().getSelectedItem();
        if (selectedJob != null) {
            int index = favoritesTable.getSelectionModel().getSelectedIndex();
            favoriteService.removeFavorite(index);
            loadFavorites(); // Refresh the list
        }
    }
    
    @FXML
    private void handleViewDetails(ActionEvent event) {
        // TODO: Implement view details functionality
    }
    
    @FXML
    private void handleRecordApplication(ActionEvent event) {
        // TODO: Implement record application functionality
    }
    
    // Show alert helper method (same as in SearchController)
}