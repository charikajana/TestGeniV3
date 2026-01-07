package testgeni.v3.executor.interceptor;

import com.microsoft.playwright.Page;
import testgeni.v3.core.context.TestContext;
import testgeni.v3.core.domain.ElementMatch;
import testgeni.v3.core.domain.StepIntent;

/**
 * Interface for "Pre-Flight" interceptors that run before an action.
 * Used for stability checks, overlay detection, etc.
 */
public interface ExecutionInterceptor {
    /**
     * Intercepts the execution flow before the action is performed.
     * Should throw a RuntimeException if the condition for execution is not met.
     */
    void intercept(TestContext context, Page page, ElementMatch match, StepIntent intent);
}
