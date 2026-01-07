package testgeni.v3.executor;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import testgeni.v3.core.context.TestContext;
import testgeni.v3.core.domain.ActionResult;
import testgeni.v3.core.domain.ElementMatch;
import testgeni.v3.core.domain.StepIntent;

/**
 * Robust executor for FILL actions.
 * Handles:
 * - Direct fill() with auto-validation
 * - Retries with focus() if value doesn't stick (Reactive UI support)
 * - Clear field if requested via modifiers
 * - Character-by-character typing if requested
 */
public class FillActionExecutor extends BaseActionExecutor {

    @Override
    protected ActionResult performAction(TestContext context, Page page, ElementMatch match, StepIntent intent) throws Exception {
        if (match == null || match.locator == null) {
            return ActionResult.elementNotFound(intent.target);
        }

        Locator locator = match.locator;
        String value = intent.value != null ? intent.value : "";

        // 1. Initial Fill
        if (intent.getModifier("type_slowly") != null) {
            locator.pressSequentially(value, new Locator.PressSequentiallyOptions().setDelay(50));
        } else {
            locator.fill(value);
        }

        // 2. Post-fill Validation (Ensuring value stuck)
        String actualValue = (String) locator.evaluate("el => el.value || ''");
        if (!actualValue.equals(value)) {
            // Retry with Focus/Click (for frameworks like React/Angular that need interaction events)
            locator.focus();
            locator.click();
            locator.fill(value);
            actualValue = (String) locator.evaluate("el => el.value || ''");
            
            if (!actualValue.equals(value)) {
                return new ActionResult.Builder(false)
                    .message("Value did not stick. Expected: " + value + ", Actual: " + actualValue)
                    .status(ActionResult.ResultStatus.FAILURE)
                    .build();
            }
        }

        return ActionResult.success("Filled " + intent.target + " with '" + value + "'");
    }
}
