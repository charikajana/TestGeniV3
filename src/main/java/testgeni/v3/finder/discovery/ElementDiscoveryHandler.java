package testgeni.v3.finder.discovery;

import com.microsoft.playwright.Page;
import testgeni.v3.core.domain.ElementMatch;
import testgeni.v3.core.domain.ScannedElement;
import testgeni.v3.core.domain.StepIntent;
import java.util.List;

/**
 * Interface for specialized element discovery logic.
 */
public interface ElementDiscoveryHandler {
    
    /**
     * Executes the discovery logic.
     * 
     * @param page The Playwright page
     * @param intent The parsed step intent
     * @param allElements All scanned elements on the page (may be null if not scanned yet)
     * @return An ElementMatch if found, or null/empty match to continue pipeline
     */
    ElementMatch discover(Page page, StepIntent intent, List<ScannedElement> allElements);
    
    /**
     * Priority of this handler. Lower values run first.
     */
    default int getPriority() {
        return 100;
    }
}
