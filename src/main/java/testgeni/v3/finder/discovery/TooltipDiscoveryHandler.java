package testgeni.v3.finder.discovery;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import testgeni.v3.core.domain.ActionType;
import testgeni.v3.core.domain.ElementMatch;
import testgeni.v3.core.domain.ScannedElement;
import testgeni.v3.core.domain.StepIntent;
import testgeni.v3.finder.SmartElementFinder;
import testgeni.v3.util.V3Logger;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Specialized discovery logic for finding floating tooltips associated with elements.
 */
public class TooltipDiscoveryHandler implements ElementDiscoveryHandler {

    private final SmartElementFinder finder;

    public TooltipDiscoveryHandler(SmartElementFinder finder) {
        this.finder = finder;
    }

    @Override
    public ElementMatch discover(Page page, StepIntent intent, List<ScannedElement> allElements) {
        if (intent.tooltipOf == null) return null;

        V3Logger.debug("Tooltip discovery triggered for anchor: " + intent.tooltipOf);

        // 1. Find the anchor element first
        StepIntent anchorIntent = new StepIntent.Builder(ActionType.VERIFY, intent.tooltipOf)
            .build();
        
        V3Logger.debug("Locating tooltip anchor: " + intent.tooltipOf);
        ElementMatch anchorMatch = finder.find(anchorIntent, allElements, page);
        
        if (anchorMatch.status != ElementMatch.MatchStatus.FOUND) {
            V3Logger.warn("Tooltip anchor not found: " + intent.tooltipOf);
            return anchorMatch;
        }
        
        // 2. Look for the tooltip element itself
        V3Logger.debug("Anchor found, searching for visible tooltip...");
        
        // Strategy A: Check aria-describedby or aria-errormessage
        if (anchorMatch.id != null) {
            ScannedElement anchor = allElements.stream()
                .filter(el -> el.id.equals(anchorMatch.id))
                .findFirst().orElse(null);
                
            if (anchor != null) {
                String tooltipId = anchor.attributes.get("aria-describedby");
                if (tooltipId == null) tooltipId = anchor.attributes.get("aria-errormessage");
                
                if (tooltipId != null) {
                    V3Logger.debug("Found aria pointer: " + tooltipId);
                    final String tid = tooltipId;
                    ScannedElement tooltip = allElements.stream()
                        .filter(el -> tid.equals(el.id) || tid.equals(el.attributes.get("id")))
                        .findFirst().orElse(null);
                        
                    if (tooltip != null && tooltip.isVisible) {
                        V3Logger.success("Found tooltip via ARIA relationship: " + tid);
                        return buildEnrichedResult(page, tooltip, anchorMatch, "Tooltip (via ARIA)", allElements);
                    }
                }
            }
        }
        
        // Strategy B: Role/Class search
        List<ScannedElement> tooltipCandidates = allElements.stream()
            .filter(el -> el.isVisible)
            .filter(el -> {
                String cls = el.attributes.getOrDefault("class", "").toLowerCase();
                String r = el.role != null ? el.role.toLowerCase() : "";
                return r.contains("tooltip") || cls.contains("tooltip") || cls.contains("popover");
            })
            .collect(Collectors.toList());
            
        if (!tooltipCandidates.isEmpty()) {
            ScannedElement bestTooltip = tooltipCandidates.get(tooltipCandidates.size() - 1);
            V3Logger.success("Found active tooltip via role/class: " + bestTooltip.tagName);
            return buildEnrichedResult(page, bestTooltip, anchorMatch, "Tooltip (via Role/Class)", allElements);
        }
        
        // Strategy C: Text-based Fallback
        if (intent.value != null && !intent.value.isEmpty()) {
            V3Logger.debug("Searching for tooltip by text content: " + intent.value);
            ScannedElement textTooltip = allElements.stream()
                .filter(el -> el.isVisible && el.text != null && el.text.toLowerCase().contains(intent.value.toLowerCase()))
                .findFirst().orElse(null);
                
            if (textTooltip != null) {
                V3Logger.success("Found tooltip by text content: " + textTooltip.tagName);
                return buildEnrichedResult(page, textTooltip, anchorMatch, "Tooltip (via Text)", allElements);
            }
        }

        V3Logger.warn("Could not find a visible tooltip element. Failing back to anchor.");
        return anchorMatch;
    }

    private ElementMatch buildEnrichedResult(Page page, ScannedElement el, ElementMatch anchorMatch, String reason, List<ScannedElement> allElements) {
        String selector = finder.getSelectorEngine().generateBestSelector(el, allElements);
        Locator locator = page.locator(selector).first();
        
        return new ElementMatch.Builder(locator, anchorMatch.confidence, ElementMatch.MatchStrategy.BY_SEMANTIC)
            .status(ElementMatch.MatchStatus.FOUND)
            .tagName(el.tagName)
            .id(el.id)
            .actualText(el.text)
            .ariaLabel(el.ariaLabel)
            .role(el.role)
            .visible(el.isVisible)
            .enabled(el.isEnabled)
            .scoreBreakdown(anchorMatch.scoreBreakdown + " + " + reason)
            .findDurationMs(anchorMatch.findDurationMs)
            .locatorString(selector)
            .selectionReason(reason)
            .build();
    }

    @Override
    public int getPriority() {
        return 20; // Run after cache, before full semantic scan
    }
}
