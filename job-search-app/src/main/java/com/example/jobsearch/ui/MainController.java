// src/main/java/com/example/jobsearch/ui/MainController.java
package com.example.jobsearch.ui;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

@Component
public class MainController implements Initializable {

    private final ApplicationContext context;
    
    @FXML
    private TabPane tabPane;
    
    @FXML
    private Tab searchTab;
    
    @FXML
    private Tab favoritesTab;
    
    @FXML
    private Tab applicationsTab;
    
    public MainController(ApplicationContext context) {
        this.context = context;
    }
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            // Load each tab content
            searchTab.setContent(loadFxml("fxml/search-view.fxml"));
            favoritesTab.setContent(loadFxml("fxml/favorites-view.fxml"));
            applicationsTab.setContent(loadFxml("fxml/applications-view.fxml"));
            
            // Add listener to refresh content when tab is selected
            tabPane.getSelectionModel().selectedItemProperty().addListener((observable, oldTab, newTab) -> {
                if (newTab == favoritesTab) {
                    FavoritesController controller = context.getBean(FavoritesController.class);
                    controller.refreshFavorites();
                } else if (newTab == applicationsTab) {
                    ApplicationsController controller = context.getBean(ApplicationsController.class);
                    controller.refreshApplications();
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    private Parent loadFxml(String fxmlPath) throws IOException {
        FXMLLoader loader = new FXMLLoader(new ClassPathResource(fxmlPath).getURL());
        loader.setControllerFactory(context::getBean);
        return loader.load();
    }
}