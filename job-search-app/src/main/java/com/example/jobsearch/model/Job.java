// src/main/java/com/example/jobsearch/model/Job.java
package com.example.jobsearch.model;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

public class Job {
    private String id;
    private String title;
    private String company;
    private String location;
    private String description;
    private String url;
    private String salary;
    private LocalDate datePosted;
    
    // Constructor
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
    
    // Getters
    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getCompany() { return company; }
    public String getLocation() { return location; }
    public String getDescription() { return description; }
    public String getUrl() { return url; }
    public String getSalary() { return salary; }
    public LocalDate getDatePosted() { return datePosted; }
    
    // Formatted date getter for TableView display
    public String getDatePostedFormatted() {
        if (datePosted == null) return "";
        return datePosted.format(DateTimeFormatter.ofPattern("MM/dd/yyyy"));
    }
    
    // Setters
    public void setId(String id) { this.id = id; }
    public void setTitle(String title) { this.title = title; }
    public void setCompany(String company) { this.company = company; }
    public void setLocation(String location) { this.location = location; }
    public void setDescription(String description) { this.description = description; }
    public void setUrl(String url) { this.url = url; }
    public void setSalary(String salary) { this.salary = salary; }
    public void setDatePosted(LocalDate datePosted) { this.datePosted = datePosted; }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Job job = (Job) o;
        return Objects.equals(title, job.title) &&
               Objects.equals(company, job.company) &&
               Objects.equals(url, job.url);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(title, company, url);
    }
    
    @Override
    public String toString() {
        return "Job{" +
                "title='" + title + '\'' +
                ", company='" + company + '\'' +
                ", location='" + location + '\'' +
                ", salary='" + salary + '\'' +
                ", datePosted=" + datePosted +
                '}';
    }
}