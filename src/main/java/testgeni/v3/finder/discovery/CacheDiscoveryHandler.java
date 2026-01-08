package testgeni.v3.finder.discovery;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import testgeni.v3.core.domain.ElementMatch;
import testgeni.v3.core.domain.ScannedElement;
import testgeni.v3.core.domain.StepIntent;
import testgeni.v3.orchestration.LocatorCacheRegistry;
import testgeni.v3.util.V3Logger;
import java.util.List;

/**
 * Discovery handler that checks the locator cache for a previously successful match.
 */
public class CacheDiscoveryHandler implements ElementDiscoveryHandler {

    private final LocatorCacheRegistry cacheRegistry;

    public CacheDiscoveryHandler(LocatorCacheRegistry cacheRegistry) {
        this.cacheRegistry = cacheRegistry;
    }

    @Override
    public ElementMatch discover(Page page, StepIntent intent, List<ScannedElement> allElements) {
        // Skip cache for tooltips - they are transient and depend on current hover state
        if (intent.tooltipOf != null) {
            return null;
        }

        String context = page.url();
        String cacheKey = intent.action.name() + "::" + (intent.target != null ? intent.target : "page");
        String cachedSelector = cacheRegistry.getSelector(context, cacheKey);
        
        if (cachedSelector != null) {
            try {
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
        return null;
    }

    @Override
    public int getPriority() {
        return 10; // Run first
    }
}
