package testgeni.v3.core.domain;

import java.util.*;

/**
 * Types of actions that can be performed in a test.
 * 
 * DESIGN PHILOSOPHY:
 * - Actions represent WHAT we do (CLICK, VERIFY, FILL)
 * - Attributes/modifiers represent WHAT we check (displayed, enabled, selected)
 * - Negation is handled by a flag in StepIntent, not separate action types
 * - This enum should NEVER need changes - it's comprehensive and complete
 * 
 * @author TestGeni v3
 * @version 3.0.0
 * @since 2026-01-06
 */
public enum ActionType {
    
    // ========================================
    // NAVIGATION ACTIONS
    // ========================================
    NAVIGATE(ActionCategory.NAVIGATION, 30000, RiskLevel.LOW, 
             "Navigate to", ElementType.ANY),
    REFRESH(ActionCategory.NAVIGATION, 10000, RiskLevel.LOW, 
            "Refresh page", ElementType.ANY),
    GO_BACK(ActionCategory.NAVIGATION, 5000, RiskLevel.LOW, 
            "Go back", ElementType.ANY),
    GO_FORWARD(ActionCategory.NAVIGATION, 5000, RiskLevel.LOW, 
               "Go forward", ElementType.ANY),
    
    // ========================================
    // MOUSE ACTIONS
    // ========================================
    CLICK(ActionCategory.MOUSE, 3000, RiskLevel.MEDIUM, 
          "Click on", ElementType.BUTTON, ElementType.LINK, ElementType.CHECKBOX, ElementType.RADIO,
          ElementType.IMAGE, ElementType.ICON, ElementType.MENUITEM, ElementType.TAB, ElementType.LIST_ITEM),
    DOUBLE_CLICK(ActionCategory.MOUSE, 3000, RiskLevel.MEDIUM, 
                 "Double-click on", ElementType.ANY),
    RIGHT_CLICK(ActionCategory.MOUSE, 3000, RiskLevel.LOW, 
                "Right-click on", ElementType.ANY),
    HOVER(ActionCategory.MOUSE, 2000, RiskLevel.LOW, 
          "Hover over", ElementType.ANY),
    DRAG_DROP(ActionCategory.MOUSE, 5000, RiskLevel.MEDIUM, 
              "Drag and drop", ElementType.ANY),
    
    // ========================================
    // INPUT ACTIONS
    // ========================================
    FILL(ActionCategory.INPUT, 2000, RiskLevel.LOW, 
         "Fill", ElementType.INPUT, ElementType.TEXTAREA),
    TYPE(ActionCategory.INPUT, 2000, RiskLevel.LOW, 
         "Type in", ElementType.INPUT, ElementType.TEXTAREA),
    CLEAR(ActionCategory.INPUT, 2000, RiskLevel.LOW, 
          "Clear", ElementType.INPUT, ElementType.TEXTAREA),
    PRESS_KEY(ActionCategory.INPUT, 2000, RiskLevel.LOW, 
              "Press key", ElementType.ANY),
    
    // ========================================
    // SELECTION ACTIONS
    // ========================================
    CHECK(ActionCategory.SELECTION, 2000, RiskLevel.LOW, 
          "Check", ElementType.CHECKBOX),
    UNCHECK(ActionCategory.SELECTION, 2000, RiskLevel.LOW, 
            "Uncheck", ElementType.CHECKBOX),
    SELECT(ActionCategory.SELECTION, 3000, RiskLevel.LOW, 
           "Select", ElementType.DROPDOWN, ElementType.SELECT),
    DESELECT(ActionCategory.SELECTION, 3000, RiskLevel.LOW, 
             "Deselect", ElementType.DROPDOWN, ElementType.SELECT),
    REMOVE(ActionCategory.SELECTION, 3000, RiskLevel.LOW, 
           "Remove", ElementType.DROPDOWN, ElementType.SELECT),
    
    // ========================================
    // VERIFICATION ACTIONS
    // ========================================
    VERIFY(ActionCategory.VERIFICATION, 5000, RiskLevel.NONE, 
           "Verify", ElementType.ANY),
    
    VERIFY_STATE(ActionCategory.VERIFICATION, 5000, RiskLevel.NONE, 
                 "Verify state", ElementType.ANY),
                 
    VERIFY_TEXT(ActionCategory.VERIFICATION, 5000, RiskLevel.NONE, 
                "Verify text", ElementType.ANY),
    
    // ========================================
    // CONTEXT SWITCH ACTIONS
    // ========================================
    SWITCH_FRAME(ActionCategory.CONTEXT, 5000, RiskLevel.LOW, 
                 "Switch to frame", ElementType.FRAME, ElementType.IFRAME),
    SWITCH_WINDOW(ActionCategory.CONTEXT, 5000, RiskLevel.LOW, 
                  "Switch to window", ElementType.ANY),
    SWITCH_TAB(ActionCategory.CONTEXT, 5000, RiskLevel.LOW, 
               "Switch to tab", ElementType.ANY),
    CLOSE_WINDOW(ActionCategory.CONTEXT, 2000, RiskLevel.MEDIUM, 
                 "Close window", ElementType.ANY),
     
    // ========================================
    // DIALOG ACTIONS
    // ========================================
    HANDLE_ALERT(ActionCategory.DIALOG, 5000, RiskLevel.MEDIUM, 
                 "Handle JavaScript Alert/Confirm/Prompt", ElementType.ANY),

    // ========================================
    // UTILITY ACTIONS
    // ========================================
    WAIT(ActionCategory.UTILITY, 0, RiskLevel.NONE, 
         "Wait", ElementType.ANY),
    SCROLL(ActionCategory.UTILITY, 2000, RiskLevel.LOW, 
           "Scroll to", ElementType.ANY),
    SCREENSHOT(ActionCategory.UTILITY, 3000, RiskLevel.NONE, 
               "Take screenshot", ElementType.ANY),
    UPLOAD_FILE(ActionCategory.UTILITY, 10000, RiskLevel.LOW, 
                "Upload file", ElementType.INPUT);
    
    // ========================================
    // ENUM FIELDS
    // ========================================
    
    private final ActionCategory category;
    private final int defaultTimeoutMs;
    private final RiskLevel riskLevel;
    private final String description;
    private final Set<ElementType> expectedElementTypes;
    
    /**
     * Constructor for ActionType enum.
     */
    ActionType(ActionCategory category, int defaultTimeoutMs, RiskLevel riskLevel, 
               String description, ElementType... expectedTypes) {
        this.category = category;
        this.defaultTimeoutMs = defaultTimeoutMs;
        this.riskLevel = riskLevel;
        this.description = description;
        this.expectedElementTypes = expectedTypes.length > 0 
            ? Set.of(expectedTypes) 
            : Set.of(ElementType.ANY);
    }
    
    // ========================================
    // CATEGORIZATION METHODS
    // ========================================
    
    /**
     * Get the category of this action.
     */
    public ActionCategory getCategory() {
        return category;
    }
    
    /**
     * Is this a navigation action?
     */
    public boolean isNavigation() {
        return category == ActionCategory.NAVIGATION;
    }
    
    /**
     * Is this a mouse action?
     */
    public boolean isMouse() {
        return category == ActionCategory.MOUSE;
    }
    
    /**
     * Is this an input action?
     */
    public boolean isInput() {
        return category == ActionCategory.INPUT;
    }
    
    /**
     * Is this a selection action?
     */
    public boolean isSelection() {
        return category == ActionCategory.SELECTION;
    }
    
    /**
     * Is this a verification action?
     */
    public boolean isVerification() {
        return category == ActionCategory.VERIFICATION;
    }
    
    /**
     * Is this a context switch action?
     */
    public boolean isContextSwitch() {
        return category == ActionCategory.CONTEXT;
    }
    
    /**
     * Is this a utility action?
     */
    public boolean isUtility() {
        return category == ActionCategory.UTILITY;
    }
    
    // ========================================
    // BEHAVIOR METHODS
    // ========================================
    
    /**
     * Does this action type require finding an element on the page?
     */
    public boolean needsElement() {
        return this != NAVIGATE && 
               this != REFRESH &&
               this != GO_BACK &&
               this != GO_FORWARD &&
               this != WAIT && 
               this != SCREENSHOT &&
               this != SWITCH_WINDOW &&
               this != SWITCH_TAB &&
               this != CLOSE_WINDOW &&
               this != HANDLE_ALERT;
    }
    
    /**
     * Is this a click action (single, double, or right-click)?
     */
    public boolean isClickAction() {
        return this == CLICK || 
               this == DOUBLE_CLICK || 
               this == RIGHT_CLICK;
    }
    
    /**
     * Is this a hover/movement action?
     */
    public boolean isHoverAction() {
        return this == HOVER;
    }
    
    /**
     * Is this a drag and drop action?
     */
    public boolean isDragDropAction() {
        return this == DRAG_DROP;
    }
    
    /**
     * Is this a keyboard action (pressing special keys)?
     */
    public boolean isKeyboardAction() {
        return this == PRESS_KEY;
    }
    
    /**
     * Is this a text input action (typing into fields)?
     */
    public boolean isTextInputAction() {
        return this == FILL || 
               this == TYPE || 
               this == CLEAR;
    }
    
    /**
     * Does this action modify page state?
     */
    public boolean modifiesPage() {
        return isClickAction() || 
               isTextInputAction() || 
               isKeyboardAction() ||
               this == CHECK || 
               this == UNCHECK || 
               this == SELECT || 
               this == DESELECT ||
               this == UPLOAD_FILE;
    }
    
    /**
     * Is this action destructive (could lose data)?
     */
    public boolean isDestructive() {
        return this == CLEAR || 
               this == CLOSE_WINDOW ||
               this == NAVIGATE; // Can lose unsaved changes
    }
    
    /**
     * Can this action trigger page navigation?
     */
    public boolean canTriggerNavigation() {
        return this == NAVIGATE ||
               this == GO_BACK ||
               this == GO_FORWARD ||
               this == REFRESH ||
               this == CLICK; // Clicking links can navigate
    }
    
    /**
     * Should wait for page load after this action?
     */
    public boolean shouldWaitForPageLoad() {
        return this == NAVIGATE ||
               this == REFRESH ||
               this == GO_BACK ||
               this == GO_FORWARD;
    }
    
    // ========================================
    // ELEMENT TYPE METHODS
    // ========================================
    
    /**
     * Get expected element types for this action.
     */
    public Set<ElementType> getExpectedElementTypes() {
        return expectedElementTypes;
    }
    
    /**
     * Is the given element type compatible with this action?
     */
    public boolean isCompatibleWith(ElementType elementType) {
        return expectedElementTypes.contains(ElementType.ANY) ||
               expectedElementTypes.contains(elementType);
    }
    
    /**
     * Get the most likely element type for this action.
     */
    public ElementType getMostLikelyElementType() {
        if (expectedElementTypes.contains(ElementType.ANY)) {
            return ElementType.ANY;
        }
        return expectedElementTypes.iterator().next();
    }
    
    // ========================================
    // METADATA METHODS
    // ========================================
    
    /**
     * Get the default timeout in milliseconds for this action.
     */
    public int getDefaultTimeoutMs() {
        return defaultTimeoutMs;
    }
    
    /**
     * Get the risk level of this action.
     */
    public RiskLevel getRiskLevel() {
        return riskLevel;
    }
    
    /**
     * Get human-readable description of this action.
     */
    public String getDescription() {
        return description;
    }
    
    /**
     * Is this action safe to retry automatically?
     */
    public boolean isSafeToRetry() {
        return riskLevel != RiskLevel.HIGH;
    }
    
    /**
     * Get recommended retry count.
     */
    public int getRecommendedRetries() {
        return switch(riskLevel) {
            case NONE -> 0;
            case LOW -> 3;
            case MEDIUM -> 2;
            case HIGH -> 0; // Don't auto-retry destructive actions
        };
    }
    
    
    // ========================================
    // EXECUTION CONTEXT (For ActionExecutor)
    // ========================================
    
    /**
     * Get required fields for this action to execute.
     * Helps validate StepIntent before execution.
     */
    public java.util.Set<String> getRequiredFields() {
        return switch(this) {
            case NAVIGATE -> Set.of("url");
            case FILL, TYPE -> Set.of("target", "value");
            case CHECK, UNCHECK, CLICK, DOUBLE_CLICK, RIGHT_CLICK, HOVER -> Set.of("target");
            case SELECT, DESELECT -> Set.of("target", "value");
            case VERIFY, VERIFY_STATE, VERIFY_TEXT -> Set.of("target", "verificationAttribute");
            case DRAG_DROP -> Set.of("dragSource", "dropTarget");
            case SWITCH_FRAME -> Set.of("frameAnchor");
            case UPLOAD_FILE -> Set.of("target", "filePath");
            case PRESS_KEY -> Set.of("key");
            case WAIT -> Set.of("waitDurationSeconds");
            case SCROLL -> Set.of("target");
            case CLEAR -> Set.of("target");
            case HANDLE_ALERT -> Set.of(); // Handled via value/modifiers
            default -> Set.of("target");
        };
    }
    
    /**
     * Get optional fields that enhance execution but aren't required.
     */
    public java.util.Set<String> getOptionalFields() {
        return switch(this) {
            case FILL, TYPE -> Set.of("elementType", "scopingContext");
            case CLICK -> Set.of("elementType", "scopingContext", "waitCondition");
            case VERIFY -> Set.of("expectedValue", "comparisonType", "negated");
            case SELECT -> Set.of("elementType");
            case NAVIGATE -> Set.of("timeoutMs");
            default -> Set.of("elementType", "scopingContext", "timeoutMs");
        };
    }
    
    /**
     * Does this action produce a result value?
     * (vs just succeeding/failing)
     */
    public boolean producesValue() {
        return this == VERIFY || this == VERIFY_STATE || this == VERIFY_TEXT; // Verification returns pass/fail + actual value
    }
    
    /**
     * Does this action extract data from the page?
     */
    public boolean extractsData() {
        return this == VERIFY || this == VERIFY_STATE || this == VERIFY_TEXT; // Could extend for future data extraction actions
    }
    
    /**
     * Expected state change after successful execution.
     */
    public String getExpectedOutcome() {
        return switch(this) {
            case NAVIGATE -> "Page loaded at new URL";
            case REFRESH -> "Page reloaded";
            case CLICK -> "Element clicked, possible navigation or state change";
            case FILL -> "Element value changed";
            case TYPE -> "Text entered character by character";
            case CLEAR -> "Element value cleared";
            case CHECK -> "Checkbox checked";
            case UNCHECK -> "Checkbox unchecked";
            case SELECT -> "Option selected from dropdown";
            case VERIFY -> "Assertion passed or failed";
            case HOVER -> "Mouse positioned over element";
            case DRAG_DROP -> "Element moved to new position";
            case SWITCH_FRAME -> "Context switched to iframe";
            case UPLOAD_FILE -> "File uploaded";
            case SCREENSHOT -> "Screenshot captured";
            default -> "Action completed";
        };
    }
    
    /**
     * Can this action be performed on a disabled element?
     */
    public boolean worksOnDisabledElements() {
        return this == VERIFY || // Can verify disabled state
               this == HOVER || // Can hover over disabled elements
               this == SCREENSHOT; // Can screenshot anything
    }
    
    /**
     * Can this action be performed on a hidden element?
     */
    public boolean worksOnHiddenElements() {
        return this == VERIFY; // Can verify hidden state
        // Most actions require visible elements
    }
    
    /**
     * Does this action require the element to be in viewport?
     */
    public boolean requiresViewport() {
        return this == CLICK ||
               this == DOUBLE_CLICK ||
               this == RIGHT_CLICK ||
               this == HOVER ||
               this == DRAG_DROP ||
               this == SCREENSHOT; // Need to see it to screenshot
    }
    
    /**
     * Should we auto-scroll element into view before action?
     */
    public boolean shouldAutoScroll() {
        return requiresViewport() && this != SCROLL; // Don't scroll for scroll action itself
    }
    
    /**
     * Does this action require focus on element?
     */
    public boolean requiresFocus() {
        return this == FILL ||
               this == TYPE ||
               this == PRESS_KEY ||
               this == CLEAR;
    }
    
    /**
     * Minimum confidence required for element match (0.0-1.0).
     * Higher risk actions require higher confidence.
     */
    public double getMinimumConfidence() {
        return switch(riskLevel) {
            case HIGH -> 0.90; // Very confident for destructive actions
            case MEDIUM -> 0.75; // Confident for state-changing actions
            case LOW -> 0.60; // Reasonable confidence for safe actions
            case NONE -> 0.50; // Low bar for read-only actions
        };
    }
    
    /**
     * Should we wait for network idle after this action?
     */
    public boolean shouldWaitForNetwork() {
        return this == NAVIGATE ||
               this == REFRESH ||
               this == CLICK || // Might trigger AJAX
               this == SELECT; // Might trigger AJAX
    }
    
    /**
     * Should we wait for DOM stability after this action?
     */
    public boolean shouldWaitForDomStability() {
        return modifiesPage();
    }
    
    /**
     * Get validation error message if StepIntent is invalid for this action.
     */
    public String validateIntent(testgeni.v3.core.domain.StepIntent intent) {
        if (intent == null) {
            return "StepIntent cannot be null";
        }
        
        if (intent.action != this) {
            return "Intent action mismatch: expected " + this + " but got " + intent.action;
        }
        
        // Check required fields
        for (String required : getRequiredFields()) {
            switch(required) {
                case "target":
                    if (intent.target == null || intent.target.trim().isEmpty()) {
                        return "Target is required for " + this;
                    }
                    break;
                case "value":
                    if (intent.value == null || intent.value.trim().isEmpty()) {
                        return "Value is required for " + this;
                    }
                    break;
                case "url":
                    if (intent.url == null || intent.url.trim().isEmpty()) {
                        return "URL is required for " + this;
                    }
                    break;
                case "filePath":
                    if (intent.filePath == null || intent.filePath.trim().isEmpty()) {
                        return "File path is required for " + this;
                    }
                    break;
                case "key":
                    if (intent.key == null || intent.key.trim().isEmpty()) {
                        return "Key is required for " + this;
                    }
                    break;
                case "dragSource":
                    if (intent.dragSource == null || intent.dragSource.trim().isEmpty()) {
                        return "Drag source is required for " + this;
                    }
                    break;
                case "dropTarget":
                    if (intent.dropTarget == null || intent.dropTarget.trim().isEmpty()) {
                        return "Drop target is required for " + this;
                    }
                    break;
                case "verificationAttribute":
                    if (intent.verificationAttribute == null || intent.verificationAttribute.trim().isEmpty()) {
                        return "Verification attribute is required for " + this;
                    }
                    break;
            }
        }
        
        // Element type compatibility check
        if (intent.elementType != null && intent.elementType != testgeni.v3.core.domain.ElementType.ANY) {
            if (!isCompatibleWith(intent.elementType)) {
                String expected = getExpectedElementTypes().toString();
                return String.format("Element type '%s' is not compatible with action '%s'. Expected: %s",
                    intent.elementType, this, expected);
            }
        }
        
        return null; // Valid
    }
    
    /**
     * Get human-readable name.
     */
    public String getDisplayName() {
        return name().toLowerCase().replace('_', ' ');
    }
    
    // ========================================
    // NESTED ENUMS
    // ========================================
    
    /**
     * Category of action (for grouping and reporting).
     */
    public enum ActionCategory {
        NAVIGATION,     // Navigate, refresh, back/forward
        MOUSE,          // Click, hover, drag/drop
        INPUT,          // Fill, type, clear, press key
        SELECTION,      // Check, uncheck, select
        VERIFICATION,   // Verify
        CONTEXT,        // Switch frame/window/tab
        DIALOG,         // Alert, Confirm, Prompt
        UTILITY         // Wait, scroll, screenshot, upload
    }
    
    /**
     * Risk level of action (for safety checks and auto-retry).
     */
    public enum RiskLevel {
        NONE,       // No risk (verify, wait, screenshot)
        LOW,        // Low risk (hover, scroll, most form actions)
        MEDIUM,     // Medium risk (click, close window)
        HIGH        // High risk (destructive actions - currently none, reserved for future)
    }
}
