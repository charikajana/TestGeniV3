package testgeni.v3.parser;

import testgeni.v3.core.domain.ActionType;
import java.util.*;

/**
 * Registry of all verb variations that map to each ActionType.
 * 
 * Purpose:
 * - Centralized place for all natural language variations
 * - Easy to maintain and extend verb mappings
 * - Used by StepParser to recognize user intent from different phrasings
 * 
 * Design:
 * - Immutable mappings loaded once at startup
 * - **Case-insensitive matching** (CLICK, Click, click all work)
 * - Supports multi-word verbs ("go to", "click on", "should see")
 * - Longest match wins (prefers "go to" over "go")
 * 
 * Examples:
 * - "I CLICK Login" → ActionType.CLICK
 * - "i click login" → ActionType.CLICK
 * - "I cLiCk login" → ActionType.CLICK
 * 
 * @author TestGeni v3
 */
public class ActionVerbRegistry {
    
    private static final Map<ActionType, String[]> VERB_MAPPINGS = initializeVerbMappings();
    
    /**
     * Initialize all verb-to-action mappings.
     * Called once during class loading.
     */
    private static Map<ActionType, String[]> initializeVerbMappings() {
        Map<ActionType, String[]> mappings = new EnumMap<>(ActionType.class);
        
        // Navigation verbs
        mappings.put(ActionType.NAVIGATE, new String[]{
            "navigate", "go to", "open", "visit", "load", "browse to", "access", "launch", "start"
        });
        mappings.put(ActionType.REFRESH, new String[]{
            "refresh", "reload", "reopen"
        });
        mappings.put(ActionType.GO_BACK, new String[]{
            "go back", "back", "navigate back", "return"
        });
        mappings.put(ActionType.GO_FORWARD, new String[]{
            "go forward", "forward", "navigate forward"
        });
        
        // Mouse action verbs
        mappings.put(ActionType.CLICK, new String[]{
            "click", "click on", "tap", "press", "hit", "select", "push", "activate", "trigger"
        });
        mappings.put(ActionType.DOUBLE_CLICK, new String[]{
            "double click", "double-click", "double tap", "dbl click"
        });
        mappings.put(ActionType.RIGHT_CLICK, new String[]{
            "right click", "right-click", "context click", "right tap", "open context menu"
        });
        mappings.put(ActionType.HOVER, new String[]{
            "hover", "hover over", "mouse over", "move to", "point to"
        });
        mappings.put(ActionType.DRAG_DROP, new String[]{
            "drag", "drag and drop", "drag to", "move", "drag from"
        });
        
        // Input action verbs
        mappings.put(ActionType.FILL, new String[]{
            "fill", "enter", "input", "set", "populate", "provide", "supply", "put", "insert", "add", "type into"
        });
        mappings.put(ActionType.TYPE, new String[]{
            "type", "type in", "write", "key in"
        });
        mappings.put(ActionType.CLEAR, new String[]{
            "clear", "empty", "erase", "remove text", "delete text", "reset"
        });
        mappings.put(ActionType.PRESS_KEY, new String[]{
            "press", "hit", "press key", "type key", "push", "strike"
        });
        
        // Selection action verbs  
        mappings.put(ActionType.CHECK, new String[]{
            "check", "tick", "mark", "enable", "activate", "turn on", "set"
        });
        mappings.put(ActionType.UNCHECK, new String[]{
            "uncheck", "untick", "unmark", "disable", "deactivate", "turn off", "unset"
        });
        mappings.put(ActionType.SELECT, new String[]{
            "select", "choose", "pick", "set", "opt for"
        });
        mappings.put(ActionType.DESELECT, new String[]{
            "deselect", "unselect", "remove selection", "clear selection"
        });
        mappings.put(ActionType.REMOVE, new String[]{
            "remove", "delete item", "unpick"
        });
        
        // Verification action verbs
        mappings.put(ActionType.VERIFY, new String[]{
            "verify", "check", "assert", "validate", "confirm", "ensure", "expect", 
            "see", "find", "should see", "should be", "is", "has", "contains",
            "must", "should", "displays", "shows"
        });
        
        // Context switch verbs
        mappings.put(ActionType.SWITCH_FRAME, new String[]{
            "switch to frame", "switch frame", "switch to iframe", "go to frame", "enter frame"
        });
        mappings.put(ActionType.SWITCH_WINDOW, new String[]{
            "switch to window", "switch window", "change window", "go to window",
            "switch to", "switch", "go to"
        });
        mappings.put(ActionType.SWITCH_TAB, new String[]{
            "switch to tab", "switch tab", "change tab", "go to tab"
        });
        mappings.put(ActionType.CLOSE_WINDOW, new String[]{
            "close window", "close tab", "close", "exit"
        });
        
        // Utility action verbs
        mappings.put(ActionType.WAIT, new String[]{
            "wait for page load", "wait for", "wait", "pause", "sleep", "hold", "delay"
        });
        mappings.put(ActionType.SCROLL, new String[]{
            "scroll", "scroll to", "scroll down", "scroll up", "scroll into view"
        });
        mappings.put(ActionType.SCREENSHOT, new String[]{
            "screenshot", "capture", "take screenshot", "snap", "snapshot", "capture screen"
        });
        mappings.put(ActionType.UPLOAD_FILE, new String[]{
            "upload", "upload file", "attach", "attach file", "choose file"
        });
        
        // Dialog actions
        mappings.put(ActionType.HANDLE_ALERT, new String[]{
            "verify alert", "verify and accept alert", "accept alert", "accept confirm", "accept prompt",
            "dismiss alert", "dismiss confirm", "dismiss prompt", "enter in prompt",
            "verify alert says", "verify confirm says", "confirm alert", "confirm confirm"
        });
        
        return Collections.unmodifiableMap(mappings);
    }
    
    /**
     * Get all verb variations for a given action type.
     * 
     * @param actionType the action type
     * @return array of verb variations (all lowercase)
     */
    public static String[] getVerbs(ActionType actionType) {
        String[] verbs = VERB_MAPPINGS.get(actionType);
        return verbs != null ? verbs : new String[0];
    }
    
    /**
     * Find which ActionType matches the given text.
     * Uses word boundary matching to avoid false positives.
     * 
     * @param text the text to analyze (e.g., "I click Login button")
     * @return matched ActionType or null if no match
     */
    public static ActionType findActionInText(String text) {
        if (text == null || text.isEmpty()) {
            return null;
        }
        
        String lowerText = text.toLowerCase();
        
        // Try to find longest matching verb first (to prefer "go to" over "go")
        ActionType bestMatch = null;
        int longestMatch = 0;
        boolean bestIsWordBoundary = false;
        
        for (Map.Entry<ActionType, String[]> entry : VERB_MAPPINGS.entrySet()) {
            ActionType currentAction = entry.getKey();
            for (String verb : entry.getValue()) {
                // Check for word boundary match first (preferred)
                String wordBoundaryPattern = "\\b" + java.util.regex.Pattern.quote(verb) + "\\b";
                boolean isWordBoundaryMatch = lowerText.matches(".*" + wordBoundaryPattern + ".*");
                
                // Also check simple contains for backwards compatibility
                boolean isContainsMatch = lowerText.contains(verb);
                
                if (isWordBoundaryMatch || isContainsMatch) {
                    boolean shouldUpdate = false;
                    
                    // Prefer word boundary matches over contains matches
                    if (isWordBoundaryMatch && !bestIsWordBoundary) {
                        shouldUpdate = true;
                    }
                    // If both are word boundary or both are contains
                    else if (isWordBoundaryMatch == bestIsWordBoundary) {
                        // Prefer longer matches
                        if (verb.length() > longestMatch) {
                            shouldUpdate = true;
                        }
                        // For ties, use priority
                        else if (verb.length() == longestMatch && longestMatch > 0) {
                            if (isHigherPriorityThan(currentAction, bestMatch)) {
                                shouldUpdate = true;
                            }
                        }
                    }
                    
                    if (shouldUpdate) {
                        bestMatch = currentAction;
                        longestMatch = verb.length();
                        bestIsWordBoundary = isWordBoundaryMatch;
                    }
                }
            }
        }
        
        return bestMatch;
    }
    
    /**
     * Priority for resolving verb conflicts of same length.
     * MOUSE/INPUT/SELECTION > NAVIGATION > CONTEXT > UTILITY > VERIFICATION
     */
    private static boolean isHigherPriorityThan(ActionType current, ActionType existing) {
        if (existing == null) return true;
        
        int currentScore = getCategoryPriority(current.getCategory());
        int existingScore = getCategoryPriority(existing.getCategory());
        
        return currentScore > existingScore;
    }
    
    private static int getCategoryPriority(ActionType.ActionCategory category) {
        return switch (category) {
            case DIALOG -> 6;
            case MOUSE, INPUT, SELECTION -> 5;
            case NAVIGATION -> 4;
            case CONTEXT -> 3;
            case UTILITY -> 2;
            case VERIFICATION -> 1;
            default -> 0;
        };
    }
    
    /**
     * Check if a specific verb matches an action type.
     * 
     * @param verb the verb to check (case-insensitive)
     * @param actionType the action type to match
     * @return true if verb matches this action
     */
    public static boolean isVerbForAction(String verb, ActionType actionType) {
        if (verb == null || actionType == null) {
            return false;
        }
        
        String lowerVerb = verb.toLowerCase();
        String[] verbs = getVerbs(actionType);
        
        for (String v : verbs) {
            if (v.equals(lowerVerb)) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Get all unique verbs across all action types.
     * Useful for documentation or debugging.
     * 
     * @return set of all verbs
     */
    public static Set<String> getAllVerbs() {
        Set<String> allVerbs = new HashSet<>();
        for (String[] verbs : VERB_MAPPINGS.values()) {
            allVerbs.addAll(Arrays.asList(verbs));
        }
        return Collections.unmodifiableSet(allVerbs);
    }
}
