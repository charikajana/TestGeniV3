package testgeni.v3.core.domain;

import java.io.Serializable;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.*;

/**
 * Immutable data object representing the result of executing an action.
 * 
 * DESIGN PHILOSOPHY:
 * - Complete record of what happened during action execution
 * - Captures success, failure, warnings, performance, evidence
 * - Rich debugging information for troubleshooting
 * - Immutable by design - represents a fact that occurred
 * - Extensible via metadata for future needs
 * 
 * This class is designed to NEVER need changes. Any new data
 * should be added to the metadata map or evidence map.
 * 
 * @author TestGeni v3
 * @version 3.0.0
 * @since 2026-01-06
 */
public class ActionResult implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    // ========================================
    // CORE RESULT
    // ========================================
    
    /** Was the action successful? */
    public final boolean success;
    
    /** Result status */
    public final ResultStatus status;
    
    /** Human-readable message */
    public final String message;
    
    /** Detailed description (for logging/reporting) */
    public final String detailedMessage;
    
    /** Exception if action failed (null if success) */
    public final Exception exception;
    
    /** Stack trace string (for serialization) */
    public final String stackTrace;
    
    /** Error code (for programmatic error handling) */
    public final String errorCode;
    
    /** Error category (NETWORK, TIMEOUT, ELEMENT_NOT_FOUND, etc.) */
    public final ErrorCategory errorCategory;
    
    // ========================================
    // VERIFICATION (For VERIFY actions)
    // ========================================
    
    /** Expected value (for verifications) */
    public final String expectedValue;
    
    /** Actual value found (for verifications) */
    public final String actualValue;
    
    /** Difference/delta between expected and actual */
    public final String difference;
    
    /** Verification passed? (for verify actions) */
    public final Boolean verificationPassed;
    
    /** Comparison type used (equals, contains, matches, etc.) */
    public final String comparisonType;
    
    /** Case sensitive comparison? */
    public final boolean caseSensitive;
    
    // ========================================
    // EXECUTION CONTEXT
    // ========================================
    
    /** The action that was executed */
    public final ActionType action;
    
    /** The target element description */
    public final String target;
    
    /** The value used (if any) */
    public final String value;
    
    /** Step number in scenario */
    public final Integer stepNumber;
    
    /** Original Gherkin step */
    public final String originalStep;
    
    /** Feature file name */
    public final String featureFile;
    
    /** Scenario name */
    public final String scenarioName;
    
    // ========================================
    // TIMING / PERFORMANCE
    // ========================================
    
    /** Time taken to execute action (ms) */
    public final long durationMs;
    
    /** Timestamp when action started */
    public final long startTimestamp;
    
    /** Timestamp when action completed */
    public final long endTimestamp;
    
    /** Time spent waiting for element (ms) */
    public final long waitTimeMs;
    
    /** Time spent in action execution (ms) */
    public final long actionTimeMs;
    
    /** Timeout used (ms) */
    public final int timeoutMs;
    
    /** Was timeout exceeded? */
    public final boolean timedOut;
    
    // ========================================
    // RETRY INFORMATION
    // ========================================
    
    /** Number of retry attempts made */
    public final int retryAttempts;
    
    /** Maximum retries allowed */
    public final int maxRetries;
    
    /** Retry reasons (why each retry was needed) */
    public final List<String> retryReasons;
    
    /** Did action succeed after retry? */
    public final boolean succeededAfterRetry;
    
    /** Which attempt succeeded (0 = first try, 1 = first retry, etc.) */
    public final Integer successfulAttempt;
    
    // ========================================
    // ELEMENT INFORMATION
    // ========================================
    
    /** Element that was interacted with */
    public final ElementMatch elementMatch;
    
    /** Element locator used */
    public final String elementLocator;
    
    /** Element confidence score */
    public final Double elementConfidence;
    
    /** Element match strategy */
    public final ElementMatch.MatchStrategy matchStrategy;
    
    /** Was element found? */
    public final boolean elementFound;
    
    /** Time to find element (ms) */
    public final long elementFindTimeMs;
    
    // ========================================
    // STATE CHANGES
    // ========================================
    
    /** Element state before action */
    public final Map<String, Object> beforeState;
    
    /** Element state after action */
    public final Map<String, Object> afterState;
    
    /** Page URL before action */
    public final String urlBefore;
    
    /** Page URL after action */
    public final String urlAfter;
    
    /** Did page navigate? */
    public final boolean pageNavigated;
    
    /** DOM changes detected */
    public final List<String> domChanges;
    
    /** Network requests triggered */
    public final List<String> networkRequests;
    
    /** Console messages during action */
    public final List<String> consoleMessages;
    
    // ========================================
    // EVIDENCE / ARTIFACTS
    // ========================================
    
    /** Screenshot path (before action) */
    public final String screenshotBefore;
    
    /** Screenshot path (after action) */
    public final String screenshotAfter;
    
    /** Screenshot on failure path */
    public final String screenshotOnFailure;
    
    /** Video recording path */
    public final String videoPath;
    
    /** HAR file path (network log) */
    public final String harFilePath;
    
    /** Trace file path (Playwright trace) */
    public final String traceFilePath;
    
    /** Evidence files (additional artifacts) */
    public final List<String> evidenceFiles;
    
    // ========================================
    // WARNINGS / SOFT FAILURES
    // ========================================
    
    /** Warnings collected during execution */
    public final List<String> warnings;
    
    /** Soft assertions that failed (but didn't stop execution) */
    public final List<String> softAssertionFailures;
    
    /** Performance warnings (slow actions, etc.) */
    public final List<String> performanceWarnings;
    
    /** Accessibility warnings */
    public final List<String> accessibilityWarnings;
    
    // ========================================
    // RECOVERY / HEALING
    // ========================================
    
    /** Was self-healing applied? */
    public final boolean selfHealingApplied;
    
    /** Self-healing actions taken */
    public final List<String> healingActions;
    
    /** Original locator (before healing) */
    public final String originalLocator;
    
    /** New locator (after healing) */
    public final String healedLocator;
    
    /** Recovery strategy used */
    public final String recoveryStrategy;
    
    // ========================================
    // DATA EXTRACTION
    // ========================================
    
    /** Data extracted from page */
    public final Map<String, String> extractedData;
    
    /** Variable assignments (name -> value) */
    public final Map<String, String> variableAssignments;
    
    /** List items extracted (for list operations) */
    public final List<String> listItems;
    
    /** Table data extracted (for table operations) */
    public final List<Map<String, String>> tableData;
    
    // ========================================
    // BROWSER CONTEXT
    // ========================================
    
    /** Browser type (chromium, firefox, webkit) */
    public final String browserType;
    
    /** Browser version */
    public final String browserVersion;
    
    /** Viewport size */
    public final String viewportSize;
    
    /** Current frame path (if in iframe) */
    public final String framePath;
    
    /** Current page title */
    public final String pageTitle;
    
    /** Current page URL */
    public final String currentUrl;
    
    // ========================================
    // REPORTING METADATA
    // ========================================
    
    /** Report category (for grouping in reports) */
    public final String reportCategory;
    
    /** Tags for filtering/categorization */
    public final List<String> tags;
    
    /** Custom properties for reporting */
    public final Map<String, String> customProperties;
    
    /** Severity level (CRITICAL, HIGH, MEDIUM, LOW, INFO) */
    public final Severity severity;
    
    /** Is this a known issue? */
    public final boolean knownIssue;
    
    /** Issue tracker ID (e.g., JIRA-123) */
    public final String issueTrackerId;
    
    // ========================================
    // EXTENSIBILITY
    // ========================================
    
    /** Generic metadata map (for future extensibility) */
    public final Map<String, Object> metadata;
    
    /** Evidence map (screenshots, videos, logs, etc.) */
    public final Map<String, String> evidence;
    
    /**
     * Full constructor - ONLY use via Builder
     */
    private ActionResult(Builder builder) {
        // Core result
        this.success = builder.success;
        this.status = builder.status;
        this.message = builder.message;
        this.detailedMessage = builder.detailedMessage;
        this.exception = builder.exception;
        this.stackTrace = builder.exception != null ? getStackTraceString(builder.exception) : null;
        this.errorCode = builder.errorCode;
        this.errorCategory = builder.errorCategory;
        
        // Verification
        this.expectedValue = builder.expectedValue;
        this.actualValue = builder.actualValue;
        this.difference = builder.difference;
        this.verificationPassed = builder.verificationPassed;
        this.comparisonType = builder.comparisonType;
        this.caseSensitive = builder.caseSensitive;
        
        // Execution context
        this.action = builder.action;
        this.target = builder.target;
        this.value = builder.value;
        this.stepNumber = builder.stepNumber;
        this.originalStep = builder.originalStep;
        this.featureFile = builder.featureFile;
        this.scenarioName = builder.scenarioName;
        
        // Timing
        this.durationMs = builder.durationMs;
        this.startTimestamp = builder.startTimestamp;
        this.endTimestamp = builder.endTimestamp != 0 ? builder.endTimestamp : 
                            (builder.startTimestamp + builder.durationMs);
        this.waitTimeMs = builder.waitTimeMs;
        this.actionTimeMs = builder.actionTimeMs;
        this.timeoutMs = builder.timeoutMs;
        this.timedOut = builder.timedOut;
        
        // Retry
        this.retryAttempts = builder.retryAttempts;
        this.maxRetries = builder.maxRetries;
        this.retryReasons = Collections.unmodifiableList(new ArrayList<>(builder.retryReasons));
        this.succeededAfterRetry = builder.retryAttempts > 0 && builder.success;
        this.successfulAttempt = builder.successfulAttempt;
        
        // Element
        this.elementMatch = builder.elementMatch;
        this.elementLocator = builder.elementLocator;
        this.elementConfidence = builder.elementConfidence;
        this.matchStrategy = builder.matchStrategy;
        this.elementFound = builder.elementFound;
        this.elementFindTimeMs = builder.elementFindTimeMs;
        
        // State changes
        this.beforeState = Collections.unmodifiableMap(new HashMap<>(builder.beforeState));
        this.afterState = Collections.unmodifiableMap(new HashMap<>(builder.afterState));
        this.urlBefore = builder.urlBefore;
        this.urlAfter = builder.urlAfter;
        this.pageNavigated = builder.urlBefore != null && !builder.urlBefore.equals(builder.urlAfter);
        this.domChanges = Collections.unmodifiableList(new ArrayList<>(builder.domChanges));
        this.networkRequests = Collections.unmodifiableList(new ArrayList<>(builder.networkRequests));
        this.consoleMessages = Collections.unmodifiableList(new ArrayList<>(builder.consoleMessages));
        
        // Evidence
        this.screenshotBefore = builder.screenshotBefore;
        this.screenshotAfter = builder.screenshotAfter;
        this.screenshotOnFailure = builder.screenshotOnFailure;
        this.videoPath = builder.videoPath;
        this.harFilePath = builder.harFilePath;
        this.traceFilePath = builder.traceFilePath;
        this.evidenceFiles = Collections.unmodifiableList(new ArrayList<>(builder.evidenceFiles));
        
        // Warnings
        this.warnings = Collections.unmodifiableList(new ArrayList<>(builder.warnings));
        this.softAssertionFailures = Collections.unmodifiableList(new ArrayList<>(builder.softAssertionFailures));
        this.performanceWarnings = Collections.unmodifiableList(new ArrayList<>(builder.performanceWarnings));
        this.accessibilityWarnings = Collections.unmodifiableList(new ArrayList<>(builder.accessibilityWarnings));
        
        // Recovery
        this.selfHealingApplied = builder.selfHealingApplied;
        this.healingActions = Collections.unmodifiableList(new ArrayList<>(builder.healingActions));
        this.originalLocator = builder.originalLocator;
        this.healedLocator = builder.healedLocator;
        this.recoveryStrategy = builder.recoveryStrategy;
        
        // Data extraction
        this.extractedData = Collections.unmodifiableMap(new HashMap<>(builder.extractedData));
        this.variableAssignments = Collections.unmodifiableMap(new HashMap<>(builder.variableAssignments));
        this.listItems = Collections.unmodifiableList(new ArrayList<>(builder.listItems));
        this.tableData = Collections.unmodifiableList(new ArrayList<>(builder.tableData));
        
        // Browser context
        this.browserType = builder.browserType;
        this.browserVersion = builder.browserVersion;
        this.viewportSize = builder.viewportSize;
        this.framePath = builder.framePath;
        this.pageTitle = builder.pageTitle;
        this.currentUrl = builder.currentUrl;
        
        // Reporting
        this.reportCategory = builder.reportCategory;
        this.tags = Collections.unmodifiableList(new ArrayList<>(builder.tags));
        this.customProperties = Collections.unmodifiableMap(new HashMap<>(builder.customProperties));
        this.severity = builder.severity;
        this.knownIssue = builder.knownIssue;
        this.issueTrackerId = builder.issueTrackerId;
        
        // Extensibility
        this.metadata = Collections.unmodifiableMap(new HashMap<>(builder.metadata));
        this.evidence = Collections.unmodifiableMap(new HashMap<>(builder.evidence));
    }
    
    // ========================================
    // STATIC FACTORY METHODS
    // ========================================
    
    /**
     * Create a successful result.
     */
    public static ActionResult success(String message) {
        return new Builder(true).message(message).build();
    }
    
    /**
     * Create a successful result with target.
     */
    public static ActionResult success(String message, String target) {
        return new Builder(true).message(message).target(target).build();
    }
    
    /**
     * Create a failed result.
     */
    public static ActionResult failure(String message) {
        return new Builder(false).message(message).build();
    }
    
    /**
     * Create a failed result with exception.
     */
    public static ActionResult failure(String message, Exception exception) {
        return new Builder(false).message(message).exception(exception).build();
    }
    
    /**
     * Create a verification result.
     */
    public static ActionResult verification(boolean passed, String expected, String actual) {
        return new Builder(passed)
            .message(passed ? "Verification passed" : "Verification failed")
            .verificationPassed(passed)
            .expectedValue(expected)
            .actualValue(actual)
            .build();
    }
    
    /**
     * Create a timeout result.
     */
    public static ActionResult timeout(String target, int timeoutMs) {
        return new Builder(false)
            .status(ResultStatus.TIMEOUT)
            .message(String.format("Timed out after %dms waiting for '%s'", timeoutMs, target))
            .target(target)
            .timeoutMs(timeoutMs)
            .timedOut(true)
            .errorCategory(ErrorCategory.TIMEOUT)
            .build();
    }
    
    /**
     * Create an element not found result.
     */
    public static ActionResult elementNotFound(String target) {
        return new Builder(false)
            .status(ResultStatus.ELEMENT_NOT_FOUND)
            .message(String.format("Element not found: '%s'", target))
            .target(target)
            .elementFound(false)
            .errorCategory(ErrorCategory.ELEMENT_NOT_FOUND)
            .build();
    }
    
    // ========================================
    // HELPER METHODS
    // ========================================
    
    /**
     * Is this a verification result?
     */
    public boolean isVerification() {
        return verificationPassed != null;
    }
    
    /**
     * Has warnings?
     */
    public boolean hasWarnings() {
        return !warnings.isEmpty() || !softAssertionFailures.isEmpty() ||
               !performanceWarnings.isEmpty() || !accessibilityWarnings.isEmpty();
    }
    
    /**
     * Get all warnings combined.
     */
    public List<String> getAllWarnings() {
        List<String> all = new ArrayList<>();
        all.addAll(warnings);
        all.addAll(softAssertionFailures);
        all.addAll(performanceWarnings);
        all.addAll(accessibilityWarnings);
        return all;
    }
    
    /**
     * Has evidence (screenshots, videos, etc.)?
     */
    public boolean hasEvidence() {
        return screenshotBefore != null || screenshotAfter != null ||
               screenshotOnFailure != null || videoPath != null ||
               harFilePath != null || traceFilePath != null ||
               !evidenceFiles.isEmpty() || !evidence.isEmpty();
    }
    
    /**
     * Get all evidence paths.
     */
    public Map<String, String> getAllEvidence() {
        Map<String, String> all = new HashMap<>(evidence);
        if (screenshotBefore != null) all.put("screenshot_before", screenshotBefore);
        if (screenshotAfter != null) all.put("screenshot_after", screenshotAfter);
        if (screenshotOnFailure != null) all.put("screenshot_failure", screenshotOnFailure);
        if (videoPath != null) all.put("video", videoPath);
        if (harFilePath != null) all.put("har", harFilePath);
        if (traceFilePath != null) all.put("trace", traceFilePath);
        for (int i = 0; i < evidenceFiles.size(); i++) {
            all.put("evidence_" + i, evidenceFiles.get(i));
        }
        return all;
    }
    
    /**
     * Was action slow (took longer than expected)?
     */
    public boolean wasSlow() {
        return action != null && durationMs > (action.getDefaultTimeoutMs() * 0.75);
    }
    
    /**
     * Get metadata value.
     */
    public Object getMetadata(String key) {
        return metadata.get(key);
    }
    
    /**
     * Get metadata value with default.
     */
    public Object getMetadata(String key, Object defaultValue) {
        return metadata.getOrDefault(key, defaultValue);
    }
    
    /**
     * Get human-readable summary.
     */
    public String getSummary() {
        StringBuilder summary = new StringBuilder();
        summary.append(success ? "✓ SUCCESS" : "✗ FAILURE");
        summary.append(": ").append(message);
        
        if (durationMs > 0) {
            summary.append(String.format(" (%dms)", durationMs));
        }
        
        if (retryAttempts > 0) {
            summary.append(String.format(" [%d retries]", retryAttempts));
        }
        
        if (hasWarnings()) {
            summary.append(String.format(" [%d warnings]", getAllWarnings().size()));
        }
        
        return summary.toString();
    }
    
    /**
     * Get detailed report.
     */
    public String getDetailedReport() {
        StringBuilder report = new StringBuilder();
        report.append("=== Action Result Report ===\n");
        report.append(String.format("Status: %s\n", success ? "SUCCESS" : "FAILURE"));
        report.append(String.format("Message: %s\n", message));
        
        if (action != null && target != null) {
            report.append(String.format("Action: %s '%s'\n", action, target));
        }
        
        if (originalStep != null) {
            report.append(String.format("Step: %s\n", originalStep));
        }
        
        report.append(String.format("Duration: %dms\n", durationMs));
        
        if (isVerification()) {
            report.append(String.format("Verification: %s\n", verificationPassed ? "PASSED" : "FAILED"));
            report.append(String.format("Expected: %s\n", expectedValue));
            report.append(String.format("Actual: %s\n", actualValue));
        }
        
        if (elementMatch != null) {
            report.append(String.format("Element: %s (%.0f%% confidence)\n", 
                elementLocator, elementConfidence * 100));
        }
        
        if (retryAttempts > 0) {
            report.append(String.format("Retries: %d/%d\n", retryAttempts, maxRetries));
        }
        
        if (hasWarnings()) {
            report.append(String.format("Warnings: %d\n", getAllWarnings().size()));
            getAllWarnings().forEach(w -> report.append("  - ").append(w).append("\n"));
        }
        
        if (exception != null) {
            report.append(String.format("Exception: %s\n", exception.getMessage()));
        }
        
        if (hasEvidence()) {
            report.append("Evidence:\n");
            getAllEvidence().forEach((k, v) -> 
                report.append(String.format("  %s: %s\n", k, v)));
        }
        
        return report.toString();
    }
    
    @Override
    public String toString() {
        return getSummary();
    }
    
    // ========================================
    // HELPER METHODS (Private)
    // ========================================
    
    private static String getStackTraceString(Exception e) {
        if (e == null) return null;
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        return sw.toString();
    }
    
    // ========================================
    // NESTED ENUMS
    // ========================================
    
    /**
     * Result status enum.
     */
    public enum ResultStatus {
        SUCCESS,                // Action completed successfully
        FAILURE,                // Action failed
        TIMEOUT,                // Action timed out
        ELEMENT_NOT_FOUND,      // Element could not be found
        ELEMENT_NOT_VISIBLE,    // Element found but not visible
        ELEMENT_NOT_ENABLED,    // Element found but not enabled
        VERIFICATION_FAILED,    // Verification assertion failed
        EXCEPTION,              // Exception was thrown
        SKIPPED,                // Action was skipped
        WARNING                 // Action completed with warnings
    }
    
    /**
     * Error category enum.
     */
    public enum ErrorCategory {
        NONE,                   // No error
        ELEMENT_NOT_FOUND,      // Element location errors
        TIMEOUT,                // Timeout errors
        NETWORK,                // Network/HTTP errors
        JAVASCRIPT,             // JavaScript execution errors
        FRAME,                  // Frame/iframe errors
        VALIDATION,             // Validation/assertion errors
        CONFIGURATION,          // Configuration errors
        EXCEPTION,              // Exception was thrown
        UNKNOWN                 // Unknown category
    }
    
    /**
     * Severity enum.
     */
    public enum Severity {
        CRITICAL,   // Critical failure
        HIGH,       // High severity
        MEDIUM,     // Medium severity
        LOW,        // Low severity
        INFO        // Informational only
    }
    
    // ========================================
    // BUILDER CLASS
    // ========================================
    
    /**
     * Builder for ActionResult (recommended construction method).
     */
    public static class Builder {
        // Required
        private final boolean success;
        
        // Optional (with defaults)
        private ResultStatus status;
        private String message = "";
        private String detailedMessage;
        private Exception exception;
        private String errorCode;
        private ErrorCategory errorCategory = ErrorCategory.NONE;
        private String expectedValue;
        private String actualValue;
        private String difference;
        private Boolean verificationPassed;
        private String comparisonType;
        private boolean caseSensitive;
        private ActionType action;
        private String target;
        private String value;
        private Integer stepNumber;
        private String originalStep;
        private String featureFile;
        private String scenarioName;
        private long durationMs;
        private long startTimestamp = System.currentTimeMillis();
        private long endTimestamp;
        private long waitTimeMs;
        private long actionTimeMs;
        private int timeoutMs;
        private boolean timedOut;
        private int retryAttempts;
        private int maxRetries;
        private List<String> retryReasons = new ArrayList<>();
        private Integer successfulAttempt;
        private ElementMatch elementMatch;
        private String elementLocator;
        private Double elementConfidence;
        private ElementMatch.MatchStrategy matchStrategy;
        private boolean elementFound = true;
        private long elementFindTimeMs;
        private Map<String, Object> beforeState = new HashMap<>();
        private Map<String, Object> afterState = new HashMap<>();
        private String urlBefore;
        private String urlAfter;
        private List<String> domChanges = new ArrayList<>();
        private List<String> networkRequests = new ArrayList<>();
        private List<String> consoleMessages = new ArrayList<>();
        private String screenshotBefore;
        private String screenshotAfter;
        private String screenshotOnFailure;
        private String videoPath;
        private String harFilePath;
        private String traceFilePath;
        private List<String> evidenceFiles = new ArrayList<>();
        private List<String> warnings = new ArrayList<>();
        private List<String> softAssertionFailures = new ArrayList<>();
        private List<String> performanceWarnings = new ArrayList<>();
        private List<String> accessibilityWarnings = new ArrayList<>();
        private boolean selfHealingApplied;
        private List<String> healingActions = new ArrayList<>();
        private String originalLocator;
        private String healedLocator;
        private String recoveryStrategy;
        private Map<String, String> extractedData = new HashMap<>();
        private Map<String, String> variableAssignments = new HashMap<>();
        private List<String> listItems = new ArrayList<>();
        private List<Map<String, String>> tableData = new ArrayList<>();
        private String browserType;
        private String browserVersion;
        private String viewportSize;
        private String framePath;
        private String pageTitle;
        private String currentUrl;
        private String reportCategory;
        private List<String> tags = new ArrayList<>();
        private Map<String, String> customProperties = new HashMap<>();
        private Severity severity;
        private boolean knownIssue;
        private String issueTrackerId;
        private Map<String, Object> metadata = new HashMap<>();
        private Map<String, String> evidence = new HashMap<>();
        
        public Builder(boolean success) {
            this.success = success;
            this.status = success ? ResultStatus.SUCCESS : ResultStatus.FAILURE;
            this.severity = success ? Severity.INFO : Severity.MEDIUM;
        }

        /**
         * Copy all fields from an existing result.
         */
        public Builder from(ActionResult other) {
            if (other == null) return this;
            this.status = other.status;
            this.message = other.message;
            this.detailedMessage = other.detailedMessage;
            this.exception = other.exception;
            this.errorCode = other.errorCode;
            this.errorCategory = other.errorCategory;
            this.expectedValue = other.expectedValue;
            this.actualValue = other.actualValue;
            this.difference = other.difference;
            this.verificationPassed = other.verificationPassed;
            this.comparisonType = other.comparisonType;
            this.caseSensitive = other.caseSensitive;
            this.action = other.action;
            this.target = other.target;
            this.value = other.value;
            this.stepNumber = other.stepNumber;
            this.originalStep = other.originalStep;
            this.featureFile = other.featureFile;
            this.scenarioName = other.scenarioName;
            this.durationMs = other.durationMs;
            this.startTimestamp = other.startTimestamp;
            this.endTimestamp = other.endTimestamp;
            this.waitTimeMs = other.waitTimeMs;
            this.actionTimeMs = other.actionTimeMs;
            this.timeoutMs = other.timeoutMs;
            this.timedOut = other.timedOut;
            this.retryAttempts = other.retryAttempts;
            this.maxRetries = other.maxRetries;
            this.elementMatch = other.elementMatch;
            this.elementLocator = other.elementLocator;
            this.elementConfidence = other.elementConfidence;
            this.matchStrategy = other.matchStrategy;
            this.elementFound = other.elementFound;
            this.elementFindTimeMs = other.elementFindTimeMs;
            this.beforeState.putAll(other.beforeState);
            this.afterState.putAll(other.afterState);
            this.urlBefore = other.urlBefore;
            this.urlAfter = other.urlAfter;
            this.screenshotBefore = other.screenshotBefore;
            this.screenshotAfter = other.screenshotAfter;
            this.screenshotOnFailure = other.screenshotOnFailure;
            this.videoPath = other.videoPath;
            this.harFilePath = other.harFilePath;
            this.traceFilePath = other.traceFilePath;
            this.browserType = other.browserType;
            this.browserVersion = other.browserVersion;
            this.viewportSize = other.viewportSize;
            this.framePath = other.framePath;
            this.pageTitle = other.pageTitle;
            this.currentUrl = other.currentUrl;
            this.reportCategory = other.reportCategory;
            this.severity = other.severity;
            this.knownIssue = other.knownIssue;
            this.issueTrackerId = other.issueTrackerId;
            return this;
        }
        
        // Fluent setters (100+ methods)
        public Builder status(ResultStatus val) { this.status = val; return this; }
        public Builder message(String val) { this.message = val; return this; }
        public Builder detailedMessage(String val) { this.detailedMessage = val; return this; }
        public Builder exception(Exception val) { this.exception = val; if (val != null) this.errorCategory = ErrorCategory.EXCEPTION; return this; }
        public Builder errorCode(String val) { this.errorCode = val; return this; }
        public Builder errorCategory(ErrorCategory val) { this.errorCategory = val; return this; }
        public Builder expectedValue(String val) { this.expectedValue = val; return this; }
        public Builder actualValue(String val) { this.actualValue = val; return this; }
        public Builder difference(String val) { this.difference = val; return this; }
        public Builder verificationPassed(Boolean val) { this.verificationPassed = val; return this; }
        public Builder comparisonType(String val) { this.comparisonType = val; return this; }
        public Builder caseSensitive(boolean val) { this.caseSensitive = val; return this; }
        public Builder action(ActionType val) { this.action = val; return this; }
        public Builder target(String val) { this.target = val; return this; }
        public Builder value(String val) { this.value = val; return this; }
        public Builder stepNumber(Integer val) { this.stepNumber = val; return this; }
        public Builder originalStep(String val) { this.originalStep = val; return this; }
        public Builder featureFile(String val) { this.featureFile = val; return this; }
        public Builder scenarioName(String val) { this.scenarioName = val; return this; }
        public Builder durationMs(long val) { this.durationMs = val; return this; }
        public Builder startTimestamp(long val) { this.startTimestamp = val; return this; }
        public Builder endTimestamp(long val) { this.endTimestamp = val; return this; }
        public Builder waitTimeMs(long val) { this.waitTimeMs = val; return this; }
        public Builder actionTimeMs(long val) { this.actionTimeMs = val; return this; }
        public Builder timeoutMs(int val) { this.timeoutMs = val; return this; }
        public Builder timedOut(boolean val) { this.timedOut = val; return this; }
        public Builder retryAttempts(int val) { this.retryAttempts = val; return this; }
        public Builder maxRetries(int val) { this.maxRetries = val; return this; }
        public Builder retryReasons(List<String> val) { if (val != null) this.retryReasons.addAll(val); return this; }
        public Builder addRetryReason(String val) { this.retryReasons.add(val); return this; }
        public Builder successfulAttempt(Integer val) { this.successfulAttempt = val; return this; }
        public Builder elementMatch(ElementMatch val) { 
            this.elementMatch = val; 
            if (val != null) {
                this.elementLocator = val.locatorString;
                this.elementConfidence = val.confidence;
                this.matchStrategy = val.strategy;
            }
            return this; 
        }
        public Builder elementLocator(String val) { this.elementLocator = val; return this; }
        public Builder elementConfidence(Double val) { this.elementConfidence = val; return this; }
        public Builder matchStrategy(ElementMatch.MatchStrategy val) { this.matchStrategy = val; return this; }
        public Builder elementFound(boolean val) { this.elementFound = val; return this; }
        public Builder elementFindTimeMs(long val) { this.elementFindTimeMs = val; return this; }
        public Builder beforeState(Map<String, Object> val) { if (val != null) this.beforeState.putAll(val); return this; }
        public Builder afterState(Map<String, Object> val) { if (val != null) this.afterState.putAll(val); return this; }
        public Builder urlBefore(String val) { this.urlBefore = val; return this; }
        public Builder urlAfter(String val) { this.urlAfter = val; return this; }
        public Builder addDomChange(String val) { this.domChanges.add(val); return this; }
        public Builder addNetworkRequest(String val) { this.networkRequests.add(val); return this; }
        public Builder addConsoleMessage(String val) { this.consoleMessages.add(val); return this; }
        public Builder screenshotBefore(String val) { this.screenshotBefore = val; return this; }
        public Builder screenshotAfter(String val) { this.screenshotAfter = val; return this; }
        public Builder screenshotOnFailure(String val) { this.screenshotOnFailure = val; return this; }
        public Builder videoPath(String val) { this.videoPath = val; return this; }
        public Builder harFilePath(String val) { this.harFilePath = val; return this; }
        public Builder traceFilePath(String val) { this.traceFilePath = val; return this; }
        public Builder addEvidenceFile(String val) { this.evidenceFiles.add(val); return this; }
        public Builder addWarning(String val) { this.warnings.add(val); return this; }
        public Builder addSoftAssertionFailure(String val) { this.softAssertionFailures.add(val); return this; }
        public Builder addPerformanceWarning(String val) { this.performanceWarnings.add(val); return this; }
        public Builder addAccessibilityWarning(String val) { this.accessibilityWarnings.add(val); return this; }
        public Builder selfHealingApplied(boolean val) { this.selfHealingApplied = val; return this; }
        public Builder addHealingAction(String val) { this.healingActions.add(val); return this; }
        public Builder originalLocator(String val) { this.originalLocator = val; return this; }
        public Builder healedLocator(String val) { this.healedLocator = val; return this; }
        public Builder recoveryStrategy(String val) { this.recoveryStrategy = val; return this; }
        public Builder extractedData(String key, String value) { this.extractedData.put(key, value); return this; }
        public Builder variableAssignment(String key, String value) { this.variableAssignments.put(key, value); return this; }
        public Builder addListItem(String val) { this.listItems.add(val); return this; }
        public Builder addTableRow(Map<String, String> val) { this.tableData.add(val); return this; }
        public Builder browserType(String val) { this.browserType = val; return this; }
        public Builder browserVersion(String val) { this.browserVersion = val; return this; }
        public Builder viewportSize(String val) { this.viewportSize = val; return this; }
        public Builder framePath(String val) { this.framePath = val; return this; }
        public Builder pageTitle(String val) { this.pageTitle = val; return this; }
        public Builder currentUrl(String val) { this.currentUrl = val; return this; }
        public Builder reportCategory(String val) { this.reportCategory = val; return this; }
        public Builder addTag(String val) { this.tags.add(val); return this; }
        public Builder customProperty(String key, String value) { this.customProperties.put(key, value); return this; }
        public Builder severity(Severity val) { this.severity = val; return this; }
        public Builder knownIssue(boolean val) { this.knownIssue = val; return this; }
        public Builder issueTrackerId(String val) { this.issueTrackerId = val; return this; }
        public Builder metadata(String key, Object value) { this.metadata.put(key, value); return this; }
        public Builder evidence(String key, String value) { this.evidence.put(key, value); return this; }
        
        /**
         * Build the immutable ActionResult.
         */
        public ActionResult build() {
            // Auto-calculate duration if not set
            if (durationMs == 0 && endTimestamp != 0) {
                durationMs = endTimestamp - startTimestamp;
            }
            if (endTimestamp == 0) {
                endTimestamp = startTimestamp + durationMs;
            }
            
            return new ActionResult(this);
        }
    }
}
