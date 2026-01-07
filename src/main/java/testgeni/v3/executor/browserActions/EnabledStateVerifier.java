package testgeni.v3.executor.browserActions;

import com.microsoft.playwright.Locator;
import testgeni.v3.core.domain.ActionResult;
import testgeni.v3.core.domain.ActionType;
import testgeni.v3.core.domain.StepIntent;

/**
 * Verifies enabled/disabled/clickable/interactive states of elements.
 * 
 * Handles canonical states:
 * - "enabled": Element is enabled and can be interacted with
 * - "disabled": Element is disabled and cannot be interacted with
 * 
 * @author TestGeni v3
 * @version 3.0.0
 * @since 2026-01-07
 */
public class EnabledStateVerifier implements StateVerifier {
    
    @Override
    public ActionResult verify(Locator locator, StepIntent intent, String canonicalState, boolean negated) {
        long startTime = System.currentTimeMillis();
        
        try {
            // Get actual state
            boolean isEnabled = locator.isEnabled();
            
            // Determine expected state based on canonical state
            boolean expectedState = switch (canonicalState) {
                case "enabled" -> true;   // Expect enabled
                case "disabled" -> false;  // Expect disabled
                default -> throw new IllegalArgumentException("Unexpected state: " + canonicalState);
            };
            
            // Apply negation if present
            if (negated) {
                expectedState = !expectedState;
            }
            
            // Compare actual vs expected
            boolean passed = (isEnabled == expectedState);
            
            // Build result message
            String message = buildResultMessage(
                intent.target, 
                canonicalState, 
                negated, 
                isEnabled, 
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
                .message("Failed to verify enabled state: " + e.getMessage())
                .detailedMessage(e.getClass().getSimpleName() + ": " + e.getMessage())
                .durationMs(duration)
                .build();
        }
    }
    
    @Override
    public boolean canHandle(String canonicalState) {
        return "enabled".equals(canonicalState) || "disabled".equals(canonicalState);
    }
    
    @Override
    public String getCategory() {
        return "enablement";
    }
    
    /**
     * Build a descriptive result message.
     */
    private String buildResultMessage(
        String target, 
        String canonicalState, 
        boolean negated,
        boolean actualIsEnabled, 
        boolean passed) {
        
        if (passed) {
            // Success messages
            if (negated) {
                // "not enabled" check passed
                if (canonicalState.equals("enabled")) {
                    return String.format("'%s' is correctly NOT enabled (disabled)", target);
                } else {
                    return String.format("'%s' is correctly NOT disabled (enabled)", target);
                }
            } else {
                // Direct check passed
                if (canonicalState.equals("enabled")) {
                    return String.format("'%s' is enabled as expected", target);
                } else {
                    return String.format("'%s' is disabled as expected", target);
                }
            }
        } else {
            // Failure messages
            String expectedDesc = getExpectedDescription(canonicalState, negated);
            String actualDesc = actualIsEnabled ? "enabled" : "disabled";
            
            return String.format("'%s' state mismatch: expected %s but found %s", 
                target, expectedDesc, actualDesc);
        }
    }
    
    /**
     * Get human-readable description of expected state.
     */
    private String getExpectedDescription(String canonicalState, boolean negated) {
        if (negated) {
            return canonicalState.equals("enabled") ? "NOT enabled (disabled)" : "NOT disabled (enabled)";
        } else {
            return canonicalState;
        }
    }
}
