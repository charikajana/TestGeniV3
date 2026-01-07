package testgeni.v3.finder;

import testgeni.v3.core.domain.ScannedElement;
import testgeni.v3.core.domain.StepIntent;
import java.util.List;

/**
 * Interface for a strategy that selects a subset of candidates from the scanned page.
 */
public interface LocatorStrategy {
    
    /**
     * Get the name of this strategy (e.g., "BY_TEXT").
     */
    String getName();

    /**
     * Filter the candidates to find those that might match the intent.
     * 
     * @param candidates All elements scanned from the page
     * @param intent The user intent
     * @return A list of potental matches
     */
    List<ScannedElement> findCandidates(List<ScannedElement> candidates, StepIntent intent);
}
