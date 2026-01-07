package testgeni.v3.executor;

import com.microsoft.playwright.Page;
import testgeni.v3.core.context.TestContext;
import testgeni.v3.core.domain.ActionResult;
import testgeni.v3.core.domain.ElementMatch;
import testgeni.v3.core.domain.StepIntent;

/**
 * Executor for MOUSE HOVER actions.
 */
public class HoverActionExecutor extends BaseActionExecutor {

    @Override
    protected ActionResult performAction(TestContext context, Page page, ElementMatch match, StepIntent intent) throws Exception {
        if (match == null || match.locator == null) {
            return ActionResult.elementNotFound(intent.target);
        }

        match.locator.hover();
        
        // Optional: Short wait after hover to let menus appear
        page.waitForTimeout(300);
        
        return ActionResult.success("Hovered over " + intent.target);
    }
}
