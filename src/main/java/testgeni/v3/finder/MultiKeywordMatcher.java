package testgeni.v3.finder;

import testgeni.v3.core.domain.ScannedElement;
import testgeni.v3.core.domain.StepIntent;
import testgeni.v3.finder.keyword.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Multi-Keyword Matcher for improved element selection.
 * Delgates logic to specialized keyword helpers.
 */
public class MultiKeywordMatcher {
    
    private final KeywordExtractor extractor;
    private final KeywordScorer scorer;

    public MultiKeywordMatcher() {
        this.extractor = new KeywordExtractor();
        this.scorer = new KeywordScorer();
    }

    // Static nested classes kept for backward compatibility if needed, 
    // but recommended to use the ones in testgeni.v3.finder.keyword package.
    
    @Deprecated
    public static class KeywordMatchResult extends testgeni.v3.finder.keyword.KeywordMatchResult {
        public KeywordMatchResult(String keyword, double score, String matchedAttribute, String matchedValue) {
            super(keyword, score, matchedAttribute, matchedValue);
        }
    }

    @Deprecated
    public static class ElementMatchResult extends testgeni.v3.finder.keyword.ElementMatchResult {
        public ElementMatchResult(ScannedElement element, List<testgeni.v3.finder.keyword.KeywordMatchResult> keywordMatches) {
            super(element, keywordMatches);
        }
    }

    /**
     * Extract keywords from the step intent.
     */
    public List<String> extractKeywords(StepIntent intent) {
        return extractor.extractKeywords(intent);
    }
    
    /**
     * Match all keywords against a single element.
     */
    public testgeni.v3.finder.keyword.ElementMatchResult matchElement(ScannedElement element, List<String> keywords) {
        List<testgeni.v3.finder.keyword.KeywordMatchResult> keywordMatches = keywords.stream()
            .map(keyword -> scorer.score(keyword, element))
            .collect(Collectors.toList());
        
        return new testgeni.v3.finder.keyword.ElementMatchResult(element, keywordMatches);
    }
    
    /**
     * Find the best matching element from a list of candidates.
     */
    public testgeni.v3.finder.keyword.ElementMatchResult findBestMatch(List<ScannedElement> candidates, StepIntent intent) {
        List<String> keywords = extractKeywords(intent);
        if (keywords.isEmpty()) return null;
        
        List<testgeni.v3.finder.keyword.ElementMatchResult> results = candidates.stream()
            .filter(el -> el.isVisible)
            .map(el -> matchElement(el, keywords))
            .sorted(Comparator
                .comparingInt((testgeni.v3.finder.keyword.ElementMatchResult r) -> r.matchedKeywordCount)
                .thenComparingDouble(r -> r.totalScore)
                .reversed())
            .collect(Collectors.toList());
        
        return results.isEmpty() ? null : results.get(0);
    }
    
    /**
     * Get top N matches for debugging/analysis.
     */
    public List<testgeni.v3.finder.keyword.ElementMatchResult> getTopMatches(List<ScannedElement> candidates, StepIntent intent, int topN) {
        List<String> keywords = extractKeywords(intent);
        if (keywords.isEmpty()) return Collections.emptyList();
        
        return candidates.stream()
            .filter(el -> el.isVisible)
            .map(el -> matchElement(el, keywords))
            .sorted(Comparator
                .comparingInt((testgeni.v3.finder.keyword.ElementMatchResult r) -> r.matchedKeywordCount)
                .thenComparingDouble(r -> r.totalScore)
                .reversed())
            .limit(topN)
            .collect(Collectors.toList());
    }
}
