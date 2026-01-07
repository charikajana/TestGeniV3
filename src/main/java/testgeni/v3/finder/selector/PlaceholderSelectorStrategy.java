package testgeni.v3.finder.selector;

import testgeni.v3.core.domain.ScannedElement;

/**
 * Strategy to use 'placeholder' attribute, ideal for input fields.
 */
public class PlaceholderSelectorStrategy implements SelectorStrategy {
    @Override
    public String generate(ScannedElement el) {
        String placeholder = el.attributes.get("placeholder");
        if (placeholder != null && !placeholder.isEmpty()) {
            return "[placeholder=\"" + placeholder.replace("\"", "\\\"") + "\"]";
        }
        return null;
    }

    @Override
    public int priority() { return 80; }
}
