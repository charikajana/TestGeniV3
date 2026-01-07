package testgeni.v3.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import testgeni.v3.core.domain.ActionResult;
import testgeni.v3.core.domain.ElementMatch;
import testgeni.v3.core.domain.StepIntent;

/**
 * Enhanced logging utility for TestGeni v3 execution.
 * Provides rich, colorized console output for debugging and tracking step execution.
 * Uses SLF4J for proper logging with timestamps and log levels.
 */
public class V3Logger {

    private static final Logger consoleLogger = LoggerFactory.getLogger("testgeni.v3.console");
    private static final Logger fileLogger = LoggerFactory.getLogger("testgeni.v3.file");

    // ANSI Color Codes
    private static final String RESET = "\u001B[0m";
    private static final String BLACK = "\u001B[30m";
    private static final String RED = "\u001B[31m";
    private static final String GREEN = "\u001B[32m";
    private static final String YELLOW = "\u001B[33m";
    private static final String BLUE = "\u001B[34m";
    private static final String PURPLE = "\u001B[35m";
    private static final String CYAN = "\u001B[36m";
    private static final String WHITE = "\u001B[37m";
    private static final String BOLD = "\u001B[1m";

    /**
     * Log the start of a step execution with comprehensive details.
     */
    public static void logStepExecution(String stepText, StepIntent intent, ElementMatch match, ActionResult result) {
        StringBuilder logOutput = new StringBuilder();
        logOutput.append("\n").append(PURPLE).append("┌──────────────────────────────────────────────────────────────────────────────").append(RESET).append("\n");
        
        // 1. Step Name
        logOutput.append(PURPLE).append("│ ").append(BOLD).append("STEP: ").append(RESET).append(WHITE).append(stepText).append(RESET).append("\n");
        
        // 2. Parsed Intent
        if (intent != null) {
            logOutput.append(PURPLE).append("│ ").append(CYAN).append("ACTION type:  ").append(RESET).append(intent.action).append("\n");
            logOutput.append(PURPLE).append("│ ").append(CYAN).append("TARGET element: ").append(RESET).append(intent.target != null ? intent.target : "None").append("\n");
            if (intent.value != null) {
                logOutput.append(PURPLE).append("│ ").append(CYAN).append("VALUE passed:  ").append(RESET).append("'").append(intent.value).append("'").append("\n");
            }
        }
        
        // 3. Locator Details
        if (match != null) {
            logOutput.append(PURPLE).append("│ ").append(YELLOW).append("LOCATOR found: ").append(RESET).append(match.locatorString).append("\n");
            logOutput.append(PURPLE).append("│ ").append(YELLOW).append("MATCH info:    ").append(RESET).append(match.strategy).append(" (").append((int)(match.confidence * 100)).append("% confidence)").append("\n");
            if (match.tagName != null) {
                String meta = match.tagName + (match.id != null ? " id=" + match.id : "");
                logOutput.append(PURPLE).append("│ ").append(YELLOW).append("ELEMENT tags:  ").append(RESET).append("<").append(meta).append(">").append("\n");
            }
        }
        
        // 4. Result Status
        if (result != null) {
            String statusColor = getStatusColor(result.status);
            String icon = getStatusIcon(result.status);
            
            logOutput.append(PURPLE).append("│ ").append(statusColor).append(icon).append(" STATUS:       ").append(result.status).append(RESET).append("\n");
            logOutput.append(PURPLE).append("│ ").append(statusColor).append("MESSAGE:      ").append(result.message).append(RESET).append("\n");
            
            if (result.durationMs > 0) {
                logOutput.append(PURPLE).append("│ ").append(BLUE).append("DURATION:     ").append(RESET).append(result.durationMs).append("ms").append("\n");
            }
        }
        
        logOutput.append(PURPLE).append("└──────────────────────────────────────────────────────────────────────────────").append(RESET);
        
        String fullOutput = logOutput.toString();
        consoleLogger.info(fullOutput);
        fileLogger.info(stripColors(fullOutput));
    }

    public static void info(String message) {
        consoleLogger.info("{}[INFO]{} {}", BLUE, RESET, message);
        fileLogger.info("[INFO] {}", stripColors(message));
    }

    public static void success(String message) {
        consoleLogger.info("{}[SUCCESS]{} {}", GREEN, RESET, message);
        fileLogger.info("[SUCCESS] {}", stripColors(message));
    }

    public static void warn(String message) {
        consoleLogger.warn("{}[WARN]{} {}", YELLOW, RESET, message);
        fileLogger.warn("[WARN] {}", stripColors(message));
    }

    public static void error(String message) {
        consoleLogger.error("{}[ERROR]{} {}", RED, RESET, message);
        fileLogger.error("[ERROR] {}", stripColors(message));
    }

    public static void error(String message, Throwable t) {
        consoleLogger.error("{}[ERROR]{} {}", RED, RESET, message, t);
        fileLogger.error("[ERROR] {}", stripColors(message), t);
    }
    
    public static void debug(String message) {
        consoleLogger.debug("{}[DEBUG]{} {}", CYAN, RESET, message);
        fileLogger.debug("[DEBUG] {}", stripColors(message));
    }
    
    public static void trace(String context, String details) {
        consoleLogger.trace("{}[TRACE]{} {} - {}", PURPLE, RESET, context, details);
        fileLogger.trace("[TRACE] {} - {}", stripColors(context), stripColors(details));
    }

    /**
     * Strips ANSI color codes from a string for file logging.
     */
    private static String stripColors(String text) {
        if (text == null) return null;
        return text.replaceAll("\u001B\\[[0-9;]*[mK]", "");
    }

    private static String getStatusColor(ActionResult.ResultStatus status) {
        if (status == null) return RESET;
        return switch (status) {
            case SUCCESS -> GREEN;
            case FAILURE, EXCEPTION, TIMEOUT, ELEMENT_NOT_FOUND, ELEMENT_NOT_VISIBLE, ELEMENT_NOT_ENABLED -> RED;
            case VERIFICATION_FAILED -> YELLOW;
            case SKIPPED -> WHITE;
            case WARNING -> PURPLE;
        };
    }

    private static String getStatusIcon(ActionResult.ResultStatus status) {
        if (status == null) return "[?]";
        return switch (status) {
            case SUCCESS -> "[PASS]";
            case FAILURE, TIMEOUT, ELEMENT_NOT_FOUND, ELEMENT_NOT_VISIBLE, ELEMENT_NOT_ENABLED -> "[FAIL]";
            case VERIFICATION_FAILED -> "[WARN]";
            case EXCEPTION -> "[ERROR]";
            case SKIPPED -> "[SKIP]";
            case WARNING -> "[!]";
        };
    }
}
