package testgeni.v3.finder;

import testgeni.v3.parser.NoiseWords;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Builds progressive phrase strategies from a target string.
 * 
 * Strategy: Start from bigrams (2-word phrases) to avoid noise from single words,
 * then progressively build up to the full target phrase.
 * 
 * Example:
 * Input: "New Browser Tab button"
 * Output:
 *   Strategy 1: "New Browser"
 *   Strategy 2: "New Browser Tab"
 *   Strategy 3: "New Browser Tab button"
 * 
 * @author TestGeni v3
 * @version 3.0.0
 * @since 2026-01-07
 */
public class ProgressivePhraseBuilder {
    
    /**
     * Represents a progressive search strategy
     */
    public static class ProgressiveStrategy {
        public final int strategyLevel;      // 2, 3, 4, etc. (number of words)
        public final String searchPhrase;    // The phrase to search for
        public final List<String> keywords;  // Individual keywords in the phrase
        
        public ProgressiveStrategy(int level, String phrase, List<String> keywords) {
            this.strategyLevel = level;
            this.searchPhrase = phrase;
            this.keywords = keywords;
        }
        
        @Override
        public String toString() {
            return String.format("Strategy %d: \"%s\" [%s]", 
                strategyLevel, searchPhrase, String.join(", ", keywords));
        }
    }
    
    /**
     * Build progressive strategies from target string.
     * Starts from bigrams (2-word phrases) to avoid single-word noise.
     * EXCEPTION: For 2-word targets, includes both unigrams to improve matching.
     * 
     * @param target Target string from step intent
     * @return List of progressive strategies, ordered from shortest to longest
     */
    public List<ProgressiveStrategy> buildStrategies(String target) {
        if (target == null || target.trim().isEmpty()) {
            return Collections.emptyList();
        }
        
        // 1. Clean and split target into words
        List<String> words = cleanAndSplit(target);
        
        if (words.isEmpty()) {
            return Collections.emptyList();
        }
        
        // 2. Build progressive strategies
        List<ProgressiveStrategy> strategies = new ArrayList<>();
        
        // Step 1: Add all unigrams (individual words) as basic strategies
        // This ensures "Yes" is found even if user says "Yes radio button"
        for (String word : words) {
            strategies.add(new ProgressiveStrategy(1, word, List.of(word)));
        }
        
        // Step 2: Add multi-word phrases starting from bigrams
        if (words.size() >= 2) {
            for (int wordCount = 2; wordCount <= words.size(); wordCount++) {
                // Take first N words
                List<String> subList = words.subList(0, wordCount);
                String phrase = String.join(" ", subList);
                
                strategies.add(new ProgressiveStrategy(wordCount, phrase, new ArrayList<>(subList)));
            }
        }
        
        return strategies;
    }
    
    /**
     * Build reverse progressive strategies from DOM element text.
     * This checks how well the DOM element matches back to the user's target.
     * 
     * @param domText Text from DOM element
     * @param userTarget Original target from user step
     * @return List of progressive strategies for reverse matching
     */
    public List<ProgressiveStrategy> buildReverseStrategies(String domText, String userTarget) {
        if (domText == null || domText.trim().isEmpty()) {
            return Collections.emptyList();
        }
        
        // Clean both texts
        List<String> domWords = cleanAndSplit(domText);
        List<String> userWords = cleanAndSplit(userTarget);
        
        if (domWords.isEmpty() || userWords.isEmpty()) {
            return Collections.emptyList();
        }
        
        // Build strategies based on DOM element's word count
        List<ProgressiveStrategy> strategies = new ArrayList<>();
        
        int startLevel = Math.min(2, domWords.size());
        for (int wordCount = startLevel; wordCount <= domWords.size(); wordCount++) {
            List<String> subList = domWords.subList(0, wordCount);
            String phrase = String.join(" ", subList);
            
            strategies.add(new ProgressiveStrategy(wordCount, phrase, new ArrayList<>(subList)));
        }
        
        return strategies;
    }
    
    /**
     * Clean target string and split into meaningful words.
     * Removes noise words, normalizes text, and filters short words.
     * 
     * @param text Text to clean and split
     * @return List of cleaned keywords
     */
    private List<String> cleanAndSplit(String text) {
        if (text == null || text.trim().isEmpty()) {
            return Collections.emptyList();
        }
        
        // 1. Normalize: lowercase, remove special chars, trim
        String normalized = text.toLowerCase()
            .replaceAll("[^a-z0-9\\s]", " ")
            .trim()
            .replaceAll("\\s+", " ");
        
        // 2. Split into words
        String[] words = normalized.split("\\s+");
        
        // 3. Filter: Remove stop words and very short words
        return Arrays.stream(words)
            .filter(word -> word.length() >= 2)  // At least 2 characters
            .filter(word -> !NoiseWords.isNoiseWord(word))  // Not a noise word
            .collect(Collectors.toList());
    }
    
    /**
     * Calculate word overlap between two texts.
     * Returns the percentage of words that overlap.
     * 
     * @param text1 First text
     * @param text2 Second text
     * @return Overlap ratio (0.0 to 1.0)
     */
    public double calculateWordOverlap(String text1, String text2) {
        List<String> words1 = cleanAndSplit(text1);
        List<String> words2 = cleanAndSplit(text2);
        
        if (words1.isEmpty() || words2.isEmpty()) {
            return 0.0;
        }
        
        // Count matching words
        Set<String> set1 = new HashSet<>(words1);
        Set<String> set2 = new HashSet<>(words2);
        
        Set<String> intersection = new HashSet<>(set1);
        intersection.retainAll(set2);
        
        Set<String> union = new HashSet<>(set1);
        union.addAll(set2);
        
        // Jaccard similarity: |intersection| / |union|
        return (double) intersection.size() / union.size();
    }
    
    /**
     * Get the optimal strategy level based on word count.
     * 
     * @param wordCount Number of words in target
     * @return Recommended starting strategy level
     */
    public int getOptimalStartLevel(int wordCount) {
        if (wordCount <= 1) return 1;  // Single word, start from 1
        if (wordCount == 2) return 2;  // Two words, start from 2
        if (wordCount >= 3) return 2;  // Three or more, start from 2 (bigrams)
        return 2;  // Default
    }
}
