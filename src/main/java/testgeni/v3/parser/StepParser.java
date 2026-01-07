package testgeni.v3.parser;

import testgeni.v3.core.domain.StepIntent;
import java.util.List;

/**
 * Interface for parsing test steps into StepIntent objects.
 * 
 * DESIGN PHILOSOPHY:
 * - Simple contract: String in, StepIntent out
 * - Stateless - no side effects
 * - Allows multiple implementations (Gherkin, Cucumber Expressions, etc.)
 * - Fail fast - throw ParseException for invalid steps
 * 
 * @author TestGeni v3
 * @version 3.0.0
 * @since 2026-01-06
 */
public interface StepParser {
    
    /**
     * Parse a single step into StepIntent.
     * 
     * @param stepText Raw step text (e.g., "When I fill 'John' in First Name")
     * @return Parsed StepIntent
     * @throws ParseException if step cannot be parsed
     */
    StepIntent parse(String stepText) throws ParseException;
    
    /**
     * Parse a single step with metadata.
     * 
     * @param stepText Raw step text
     * @param lineNumber Line number in feature file (for error reporting)
     * @param featureFile Feature file name
     * @param scenarioName Scenario name
     * @return Parsed StepIntent with metadata
     * @throws ParseException if step cannot be parsed
     */
    StepIntent parse(String stepText, int lineNumber, String featureFile, String scenarioName) 
        throws ParseException;
    
    /**
     * Parse multiple steps.
     * 
     * @param steps List of raw step texts
     * @return List of parsed StepIntents
     * @throws ParseException if any step cannot be parsed
     */
    List<StepIntent> parseSteps(List<String> steps) throws ParseException;
    
    /**
     * Check if a step can be parsed (without actually parsing).
     * 
     * @param stepText Raw step text
     * @return true if step is valid, false otherwise
     */
    boolean canParse(String stepText);
    
    /**
     * Get the reason why a step cannot be parsed.
     * 
     * @param stepText Raw step text
     * @return Error message, or null if step is valid
     */
    String getParseError(String stepText);
    
    /**
     * Exception thrown when step cannot be parsed.
     */
    class ParseException extends Exception {
        private final String stepText;
        private final int lineNumber;
        private final String reason;
        
        public ParseException(String stepText, String reason) {
            this(stepText, -1, reason);
        }
        
        public ParseException(String stepText, int lineNumber, String reason) {
            super(String.format("Failed to parse step at line %d: '%s'. Reason: %s", 
                lineNumber, stepText, reason));
            this.stepText = stepText;
            this.lineNumber = lineNumber;
            this.reason = reason;
        }
        
        public String getStepText() {
            return stepText;
        }
        
        public int getLineNumber() {
            return lineNumber;
        }
        
        public String getReason() {
            return reason;
        }
    }
}
