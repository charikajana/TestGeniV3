package testgeni.v3.finder.selector;

import testgeni.v3.core.domain.ScannedElement;

/**
 * Interface for strategies that generate Playwright selectors for a scanned element.
 */
public interface SelectorStrategy {
    /**
     * Generates a Playwright selector for the given element.
     * Returns null if the strategy cannot generate a selector for this element.
     */
    String generate(ScannedElement element);
    
    /**
     * Higher priority strategies are checked first.
     */
    int priority();
}
