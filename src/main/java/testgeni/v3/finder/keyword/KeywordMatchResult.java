package testgeni.v3.finder.keyword;

/**
 * Represents the result of matching a single keyword against a DOM element.
 */
public class KeywordMatchResult {
    public final String keyword;
    public final double score;
    public final String matchedAttribute;
    public final String matchedValue;
    
    public KeywordMatchResult(String keyword, double score, String matchedAttribute, String matchedValue) {
        this.keyword = keyword;
        this.score = score;
        this.matchedAttribute = matchedAttribute;
        this.matchedValue = matchedValue;
    }
}
