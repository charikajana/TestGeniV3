package testgeni.v3.finder;

import testgeni.v3.core.domain.ScannedElement;
import testgeni.v3.core.domain.StepIntent;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Bidirectional matcher that performs both forward and reverse matching
 * between user target and DOM elements using progressive strategies.
 * 
 * FORWARD MATCHING: User Target → DOM Elements
 *   - Try progressive phrases (bigrams → full phrase)
 *   - Find DOM elements that match each strategy
 *   - Score how well DOM matches user expectation
 * 
 * REVERSE MATCHING: DOM Element → User Target
 *   - Check how well DOM element text matches back to user target
 *   - Calculate confidence: What percentage of user's expectation is met?
 * 
 * Example:
 *   User: "click on New Browser Tab button"
 *   DOM Element: text="New Browser Tab" (link)
 *   
 *   Forward: "New Browser Tab" matches perfectly (1.0)
 *   Reverse: DOM has 3/4 words (0.75 confidence - missing "button")
 *   
 *   Decision: Accept with 75% confidence
 * 
 * @author TestGeni v3
 * @version 3.0.0
 * @since 2026-01-07
 */
public class BidirectionalMatcher {
    
    private final ProgressivePhraseBuilder phraseBuilder;
    
    /**
     * Common element type keywords that should be filtered from reverse validation.
     * These are metadata descriptors, not actual content to match.
     */
    private static final Set<String> ELEMENT_TYPE_KEYWORDS = Set.of(
        "button", "btn", "radio", "checkbox", "check", "link", "field", "input",
        "box", "dropdown", "select", "menu", "icon", "image", "img", "label",
        "textbox", "textarea", "tab", "window", "dialog", "modal", "popup",
        "form", "table", "row", "column", "cell", "list", "item", "option",
        "slider", "toggle", "switch", "panel", "section", "div", "span"
    );
    
    /**
     * Result of a forward match attempt
     */
    public static class ForwardMatchResult {
        public final ProgressivePhraseBuilder.ProgressiveStrategy strategy;
        public final ScannedElement element;
        public final double score;
        public final String matchedAttribute;
        
        public ForwardMatchResult(
            ProgressivePhraseBuilder.ProgressiveStrategy strategy,
            ScannedElement element,
            double score,
            String matchedAttribute) {
            
            this.strategy = strategy;
            this.element = element;
            this.score = score;
            this.matchedAttribute = matchedAttribute;
        }
    }
    
    /**
     * Result of a reverse match validation
     */
    public static class ReverseMatchResult {
        public final String domText;
        public final String userTarget;
        public final double confidence;          // 0.0 to 1.0
        public final int matchedWords;
        public final int totalExpectedWords;
        public final List<String> missingKeywords;
        
        public ReverseMatchResult(
            String domText,
            String userTarget,
            double confidence,
            int matchedWords,
            int totalExpectedWords,
            List<String> missingKeywords) {
            
            this.domText = domText;
            this.userTarget = userTarget;
            this.confidence = confidence;
            this.matchedWords = matchedWords;
            this.totalExpectedWords = totalExpectedWords;
            this.missingKeywords = missingKeywords;
        }
        
        @Override
        public String toString() {
            return String.format("Reverse Match: %.0f%% confidence (%d/%d words) Missing: %s",
                confidence * 100, matchedWords, totalExpectedWords,
                missingKeywords.isEmpty() ? "none" : String.join(", ", missingKeywords));
        }
    }
    
    /**
     * Final bidirectional match result
     */
    public static class BidirectionalMatchResult {
        public final ForwardMatchResult forwardMatch;
        public final ReverseMatchResult reverseMatch;
        public final double finalConfidence;
        public final boolean isAcceptable;
        public final double appliedThreshold;
        
        public BidirectionalMatchResult(
            ForwardMatchResult forward,
            ReverseMatchResult reverse,
            double minConfidenceThreshold) {
            
            this.forwardMatch = forward;
            this.reverseMatch = reverse;
            
            // Final confidence is weighted average of forward and reverse
            // Forward: 60%, Reverse: 40% (forward is more important)
            this.finalConfidence = (forward.score * 0.6) + (reverse.confidence * 0.4);
            
            // Dynamic threshold based on target complexity
            // For short targets (1-2 words after filtering), use low threshold
            // For longer targets (3+ words), use standard threshold
            double dynamicThreshold = reverse.totalExpectedWords <= 2 ? 0.40 : minConfidenceThreshold;
            
            // Leniency rules:
            // 1. If we have a PERFECT match on the meaningful words (confidence 1.0) -> ACCEPT
            // 2. If forward match is very strong (>= 0.9) -> Use lower threshold
            if (reverse.confidence >= 1.0) {
                dynamicThreshold = 0.30; // Very high contextual match
            } else if (forward.score >= 0.9) {
                dynamicThreshold = 0.50; // Strong forward match
            }
            
            this.appliedThreshold = dynamicThreshold;
            
            // Accept if reverse confidence meets dynamic threshold
            this.isAcceptable = reverse.confidence >= dynamicThreshold;
        }
        
        @Override
        public String toString() {
            return String.format(
                "Bidirectional Match: Forward=%.2f, Reverse=%.2f, Final=%.2f, Acceptable=%s (threshold=%.2f)\n" +
                "  Strategy: %s\n" +
                "  Element: %s\n" +
                "  %s",
                forwardMatch.score,
                reverseMatch.confidence,
                finalConfidence,
                isAcceptable,
                appliedThreshold,
                forwardMatch.strategy,
                forwardMatch.element.text,
                reverseMatch
            );
        }
    }
    
    public BidirectionalMatcher() {
        this.phraseBuilder = new ProgressivePhraseBuilder();
    }
    
    /**
     * Find the best element using bidirectional progressive matching.
     * 
     * @param elements List of scanned DOM elements
     * @param intent Step intent with target
     * @param minConfidence Minimum reverse confidence threshold (e.g., 0.7 for 70%)
     * @return Best bidirectional match, or null if none acceptable
     */
    public BidirectionalMatchResult findBestMatch(
        List<ScannedElement> elements,
        StepIntent intent,
        double minConfidence) {
        
        if (elements == null || elements.isEmpty() || intent.target == null) {
            return null;
        }
        
        // 1. Build progressive strategies from user target
        List<ProgressivePhraseBuilder.ProgressiveStrategy> strategies = 
            phraseBuilder.buildStrategies(intent.target);
        
        if (strategies.isEmpty()) {
            return null;
        }
        
        // 2. Try each strategy and collect forward matches
        List<ForwardMatchResult> allForwardMatches = new ArrayList<>();
        
        for (ProgressivePhraseBuilder.ProgressiveStrategy strategy : strategies) {
            List<ForwardMatchResult> matches = findForwardMatches(elements, strategy, intent);
            allForwardMatches.addAll(matches);
        }
        
        if (allForwardMatches.isEmpty()) {
            return null;
        }
        
        // 3. Score all forward matches with reverse validation and collect results
        List<BidirectionalMatchResult> candidates = new ArrayList<>();
        for (ForwardMatchResult forward : allForwardMatches) {
            ReverseMatchResult reverse = performReverseMatch(
                forward.element,
                intent.target
            );
            
            candidates.add(new BidirectionalMatchResult(forward, reverse, minConfidence));
        }
        
        // 4. Sort candidates by final confidence (highest first)
        candidates.sort(Comparator.comparingDouble((BidirectionalMatchResult r) -> r.finalConfidence).reversed());
        
        // 5. Return the best candidate if it meets the acceptability criteria
        // If not, we still return the best one as a "best effort"
        for (BidirectionalMatchResult result : candidates) {
            if (result.isAcceptable) {
                return result;
            }
        }
        
        return candidates.isEmpty() ? null : candidates.get(0);
    }
    
    /**
     * Find forward matches for a specific strategy.
     */
    private List<ForwardMatchResult> findForwardMatches(
        List<ScannedElement> elements,
        ProgressivePhraseBuilder.ProgressiveStrategy strategy,
        StepIntent intent) {
        
        List<ForwardMatchResult> matches = new ArrayList<>();
        
        for (ScannedElement element : elements) {
            if (!element.isVisible) {
                continue;  // Skip hidden elements
            }
            
            // Check each attribute for match
            MatchScore matchScore = calculateMatchScore(element, strategy.searchPhrase, intent);
            
            if (matchScore.score > 0.0) {
                matches.add(new ForwardMatchResult(
                    strategy,
                    element,
                    matchScore.score,
                    matchScore.matchedAttribute
                ));
            }
        }
        
        return matches;
    }
    
    /**
     * Calculate match score between element and search phrase.
     */
    private MatchScore calculateMatchScore(ScannedElement element, String searchPhrase, StepIntent intent) {
        double bestScore = 0.0;
        String bestAttribute = "none";
        
        String search = searchPhrase.toLowerCase();
        
        // Priority 1: Text (visible content)
        if (element.text != null && !element.text.isEmpty()) {
            double score = scoreStringMatch(search, element.text.toLowerCase());
            if (score > bestScore) {
                bestScore = score;
                bestAttribute = "text";
            }
        }
        
        // Priority 2: Aria-label
        if (element.ariaLabel != null && !element.ariaLabel.isEmpty()) {
            double score = scoreStringMatch(search, element.ariaLabel.toLowerCase());
            if (score > bestScore) {
                bestScore = score;
                bestAttribute = "aria-label";
            }
        }
        
        // Priority 3: Placeholder
        if (element.placeholder != null && !element.placeholder.isEmpty()) {
            double score = scoreStringMatch(search, element.placeholder.toLowerCase());
            if (score > bestScore) {
                bestScore = score;
                bestAttribute = "placeholder";
            }
        }
        
        // Priority 4: Title
        if (element.title != null && !element.title.isEmpty()) {
            double score = scoreStringMatch(search, element.title.toLowerCase());
            if (score > bestScore) {
                bestScore = score;
                bestAttribute = "title";
            }
        }
        
        // Priority 5: Name attribute
        String name = element.attributes.getOrDefault("name", "");
        if (!name.isEmpty()) {
            double score = scoreStringMatch(search, name.toLowerCase());
            if (score > bestScore) {
                bestScore = score;
                bestAttribute = "name";
            }
        }
        
        // ELEMENT TYPE BONUS (Self-Healing / Tie-breaking)
        // If the user specified a type (e.g., "button") and this element matches it, give a bonus.
        if (bestScore > 0 && intent.elementType != null && intent.elementType != testgeni.v3.core.domain.ElementType.ANY) {
            if (element.detectedType == intent.elementType) {
                bestScore += 0.05; // Small bonus to win ties
            } else if (intent.action != null && element.detectedType.isCompatibleWith(intent.action)) {
                bestScore += 0.02; // Smaller compatibility bonus
            } else if (!element.detectedType.isCompatibleWith(intent.action)) {
                bestScore -= 0.1;  // Penalty for incompatible types (e.g. clicking a div when a button exists)
            }
        }
        
        return new MatchScore(bestScore, bestAttribute);
    }
    
    /**
     * Score how well a search phrase matches a target string.
     */
    private double scoreStringMatch(String search, String target) {
        if (target.equals(search)) {
            return 1.0;  // Perfect match
        }
        
        if (target.matches(".*\\b" + java.util.regex.Pattern.quote(search) + "\\b.*")) {
            return 0.9;  // Word boundary match
        }
        
        if (target.contains(search)) {
            return 0.7;  // Contains match
        }
        
        // Check word overlap
        double overlap = phraseBuilder.calculateWordOverlap(search, target);
        if (overlap > 0.5) {
            return 0.5 + (overlap * 0.2);  // 0.5 to 0.7 based on overlap
        }
        
        return 0.0;  // No match
    }
    
    /**
     * Perform reverse matching: Check how well DOM element matches user expectations.
     * Filters out element type keywords (button, radio, etc.) to focus on content.
     */
    private ReverseMatchResult performReverseMatch(ScannedElement element, String userTarget) {
        // Get the best text representation of the element
        String domText = getBestElementText(element);
        
        if (domText == null || domText.isEmpty()) {
            return new ReverseMatchResult(
                "",
                userTarget,
                0.0,
                0,
                phraseBuilder.buildStrategies(userTarget).size(),
                Collections.emptyList()
            );
        }
        
        // Clean and split both
        List<String> domWords = Arrays.asList(domText.toLowerCase().split("\\s+"));
        
        // Filter user words to remove element type keywords
        List<String> userWords = Arrays.asList(userTarget.toLowerCase().split("\\s+"))
            .stream()
            .filter(word -> !ELEMENT_TYPE_KEYWORDS.contains(word))  // Remove type keywords
            .collect(Collectors.toList());
        
        // If all user words were type keywords, use original list
        if (userWords.isEmpty()) {
            userWords = Arrays.asList(userTarget.toLowerCase().split("\\s+"));
        }
        
        // Find which user words are present in DOM
        int matchedCount = 0;
        List<String> missingWords = new ArrayList<>();
        
        // Technical keywords that were filtered (from userTarget)
        List<String> typeKeywords = Arrays.asList(userTarget.toLowerCase().split("\\s+"))
            .stream()
            .filter(word -> ELEMENT_TYPE_KEYWORDS.contains(word))
            .collect(Collectors.toList());

        for (String userWord : userWords) {
            boolean found = false;
            for (String domWord : domWords) {
                if (domWord.contains(userWord) || userWord.contains(domWord)) {
                    found = true;
                    break;
                }
            }
            
            if (found) {
                matchedCount++;
            } else {
                missingWords.add(userWord);
            }
        }
        
        // EFFECTIVE METADATA MATCHING:
        // If technical words like "radio" or "button" were provided, check if the DOM element matches them
        // This resolves ambiguity (e.g., Label "Yes" vs Span "Yes")
        double metadataBoost = 0.0;
        int metadataMatches = 0;
        
        for (String typeWord : typeKeywords) {
            boolean matchesMetadata = false;
            
            // Check Tag Name
            if (element.tagName != null && element.tagName.toLowerCase().contains(typeWord)) matchesMetadata = true;
            // Check Role
            if (element.role != null && element.role.toLowerCase().contains(typeWord)) matchesMetadata = true;
            // Check Class Attributes
            String className = element.attributes.getOrDefault("class", "");
            if (className.toLowerCase().contains(typeWord)) matchesMetadata = true;
            // Check Type Attribute (e.g., type="radio")
            String typeAttr = element.attributes.getOrDefault("type", "");
            if (typeAttr.toLowerCase().contains(typeWord)) matchesMetadata = true;

            if (matchesMetadata) {
                metadataMatches++;
            }
        }
        
        // Confidence calculation
        double confidence = userWords.isEmpty() ? 0.0 : (double) matchedCount / userWords.size();
        
        // Apply Metadata Boost: If we have metadata matches, it scales the confidence
        // This ensures a "Yes" label (radio) wins over a "Yes" span when "radio" is in the request.
        if (!typeKeywords.isEmpty() && metadataMatches > 0) {
            double boostFactor = (double) metadataMatches / typeKeywords.size();
            confidence = confidence + (boostFactor * 0.2); 
        }
        
        return new ReverseMatchResult(
            domText,
            userTarget,
            confidence,
            matchedCount,
            userWords.size(),
            missingWords
        );
    }
    
    /**
     * Get the best text representation of an element.
     */
    private String getBestElementText(ScannedElement element) {
        if (element.text != null && !element.text.isEmpty()) {
            return element.text;
        }
        if (element.ariaLabel != null && !element.ariaLabel.isEmpty()) {
            return element.ariaLabel;
        }
        if (element.placeholder != null && !element.placeholder.isEmpty()) {
            return element.placeholder;
        }
        if (element.title != null && !element.title.isEmpty()) {
            return element.title;
        }
        return element.attributes.getOrDefault("name", "");
    }
    
    /**
     * Helper class for match scoring
     */
    private static class MatchScore {
        final double score;
        final String matchedAttribute;
        
        MatchScore(double score, String matchedAttribute) {
            this.score = score;
            this.matchedAttribute = matchedAttribute;
        }
    }
}
