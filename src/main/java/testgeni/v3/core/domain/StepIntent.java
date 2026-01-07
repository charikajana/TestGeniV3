package testgeni.v3.core.domain;

import java.io.Serializable;
import java.util.*;

/**
 * Immutable data object representing a parsed test step's intent.
 * 
 * DESIGN PHILOSOPHY:
 * - This is the SINGLE SOURCE OF TRUTH for what a step wants to do
 * - Immutable by design - no setters, all fields final
 * - Exhaustive - captures EVERY possible variation we might encounter
 * - Future-proof - extensible via modifiers map without class changes
 * - Serializable - can be saved/loaded for debugging/reporting
 * 
 * This class is designed to NEVER need changes. Any new requirements
 * should be handled via the modifiers map or by adding to ActionType/ElementType enums.
 * 
 * @author TestGeni v3
 * @version 3.0.0
 * @since 2026-01-06
 */
public class StepIntent implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    // ========================================
    // CORE FIELDS (Always present)
    // ========================================
    
    /** The action to perform */
    public final ActionType action;
    
    /** Target element description */
    public final String target;
    
    // ========================================
    // STEP METADATA (For debugging/logging)
    // ========================================
    
    /** Original Gherkin step as written by user */
    public final String originalStep;
    
    /** Cleaned step (normalized, without Gherkin keywords) */
    public final String cleanStep;
    
    /** Line number in feature file (for error reporting) */
    public final Integer lineNumber;
    
    /** Feature file name */
    public final String featureFile;
    
    /** Scenario name */
    public final String scenarioName;
    
    // ========================================
    // VALUES (Data to use in action)
    // ========================================
    
    /** Primary value/data */
    public final String value;
    
    /** Multiple values (for multi-value steps) */
    public final List<String> values;
    
    /** Regex pattern (if value should be treated as regex) */
    public final String regexPattern;
    
    /** Use regex matching instead of exact/fuzzy */
    public final boolean isRegex;
    
    // ========================================
    // ELEMENT IDENTIFICATION
    // ========================================
    
    /** Element type hint */
    public final ElementType elementType;
    
    /** Element index/position (e.g., "1st button", "2nd link") */
    public final Integer elementIndex;
    
    /** Shadow DOM flag */
    public final boolean inShadowDom;
    
    /** CSS selector (explicit locator) */
    public final String cssSelector;
    
    /** XPath (explicit locator) */
    public final String xpath;
    
    // ========================================
    // VERIFICATION / ASSERTIONS
    // ========================================
    
    /** Negation flag (NOT displayed, NOT enabled) */
    public final boolean negated;
    
    /** Verification attribute (displayed, enabled, selected, value, etc.) */
    public final String verificationAttribute;
    
    /** Expected count (for "verify 3 items are displayed") */
    public final Integer expectedCount;
    
    /** Comparison operator (equals, contains, startsWith, endsWith, greaterThan, lessThan) */
    public final String comparisonOperator;
    
    // ========================================
    // CONTEXT / SCOPING
    // ========================================
    
    /** Scoping context ("inside", "within") */
    public final String scopingContext;

    /** Context index (e.g., "2nd" in "in the 2nd row") */
    public final Integer contextIndex;
    
    /** Parent reference (alias for scopingContext) */
    public final String parentReference;
    
    /** Frame/iframe anchor */
    public final String frameAnchor;
    
    /** Shadow DOM host */
    public final String shadowHost;
    
    // ========================================
    // TABLE OPERATIONS
    // ========================================
    
    /** Table row condition ("where firstName is 'John'") */
    public final String tableRowCondition;
    
    /** Table column */
    public final String tableColumn;
    
    /** Row index (0-based) */
    public final Integer rowIndex;
    
    /** Column index (0-based) */
    public final Integer columnIndex;
    
    // ========================================
    // SPECIAL OPERATIONS
    // ========================================
    
    /** Tooltip element reference */
    public final String tooltipOf;
    
    /** File path (for UPLOAD_FILE action) */
    public final String filePath;
    
    /** URL (for NAVIGATE action) */
    public final String url;
    
    /** Keyboard key (for PRESS_KEY action) */
    public final String key;
    
    /** Scroll direction (up, down, left, right) */
    public final String scrollDirection;
    
    /** Scroll amount in pixels */
    public final Integer scrollAmount;
    
    /** Drag source element */
    public final String dragSource;
    
    /** Drop target element */
    public final String dropTarget;
    
    // ========================================
    // TIMING / WAITING
    // ========================================
    
    /** Custom timeout in milliseconds (overrides default) */
    public final Integer timeoutMs;
    
    /** Wait condition before action (waitForVisible, waitForEnabled, etc.) */
    public final String waitCondition;
    
    /** Wait duration in seconds (for explicit WAIT action) */
    public final Integer waitDurationSeconds;
    
    /** Polling interval in milliseconds */
    public final Integer pollingIntervalMs;
    
    // ========================================
    // RETRY / ERROR HANDLING
    // ========================================
    
    /** Number of retry attempts */
    public final Integer retryAttempts;
    
    /** Continue on failure flag */
   
 public final boolean continueOnFailure;
    
    /** Screenshot on failure */
    public final boolean screenshotOnFailure;
    
    // ========================================
    // DATA-DRIVEN / LOOPS
    // ========================================
    
    /** From index (for data-driven loops) */
    public final Integer fromIndex;
    
    /** To index (for data-driven loops) */
    public final Integer toIndex;
    
    /** Variable name (for storing extracted values) */
    public final String variableName;
    
    // ========================================
    // EXTENSIBILITY
    // ========================================
    
    /** Generic modifiers map (for future extensibility) */
    public final Map<String, String> modifiers;
    
    /**
     * Full constructor - ONLY use via Builder
     */
    private StepIntent(Builder builder) {
        // Core (target is optional for some actions)
        this.action = Objects.requireNonNull(builder.action, "Action cannot be null");
        this.target = builder.target != null ? builder.target : "page";
        
        // Step metadata
        this.originalStep = builder.originalStep;
        this.cleanStep = builder.cleanStep;
        this.lineNumber = builder.lineNumber;
        this.featureFile = builder.featureFile;
        this.scenarioName = builder.scenarioName;
        
        // Values
        this.value = builder.value;
        this.values = Collections.unmodifiableList(new ArrayList<>(builder.values));
        this.regexPattern = builder.regexPattern;
        this.isRegex = builder.isRegex;
        
        // Element identification
        this.elementType = builder.elementType != null ? builder.elementType : ElementType.ANY;
        this.elementIndex = builder.elementIndex;
        this.inShadowDom = builder.inShadowDom;
        this.cssSelector = builder.cssSelector;
        this.xpath = builder.xpath;
        
        // Verification
        this.negated = builder.negated;
        this.verificationAttribute = builder.verificationAttribute;
        this.expectedCount = builder.expectedCount;
        this.comparisonOperator = builder.comparisonOperator;
        
        // Context
        this.scopingContext = builder.scopingContext;
        this.contextIndex = builder.contextIndex;
        this.parentReference = builder.scopingContext; // Alias
        this.frameAnchor = builder.frameAnchor;
        this.shadowHost = builder.shadowHost;
        
        // Table operations
        this.tableRowCondition = builder.tableRowCondition;
        this.tableColumn = builder.tableColumn;
        this.rowIndex = builder.rowIndex;
        this.columnIndex = builder.columnIndex;
        
        // Special operations
        this.tooltipOf = builder.tooltipOf;
        this.filePath = builder.filePath;
        this.url = builder.url;
        this.key = builder.key;
        this.scrollDirection = builder.scrollDirection;
        this.scrollAmount = builder.scrollAmount;
        this.dragSource = builder.dragSource;
        this.dropTarget = builder.dropTarget;
        
        // Timing
        this.timeoutMs = builder.timeoutMs;
        this.waitCondition = builder.waitCondition;
        this.waitDurationSeconds = builder.waitDurationSeconds;
        this.pollingIntervalMs = builder.pollingIntervalMs;
        
        // Retry
        this.retryAttempts = builder.retryAttempts;
        this.continueOnFailure = builder.continueOnFailure;
        this.screenshotOnFailure = builder.screenshotOnFailure;
        
        // Data-driven
        this.fromIndex = builder.fromIndex;
        this.toIndex = builder.toIndex;
        this.variableName = builder.variableName;
        
        // Extensibility
        this.modifiers = Collections.unmodifiableMap(new HashMap<>(builder.modifiers));
    }
    
    // ========================================
    // CONVENIENCE CONSTRUCTORS
    // ========================================
    
    /**
     * Simple constructor for basic actions.
     */
    public StepIntent(ActionType action, String target) {
        this(new Builder(action, target));
    }
    
    /**
     * Constructor with value.
     */
    public StepIntent(ActionType action, String target, String value) {
        this(new Builder(action, target).value(value));
    }
    
    // ========================================
    // HELPER METHODS
    // ========================================
    
    public boolean hasScoping() {
        return scopingContext != null && !scopingContext.trim().isEmpty();
    }
    
    public boolean hasFrameContext() {
        return frameAnchor != null && !frameAnchor.trim().isEmpty();
    }
    
    public boolean hasTableContext() {
        return (tableRowCondition != null && !tableRowCondition.trim().isEmpty()) ||
               (tableColumn != null && !tableColumn.trim().isEmpty()) ||
               rowIndex != null || columnIndex != null;
    }
    
    public boolean isTooltipVerification() {
        return tooltipOf != null && !tooltipOf.trim().isEmpty();
    }
    
    public boolean hasExplicitLocator() {
        return (cssSelector != null && !cssSelector.trim().isEmpty()) ||
               (xpath != null && !xpath.trim().isEmpty());
    }
    
    public boolean hasCustomTimeout() {
        return timeoutMs != null;
    }
    
    public int getEffectiveTimeout() {
        return timeoutMs != null ? timeoutMs : action.getDefaultTimeoutMs();
    }
    
    public boolean hasModifier(String key) {
        return modifiers.containsKey(key);
    }
    
    public String getModifier(String key) {
        return modifiers.get(key);
    }
    
    public String getModifier(String key, String defaultValue) {
        return modifiers.getOrDefault(key, defaultValue);
    }
    
    /**
     * Is this a verification action?
     */
    public boolean isVerification() {
        return action != null && action.isVerification();
    }
    
    /**
     * Does this action need to find an element?
     */
    public boolean needsElement() {
        return action != null && action.needsElement();
    }
    
    /**
     * Does this step have a value?
     */
    public boolean hasValue() {
        return value != null && !value.trim().isEmpty();
    }
    
    /**
     * Does this step have multiple values?
     */
    public boolean hasMultipleValues() {
        return values != null && !values.isEmpty();
    }
    
    /**
     * Is this a negated verification (e.g., "is NOT displayed")?
     */
    public boolean isNegated() {
        return negated;
    }
    
    /**
     * Validate this StepIntent (delegating to ActionType).
     */
    public String validate() {
        if (action == null) {
            return "Action cannot be null";
        }
        return action.validateIntent(this);
    }
    
    /**
     * Is this intent valid?
     */
    public boolean isValid() {
        return validate() == null;
    }
    
    /**
     * Get human-readable description.
     */
    public String getDescription() {
        StringBuilder desc = new StringBuilder();
        
        if (originalStep != null) {
            return originalStep; // Best description is the original step
        }
        
        desc.append(action.getDescription()).append(" ").append(target);
        
        if (value != null && !value.equals(target)) {
            desc.append(" = '").append(value).append("'");
        }
        
        if (hasScoping()) {
            desc.append(" inside ").append(scopingContext);
        }
        
        if (verificationAttribute != null) {
            desc.append(negated ? " is NOT " : " is ").append(verificationAttribute);
        }
        
        return desc.toString();
    }
    
    @Override
    public String toString() {
        return String.format("StepIntent{action=%s, target='%s', value='%s'}", 
            action, target, value);
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof StepIntent that)) return false;
        return action == that.action && Objects.equals(target, that.target) && 
               Objects.equals(value, that.value);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(action, target, value);
    }
    
    // ========================================
    // BUILDER CLASS
    // ========================================
    
    /**
     * Builder for StepIntent (recommended construction method).
     */
    public static class Builder {
        // Required
        private final ActionType action;
        private final String target;
        
        // Optional (with defaults)
        private String originalStep;
        private String cleanStep;
        private Integer lineNumber;
        private String featureFile;
        private String scenarioName;
        private String value;
        private List<String> values = new ArrayList<>();
        private String regexPattern;
        private boolean isRegex;
        private ElementType elementType;
        private Integer elementIndex;
        private boolean inShadowDom;
        private String cssSelector;
        private String xpath;
        private boolean negated;
        private String verificationAttribute;
        private Integer expectedCount;
        private String comparisonOperator;
        private String scopingContext;
        private Integer contextIndex;
        private String frameAnchor;
        private String shadowHost;
        private String tableRowCondition;
        private String tableColumn;
        private Integer rowIndex;
        private Integer columnIndex;
        private String tooltipOf;
        private String filePath;
        private String url;
        private String key;
        private String scrollDirection;
        private Integer scrollAmount;
        private String dragSource;
        private String dropTarget;
        private Integer timeoutMs;
        private String waitCondition;
        private Integer waitDurationSeconds;
        private Integer pollingIntervalMs;
        private Integer retryAttempts;
        private boolean continueOnFailure;
        private boolean screenshotOnFailure;
        private Integer fromIndex;
        private Integer toIndex;
        private String variableName;
        private Map<String, String> modifiers = new HashMap<>();
        
        public Builder(ActionType action, String target) {
            this.action = action;
            this.target = target;
        }
        
        // Fluent setters (returns Builder for chaining)
        public Builder originalStep(String val) { this.originalStep = val; return this; }
        public Builder cleanStep(String val) { this.cleanStep = val; return this; }
        public Builder lineNumber(Integer val) { this.lineNumber = val; return this; }
        public Builder featureFile(String val) { this.featureFile = val; return this; }
        public Builder scenarioName(String val) { this.scenarioName = val; return this; }
        public Builder value(String val) { 
            this.value = val; 
            if (val != null && !this.values.contains(val)) {
                this.values.add(val);
            }
            return this; 
        }
        public Builder values(List<String> val) { 
            if (val != null) {
                this.values.addAll(val);
                if (this.value == null && !val.isEmpty()) {
                    this.value = val.get(0);
                }
            }
            return this; 
        }
        public Builder addValue(String val) { 
            if (val != null) {
                this.values.add(val);
                if (this.value == null) {
                    this.value = val;
                }
            }
            return this; 
        }
        public Builder regexPattern(String val) { this.regexPattern = val; this.isRegex = true; return this; }
        public Builder elementType(ElementType val) { this.elementType = val; return this; }
        public Builder elementIndex(Integer val) { this.elementIndex = val; return this; }
        public Builder inShadowDom(boolean val) { this.inShadowDom = val; return this; }
        public Builder cssSelector(String val) { this.cssSelector = val; return this; }
        public Builder xpath(String val) { this.xpath = val; return this; }
        public Builder negated(boolean val) { this.negated = val; return this; }
        public Builder verificationAttribute(String val) { this.verificationAttribute = val; return this; }
        public Builder expectedCount(Integer val) { this.expectedCount = val; return this; }
        public Builder comparisonOperator(String val) { this.comparisonOperator = val; return this; }
        public Builder scopingContext(String val) { this.scopingContext = val; return this; }
        public Builder contextIndex(Integer val) { this.contextIndex = val; return this; }
        public Builder frameAnchor(String val) { this.frameAnchor = val; return this; }
        public Builder shadowHost(String val) { this.shadowHost = val; return this; }
        public Builder tableRowCondition(String val) { this.tableRowCondition = val; return this; }
        public Builder tableColumn(String val) { this.tableColumn = val; return this; }
        public Builder rowIndex(Integer val) { this.rowIndex = val; return this; }
        public Builder columnIndex(Integer val) { this.columnIndex = val; return this; }
        public Builder tooltipOf(String val) { this.tooltipOf = val; return this; }
        public Builder filePath(String val) { this.filePath = val; return this; }
        public Builder url(String val) { this.url = val; return this; }
        public Builder key(String val) { this.key = val; return this; }
        public Builder scrollDirection(String val) { this.scrollDirection = val; return this; }
        public Builder scrollAmount(Integer val) { this.scrollAmount = val; return this; }
        public Builder dragSource(String val) { this.dragSource = val; return this; }
        public Builder dropTarget(String val) { this.dropTarget = val; return this; }
        public Builder timeoutMs(Integer val) { this.timeoutMs = val; return this; }
        public Builder waitCondition(String val) { this.waitCondition = val; return this; }
        public Builder waitDurationSeconds(Integer val) { this.waitDurationSeconds = val; return this; }
        public Builder pollingIntervalMs(Integer val) { this.pollingIntervalMs = val; return this; }
        public Builder retryAttempts(Integer val) { this.retryAttempts = val; return this; }
        public Builder continueOnFailure(boolean val) { this.continueOnFailure = val; return this; }
        public Builder screenshotOnFailure(boolean val) { this.screenshotOnFailure = val; return this; }
        public Builder fromIndex(Integer val) { this.fromIndex = val; return this; }
        public Builder toIndex(Integer val) { this.toIndex = val; return this; }
        public Builder variableName(String val) { this.variableName = val; return this; }
        public Builder isRegex(boolean val) { this.isRegex = val; return this; }
        public Builder metadata(String key, String value) { 
            this.modifiers.put(key, value); 
            return this; 
        }
        public Builder modifier(String key, String value) { 
            this.modifiers.put(key, value); 
            return this; 
        }
        public Builder modifiers(Map<String, String> val) { 
            if (val != null) {
                this.modifiers.putAll(val);
            }
            return this; 
        }
        
        /**
         * Build the immutable StepIntent.
         */
        public StepIntent build() {
            return new StepIntent(this);
        }
    }
}
