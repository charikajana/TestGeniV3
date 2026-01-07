package testgeni.v3.finder;

import testgeni.v3.core.domain.ScannedElement;
import testgeni.v3.core.domain.StepIntent;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import testgeni.v3.finder.keyword.ElementMatchResult;

/**
 * Calculates a match score (0.0 to 1.0) between a StepIntent and a ScannedElement.
 * 
 * Logic includes:
 * - Multi-keyword matching (NEW: Compares multiple keywords from step against DOM elements)
 * - Text matching (Exact, fuzzy, partial)
 * - Semantic type matching (Is it actually a button?)
 * - Attribute matching (ID, Name, Title)
 * - Proximity/Context matching (Is it inside the correct container?)
 * - State matching (Is it visible/enabled?)
 */
public class ElementScorer {
    
    private final MultiKeywordMatcher keywordMatcher;

    public static class ScoreResult {
        public final double totalScore;
        public final String breakdown;
        public final ElementMatchResult result;
        public final List<String> bonuses = new ArrayList<>();
        public final List<String> penalties = new ArrayList<>();

        public ScoreResult(double totalScore, String breakdown, ElementMatchResult result) {
            this.totalScore = Math.max(0.0, Math.min(1.0, totalScore));
            this.breakdown = breakdown;
            this.result = result;
        }
    }
    
    /**
     * Constructor
     */
    public ElementScorer() {
        this.keywordMatcher = new MultiKeywordMatcher();
    }

    /**
     * Scores a candidate element against the intent.
     */
    public ScoreResult score(ScannedElement element, StepIntent intent) {
        double score = 0.0;
        StringBuilder explanation = new StringBuilder();
        
        // 1. Fundamental State Penalty (Non-negotiable)
        if (!element.isVisible) {
            explanation.append("[-1.0: Hidden] ");
            return new ScoreResult(0.0, explanation.toString(), null);
        }

        // 2. Element Type Match (Weight: 0.2)
        double typeScore = calculateTypeScore(element, intent);
        score += typeScore * 0.2;
        explanation.append(String.format("[Type: %.2f] ", typeScore));

        // 3. Text/Name Match (Weight: 0.5)
        // We capture the result for XAI reporting
        ElementMatchResult textMatchResult = getKeywordMatch(element, intent);
        double textScore = calculateFinalTextScore(textMatchResult, element, intent);
        score += textScore * 0.5;
        explanation.append(String.format("[Text: %.2f] ", textScore));

        // 4. Identity Match (ID/Role/Attributes) (Weight: 0.2)
        double identityScore = calculateIdentityScore(element, intent);
        score += identityScore * 0.2;
        explanation.append(String.format("[ID: %.2f] ", identityScore));

        // 5. Context/Proximity Match (Weight: 0.1)
        double contextScore = calculateContextScore(element, intent);
        score += contextScore * 0.1;
        explanation.append(String.format("[Context: %.2f] ", contextScore));

        return new ScoreResult(score, explanation.toString().trim(), textMatchResult);
    }

    private double calculateTypeScore(ScannedElement element, StepIntent intent) {
        if (intent.elementType == null || intent.elementType == testgeni.v3.core.domain.ElementType.ANY) return 0.8; // Neutral
        if (element.detectedType == intent.elementType) return 1.0;
        if (element.detectedType.isCompatibleWith(intent.action)) return 0.6;
        return 0.1;
    }

    private ElementMatchResult getKeywordMatch(ScannedElement element, StepIntent intent) {
        List<String> keywords = keywordMatcher.extractKeywords(intent);
        if (keywords.isEmpty()) return null;
        return keywordMatcher.matchElement(element, keywords);
    }
    
    private double calculateFinalTextScore(ElementMatchResult matchResult, ScannedElement element, StepIntent intent) {
        if (matchResult == null) {
            return calculateLegacyTextScore(element, intent);
        }
        
        int totalKeywords = matchResult.keywordMatches.size();
        int matchedKeywords = matchResult.matchedKeywordCount;
        double totalMatchScore = matchResult.totalScore;
        
        if (matchedKeywords == 0) return 0.0;
        
        double keywordCoverage = (double) matchedKeywords / totalKeywords;
        double avgMatchQuality = totalMatchScore / totalKeywords;
        
        return Math.min(1.0, (keywordCoverage * 0.4) + (avgMatchQuality * 0.6));
    }
    
    /**
     * Legacy text scoring method (fallback)
     */
    private double calculateLegacyTextScore(ScannedElement element, StepIntent intent) {
        String target = intent.target.toLowerCase();
        String elText = element.text != null ? element.text.toLowerCase() : "";
        String elAria = element.ariaLabel != null ? element.ariaLabel.toLowerCase() : "";
        String elPlaceholder = element.placeholder != null ? element.placeholder.toLowerCase() : "";
        String elTitle = element.title != null ? element.title.toLowerCase() : "";
        String elValue = element.value != null ? element.value.toLowerCase() : "";

        if (elText.equals(target) || elAria.equals(target)) return 1.0;
        
        // Complete word match is better than partial string match
        if (elText.matches(".*\\b" + Pattern.quote(target) + "\\b.*")) return 0.9;
        
        if (elText.contains(target) || elAria.contains(target)) return 0.7;
        if (elPlaceholder.contains(target) || elTitle.contains(target) || elValue.contains(target)) return 0.6;

        return 0.0;
    }

    private double calculateIdentityScore(ScannedElement element, StepIntent intent) {
        String target = intent.target.toLowerCase().replace(" ", "");
        String id = element.id != null ? element.id.toLowerCase() : "";
        
        // 1. Exact technical matches (Highest potential)
        if (id.equals(target)) {
            // Check for volatility (penalize auto-generated IDs)
            if (isVolatile(id)) return 0.4; 
            return 1.0;
        }

        // 2. Stable Test ID matches (Preferred over ID)
        String testId = element.attributes.getOrDefault("data-testid", "").toLowerCase();
        if (testId.equals(target) || testId.contains(target)) return 1.0;

        // 3. Name Attribute
        String name = element.attributes.getOrDefault("name", "").toLowerCase();
        if (name.equals(target)) return 0.9;
        
        // 4. Fuzzy ID matches
        if (!id.isEmpty() && id.contains(target)) {
            if (isVolatile(id)) return 0.2;
            return 0.6;
        }
        
        // 5. General Attribute matching
        for (String attrVal : element.attributes.values()) {
            if (attrVal.toLowerCase().contains(target)) return 0.5;
        }

        return 0.0;
    }

    /**
     * Detects if an ID is likely auto-generated (unstable).
     * Examples: "id-123456", "ember-781", "j_id_jsp_123"
     */
    private boolean isVolatile(String id) {
        if (id == null) return false;
        // Contains 4 or more digits (strong signal for ID generators)
        if (id.matches(".*\\d{4,}.*")) return true;
        // Common pattern prefixes for auto-generated IDs
        return id.startsWith("ember") || id.startsWith("j_id") || 
               id.startsWith("react-") || id.startsWith("vw-");
    }

    private double calculateContextScore(ScannedElement element, StepIntent intent) {
        if (intent.scopingContext == null) return 1.0; // No scoping required
        
        // This will be enhanced in SmartElementFinder to check actual ancestry
        return 0.5; 
    }
}
