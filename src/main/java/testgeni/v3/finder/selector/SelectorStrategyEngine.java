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
     * Finds the first strategy that can generate a unique selector for the element.
     * If a generated selector is not unique, it tries to refine it with text context.
     */
    public String generateBestSelector(ScannedElement element, List<ScannedElement> allElements) {
        for (SelectorStrategy strategy : strategies) {
            String selector = strategy.generate(element);
            if (selector == null) continue;

            // 1. If selector is already unique, return it
            if (isUnique(selector, allElements)) {
                return selector;
            }

            // 2. If not unique, try to refine it with text if available
            String refined = refineWithText(selector, element, allElements);
            if (refined != null) {
                return refined;
            }
        }

        // 3. Fallback: use tag name with index if we have to, but try to find the correct index
        int index = 0;
        if (allElements != null) {
            for (int i = 0; i < allElements.size(); i++) {
                if (allElements.get(i).id.equals(element.id)) {
                    // This is an oversimplification, but better than nothing
                    long countMatch = allElements.subList(0, i).stream()
                        .filter(el -> el.tagName.equals(element.tagName))
                        .count();
                    return element.tagName + " >> nth=" + countMatch;
                }
            }
        }
        
        return element.tagName + " >> nth=0";
    }

    private boolean isUnique(String selector, List<ScannedElement> allElements) {
        if (allElements == null || allElements.isEmpty()) return true;

        if (selector.startsWith("id=")) {
            String id = selector.substring(3);
            long count = allElements.stream()
                .filter(el -> id.equals(el.attributes.get("id")))
                .count();
            return count <= 1;
        }
        
        if (selector.startsWith("name=")) {
            String name = selector.substring(5);
            long count = allElements.stream()
                .filter(el -> name.equals(el.attributes.get("name")))
                .count();
            return count <= 1;
        }

        // For other selectors or text-based ones, assume Playwright's intelligence or uniqueness
        return true; 
    }

    private String refineWithText(String selector, ScannedElement element, List<ScannedElement> allElements) {
        if (element.text == null || element.text.trim().isEmpty()) return null;

        String refined = selector + " >> text=\"" + element.text.trim() + "\"";
        
        // Check if refined is unique (or at least better)
        // Since double-checking refined uniqueness is complex with just ScannedElements, 
        // we'll at least append it if it provides more context than the raw selector.
        return refined;
    }
}
