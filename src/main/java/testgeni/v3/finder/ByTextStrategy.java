package testgeni.v3.finder;

import testgeni.v3.core.domain.ScannedElement;
import testgeni.v3.core.domain.StepIntent;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Strategy that selects candidates containing the intent target text.
 */
public class ByTextStrategy implements LocatorStrategy {
    
    @Override
    public String getName() {
        return "BY_TEXT";
    }

    @Override
    public List<ScannedElement> findCandidates(List<ScannedElement> candidates, StepIntent intent) {
        String target = intent.target.toLowerCase();
        
        return candidates.stream()
            .filter(el -> matches(el, target))
            .collect(Collectors.toList());
    }

    private boolean matches(ScannedElement el, String target) {
        return (el.text != null && el.text.toLowerCase().contains(target)) ||
               (el.ariaLabel != null && el.ariaLabel.toLowerCase().contains(target)) ||
               (el.placeholder != null && el.placeholder.toLowerCase().contains(target)) ||
               (el.title != null && el.title.toLowerCase().contains(target)) ||
               (el.attributes.get("id") != null && el.attributes.get("id").toLowerCase().contains(target)) ||
               (el.attributes.get("name") != null && el.attributes.get("name").toLowerCase().contains(target));
    }
}
