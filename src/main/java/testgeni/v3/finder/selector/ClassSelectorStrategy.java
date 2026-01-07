package testgeni.v3.finder.selector;

import testgeni.v3.core.domain.ScannedElement;
import java.util.Set;

/**
 * Strategy to use CSS classes. Lower priority as classes often change for styling.
 */
public class ClassSelectorStrategy implements SelectorStrategy {
    @Override
    public String generate(ScannedElement el) {
        String classes = el.attributes.get("class");
        if (classes != null && !classes.isEmpty()) {
            // Use the first class if multiple exist, as it's often the primary identifier
            String primaryClass = classes.split("\\s+")[0];
            
            // BLACKLIST: Skip common generic layout classes that cause collisions
            Set<String> blacklist = Set.of(
                "row", "col", "container", "wrapper", "content", "main", "body", 
                "footer", "header", "form-control", "form-group", "col-md-", 
                "btn-primary", "btn-secondary", "active", "show", "fade"
            );
            
            boolean isBlacklisted = blacklist.stream().anyMatch(b -> primaryClass.toLowerCase().contains(b));

            if (!isBlacklisted && !primaryClass.startsWith("tg-") && primaryClass.length() > 2) {
                return "." + primaryClass;
            }
        }
        return null;
    }

    @Override
    public int priority() { return 30; }
}
