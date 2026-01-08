package testgeni.v3.finder.discovery;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import testgeni.v3.core.domain.*;
import testgeni.v3.finder.BidirectionalMatcher;
import testgeni.v3.finder.ElementScorer;
import testgeni.v3.finder.LocatorStrategy;
import testgeni.v3.finder.SmartElementFinder;
import testgeni.v3.util.V3Logger;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Core discovery handler that performs semantic matching using NLP and keyword strategies.
 */
public class SemanticDiscoveryHandler implements ElementDiscoveryHandler {

    private final SmartElementFinder finder;
    private final ElementScorer scorer;
    private final List<LocatorStrategy> strategies;
    private final BidirectionalMatcher bidirectionalMatcher;
    private static final double MIN_REVERSE_CONFIDENCE = 0.70;

    public SemanticDiscoveryHandler(SmartElementFinder finder, List<LocatorStrategy> strategies) {
        this.finder = finder;
        this.scorer = new ElementScorer();
        this.strategies = strategies;
        this.bidirectionalMatcher = new BidirectionalMatcher();
    }

    @Override
    public ElementMatch discover(Page page, StepIntent intent, List<ScannedElement> elements) {
        long startTime = System.currentTimeMillis();
        
        if (elements == null || elements.isEmpty()) {
            return finder.createErrorMatch("No elements found on page during scan.", ElementMatch.MatchStatus.NOT_FOUND);
        }

        // 1. Scoping
        List<ScannedElement> searchScope = elements;
        if (intent.scopingContext != null) {
            ScannedElement container = finder.findBestContainer(elements, intent);
            if (container != null) {
                final String containerId = container.id;
                searchScope = elements.stream()
                    .filter(el -> el.ancestorIds.contains(containerId))
                    .collect(Collectors.toList());
            }
        }

        List<ScannedElement> filteredScope = searchScope.stream()
            .filter(el -> el.isVisible)
            .collect(Collectors.toList());
        
        V3Logger.debug("Searching " + filteredScope.size() + " elements for: '" + intent.target + "'");

        // 2. Bidirectional Matching
        BidirectionalMatcher.BidirectionalMatchResult bidirectionalResult = 
            bidirectionalMatcher.findBestMatch(filteredScope, intent, MIN_REVERSE_CONFIDENCE);
        
        if (bidirectionalResult != null && bidirectionalResult.isAcceptable) {
            long findDuration = System.currentTimeMillis() - startTime;
            return buildBidirectionalMatch(page, bidirectionalResult, intent, findDuration, elements);
        }
        
        // 3. Fallback: Strategy Search
        V3Logger.debug("Bidirectional matching failed, using fallback strategies...");
        Set<ScannedElement> candidates = new LinkedHashSet<>();
        for (LocatorStrategy strategy : strategies) {
            candidates.addAll(strategy.findCandidates(filteredScope, intent));
        }
        
        if (candidates.isEmpty()) {
            return finder.createErrorMatch("Could not find any candidates for target: " + intent.target, ElementMatch.MatchStatus.NOT_FOUND);
        }

        List<MatchEvaluation> evaluations = candidates.stream()
            .map(el -> new MatchEvaluation(el, scorer.score(el, intent)))
            .sorted(Comparator.comparingDouble((MatchEvaluation e) -> e.score.totalScore).reversed())
            .collect(Collectors.toList());

        MatchEvaluation bestEval = evaluations.get(0);

        // 4. Ordinals
        if (intent.elementIndex != null) {
            int index = intent.elementIndex;
            if (index > 0 && index <= evaluations.size()) {
                bestEval = evaluations.get(index - 1);
            } else if (index == -1 && !evaluations.isEmpty()) {
                bestEval = evaluations.get(evaluations.size() - 1);
            }
        }

        long findDuration = System.currentTimeMillis() - startTime;
        return buildMatch(page, bestEval, intent, findDuration, evaluations.size(), elements);
    }

    private ElementMatch buildBidirectionalMatch(Page page, BidirectionalMatcher.BidirectionalMatchResult result, StepIntent intent, long durationMs, List<ScannedElement> allElements) {
        ScannedElement el = result.forwardMatch.element;
        String selector = finder.getSelectorEngine().generateBestSelector(el, allElements);
        Locator locator = page.locator(selector).first();
        
        String scoreBreakdown = String.format("Bidirectional: Forward=%.2f, Reverse=%.0f%%, Final=%.2f",
            result.forwardMatch.score, result.reverseMatch.confidence * 100, result.finalConfidence);

        return new ElementMatch.Builder(locator, result.finalConfidence, ElementMatch.MatchStrategy.BY_SEMANTIC)
            .status(ElementMatch.MatchStatus.FOUND)
            .tagName(el.tagName).id(el.id).actualText(el.text)
            .scoreBreakdown(scoreBreakdown).findDurationMs(durationMs)
            .locatorString(selector).selectionReason("Bidirectional Match").build();
    }

    private ElementMatch buildMatch(Page page, MatchEvaluation eval, StepIntent intent, long durationMs, int totalCandidates, List<ScannedElement> allElements) {
        ScannedElement el = eval.element;
        String selector = finder.getSelectorEngine().generateBestSelector(el, allElements);
        Locator locator = page.locator(selector).first();

        return new ElementMatch.Builder(locator, eval.score.totalScore, ElementMatch.MatchStrategy.BY_SEMANTIC)
            .status(ElementMatch.MatchStatus.FOUND)
            .tagName(el.tagName).id(el.id).actualText(el.text)
            .scoreBreakdown(eval.score.breakdown).findDurationMs(durationMs)
            .candidatesScanned(totalCandidates).locatorString(selector).build();
    }

    private static class MatchEvaluation {
        final ScannedElement element;
        final ElementScorer.ScoreResult score;
        MatchEvaluation(ScannedElement element, ElementScorer.ScoreResult score) {
            this.element = element;
            this.score = score;
        }
    }

    @Override
    public int getPriority() {
        return 50; // Core search
    }
}
