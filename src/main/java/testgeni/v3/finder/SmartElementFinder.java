package testgeni.v3.finder;

import com.microsoft.playwright.Page;
import testgeni.v3.core.domain.*;
import testgeni.v3.scanner.PageScanner;
import testgeni.v3.finder.discovery.*;
import testgeni.v3.finder.selector.SelectorStrategyEngine;
import testgeni.v3.orchestration.LocatorCacheRegistry;
import testgeni.v3.util.V3Logger;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Intelligent implementation of ElementFinder using a modular Discovery Pipeline.
 */
public class SmartElementFinder implements ElementFinder {

    private final PageScanner scanner;
    private final ElementScorer scorer;
    private final List<LocatorStrategy> strategies;
    private final SelectorStrategyEngine selectorEngine;
    private final LocatorCacheRegistry cacheRegistry;
    
    // Discovery Pipeline
    private final List<ElementDiscoveryHandler> discoveryPipeline;
    private final ProximityDiscoveryHandler proximityHandler;

    public SmartElementFinder(PageScanner scanner, LocatorCacheRegistry cacheRegistry) {
        this.scanner = scanner;
        this.scorer = new ElementScorer();
        this.strategies = List.of(
            new ByAttributeStrategy(),
            new ByTextStrategy()
        );
        this.selectorEngine = new SelectorStrategyEngine();
        this.cacheRegistry = cacheRegistry != null ? cacheRegistry : new LocatorCacheRegistry();
        
        // Initialize Pipeline
        this.proximityHandler = new ProximityDiscoveryHandler(this);
        this.discoveryPipeline = new ArrayList<>();
        this.discoveryPipeline.add(new CacheDiscoveryHandler(this.cacheRegistry));
        this.discoveryPipeline.add(new TooltipDiscoveryHandler(this));
        this.discoveryPipeline.add(new SemanticDiscoveryHandler(this, this.strategies));
        
        // Sort by priority
        this.discoveryPipeline.sort(Comparator.comparingInt(ElementDiscoveryHandler::getPriority));
    }

    @Override
    public ElementMatch find(Page page, StepIntent intent) {
        // 1. Run Pipeline
        List<ScannedElement> allElements = null;
        ElementMatch match = null;

        for (ElementDiscoveryHandler handler : discoveryPipeline) {
            // Some handlers (like Cache) don't need elements. Others do.
            if (handler instanceof CacheDiscoveryHandler) {
                match = handler.discover(page, intent, null);
            } else {
                if (allElements == null) {
                    allElements = scanAllFrames(page);
                }
                match = handler.discover(page, intent, allElements);
            }

            // If we found a definitive match, stop early
            if (match != null && match.status == ElementMatch.MatchStatus.FOUND) {
                break;
            }
        }

        // 2. Final Fallback if no handler caught it
        if (match == null) {
            match = createErrorMatch("Element not found: " + intent.target, ElementMatch.MatchStatus.NOT_FOUND);
        }

        // 3. Proximity Enrichment (Action Compatibility check)
        if (match.status == ElementMatch.MatchStatus.FOUND) {
            if (allElements == null) allElements = scanAllFrames(page);
            match = proximityHandler.enrich(page, match, intent, allElements);
        }

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

    public List<ScannedElement> scanAllFrames(Page page) {
        List<ScannedElement> allElements = new ArrayList<>();
        allElements.addAll(scanner.scan(page));
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

    // --- Helper Methods for Handlers ---

    @Override
    public ElementMatch find(StepIntent intent, List<ScannedElement> elements, Page page) {
        // Legacy support/direct access for handlers
        SemanticDiscoveryHandler semantic = (SemanticDiscoveryHandler) discoveryPipeline.stream()
            .filter(h -> h instanceof SemanticDiscoveryHandler)
            .findFirst().orElse(null);
        return semantic != null ? semantic.discover(page, intent, elements) : null;
    }

    public ScannedElement findBestContainer(List<ScannedElement> elements, StepIntent intent) {
        StepIntent containerIntent = new StepIntent.Builder(ActionType.VERIFY, intent.scopingContext)
            .elementIndex(intent.contextIndex)
            .build();
        
        Set<ScannedElement> containerCandidates = new LinkedHashSet<>();
        for (LocatorStrategy strategy : strategies) {
            containerCandidates.addAll(strategy.findCandidates(elements, containerIntent));
        }

        if (containerCandidates.isEmpty()) return null;

        return containerCandidates.stream()
            .map(el -> new MatchEvaluation(el, scorer.score(el, containerIntent)))
            .sorted(Comparator.comparingDouble((MatchEvaluation e) -> e.score.totalScore).reversed())
            .findFirst().get().element;
    }

    public SelectorStrategyEngine getSelectorEngine() {
        return selectorEngine;
    }

    public ElementMatch createErrorMatch(String message, ElementMatch.MatchStatus status) {
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
