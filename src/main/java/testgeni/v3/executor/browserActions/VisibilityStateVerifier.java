package testgeni.v3.executor.browserActions;

import com.microsoft.playwright.Locator;
import testgeni.v3.core.domain.ActionResult;
import testgeni.v3.core.domain.ActionType;
import testgeni.v3.core.domain.StepIntent;

/**
 * Verifies visible/hidden/displayed states of elements.
 * 
 * Handles canonical states:
 * - "visible": Element is visible on the page
 * - "hidden": Element is hidden/not visible
 * 
 * @author TestGeni v3
 * @version 3.0.0
 * @since 2026-01-07
 */
public class VisibilityStateVerifier implements StateVerifier {
    
    @Override
    public ActionResult verify(Locator locator, StepIntent intent, String canonicalState, boolean negated) {
        long startTime = System.currentTimeMillis();
        
        try {
            // Get actual state
            boolean isVisible = locator.isVisible();
            
            // Determine expected state based on canonical state
            boolean expectedState = switch (canonicalState) {
                case "visible" -> true;   // Expect visible
                case "hidden" -> false;   // Expect hidden
                default -> throw new IllegalArgumentException("Unexpected state: " + canonicalState);
            };
            
            // Apply negation if present
            if (negated) {
                expectedState = !expectedState;
            }
            
            // Compare actual vs expected
            boolean passed = (isVisible == expectedState);
            
            // Build result message
            String message = buildResultMessage(
                intent.target, 
                canonicalState, 
                negated, 
                isVisible, 
                passed
            );
            
            long duration = System.currentTimeMillis() - startTime;
            
            return new ActionResult.Builder(passed)
                .action(ActionType.VERIFY)
                .target(intent.target)
                .status(passed ? ActionResult.ResultStatus.SUCCESS : ActionResult.ResultStatus.VERIFICATION_FAILED)
                .message(message)
                .detailedMessage(message)
                .durationMs(duration)
                .build();
                
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            
            return new ActionResult.Builder(false)
                .action(ActionType.VERIFY)
                .target(intent.target)
                .status(ActionResult.ResultStatus.EXCEPTION)
                .message("Failed to verify visibility state: " + e.getMessage())
                .detailedMessage(e.getClass().getSimpleName() + ": " + e.getMessage())
                .durationMs(duration)
                .build();
        }
    }
    
    @Override
    public boolean canHandle(String canonicalState) {
        return "visible".equals(canonicalState) || "hidden".equals(canonicalState);
    }
    
    @Override
    public String getCategory() {
        return "visibility";
    }
    
    /**
     * Build a descriptive result message.
     */
    private String buildResultMessage(
        String target, 
        String canonicalState, 
        boolean negated,
        boolean actualIsVisible, 
        boolean passed) {
        
        if (passed) {
            // Success messages
            if (negated) {
                // "not visible" check passed
                if (canonicalState.equals("visible")) {
                    return String.format("'%s' is correctly NOT visible (hidden)", target);
                } else {
                    return String.format("'%s' is correctly NOT hidden (visible)", target);
                }
            } else {
                // Direct check passed
                if (canonicalState.equals("visible")) {
                    return String.format("'%s' is visible as expected", target);
                } else {
                    return String.format("'%s' is hidden as expected", target);
                }
            }
        } else {
            // Failure messages
            String expectedDesc = getExpectedDescription(canonicalState, negated);
            String actualDesc = actualIsVisible ? "visible" : "hidden";
            
            return String.format("'%s' state mismatch: expected %s but found %s", 
                target, expectedDesc, actualDesc);
        }
    }
    
    /**
     * Get human-readable description of expected state.
     */
    private String getExpectedDescription(String canonicalState, boolean negated) {
        if (negated) {
            return canonicalState.equals("visible") ? "NOT visible (hidden)" : "NOT hidden (visible)";
        } else {
            return canonicalState;
        }
    }
}
