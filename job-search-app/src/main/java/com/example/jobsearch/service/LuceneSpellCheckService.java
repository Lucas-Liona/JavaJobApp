package com.example.jobsearch.service.impl;

import com.example.jobsearch.service.SpellCheckService;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.search.spell.Dictionary;
import org.apache.lucene.search.spell.LuceneDictionary;
import org.apache.lucene.search.spell.SpellChecker;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

@Service
public class LuceneSpellCheckService implements SpellCheckService {

    private SpellChecker spellChecker;
    private Directory spellIndexDirectory;
    private Directory dictionaryDirectory;
    private static final int MAX_SUGGESTIONS = 5;
    
    // Abbreviations dictionary
    private Map<String, String> abbreviations = new HashMap<>();
    
    @PostConstruct
    public void init() throws IOException {
        // Initialize directories for indices
        Path tempPath = Paths.get(System.getProperty("java.io.tmpdir"), "jobsearch-spell-index");
        spellIndexDirectory = FSDirectory.open(tempPath);
        
        Path dictPath = Paths.get(System.getProperty("java.io.tmpdir"), "jobsearch-dictionary");
        dictionaryDirectory = FSDirectory.open(dictPath);
        
        // Initialize spell checker
        spellChecker = new SpellChecker(spellIndexDirectory);
        
        // Create job search domain dictionary
        createJobSearchDictionary();
        
        // Load dictionary into spell checker
        IndexReader reader = DirectoryReader.open(dictionaryDirectory);
        Dictionary dictionary = new LuceneDictionary(reader, "term");
        spellChecker.indexDictionary(dictionary, new IndexWriterConfig(new StandardAnalyzer()), false);
        
        // Initialize abbreviations
        initializeAbbreviations();
    }
    
    private void createJobSearchDictionary() throws IOException {
        // Create and populate the job search domain dictionary
        StandardAnalyzer analyzer = new StandardAnalyzer();
        IndexWriterConfig config = new IndexWriterConfig(analyzer);
        IndexWriter writer = new IndexWriter(dictionaryDirectory, config);
        
        // Add common job search terms
        List<String> jobTerms = Arrays.asList(
            // Job titles
            "software", "developer", "engineer", "programmer", "analyst", 
            "manager", "director", "administrator", "specialist", "consultant",
            "designer", "architect", "technician", "scientist", "researcher",
            
            // Technical terms
            "java", "javascript", "python", "ruby", "c++", "c#", "php", "html", 
            "css", "sql", "nosql", "react", "angular", "vue", "node", "express",
            "spring", "hibernate", "maven", "gradle", "docker", "kubernetes",
            "aws", "azure", "gcp", "cloud", "devops", "agile", "scrum", "kanban",
            
            // Data science terms
            "data", "science", "machine", "learning", "artificial", "intelligence",
            "deep", "neural", "network", "analytics", "statistics", "visualization",
            "tensorflow", "pytorch", "pandas", "numpy", "scikit", "regression",
            "classification", "clustering", "reinforcement", "natural", "language",
            "processing", "nlp", "computer", "vision", "algorithm",
            
            // Job types
            "full-time", "part-time", "contract", "freelance", "permanent",
            "temporary", "remote", "hybrid", "onsite",
            
            // Other job-related terms
            "salary", "benefits", "skills", "experience", "requirements", "degree",
            "bachelor", "master", "phd", "entry", "junior", "senior", "lead"
        );
        
        // Add all terms to the index
        for (String term : jobTerms) {
            Document doc = new Document();
            doc.add(new TextField("term", term.toLowerCase(), Field.Store.YES));
            writer.addDocument(doc);
        }
        
        writer.close();
    }
    
    private void initializeAbbreviations() {
        // Common job search abbreviations
        abbreviations.put("dev", "developer");
        abbreviations.put("eng", "engineer");
        abbreviations.put("prog", "programmer");
        abbreviations.put("mgr", "manager");
        abbreviations.put("admin", "administrator");
        abbreviations.put("spec", "specialist");
        abbreviations.put("sr", "senior");
        abbreviations.put("jr", "junior");
        abbreviations.put("dat", "data");
        abbreviations.put("sci", "science");
        abbreviations.put("ml", "machine learning");
        abbreviations.put("ai", "artificial intelligence");
        abbreviations.put("js", "javascript");
        abbreviations.put("py", "python");
        abbreviations.put("ft", "full-time");
        abbreviations.put("pt", "part-time");
        // Add more abbreviations as needed
    }
    
    @Override
    public boolean needsCorrection(String term) {
        try {
            // Check if the term exists in the dictionary
            return !spellChecker.exist(term.toLowerCase());
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    @Override
    public List<String> getSuggestions(String term) {
        try {
            // Get spelling suggestions for the term
            String[] suggestions = spellChecker.suggestSimilar(term.toLowerCase(), MAX_SUGGESTIONS);
            return Arrays.asList(suggestions);
        } catch (IOException e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }
    
    @Override
    public Map<String, List<String>> processQuery(String query) {
        Map<String, List<String>> corrections = new HashMap<>();
        
        // Split the query into terms
        String[] terms = query.toLowerCase().split("\\s+");
        
        for (String term : terms) {
            // Skip very short terms
            if (term.length() < 2) continue;
            
            // Check if term needs correction
            if (needsCorrection(term)) {
                List<String> suggestions = getSuggestions(term);
                if (!suggestions.isEmpty()) {
                    corrections.put(term, suggestions);
                }
            }
        }
        
        return corrections;
    }
    
    @Override
    public String getDidYouMeanSuggestion(String query) {
        Map<String, List<String>> corrections = processQuery(query);
        if (corrections.isEmpty()) {
            return null;
        }
        
        // Split the query into terms
        String[] terms = query.toLowerCase().split("\\s+");
        StringBuilder suggestion = new StringBuilder();
        
        for (String term : terms) {
            if (corrections.containsKey(term) && !corrections.get(term).isEmpty()) {
                // Use the top suggestion for this term
                suggestion.append(corrections.get(term).get(0)).append(" ");
            } else {
                suggestion.append(term).append(" ");
            }
        }
        
        return suggestion.toString().trim();
    }
    
    @Override
    public String expandKnownAbbreviations(String query) {
        // Split the query into terms
        String[] terms = query.toLowerCase().split("\\s+");
        StringBuilder expanded = new StringBuilder();
        
        for (String term : terms) {
            if (abbreviations.containsKey(term)) {
                expanded.append(abbreviations.get(term)).append(" ");
            } else {
                expanded.append(term).append(" ");
            }
        }
        
        return expanded.toString().trim();
    }
}