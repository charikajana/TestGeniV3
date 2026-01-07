package testgeni.v3.finder.keyword;

import testgeni.v3.core.domain.ScannedElement;
import java.util.List;

/**
 * Represents the overall match result for an element against multiple keywords.
 */
public class ElementMatchResult {
    public final ScannedElement element;
    public final List<KeywordMatchResult> keywordMatches;
    public final double totalScore;
    public final int matchedKeywordCount;
    
    public ElementMatchResult(ScannedElement element, List<KeywordMatchResult> keywordMatches) {
        this.element = element;
        this.keywordMatches = keywordMatches;
        this.totalScore = keywordMatches.stream().mapToDouble(m -> m.score).sum();
        this.matchedKeywordCount = (int) keywordMatches.stream().filter(m -> m.score > 0).count();
    }
    
    public String getBreakdown() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("Total: %.2f (Matched %d/%d keywords) ", 
            totalScore, matchedKeywordCount, keywordMatches.size()));
        sb.append("[");
        for (KeywordMatchResult match : keywordMatches) {
            if (match.score > 0) {
                sb.append(String.format("%s:%.2f@%s, ", 
                    match.keyword, match.score, match.matchedAttribute));
            }
        }
        if (matchedKeywordCount > 0) {
            sb.setLength(sb.length() - 2); 
        }
        sb.append("]");
        return sb.toString();
    }
}
