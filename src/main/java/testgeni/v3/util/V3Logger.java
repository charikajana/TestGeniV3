package testgeni.v3.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import testgeni.v3.core.domain.ActionResult;
import testgeni.v3.core.domain.ElementMatch;
import testgeni.v3.core.domain.StepIntent;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Enhanced logging utility for TestGeni v3 execution.
 * Provides rich, colorized console output for debugging and tracking step execution.
 * Uses SLF4J for proper logging with timestamps and log levels.
 */
public class V3Logger {

    private static final Logger consoleLogger = LoggerFactory.getLogger("testgeni.v3.console");
    private static final Logger fileLogger = LoggerFactory.getLogger("testgeni.v3.file");
    private static final ObjectMapper objectMapper = new ObjectMapper()
            .enable(SerializationFeature.INDENT_OUTPUT);

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
     * Log the start of a step execution in JSON format.
     */
    public static void logStepExecution(String stepText, StepIntent intent, ElementMatch match, ActionResult result) {
        Map<String, Object> logObj = new LinkedHashMap<>();
        
        // 1. Step Context
        logObj.put("step", stepText);
        
        // 2. Parsed Intent
        if (intent != null) {
            Map<String, Object> intentObj = new LinkedHashMap<>();
            intentObj.put("action", intent.action != null ? intent.action.toString() : null);
            intentObj.put("target", intent.target);
            intentObj.put("value", intent.value);
            logObj.put("intent", intentObj);
        }
        
        // 3. Locator Details
        if (match != null) {
            Map<String, Object> matchObj = new LinkedHashMap<>();
            matchObj.put("locator", match.locatorString);
            matchObj.put("strategy", match.strategy != null ? match.strategy.toString() : null);
            matchObj.put("confidence", match.confidence);
            if (match.tagName != null) {
                matchObj.put("tagName", match.tagName);
                matchObj.put("id", match.id);
            }
            logObj.put("element", matchObj);
        }
        
        // 4. Result Status
        if (result != null) {
            Map<String, Object> resultObj = new LinkedHashMap<>();
            resultObj.put("status", result.status != null ? result.status.toString() : null);
            resultObj.put("message", result.message);
            resultObj.put("durationMs", result.durationMs);
            
            if (result.expectedValue != null || result.actualValue != null) {
                resultObj.put("expected", result.expectedValue);
                resultObj.put("actual", result.actualValue);
            }
            logObj.put("result", resultObj);
        }
        
        try {
            String jsonOutput = objectMapper.writeValueAsString(logObj);
            consoleLogger.info("\n{}\n", jsonOutput);
            fileLogger.info(jsonOutput);
        } catch (Exception e) {
            error("Failed to serialize step execution to JSON", e);
        }
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
