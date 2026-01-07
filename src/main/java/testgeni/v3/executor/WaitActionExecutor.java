package testgeni.v3.executor;

import com.microsoft.playwright.Page;
import testgeni.v3.core.context.TestContext;
import testgeni.v3.core.domain.ActionResult;
import testgeni.v3.core.domain.ElementMatch;
import testgeni.v3.core.domain.StepIntent;

/**
 * Executor for WAIT actions.
 * Supports static waits (seconds) and conditional waits (visibility/URL).
 */
public class WaitActionExecutor extends BaseActionExecutor {

    @Override
    protected ActionResult performAction(TestContext context, Page page, ElementMatch match, StepIntent intent) throws Exception {
        int durationMs = intent.waitDurationSeconds != null ? intent.waitDurationSeconds * 1000 : 2000;
        
        if (intent.waitDurationSeconds != null) {
            page.waitForTimeout(durationMs);
            return ActionResult.success("Waited for " + intent.waitDurationSeconds + " seconds");
        }
        
        // If it's a conditional wait, we check the modifier
        String condition = intent.getModifier("condition");
        if ("visible".equals(condition) && match != null && match.locator != null) {
            match.locator.waitFor();
            return ActionResult.success("Waited for element to become visible");
        }

        // Default static wait
        page.waitForTimeout(2000);
        return ActionResult.success("Waited for 2 seconds (default)");
    }
}
