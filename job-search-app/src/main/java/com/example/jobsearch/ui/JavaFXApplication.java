// src/main/java/com/example/jobsearch/ui/JavaFXApplication.java
package com.example.jobsearch.ui;

import com.example.jobsearch.JobSearchApplication;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.io.ClassPathResource;
import java.io.IOException;


public class JavaFXApplication extends Application {

    private ConfigurableApplicationContext context;

    @Override
    public void init() {
        // Get the Spring context from our main class
        context = JobSearchApplication.getContext();
    }

    @Override
    public void start(Stage primaryStage) throws IOException {
        try {
            // Load the FXML file and setup the controller
            FXMLLoader loader = new FXMLLoader(new ClassPathResource("fxml/main-view.fxml").getURL());
            loader.setControllerFactory(context::getBean); // Use Spring to create controllers
            
            Parent root = loader.load();
            Scene scene = new Scene(root, 1000, 700);
            
            // Add stylesheet
            String cssPath = getClass().getResource("/css/modern-style.css").toExternalForm();
            scene.getStylesheets().add(cssPath);
            
            // Load image with specified dimensions (will scale while maintaining aspect ratio)
            Image icon = new Image(getClass().getResourceAsStream("/images/OnBoardIcon.png"), 
                                1024, 1536,    // Width & height to scale to
                                true,        // Preserve ratio
                                true);       // Smooth scaling

            primaryStage.getIcons().add(icon);

            primaryStage.setTitle("On Board!");
            primaryStage.setScene(scene);
            primaryStage.show();
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Error loading application: " + e.getMessage());
        }
    }

    @Override
    public void stop() {
        // Close the Spring context when the JavaFX application closes
        context.close();
        Platform.exit();
    }
}