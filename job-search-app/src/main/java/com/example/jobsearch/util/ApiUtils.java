// src/main/java/com/example/jobsearch/util/ApiUtils.java
package com.example.jobsearch.util;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class ApiUtils {
    
    /**
     * Extracts a salary range from a JSON object based on the API response format
     */
    public String extractSalary(JSONObject compensationInfo) {
        if (compensationInfo == null) {
            return "Not specified";
        }
        
        try {
            // Check for Google's compensation format
            if (compensationInfo.has("entries")) {
                JSONArray entries = compensationInfo.getJSONArray("entries");
                
                if (entries.length() > 0) {
                    JSONObject entry = entries.getJSONObject(0);
                    
                    // Check if there's a range
                    if (entry.has("min") && entry.has("max") && 
                        entry.getJSONObject("min").has("units") && 
                        entry.getJSONObject("max").has("units")) {
                        
                        long minUnits = entry.getJSONObject("min").getLong("units");
                        long maxUnits = entry.getJSONObject("max").getLong("units");
                        
                        // Format as range with K/year for readability
                        if (minUnits > 0 && maxUnits > 0) {
                            if (minUnits >= 1000 && maxUnits >= 1000) {
                                return String.format("$%.0fK - $%.0fK/year", 
                                        minUnits / 1000.0, maxUnits / 1000.0);
                            } else {
                                return String.format("$%d - $%d/year", minUnits, maxUnits);
                            }
                        } else if (minUnits > 0) {
                            // Only min available
                            if (minUnits >= 1000) {
                                return String.format("$%.0fK+/year", minUnits / 1000.0);
                            } else {
                                return String.format("$%d+/year", minUnits);
                            }
                        }
                    }
                    
                    // Check if there's a single amount
                    if (entry.has("amount") && entry.getJSONObject("amount").has("units")) {
                        long units = entry.getJSONObject("amount").getLong("units");
                        
                        if (units > 0) {
                            if (units >= 1000) {
                                return String.format("$%.0fK/year", units / 1000.0);
                            } else {
                                return String.format("$%d/year", units);
                            }
                        }
                    }
                }
            }
            
            // Generic text extraction for other formats
            if (compensationInfo.has("description")) {
                String description = compensationInfo.getString("description");
                
                // Try to extract salary range from text with regex
                Pattern pattern = Pattern.compile("\\$([\\d,]+)\\s*-\\s*\\$([\\d,]+)");
                Matcher matcher = pattern.matcher(description);
                
                if (matcher.find()) {
                    return "$" + matcher.group(1) + " - $" + matcher.group(2);
                }
                
                // Try to extract single salary amount
                pattern = Pattern.compile("\\$([\\d,]+)");
                matcher = pattern.matcher(description);
                
                if (matcher.find()) {
                    return "$" + matcher.group(1);
                }
                
                // Return the description if it mentions salary
                if (description.toLowerCase().contains("salary") || 
                    description.contains("$") || 
                    description.toLowerCase().contains("compensation")) {
                    return description;
                }
            }
            
            return "Not specified";
        } catch (Exception e) {
            return "Not specified";
        }
    }
    
    /**
     * Parses a date string to LocalDate, handles multiple formats
     */
    public LocalDate parseDate(String dateString) {
        if (dateString == null || dateString.isEmpty()) {
            return LocalDate.now();
        }
        
        try {
            // Try ISO format first (most common API format)
            if (dateString.contains("T")) {
                return LocalDate.parse(dateString.substring(0, dateString.indexOf("T")));
            }
            
            // Try common formats
            String[] formats = {
                "yyyy-MM-dd", "MM/dd/yyyy", "dd/MM/yyyy", "yyyy/MM/dd",
                "MMM d, yyyy", "d MMM yyyy", "yyyy-MM-dd'Z'",
                "EEE MMM dd HH:mm:ss zzz yyyy"
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
    
    /**
     * Truncates a string to a specified length and adds ellipsis if needed
     */
    public String truncate(String text, int maxLength) {
        if (text == null || text.length() <= maxLength) {
            return text;
        }
        return text.substring(0, maxLength - 3) + "...";
    }
}