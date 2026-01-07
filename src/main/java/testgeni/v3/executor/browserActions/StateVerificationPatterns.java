package testgeni.v3.executor.browserActions;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Centralized repository for state verification patterns and synonyms.
 * Maps various natural language phrasings to canonical state attributes.
 * 
 * @author TestGeni v3
 * @version 3.0.0
 * @since 2026-01-07
 */
public class StateVerificationPatterns {
    
    // ========================================
    // ENABLED/ACTIVE STATE SYNONYMS
    // ========================================
    
    /**
     * Synonyms for "enabled" state.
     * All map to canonical: "enabled"
     */
    public static final Set<String> ENABLED_SYNONYMS = Set.of(
        "enabled", "isenabled", "is enabled",
        "active", "isactive", "is active",
        "clickable", "isclickable", "is clickable",
        "interactive", "isinteractive", "is interactive",
        "available", "isavailable", "is available"
    );
    
    /**
     * Synonyms for "disabled" state.
     * All map to canonical: "disabled"
     */
    public static final Set<String> DISABLED_SYNONYMS = Set.of(
        "disabled", "isdisabled", "is disabled",
        "greyed out", "greyedout", "is greyed out",
        "grayed out", "grayedout", "is grayed out",
        "inactive", "isinactive", "is inactive",
        "readonly", "is readonly"
    );
    
    // ========================================
    // SELECTION STATE SYNONYMS
    // ========================================
    
    /**
     * Synonyms for "selected/checked" state.
     * All map to canonical: "selected"
     */
    public static final Set<String> SELECTED_SYNONYMS = Set.of(
        "selected", "isselected", "is selected",
        "checked", "ischecked", "is checked",
        "on", "ison", "is on",
        "chosen", "ischosen", "is chosen",
        "picked", "ispicked", "is picked",
        "marked", "ismarked", "is marked"
    );
    
    /**
     * Synonyms for "not selected/unchecked" state.
     * All map to canonical: "unselected"
     */
    public static final Set<String> UNSELECTED_SYNONYMS = Set.of(
        "unselected", "isunselected", "is unselected",
        "unchecked", "isunchecked", "is unchecked",
        "off", "isoff", "is off",
        "unmarked", "isunmarked", "is unmarked",
        "deselected", "isdeselected", "is deselected"
    );
    
    // ========================================
    // VISIBILITY STATE SYNONYMS
    // ========================================
    
    /**
     * Synonyms for "visible" state.
     * All map to canonical: "visible"
     */
    public static final Set<String> VISIBLE_SYNONYMS = Set.of(
        "visible", "isvisible", "is visible",
        "displayed", "isdisplayed", "is displayed",
        "shown", "isshown", "is shown",
        "present", "ispresent", "is present",
        "exists", "isexists", "is exists"
    );
    
    /**
     * Synonyms for "hidden" state.
     * All map to canonical: "hidden"
     */
    public static final Set<String> HIDDEN_SYNONYMS = Set.of(
        "hidden", "ishidden", "is hidden",
        "invisible", "isinvisible", "is invisible",
        "absent", "isabsent", "is absent"
    );
    
    // ========================================
    // COMBINED SETS
    // ========================================
    
    /**
     * All state synonyms combined.
     */
    public static final Set<String> ALL_STATE_SYNONYMS = 
        Stream.of(ENABLED_SYNONYMS, DISABLED_SYNONYMS, SELECTED_SYNONYMS, 
                  UNSELECTED_SYNONYMS, VISIBLE_SYNONYMS, HIDDEN_SYNONYMS)
            .flatMap(Set::stream)
            .collect(Collectors.toUnmodifiableSet());
    
    // ========================================
    // CANONICAL STATE MAPPING
    // ========================================
    
    /**
     * Map synonyms to canonical state names.
     * 
     * @param synonym State synonym from step
     * @return Canonical state name
     */
    public static String getCanonicalState(String synonym) {
        if (synonym == null) return null;
        
        String normalized = synonym.toLowerCase()
            .replaceAll("\\s+", " ")
            .trim();
        
        // Enabled states
        if (ENABLED_SYNONYMS.contains(normalized)) {
            return "enabled";
        }
        
        // Disabled states
        if (DISABLED_SYNONYMS.contains(normalized)) {
            return "disabled";
        }
        
        // Selected states
        if (SELECTED_SYNONYMS.contains(normalized)) {
            return "selected";
        }
        
        // Unselected states
        if (UNSELECTED_SYNONYMS.contains(normalized)) {
            return "unselected";
        }
        
        // Visible states
        if (VISIBLE_SYNONYMS.contains(normalized)) {
            return "visible";
        }
        
        // Hidden states
        if (HIDDEN_SYNONYMS.contains(normalized)) {
            return "hidden";
        }
        
        return null;
    }
    
    /**
     * Check if a string is a state attribute.
     * 
     * @param text Text to check
     * @return true if it's a recognized state synonym
     */
    public static boolean isStateAttribute(String text) {
        if (text == null) return false;
        String normalized = text.toLowerCase().replaceAll("\\s+", " ").trim();
        return ALL_STATE_SYNONYMS.contains(normalized);
    }
    
    /**
     * Get the state category (for routing to correct verifier).
     * 
     * @param canonicalState Canonical state name
     * @return State category: "enablement", "selection", "visibility"
     */
    public static String getStateCategory(String canonicalState) {
        if (canonicalState == null) return null;
        
        return switch (canonicalState) {
            case "enabled", "disabled" -> "enablement";
            case "selected", "unselected" -> "selection";
            case "visible", "hidden" -> "visibility";
            default -> null;
        };
    }
    
    /**
     * Private constructor to prevent instantiation.
     */
    private StateVerificationPatterns() {
        throw new UnsupportedOperationException("StateVerificationPatterns is a utility class");
    }
}
