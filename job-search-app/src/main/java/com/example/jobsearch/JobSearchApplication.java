// src/main/java/com/example/jobsearch/JobSearchApplication.java
package com.example.jobsearch;

import com.example.jobsearch.ui.JavaFXApplication;
import javafx.application.Application;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
public class JobSearchApplication {

    private static ConfigurableApplicationContext context;

    public static void main(String[] args) {
        // Store the Spring context to be used by JavaFX
        context = new SpringApplicationBuilder(JobSearchApplication.class)
            .headless(false)
            .run(args);
        
        // Launch the JavaFX application
        Application.launch(JavaFXApplication.class, args);
    }
    
    public static ConfigurableApplicationContext getContext() {
        return context;
    }
}