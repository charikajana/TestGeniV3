package testgeni.v3.finder.keyword;

import testgeni.v3.core.domain.ActionType;
import testgeni.v3.core.domain.ElementType;
import testgeni.v3.core.domain.StepIntent;

import java.util.ArrayList;
import java.util.List;

/**
 * Extracts and normalizes search keywords from a StepIntent.
 */
public class KeywordExtractor {

    /**
     * Extract keywords from the step intent at MULTIPLE GRANULARITY LEVELS.
     */
    public List<String> extractKeywords(StepIntent intent) {
        List<String> keywords = new ArrayList<>();
        
        // 1. Extract from target (most important) at multiple levels
        if (intent.target != null && !intent.target.trim().isEmpty()) {
            String targetWithoutQuotes = removeQuotedStrings(intent.target);
            keywords.addAll(extractMultiLevelKeywords(targetWithoutQuotes));
        }
        
        // 2. Extract from scoping context at multiple levels
        if (intent.scopingContext != null && !intent.scopingContext.trim().isEmpty()) {
            String scopeWithoutQuotes = removeQuotedStrings(intent.scopingContext);
            keywords.addAll(extractMultiLevelKeywords(scopeWithoutQuotes));
        }
        
        // 3. Add element type hint if available
        if (intent.elementType != null && intent.elementType != ElementType.ANY) {
            keywords.add(intent.elementType.name().toLowerCase());
        }
        
        // 4. For VERIFY actions, add value as keyword ONLY if it's verification-related
        if (intent.value != null && !intent.value.trim().isEmpty() && 
            intent.action != null && intent.action.name().contains("VERIFY")) {
            if (KeywordConstants.isVerificationKeyword(intent.value)) {
                keywords.addAll(extractMultiLevelKeywords(intent.value));
            }
        }
        
        return keywords;
    }
    
    /**
     * Extracts keywords at multiple granularity levels (n-grams).
     */
    private List<String> extractMultiLevelKeywords(String text) {
        List<String> keywords = new ArrayList<>();
        
        if (text == null || text.trim().isEmpty()) {
            return keywords;
        }
        
        // Normalize text
        String normalized = text.toLowerCase()
            .replaceAll("[^a-z0-9\\s]", " ")
            .trim()
            .replaceAll("\\s+", " ");
        
        String[] words = normalized.split("\\s+");
        
        // Filter out stop words and very short words
        List<String> filteredWords = new ArrayList<>();
        for (String word : words) {
            if (word.length() >= 2 && !KeywordConstants.isStopWord(word)) {
                filteredWords.add(word);
            }
        }
        
        if (filteredWords.isEmpty()) {
            return keywords;
        }
        
        // Priority 1: Full phrase (highest priority - exact match)
        if (filteredWords.size() > 1) {
            keywords.add(String.join(" ", filteredWords));
        }
        
        // Priority 2: Bigrams (word pairs)
        if (filteredWords.size() >= 2) {
            for (int i = 0; i < filteredWords.size() - 1; i++) {
                keywords.add(filteredWords.get(i) + " " + filteredWords.get(i + 1));
            }
        }
        
        // Priority 3: Individual words (unigrams - fallback)
        keywords.addAll(filteredWords);
        
        return keywords;
    }
    
    /**
     * Remove all quoted strings from text.
     */
    private String removeQuotedStrings(String text) {
        if (text == null) return "";
        String result = text.replaceAll("'[^']*'", " ");
        result = result.replaceAll("\"[^\"]*\"", " ");
        return result.replaceAll("\\s+", " ").trim();
    }
}
