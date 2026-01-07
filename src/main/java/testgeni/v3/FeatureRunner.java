package testgeni.v3;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import testgeni.v3.orchestration.TestConfig;
import testgeni.v3.orchestration.TestReporter;
import testgeni.v3.orchestration.TestRunner;
import testgeni.v3.core.domain.ActionResult;
import testgeni.v3.util.FeatureFileReader;
import testgeni.v3.util.V3Logger;

import java.util.List;

/**
 * Main runner to execute feature files using TestGeni V3 framework.
 * 
 * Usage:
 *   java testgeni.v3.FeatureRunner [path/to/feature/file.feature]
 *   
 * If no path is provided, it defaults to ReproductionWindow.feature
 */
public class FeatureRunner {

    public static void main(String[] args) {
        String featurePath = "src/main/resources/features/ReproductionWindow.feature";
        if (args.length > 0) {
            featurePath = args[0];
        }

        V3Logger.info("==========================================================");
        V3Logger.info("       TestGeni V3 Feature Runner                        ");
        V3Logger.info("==========================================================");
        V3Logger.info("Feature: " + featurePath);

        try (Playwright playwright = Playwright.create()) {
            V3Logger.debug("Playwright instance created successfully");
            
            Browser browser = playwright.chromium().launch(new BrowserType.LaunchOptions()
                .setHeadless(false)
                .setSlowMo(500));  // Slow down for better visibility
            
            V3Logger.debug("Browser launched: Chromium (headless=false, slowMo=500ms)");
            
            Page page = browser.newPage();
            V3Logger.debug("New page created");
            
            // Initialize components
            TestConfig config = TestConfig.builder()
                .captureScreenshots(true)
                .build();
            
            V3Logger.debug("TestConfig initialized: captureScreenshots=true");
            
            TestRunner runner = new TestRunner(config);
            TestReporter reporter = new TestReporter();
            FeatureFileReader reader = new FeatureFileReader();

            V3Logger.debug("Test components initialized: TestRunner, TestReporter, FeatureFileReader");

            // Read steps
            List<String> steps = reader.readSteps(featurePath);
            V3Logger.info("Found " + steps.size() + " steps to execute");
            V3Logger.info("════════════════════════════════════════════════════════════");

            // Execute scenario
            V3Logger.debug("Starting scenario execution...");
            runner.executeScenario(page, steps, reporter);

            // Print final summary
            printSummary(reporter);
            
            // Keep browser open for 3 seconds to see final state
            V3Logger.debug("Keeping browser open for 3 seconds...");
            Thread.sleep(3000);
            
            browser.close();
            V3Logger.debug("Browser closed");

        } catch (Exception e) {
            V3Logger.error("CRITICAL ERROR: " + e.getMessage(), e);
            System.exit(1);
        }
    }

    private static void printSummary(TestReporter reporter) {
        V3Logger.info("");
        V3Logger.info("============================================================");
        V3Logger.info("TEST EXECUTION COMPLETE");
        V3Logger.info("============================================================");
        V3Logger.info("Total Steps: " + reporter.getTotalCount());
        V3Logger.info("Passed:      " + reporter.getSuccessCount());
        V3Logger.info("Failed:      " + reporter.getFailureCount());
        V3Logger.info("Skipped:     " + reporter.getSkippedCount());
        V3Logger.info("Duration:    " + reporter.getTotalDuration() + "ms");
        V3Logger.info("============================================================");
        V3Logger.info("");
        
        // Print detailed results
        reporter.getResults().forEach(result -> {
            String status = result.success ? "[PASS]" : "[FAIL]";
            if (result.status == ActionResult.ResultStatus.SKIPPED) {
                status = "[SKIP]";
            }
            V3Logger.info(status + " " + (result.action != null ? result.action : "NA") + " - " + (result.target != null ? result.target : result.originalStep));
            if (!result.success && result.message != null) {
                V3Logger.warn("   └─ Error: " + result.message);
            }
        });
    }
}
