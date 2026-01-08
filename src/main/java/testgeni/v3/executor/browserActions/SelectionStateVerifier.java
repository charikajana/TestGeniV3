package testgeni.v3.executor.browserActions;

import com.microsoft.playwright.Locator;
import testgeni.v3.core.domain.ActionResult;
import testgeni.v3.core.domain.ActionType;
import testgeni.v3.core.domain.StepIntent;

/**
 * Verifies selected/checked/unchecked states of elements.
 * Primarily used for checkboxes, radio buttons, and selectable items.
 * 
 * Handles canonical states:
 * - "selected": Element is selected/checked
 * - "unselected": Element is not selected/unchecked
 * 
 * @author TestGeni v3
 * @version 3.0.0
 * @since 2026-01-07
 */
public class SelectionStateVerifier implements StateVerifier {
    
    @Override
    public ActionResult verify(Locator locator, StepIntent intent, String canonicalState, boolean negated) {
        long startTime = System.currentTimeMillis();
        
        try {
            // Get actual state
            boolean isChecked;
            try {
                isChecked = locator.isChecked();
            } catch (Exception e) {
                // If the element itself is not checkable, it might be a wrapper (like Bootstrap's custom-control).
                // Try finding a native checkable child element.
                Locator childInput = locator.locator("input[type='checkbox'], input[type='radio']").first();
                if (childInput.count() > 0) {
                    isChecked = childInput.isChecked();
                } else {
                    throw e; // Rethrow if no checkable child found
                }
            }
            
            // Determine expected state based on canonical state
            boolean expectedState = switch (canonicalState) {
                case "selected" -> true;     // Expect selected/checked
                case "unselected" -> false;  // Expect unselected/unchecked
                default -> throw new IllegalArgumentException("Unexpected state: " + canonicalState);
            };
            
            // Apply negation if present
            if (negated) {
                expectedState = !expectedState;
            }
            
            // Compare actual vs expected
            boolean passed = (isChecked == expectedState);
            
            // ========================================
            // SMART FALLBACK: Non-native components
            // ========================================
            if (!passed && !isChecked) {
                // If it's not a native checkable, try ARIA and class patterns
                String ariaChecked = locator.getAttribute("aria-checked");
                String ariaSelected = locator.getAttribute("aria-selected");
                String className = locator.getAttribute("class");
                if (className == null) className = "";

                boolean ariaPassed = "true".equalsIgnoreCase(ariaChecked) || "true".equalsIgnoreCase(ariaSelected);
                boolean classPassed = className.toLowerCase().contains("selected") || 
                                     className.toLowerCase().contains("active") || 
                                     className.toLowerCase().contains("checked") ||
                                     className.toLowerCase().contains("multi-value") ||
                                     className.toLowerCase().contains("tag") ||
                                     className.toLowerCase().contains("pill");
                
                if (ariaPassed || classPassed) {
                   isChecked = true;
                   passed = (isChecked == expectedState);
                }
            }
            
            // Build result message
            String message = buildResultMessage(
                intent.target, 
                canonicalState, 
                negated, 
                isChecked, 
                passed
            );
            
            long duration = System.currentTimeMillis() - startTime;
            
            return new ActionResult.Builder(passed)
                .action(ActionType.VERIFY)
                .target(intent.target)
                .expectedValue(getExpectedDescription(canonicalState, negated))
                .actualValue(isChecked ? "selected" : "not selected")
                .status(passed ? ActionResult.ResultStatus.SUCCESS : ActionResult.ResultStatus.VERIFICATION_FAILED)
                .message(message)
                .detailedMessage(message)
                .durationMs(duration)
                .build();
                
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            
            // SMART FALLBACK: If it failed because it's not a checkable, try the content/aria check anyway
            try {
                String ariaChecked = locator.getAttribute("aria-checked");
                String ariaSelected = locator.getAttribute("aria-selected");
                String className = locator.getAttribute("class");
                if (className == null) className = "";

                boolean isC = "true".equalsIgnoreCase(ariaChecked) || 
                              "true".equalsIgnoreCase(ariaSelected) ||
                              className.toLowerCase().contains("selected") || 
                              className.toLowerCase().contains("active");

                boolean expectedState = canonicalState.equals("selected");
                if (negated) expectedState = !expectedState;
                boolean passed = (isC == expectedState);

                if (passed || isC) { // If it's actually selected or matches expectation
                     String msg = buildResultMessage(intent.target, canonicalState, negated, isC, passed);
                      return new ActionResult.Builder(passed)
                        .action(ActionType.VERIFY)
                        .target(intent.target)
                        .expectedValue(getExpectedDescription(canonicalState, negated))
                        .actualValue(isC ? "selected" : "not selected")
                        .status(passed ? ActionResult.ResultStatus.SUCCESS : ActionResult.ResultStatus.VERIFICATION_FAILED)
                        .message(msg)
                        .durationMs(duration)
                        .build();
                }
            } catch (Exception ignored) {}

            // Special handling for elements that don't support isChecked()
            String errorMessage = e.getMessage();
            if (errorMessage != null && errorMessage.contains("Not a checkbox or radio button")) {
                return new ActionResult.Builder(false)
                    .action(ActionType.VERIFY)
                    .target(intent.target)
                    .status(ActionResult.ResultStatus.EXCEPTION)
                    .message("'" + intent.target + "' is not a checkbox/radio button - cannot verify selection state")
                    .detailedMessage("Element does not support selection state verification")
                    .durationMs(duration)
                    .build();
            }
            
            return new ActionResult.Builder(false)
                .action(ActionType.VERIFY)
                .target(intent.target)
                .status(ActionResult.ResultStatus.EXCEPTION)
                .message("Failed to verify selection state: " + e.getMessage())
                .detailedMessage(e.getClass().getSimpleName() + ": " + e.getMessage())
                .durationMs(duration)
                .build();
        }
    }
    
    @Override
    public boolean canHandle(String canonicalState) {
        return "selected".equals(canonicalState) || "unselected".equals(canonicalState);
    }
    
    @Override
    public String getCategory() {
        return "selection";
    }
    
    /**
     * Build a descriptive result message.
     */
    private String buildResultMessage(
        String target, 
        String canonicalState, 
        boolean negated,
        boolean actualIsChecked, 
        boolean passed) {
        
        if (passed) {
            // Success messages
            if (negated) {
                // "not selected" check passed
                if (canonicalState.equals("selected")) {
                    return String.format("'%s' is correctly NOT selected (unchecked)", target);
                } else {
                    return String.format("'%s' is correctly NOT unselected (checked)", target);
                }
            } else {
                // Direct check passed
                if (canonicalState.equals("selected")) {
                    return String.format("'%s' is selected/checked as expected", target);
                } else {
                    return String.format("'%s' is unselected/unchecked as expected", target);
                }
            }
        } else {
            // Failure messages
            String expectedDesc = getExpectedDescription(canonicalState, negated);
            String actualDesc = actualIsChecked ? "selected/checked" : "unselected/unchecked";
            
            return String.format("'%s' state mismatch: expected %s but found %s", 
                target, expectedDesc, actualDesc);
        }
    }
    
    /**
     * Get human-readable description of expected state.
     */
    private String getExpectedDescription(String canonicalState, boolean negated) {
        if (negated) {
            return canonicalState.equals("selected") 
                ? "NOT selected (unchecked)" 
                : "NOT unselected (checked)";
        } else {
            return canonicalState.equals("selected") 
                ? "selected/checked" 
                : "unselected/unchecked";
        }
    }
}
