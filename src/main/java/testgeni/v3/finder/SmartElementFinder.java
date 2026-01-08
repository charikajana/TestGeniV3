package testgeni.v3.finder;

import com.microsoft.playwright.Page;
import com.microsoft.playwright.Locator;
import testgeni.v3.core.domain.*;
import testgeni.v3.scanner.PageScanner;
import testgeni.v3.finder.selector.SelectorStrategyEngine;
import testgeni.v3.orchestration.LocatorCacheRegistry;
import testgeni.v3.util.V3Logger;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Intelligent implementation of ElementFinder.
 */
public class SmartElementFinder implements ElementFinder {

    private final PageScanner scanner;
    private final ElementScorer scorer;
    private final List<LocatorStrategy> strategies;
    private final BidirectionalMatcher bidirectionalMatcher;
    private final SelectorStrategyEngine selectorEngine;
    private final LocatorCacheRegistry cacheRegistry;
    
    // Configuration
    private static final double MIN_REVERSE_CONFIDENCE = 0.70; // 70% minimum confidence

    public SmartElementFinder(PageScanner scanner, LocatorCacheRegistry cacheRegistry) {
        this.scanner = scanner;
        this.scorer = new ElementScorer();
        this.strategies = List.of(
            new ByAttributeStrategy(),
            new ByTextStrategy()
        );
        this.bidirectionalMatcher = new BidirectionalMatcher();
        this.selectorEngine = new SelectorStrategyEngine();
        this.cacheRegistry = cacheRegistry != null ? cacheRegistry : new LocatorCacheRegistry();
    }

    @Override
    public ElementMatch find(Page page, StepIntent intent) {
        // 1. Check Cache first (Fast-Track) - Use Page URL as context
        String context = page.url();
        String cacheKey = intent.action.name() + "::" + (intent.target != null ? intent.target : "page");
        String cachedSelector = cacheRegistry.getSelector(context, cacheKey);
        if (cachedSelector != null) {
            try {
                // Try finding the element in the main frame or subframes using the cached selector
                Locator locator = page.locator(cachedSelector).first();
                if (locator != null && locator.isVisible()) {
                    V3Logger.info("FAST-TRACK: Using cached selector: " + cachedSelector);
                    return new ElementMatch.Builder(locator, 1.0, ElementMatch.MatchStrategy.CACHED)
                        .locatorString(cachedSelector)
                        .status(ElementMatch.MatchStatus.FOUND)
                        .build();
                } else {
                    V3Logger.trace("Cache Miss", "Cached selector no longer visible. Switching to Intelligence Layer...");
                }
            } catch (Exception e) {
                V3Logger.trace("Cache Error", "Error using cached selector: " + e.getMessage());
            }
        }

        // 2. Intelligence Layer: Full Scan (Multi-frame support)
        V3Logger.debug("Intelligence Layer: Performing full page scan...");
        List<ScannedElement> allElements = scanAllFrames(page);
        V3Logger.trace("Scan Result", String.format("Found %d elements across all frames", allElements.size()));
        
        ElementMatch match = find(intent, allElements, page);
        
        return match;
    }

    @Override
    public void saveMatchToCache(Page page, StepIntent intent, ElementMatch match) {
        if (match != null && match.status == ElementMatch.MatchStatus.FOUND && match.confidence >= 0.9) {
            String context = page.url();
            String cacheKey = intent.action.name() + "::" + (intent.target != null ? intent.target : "page");
            cacheRegistry.putSelector(context, cacheKey, match.locatorString);
        }
    }

    private List<ScannedElement> scanAllFrames(Page page) {
        List<ScannedElement> allElements = new ArrayList<>();
        
        // Scan main frame
        allElements.addAll(scanner.scan(page));
        
        // Scan child frames
        page.frames().stream()
            .filter(f -> f != page.mainFrame())
            .forEach(frame -> {
                try {
                    allElements.addAll(scanner.scan(frame));
                } catch (Exception e) {
                    V3Logger.debug("Skipping inaccessible frame: " + e.getMessage());
                }
            });
            
        return allElements;
    }

    @Override
    public ElementMatch find(StepIntent intent, List<ScannedElement> elements, Page page) {
        long startTime = System.currentTimeMillis();
        
        if (elements == null || elements.isEmpty()) {
            return createErrorMatch("No elements found on page during scan.", ElementMatch.MatchStatus.NOT_FOUND);
        }

        // 2. Handle Scoping Context if present (Find the container first)
        List<ScannedElement> searchScope = elements;
        ScannedElement container = null;
        if (intent.scopingContext != null) {
            container = findBestContainer(elements, intent);
            if (container != null) {
                final String containerId = container.id;
                searchScope = elements.stream()
                    .filter(el -> el.ancestorIds.contains(containerId))
                    .collect(Collectors.toList());
            }
        }

        // 3. Apply basic filtering (Action compatibility)
        List<ScannedElement> filteredScope = searchScope.stream()
            .filter(el -> el.isVisible)
            .collect(Collectors.toList());
        
        // ========================================
        // NEW: Try Bidirectional Progressive Matching First
        // ========================================
        
        // Debug: Log sample of elements we're searching through
        if (filteredScope.size() > 0) {
            testgeni.v3.util.V3Logger.debug(String.format(
                "Searching %d visible elements for target: '%s'", 
                filteredScope.size(), 
                intent.target
            ));
            
            // Show first 10 element texts for debugging
            java.util.List<String> sampleTexts = filteredScope.stream()
                .limit(10)
                .map(el -> String.format("[%s] text='%s'", el.tagName, el.text != null ? el.text : "null"))
                .collect(java.util.stream.Collectors.toList());
            testgeni.v3.util.V3Logger.trace("Sample elements found", String.join(", ", sampleTexts));
        }
        
        BidirectionalMatcher.BidirectionalMatchResult bidirectionalResult = 
            bidirectionalMatcher.findBestMatch(filteredScope, intent, MIN_REVERSE_CONFIDENCE);
        
        if (bidirectionalResult != null && bidirectionalResult.isAcceptable) {
            // Found acceptable match with bidirectional strategy
            long findDuration = System.currentTimeMillis() - startTime;
            
            testgeni.v3.util.V3Logger.debug(String.format(
                "Bidirectional Match Found: Strategy=%s, Forward=%.2f, Reverse=%.2f, Final=%.2f",
                bidirectionalResult.forwardMatch.strategy,
                bidirectionalResult.forwardMatch.score,
                bidirectionalResult.reverseMatch.confidence,
                bidirectionalResult.finalConfidence
            ));
            
            if (!bidirectionalResult.reverseMatch.missingKeywords.isEmpty()) {
                testgeni.v3.util.V3Logger.warn(String.format(
                    "Element found but missing keywords: %s", 
                    String.join(", ", bidirectionalResult.reverseMatch.missingKeywords)
                ));
            }
            
            ElementMatch matchResult = buildBidirectionalMatch(page, bidirectionalResult, intent, findDuration, elements);
            
            // apply proximity matching if needed
            if (intent.action.isTextInputAction() || intent.action == ActionType.SELECT) {
                matchResult = applyProximityMatching(page, matchResult, intent, elements);
            }
            return matchResult;
        } else if (bidirectionalResult != null) {
            // Found a match but confidence too low
            testgeni.v3.util.V3Logger.debug(String.format(
                "Bidirectional match rejected: Forward=%.2f, Reverse=%.2f (min=%.2f), Missing=%s",
                bidirectionalResult.forwardMatch.score,
                bidirectionalResult.reverseMatch.confidence,
                MIN_REVERSE_CONFIDENCE,
                String.join(", ", bidirectionalResult.reverseMatch.missingKeywords)
            ));
        } else {
            testgeni.v3.util.V3Logger.debug("Bidirectional matching returned null - no matches found");
        }
        
        // ========================================
        // FALLBACK: Original Multi-Keyword Strategy
        // ========================================
        testgeni.v3.util.V3Logger.debug("Bidirectional matching failed, falling back to original strategy");
        
        // 4. Gather Candidates from all strategies
        Set<ScannedElement> candidates = new LinkedHashSet<>();
        for (LocatorStrategy strategy : strategies) {
            candidates.addAll(strategy.findCandidates(filteredScope, intent));
        }
        
        if (candidates.isEmpty()) {
            return createErrorMatch("Could not find any candidates for target: " + intent.target, ElementMatch.MatchStatus.NOT_FOUND);
        }

        // 6. Sort and get best evaluation
        List<MatchEvaluation> evaluations = candidates.stream()
            .map(el -> new MatchEvaluation(el, scorer.score(el, intent)))
            .sorted(Comparator.comparingDouble((MatchEvaluation e) -> e.score.totalScore).reversed())
            .collect(Collectors.toList());

        MatchEvaluation bestEval = evaluations.get(0);

        // 7. Handle Ordinals (1st, 2nd, last)
        if (intent.elementIndex != null) {
            int index = intent.elementIndex;
            if (index > 0 && index <= evaluations.size()) {
                bestEval = evaluations.get(index - 1);
            } else if (index == -1 && !evaluations.isEmpty()) {
                bestEval = evaluations.get(evaluations.size() - 1);
            }
        }

        // 8. Build the final Playwright Locator and Match response
        long findDuration = System.currentTimeMillis() - startTime;
        ElementMatch match = buildMatch(page, bestEval, intent, findDuration, evaluations.size(), elements);
        
        // ========================================
        // NEW: Compatibility Filtering & Proximity Matching
        // ========================================
        if (intent.action.isTextInputAction() || intent.action == ActionType.SELECT) {
            match = applyProximityMatching(page, match, intent, elements);
        }
        
        // 9. Generate and Log XAI Report
        List<testgeni.v3.finder.keyword.ElementMatchResult> results = evaluations.stream()
            .map(e -> e.score.result)
            .collect(Collectors.toList());
            
        String report = testgeni.v3.util.VisualEvidenceGenerator.generateComparisonReport(match, results);
        testgeni.v3.util.V3Logger.info(report);
        
        return match;
    }

    /**
     * If the matched element is not compatible with the action (e.g. a DIV for FILL),
     * this method searches for the nearest compatible descendant or sibling.
     */
    private ElementMatch applyProximityMatching(Page page, ElementMatch match, StepIntent intent, List<ScannedElement> allElements) {
        if (match.status != ElementMatch.MatchStatus.FOUND) return match;
        
        // Find the scanned element corresponding to this match
        ScannedElement matchedEl = allElements.stream()
            .filter(el -> el.id.equals(match.id))
            .findFirst()
            .orElse(null);
            
        if (matchedEl == null) return match;
        
        // If already compatible, no need to look further
        if (intent.action.isCompatibleWith(matchedEl.detectedType)) {
            return match;
        }
        
        V3Logger.debug("Proximity Search: Matched element (" + matchedEl.tagName + ") is not compatible with " + intent.action + ". Searching for compatible proximity element...");
        
        // 1. Search for compatible DESCENDANTS
        ScannedElement bestDescendant = allElements.stream()
            .filter(el -> el.isVisible && intent.action.isCompatibleWith(el.detectedType))
            .filter(el -> el.ancestorIds.contains(matchedEl.id))
            .findFirst()
            .orElse(null);
            
        if (bestDescendant != null) {
            V3Logger.info("Proximity MATCH: Found compatible descendant: " + bestDescendant.tagName + " (ID: " + bestDescendant.id + ")");
            return buildBestMatchResult(page, bestDescendant, match, "Proximity: Compatible Descendant", allElements);
        }
        
        // 2. Search for compatible SIBLINGS / NEARBY Elements
        int matchedIndex = allElements.indexOf(matchedEl);
        if (matchedIndex != -1) {
            // Check previous 3 and next 5 candidates
            int start = Math.max(0, matchedIndex - 3);
            int end = Math.min(allElements.size(), matchedIndex + 6);
            
            for (int i = start; i < end; i++) {
                if (i == matchedIndex) continue;
                ScannedElement candidate = allElements.get(i);
                if (candidate.isVisible && intent.action.isCompatibleWith(candidate.detectedType)) {
                    V3Logger.info("Proximity MATCH: Found compatible nearby relationship: " + candidate.tagName + " (ID: " + candidate.id + ")");
                    return buildBestMatchResult(page, candidate, match, "Proximity: Nearby Element", allElements);
                }
            }
        }
        
        // 3. Search via Parent (Check if parent has other compatible children)
        if (!matchedEl.ancestorIds.isEmpty()) {
            String parentId = matchedEl.ancestorIds.get(0);
            ScannedElement cousin = allElements.stream()
                .filter(el -> el.isVisible && intent.action.isCompatibleWith(el.detectedType))
                .filter(el -> el.ancestorIds.contains(parentId) && !el.id.equals(matchedEl.id))
                .findFirst()
                .orElse(null);
                
            if (cousin != null) {
                V3Logger.info("Proximity MATCH: Found compatible cousin via parent: " + cousin.tagName + " (ID: " + cousin.id + ")");
                return buildBestMatchResult(page, cousin, match, "Proximity: Sibling of Target Container", allElements);
            }
        }
        
        V3Logger.warn("Proximity Search: No compatible input/select found near " + matchedEl.tagName);
        return match;
    }

    private ElementMatch buildBestMatchResult(Page page, ScannedElement el, ElementMatch originalMatch, String reason, List<ScannedElement> allElements) {
        String selector = generatePlaywrightSelector(el, allElements);
        Locator locator = page.locator(selector).first();
        
        return new ElementMatch.Builder(locator, originalMatch.confidence, ElementMatch.MatchStrategy.BY_SEMANTIC)
            .status(ElementMatch.MatchStatus.FOUND)
            .tagName(el.tagName)
            .id(el.id)
            .actualText(el.text)
            .ariaLabel(el.ariaLabel)
            .role(el.role)
            .visible(el.isVisible)
            .enabled(el.isEnabled)
            .scoreBreakdown(originalMatch.scoreBreakdown + " + " + reason)
            .findDurationMs(originalMatch.findDurationMs)
            .candidatesScanned(originalMatch.candidatesScanned)
            .locatorString(selector)
            .selectionReason(reason)
            .build();
    }

    private ScannedElement findBestContainer(List<ScannedElement> elements, StepIntent intent) {
        StepIntent containerIntent = new StepIntent.Builder(ActionType.VERIFY, intent.scopingContext)
            .elementIndex(intent.contextIndex)
            .build();
        
        Set<ScannedElement> containerCandidates = new LinkedHashSet<>();
        for (LocatorStrategy strategy : strategies) {
            containerCandidates.addAll(strategy.findCandidates(elements, containerIntent));
        }

        if (containerCandidates.isEmpty()) return null;

        List<MatchEvaluation> evaluations = containerCandidates.stream()
            .map(el -> new MatchEvaluation(el, scorer.score(el, containerIntent)))
            .sorted(Comparator.comparingDouble((MatchEvaluation e) -> e.score.totalScore).reversed())
            .collect(Collectors.toList());

        return evaluations.get(0).element;
    }
    
    private ElementMatch buildBidirectionalMatch(Page page, BidirectionalMatcher.BidirectionalMatchResult result, StepIntent intent, long durationMs, List<ScannedElement> allElements) {
        ScannedElement el = result.forwardMatch.element;
        String selector = generatePlaywrightSelector(el, allElements);
        Locator locator = page.locator(selector).first();
        
        // Build score breakdown
        String scoreBreakdown = String.format(
            "Bidirectional: Forward=%.2f (%s), Reverse=%.0f%% (%d/%d words), Final=%.2f",
            result.forwardMatch.score,
            result.forwardMatch.matchedAttribute,
            result.reverseMatch.confidence * 100,
            result.reverseMatch.matchedWords,
            result.reverseMatch.totalExpectedWords,
            result.finalConfidence
        );

        return new ElementMatch.Builder(locator, result.finalConfidence, ElementMatch.MatchStrategy.BY_SEMANTIC)
            .status(ElementMatch.MatchStatus.FOUND)
            .tagName(el.tagName)
            .id(el.id)
            .actualText(el.text)
            .ariaLabel(el.ariaLabel)
            .role(el.role)
            .visible(el.isVisible)
            .enabled(el.isEnabled)
            .scoreBreakdown(scoreBreakdown)
            .findDurationMs(durationMs)
            .candidatesScanned(1)  // Bidirectional returns single best match
            .locatorString(selector)
            .selectionReason("Bidirectional Progressive Match: " + result.forwardMatch.strategy)
            .build();
    }

    private ElementMatch buildMatch(Page page, MatchEvaluation eval, StepIntent intent, long durationMs, int totalCandidates, List<ScannedElement> allElements) {
        ScannedElement el = eval.element;
        String selector = generatePlaywrightSelector(el, allElements);
        Locator locator = page.locator(selector).first();

        return new ElementMatch.Builder(locator, eval.score.totalScore, ElementMatch.MatchStrategy.BY_SEMANTIC)
            .status(ElementMatch.MatchStatus.FOUND)
            .tagName(el.tagName)
            .id(el.id)
            .actualText(el.text)
            .ariaLabel(el.ariaLabel)
            .role(el.role)
            .visible(el.isVisible)
            .enabled(el.isEnabled)
            .scoreBreakdown(eval.score.breakdown)
            .findDurationMs(durationMs)
            .candidatesScanned(totalCandidates)
            .locatorString(selector)
            .build();
    }

    private String generatePlaywrightSelector(ScannedElement el, List<ScannedElement> allElements) {
        return selectorEngine.generateBestSelector(el, allElements);
    }

    private ElementMatch createErrorMatch(String message, ElementMatch.MatchStatus status) {
        return new ElementMatch.Builder(null, 0.0, ElementMatch.MatchStrategy.BY_SEMANTIC)
            .status(status)
            .selectionReason(message)
            .build();
    }

    private static class MatchEvaluation {
        final ScannedElement element;
        final ElementScorer.ScoreResult score;

        MatchEvaluation(ScannedElement element, ElementScorer.ScoreResult score) {
            this.element = element;
            this.score = score;
        }
    }
}
