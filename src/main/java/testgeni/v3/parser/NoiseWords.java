package testgeni.v3.parser;

import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Centralized repository for noise words used in Gherkin step parsing.
 * Noise words are common words that don't contribute to element identification
 * and should be removed during target extraction.
 * 
 * @author TestGeni v3
 * @version 3.0.0
 * @since 2026-01-07
 */
public class NoiseWords {
    
    // ========================================
    // PREPOSITIONS & ARTICLES
    // ========================================
    
    /**
     * Common prepositions used in natural language that don't identify elements.
     * Examples: in, on, at, from, to, with, about, as, of, by, for
     */
    public static final Set<String> PREPOSITIONS = Set.of(
        "in", "into", "on", "from", "to", "at", "with", "about", "as", "of", "by", "for"
    );
    
    /**
     * Articles (definite and indefinite).
     * Examples: the, a, an
     */
    public static final Set<String> ARTICLES = Set.of(
        "the", "a", "an"
    );
    
    // ========================================
    // AUXILIARY VERBS & MODALS
    // ========================================
    
    /**
     * Common auxiliary verbs that don't contribute to element identification.
     * Examples: should, be, is, are, has, have
     */
    public static final Set<String> AUXILIARY_VERBS = Set.of(
        "should", "be", "is", "are", "has", "have", "was", "were", "been"
    );
    
    /**
     * Common verbs used in assertions/descriptions.
     * Examples: contain, contains
     */
    public static final Set<String> COMMON_VERBS = Set.of(
        "contain", "contains"
    );
    
    // ========================================
    // TECHNICAL TERMS
    // ========================================
    
    /**
     * Technical selector terms that are purely for locator strategies.
     * These should NOT appear in element names/descriptions.
     * Examples: css, xpath, attribute, role, class
     */
    public static final Set<String> TECHNICAL_TERMS = Set.of(
        "css", "xpath", "attribute", "role", "class"
    );
    
    // ========================================
    // SCOPING & CONTEXT KEYWORDS
    // ========================================
    
    /**
     * Keywords that indicate scoping/nesting but aren't part of element names.
     * Examples: inside, within
     * Note: 'in' is in PREPOSITIONS as it serves dual purpose
     */
    public static final Set<String> SCOPING_KEYWORDS = Set.of(
        "inside", "within"
    );
    
    /**
     * Keywords related to frame/iframe context.
     * Examples: iframe, frame
     */
    public static final Set<String> FRAME_KEYWORDS = Set.of(
        "iframe", "frame"
    );
    
    // ========================================
    // COMBINED SETS
    // ========================================
    
    /**
     * All prepositions and articles combined.
     * Used for removing common grammatical noise.
     */
    public static final Set<String> PREPOSITIONS_AND_ARTICLES = 
        Set.of(PREPOSITIONS, ARTICLES)
            .stream()
            .flatMap(Set::stream)
            .collect(Collectors.toUnmodifiableSet());
    
    /**
     * All auxiliary verbs and common verbs combined.
     */
    public static final Set<String> ALL_VERBS = 
        Set.of(AUXILIARY_VERBS, COMMON_VERBS)
            .stream()
            .flatMap(Set::stream)
            .collect(Collectors.toUnmodifiableSet());
    
    /**
     * All standard noise words (prepositions, articles, verbs) combined.
     * This is the primary set used for cleaning step text.
     */
    public static final Set<String> STANDARD_NOISE_WORDS = 
        Set.of(PREPOSITIONS, ARTICLES, AUXILIARY_VERBS, COMMON_VERBS)
            .stream()
            .flatMap(Set::stream)
            .collect(Collectors.toUnmodifiableSet());
    
    // ========================================
    // COMPILED PATTERNS
    // ========================================
    
    /**
     * Precompiled pattern for removing standard noise words.
     * Matches all prepositions, articles, and common verbs as whole words (case-insensitive).
     */
    public static final Pattern STANDARD_NOISE_PATTERN = buildNoisePattern(STANDARD_NOISE_WORDS);
    
    /**
     * Precompiled pattern for removing technical terms.
     * Matches technical selector terms as whole words (case-insensitive).
     */
    public static final Pattern TECHNICAL_TERMS_PATTERN = buildNoisePattern(TECHNICAL_TERMS);
    
    /**
     * Precompiled pattern for removing scoping keywords.
     * Matches scoping keywords with trailing content.
     */
    public static final Pattern SCOPING_REMOVAL_PATTERN = Pattern.compile(
        "(?i)\\b(inside|within)\\s+.+$"
    );
    
    /**
     * Precompiled pattern for removing frame keywords.
     * Matches frame references with trailing content.
     */
    public static final Pattern FRAME_REMOVAL_PATTERN = Pattern.compile(
        "(?i)\\b(in|inside)\\s+iframe\\s+.+$"
    );
    
    // ========================================
    // UTILITY METHODS
    // ========================================
    
    /**
     * Builds a regex pattern from a set of words.
     * Creates a pattern that matches any of the words as whole words (case-insensitive).
     * 
     * @param words Set of words to include in pattern
     * @return Compiled Pattern object
     */
    private static Pattern buildNoisePattern(Set<String> words) {
        String pattern = "(?i)\\b(" + String.join("|", words) + ")\\b";
        return Pattern.compile(pattern);
    }
    
    /**
     * Checks if a word is a noise word.
     * 
     * @param word Word to check
     * @return true if the word is in the standard noise words set
     */
    public static boolean isNoiseWord(String word) {
        if (word == null || word.isBlank()) {
            return false;
        }
        return STANDARD_NOISE_WORDS.contains(word.toLowerCase());
    }
    
    /**
     * Checks if a word is a technical term.
     * 
     * @param word Word to check
     * @return true if the word is a technical term
     */
    public static boolean isTechnicalTerm(String word) {
        if (word == null || word.isBlank()) {
            return false;
        }
        return TECHNICAL_TERMS.contains(word.toLowerCase());
    }
    
    /**
     * Removes all standard noise words from the given text.
     * 
     * @param text Text to clean
     * @return Cleaned text with noise words removed
     */
    public static String removeNoiseWords(String text) {
        if (text == null || text.isBlank()) {
            return text;
        }
        return STANDARD_NOISE_PATTERN.matcher(text).replaceAll(" ").trim();
    }
    
    /**
     * Removes technical terms from the given text.
     * 
     * @param text Text to clean
     * @return Cleaned text with technical terms removed
     */
    public static String removeTechnicalTerms(String text) {
        if (text == null || text.isBlank()) {
            return text;
        }
        return TECHNICAL_TERMS_PATTERN.matcher(text).replaceAll(" ").trim();
    }
    
    /**
     * Private constructor to prevent instantiation.
     * This is a utility class with only static members.
     */
    private NoiseWords() {
        throw new UnsupportedOperationException("NoiseWords is a utility class and cannot be instantiated");
    }
}
