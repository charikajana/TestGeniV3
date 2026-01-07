package testgeni.v3.parser;

import java.util.Set;
import java.util.regex.Pattern;

/**
 * Centralized repository for all regex patterns used in Gherkin step parsing.
 * This class provides reusable patterns for extracting various elements from
 * natural language test steps.
 * 
 * @author TestGeni v3
 * @version 3.0.0
 * @since 2026-01-07
 */
public class RegexPatterns {
    
    // ========================================
    // GHERKIN & LANGUAGE PATTERNS
    // ========================================
    
    /**
     * Pattern to match Gherkin keywords (Given, When, Then, And, But).
     * Used to clean step text before parsing.
     */
    public static final Pattern GHERKIN_KEYWORD = Pattern.compile(
        "^\\s*(Given|When|Then|And|But)\\s+", Pattern.CASE_INSENSITIVE);
    
    /**
     * Pattern to match subject pronouns at the beginning of a step.
     * Matches: I, USER, We, He, She, They
     */
    public static final Pattern SUBJECT_PRONOUN = Pattern.compile(
        "^\\s*(I|USER|We|He|She|They)\\s+", Pattern.CASE_INSENSITIVE);
    
    // ========================================
    // VALUE EXTRACTION PATTERNS
    // ========================================
    
    /**
     * Pattern to match quoted strings (single or double quotes).
     * Captures the content between quotes.
     */
    public static final Pattern QUOTED_STRING = Pattern.compile(
        "['\"](([^'\"])+)['\"]");
    
    /**
     * Pattern to detect negation in step text.
     * Matches: not, n't, NOT
     */
    public static final Pattern NEGATION = Pattern.compile(
        "\\b(not|n't|NOT)\\b");
    
    /**
     * Pattern for multi-value select operations.
     * Matches patterns like: Select "A" and "B" and "C"
     */
    public static final Pattern MULTI_VALUE_SELECT = Pattern.compile(
        "and\\s+['\"]([^'\"]+)['\"]", Pattern.CASE_INSENSITIVE);
    
    // ========================================
    // CONTEXT & SCOPING PATTERNS
    // ========================================
    
    /**
     * Pattern to extract scoping context.
     * Matches: inside [element], within [element], in the [element]
     * Must be explicit to avoid misidentifying 'in [Target]'
     */
    public static final Pattern SCOPING = Pattern.compile(
        "\\b(inside|within|in\\s+the)\\s+([^,]+)$", Pattern.CASE_INSENSITIVE);
    
    /**
     * Pattern to extract frame/iframe references.
     * Matches: in iframe "frame-name", inside iframe "frame-id"
     */
    public static final Pattern FRAME = Pattern.compile(
        "\\b(in|inside)\\s+iframe\\s+['\"]([^'\"]+)['\"]", Pattern.CASE_INSENSITIVE);
    
    // ========================================
    // TABLE PATTERNS
    // ========================================
    
    /**
     * Pattern to extract table row conditions.
     * Example: in the row where Name is "John"
     */
    public static final Pattern TABLE_ROW = Pattern.compile(
        "in\\s+the\\s+row\\s+where\\s+(.+?)\\s+is\\s+['\"]([^'\"]+)['\"]", Pattern.CASE_INSENSITIVE);
    
    /**
     * Pattern to extract table column references.
     * Example: in Email column
     */
    public static final Pattern TABLE_COLUMN = Pattern.compile(
        "in\\s+(.+?)\\s+column", Pattern.CASE_INSENSITIVE);
    
    // ========================================
    // ELEMENT INTERACTION PATTERNS
    // ========================================
    
    /**
     * Pattern to extract tooltip references.
     * Example: tooltip of Submit button contains "Click to submit"
     */
    public static final Pattern TOOLTIP = Pattern.compile(
        "tooltip\\s+of\\s+(.+?)\\s+(contains|is|matches)", Pattern.CASE_INSENSITIVE);
    
    /**
     * Pattern to extract element index.
     * Matches: 1st, 2nd, 3rd, first, second, third, last
     */
    public static final Pattern ELEMENT_INDEX = Pattern.compile(
        "\\b(\\d+)(st|nd|rd|th)\\b|\\b(first|second|third|fourth|fifth|last)\\b", Pattern.CASE_INSENSITIVE);
    
    // ========================================
    // VERIFICATION PATTERNS
    // ========================================
    
    /**
     * Pattern to extract URL verification modifiers.
     * Example: URL contains "shopping", URL is exactly "http://example.com"
     */
    public static final Pattern URL_VERIFICATION = Pattern.compile(
        "URL\\s+(contains|is exactly|is|matches)\\s+['\"]([^'\"]+)['\"]", Pattern.CASE_INSENSITIVE);
    
    // ========================================
    // ALERT & DIALOG PATTERNS
    // ========================================
    
    /**
     * Pattern to extract alert message.
     * Example: alert says "Welcome"
     */
    public static final Pattern ALERT_MESSAGE = Pattern.compile(
        "alert\\s+says\\s+['\"]([^'\"]+)['\"]", Pattern.CASE_INSENSITIVE);
    
    /**
     * Pattern to extract accept alert with message.
     * Example: accept alert with message "Are you sure?"
     */
    public static final Pattern ACCEPT_ALERT = Pattern.compile(
        "(accept|verify and accept)\\s+alert\\s+with\\s+(message\\s+)?['\"]([^'\"]+)['\"]", Pattern.CASE_INSENSITIVE);
    
    /**
     * Pattern to extract prompt input.
     * Example: Enter "John Doe" in prompt
     */
    public static final Pattern ENTER_PROMPT = Pattern.compile(
        "Enter\\s+['\"]([^'\"]+)['\"]\\s+in\\s+prompt", Pattern.CASE_INSENSITIVE);
    
    /**
     * Pattern for removing an item from a list or multi-select.
     * Matches: remove [Value] from [Target]
     */
    public static final Pattern REMOVE_ITEM = Pattern.compile(
        "remove\\s+(.+?)\\s+from\\s+(.+)", Pattern.CASE_INSENSITIVE);
    
    // ========================================
    // WINDOW & TAB PATTERNS
    // ========================================
    
    /**
     * Pattern to detect click and switch to window operation.
     * Example: click on button and switch to new window
     */
    public static final Pattern CLICK_AND_SWITCH = Pattern.compile(
        "and\\s+switch\\s+to\\s+(new|parent)\\s+window", Pattern.CASE_INSENSITIVE);
    
    /**
     * Pattern to extract window count for verification.
     * Example: window count is 3
     */
    public static final Pattern WINDOW_COUNT = Pattern.compile(
        "window\\s+count\\s+is\\s+(\\d+)", Pattern.CASE_INSENSITIVE);
    
    // ========================================
    // WAIT PATTERNS
    // ========================================
    
    /**
     * Pattern to extract wait for progress.
     * Example: Wait for progress bar to reach "100%"
     */
    public static final Pattern WAIT_FOR_PROGRESS = Pattern.compile(
        "(Wait for|Monitor.*until reach)\\s+.*?['\"]([^'\"]+)['\"]", Pattern.CASE_INSENSITIVE);
    
    /**
     * Pattern to extract wait duration.
     * Example: wait 5 seconds, wait for 10s, wait 500ms
     */
    public static final Pattern WAIT_DURATION = Pattern.compile(
        "\\b(\\d+)\\s*(s|sec|second|seconds|ms|millis|milliseconds)\\b", Pattern.CASE_INSENSITIVE);
    
    // ========================================
    // VERIFICATION ATTRIBUTES
    // ========================================
    
    /**
     * Set of verification attributes used in assertions.
     */
    public static final Set<String> VERIFICATION_ATTRIBUTES = Set.of(
        "displayed", "visible", "enabled", "disabled", "selected", "checked",
        "readonly", "required", "focused", "present", "exists"
    );
    
    /**
     * Set of verification synonyms.
     */
    public static final Set<String> VERIFICATION_SYNONYMS = Set.of(
        "verify", "validate", "check", "ensure", "assert"
    );
    
    // ========================================
    // UTILITY METHODS
    // ========================================
    
    /**
     * Private constructor to prevent instantiation.
     * This is a utility class with only static members.
     */
    private RegexPatterns() {
        throw new UnsupportedOperationException("RegexPatterns is a utility class and cannot be instantiated");
    }
}
