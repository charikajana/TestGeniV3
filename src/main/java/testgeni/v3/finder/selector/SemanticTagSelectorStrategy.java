package testgeni.v3.finder.selector;

import testgeni.v3.core.domain.ScannedElement;
import java.util.Set;

/**
 * Generates selector for specific semantic tags (label, button, a) to avoid ambiguity.
 */
public class SemanticTagSelectorStrategy implements SelectorStrategy {
    private static final Set<String> SEMANTIC_TAGS = Set.of("label", "button", "a");

    @Override
    public String generate(ScannedElement el) {
        String text = el.text != null ? el.text.trim() : "";
        if (!text.isEmpty() && text.length() < 50 && SEMANTIC_TAGS.contains(el.tagName)) {
            return el.tagName + ":has-text(\"" + text.replace("\"", "\\\"") + "\")";
        }
        return null;
    }

    @Override
    public int priority() { return 50; }
}
