package testgeni.v3.executor;

import com.microsoft.playwright.Page;
import com.microsoft.playwright.PlaywrightException;
import testgeni.v3.core.context.TestContext;
import testgeni.v3.core.domain.ActionResult;
import testgeni.v3.core.domain.ElementMatch;
import testgeni.v3.core.domain.StepIntent;

import java.util.ArrayList;
import java.util.List;

/**
 * Base class for all action executors, providing common robustness features.
 */
public abstract class BaseActionExecutor implements ActionExecutor {

    @Override
    public ActionResult execute(TestContext context, Page page, ElementMatch match, StepIntent intent) {
        long startTime = System.currentTimeMillis();
        int maxRetries = 2; // Default retry for transient failures
        int attempt = 0;
        List<String> retryReasons = new ArrayList<>();
        
        while (attempt <= maxRetries) {
            try {
                // 1. Pre-execution checks (Overlay detection, etc.)
                preExecute(context, page, match, intent);
                
                // 2. Perform the actual action
                ActionResult result = performAction(context, page, match, intent);
                
                // 3. Post-execution (Log duration, etc.)
                long duration = System.currentTimeMillis() - startTime;
                return new ActionResult.Builder(result.success)
                    .from(result) // Copy from performAction result
                    .durationMs(duration)
                    .retryAttempts(attempt)
                    .retryReasons(retryReasons)
                    .elementMatch(match)
                    .action(intent.action)
                    .target(intent.target)
                    .value(intent.value)
                    .build();
                    
            } catch (PlaywrightException e) {
                attempt++;
                retryReasons.add(e.getMessage());
                if (attempt > maxRetries || !isRetryable(e)) {
                    return buildFailureResult(e, intent, match, attempt, retryReasons, startTime);
                }
                // Wait a bit before retry
                page.waitForTimeout(500);
            } catch (Exception e) {
                return buildFailureResult(e, intent, match, attempt, retryReasons, startTime);
            }
        }
        
        return ActionResult.failure("Action failed after " + maxRetries + " retries.");
    }

    /**
     * The actual implementation of the action (Click, Fill, etc.)
     */
    protected abstract ActionResult performAction(TestContext context, Page page, ElementMatch match, StepIntent intent) throws Exception;

    protected void preExecute(TestContext context, Page page, ElementMatch match, StepIntent intent) {
        // Run all registered "Pre-Flight" interceptors
        for (testgeni.v3.executor.interceptor.ExecutionInterceptor interceptor : context.getInterceptors()) {
            interceptor.intercept(context, page, match, intent);
        }
    }

    protected boolean isRetryable(PlaywrightException e) {
        String msg = e.getMessage().toLowerCase();
        return msg.contains("covered by") || 
               msg.contains("stale") || 
               msg.contains("not visible") ||
               msg.contains("detached");
    }

    private ActionResult buildFailureResult(Exception e, StepIntent intent, ElementMatch match, int attempt, List<String> retryReasons, long startTime) {
        return new ActionResult.Builder(false)
            .message(e.getMessage())
            .exception(e)
            .action(intent.action)
            .target(intent.target)
            .value(intent.value)
            .elementMatch(match)
            .retryAttempts(attempt)
            .retryReasons(retryReasons)
            .durationMs(System.currentTimeMillis() - startTime)
            .status(ActionResult.ResultStatus.FAILURE)
            .build();
    }
}
