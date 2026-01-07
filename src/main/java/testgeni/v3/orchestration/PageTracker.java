package testgeni.v3.orchestration;

import com.microsoft.playwright.Page;

/**
 * Tracks the last explicitly switched page across window operations.
 * Used to ensure TestRunner uses the correct active page after window switches.
 */
public class PageTracker {
    
    private static Page lastSwitchedPage = null;
    
    /**
     * Set the last switched page (called by ContextActionExecutor).
     */
    public static void setLastSwitchedPage(Page page) {
        lastSwitchedPage = page;
    }
    
    /**
     * Get the last switched page.
     */
    public static Page getLastSwitchedPage() {
        return lastSwitchedPage;
    }
    
    /**
     * Clear the tracked page (called when returning to single window state).
     */
    public static void clear() {
        lastSwitchedPage = null;
    }
    
    /**
     * Check if the tracked page is still valid.
     */
    public static boolean isValid() {
        if (lastSwitchedPage == null) {
            return false;
        }
        try {
            // Try to access the page - will throw if closed
            lastSwitchedPage.url();
            return true;
        } catch (Exception e) {
            lastSwitchedPage = null;
            return false;
        }
    }
}
