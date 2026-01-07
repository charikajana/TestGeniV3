package testgeni.v3.scanner;

import com.microsoft.playwright.Frame;
import com.microsoft.playwright.Page;
import testgeni.v3.core.domain.ScannedElement;
import java.util.List;

/**
 * Interface for scanning a web page or frame and extracting semantic elements.
 */
public interface PageScanner {
    
    /**
     * Scans the given page and returns a list of all semantic elements.
     * 
     * @param page The page to scan
     * @return List of scanned elements
     */
    List<ScannedElement> scan(Page page);

    /**
     * Scans a specific frame.
     * 
     * @param frame The frame to scan
     * @return List of scanned elements
     */
    List<ScannedElement> scan(Frame frame);
    
    /**
     * Scans with a specific CSS selector root.
     */
    List<ScannedElement> scan(Page page, String selector);
    
    /**
     * Clears any caches.
     */
    void refresh();
}
