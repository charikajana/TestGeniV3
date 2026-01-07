package testgeni.v3.core.domain;

import java.util.*;

/**
 * Types of HTML elements we commonly interact with.
 * 
 * DESIGN PHILOSOPHY:
 * - Comprehensive coverage of all web elements
 * - Maps to HTML tags and ARIA roles
 * - Provides validation and compatibility checking
 * - This enum should NEVER need changes - it's complete
 * 
 * @author TestGeni v3
 * @version 3.0.0
 * @since 2026-01-06
 */
public enum ElementType {
    
    // ========================================
    // INTERACTIVE ELEMENTS
    // NOTE: Tag patterns are HINTS for scoring, NOT rigid selectors
    // Element finding uses text/ARIA/semantic matching, not these patterns
    // ========================================
    BUTTON(Set.of("button"), "button", true, true, 
           Set.of("type", "disabled", "aria-label", "aria-pressed"),
           Set.of("submit", "reset", "button")),
    
    LINK(Set.of("a"), "link", true, false,
         Set.of("href", "target", "aria-label"),
         Set.of()),
    
    INPUT(Set.of("input"), "textbox", true, true,
          Set.of("type", "name", "id", "placeholder", "value", "disabled", "readonly", "required", "aria-label"),
          Set.of("text", "email", "password", "number", "tel", "url", "search", "date", "time")),
    
    TEXTAREA(Set.of("textarea"), "textbox", true, true,
             Set.of("name", "id", "placeholder", "disabled", "readonly", "required", "rows", "cols"),
             Set.of()),
    
    // IMPORTANT: These work for BOTH native AND custom implementations
    // Element finder will match by ARIA role, NOT tag name
    CHECKBOX(Set.of("input"), "checkbox", true, true,
             Set.of("name", "id", "checked", "disabled", "value", "aria-label", "aria-checked"),
             Set.of("checkbox")),
    
    RADIO(Set.of("input"), "radio", true, true,
          Set.of("name", "id", "checked", "disabled", "value", "aria-label", "aria-checked"),
          Set.of("radio")),
    
    // SELECT works for native <select> OR any element with role='listbox'/'combobox'
    SELECT(Set.of("select"), "combobox", true, true,
           Set.of("name", "id", "disabled", "multiple", "size", "aria-label", "aria-expanded"),
           Set.of()),
    
    DROPDOWN(Set.of("select"), "listbox", true, true,
             Set.of("name", "id", "disabled", "aria-label", "aria-expanded"),
             Set.of()),
    
    OPTION(Set.of("option", "li"), "option", false, true,
           Set.of("value", "selected", "disabled", "label", "aria-selected"),
           Set.of()),
    
    // ========================================
    // DISPLAY ELEMENTS
    // ========================================
    TEXT(Set.of("p", "span", "div", "h1", "h2", "h3", "h4", "h5", "h6"), "text", false, false,
         Set.of("class", "id"),
         Set.of()),
    
    LABEL(Set.of("label"), "label", false, false,
          Set.of("for", "id"),
          Set.of()),
    
    HEADING(Set.of("h1", "h2", "h3", "h4", "h5", "h6"), "heading", false, false,
            Set.of("class", "id", "aria-level"),
            Set.of()),
    
    PARAGRAPH(Set.of("p"), "paragraph", false, false,
              Set.of("class", "id"),
              Set.of()),
    
    SPAN(Set.of("span"), "none", false, false,
         Set.of("class", "id"),
         Set.of()),
    
    DIV(Set.of("div"), "none", false, false,
        Set.of("class", "id", "role"),
        Set.of()),
    
    // ========================================
    // MEDIA ELEMENTS
    // ========================================
    IMAGE(Set.of("img"), "img", false, false,
          Set.of("src", "alt", "title", "width", "height"),
          Set.of()),
    
    VIDEO(Set.of("video"), "video", true, false,
          Set.of("src", "controls", "autoplay", "loop", "muted"),
          Set.of()),
    
    AUDIO(Set.of("audio"), "audio", true, false,
          Set.of("src", "controls", "autoplay", "loop", "muted"),
          Set.of()),
    
    // ========================================
    // STRUCTURAL ELEMENTS
    // ========================================
    TABLE(Set.of("table"), "table", false, false,
          Set.of("class", "id", "border", "cellpadding", "cellspacing"),
          Set.of()),
    
    TABLE_ROW(Set.of("tr"), "row", false, false,
              Set.of("class", "id"),
              Set.of()),
    
    TABLE_CELL(Set.of("td", "th"), "cell", false, false,
               Set.of("class", "id", "colspan", "rowspan"),
               Set.of()),
    
    LIST(Set.of("ul", "ol"), "list", false, false,
         Set.of("class", "id"),
         Set.of()),
    
    LIST_ITEM(Set.of("li"), "listitem", false, false,
              Set.of("class", "id", "value"),
              Set.of()),
    
    // ========================================
    // FORM ELEMENTS
    // ========================================
    FORM(Set.of("form"), "form", false, false,
         Set.of("action", "method", "name", "id"),
         Set.of()),
    
    FIELDSET(Set.of("fieldset"), "group", false, false,
             Set.of("disabled", "name"),
             Set.of()),
    
    LEGEND(Set.of("legend"), "legend", false, false,
           Set.of(),
           Set.of()),
    
    // ========================================
    // IFRAME / FRAME
    // ========================================
    IFRAME(Set.of("iframe"), "iframe", false, false,
           Set.of("src", "name", "id", "title"),
           Set.of()),
    
    FRAME(Set.of("frame"), "frame", false, false,
          Set.of("src", "name", "id"),
          Set.of()),
    
    // ========================================
    // CUSTOM / SPECIAL (ARIA-based, tag agnostic)
    // CRITICAL: These match by ARIA role, NOT tag name
    // ========================================
    ICON(Set.of("i", "svg", "img"), "img", true, false,
         Set.of("class", "aria-label", "title"),
         Set.of()),
    
    // TOOLTIP can be ANY tag with role='tooltip'
    TOOLTIP(Set.of(), "tooltip", false, false,
            Set.of("aria-label", "aria-describedby"),
            Set.of()),
    
    // MODAL can be ANY tag with role='dialog'
    MODAL(Set.of(), "dialog", true, false,
          Set.of("aria-modal", "aria-labelledby", "aria-describedby"),
          Set.of()),
    
    // ALERT can be ANY tag with role='alert'
    ALERT(Set.of(), "alert", false, false,
          Set.of("aria-live", "aria-atomic"),
          Set.of()),
    
    // MENU can be ANY tag with role='menu'
    MENU(Set.of("ul", "nav"), "menu", true, false,
         Set.of("aria-label", "aria-orientation"),
         Set.of()),
    
    MENUITEM(Set.of("li", "a"), "menuitem", true, false,
             Set.of("aria-label", "aria-disabled"),
             Set.of()),
    
    // TAB can be ANY tag with role='tab'
    TAB(Set.of(), "tab", true, false,
        Set.of("aria-selected", "aria-controls", "aria-label"),
        Set.of()),
    
    TABPANEL(Set.of(), "tabpanel", false, false,
             Set.of("aria-labelledby", "aria-hidden"),
             Set.of()),
    
    SLIDER(Set.of("input"), "slider", true, false,
           Set.of("min", "max", "value", "step", "aria-valuemin", "aria-valuemax", "aria-valuenow"),
           Set.of()),
    
    PROGRESS(Set.of("progress"), "progressbar", false, false,
             Set.of("value", "max", "aria-valuemin", "aria-valuemax", "aria-valuenow"),
             Set.of()),
    
    // ========================================
    // FALLBACK
    // ========================================
    ANY(Set.of(), "any", true, true,
        Set.of("class", "id", "name", "role", "aria-label"),
        Set.of());
    
    // ========================================
    // ENUM FIELDS
    // ========================================
    
    /**
     * Typical tag names for this element type (for scoring bonus, NOT filtering).
     * Example: BUTTON has Set.of("button") but can also match <div role="button">
     */
    private final Set<String> typicalTags;
    
    private final String ariaRole;
    private final boolean interactive;
    private final boolean formElement;
    private final Set<String> commonAttributes;
    private final Set<String> typeValues;
    
    /**
     * Constructor for ElementType enum.
     */
    ElementType(Set<String> typicalTags, String ariaRole, boolean interactive, boolean formElement,
                Set<String> commonAttributes, Set<String> typeValues) {
        this.typicalTags = Set.copyOf(typicalTags); // Immutable
        this.ariaRole = ariaRole;
        this.interactive = interactive;
        this.formElement = formElement;
        this.commonAttributes = commonAttributes;
        this.typeValues = typeValues;
    }
    
    // ========================================
    // METADATA METHODS
    // ========================================
    
    /**
     * Get typical tag names for this element type (hint for scoring).
     */
    public Set<String> getTypicalTags() {
        return typicalTags;
    }
    
    /**
     * Check if given tag name is typical for this element type.
     * Used for scoring bonus, NOT for filtering.
     */
    public boolean isTypicalTag(String tagName) {
        if (tagName == null || typicalTags.isEmpty()) {
            return false;
        }
        return typicalTags.contains(tagName.toLowerCase());
    }
    
    /**
     * Get ARIA role for this element type.
     */
    public String getAriaRole() {
        return ariaRole;
    }
    
    
    // ========================================
    // DOM SCAN MATCHING (CRITICAL FOR ACCURACY)
    // ========================================
    
    /**
     * Calculate how well a scanned DOM element matches this ElementType.
     * 
     * PRIORITY (highest to lowest):
     * 1. ARIA role match (most reliable) - 0.9 confidence
     * 2. Tag name + type attribute - 0.8 confidence
     * 3. Tag name only - 0.6 confidence
     * 4. Attribute patterns - 0.4 confidence
     * 5. Class patterns - 0.3 confidence
     * 
     * @param tagName element tag name (div, button, input, etc.)
     * @param role ARIA role attribute
     * @param type type attribute (for input elements)
     * @param className class attribute
     * @param attributes all element attributes
     * @return confidence score 0.0 (no match) to 1.0 (perfect match)
     */
    public double calculateMatchConfidence(String tagName, String role, String type, 
                                          String className, Map<String, String> attributes) {
        double confidence = 0.0;
        
        // 1. ARIA ROLE - Highest priority (most reliable indicator)
        if (role != null && !role.isEmpty()) {
            String normalizedRole = role.toLowerCase().trim();
            
            // Exact role match
            if (normalizedRole.equals(this.ariaRole)) {
                return 0.95; // Very high confidence
            }
            
            // Role aliases/variations
            if (matchesRoleVariation(normalizedRole)) {
                return 0.90; // High confidence
            }
        }
        
        // 2. TAG NAME + TYPE - For native HTML elements (e.g., input[type=radio])
        if (tagName != null) {
            String normalizedTag = tagName.toLowerCase().trim();
            
            // Special handling for <input> - type attribute is critical
            if ("input".equals(normalizedTag) && type != null) {
                String normalizedType = type.toLowerCase().trim();
                
                if (this == CHECKBOX && "checkbox".equals(normalizedType)) return 0.85;
                if (this == RADIO && "radio".equals(normalizedType)) return 0.85;
                if (this == BUTTON && ("submit".equals(normalizedType) || "button".equals(normalizedType))) return 0.80;
                if (this == INPUT && ("text".equals(normalizedType) || "email".equals(normalizedType) || 
                        "password".equals(normalizedType) ||"number".equals(normalizedType) || 
                        "tel".equals(normalizedType) || "url".equals(normalizedType) || 
                        "search".equals(normalizedType))) return 0.85;
                if (this == SLIDER && "range".equals(normalizedType)) return 0.85;
            }
            
            // Check if tag is typical for this element type
            if (isTypicalTag(normalizedTag)) {
                confidence = Math.max(confidence, 0.70); // Good match
            }
            
            // Tag name partial match (e.g., h1-h6 for HEADING)
            if (matchesTagVariation(normalizedTag)) {
                confidence = Math.max(confidence, 0.65);
            }
        }
        
        // 3. COMMON ATTRIBUTES - Check for expected attributes
        if (attributes != null && !attributes.isEmpty()) {
            int matchingAttributes = 0;
            int totalCommonAttributes = commonAttributes.size();
            
            for (String commonAttr : commonAttributes) {
                if (attributes.containsKey(commonAttr)) {
                    matchingAttributes++;
                }
            }
            
            if (totalCommonAttributes > 0) {
                double attributeScore = (double) matchingAttributes / totalCommonAttributes;
                confidence = Math.max(confidence, 0.30 + (attributeScore * 0.20)); // 0.30-0.50
            }
        }
        
        // 4. CLASS PATTERNS - Weakest signal but still useful
        if (className != null && !className.isEmpty()) {
            String normalizedClass = className.toLowerCase();
            
            if (matchesClassPattern(normalizedClass)) {
                confidence = Math.max(confidence, 0.35);
            }
        }
        
        // 5. SPECIAL CHECKS - Behavioral/structural hints
        
        // Clickable elements usually have onclick, cursor:pointer, or button-like classes
        if (this.interactive && attributes != null) {
            if (attributes.containsKey("onclick") || 
                attributes.containsKey("data-action") ||
                (className != null && (className.contains("clickable") || className.contains("btn")))) {
                confidence = Math.max(confidence, 0.25);
            }
        }
        
        // Form elements usually have name attribute
        if (this.formElement && attributes != null) {
            if (attributes.containsKey("name") || attributes.containsKey("id")) {
                confidence = Math.max(confidence, 0.20);
            }
        }
        
        return Math.min(confidence, 1.0); // Cap at 1.0
    }
    
    /**
     * Check if role matches variations/aliases.
     */
    private boolean matchesRoleVariation(String role) {
        return switch(this) {
            case BUTTON -> role.equals("button");
            case LINK -> role.equals("link");
            case INPUT, TEXTAREA -> role.equals("textbox");
            case CHECKBOX -> role.equals("checkbox");
            case RADIO -> role.equals("radio");
            case SELECT, DROPDOWN -> role.equals("listbox") || role.equals("combobox");
            case OPTION -> role.equals("option");
            case HEADING -> role.equals("heading");
            case IMAGE -> role.equals("img") || role.equals("image");
            case TABLE -> role.equals("table");
            case TABLE_ROW -> role.equals("row");
            case TABLE_CELL -> role.equals("cell") || role.equals("gridcell");
            case LIST -> role.equals("list");
            case LIST_ITEM -> role.equals("listitem");
            case MODAL -> role.equals("dialog") || role.equals("alertdialog");
            case ALERT -> role.equals("alert");
            case MENU -> role.equals("menu");
            case MENUITEM -> role.equals("menuitem");
            case TAB -> role.equals("tab");
            case TABPANEL -> role.equals("tabpanel");
            case SLIDER -> role.equals("slider");
            case PROGRESS -> role.equals("progressbar");
            case TOOLTIP -> role.equals("tooltip");
            default -> false;
        };
    }
    
    /**
     * Check if tag matches variations (e.g., h1-h6 for HEADING).
     */
    private boolean matchesTagVariation(String tag) {
        return switch(this) {
            case HEADING -> tag.matches("h[1-6]");
            case TABLE_CELL -> tag.equals("td") || tag.equals("th");
            case LIST -> tag.equals("ul") || tag.equals("ol");
            case TEXT -> tag.equals("p") || tag.equals("span") || tag.equals("div");
            default -> false;
        };
    }
    
    /**
     * Check if class name contains patterns for this element type.
     */
    private boolean matchesClassPattern(String className) {
        String classes = className.toLowerCase();
        
        return switch(this) {
            case BUTTON -> classes.contains("button") || classes.contains("btn");
            case LINK -> classes.contains("link");
            case CHECKBOX -> classes.contains("checkbox") || classes.contains("check");
            case RADIO -> classes.contains("radio");
            case SELECT, DROPDOWN -> classes.contains("select") || classes.contains("dropdown") || 
                                      classes.contains("combobox");
            case INPUT -> classes.contains("input") || classes.contains("textbox") || 
                         classes.contains("field");
            case MODAL -> classes.contains("modal") || classes.contains("dialog");
            case ALERT -> classes.contains("alert") || classes.contains("notification");
            case TOOLTIP -> classes.contains("tooltip") || classes.contains("popover");
            case TAB -> classes.contains("tab");
            case MENU -> classes.contains("menu") || classes.contains("nav");
            case ICON -> classes.contains("icon") || classes.contains("fa-") || 
                        classes.contains("material-icons");
            case SLIDER -> classes.contains("slider") || classes.contains("range");
            default -> false;
        };
    }
    
    
    /**
     * Is this an interactive element (can be clicked/focused)?
     */
    public boolean isInteractive() {
        return interactive;
    }
    
    /**
     * Is this a form element (part of forms)?
     */
    public boolean isFormElement() {
        return formElement;
    }
    
    /**
     * Get common attributes for this element type.
     */
    public Set<String> getCommonAttributes() {
        return commonAttributes;
    }
    
    /**
     * Get possible type attribute values.
     */
    public Set<String> getTypeValues() {
        return typeValues;
    }
    
    // ========================================
    // CATEGORIZATION METHODS
    // ========================================
    
    /**
     * Is this typically clickable?
     */
    public boolean isClickable() {
        return this == BUTTON || 
               this == LINK || 
               this == CHECKBOX || 
               this == RADIO ||
               this == ICON ||
               this == MENUITEM ||
               this == TAB;
    }
    
    /**
     * Can this receive text input?
     */
    public boolean isTextInput() {
        return this == INPUT || 
               this == TEXTAREA;
    }
    
    /**
     * Is this a selection element (checkbox, radio, select)?
     */
    public boolean isSelectable() {
        return this == CHECKBOX ||
               this == RADIO ||
               this == SELECT ||
               this == DROPDOWN ||
               this == OPTION;
    }
    
    /**
     * Is this a display-only element (no interaction)?
     */
    public boolean isDisplayOnly() {
        return !interactive;
    }
    
    /**
     * Is this a container element?
     */
    public boolean isContainer() {
        return this == DIV ||
               this == FORM ||
               this == FIELDSET ||
               this == TABLE ||
               this == LIST ||
               this == MODAL ||
               this == TABPANEL;
    }
    
    /**
     * Is this a media element?
     */
    public boolean isMedia() {
        return this == IMAGE ||
               this == VIDEO ||
               this == AUDIO ||
               this == ICON;
    }
    
    /**
     * Is this a table-related element?
     */
    public boolean isTableElement() {
        return this == TABLE ||
               this == TABLE_ROW ||
               this == TABLE_CELL;
    }
    
    /**
     * Is this a list-related element?
     */
    public boolean isListElement() {
        return this == LIST ||
               this == LIST_ITEM ||
               this == MENU ||
               this == MENUITEM;
    }
    
    // ========================================
    // VALIDATION METHODS
    // ========================================
    
    /**
     * Check if an attribute is common for this element type.
     */
    public boolean hasAttribute(String attributeName) {
        return commonAttributes.contains(attributeName.toLowerCase());
    }
    
    /**
     * Check if a type value is valid for this element type.
     */
    public boolean hasTypeValue(String typeValue) {
        return typeValues.isEmpty() || typeValues.contains(typeValue.toLowerCase());
    }
    
    /**
     * Is this element type compatible with the given action?
     */
    public boolean isCompatibleWith(ActionType action) {
        return action.isCompatibleWith(this);
    }
    
    /**
     * Get validation message if incompatible with action.
     */
    public String getIncompatibilityReason(ActionType action) {
        if (isCompatibleWith(action)) {
            return null;
        }
        
        return String.format("Action '%s' is not compatible with element type '%s'. " +
                           "Expected types: %s", 
                           action, this, action.getExpectedElementTypes());
    }
    
    // ========================================
    // HELPER METHODS
    // ========================================
    
    /**
     * Intelligently detect ElementType from element attributes.
     * PRIORITY: ARIA role > Tag name > Class patterns
     * 
     * This handles both native HTML and custom styled elements.
     */
    public static ElementType detectFromElement(String tagName, String role, String type, 
                                               String className, Map<String, String> computedStyle) {
        // 1. ARIA role has highest priority (catches custom implementations)
        if (role != null && !role.isEmpty()) {
            ElementType fromRole = fromAriaRole(role);
            if (fromRole != ANY) {
                return fromRole;
            }
        }
        
        // 2. CSS Cursor Hint (Strong signal for buttons/links)
        if (computedStyle != null && "pointer".equalsIgnoreCase(computedStyle.get("cursor"))) {
            if ("a".equalsIgnoreCase(tagName)) return LINK;
            return BUTTON; // Most clickable non-links are effective buttons
        }
        
        // 3. For <input>, check type attribute
        if ("input".equalsIgnoreCase(tagName) && type != null) {
            String inputType = type.toLowerCase();
            return switch(inputType) {
                case "checkbox" -> CHECKBOX;
                case "radio" -> RADIO;
                case "text", "email", "password", "number", "tel", "url", "search" -> INPUT;
                case "submit", "button", "reset" -> BUTTON;
                case "file" -> INPUT; // File upload
                case "range" -> SLIDER;
                case "date", "datetime-local", "time", "month", "week" -> INPUT;
                default -> INPUT;
            };
        }
        
        // 3. Check tag name
        ElementType fromTag = fromTagName(tagName);
        if (fromTag != ANY) {
            return fromTag;
        }
        
        // 4. Check class patterns (for custom styled elements)
        if (className != null && !className.isEmpty()) {
            String classes = className.toLowerCase();
            
            // Common class patterns for custom elements
            if (classes.contains("radio")) return RADIO;
            if (classes.contains("checkbox")) return CHECKBOX;
            if (classes.contains("select") || classes.contains("dropdown")) return SELECT;
            if (classes.contains("button") || classes.contains("btn")) return BUTTON;
            if (classes.contains("link")) return LINK;
            if (classes.contains("icon")) return ICON;
            if (classes.contains("modal") || classes.contains("dialog")) return MODAL;
            if (classes.contains("alert")) return ALERT;
            if (classes.contains("menu")) return MENU;
            if (classes.contains("tab")) return TAB;
            if (classes.contains("slider")) return SLIDER;
        }
        
        return ANY;
    }
    
    /**
     * Find ElementType from tag name only.
     */
    public static ElementType fromTagName(String tagName) {
        if (tagName == null || tagName.isEmpty()) {
            return ANY;
        }
        
        String tag = tagName.toLowerCase();
        return switch(tag) {
            case "button" -> BUTTON;
            case "a" -> LINK;
            case "input" -> INPUT; // Caller should check type attribute or use detectFromElement()
            case "textarea" -> TEXTAREA;
            case "select" -> SELECT;
            case "option" -> OPTION;
            case "label" -> LABEL;
            case "p" -> PARAGRAPH;
            case "h1", "h2", "h3", "h4", "h5", "h6" -> HEADING;
            case "span" -> SPAN;
            case "div" -> DIV;
            case "img" -> IMAGE;
            case "video" -> VIDEO;
            case "audio" -> AUDIO;
            case "table" -> TABLE;
            case "tr" -> TABLE_ROW;
            case "td", "th" -> TABLE_CELL;
            case "ul", "ol" -> LIST;
            case "li" -> LIST_ITEM;
            case "form" -> FORM;
            case "fieldset" -> FIELDSET;
            case "legend" -> LEGEND;
            case "iframe" -> IFRAME;
            case "frame" -> FRAME;
            case "i", "svg" -> ICON;
            case "nav" -> MENU;
            case "progress" -> PROGRESS;
            default -> ANY;
        };
    }
    
    /**
     * Find ElementType from ARIA role.
     */
    public static ElementType fromAriaRole(String role) {
        if (role == null || role.isEmpty()) {
            return ANY;
        }
        
        String ariaRole = role.toLowerCase();
        
        // Direct role matching
        return switch(ariaRole) {
            case "button" -> BUTTON;
            case "link" -> LINK;
            case "textbox" -> INPUT;
            case "checkbox" -> CHECKBOX;
            case "radio" -> RADIO;
            case "listbox", "combobox" -> SELECT;
            case "option" -> OPTION;
            case "heading" -> HEADING;
            case "img", "image" -> IMAGE;
            case "table" -> TABLE;
            case "row" -> TABLE_ROW;
            case "cell", "gridcell" -> TABLE_CELL;
            case "list" -> LIST;
            case "listitem" -> LIST_ITEM;
            case "form" -> FORM;
            case "group" -> FIELDSET;
            case "dialog", "alertdialog" -> MODAL;
            case "alert" -> ALERT;
            case "menu" -> MENU;
            case "menuitem" -> MENUITEM;
            case "tab" -> TAB;
            case "tabpanel" -> TABPANEL;
            case "slider" -> SLIDER;
            case "progressbar" -> PROGRESS;
            case "tooltip" -> TOOLTIP;
            default -> {
                // Check if role matches any enum's ariaRole field
                for (ElementType type : values()) {
                    if (type.ariaRole.equals(ariaRole)) {
                        yield type;
                    }
                }
                yield ANY;
            }
        };
    }
    
    /**
     * Get human-readable name.
     */
    public String getDisplayName() {
        return name().toLowerCase().replace('_', ' ');
    }
}
