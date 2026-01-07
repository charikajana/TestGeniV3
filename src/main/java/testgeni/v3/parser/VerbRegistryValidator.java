package testgeni.v3.parser;

import testgeni.v3.core.domain.ActionType;
import testgeni.v3.util.V3Logger;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Validation and debugging utilities for ActionVerbRegistry.
 * 
 * Purpose:
 * - Detect conflicts and issues in verb mappings
 * - Provide debugging information
 * - Generate example steps
 * - Validate registry integrity on startup
 * 
 * Design:
 * - Separate class from ActionVerbRegistry (no risk of breaking it)
 * - Static utility methods
 * - Can be called during application startup for validation
 * 
 * @author TestGeni v3
 */
public class VerbRegistryValidator {
    
    /**
     * Validate all verb mappings for common issues.
     * Should be called during application startup.
     * 
     * @throws IllegalStateException if validation fails
     */
    public static void validateRegistry() {
        List<String> errors = new ArrayList<>();
        
        // Check 1: Every action has at least one verb
        for (ActionType action : ActionType.values()) {
            String[] verbs = ActionVerbRegistry.getVerbs(action);
            if (verbs == null || verbs.length == 0) {
                errors.add("ActionType." + action + " has no verbs defined");
            }
        }
        
        // Check 2: No null or empty verbs
        for (ActionType action : ActionType.values()) {
            String[] verbs = ActionVerbRegistry.getVerbs(action);
            if (verbs != null) {
                for (int i = 0; i < verbs.length; i++) {
                    if (verbs[i] == null) {
                        errors.add("ActionType." + action + " has null verb at index " + i);
                    } else if (verbs[i].trim().isEmpty()) {
                        errors.add("ActionType." + action + " has empty verb at index " + i);
                    }
                }
            }
        }
        
        // Check 3: All verbs are lowercase (for consistency)
        for (ActionType action : ActionType.values()) {
            String[] verbs = ActionVerbRegistry.getVerbs(action);
            if (verbs != null) {
                for (String verb : verbs) {
                    if (verb != null && !verb.equals(verb.toLowerCase())) {
                        errors.add("Verb '" + verb + "' for " + action + " is not lowercase");
                    }
                }
            }
        }
        
        // Check 4: No duplicate verbs within same action
        for (ActionType action : ActionType.values()) {
            String[] verbs = ActionVerbRegistry.getVerbs(action);
            if (verbs != null) {
                Set<String> seen = new HashSet<>();
                for (String verb : verbs) {
                    if (verb != null && !seen.add(verb)) {
                        errors.add("Duplicate verb '" + verb + "' in " + action);
                    }
                }
            }
        }
        
        // If any errors found, throw exception
        if (!errors.isEmpty()) {
            throw new IllegalStateException(
                "ActionVerbRegistry validation failed:\n" + 
                String.join("\n", errors)
            );
        }
    }
    
    /**
     * Find verbs that are used by multiple action types.
     * These might cause ambiguity in parsing.
     * 
     * @return map of conflicting verb to list of actions that use it
     */
    public static Map<String, List<ActionType>> findConflicts() {
        Map<String, List<ActionType>> verbToActions = new HashMap<>();
        
        // Build reverse map: verb -> list of actions
        for (ActionType action : ActionType.values()) {
            String[] verbs = ActionVerbRegistry.getVerbs(action);
            if (verbs != null) {
                for (String verb : verbs) {
                    if (verb != null) {
                        verbToActions.computeIfAbsent(verb, k -> new ArrayList<>()).add(action);
                    }
                }
            }
        }
        
        // Filter only conflicts (verbs used by multiple actions)
        return verbToActions.entrySet().stream()
            .filter(entry -> entry.getValue().size() > 1)
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                Map.Entry::getValue
            ));
    }
    
    /**
     * Print a report of all conflicts found.
     * Useful for debugging and documentation.
     */
    public static void printConflictReport() {
        Map<String, List<ActionType>> conflicts = findConflicts();
        
        if (conflicts.isEmpty()) {
            V3Logger.success("No verb conflicts found!");
            return;
        }
        
        V3Logger.warn("Found " + conflicts.size() + " verb conflicts:");
        V3Logger.info("=" .repeat(60));
        
        conflicts.forEach((verb, actions) -> {
            V3Logger.info("\nVerb: \"" + verb + "\"");
            V3Logger.info("  Used by:");
            actions.forEach(action -> 
                V3Logger.info("    - " + action)
            );
        });
        
        V3Logger.info("\n" + "=".repeat(60));
        V3Logger.info("Note: Conflicts are resolved by longest-match-first strategy");
    }
    
    /**
     * Explain which verb matched for a given text.
     * Useful for debugging why a certain action was detected.
     * 
     * @param text the input text
     * @return explanation string
     */
    public static String explainMatch(String text) {
        if (text == null || text.isEmpty()) {
            return "No text provided";
        }
        
        String lowerText = text.toLowerCase();
        ActionType matched = ActionVerbRegistry.findActionInText(text);
        
        if (matched == null) {
            return "No action verb found in: \"" + text + "\"";
        }
        
        // Find which specific verb matched
        String[] verbs = ActionVerbRegistry.getVerbs(matched);
        String matchedVerb = null;
        int position = -1;
        
        for (String verb : verbs) {
            if (lowerText.contains(verb)) {
                int pos = lowerText.indexOf(verb);
                if (matchedVerb == null || verb.length() > matchedVerb.length()) {
                    matchedVerb = verb;
                    position = pos;
                }
            }
        }
        
        return String.format(
            "Matched: ActionType.%s\n" +
            "  Verb: \"%s\" (at position %d)\n" +
            "  Original text: \"%s\"",
            matched, matchedVerb, position, text
        );
    }
    
    /**
     * Generate example step for a given action type.
     * 
     * @param action the action type
     * @return example Gherkin step
     */
    public static String generateExampleStep(ActionType action) {
        String[] verbs = ActionVerbRegistry.getVerbs(action);
        if (verbs == null || verbs.length == 0) {
            return "No verbs defined for " + action;
        }
        
        // Use first verb as it's usually the most common
        String primaryVerb = verbs[0];
        
        return switch(action) {
            case NAVIGATE -> "When I " + primaryVerb + " \"https://example.com\"";
            case CLICK -> "When I " + primaryVerb + " Login button";
            case FILL -> "When I " + primaryVerb + " \"John\" in First Name";
            case VERIFY -> "Then I " + primaryVerb + " \"Welcome message\" is displayed";
            case CHECK -> "When I " + primaryVerb + " Remember Me checkbox";
            case UNCHECK -> "When I " + primaryVerb + " Newsletter checkbox";
            case SELECT -> "When I " + primaryVerb + " \"USA\" from Country dropdown";
            case WAIT -> "And I " + primaryVerb + " for 2 seconds";
            case HOVER -> "When I " + primaryVerb + " Tooltip icon";
            case SCREENSHOT -> "Then I " + primaryVerb;
            case DOUBLE_CLICK -> "When I " + primaryVerb + " File name";
            case RIGHT_CLICK -> "When I " + primaryVerb + " File menu";
            case PRESS_KEY -> "When I " + primaryVerb + " ENTER";
            case TYPE -> "When I " + primaryVerb + " \"Hello World\"";
            case CLEAR -> "When I " + primaryVerb + " Search field";
            case SCROLL -> "When I " + primaryVerb + " Footer";
            case DRAG_DROP -> "When I " + primaryVerb + " Item to Cart";
            case UPLOAD_FILE -> "When I " + primaryVerb + " \"document.pdf\"";
            default -> "When I " + primaryVerb + " [target]";
        };
    }
    
    /**
     * Get statistics about verb coverage.
     * 
     * @return statistics report
     */
    public static String getStatistics() {
        int totalActions = ActionType.values().length;
        int totalVerbs = ActionVerbRegistry.getAllVerbs().size();
        int conflicts = findConflicts().size();
        
        // Find action with most verbs
        ActionType mostVerbs = null;
        int maxVerbs = 0;
        for (ActionType action : ActionType.values()) {
            int count = ActionVerbRegistry.getVerbs(action).length;
            if (count > maxVerbs) {
                maxVerbs = count;
                mostVerbs = action;
            }
        }
        
        // Find action with fewest verbs
        ActionType fewestVerbs = null;
        int minVerbs = Integer.MAX_VALUE;
        for (ActionType action : ActionType.values()) {
            int count = ActionVerbRegistry.getVerbs(action).length;
            if (count < minVerbs) {
                minVerbs = count;
                fewestVerbs = action;
            }
        }
        
        return String.format(
            "ActionVerbRegistry Statistics:\n" +
            "  Total Actions: %d\n" +
            "  Total Unique Verbs: %d\n" +
            "  Verb Conflicts: %d\n" +
            "  Most Verbs: %s (%d verbs)\n" +
            "  Fewest Verbs: %s (%d verbs)\n" +
            "  Average Verbs per Action: %.1f",
            totalActions, totalVerbs, conflicts,
            mostVerbs, maxVerbs,
            fewestVerbs, minVerbs,
            (double) totalVerbs / totalActions
        );
    }
    
    /**
     * Print all verbs for all actions in a readable format.
     * Useful for documentation.
     */
    public static void printAllMappings() {
        V3Logger.info("=".repeat(70));
        V3Logger.info("ACTION VERB MAPPINGS");
        V3Logger.info("=".repeat(70));
        
        for (ActionType action : ActionType.values()) {
            String[] verbs = ActionVerbRegistry.getVerbs(action);
            V3Logger.info(String.format("\n%-20s (%d verbs):", action, verbs.length));
            for (String verb : verbs) {
                V3Logger.info("  - " + verb);
            }
        }
        
        V3Logger.info("\n" + "=".repeat(70));
    }
    
    /**
     * Test parsing with various case combinations.
     * 
     * @param baseStep step in lowercase
     * @return true if all case variations parse to same action
     */
    public static boolean testCaseInsensitivity(String baseStep) {
        ActionType lowercase = ActionVerbRegistry.findActionInText(baseStep);
        ActionType uppercase = ActionVerbRegistry.findActionInText(baseStep.toUpperCase());
        ActionType titlecase = ActionVerbRegistry.findActionInText(toTitleCase(baseStep));
        
        return lowercase == uppercase && uppercase == titlecase;
    }
    
    private static String toTitleCase(String text) {
        String[] words = text.split(" ");
        StringBuilder result = new StringBuilder();
        for (String word : words) {
            if (!word.isEmpty()) {
                result.append(Character.toUpperCase(word.charAt(0)))
                      .append(word.substring(1).toLowerCase())
                      .append(" ");
            }
        }
        return result.toString().trim();
    }
}
