// src/main/java/com/example/jobsearch/util/ApiUtils.java
package com.example.jobsearch.util;

import org.json.JSONObject;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

@Component
public class ApiUtils {
    
    /**
     * Extracts a salary range from a JSON object
     */
    public String extractSalary(JSONObject compensationInfo) {
        if (compensationInfo == null) {
            return "Not specified";
        }
        
        try {
            // Implementation details depend on API response format
            // TODO: Implement based on specific API response structure
            return "Not specified";
        } catch (Exception e) {
            return "Not specified";
        }
    }
    
    /**
     * Parses a date string to LocalDate
     */
    public LocalDate parseDate(String dateString) {
        if (dateString == null || dateString.isEmpty()) {
            return LocalDate.now();
        }
        
        try {
            // Try ISO format
            if (dateString.contains("T")) {
                return LocalDate.parse(dateString.substring(0, dateString.indexOf("T")));
            }
            
            // Try other common formats
            String[] formats = {
                "yyyy-MM-dd", "MM/dd/yyyy", "dd/MM/yyyy", "yyyy/MM/dd"
            };
            
            for (String format : formats) {
                try {
                    return LocalDate.parse(dateString, DateTimeFormatter.ofPattern(format));
                } catch (DateTimeParseException e) {
                    // Try next format
                }
            }
            
            // Default to today if all parsing fails
            return LocalDate.now();
        } catch (Exception e) {
            return LocalDate.now();
        }
    }
    
    /**
     * Escapes a string for CSV output
     */
    public String escapeCSV(String input) {
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
}