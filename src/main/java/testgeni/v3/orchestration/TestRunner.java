package testgeni.v3.orchestration;

import com.microsoft.playwright.Page;
import testgeni.v3.core.context.TestContext;
import testgeni.v3.core.domain.*;
import testgeni.v3.executor.ActionExecutor;
import testgeni.v3.executor.ActionExecutorRegistry;
import testgeni.v3.finder.ElementFinder;
import testgeni.v3.finder.SmartElementFinder;
import testgeni.v3.parser.GherkinStepParser;
import testgeni.v3.parser.StepParser;
import testgeni.v3.scanner.PageScanner;
import testgeni.v3.scanner.PlaywrightScanner;
import testgeni.v3.util.V3Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * The central engine that orchestrates the entire V3 pipeline:
 * Parser -> Scanner -> Finder -> Executor.
 */
public class TestRunner {

    private final TestContext context;

    public TestRunner(TestConfig config) {
        this.context = new TestContext(config != null ? config : TestConfig.defaultConfig());
        
        // Initialize and Register Services
        StepParser parser = new GherkinStepParser();
        PageScanner scanner = new PlaywrightScanner();
        LocatorCacheRegistry cacheRegistry = new LocatorCacheRegistry();
        ElementFinder finder = new SmartElementFinder(scanner, cacheRegistry);
        ActionExecutorRegistry executorRegistry = new ActionExecutorRegistry();
        
        context.register(StepParser.class, parser);
        context.register(PageScanner.class, scanner);
        context.register(LocatorCacheRegistry.class, cacheRegistry);
        context.register(ElementFinder.class, finder);
        context.register(ActionExecutorRegistry.class, executorRegistry);

        // Register Interceptors
        context.addInterceptor(new testgeni.v3.executor.interceptor.StabilityInterceptor());
    }

    /**
     * Executes a single natural language step.
     */
    public ActionResult executeStep(Page page, String stepText) {
        long startTime = System.currentTimeMillis();
        StepIntent intent = null;
        ElementMatch match = null;
        ActionResult result = null;
        
        V3Logger.debug("═══════════════════════════════════════════════════════════");
        V3Logger.debug("Starting step execution");
        V3Logger.trace("Step Text", stepText);
        
        try {
            // 1. Parse Intent
            V3Logger.debug("STAGE 1: Parsing step intent...");
            intent = context.getParser().parse(stepText);
            V3Logger.trace("Parse Result", String.format("Action=%s, Target=%s, Value=%s", 
                intent.action, intent.target, intent.value));
            
            // 2. Find Element (Let the finder handle caching and scanning internally)
            boolean needsElementForClickAndSwitch = (intent.action == ActionType.SWITCH_WINDOW || intent.action == ActionType.SWITCH_TAB) 
                && "true".equals(intent.getModifier("performClick"));
            
            // SPECIAL CASE: Page/URL verification doesn't require a DOM element
            boolean isPageLevel = (intent.action == ActionType.VERIFY || intent.action == ActionType.VERIFY_STATE || intent.action == ActionType.VERIFY_TEXT) && 
                (intent.target != null && (intent.target.toLowerCase().contains("page title") || intent.target.toLowerCase().contains("url") || intent.target.toLowerCase().contains("window count")));

            if ((intent.action.needsElement() || needsElementForClickAndSwitch) && !isPageLevel) {
                V3Logger.debug("STAGE 2 & 3: Locating target element...");
                match = context.getFinder().find(page, intent);
                
                if (match == null || match.status == ElementMatch.MatchStatus.NOT_FOUND) {
                    V3Logger.warn(String.format("Element not found: %s", intent.target));
                    
                    if (!isPageLevel) {
                        V3Logger.error("Element not found and not a page-level operation. Failing step.");
                        result = enrich(ActionResult.elementNotFound(intent.target), intent, startTime);
                        V3Logger.logStepExecution(stepText, intent, match, result);
                        return result;
                    } else {
                        V3Logger.debug("Page-level verification detected, skipping element search");
                    }
                } else {
                    V3Logger.success(String.format("Element found: %s (confidence: %.0f%%)", 
                        match.locatorString, match.confidence * 100));
                }
            } else {
                V3Logger.debug("STAGE 2 & 3: Element not needed for action type: " + intent.action);
            }
            
            // 3. Execute Action
            V3Logger.debug("STAGE 4: Executing action...");
            ActionExecutor executor = context.getExecutorRegistry().getExecutor(intent.action);
            if (executor == null) {
                V3Logger.error("No executor registered for action: " + intent.action);
                result = enrich(ActionResult.failure("No executor registered for " + intent.action), intent, startTime);
                V3Logger.logStepExecution(stepText, intent, match, result);
                return result;
            }
            
            V3Logger.trace("Executor", executor.getClass().getSimpleName());
            ActionResult actionResult = executor.execute(context, page, match, intent);
            V3Logger.trace("Execution Result", String.format("Status=%s, Message=%s", 
                actionResult.status, actionResult.message));
            
            result = enrich(actionResult, intent, startTime);
            
            // 4. Update Cache on Success (Self-Healing)
            if (result.success && match != null) {
                context.getFinder().saveMatchToCache(page, intent, match);
            }
            
            V3Logger.logStepExecution(stepText, intent, match, result);
            return result;
                
        } catch (Exception e) {
            V3Logger.error("EXCEPTION during step execution", e);
            V3Logger.trace("Exception Details", e.getClass().getName() + ": " + e.getMessage());
            result = enrich(ActionResult.failure("Execution failed: " + e.getMessage(), e), intent, startTime);
            V3Logger.logStepExecution(stepText, intent, match, result);
            return result;
        }
    }

    private ActionResult enrich(ActionResult result, StepIntent intent, long startTime) {
        return new ActionResult.Builder(result.success)
            .from(result)
            .action(intent != null ? intent.action : null)
            .target(intent != null ? intent.target : null)
            .durationMs(System.currentTimeMillis() - startTime)
            .build();
    }

    public List<ActionResult> executeScenario(Page page, List<String> steps, TestReporter reporter) {
        V3Logger.info("\nStarting scenario execution with " + steps.size() + " step(s)");
        
        // 1. Setup Global Dialog Handler
        setupGlobalDialogHandler(page);
        
        List<ActionResult> scenarioResults = new ArrayList<>();
        Page currentPage = page;
        
        int stepNumber = 0;
        for (String step : steps) {
            stepNumber++;
            V3Logger.info(String.format("\n>>> Step %d of %d >>>", stepNumber, steps.size()));
            
            // Re-detect the active page in case of window switches or popups
            V3Logger.debug("Detecting active page...");
            currentPage = getActivePage(currentPage);
            V3Logger.trace("Active Page URL", currentPage.url());
            
            ActionResult result = executeStep(currentPage, step);
            scenarioResults.add(result);
            if (reporter != null) {
                reporter.addResult(result);
            }
            
            // Stop on failure for sequential scenarios
            if (!result.success && result.status != ActionResult.ResultStatus.WARNING) {
                V3Logger.error("Stopping scenario execution due to failure");
                V3Logger.warn(String.format("Failed at step %d of %d", stepNumber, steps.size()));
                
                // Add SKIPPED status for remaining steps
                for (int i = stepNumber; i < steps.size(); i++) {
                    String skippedStep = steps.get(i);
                    ActionResult skippedResult = new ActionResult.Builder(false)
                        .status(ActionResult.ResultStatus.SKIPPED)
                        .message("Skipped due to previous failure")
                        .originalStep(skippedStep)
                        .build();
                    scenarioResults.add(skippedResult);
                    if (reporter != null) {
                        reporter.addResult(skippedResult);
                    }
                }
                break;
            }
        }
        
        V3Logger.info("\nScenario execution completed");
        return scenarioResults;
    }

    private void setupGlobalDialogHandler(Page page) {
        page.onDialog(dialog -> {
            String message = dialog.message();
            V3Logger.debug("BROWSER DIALOG: [" + dialog.type() + "] " + message);
            
            // Store message for later verification
            context.setLastDialogMessage(message);
            
            // Handle according to instruction from previous/current step
            String action = context.getNextDialogAction();
            if ("DISMISS".equalsIgnoreCase(action)) {
                V3Logger.trace("Dialog Handler", "Dismissing dialog...");
                dialog.dismiss();
            } else {
                V3Logger.trace("Dialog Handler", "Accepting dialog...");
                if (context.getNextPromptText() != null) {
                    dialog.accept(context.getNextPromptText());
                } else {
                    dialog.accept();
                }
            }
            
            // Reset for next dialog (unless explicitly set again)
            context.setNextDialogAction("ACCEPT");
            context.setNextPromptText(null);
        });
    }

    /**
     * Detects the currently focused page in the browser context.
     * Relies on PageTracker for accuracy since bringToFront() doesn't always 
     * immediately update browser focus state.
     */
    private Page getActivePage(Page defaultPage) {
        try {
            List<Page> pages = defaultPage.context().pages();
            V3Logger.trace("Window Count", String.valueOf(pages.size()));
            
            if (pages.size() <= 1) {
                // Even with one window, check if it was explicitly tracked (e.g., "switch to parent")
                Page trackedPage = PageTracker.getLastSwitchedPage();
                if (trackedPage != null && pages.contains(trackedPage)) {
                    try {
                        String url = trackedPage.url(); // Verify still accessible
                        V3Logger.trace("Active Page", "Using tracked page (even with one window): " + url);
                        return trackedPage;
                    } catch (Exception e) {
                        V3Logger.debug("Tracked page is no longer accessible: " + e.getMessage());
                    }
                }
                
                // No explicit tracking, use default and clear tracker
                V3Logger.trace("Active Page", "Using default page (only one window open)");
                PageTracker.clear();
                return defaultPage;
            }
            
            // Give a brief moment for page state to stabilize after window switches
            try { Thread.sleep(200); } catch (InterruptedException e) {}
            
            // TRUST PageTracker: Use the last explicitly switched page if it's still valid
            Page trackedPage = PageTracker.getLastSwitchedPage();
            if (trackedPage != null && pages.contains(trackedPage)) {
                try {
                    // Verify the page is still accessible
                    String url = trackedPage.url(); // This will throw if page is closed
                    
                    // Attempt to bring to front to sync browser visual state
                    // (This may not always work immediately, but we trust the tracked page)
                    trackedPage.bringToFront();
                    
                    V3Logger.info("Using tracked page: " + url);
                    return trackedPage;
                } catch (Exception e) {
                    // Page was closed or is inaccessible, clear it
                    V3Logger.debug("Tracked page is no longer accessible: " + e.getMessage());
                    PageTracker.clear();
                }
            }
            
            // Fallback: No tracked page or it's invalid - use the newest page
            Page lastPage = pages.get(pages.size() - 1);
            V3Logger.info("No tracked page, using last created page: " + lastPage.url());
            return lastPage;
        } catch (Exception e) {
            V3Logger.warn("Error detecting active page, using default: " + e.getMessage());
            return defaultPage;
        }
    }
}
