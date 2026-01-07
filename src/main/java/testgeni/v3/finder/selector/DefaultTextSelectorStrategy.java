package testgeni.v3.finder.selector;

import testgeni.v3.core.domain.ScannedElement;

/**
 * Fallback selector using pure text.
 */
public class DefaultTextSelectorStrategy implements SelectorStrategy {
    @Override
    public String generate(ScannedElement el) {
        String text = el.text != null ? el.text.trim() : "";
        if (!text.isEmpty() && text.length() < 50) {
            return "text=\"" + text.replace("\"", "\\\"") + "\"";
        }
        return null;
    }

    @Override
    public int priority() { return 10; }
}
