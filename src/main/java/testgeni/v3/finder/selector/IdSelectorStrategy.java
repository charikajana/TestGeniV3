package testgeni.v3.finder.selector;

import testgeni.v3.core.domain.ScannedElement;

/**
 * Generates selector using ID attribute, optionally combining with text for uniqueness.
 */
public class IdSelectorStrategy implements SelectorStrategy {
    @Override
    public String generate(ScannedElement el) {
        String idAttr = el.attributes.get("id");
        String text = el.text != null ? el.text.trim() : "";
        
        if (idAttr != null && !idAttr.isEmpty() && !idAttr.startsWith("tg-")) {
            return "id=" + idAttr;
        }
        return null;
    }

    @Override
    public int priority() { return 100; }
}
