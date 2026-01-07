package testgeni.v3.finder.selector;

import testgeni.v3.core.domain.ScannedElement;

/**
 * Generates selector using data-testid or custom test attributes.
 */
public class DataTestIdSelectorStrategy implements SelectorStrategy {
    @Override
    public String generate(ScannedElement el) {
        String testId = el.attributes.get("data-testid");
        if (testId != null) return "[data-testid='" + testId + "']";
        
        // Add other common test attributes if needed
        String testId2 = el.attributes.get("test-id");
        if (testId2 != null) return "[test-id='" + testId2 + "']";
        
        return null;
    }

    @Override
    public int priority() { return 90; }
}
