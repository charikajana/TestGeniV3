package testgeni.v3.executor.browserActions;

import com.microsoft.playwright.Locator;
import testgeni.v3.core.domain.ActionResult;
import testgeni.v3.core.domain.StepIntent;

import java.util.HashMap;
import java.util.Map;

/**
 * Registry that routes state verification requests to the appropriate verifier.
 * Acts as a facade for all state verification operations.
 * 
 * @author TestGeni v3
 * @version 3.0.0
 * @since 2026-01-07
 */
public class StateVerifierRegistry {
    
    private final Map<String, StateVerifier> verifiers;
    
    /**
     * Constructor - registers all state verifiers.
     */
    public StateVerifierRegistry() {
        this.verifiers = new HashMap<>();
        registerVerifiers();
    }
    
    /**
     * Register all state verifiers.
     */
    private void registerVerifiers() {
        // Enablement verifier
        StateVerifier enabledVerifier = new EnabledStateVerifier();
        verifiers.put(enabledVerifier.getCategory(), enabledVerifier);
        
        // Selection verifier
        StateVerifier selectionVerifier = new SelectionStateVerifier();
        verifiers.put(selectionVerifier.getCategory(), selectionVerifier);
        
        // Visibility verifier
        StateVerifier visibilityVerifier = new VisibilityStateVerifier();
        verifiers.put(visibilityVerifier.getCategory(), visibilityVerifier);
    }
    
    /**
     * Verify element state using the appropriate verifier.
     * 
     * @param locator Playwright locator for the element
     * @param intent Step intent containing verification details
     * @return Action result with pass/fail status
     */
    public ActionResult verify(Locator locator, StepIntent intent) {
        // Get the verification attribute (state synonym)
        String stateAttribute = intent.verificationAttribute;
        
        if (stateAttribute == null || stateAttribute.isEmpty()) {
            return new ActionResult.Builder(false)
                .action(intent.action)
                .target(intent.target)
                .status(ActionResult.ResultStatus.EXCEPTION)
                .message("No state attribute specified for verification")
                .detailedMessage("verificationAttribute is null or empty")
                .durationMs(0)
                .build();
        }
        
        // Convert synonym to canonical state
        String canonicalState = StateVerificationPatterns.getCanonicalState(stateAttribute);
        
        if (canonicalState == null) {
            return new ActionResult.Builder(false)
                .action(intent.action)
                .target(intent.target)
                .status(ActionResult.ResultStatus.EXCEPTION)
                .message("Unknown state attribute: " + stateAttribute)
                .detailedMessage("State attribute not recognized: " + stateAttribute)
                .durationMs(0)
                .build();
        }
        
        // Get the state category
        String category = StateVerificationPatterns.getStateCategory(canonicalState);
        
        if (category == null) {
            return new ActionResult.Builder(false)
                .action(intent.action)
                .target(intent.target)
                .status(ActionResult.ResultStatus.EXCEPTION)
                .message("No category found for state: " + canonicalState)
                .detailedMessage("Cannot determine category for: " + canonicalState)
                .durationMs(0)
                .build();
        }
        
        // Get the appropriate verifier
        StateVerifier verifier = verifiers.get(category);
        
        if (verifier == null) {
            return new ActionResult.Builder(false)
                .action(intent.action)
                .target(intent.target)
                .status(ActionResult.ResultStatus.EXCEPTION)
                .message("No verifier registered for category: " + category)
                .detailedMessage("Missing verifier implementation for: " + category)
                .durationMs(0)
                .build();
        }
        
        // Perform verification
        boolean negated = intent.negated;
        return verifier.verify(locator, intent, canonicalState, negated);
    }
    
    /**
     * Check if a verification attribute is a state verification.
     * 
     * @param verificationAttribute Attribute to check
     * @return true if it's a state attribute
     */
    public boolean isStateVerification(String verificationAttribute) {
        if (verificationAttribute == null) {
            return false;
        }
        return StateVerificationPatterns.isStateAttribute(verificationAttribute);
    }
    
    /**
     * Get canonical state name for a synonym.
     * 
     * @param synonym State synonym
     * @return Canonical state name, or null if not recognized
     */
    public String getCanonicalState(String synonym) {
        return StateVerificationPatterns.getCanonicalState(synonym);
    }
}
