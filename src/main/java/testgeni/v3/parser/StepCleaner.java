package testgeni.v3.parser;

import java.util.regex.Matcher;

/**
 * Handles cleaning of Gherkin step text by removing keywords, 
 * pronouns, and normalizing whitespace.
 */
public class StepCleaner {

    /**
     * Clean step by removing Gherkin keywords and subject pronouns.
     */
    public String clean(String step) {
        if (step == null) return null;
        
        String cleaned = step.trim();
        
        // Remove Gherkin keyword (Given, When, Then, And, But)
        Matcher gherkinMatcher = RegexPatterns.GHERKIN_KEYWORD.matcher(cleaned);
        if (gherkinMatcher.find()) {
            cleaned = gherkinMatcher.replaceFirst("");
        }
        
        // Remove subject pronoun (I, USER, We, etc.)
        Matcher pronounMatcher = RegexPatterns.SUBJECT_PRONOUN.matcher(cleaned);
        if (pronounMatcher.find()) {
            cleaned = pronounMatcher.replaceFirst("");
        }
        
        return cleaned.trim();
    }
}
