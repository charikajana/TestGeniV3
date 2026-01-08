package testgeni.v3.finder.discovery;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import testgeni.v3.core.domain.ElementMatch;
import testgeni.v3.core.domain.ScannedElement;
import testgeni.v3.core.domain.StepIntent;
import testgeni.v3.finder.SmartElementFinder;
import testgeni.v3.util.V3Logger;
import java.util.List;

/**
 * Discovery handler that adjusts matches based on proximity and action compatibility.
 * (e.g., if a click hit a label, find the associated checkbox).
 */
public class ProximityDiscoveryHandler implements ElementDiscoveryHandler {

    private final SmartElementFinder finder;

    public ProximityDiscoveryHandler(SmartElementFinder finder) {
        this.finder = finder;
    }

    @Override
    public ElementMatch discover(Page page, StepIntent intent, List<ScannedElement> allElements) {
        // This handler is special: it enriches an EXISTING match.
        // It will be called explicitly by the Discovery Engine after a semantic search.
        return null; // Not intended for primary discovery
    }
    
    public ElementMatch enrich(Page page, ElementMatch match, StepIntent intent, List<ScannedElement> allElements) {
        if (match.status != ElementMatch.MatchStatus.FOUND) return match;
        
        // Only apply proximity matching for interactive input actions
        if (!intent.action.isTextInputAction() && !intent.action.name().equals("SELECT")) {
            return match;
        }

        ScannedElement matchedEl = allElements.stream()
            .filter(el -> el.id.equals(match.id))
            .findFirst()
            .orElse(null);
            
        if (matchedEl == null || intent.action.isCompatibleWith(matchedEl.detectedType)) {
            return match;
        }
        
        V3Logger.debug("Proximity Search: Adjusting match for " + intent.action + " compatibility...");
        
        // 1. Compatible DESCENDANTS
        ScannedElement bestDescendant = allElements.stream()
            .filter(el -> el.isVisible && intent.action.isCompatibleWith(el.detectedType))
            .filter(el -> el.ancestorIds.contains(matchedEl.id))
            .findFirst()
            .orElse(null);
            
        if (bestDescendant != null) {
            V3Logger.info("Proximity MATCH: Found compatible descendant: " + bestDescendant.tagName);
            return buildEnrichedResult(page, bestDescendant, match, "Proximity: Compatible Descendant", allElements);
        }
        
        // 2. Compatible SIBLINGS / NEARBY
        int matchedIndex = allElements.indexOf(matchedEl);
        if (matchedIndex != -1) {
            int start = Math.max(0, matchedIndex - 3);
            int end = Math.min(allElements.size(), matchedIndex + 6);
            for (int i = start; i < end; i++) {
                if (i == matchedIndex) continue;
                ScannedElement candidate = allElements.get(i);
                if (candidate.isVisible && intent.action.isCompatibleWith(candidate.detectedType)) {
                    V3Logger.info("Proximity MATCH: Found compatible nearby relationship: " + candidate.tagName);
                    return buildEnrichedResult(page, candidate, match, "Proximity: Nearby Element", allElements);
                }
            }
        }
        
        return match;
    }

    private ElementMatch buildEnrichedResult(Page page, ScannedElement el, ElementMatch originalMatch, String reason, List<ScannedElement> allElements) {
        String selector = finder.getSelectorEngine().generateBestSelector(el, allElements);
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
            .locatorString(selector)
            .selectionReason(reason)
            .build();
    }
}
