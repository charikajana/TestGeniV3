package testgeni.v3.executor;

import com.microsoft.playwright.Page;
import testgeni.v3.core.context.TestContext;
import testgeni.v3.core.domain.ActionResult;
import testgeni.v3.core.domain.ElementMatch;
import testgeni.v3.core.domain.StepIntent;

/**
 * Interface for executing a specific action on a page.
 */
public interface ActionExecutor {

    /**
     * Executes the action defined in the intent on the matched element.
     * 
     * @param context The current execution context (service registry)
     * @param page The Playwright page
     * @param match The matched element (can be null for some actions)
     * @param intent The user intent
     * @return ActionResult containing success status and evidence
     */
    ActionResult execute(TestContext context, Page page, ElementMatch match, StepIntent intent);
}
