package testgeni.v3.executor.interceptor;

import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.LoadState;
import testgeni.v3.core.context.TestContext;
import testgeni.v3.core.domain.ElementMatch;
import testgeni.v3.core.domain.StepIntent;
import testgeni.v3.util.V3Logger;

/**
 * Automates "Implicit Stability" by waiting for page conditions before actions.
 * Removes the need for manual "wait for page load" steps.
 */
public class StabilityInterceptor implements ExecutionInterceptor {

    @Override
    public void intercept(TestContext context, Page page, ElementMatch match, StepIntent intent) {
        // 1. Wait for Network Idle (if configured)
        try {
            V3Logger.trace("StabilityInterceptor", "Checking page stability...");
            page.waitForLoadState(LoadState.NETWORKIDLE, new Page.WaitForLoadStateOptions().setTimeout(2000));
        } catch (Exception e) {
            V3Logger.trace("StabilityInterceptor", "Network did not reach idle within 2s, proceeding anyway.");
        }

        // 2. Clear common loading overlays
        waitForLoadingPickers(page);

        // 3. Target Element Stability (if element exists)
        if (match != null && match.locator != null) {
            try {
                // Ensure element is visible and stable
                match.locator.waitFor(new com.microsoft.playwright.Locator.WaitForOptions().setTimeout(2000));
            } catch (Exception e) {
                V3Logger.warn("StabilityInterceptor: Element not stable after 2s: " + intent.target);
            }
        }
    }

    private void waitForLoadingPickers(Page page) {
        // List of common CSS selectors for loading spinners/overlays
        String[] loadingSelectors = {
            ".spinner", ".loading", ".loader", "#loading-bar", ".overlay", 
            ".busy", "[aria-busy='true']", ".nprogress-busy"
        };

        for (String selector : loadingSelectors) {
            try {
                // Check if any are visible
                if (page.locator(selector).first().isVisible()) {
                    V3Logger.debug("Pre-Flight: Detected loading indicator [" + selector + "], waiting for it to disappear...");
                    page.locator(selector).first().waitFor(new com.microsoft.playwright.Locator.WaitForOptions()
                        .setState(com.microsoft.playwright.options.WaitForSelectorState.HIDDEN)
                        .setTimeout(5000));
                    V3Logger.debug("Pre-Flight: Loading indicator cleared.");
                }
            } catch (Exception e) {
                // Ignore if selector not found or timeout
            }
        }
    }
}
