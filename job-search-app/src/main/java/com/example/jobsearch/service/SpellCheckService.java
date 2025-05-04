package com.example.jobsearch.service;

import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Map;

@Service
public interface SpellCheckService {
    // Check if a term needs correction
    boolean needsCorrection(String term);
    
    // Get suggested corrections for a term
    List<String> getSuggestions(String term);
    
    // Process a query string and return corrected version
    Map<String, List<String>> processQuery(String query);
    
    // Get a "Did you mean" suggestion for the whole query
    String getDidYouMeanSuggestion(String query);
    
    // Expand abbreviations (e.g., "dat sci" → "data science")
    String expandKnownAbbreviations(String query);
}