package testgeni.v3.util;

import testgeni.v3.core.domain.ActionType;
import testgeni.v3.core.domain.StepIntent;
import testgeni.v3.parser.GherkinStepParser;
import testgeni.v3.parser.StepParser;

import java.util.List;

/**
 * Diagnostic tool to analyze Gherkin steps without executing them.
 * Provides details on Action Type, Target Element, and Step Validity.
 */
public class StepAnalyzer {

    private final StepParser parser;

    public StepAnalyzer() {
        this.parser = new GherkinStepParser();
    }

    public AnalysisResult analyze(String stepText) {
        try {
            StepIntent intent = parser.parse(stepText);
            
            boolean isValid = true;
            String reason = "Parsed successfully";

            // Validation Logic
            if (intent.action == null) {
                isValid = false;
                reason = "Action could not be identified. No matching verb found in ActionVerbRegistry.";
            } else if (intent.action.needsElement() && (intent.target == null || intent.target.isEmpty())) {
                isValid = false; 
                reason = "Action " + intent.action + " requires a specific target element, but none was found.";
            }

            return new AnalysisResult(
                stepText,
                intent.action,
                intent.target,
                intent.value,
                isValid,
                reason
            );
        } catch (Exception e) {
            return new AnalysisResult(stepText, null, "error", null, false, "Parser Error: " + e.getMessage());
        }
    }

    public static class AnalysisResult {
        public final String originalStep;
        public final ActionType action;
        public final String target;
        public final String value;
        public final boolean isValid;
        public final String message;

        public AnalysisResult(String step, ActionType action, String target, String value, boolean isValid, String message) {
            this.originalStep = step;
            this.action = action;
            this.target = target;
            this.value = value;
            this.isValid = isValid;
            this.message = message;
        }

        @Override
        public String toString() {
            String status = isValid ? "[VALID]" : "[INVALID]";
            return String.format(
                "%s\n" +
                "  Step:   %s\n" +
                "  Action: %-12s\n" +
                "  Target: %-12s\n" +
                "  Value:  %-12s\n" +
                "  Reason: %s\n",
                status, originalStep, action, target, (value != null ? value : "N/A"), message
            );
        }
    }

    public void scanFeatureFile(String featurePath) {
        FeatureFileReader reader = new FeatureFileReader();
        System.out.println("==========================================================");
        System.out.println("ANALYZING FEATURE FILE: " + featurePath);
        System.out.println("==========================================================");
        System.out.println(String.format("%-10s | %-12s | %-20s | %-15s | %s", "STATUS", "ACTION", "TARGET", "VALUE", "STEP"));
        System.out.println("-".repeat(100));

        try {
            List<String> steps = reader.readSteps(featurePath);
            int validCount = 0;
            int total = steps.size();

            for (String step : steps) {
                AnalysisResult result = analyze(step);
                String status = result.isValid ? "[VALID]" : "[INVALID]";
                
                System.out.println(String.format("%-10s | %-12s | %-20s | %-15s | %s", 
                    status, 
                    result.action != null ? result.action : "UNKNOWN",
                    result.target != null ? result.target : "None",
                    result.value != null ? result.value : "",
                    step));
                
                if (result.isValid) validCount++;
            }

            System.out.println("-".repeat(100));
            System.out.println(String.format("SUMMARY: %d/%d steps are valid (%.1f%%)", 
                validCount, total, (total > 0 ? (double)validCount/total*100 : 0)));
            System.out.println("==========================================================\n");

        } catch (Exception e) {
            System.err.println("Error reading file: " + e.getMessage());
        }
    }

    /**
     * CLI Runner for Step Analysis
     */
    public static void main(String[] args) {
        StepAnalyzer analyzer = new StepAnalyzer();
        
        if (args.length > 0) {
            for (String arg : args) {
                if (arg.endsWith(".feature")) {
                    analyzer.scanFeatureFile(arg);
                } else {
                    System.out.println(analyzer.analyze(arg));
                }
            }
        } else {
            // Default: analyze the main state verification feature
            analyzer.scanFeatureFile("src/main/resources/features/StateVerification.feature");
        }
    }
}
