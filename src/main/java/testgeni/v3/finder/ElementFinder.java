package testgeni.v3.finder;

import com.microsoft.playwright.Page;
import testgeni.v3.core.domain.ElementMatch;
import testgeni.v3.core.domain.ScannedElement;
import testgeni.v3.core.domain.StepIntent;
import java.util.List;

/**
 * Interface for finding the best matching element on a page based on a StepIntent.
 */
public interface ElementFinder {

    /**
     * Finds the best matching element for the given intent.
     */
    ElementMatch find(Page page, StepIntent intent);

    /**
     * Finds the best matching element using a pre-resolved list of scanned elements.
     * Essential for multi-frame support where elements are aggregated from multiple frames.
     */
    ElementMatch find(StepIntent intent, List<ScannedElement> elements, Page page);

    /**
     * Persistently saves a successful match to the cache for future fast-tracking.
     */
    void saveMatchToCache(Page page, StepIntent intent, ElementMatch match);
}
