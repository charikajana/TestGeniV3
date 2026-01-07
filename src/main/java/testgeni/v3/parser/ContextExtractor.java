package testgeni.v3.parser;

import java.util.regex.Matcher;

/**
 * Extracts contextual information from Gherkin steps such as scoping, 
 * frames, table information, and indices.
 */
public class ContextExtractor {

    /**
     * Extract scoping context (inside/within).
     */
    public String extractScoping(String step) {
        Matcher matcher = RegexPatterns.SCOPING.matcher(step);
        if (matcher.find()) {
            return matcher.group(2).trim();
        }
        return null;
    }

    /**
     * Extract frame anchor.
     */
    public String extractFrame(String step) {
        Matcher matcher = RegexPatterns.FRAME.matcher(step);
        if (matcher.find()) {
            return matcher.group(2);
        }
        return null;
    }

    /**
     * Extract table row condition.
     */
    public String extractTableRow(String step) {
        Matcher matcher = RegexPatterns.TABLE_ROW.matcher(step);
        if (matcher.find()) {
            return matcher.group(1) + " is '" + matcher.group(2) + "'";
        }
        return null;
    }

    /**
     * Extract table column.
     */
    public String extractTableColumn(String step) {
        Matcher matcher = RegexPatterns.TABLE_COLUMN.matcher(step);
        if (matcher.find()) {
            return matcher.group(1).trim();
        }
        return null;
    }

    /**
     * Extract tooltip reference.
     */
    public String extractTooltip(String step) {
        Matcher matcher = RegexPatterns.TOOLTIP.matcher(step);
        if (matcher.find()) {
            return matcher.group(1).trim();
        }
        return null;
    }

    /**
     * Extract alert/prompt message.
     */
    public String extractAlertMessage(String step) {
        // Check for "alert says"
        Matcher saysMatcher = RegexPatterns.ALERT_MESSAGE.matcher(step);
        if (saysMatcher.find()) {
            return saysMatcher.group(1);
        }
        
        // Check for "accept alert with message"
        Matcher acceptMatcher = RegexPatterns.ACCEPT_ALERT.matcher(step);
        if (acceptMatcher.find()) {
            return acceptMatcher.group(3);
        }
        
        // Check for "Enter in prompt"
        Matcher promptMatcher = RegexPatterns.ENTER_PROMPT.matcher(step);
        if (promptMatcher.find()) {
            return promptMatcher.group(1);
        }
        
        return null;
    }

    /**
     * Extract window count for verification.
     */
    public Integer extractWindowCount(String step) {
        Matcher matcher = RegexPatterns.WINDOW_COUNT.matcher(step);
        if (matcher.find()) {
            try {
                return Integer.parseInt(matcher.group(1));
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }

    /**
     * Extract element index (1st, 2nd, first, second, etc.).
     */
    public Integer extractElementIndex(String step) {
        Matcher matcher = RegexPatterns.ELEMENT_INDEX.matcher(step);
        if (matcher.find()) {
            // Check numeric ordinal (1st, 2nd...)
            if (matcher.group(1) != null) {
                return Integer.parseInt(matcher.group(1));
            }
            
            // Check word ordinal
            String word = matcher.group(3).toLowerCase();
            return switch (word) {
                case "first" -> 1;
                case "second" -> 2;
                case "third" -> 3;
                case "fourth" -> 4;
                case "fifth" -> 5;
                case "last" -> -1; // Special value for last
                default -> null;
            };
        }
        return null;
    }

    /**
     * Extracts the specific item value to be removed in a REMOVE action.
     */
    public String extractRemoveValue(String step) {
        Matcher matcher = RegexPatterns.REMOVE_ITEM.matcher(step);
        if (matcher.find()) {
            return matcher.group(1).trim().replace("\"", "").replace("'", "");
        }
        return null;
    }
}
