package testgeni.v3.executor;

import com.microsoft.playwright.Page;
import testgeni.v3.core.context.TestContext;
import testgeni.v3.core.domain.ActionResult;
import testgeni.v3.core.domain.ElementMatch;
import testgeni.v3.core.domain.StepIntent;

/**
 * Executor for SCROLL actions.
 */
public class ScrollActionExecutor extends BaseActionExecutor {

    @Override
    protected ActionResult performAction(TestContext context, Page page, ElementMatch match, StepIntent intent) throws Exception {
        if (match != null && match.locator != null) {
            match.locator.scrollIntoViewIfNeeded();
            return ActionResult.success("Scrolled to element: " + intent.target);
        }

        String direction = intent.getModifier("direction");
        if ("down".equalsIgnoreCase(direction)) {
            page.mouse().wheel(0, 600);
            return ActionResult.success("Scrolled down");
        } else if ("up".equalsIgnoreCase(direction)) {
            page.mouse().wheel(0, -600);
            return ActionResult.success("Scrolled up");
        } else if ("bottom".equalsIgnoreCase(intent.target) || "bottom".equalsIgnoreCase(direction)) {
            page.evaluate("window.scrollTo(0, document.body.scrollHeight)");
            return ActionResult.success("Scrolled to bottom of page");
        } else if ("top".equalsIgnoreCase(intent.target) || "top".equalsIgnoreCase(direction)) {
            page.evaluate("window.scrollTo(0, 0)");
            return ActionResult.success("Scrolled to top of page");
        }

        return ActionResult.failure("Could not determine scroll target or direction");
    }
}
