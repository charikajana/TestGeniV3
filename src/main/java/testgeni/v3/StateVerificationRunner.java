package testgeni.v3;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import testgeni.v3.orchestration.TestConfig;
import testgeni.v3.orchestration.TestReporter;
import testgeni.v3.orchestration.TestRunner;
import testgeni.v3.util.FeatureFileReader;
import testgeni.v3.util.V3Logger;

import java.util.List;

/**
 * Runner for StateVerification.feature
 */
public class StateVerificationRunner {
    public static void main(String[] args) {
        String featurePath = "src/main/resources/features/StateVerification.feature";
        
        V3Logger.info("==========================================================");
        V3Logger.info("       TestGeni V3 - State Verification Test             ");
        V3Logger.info("==========================================================");
        V3Logger.info("Feature: " + featurePath);
        V3Logger.info("");

        try {
            // Just delegate to FeatureRunner with our feature path
            FeatureRunner.main(new String[]{featurePath});
        } catch (Exception e) {
            V3Logger.error("Fatal error during test execution", e);
            System.exit(1);
        }
    }
}
