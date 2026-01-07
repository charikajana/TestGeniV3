package testgeni.v3.finder.selector;

import testgeni.v3.core.domain.ScannedElement;

/**
 * Strategy to use the 'name' attribute, highly stable for form elements.
 */
public class NameSelectorStrategy implements SelectorStrategy {
    @Override
    public String generate(ScannedElement el) {
        String name = el.attributes.get("name");
        if (name != null && !name.isEmpty()) {
            return "[name=\"" + name + "\"]";
        }
        return null;
    }

    @Override
    public int priority() { return 85; }
}
