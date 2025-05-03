// src/main/java/com/example/jobsearch/ui/JavaFXApplication.java
package com.example.jobsearch.ui;

import com.example.jobsearch.JobSearchApplication;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
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
        // Load the FXML file and setup the controller
        FXMLLoader loader = new FXMLLoader(new ClassPathResource("fxml/main-view.fxml").getURL());
        loader.setControllerFactory(context::getBean); // Use Spring to create controllers
        
        Parent root = loader.load();
        Scene scene = new Scene(root, 900, 700);
        
        primaryStage.setTitle("Job Search Application");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    @Override
    public void stop() {
        // Close the Spring context when the JavaFX application closes
        context.close();
        Platform.exit();
    }
}