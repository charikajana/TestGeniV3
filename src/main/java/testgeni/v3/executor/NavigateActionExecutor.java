package testgeni.v3.executor;

import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.WaitUntilState;
import testgeni.v3.core.context.TestContext;
import testgeni.v3.core.domain.ActionResult;
import testgeni.v3.core.domain.ElementMatch;
import testgeni.v3.core.domain.StepIntent;

/**
 * Executor for navigation actions like Goto, Back, Forward, Reload.
 */
public class NavigateActionExecutor extends BaseActionExecutor {

    @Override
    protected ActionResult performAction(TestContext context, Page page, ElementMatch match, StepIntent intent) throws Exception {
        String actionName = intent.action.name();
        
        switch (intent.action) {
            case NAVIGATE:
                String url = intent.url;
                if (url == null && intent.target != null && intent.target.startsWith("http")) {
                    url = intent.target;
                }
                if (url == null) return ActionResult.failure("No URL specified for navigation");
                
                page.navigate(url, new Page.NavigateOptions().setWaitUntil(WaitUntilState.LOAD));
                return ActionResult.success("Navigated to " + url);

            case GO_BACK:
                page.goBack();
                return ActionResult.success("Navigated back");

            case GO_FORWARD:
                page.goForward();
                return ActionResult.success("Navigated forward");

            case REFRESH:
                page.reload();
                return ActionResult.success("Page reloaded");

            default:
                return ActionResult.failure("Unsupported navigation action: " + actionName);
        }
    }
}
