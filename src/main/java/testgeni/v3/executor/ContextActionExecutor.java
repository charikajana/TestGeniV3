package testgeni.v3.executor;

import com.microsoft.playwright.Page;
import testgeni.v3.core.context.TestContext;
import testgeni.v3.core.domain.ActionResult;
import testgeni.v3.core.domain.ElementMatch;
import testgeni.v3.core.domain.StepIntent;
import testgeni.v3.executor.browserActions.SwitchActions;

/**
 * Executor for Context switching actions (Windows, Tabs, Frames).
 * Delegates to SwitchActions for all switching logic.
 */
public class ContextActionExecutor extends BaseActionExecutor {

    @Override
    protected ActionResult performAction(TestContext context, Page page, ElementMatch match, StepIntent intent) throws Exception {
        switch (intent.action) {
            case SWITCH_WINDOW:
            case SWITCH_TAB:
                return SwitchActions.handleWindowSwitch(page, match, intent);
            case SWITCH_FRAME:
                return SwitchActions.handleFrameSwitch(page, match, intent);
            case CLOSE_WINDOW:
                page.close();
                return ActionResult.success("Closed the current window/tab");
            default:
                return ActionResult.failure("Action " + intent.action + " not supported by ContextActionExecutor");
        }
    }
}
