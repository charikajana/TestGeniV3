package testgeni.v3.finder.selector;

import testgeni.v3.core.domain.ScannedElement;

/**
 * Generates selector using ARIA roles and accessible names.
 */
public class RoleSelectorStrategy implements SelectorStrategy {
    @Override
    public String generate(ScannedElement el) {
        String role = el.role;
        String text = el.text != null ? el.text.trim() : "";
        
        if (role != null && !text.isEmpty() && text.length() < 50) {
            return "role=" + role + "[name=\"" + text.replace("\"", "\\\"") + "\"]";
        }
        return null;
    }

    @Override
    public int priority() { return 80; }
}
