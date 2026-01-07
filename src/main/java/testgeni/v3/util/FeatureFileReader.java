package testgeni.v3.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Simple reader for Gherkin feature files.
 * Extracts executable steps (Given/When/Then/And/But).
 */
public class FeatureFileReader {

    public List<String> readSteps(String featurePath) throws IOException {
        List<String> steps = new ArrayList<>();
        Path path = Paths.get(featurePath);
        
        if (!Files.exists(path)) {
            throw new IOException("Feature file not found: " + featurePath);
        }
        
        List<String> lines = Files.readAllLines(path);
        
        for (String line : lines) {
            String trimmed = line.trim();
            
            // Skip empty lines, comments, and non-step lines
            if (trimmed.isEmpty() || trimmed.startsWith("#") || 
                trimmed.startsWith("Feature:") || trimmed.startsWith("Scenario:") ||
                trimmed.startsWith("Background:") || trimmed.startsWith("@")) {
                continue;
            }
            
            // Extract steps (Given/When/Then/And/But)
            if (trimmed.startsWith("Given ") || trimmed.startsWith("When ") || 
                trimmed.startsWith("Then ") || trimmed.startsWith("And ") || 
                trimmed.startsWith("But ")) {
                steps.add(trimmed);
            }
        }
        
        return steps;
    }
}
