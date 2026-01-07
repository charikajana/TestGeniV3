package testgeni.v3.finder.selector;

import testgeni.v3.core.domain.ScannedElement;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Manages multiple selector strategies and picks the best one for an element.
 */
public class SelectorStrategyEngine {
    
    private final List<SelectorStrategy> strategies;

    public SelectorStrategyEngine() {
        this.strategies = new ArrayList<>(List.of(
            new IdSelectorStrategy(),
            new DataTestIdSelectorStrategy(),
            new NameSelectorStrategy(),
            new PlaceholderSelectorStrategy(),
            new RoleSelectorStrategy(),
            new SemanticTagSelectorStrategy(),
            new ClassSelectorStrategy(),
            new RelativeXpathSelectorStrategy(),
            new DefaultTextSelectorStrategy()
        ));
        // Sort by priority descending
        this.strategies.sort(Comparator.comparingInt(SelectorStrategy::priority).reversed());
    }

    /**
     * Finds the first strategy that can generate a selector for the element.
     */
    public String generateBestSelector(ScannedElement element) {
        for (SelectorStrategy strategy : strategies) {
            String selector = strategy.generate(element);
            if (selector != null) {
                return selector;
            }
        }
        // Absolute fallback
        return element.tagName + " >> nth=0";
    }
}
