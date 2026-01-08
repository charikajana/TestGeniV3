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

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Regression Runner for TestGeni V3.
 * Executes a set of core feature files to ensure stability and catch regressions.
 */
public class V3RegressionRunner {

    // List of core features that define the "Gold Standard" of framework stability
    private static final List<String> GOLD_FEATURES = Arrays.asList(
        "src/test/resources/Alerts.feature",
        "src/test/resources/AutoComplete.feature",
        "src/test/resources/ReproductionWindow.feature",
        "src/test/resources/Buttons.feature",
        "src/test/resources/TextBox.feature",
        "src/test/resources/Navigation.feature",
        "src/test/resources/StateVerification.feature",
        "src/test/resources/RadioButton.feature"
    );

    public static void main(String[] args) {
        List<String> featuresToRun = new ArrayList<>();
        
        if (args.length > 0) {
            // If directory provided, scan for all features
            File path = new File(args[0]);
            if (path.isDirectory()) {
                File[] files = path.listFiles((dir, name) -> name.endsWith(".feature"));
                if (files != null) {
                    featuresToRun = Arrays.stream(files)
                        .map(File::getPath)
                        .collect(Collectors.toList());
                }
            } else {
                featuresToRun.add(args[0]);
            }
        } else {
            // Default to Gold Standard
            featuresToRun = GOLD_FEATURES;
        }

        V3Logger.info("==========================================================");
        V3Logger.info("       TestGeni V3 REGRESSION RUNNER                     ");
        V3Logger.info("==========================================================");
        V3Logger.info("Targeting " + featuresToRun.size() + " feature(s)");

        int totalFailedScenarios = 0;
        TestReporter globalReporter = new TestReporter();

        try (Playwright playwright = Playwright.create()) {
            Browser browser = playwright.chromium().launch(new BrowserType.LaunchOptions()
                .setHeadless(false) // Changed to headed mode for visibility
                .setSlowMo(100)); // Added a small delay for visibility
            
            TestConfig config = TestConfig.builder()
                .captureScreenshots(true)
                .build();
            
            FeatureFileReader reader = new FeatureFileReader();
            
            for (String featurePath : featuresToRun) {
                V3Logger.info("\n>>> RUNNING FEATURE: " + featurePath);
                
                Page page = browser.newPage();
                TestRunner runner = new TestRunner(config);
                TestReporter featureReporter = new TestReporter();
                
                try {
                    List<String> steps = reader.readSteps(featurePath);
                    runner.executeScenario(page, steps, featureReporter);
                    
                    // Aggregate results
                    if (featureReporter.getFailureCount() > 0) {
                        totalFailedScenarios++;
                    }
                    
                    // Add to global stats (manual aggregation as TestReporter is usually per scenario)
                    featureReporter.getResults().forEach(r -> {
                        // We use a dummy reporter or just track manually for the summary
                    });
                    
                    printFeatureSummary(featurePath, featureReporter);
                    
                } catch (Exception e) {
                    V3Logger.error("Error running feature " + featurePath + ": " + e.getMessage());
                    totalFailedScenarios++;
                } finally {
                    page.close();
                }
            }
            
            browser.close();
            
            V3Logger.info("\n============================================================");
            V3Logger.info("REGRESSION SUITE COMPLETE");
            V3Logger.info("============================================================");
            V3Logger.info("Total Features Run:  " + featuresToRun.size());
            V3Logger.info("Features Passed:     " + (featuresToRun.size() - totalFailedScenarios));
            V3Logger.info("Features Failed:     " + totalFailedScenarios);
            V3Logger.info("============================================================");

            if (totalFailedScenarios > 0) {
                V3Logger.error("REGRESSION FAILED!");
                System.exit(1);
            } else {
                V3Logger.success("REGRESSION PASSED!");
                System.exit(0);
            }

        } catch (Exception e) {
            V3Logger.error("CRITICAL ERROR: " + e.getMessage(), e);
            System.exit(1);
        }
    }

    private static void printFeatureSummary(String path, TestReporter reporter) {
        String status = reporter.getFailureCount() == 0 ? "[PASS]" : "[FAIL]";
        V3Logger.info(String.format("%s %s (%d steps, %d failed)", 
            status, path, reporter.getTotalCount(), reporter.getFailureCount()));
    }
}
