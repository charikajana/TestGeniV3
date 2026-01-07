package testgeni.v3.orchestration;

/**
 * Configuration for the V3 Test Execution Engine.
 * Manually implemented builder to avoid dependency issues.
 */
public class TestConfig {

    private String browserType = "chromium";
    private boolean headless = false;
    private int defaultTimeoutMs = 30000;
    private int elementDiscoveryTimeoutMs = 5000;
    private double minConfidenceThreshold = 0.6;
    private boolean captureScreenshots = true;
    private boolean slowMo = false;
    private int slowMoMs = 0;
    private String reportDirectory = "target/testgeni-reports";

    private TestConfig() {}

    public static Builder builder() {
        return new Builder();
    }

    public static TestConfig defaultConfig() {
        return new TestConfig();
    }

    // Getters
    public String getBrowserType() { return browserType; }
    public boolean isHeadless() { return headless; }
    public int getDefaultTimeoutMs() { return defaultTimeoutMs; }
    public int getElementDiscoveryTimeoutMs() { return elementDiscoveryTimeoutMs; }
    public double getMinConfidenceThreshold() { return minConfidenceThreshold; }
    public boolean isCaptureScreenshots() { return captureScreenshots; }
    public boolean isSlowMo() { return slowMo; }
    public int getSlowMoMs() { return slowMoMs; }
    public String getReportDirectory() { return reportDirectory; }

    public static class Builder {
        private final TestConfig config = new TestConfig();

        public Builder browserType(String val) { config.browserType = val; return this; }
        public Builder headless(boolean val) { config.headless = val; return this; }
        public Builder defaultTimeoutMs(int val) { config.defaultTimeoutMs = val; return this; }
        public Builder captureScreenshots(boolean val) { config.captureScreenshots = val; return this; }
        public Builder reportDirectory(String val) { config.reportDirectory = val; return this; }
        // Add more as needed...

        public TestConfig build() {
            return config;
        }
    }
}
