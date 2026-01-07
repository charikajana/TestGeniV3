package testgeni.v3.parser;

import testgeni.v3.core.domain.ActionType;
import testgeni.v3.executor.browserActions.StateVerificationPatterns;
import testgeni.v3.executor.browserActions.StateVerifierRegistry;
import testgeni.v3.util.V3Logger;

import java.util.regex.Matcher;

/**
 * Specializes generic actions (like VERIFY) into more specific ones 
 * based on the step context and attributes.
 */
public class IntentSpecializer {

    private final StateVerifierRegistry stateVerifierRegistry;

    public IntentSpecializer() {
        this.stateVerifierRegistry = new StateVerifierRegistry();
    }

    /**
     * Specializes the action based on the verification attribute.
     */
    public ActionType specialize(ActionType action, String cleanStep, String verificationAttribute) {
        if (action == ActionType.VERIFY) {
            if (stateVerifierRegistry.isStateVerification(verificationAttribute)) {
                V3Logger.trace("Action Specialized", "VERIFY -> VERIFY_STATE (" + verificationAttribute + ")");
                return ActionType.VERIFY_STATE;
            } else {
                V3Logger.trace("Action Specialized", "VERIFY -> VERIFY_TEXT (" + verificationAttribute + ")");
                return ActionType.VERIFY_TEXT;
            }
        }
        return action;
    }

    /**
     * Extract verification attribute (displayed, enabled, selected, etc.).
     */
    public String extractVerificationAttribute(String step) {
        String lowerStep = step.toLowerCase();
        
        // PRIORITY 1: Check for STATE attributes (boolean checks)
        for (String synonym : StateVerificationPatterns.ALL_STATE_SYNONYMS) {
            if (lowerStep.matches(".*\\b" + java.util.regex.Pattern.quote(synonym) + "\\b.*")) {
                return synonym;
            }
        }
        
        // PRIORITY 2: Check for CONTENT attributes (text/value checks)
        for (String attr : RegexPatterns.VERIFICATION_ATTRIBUTES) {
            if (lowerStep.contains(attr)) {
                return attr;
            }
        }
        
        // Default: element existence check
        return "displayed";
    }

    /**
     * Extract URL modifier for verification (contains, is exactly, etc.).
     */
    public String extractUrlModifier(String step) {
        Matcher matcher = RegexPatterns.URL_VERIFICATION.matcher(step);
        if (matcher.find()) {
            String modifier = matcher.group(1).toLowerCase();
            return switch (modifier) {
                case "is exactly" -> "exact";
                case "contains" -> "contains";
                case "matches" -> "matches";
                default -> "contains";
            };
        }
        return null;
    }
}
