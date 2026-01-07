package testgeni.v3.core.domain;

import com.microsoft.playwright.Locator;
import java.io.Serializable;
import java.util.*;

/**
 * Immutable data object representing a matched element from the DOM.
 * 
 * DESIGN PHILOSOPHY:
 * - Result of ElementFinder.find() operation
 * - Contains everything needed to interact with the element
 * - Immutable by design - represents a snapshot in time
 * - Rich metadata for debugging and reporting
 * - Confidence scoring for transparency
 * 
 * This class is designed to NEVER need changes. Any new metadata
 * should be added to the elementMetadata map.
 * 
 * @author TestGeni v3
 * @version 3.0.0
 * @since 2026-01-06
 */
public class ElementMatch implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    // Confidence thresholds
    public static final double HIGH_CONFIDENCE = 0.8;
    public static final double MEDIUM_CONFIDENCE = 0.5;
    public static final double LOW_CONFIDENCE = 0.3;
    
    // ========================================
    // CORE FIELDS (Always present)
    // ========================================
    
    /** Playwright Locator (ready to use for actions) */
    public final transient Locator locator; // transient because Locator is not serializable
    
    /** Confidence score (0.0 to 1.0) */
    public final double confidence;
    
    /** Whether match meets minimum confidence threshold */
    public final boolean isConfident;
    
    /** Match status */
    public final MatchStatus status;
    
    // ========================================
    // MATCH STRATEGY
    // ========================================
    
    /** Strategy used to find this element */
    public final MatchStrategy strategy;
    
    /** Locator string used (CSS, XPath, text, etc.) */
    public final String locatorString;
    
    /** Locator type (css, xpath, text, role, testId) */
    public final String locatorType;
    
    /** Backup strategies tried (if primary failed) */
    public final List<MatchStrategy> fallbackStrategies;
    
    // ========================================
    // ELEMENT METADATA
    // ========================================
    
    /** Actual visible text of the element */
    public final String actualText;
    
    /** Inner text (including hidden children) */
    public final String innerText;
    
    /** Tag name (button, input, div, etc.) */
    public final String tagName;
    
    /** Element ID attribute */
    public final String id;
    
    /** Element class attribute */
    public final String className;
    
    /** Element name attribute */
    public final String name;
    
    /** Element type attribute (for inputs) */
    public final String type;
    
    /** Element value attribute */
    public final String valueAttribute;
    
    /** Element placeholder */
    public final String placeholder;
    
    /** Element aria-label */
    public final String ariaLabel;
    
    /** Element role */
    public final String role;
    
    /** Element title attribute */
    public final String title;
    
    /** Element data-testid */
    public final String dataTestId;
    
    /** All element attributes */
    public final Map<String, String> attributes;
    
    // ========================================
    // ELEMENT STATE
    // ========================================
    
    /** Is element visible? */
    public final boolean visible;
    
    /** Is element enabled? */
    public final boolean enabled;
    
    /** Is element focused? */
    public final boolean focused;
    
    /** Is element checked? (for checkboxes/radios) */
    public final Boolean checked;
    
    /** Is element selected? (for options) */
    public final Boolean selected;
    
    /** Is element readonly? */
    public final boolean readonly;
    
    /** Is element required? */
    public final boolean required;
    
    // ========================================
    // POSITION / LAYOUT
    // ========================================
    
    /** Element index in DOM scan (0-based) */
    public final Integer elementIndex;
    
    /** Element's bounding box */
    public final BoundingBox boundingBox;
    
    /** Is element in viewport? */
    public final boolean inViewport;
    
    /** Z-index (for layering) */
    public final Integer zIndex;
    
    // ========================================
    // CONTEXT
    // ========================================
    
    /** Parent element descriptor */
    public final String parentElement;
    
    /** Nearest labeled element (for inputs) */
    public final String labelText;
    
    /** Frame path (if in iframe) */
    public final String framePath;
    
    /** Shadow DOM host (if in shadow root) */
    public final String shadowHost;
    
    // ========================================
    // SCORING DETAILS (For debugging)
    // ========================================
    
    /** Text match score component */
    public final double textScore;
    
    /** Attribute match score component */
    public final double attributeScore;
    
    /** Type match score component */
    public final double typeScore;
    
    /** Accessibility score component */
    public final double accessibilityScore;
    
    /** Position/proximity score component */
    public final double proximityScore;
    
    /** Score breakdown explanation */
    public final String scoreBreakdown;
    
    /** Penalties applied */
    public final List<String> penalties;
    
    /** Bonuses applied */
    public final List<String> bonuses;
    
    // ========================================
    // PERFORMANCE METRICS
    // ========================================
    
    /** Time taken to find element (ms) */
    public final long findDurationMs;
    
    /** Number of candidates scanned */
    public final int candidatesScanned;
    
    /** Whether DOM scan cache was used */
    public final boolean usedCache;
    
    /** Timestamp when match was created */
    public final long timestamp;
    
    // ========================================
    // ALTERNATIVES
    // ========================================
    
    /** Alternative matches (lower confidence) */
    public final List<ElementMatch> alternatives;
    
    /** Why this match was chosen over alternatives */
    public final String selectionReason;
    
    // ========================================
    // EXTENSIBILITY
    // ========================================
    
    /** Generic metadata map (for future extensibility) */
    public final Map<String, Object> elementMetadata;
    
    /**
     * Full constructor - ONLY use via Builder
     */
    private ElementMatch(Builder builder) {
        // Core
        this.locator = builder.locator;
        this.confidence = builder.confidence;
        this.isConfident = builder.confidence >= builder.confidenceThreshold;
        this.status = builder.status;
        
        // Strategy
        this.strategy = builder.strategy;
        this.locatorString = builder.locatorString;
        this.locatorType = builder.locatorType;
        this.fallbackStrategies = Collections.unmodifiableList(new ArrayList<>(builder.fallbackStrategies));
        
        // Element metadata
        this.actualText = builder.actualText;
        this.innerText = builder.innerText;
        this.tagName = builder.tagName;
        this.id = builder.id;
        this.className = builder.className;
        this.name = builder.name;
        this.type = builder.type;
        this.valueAttribute = builder.valueAttribute;
        this.placeholder = builder.placeholder;
        this.ariaLabel = builder.ariaLabel;
        this.role = builder.role;
        this.title = builder.title;
        this.dataTestId = builder.dataTestId;
        this.attributes = Collections.unmodifiableMap(new HashMap<>(builder.attributes));
        
        // Element state
        this.visible = builder.visible;
        this.enabled = builder.enabled;
        this.focused = builder.focused;
        this.checked = builder.checked;
        this.selected = builder.selected;
        this.readonly = builder.readonly;
        this.required = builder.required;
        
        // Position
        this.elementIndex = builder.elementIndex;
        this.boundingBox = builder.boundingBox;
        this.inViewport = builder.inViewport;
        this.zIndex = builder.zIndex;
        
        // Context
        this.parentElement = builder.parentElement;
        this.labelText = builder.labelText;
        this.framePath = builder.framePath;
        this.shadowHost = builder.shadowHost;
        
        // Scoring details
        this.textScore = builder.textScore;
        this.attributeScore = builder.attributeScore;
        this.typeScore = builder.typeScore;
        this.accessibilityScore = builder.accessibilityScore;
        this.proximityScore = builder.proximityScore;
        this.scoreBreakdown = builder.scoreBreakdown;
        this.penalties = Collections.unmodifiableList(new ArrayList<>(builder.penalties));
        this.bonuses = Collections.unmodifiableList(new ArrayList<>(builder.bonuses));
        
        // Performance
        this.findDurationMs = builder.findDurationMs;
        this.candidatesScanned = builder.candidatesScanned;
        this.usedCache = builder.usedCache;
        this.timestamp = builder.timestamp != 0 ? builder.timestamp : System.currentTimeMillis();
        
        // Alternatives
        this.alternatives = Collections.unmodifiableList(new ArrayList<>(builder.alternatives));
        this.selectionReason = builder.selectionReason;
        
        // Extensibility
        this.elementMetadata = Collections.unmodifiableMap(new HashMap<>(builder.elementMetadata));
    }
    
    // ========================================
    // HELPER METHODS
    // ========================================
    
    /**
     * Is this a high-confidence match?
     */
    public boolean isHighConfidence() {
        return confidence >= HIGH_CONFIDENCE;
    }
    
    /**
     * Is this a medium-confidence match?
     */
    public boolean isMediumConfidence() {
        return confidence >= MEDIUM_CONFIDENCE && confidence < HIGH_CONFIDENCE;
    }
    
    /**
     * Is this a low-confidence match?
     */
    public boolean isLowConfidence() {
        return confidence < MEDIUM_CONFIDENCE;
    }
    
    /**
     * Is element in a usable state?
     */
    public boolean isUsable() {
        return visible && enabled;
    }
    
    /**
     * Has penalties?
     */
    public boolean hasPenalties() {
        return !penalties.isEmpty();
    }
    
    /**
     * Has alternatives?
     */
    public boolean hasAlternatives() {
        return !alternatives.isEmpty();
    }
    
    /**
     * Get metadata value.
     */
    public Object getMetadata(String key) {
        return elementMetadata.get(key);
    }
    
    /**
     * Get metadata value with default.
     */
    public Object getMetadata(String key, Object defaultValue) {
        return elementMetadata.getOrDefault(key, defaultValue);
    }
    
    /**
     * Get attribute value.
     */
    public String getAttribute(String key) {
        return attributes.get(key);
    }
    
    /**
     * Get attribute value with default.
     */
    public String getAttribute(String key, String defaultValue) {
        return attributes.getOrDefault(key, defaultValue);
    }
    
    /**
     * Get human-readable description of this match.
     */
    public String getDescription() {
        StringBuilder desc = new StringBuilder();
        
        desc.append(String.format("Match[%.0f%% confidence]", confidence * 100));
        
        if (tagName != null) {
            desc.append(" <").append(tagName).append(">");
        }
        
        if (id != null && !id.isEmpty()) {
            desc.append(" #").append(id);
        }
        
        if (actualText != null && !actualText.isEmpty()) {
            desc.append(" '").append(actualText.substring(0, Math.min(30, actualText.length()))).append("'");
        }
        
        desc.append(" via ").append(strategy);
        
        if (!isUsable()) {
            desc.append(" [NOT USABLE]");
        }
        
        return desc.toString();
    }
    
    /**
     * Get detailed debug information.
     */
    public String getDebugInfo() {
        StringBuilder debug = new StringBuilder();
        debug.append("=== ElementMatch Debug Info ===\n");
        debug.append(String.format("Confidence: %.2f (%.0f%%)\n", confidence, confidence * 100));
        debug.append(String.format("Strategy: %s\n", strategy));
        debug.append(String.format("Locator: %s (%s)\n", locatorString, locatorType));
        debug.append(String.format("Tag: %s, ID: %s, Class: %s\n", tagName, id, className));
        debug.append(String.format("Text: '%s'\n", actualText));
        debug.append(String.format("Visible: %s, Enabled: %s\n", visible, enabled));
        debug.append(String.format("Score Breakdown: %s\n", scoreBreakdown));
        
        if (!penalties.isEmpty()) {
            debug.append(String.format("Penalties: %s\n", String.join(", ", penalties)));
        }
        
        if (!bonuses.isEmpty()) {
            debug.append(String.format("Bonuses: %s\n", String.join(", ", bonuses)));
        }
        
        debug.append(String.format("Performance: %dms, %d candidates scanned\n", findDurationMs, candidatesScanned));
        
        if (hasAlternatives()) {
            debug.append(String.format("Alternatives: %d other matches found\n", alternatives.size()));
        }
        
        return debug.toString();
    }
    
    @Override
    public String toString() {
        return getDescription();
    }
    
    // ========================================
    // NESTED CLASSES
    // ========================================
    
    /**
     * Match status enum.
     */
    public enum MatchStatus {
        FOUND,              // Element found successfully
        NOT_FOUND,          // No element matched
        MULTIPLE_FOUND,     // Multiple elements matched (ambiguous)
        TIMEOUT,            // Timed out waiting for element
        ERROR               // Error during finding
    }
    
    /**
     * Match strategy enum.
     */
    public enum MatchStrategy {
        BY_ID,              // Found by ID attribute
        BY_TEST_ID,         // Found by data-testid
        BY_NAME,            // Found by name attribute
        BY_TEXT,            // Found by exact text match
        BY_FUZZY_TEXT,      // Found by fuzzy text matching
        BY_PLACEHOLDER,     // Found by placeholder text
        BY_ARIA_LABEL,      // Found by aria-label
        BY_ROLE,            // Found by ARIA role
        BY_CSS,             // Found by CSS selector
        BY_XPATH,           // Found by XPath
        BY_SEMANTIC,        // Found by semantic analysis
        BY_ML,              // Found by ML model
        BY_INDEX,           // Found by index position
        BY_LABEL,           // Found by associated label
        CACHED              // Retrieved from cache
    }
    
    /**
     * Bounding box (position and size).
     */
    public static class BoundingBox implements Serializable {
        private static final long serialVersionUID = 1L;
        
        public final double x;
        public final double y;
        public final double width;
        public final double height;
        
        public BoundingBox(double x, double y, double width, double height) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
        }
        
        public double centerX() {
            return x + width / 2;
        }
        
        public double centerY() {
            return y + height / 2;
        }
        
        public double area() {
            return width * height;
        }
        
        @Override
        public String toString() {
            return String.format("BoundingBox{x=%.1f, y=%.1f, w=%.1f, h=%.1f}", x, y, width, height);
        }
    }
    
    // ========================================
    // BUILDER CLASS
    // ========================================
    
    /**
     * Builder for ElementMatch (recommended construction method).
     */
    public static class Builder {
        // Required
        private Locator locator;
        private double confidence;
        private MatchStrategy strategy;
        
        // Optional (with defaults)
        private double confidenceThreshold = HIGH_CONFIDENCE;
        private MatchStatus status = MatchStatus.FOUND;
        private String locatorString;
        private String locatorType;
        private List<MatchStrategy> fallbackStrategies = new ArrayList<>();
        private String actualText;
        private String innerText;
        private String tagName;
        private String id;
        private String className;
        private String name;
        private String type;
        private String valueAttribute;
        private String placeholder;
        private String ariaLabel;
        private String role;
        private String title;
        private String dataTestId;
        private Map<String, String> attributes = new HashMap<>();
        private boolean visible = true;
        private boolean enabled = true;
        private boolean focused;
        private Boolean checked;
        private Boolean selected;
        private boolean readonly;
        private boolean required;
        private Integer elementIndex;
        private BoundingBox boundingBox;
        private boolean inViewport = true;
        private Integer zIndex;
        private String parentElement;
        private String labelText;
        private String framePath;
        private String shadowHost;
        private double textScore;
        private double attributeScore;
        private double typeScore;
        private double accessibilityScore;
        private double proximityScore;
        private String scoreBreakdown;
        private List<String> penalties = new ArrayList<>();
        private List<String> bonuses = new ArrayList<>();
        private long findDurationMs;
        private int candidatesScanned;
        private boolean usedCache;
        private long timestamp;
        private List<ElementMatch> alternatives = new ArrayList<>();
        private String selectionReason;
        private Map<String, Object> elementMetadata = new HashMap<>();
        
        public Builder(Locator locator, double confidence, MatchStrategy strategy) {
            this.locator = locator;
            this.confidence = confidence;
            this.strategy = strategy;
        }
        
        // Fluent setters
        public Builder confidenceThreshold(double val) { this.confidenceThreshold = val; return this; }
        public Builder status(MatchStatus val) { this.status = val; return this; }
        public Builder locatorString(String val) { this.locatorString = val; return this; }
        public Builder locatorType(String val) { this.locatorType = val; return this; }
        public Builder addFallbackStrategy(MatchStrategy val) { this.fallbackStrategies.add(val); return this; }
        public Builder actualText(String val) { this.actualText = val; return this; }
        public Builder innerText(String val) { this.innerText = val; return this; }
        public Builder tagName(String val) { this.tagName = val; return this; }
        public Builder id(String val) { this.id = val; return this; }
        public Builder className(String val) { this.className = val; return this; }
        public Builder name(String val) { this.name = val; return this; }
        public Builder type(String val) { this.type = val; return this; }
        public Builder valueAttribute(String val) { this.valueAttribute = val; return this; }
        public Builder placeholder(String val) { this.placeholder = val; return this; }
        public Builder ariaLabel(String val) { this.ariaLabel = val; return this; }
        public Builder role(String val) { this.role = val; return this; }
        public Builder title(String val) { this.title = val; return this; }
        public Builder dataTestId(String val) { this.dataTestId = val; return this; }
        public Builder attribute(String key, String value) { this.attributes.put(key, value); return this; }
        public Builder attributes(Map<String, String> val) { if (val != null) this.attributes.putAll(val); return this; }
        public Builder visible(boolean val) { this.visible = val; return this; }
        public Builder enabled(boolean val) { this.enabled = val; return this; }
        public Builder focused(boolean val) { this.focused = val; return this; }
        public Builder checked(Boolean val) { this.checked = val; return this; }
        public Builder selected(Boolean val) { this.selected = val; return this; }
        public Builder readonly(boolean val) { this.readonly = val; return this; }
        public Builder required(boolean val) { this.required = val; return this; }
        public Builder elementIndex(Integer val) { this.elementIndex = val; return this; }
        public Builder boundingBox(BoundingBox val) { this.boundingBox = val; return this; }
        public Builder boundingBox(double x, double y, double width, double height) { 
            this.boundingBox = new BoundingBox(x, y, width, height); 
            return this; 
        }
        public Builder inViewport(boolean val) { this.inViewport = val; return this; }
        public Builder zIndex(Integer val) { this.zIndex = val; return this; }
        public Builder parentElement(String val) { this.parentElement = val; return this; }
        public Builder labelText(String val) { this.labelText = val; return this; }
        public Builder framePath(String val) { this.framePath = val; return this; }
        public Builder shadowHost(String val) { this.shadowHost = val; return this; }
        public Builder textScore(double val) { this.textScore = val; return this; }
        public Builder attributeScore(double val) { this.attributeScore = val; return this; }
        public Builder typeScore(double val) { this.typeScore = val; return this; }
        public Builder accessibilityScore(double val) { this.accessibilityScore = val; return this; }
        public Builder proximityScore(double val) { this.proximityScore = val; return this; }
        public Builder scoreBreakdown(String val) { this.scoreBreakdown = val; return this; }
        public Builder addPenalty(String val) { this.penalties.add(val); return this; }
        public Builder addBonus(String val) { this.bonuses.add(val); return this; }
        public Builder findDurationMs(long val) { this.findDurationMs = val; return this; }
        public Builder candidatesScanned(int val) { this.candidatesScanned = val; return this; }
        public Builder usedCache(boolean val) { this.usedCache = val; return this; }
        public Builder timestamp(long val) { this.timestamp = val; return this; }
        public Builder addAlternative(ElementMatch val) { this.alternatives.add(val); return this; }
        public Builder alternatives(List<ElementMatch> val) { if (val != null) this.alternatives.addAll(val); return this; }
        public Builder selectionReason(String val) { this.selectionReason = val; return this; }
        public Builder metadata(String key, Object value) { this.elementMetadata.put(key, value); return this; }
        public Builder elementMetadata(Map<String, Object> val) { if (val != null) this.elementMetadata.putAll(val); return this; }
        
        /**
         * Build the immutable ElementMatch.
         */
        public ElementMatch build() {
            return new ElementMatch(this);
        }
    }
}
