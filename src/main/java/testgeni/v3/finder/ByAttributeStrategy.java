package testgeni.v3.finder;

import testgeni.v3.core.domain.ScannedElement;
import testgeni.v3.core.domain.StepIntent;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Strategy that selects candidates matching data-testid, id, or name exactly.
 */
public class ByAttributeStrategy implements LocatorStrategy {
    
    @Override
    public String getName() {
        return "BY_ATTRIBUTE";
    }

    @Override
    public List<ScannedElement> findCandidates(List<ScannedElement> candidates, StepIntent intent) {
        String target = intent.target.toLowerCase().replace(" ", "-");
        String originalTarget = intent.target.toLowerCase();
        
        return candidates.stream()
            .filter(el -> {
                String id = el.attributes.getOrDefault("id", "").toLowerCase();
                String name = el.attributes.getOrDefault("name", "").toLowerCase();
                String testId = el.attributes.getOrDefault("data-testid", "").toLowerCase();
                
                return id.equals(target) || id.equals(originalTarget) ||
                       name.equals(target) || name.equals(originalTarget) ||
                       testId.equals(target) || testId.equals(originalTarget);
            })
            .collect(Collectors.toList());
    }
}
