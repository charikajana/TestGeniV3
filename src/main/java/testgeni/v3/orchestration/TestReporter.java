package testgeni.v3.orchestration;

import testgeni.v3.core.domain.ActionResult;

import java.util.ArrayList;
import java.util.List;

/**
 * Handles reporting and aggregation of test results.
 */
public class TestReporter {

    private final List<ActionResult> results = new ArrayList<>();

    public void addResult(ActionResult result) {
        results.add(result);
        logToConsole(result);
    }

    private void logToConsole(ActionResult result) {
        String icon = "[FAIL]";
        if (result.success) {
            icon = "[PASS]";
        } else if (result.status == ActionResult.ResultStatus.SKIPPED) {
            icon = "[SKIP]";
        }

        System.out.printf("%s [%s] %s (%dms)%n", 
            icon, 
            result.action != null ? result.action : "UNKNOWN",
            result.message,
            result.durationMs);
            
        if (result.status == ActionResult.ResultStatus.FAILURE && result.detailedMessage != null) {
            System.err.println("   └─ Error: " + result.detailedMessage);
        }
    }

    public List<ActionResult> getResults() {
        return results;
    }

    public long getTotalDuration() {
        return results.stream().mapToLong(r -> r.durationMs).sum();
    }

    public int getSuccessCount() {
        return (int) results.stream().filter(r -> r.success).count();
    }

    public int getFailureCount() {
        return (int) results.stream()
            .filter(r -> !r.success && r.status != ActionResult.ResultStatus.SKIPPED)
            .count();
    }

    public int getSkippedCount() {
        return (int) results.stream()
            .filter(r -> r.status == ActionResult.ResultStatus.SKIPPED)
            .count();
    }

    public int getTotalCount() {
        return results.size();
    }

    public void printSummary() {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("TEST EXECUTION COMPLETE");
        System.out.println("=".repeat(60));
        System.out.printf("Total Steps: %d%n", getTotalCount());
        System.out.printf("Passed:      %d%n", getSuccessCount());
        System.out.printf("Failed:      %d%n", getFailureCount());
        System.out.printf("Skipped:     %d%n", getSkippedCount());
        System.out.printf("Duration:    %dms%n", getTotalDuration());
        System.out.println("=".repeat(60));
    }
}
