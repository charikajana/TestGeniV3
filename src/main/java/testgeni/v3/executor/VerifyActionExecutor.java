package testgeni.v3.executor;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import testgeni.v3.core.context.TestContext;
import testgeni.v3.core.domain.ActionResult;
import testgeni.v3.core.domain.ElementMatch;
import testgeni.v3.core.domain.StepIntent;
import testgeni.v3.executor.browserActions.StateVerifierRegistry;

/**
 * Comprehensive executor for VERIFY actions.
 */
public class VerifyActionExecutor extends BaseActionExecutor {
    
    private final StateVerifierRegistry stateVerifierRegistry;
    
    public VerifyActionExecutor() {
        this.stateVerifierRegistry = new StateVerifierRegistry();
    }

    @Override
    protected ActionResult performAction(TestContext context, Page page, ElementMatch match, StepIntent intent) throws Exception {
        boolean isNegated = intent.negated;
        String attribute = intent.verificationAttribute != null ? intent.verificationAttribute.toLowerCase() : "visible";
        String expectedValue = intent.value != null ? intent.value : intent.target;
        String actualValue = "";
        boolean passed = false;

        // 1. Handle Page-level verifications (Page Title, URL) that don't need a locator
        if (intent.target != null && (intent.target.toLowerCase().contains("page title") || "title".equals(attribute))) {
            actualValue = page.title();
            passed = actualValue.toLowerCase().contains(expectedValue.toLowerCase());
            return buildResult(passed, isNegated, expectedValue, actualValue);
        }

        if (intent.target != null && (intent.target.toLowerCase().contains("url") || "url".equals(attribute))) {
            actualValue = page.url();
            passed = actualValue.toLowerCase().contains(expectedValue.toLowerCase());
            return buildResult(passed, isNegated, expectedValue, actualValue);
        }

        if (intent.target != null && intent.target.toLowerCase().contains("window count")) {
            actualValue = String.valueOf(page.context().pages().size());
            passed = actualValue.equals(expectedValue);
            return buildResult(passed, isNegated, expectedValue, actualValue);
        }

        // 2. Handle "Presence" check specifically (if match is null)
        if (match == null || match.status != ElementMatch.MatchStatus.FOUND) {
            if (isNegated && ("present".equals(attribute) || "exists".equals(attribute) || "visible".equals(attribute))) {
                return ActionResult.success("Verification passed: Element is NOT " + attribute);
            }
            return ActionResult.elementNotFound(intent.target);
        }

        Locator locator = match.locator;
        
        // ========================================
        // NEW: Route to State Verifier if it's a state verification
        // ========================================
        if (stateVerifierRegistry.isStateVerification(attribute)) {
            return stateVerifierRegistry.verify(locator, intent);
        }
        
        // ========================================
        // LEGACY: Content/Value verification (text, value, etc.)
        // ========================================

        switch (attribute) {
            case "enabled":
                passed = locator.isEnabled();
                actualValue = passed ?  "enabled" : "disabled";
                break;
            case "disabled":
                passed = !locator.isEnabled();
                actualValue = passed ? "disabled" : "enabled";
                break;
            case "checked":
            case "selected":
                passed = locator.isChecked();
                actualValue = passed ? "checked" : "unchecked";
                break;
            case "focused":
                passed = (Boolean) locator.evaluate("el => document.activeElement === el");
                actualValue = passed ? "focused" : "not focused";
                break;
            case "value":
                actualValue = (String) locator.evaluate("el => el.value || ''");
                passed = actualValue.contains(expectedValue);
                break;
            case "visible":
            case "displayed":
            case "present":
            case "exists":
            default:
                if (intent.value != null || !"visible".equals(attribute)) {
                    actualValue = locator.innerText();
                    if (actualValue == null || actualValue.isEmpty()) {
                        actualValue = (String) locator.evaluate("el => el.value || ''");
                    }
                    passed = actualValue != null && actualValue.toLowerCase().contains(expectedValue.toLowerCase());
                } else {
                    passed = locator.isVisible();
                    actualValue = passed ? "visible" : "hidden";
                }
                break;
        }

        return buildResult(passed, isNegated, expectedValue, actualValue);
    }

    private ActionResult buildResult(boolean passed, boolean isNegated, String expected, String actual) {
        boolean finalResult = isNegated ? !passed : passed;
        String message;
        if (finalResult) {
            message = String.format("Verification Passed: Found [%s] as expected", actual);
        } else {
            message = String.format("Verification Failed: Expected [%s] but found [%s]", 
                (isNegated ? "NOT " + expected : expected), actual);
        }

        return new ActionResult.Builder(finalResult)
            .verificationPassed(finalResult)
            .expectedValue(isNegated ? "NOT " + expected : expected)
            .actualValue(actual)
            .message(message)
            .status(finalResult ? ActionResult.ResultStatus.SUCCESS : ActionResult.ResultStatus.VERIFICATION_FAILED)
            .build();
    }
}
