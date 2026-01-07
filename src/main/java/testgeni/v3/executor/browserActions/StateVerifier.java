package testgeni.v3.executor.browserActions;

import com.microsoft.playwright.Locator;
import testgeni.v3.core.domain.ActionResult;
import testgeni.v3.core.domain.StepIntent;

/**
 * Interface for state verification handlers.
 * Each implementation verifies a specific category of element states.
 * 
 * @author TestGeni v3
 * @version 3.0.0
 * @since 2026-01-07
 */
public interface StateVerifier {
    
    /**
     * Verify the state of an element.
     * 
     * @param locator Playwright locator for the element
     * @param intent Step intent containing verification details
     * @param canonicalState Canonical state name (e.g., "enabled", "selected")
     * @param negated Whether the verification is negated (e.g., "not enabled")
     * @return Action result with pass/fail status
     */
    ActionResult verify(Locator locator, StepIntent intent, String canonicalState, boolean negated);
    
    /**
     * Check if this verifier can handle the given canonical state.
     * 
     * @param canonicalState Canonical state name
     * @return true if this verifier can handle the state
     */
    boolean canHandle(String canonicalState);
    
    /**
     * Get the category this verifier handles.
     * 
     * @return Category name (e.g., "enablement", "selection", "visibility")
     */
    String getCategory();
}
