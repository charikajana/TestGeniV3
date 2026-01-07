package testgeni.v3.finder.keyword;

import testgeni.v3.core.domain.ScannedElement;

import java.util.Map;

/**
 * Calculates match scores between keywords and DOM elements.
 */
public class KeywordScorer {

    /**
     * Match a single keyword against an element and return the best score.
     */
    public KeywordMatchResult score(String keyword, ScannedElement element) {
        double bestScore = 0.0;
        String bestAttribute = "none";
        String bestValue = "";
        
        String lowerKeyword = keyword.toLowerCase();
        
        // Calculate phrase bonus: longer keywords (multi-word) get higher weight
        int wordCount = lowerKeyword.split("\\s+").length;
        double phraseBonus = 1.0 + (wordCount - 1) * 0.3;
        
        // 1. Text matching
        if (element.text != null && !element.text.isEmpty()) {
            double score = calculateStringMatch(lowerKeyword, element.text.toLowerCase()) * phraseBonus;
            if (score > bestScore) {
                bestScore = score;
                bestAttribute = "text";
                bestValue = element.text;
            }
        }
        
        // 2. Aria-label matching
        if (element.ariaLabel != null && !element.ariaLabel.isEmpty()) {
            double score = calculateStringMatch(lowerKeyword, element.ariaLabel.toLowerCase()) * phraseBonus;
            if (score > bestScore) {
                bestScore = score;
                bestAttribute = "aria-label";
                bestValue = element.ariaLabel;
            }
        }
        
        // 3. Placeholder matching
        if (element.placeholder != null && !element.placeholder.isEmpty()) {
            double score = calculateStringMatch(lowerKeyword, element.placeholder.toLowerCase()) * phraseBonus;
            if (score > bestScore) {
                bestScore = score;
                bestAttribute = "placeholder";
                bestValue = element.placeholder;
            }
        }
        
        // 4. Title matching
        if (element.title != null && !element.title.isEmpty()) {
            double score = calculateStringMatch(lowerKeyword, element.title.toLowerCase()) * phraseBonus;
            if (score > bestScore) {
                bestScore = score;
                bestAttribute = "title";
                bestValue = element.title;
            }
        }
        
        // 5. Name attribute matching
        String name = element.attributes.getOrDefault("name", "");
        if (!name.isEmpty()) {
            double score = calculateStringMatch(lowerKeyword, name.toLowerCase()) * phraseBonus;  
            if (score > bestScore) {
                bestScore = score;
                bestAttribute = "name";
                bestValue = name;
            }
        }
        
        // 6. ID matching
        if (element.id != null && !element.id.startsWith("tg-")) {
            double score = calculateStringMatch(lowerKeyword, element.id.toLowerCase());
            if (isVolatileId(element.id)) score *= 0.3;
            score *= phraseBonus;
            if (score > bestScore) {
                bestScore = score;
                bestAttribute = "id";
                bestValue = element.id;
            }
        }
        
        // 7. Tag name matching
        if (element.tagName != null) {
            double score = calculateStringMatch(lowerKeyword, element.tagName.toLowerCase()) * phraseBonus;
            if (score * 0.7 > bestScore) {
                bestScore = score * 0.7;
                bestAttribute = "tagName";
                bestValue = element.tagName;
            }
        }
        
        // 8. Role matching
        if (element.role != null && !element.role.isEmpty()) {
            double score = calculateStringMatch(lowerKeyword, element.role.toLowerCase()) * phraseBonus;
            if (score * 0.8 > bestScore) {
                bestScore = score * 0.8;
                bestAttribute = "role";
                bestValue = element.role;
            }
        }
        
        // 9. Other attributes (generic fallback)
        for (Map.Entry<String, String> attr : element.attributes.entrySet()) {
            String attrValue = attr.getValue().toLowerCase();
            double score = calculateStringMatch(lowerKeyword, attrValue) * phraseBonus;
            if (score * 0.5 > bestScore && score > 0.5) {
                bestScore = score * 0.5;
                bestAttribute = attr.getKey();
                bestValue = attr.getValue();
            }
        }
        
        bestScore = Math.min(1.5, bestScore);
        return new KeywordMatchResult(keyword, bestScore, bestAttribute, bestValue);
    }
    
    private double calculateStringMatch(String keyword, String text) {
        if (text == null || text.isEmpty()) return 0.0;
        
        if (text.equals(keyword)) return 1.0;
        if (text.matches(".*\\b" + java.util.regex.Pattern.quote(keyword) + "\\b.*")) return 0.9;
        if (text.contains(keyword)) return 0.7;
        
        if (calculateSimilarity(keyword, text) > 0.7) return 0.5;
        
        return 0.0;
    }
    
    private double calculateSimilarity(String s1, String s2) {
        if (s1.length() < 3 || s2.length() < 3) return 0.0;
        int matchCount = 0;
        int minLen = Math.min(s1.length(), s2.length());
        for (int i = 0; i < minLen; i++) {
            if (s1.charAt(i) == s2.charAt(i)) matchCount++;
        }
        return (double) matchCount / Math.max(s1.length(), s2.length());
    }
    
    private boolean isVolatileId(String id) {
        if (id == null) return false;
        return id.matches(".*\\d{4,}.*") || id.startsWith("ember") || id.startsWith("j_id") ||
               id.startsWith("react-") || id.startsWith("vw-");
    }
}
