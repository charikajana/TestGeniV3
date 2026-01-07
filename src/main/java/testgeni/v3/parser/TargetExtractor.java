package testgeni.v3.parser;

import testgeni.v3.core.domain.ActionType;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Extracts the target element description from a Gherkin step.
 */
public class TargetExtractor {

    private final ActionVerbRegistry verbRegistry;

    public TargetExtractor(ActionVerbRegistry verbRegistry) {
        this.verbRegistry = verbRegistry;
    }

    public String extract(String cleanStep, ActionType action, List<String> quotedValues, String primaryValue) {
        String step = cleanStep;
        
        // Stage 1: Filter out known non-target content
        // Remove action verb
        for (String verb : verbRegistry.getVerbs(action)) {
            step = step.replaceFirst("(?i)\\b" + Pattern.quote(verb) + "\\b", "");
        }

        // Remove switch window phrasing early
        step = RegexPatterns.CLICK_AND_SWITCH.matcher(step).replaceAll("");
        
        // Remove prepositions and noise words
        step = NoiseWords.STANDARD_NOISE_PATTERN.matcher(step).replaceAll(" ");
        
        // Remove descriptive keywords that aren't part of the target name
        step = NoiseWords.TECHNICAL_TERMS_PATTERN.matcher(step).replaceAll(" ");
        
        // Remove scoping keywords
        step = NoiseWords.SCOPING_REMOVAL_PATTERN.matcher(step).replaceAll("");
        
        // Remove frame keywords
        step = NoiseWords.FRAME_REMOVAL_PATTERN.matcher(step).replaceAll("");
        
        // Remove table keywords
        step = step.replaceAll("(?i)in\\s+the\\s+row\\s+.+$", "");
        step = step.replaceAll("(?i)in\\s+.+?\\s+column", "");
        
        // Remove verification attributes
        for (String attr : RegexPatterns.VERIFICATION_ATTRIBUTES) {
            step = step.replaceAll("(?i)\\b" + Pattern.quote(attr) + "\\b", " ");
        }
        
        // Remove negation words
        step = step.replaceAll("(?i)\\b(not|n't)\\b", "");

        // Stage 2: Handle Quoted Strings
        // If the remaining descriptive text is empty, then one of the quoted strings MUST be the target
        String descriptiveText = step.replaceAll("\\s+", " ").trim();
        descriptiveText = removeQuotes(descriptiveText, quotedValues).trim();

        if (descriptiveText.isEmpty() && !quotedValues.isEmpty()) {
            // Heuristic:
            // 1. For VERIFY_STATE/CLICK/SELECT with 1 quote, that quote is the target
            // 2. For FILL with 2 quotes, the 2nd is the target
            // 3. For VERIFY with 2 quotes, the 1st is the target (Verify "Name" is "John")
            if (quotedValues.size() == 1) {
                return quotedValues.get(0);
            } else if (quotedValues.size() >= 2) {
                if (action == ActionType.FILL || action == ActionType.TYPE) {
                    return quotedValues.get(1); // Usually: Enter "Value" in "Target"
                }
                return quotedValues.get(0); // Usually: Verify "Target" is "Value"
            }
        }
        
        // Final clean up of whitespace
        step = step.replaceAll("\\s+", " ").trim();
        step = removeQuotes(step, quotedValues).trim();
        
        return step.isEmpty() ? null : step;
    }

    private String removeQuotes(String text, List<String> quotedValues) {
        String result = text;
        for (String quoted : quotedValues) {
            result = result.replace("'" + quoted + "'", "");
            result = result.replace("\"" + quoted + "\"", "");
        }
        return result;
    }
}
