package testgeni.v3.util;

import testgeni.v3.core.domain.ElementMatch;
import testgeni.v3.finder.keyword.ElementMatchResult;
import testgeni.v3.finder.keyword.KeywordMatchResult;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Generates transparent, human-readable explanations for AI decisions.
 * Implements the "Explainable AI (XAI)" architecture pillar.
 */
public class VisualEvidenceGenerator {

    /**
     * Generates a "Why I chose this" explanation for a matched element.
     */
    public static String generateExplanation(ElementMatch match) {
        if (match == null) return "No element was found.";

        StringBuilder sb = new StringBuilder();
        sb.append("AI Selection Evidence:\n");
        sb.append(String.format("- Picked element <%s> with ID '%s'\n", match.tagName, match.id != null ? match.id : "none"));
        sb.append(String.format("- Confidence: %.0f%%\n", match.confidence * 100));
        sb.append(String.format("- Primary Strategy: %s\n", match.strategy));
        
        if (match.scoreBreakdown != null && !match.scoreBreakdown.isEmpty()) {
            sb.append("- Scoring Breakdown: ").append(match.scoreBreakdown).append("\n");
        }

        if (match.selectionReason != null) {
            sb.append("- Rational: ").append(match.selectionReason).append("\n");
        }

        return sb.toString();
    }

    /**
     * Generates a comparison report between the winner and top alternatives.
     */
    public static String generateComparisonReport(ElementMatch winner, List<ElementMatchResult> allResults) {
        StringBuilder sb = new StringBuilder();
        sb.append("\n==========================================================\n");
        sb.append("             EXPLAINABLE AI (XAI) REPORT                 \n");
        sb.append("==========================================================\n");
        
        if (winner == null || winner.status != ElementMatch.MatchStatus.FOUND) {
            sb.append("RESOLUTION: Element Not Found.\n");
            sb.append("TOP CANDIDATES ANALYZED:\n");
            appendTopCandidates(sb, allResults, 3);
        } else {
            sb.append(String.format("RESOLUTION: Selected <%s> via %s\n", winner.tagName, winner.strategy));
            sb.append("EVIDENCE:\n");
            sb.append(String.format("  - Final Score: %.2f\n", winner.confidence));
            sb.append(String.format("  - Best Match: %s\n", winner.scoreBreakdown));
            
            if (winner.hasAlternatives()) {
                sb.append("\nDECISION LOGIC (Why this instead of others?):\n");
                sb.append("  - This element had the highest keyword density and attribute relevance.\n");
                sb.append("  - Comparison with closest competitors:\n");
                // Find results excluding the winner
                List<ElementMatchResult> losers = allResults.stream()
                    .filter(r -> !r.element.id.equals(winner.id))
                    .sorted((a, b) -> Double.compare(b.totalScore, a.totalScore))
                    .limit(2)
                    .collect(Collectors.toList());
                
                for (int i = 0; i < losers.size(); i++) {
                    ElementMatchResult loser = losers.get(i);
                    sb.append(String.format("    [%d] Candidate <%s>: Score %.2f. Reason: %s\n", 
                        i + 1, loser.element.tagName, loser.totalScore, getReasonForLosing(winner.confidence, loser)));
                }
            }
        }
        sb.append("==========================================================\n");
        return sb.toString();
    }

    private static void appendTopCandidates(StringBuilder sb, List<ElementMatchResult> results, int limit) {
        List<ElementMatchResult> top = results.stream()
            .sorted((a, b) -> Double.compare(b.totalScore, a.totalScore))
            .limit(limit)
            .collect(Collectors.toList());
        
        if (top.isEmpty()) {
            sb.append("  (No visible elements found to analyze)\n");
            return;
        }

        for (int i = 0; i < top.size(); i++) {
            ElementMatchResult r = top.get(i);
            sb.append(String.format("  [%d] <%s> ID='%s' - Score: %.2f (%s)\n", 
                i + 1, r.element.tagName, r.element.id, r.totalScore, r.getBreakdown()));
        }
    }

    private static String getReasonForLosing(double winnerScore, ElementMatchResult loser) {
        if (loser.totalScore == 0) return "No keywords matched.";
        if (winnerScore - loser.totalScore > 0.5) return "Significantly lower text relevance.";
        return "Lower overall attribute confidence.";
    }
}
