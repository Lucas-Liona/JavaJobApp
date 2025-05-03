// src/main/java/com/example/jobsearch/model/Job.java
package com.example.jobsearch.model;

import java.time.LocalDate;

public class Job {
    private String id;
    private String title;
    private String company;
    private String location;
    private String description;
    private String url;
    private String salary;
    private LocalDate datePosted;
    
    // Constructor, getters, setters
    
    // Example of constructor
    public Job(String title, String company, String location, String description, 
               String url, String salary, LocalDate datePosted) {
        this.title = title;
        this.company = company;
        this.location = location;
        this.description = description;
        this.url = url;
        this.salary = salary;
        this.datePosted = datePosted;
    }
    
    // TODO: Add getters and setters
    
    // TODO: Add equals, hashCode, and toString methods
}