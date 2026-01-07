package testgeni.v3.finder.keyword;

import java.util.Set;

/**
 * Constants used by keyword matching logic.
 */
public class KeywordConstants {
    
    public static final Set<String> STOP_WORDS = Set.of(
        "the", "a", "an", "is", "are", "was", "were", "be", "been", "being",
        "have", "has", "had", "do", "does", "did", "will", "would", "should",
        "may", "might", "can", "could", "to", "of", "in", "for", "on", "at",
        "by", "with", "from", "as", "into", "through", "during", "before", 
        "after", "above", "below", "between", "under", "again", "further",
        "then", "once", "here", "there", "when", "where", "why", "how", "all",
        "both", "each", "few", "more", "most", "other", "some", "such", "no",
        "nor", "not", "only", "own", "same", "so", "than", "too", "very"
    );

    public static final String VERIFICATION_PATTERN = ".*(displayed|visible|enabled|disabled|selected|checked|" +
                                                     "present|exists|url|title|text|value|empty|readonly|required).*";

    public static boolean isStopWord(String word) {
        return STOP_WORDS.contains(word.toLowerCase());
    }

    public static boolean isVerificationKeyword(String value) {
        String lower = value.toLowerCase();
        return lower.matches(VERIFICATION_PATTERN) || lower.length() > 50;
    }
}
